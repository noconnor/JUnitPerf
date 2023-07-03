package com.github.noconnor.junitperf;

import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.reporting.ReportGenerator;
import com.github.noconnor.junitperf.reporting.providers.ConsoleReportGenerator;
import com.github.noconnor.junitperf.statements.FullStatement;
import com.github.noconnor.junitperf.statements.ExceptionsRegistry;
import com.github.noconnor.junitperf.statements.PerformanceEvaluationStatement;
import com.github.noconnor.junitperf.statements.PerformanceEvaluationStatement.PerformanceEvaluationStatementBuilder;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import com.github.noconnor.junitperf.statistics.providers.DescriptiveStatisticsCalculator;
import com.github.noconnor.junitperf.suite.SuiteRegistry;
import com.github.noconnor.junitperf.utils.TestReflectionUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.opentest4j.TestAbortedException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
public class JUnitPerfInterceptor implements InvocationInterceptor, TestInstancePostProcessor, ParameterResolver {

    protected static final ReportGenerator DEFAULT_REPORTER = new ConsoleReportGenerator();
    protected static final Map<String, TestDetails> testContexts = new ConcurrentHashMap<>();
    protected static final Map<String, SharedConfig> sharedContexts = new ConcurrentHashMap<>();

    static {
        ExceptionsRegistry.registerIgnorable(InterruptedException.class);
        ExceptionsRegistry.registerAbort(TestAbortedException.class);
    }

    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    protected static class TestDetails {
        @EqualsAndHashCode.Include
        private Class<?> testClass;
        @EqualsAndHashCode.Include
        private Method testMethod;
        private long measurementsStartTimeMs;
        private EvaluationContext context;
        private StatisticsCalculator statsCalculator;
        private Collection<ReportGenerator> activeReporters;
        private PerformanceEvaluationStatementBuilder statementBuilder;
    }

    @Data
    protected static class SharedConfig {
        private Collection<ReportGenerator> activeReporters = singletonList(DEFAULT_REPORTER);
        private Supplier<StatisticsCalculator> statsSupplier = DescriptiveStatisticsCalculator::new;
        private Supplier<PerformanceEvaluationStatementBuilder> statementBuilder = PerformanceEvaluationStatement::builder;
    }

    @Override
    public synchronized void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        if (sharedContexts.containsKey(context.getUniqueId())) {
            log.info("Test already configured");
            return;
        }

        SharedConfig test = new SharedConfig();
        SuiteRegistry.scanForSuiteDetails(context);
        JUnitPerfReportingConfig reportingConfig = findTestActiveConfigField(testInstance, context);
        if (nonNull(reportingConfig)) {
            test.setActiveReporters(reportingConfig.getReportGenerators());
            test.setStatsSupplier(reportingConfig.getStatisticsCalculatorSupplier());
        }
        sharedContexts.put(context.getUniqueId(), test);
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {


        // Will be called for every instance of @Test
        Method method = extensionContext.getRequiredTestMethod();
        Object testInstance = extensionContext.getRequiredTestInstance();

        JUnitPerfTest perfTestAnnotation = getJUnitPerfTestDetails(method, extensionContext);
        JUnitPerfTestRequirement requirementsAnnotation = getJUnitPerfTestRequirementDetails(method, extensionContext);

        if (nonNull(perfTestAnnotation)) {
            log.trace("Using {} for {} : {}", perfTestAnnotation, getUniqueId(extensionContext), getUniqueId(extensionContext.getRoot()));
            
            boolean isAsync = invocationContext.getArguments().stream().anyMatch(arg -> arg instanceof TestContextSupplier);
            EvaluationContext context = createEvaluationContext(method, isAsync);
            context.loadConfiguration(perfTestAnnotation);
            context.loadRequirements(requirementsAnnotation);

            TestDetails test = getTestDetails(extensionContext);
            test.setTestClass(method.getDeclaringClass());
            test.setTestMethod(method);
            test.setMeasurementsStartTimeMs(currentTimeMillis() + perfTestAnnotation.warmUpMs());
            test.setContext(context);

            FullStatement testStatement = new FullStatement(testInstance, method, invocationContext.getArguments());
            testStatement.setBeforeEach(TestReflectionUtils.findBeforeEach(testInstance));
            testStatement.setAfterEach(TestReflectionUtils.findAfterEach(testInstance));

            PerformanceEvaluationStatement parallelExecution = test.getStatementBuilder()
                    .baseStatement(testStatement)
                    .statistics(test.getStatsCalculator())
                    .context(context)
                    .listener(complete -> updateReport(test))
                    .build();

            parallelExecution.runParallelEvaluation();

            // Must be called for framework to proceed
            invocation.skip();

        } else {
            log.trace("No @JUnitPerfTest annotation for {} : {}", getUniqueId(extensionContext), getUniqueId(extensionContext.getRoot()));
            invocation.proceed();
        }

    }
    
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == TestContextSupplier.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        TestDetails test = getTestDetails(extensionContext);
        return new TestContextSupplier(test.getMeasurementsStartTimeMs(), test.getStatsCalculator());
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
        ctx.setGroupName(method.getDeclaringClass().getName());
        return ctx;
    }

    private synchronized void updateReport(TestDetails test) {
        test.getActiveReporters().forEach(r -> {
            LinkedHashSet<EvaluationContext> ctxt = new LinkedHashSet<>();
            ctxt.add(test.getContext());
            r.generateReport(ctxt);
        });
    }

    private static void failIfNonStatic(Field field) {
        boolean isStatic = Modifier.isStatic(field.getModifiers());
        if (!isStatic) {
            throw new IllegalStateException("JUnitPerfTestConfig should be static ");
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
                failIfNonStatic(field);
                field.setAccessible(true);
                return (JUnitPerfReportingConfig) field.get(testInstance);
            }
        }
        return scanForReportingConfig(testInstance, testClass.getSuperclass());
    }

    private static String getUniqueId(ExtensionContext extensionContext) {
        return nonNull(extensionContext) ? extensionContext.getUniqueId() : "(no root)";
    }

    private static TestDetails getTestDetails(ExtensionContext extensionContext) {
        String testId = extensionContext.getUniqueId();
        testContexts.computeIfAbsent(testId, newTestId -> {
            String parentId = extensionContext.getParent().map(ExtensionContext::getUniqueId).orElse("");
            SharedConfig parentDetails = sharedContexts.getOrDefault(parentId, new SharedConfig());
            TestDetails testDetails = new TestDetails();
            testDetails.setStatementBuilder(parentDetails.getStatementBuilder().get());
            testDetails.setActiveReporters(parentDetails.getActiveReporters());
            testDetails.setStatsCalculator(parentDetails.getStatsSupplier().get());
            return testDetails;
        });
        return testContexts.get(testId);
    }

}
