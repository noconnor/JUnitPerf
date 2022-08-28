package com.github.noconnor.junitperf;

import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.data.NoOpTestContext;
import com.github.noconnor.junitperf.data.TestContext;
import com.github.noconnor.junitperf.reporting.ReportGenerator;
import com.github.noconnor.junitperf.reporting.providers.ConsoleReportGenerator;
import com.github.noconnor.junitperf.statements.PerformanceEvaluationStatement;
import com.github.noconnor.junitperf.statements.SimpleTestStatement;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import com.github.noconnor.junitperf.statistics.providers.DescriptiveStatisticsCalculator;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
public class JUnitPerfInterceptor implements InvocationInterceptor, TestInstancePostProcessor, ParameterResolver {

    private static final ReportGenerator DEFAULT_REPORTER = new ConsoleReportGenerator();
    private static final Map<Class<?>, LinkedHashSet<EvaluationContext>> ACTIVE_CONTEXTS = new ConcurrentHashMap<>();

    private Collection<ReportGenerator> activeReporters;
    private StatisticsCalculator activeStatisticsCalculator;

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        // Will be called for every instance of @Test

        for (Field field : testInstance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(JUnitPerfTestActiveConfig.class)) {
                warnIfNonStatic(field);
                field.setAccessible(true);
                JUnitPerfReportingConfig reportingConfig = (JUnitPerfReportingConfig) field.get(testInstance);
                activeReporters = reportingConfig.getReportGenerators();
                activeStatisticsCalculator = reportingConfig.getStatisticsCalculatorSupplier().get();
            }
        }
        // Defaults if no overrides provided
        if (isNull(activeReporters) || activeReporters.isEmpty()) {
            activeReporters = singletonList(DEFAULT_REPORTER);
        }
        if (isNull(activeStatisticsCalculator)) {
            activeStatisticsCalculator = new DescriptiveStatisticsCalculator();
        }
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {
        // Will be called for every instance of @Test

        Method method = extensionContext.getRequiredTestMethod();

        JUnitPerfTest perfTestAnnotation = method.getAnnotation(JUnitPerfTest.class);
        JUnitPerfTestRequirement requirementsAnnotation = method.getAnnotation(JUnitPerfTestRequirement.class);

        if (nonNull(perfTestAnnotation)) {
            TestInvoker testInvoker = new TestInvoker(method,invocationContext.getArguments());
            testInvoker.setStatsCalculator(activeStatisticsCalculator);
            testInvoker.setMeasurementsStartTimeMs(currentTimeMillis() + perfTestAnnotation.warmUpMs());

            EvaluationContext context = createEvaluationContext(method, testInvoker.isAsyncTest());
            context.loadConfiguration(perfTestAnnotation);
            context.loadRequirements(requirementsAnnotation);

            ACTIVE_CONTEXTS.putIfAbsent(extensionContext.getRequiredTestClass(), new LinkedHashSet<>());
            ACTIVE_CONTEXTS.get(extensionContext.getRequiredTestClass()).add(context);

            SimpleTestStatement testStatement = () -> testInvoker.invoke(extensionContext.getRequiredTestInstance());

            PerformanceEvaluationStatement parallelExecution = PerformanceEvaluationStatement.builder()
                    .baseStatement(testStatement)
                    // new instance for each call to @Test
                    .statistics(activeStatisticsCalculator)
                    .context(context)
                    .listener(complete -> updateReport(method.getDeclaringClass()))
                    .build();

            parallelExecution.runParallelEvaluation();
        }
        try {
            // Must be called for framework to proceed
            invocation.proceed();
        } catch (Exception e) {
            // Ignore
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == TestContext.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return NoOpTestContext.INSTANCE;
    }

    EvaluationContext createEvaluationContext(Method method, boolean isAsync) {
        return new EvaluationContext(method.getName(), nanoTime(), isAsync);
    }


    private synchronized void updateReport(Class<?> clazz) {
        activeReporters.forEach(r -> {
            r.generateReport(ACTIVE_CONTEXTS.get(clazz));
        });
    }

    private static void warnIfNonStatic(Field field) {
        boolean isStatic = Modifier.isStatic(field.getModifiers());
        if (!isStatic) {
            log.warn("Warning: JUnitPerfTestConfig should be static or a new instance will be created for each @Test method");
        }
    }

    private static class TestInvoker {
        private final Object[] args;
        private final Method method;
        @Setter
        private long measurementsStartTimeMs;
        @Setter
        private StatisticsCalculator statsCalculator;
        private int asyncArgIndex = -1;

        public TestInvoker(Method method, List<Object> args) {
            for (int i = 0; i < args.size(); i++) {
                if (args.get(i) instanceof TestContext) {
                    asyncArgIndex = i;
                    break;
                }
            }
            this.args = args.toArray();
            this.method = method;
        }

        public boolean isAsyncTest() {
            return asyncArgIndex >= 0;
        }

        public void invoke(Object testInstance) throws InvocationTargetException, IllegalAccessException {
            if (isAsyncTest() && hasMeasurementStarted() && nonNull(statsCalculator)) {
                args[asyncArgIndex] = new TestContext(statsCalculator);
            }
            method.invoke(testInstance, args);
        }

        private boolean hasMeasurementStarted() {
            return currentTimeMillis() >= measurementsStartTimeMs;
        }

    }
}
