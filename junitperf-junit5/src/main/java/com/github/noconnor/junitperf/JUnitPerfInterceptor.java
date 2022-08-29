package com.github.noconnor.junitperf;

import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.reporting.ReportGenerator;
import com.github.noconnor.junitperf.reporting.providers.ConsoleReportGenerator;
import com.github.noconnor.junitperf.statements.PerformanceEvaluationStatement;
import com.github.noconnor.junitperf.statements.SimpleTestStatement;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import com.github.noconnor.junitperf.statistics.providers.DescriptiveStatisticsCalculator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedHashSet;
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
    private long measurementsStartTimeMs;

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
            measurementsStartTimeMs = currentTimeMillis() + perfTestAnnotation.warmUpMs();
            boolean isAsync = invocationContext.getArguments().stream().anyMatch(arg -> arg instanceof TestContextSupplier);

            EvaluationContext context = createEvaluationContext(method, isAsync);
            context.loadConfiguration(perfTestAnnotation);
            context.loadRequirements(requirementsAnnotation);

            ACTIVE_CONTEXTS.putIfAbsent(extensionContext.getRequiredTestClass(), new LinkedHashSet<>());
            ACTIVE_CONTEXTS.get(extensionContext.getRequiredTestClass()).add(context);

            SimpleTestStatement testStatement = () -> method.invoke(
                    extensionContext.getRequiredTestInstance(),
                    invocationContext.getArguments().toArray()
            );

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
        return parameterContext.getParameter().getType() == TestContextSupplier.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return new TestContextSupplier(measurementsStartTimeMs, activeStatisticsCalculator);
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

}
