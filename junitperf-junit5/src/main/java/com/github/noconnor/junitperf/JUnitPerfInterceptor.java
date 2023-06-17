package com.github.noconnor.junitperf;

import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.reporting.ReportGenerator;
import com.github.noconnor.junitperf.reporting.providers.ConsoleReportGenerator;
import com.github.noconnor.junitperf.statements.PerformanceEvaluationStatement;
import com.github.noconnor.junitperf.statements.PerformanceEvaluationStatement.PerformanceEvaluationStatementBuilder;
import com.github.noconnor.junitperf.statements.SimpleTestStatement;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import com.github.noconnor.junitperf.statistics.providers.DescriptiveStatisticsCalculator;
import com.github.noconnor.junitperf.suite.SuiteRegistry;
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
    protected static final Map<String, LinkedHashSet<EvaluationContext>> ACTIVE_CONTEXTS = new ConcurrentHashMap<>();

    protected Collection<ReportGenerator> activeReporters;
    protected StatisticsCalculator activeStatisticsCalculator;
    protected long measurementsStartTimeMs;
    protected PerformanceEvaluationStatementBuilder statementBuilder;


    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {

        SuiteRegistry.register(context);

        JUnitPerfReportingConfig reportingConfig = findTestActiveConfigField(testInstance, context);

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

        JUnitPerfTest perfTestAnnotation = getJUnitPerfTestDetails(method, extensionContext);
        JUnitPerfTestRequirement requirementsAnnotation = getJUnitPerfTestRequirementDetails(method, extensionContext);

        if (nonNull(perfTestAnnotation)) {
            measurementsStartTimeMs = currentTimeMillis() + perfTestAnnotation.warmUpMs();
            boolean isAsync = invocationContext.getArguments().stream().anyMatch(arg -> arg instanceof TestContextSupplier);

            EvaluationContext context = createEvaluationContext(method, isAsync);
            context.loadConfiguration(perfTestAnnotation);
            context.loadRequirements(requirementsAnnotation);

            ACTIVE_CONTEXTS.putIfAbsent(extensionContext.getUniqueId(), new LinkedHashSet<>());
            ACTIVE_CONTEXTS.get(extensionContext.getUniqueId()).add(context);

            SimpleTestStatement testStatement = () -> method.invoke(
                    extensionContext.getRequiredTestInstance(),
                    invocationContext.getArguments().toArray()
            );

            PerformanceEvaluationStatement parallelExecution = statementBuilder
                    .baseStatement(testStatement)
                    .statistics(activeStatisticsCalculator)
                    .context(context)
                    .listener(complete -> updateReport(extensionContext))
                    .build();

            parallelExecution.runParallelEvaluation();

            proceedQuietly(invocation);

        } else {
            invocation.proceed();
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

    protected JUnitPerfTestRequirement getJUnitPerfTestRequirementDetails(Method method, ExtensionContext ctxt) {
        JUnitPerfTestRequirement methodAnnotation = method.getAnnotation(JUnitPerfTestRequirement.class);
        JUnitPerfTestRequirement classAnnotation = method.getDeclaringClass().getAnnotation(JUnitPerfTestRequirement.class);
        JUnitPerfTestRequirement suiteAnnotation = SuiteRegistry.getPerfRequirements(ctxt);
        // Precedence: method, then class, then suite
        JUnitPerfTestRequirement specifiedAnnotation = nonNull(methodAnnotation) ? methodAnnotation : classAnnotation;
        return nonNull(specifiedAnnotation) ? specifiedAnnotation : suiteAnnotation;
    }

    protected JUnitPerfTest getJUnitPerfTestDetails(Method method, ExtensionContext ctxt) {
        JUnitPerfTest methodAnnotation = method.getAnnotation(JUnitPerfTest.class);
        JUnitPerfTest classAnnotation = method.getDeclaringClass().getAnnotation(JUnitPerfTest.class);
        JUnitPerfTest suiteAnnotation = SuiteRegistry.getPerfTestData(ctxt);
        // Precedence: method, then class, then suite
        JUnitPerfTest specifiedAnnotation = nonNull(methodAnnotation) ? methodAnnotation : classAnnotation;
        return nonNull(specifiedAnnotation) ? specifiedAnnotation : suiteAnnotation;
    }

    protected EvaluationContext createEvaluationContext(Method method, boolean isAsync) {
        EvaluationContext ctx = new EvaluationContext(method.getName(), nanoTime(), isAsync);
        ctx.setGroupName(method.getDeclaringClass().getSimpleName());
        return ctx;
    }

    private synchronized void updateReport(ExtensionContext ctxt) {
        activeReporters.forEach(r -> {
            r.generateReport(ACTIVE_CONTEXTS.get(ctxt.getUniqueId()));
        });
    }

    private static void warnIfNonStatic(Field field) {
        boolean isStatic = Modifier.isStatic(field.getModifiers());
        if (!isStatic) {
            log.warn("Warning: JUnitPerfTestConfig should be static or a new instance will be created for each @Test method");
        }
    }

    private static JUnitPerfReportingConfig findTestActiveConfigField(Object testInstance, ExtensionContext ctxt) throws IllegalAccessException {
        Class<?> testClass = testInstance.getClass();
        JUnitPerfReportingConfig config = scanForReportingConfig(testInstance, testClass);
        return isNull(config) ? SuiteRegistry.getReportingConfig(ctxt) : config;
    }

    private static JUnitPerfReportingConfig scanForReportingConfig(Object testInstance, Class<?> testClass) throws IllegalAccessException {
        if (isNull(testClass)) {
            return null;
        }
        for (Field field : testClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(JUnitPerfTestActiveConfig.class)) {
                warnIfNonStatic(field);
                field.setAccessible(true);
                return (JUnitPerfReportingConfig) field.get(testInstance);
            }
        }
        return scanForReportingConfig(testInstance, testClass.getSuperclass());
    }

    private static void proceedQuietly(Invocation<Void> invocation) throws Throwable {
        try {
            // Must be called for framework to proceed
            invocation.proceed();
        } catch (Throwable e) {
            // Ignore
        }
    }

}
