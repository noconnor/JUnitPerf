package com.github.noconnor.junitperf;

import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.reporting.ReportGenerator;
import com.github.noconnor.junitperf.reporting.providers.ConsoleReportGenerator;
import com.github.noconnor.junitperf.statements.PerformanceEvaluationStatement;
import com.github.noconnor.junitperf.statements.PerformanceEvaluationStatement.PerformanceEvaluationStatementBuilder;
import com.github.noconnor.junitperf.statements.SimpleTestStatement;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import com.github.noconnor.junitperf.statistics.providers.DescriptiveStatisticsCalculator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

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

    protected static final ReportGenerator DEFAULT_REPORTER = new ConsoleReportGenerator();
    protected static final Map<Class<?>, LinkedHashSet<EvaluationContext>> ACTIVE_CONTEXTS = new ConcurrentHashMap<>();

    protected Collection<ReportGenerator> activeReporters;
    protected StatisticsCalculator activeStatisticsCalculator;
    protected long measurementsStartTimeMs;
    protected PerformanceEvaluationStatementBuilder statementBuilder;


    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {

        JUnitPerfReportingConfig reportingConfig = findTestActiveConfigField(testInstance);
        if (nonNull(reportingConfig)) {
            activeReporters = reportingConfig.getReportGenerators();
            activeStatisticsCalculator = reportingConfig.getStatisticsCalculatorSupplier().get();
        }

        // Defaults if no overrides provided
        if (isNull(activeReporters) || activeReporters.isEmpty()) {
            activeReporters = singletonList(DEFAULT_REPORTER);
        }
        if (isNull(activeStatisticsCalculator)) {
            activeStatisticsCalculator = new DescriptiveStatisticsCalculator();
        }
        statementBuilder = PerformanceEvaluationStatement.builder();
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {
        // Will be called for every instance of @Test

        Method method = extensionContext.getRequiredTestMethod();

        JUnitPerfTest perfTestAnnotation = getJUnitPerfTestDetails(method);
        JUnitPerfTestRequirement requirementsAnnotation = getJUnitPerfTestRequirementDetails(method);

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

            PerformanceEvaluationStatement parallelExecution = statementBuilder
                    .baseStatement(testStatement)
                    .statistics(activeStatisticsCalculator)
                    .context(context)
                    .listener(complete -> updateReport(method))
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


    protected JUnitPerfTestRequirement getJUnitPerfTestRequirementDetails(Method method) {
        JUnitPerfTestRequirement methodAnnotation = method.getAnnotation(JUnitPerfTestRequirement.class);
        JUnitPerfTestRequirement registeredAnnotation = JUnitPerfTestRegistry.getPerfRequirements(method.getDeclaringClass());
        return nonNull(methodAnnotation) ? methodAnnotation : registeredAnnotation;
    }

    protected JUnitPerfTest getJUnitPerfTestDetails(Method method) {
        JUnitPerfTest methodAnnotation = method.getAnnotation(JUnitPerfTest.class);
        JUnitPerfTest registeredAnnotation = JUnitPerfTestRegistry.getPerfTestData(method.getDeclaringClass());
        return nonNull(methodAnnotation) ? methodAnnotation : registeredAnnotation;
    }

    protected EvaluationContext createEvaluationContext(Method method, boolean isAsync) {
        EvaluationContext ctx =  new EvaluationContext(method.getName(), nanoTime(), isAsync);
        ctx.setGroupName(method.getDeclaringClass().getSimpleName());
        return ctx;
    }

    private synchronized void updateReport(Method method) {
        activeReporters.forEach(r -> {
            r.generateReport(ACTIVE_CONTEXTS.get(method.getDeclaringClass()));
        });
    }

    private static void warnIfNonStatic(Field field) {
        boolean isStatic = Modifier.isStatic(field.getModifiers());
        if (!isStatic) {
            log.warn("Warning: JUnitPerfTestConfig should be static or a new instance will be created for each @Test method");
        }
    }

    private static JUnitPerfReportingConfig findTestActiveConfigField(Object testInstance) throws IllegalAccessException {
        for (Field field : testInstance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(JUnitPerfTestActiveConfig.class)) {
                warnIfNonStatic(field);
                field.setAccessible(true);
                return (JUnitPerfReportingConfig) field.get(testInstance);
            }
        }
        return JUnitPerfTestRegistry.getReportingConfig(testInstance.getClass());
    }

}
