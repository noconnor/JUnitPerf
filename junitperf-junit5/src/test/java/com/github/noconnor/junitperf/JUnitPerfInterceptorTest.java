package com.github.noconnor.junitperf;

import com.github.noconnor.junitperf.JUnitPerfInterceptor.SharedConfig;
import com.github.noconnor.junitperf.JUnitPerfInterceptor.TestDetails;
import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.reporting.ReportGenerator;
import com.github.noconnor.junitperf.reporting.providers.ConsoleReportGenerator;
import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;
import com.github.noconnor.junitperf.statements.PerformanceEvaluationStatement;
import com.github.noconnor.junitperf.statements.PerformanceEvaluationStatement.PerformanceEvaluationStatementBuilder;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import com.github.noconnor.junitperf.statistics.providers.DescriptiveStatisticsCalculator;
import com.github.noconnor.junitperf.suite.SuiteRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.platform.suite.api.Suite;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
class JUnitPerfInterceptorTest {

    private JUnitPerfInterceptor interceptor;

    @Mock(answer = Answers.RETURNS_SELF)
    private PerformanceEvaluationStatementBuilder statementBuilderMock;

    @BeforeEach
    void setup() {
        SuiteRegistry.clearRegistry();
        JUnitPerfInterceptor.testContexts.clear();
        JUnitPerfInterceptor.sharedContexts.clear();
        interceptor = new JUnitPerfInterceptor();
    }
    
    @AfterEach
    void teardown() {
        SuiteRegistry.clearRegistry();
    }

    @Test
    void whenATestClassHasNoReportingOverrides_thenDefaultReportingConfigsShouldBeSet() throws Exception {
        ExtensionContext context = mockTestContext();
        assertNull(getSharedContext(context));
        
        interceptor.postProcessTestInstance(new SampleTestNoReportingOverrides(), getParent(context));

        SharedConfig shared = getSharedContext(context);
        
        assertTrue(shared.getStatsSupplier().get() instanceof DescriptiveStatisticsCalculator);
        assertEquals(1, shared.getActiveReporters().size());
        assertEquals(JUnitPerfInterceptor.DEFAULT_REPORTER, shared.getActiveReporters().toArray()[0]);
    }
    
    @SuppressWarnings("InstantiationOfUtilityClass")
    @Test
    void whenATestClassHasReportingOverrides_butOverridesAreMissingAnnotation_thenDefaultReportingConfigsShouldBeSet() throws Exception {
        ExtensionContext context = mockTestContext();
        assertNull(getSharedContext(context));

        interceptor.postProcessTestInstance(new SampleTestWithReportingOverridesMissingAnnotation(), getParent(context));

        SharedConfig shared = getSharedContext(context);
        assertTrue(shared.getStatsSupplier().get() instanceof DescriptiveStatisticsCalculator);
        assertEquals(1, shared.getActiveReporters().size());
        assertEquals(JUnitPerfInterceptor.DEFAULT_REPORTER, shared.getActiveReporters().toArray()[0]);
    }

    @SuppressWarnings("InstantiationOfUtilityClass")
    @Test
    void whenATestClassHasReportingOverrides_thenOverridesShouldBeAccepted() throws Exception {
        ExtensionContext context = mockTestContext();
        assertNull(getSharedContext(context));

        interceptor.postProcessTestInstance(new SampleTestWithReportingOverrides(), getParent(context));

        SharedConfig shared = getSharedContext(context);
        assertTrue(shared.getStatsSupplier().get() instanceof DescriptiveStatisticsCalculator);
        assertEquals(1, shared.getActiveReporters().size());
        assertEquals(SampleTestWithReportingOverrides.REPORTER, shared.getActiveReporters().toArray()[0]);
    }

    @SuppressWarnings("InstantiationOfUtilityClass")
    @Test
    void whenATestClassHasReportingAndStatsOverrides_thenOverridesShouldBeAccepted() throws Exception {
        ExtensionContext context = mockTestContext();
        assertNull(getSharedContext(context));

        interceptor.postProcessTestInstance(new SampleTestWithReportingAndStatisticsOverrides(), getParent(context));

        SharedConfig shared = getSharedContext(context);
        assertEquals(SampleTestWithReportingAndStatisticsOverrides.CALCULATOR, shared.getStatsSupplier().get());
        assertEquals(1, shared.getActiveReporters().size());
        assertEquals(SampleTestWithReportingAndStatisticsOverrides.REPORTER, shared.getActiveReporters().toArray()[0]);
    }

    @SuppressWarnings("unchecked")
    @Test
    void whenTestHasNotBeenAnnotatedWithPerfAnnotations_thenTestWillBeExecutedOnce() throws Throwable {
        ExtensionContext extensionContextMock = mockTestContext();
        assertNull(getSharedContext(extensionContextMock));
        
        SampleNoAnnotationsTest test = new SampleNoAnnotationsTest();

        Method methodMock = test.getClass().getMethod("someTestMethod");
        Invocation<Void> invocationMock = mock(Invocation.class);
        ReflectiveInvocationContext<Method> invocationContextMock = mock(ReflectiveInvocationContext.class);
        when(extensionContextMock.getRequiredTestMethod()).thenReturn(methodMock);

        interceptor.postProcessTestInstance(test, getParent(extensionContextMock));
        // Override statement builder
        getSharedContext(extensionContextMock).setStatementBuilder(() -> statementBuilderMock);
        interceptor.interceptTestMethod(invocationMock, invocationContextMock, extensionContextMock);

        verify(invocationMock).proceed();
        verifyNoInteractions(statementBuilderMock);
    }

    @SuppressWarnings("unchecked")
    @Test
    void whenTestHasBeenAnnotatedWithPerfAnnotations_thenTestStatementShouldBeBuilt() throws Throwable {
        ExtensionContext extensionContextMock = mockTestContext();
        assertNull(getSharedContext(extensionContextMock));
        
        SampleAnnotatedTest test = new SampleAnnotatedTest();

        Method methodMock = test.getClass().getMethod("someTestMethod");
        PerformanceEvaluationStatement statementMock = mock(PerformanceEvaluationStatement.class);
        Invocation<Void> invocationMock = mock(Invocation.class);
        ReflectiveInvocationContext<Method> invocationContextMock = mock(ReflectiveInvocationContext.class);

        when(extensionContextMock.getRequiredTestMethod()).thenReturn(methodMock);
        when(extensionContextMock.getRequiredTestClass()).thenReturn((Class) test.getClass());
        when(statementBuilderMock.build()).thenReturn(statementMock);

        interceptor.postProcessTestInstance(test, getParent(extensionContextMock));
        // Override statement builder
        getSharedContext(extensionContextMock).setStatementBuilder(() -> statementBuilderMock);
        interceptor.interceptTestMethod(invocationMock, invocationContextMock, extensionContextMock);

        verify(invocationMock).proceed();
        verify(statementMock).runParallelEvaluation();

        assertNotNull(getTestContext(extensionContextMock));
        EvaluationContext context = captureEvaluationContext();
        assertFalse(context.isAsyncEvaluation());
    }

    @SuppressWarnings("unchecked")
    @Test
    void whenTestHasBeenAnnotatedWithPerfAnnotations_thenMeasurementStartMsShouldBeCaptured() throws Throwable {
        ExtensionContext extensionContextMock = mockTestContext();
        assertNull(getSharedContext(extensionContextMock));

        SampleAnnotatedTest test = new SampleAnnotatedTest();

        Method methodMock = test.getClass().getMethod("someTestMethod");
        PerformanceEvaluationStatement statementMock = mock(PerformanceEvaluationStatement.class);
        Invocation<Void> invocationMock = mock(Invocation.class);
        ReflectiveInvocationContext<Method> invocationContextMock = mock(ReflectiveInvocationContext.class);

        when(extensionContextMock.getRequiredTestMethod()).thenReturn(methodMock);
        when(extensionContextMock.getRequiredTestClass()).thenReturn((Class) test.getClass());
        when(statementBuilderMock.build()).thenReturn(statementMock);

        interceptor.postProcessTestInstance(test, getParent(extensionContextMock));
        
        getSharedContext(extensionContextMock).setStatementBuilder(() -> statementBuilderMock);
        
        interceptor.interceptTestMethod(invocationMock, invocationContextMock, extensionContextMock);

        TestDetails testDetails = getTestContext(extensionContextMock);
        assertTrue(testDetails.getMeasurementsStartTimeMs() > 0);
        assertTrue(testDetails.getMeasurementsStartTimeMs() <= currentTimeMillis() + 100); // see warmUpMs in annotation
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void whenAsyncTestHasBeenAnnotatedWithPerfAnnotations_thenContextShouldBeMarkedAsAsync() throws Throwable {
        ExtensionContext extensionContextMock = mockTestContext();
        assertNull(getSharedContext(extensionContextMock));
        
        SampleAsyncAnnotatedTest test = new SampleAsyncAnnotatedTest();

        Method methodMock = test.getClass().getMethod("someTestMethod", TestContextSupplier.class);
        PerformanceEvaluationStatement statementMock = mock(PerformanceEvaluationStatement.class);
        Invocation<Void> invocationMock = mock(Invocation.class);
        ReflectiveInvocationContext<Method> invocationContextMock = mock(ReflectiveInvocationContext.class);

        when(invocationContextMock.getArguments()).thenReturn(mockAsyncArgs());
        when(extensionContextMock.getRequiredTestMethod()).thenReturn(methodMock);
        when(extensionContextMock.getUniqueId()).thenReturn(test.getClass().getSimpleName());
        when(statementBuilderMock.build()).thenReturn(statementMock);

        interceptor.postProcessTestInstance(test, getParent(extensionContextMock));

        getSharedContext(extensionContextMock).setStatementBuilder(() -> statementBuilderMock);

        interceptor.interceptTestMethod(invocationMock, invocationContextMock, extensionContextMock);

        EvaluationContext context = captureEvaluationContext();
        assertTrue(context.isAsyncEvaluation());
    }

    @Test
    void whenASuiteAnnotationsAreAvailable_thenSuiteAnnotationsShouldBeUsed() throws Throwable {
        ExtensionContext extensionContextMock = mockTestContext();
        assertNull(getSharedContext(extensionContextMock));

        SampleNoAnnotationsTest test = new SampleNoAnnotationsTest();
        
        Method methodMock = test.getClass().getMethod("someTestMethod");
        PerformanceEvaluationStatement statementMock = mock(PerformanceEvaluationStatement.class);
        Invocation<Void> invocationMock = mock(Invocation.class);
        ReflectiveInvocationContext<Method> invocationContextMock = mock(ReflectiveInvocationContext.class);
        mockActiveSuite(extensionContextMock, SuiteSampleTest.class);
        
        when(extensionContextMock.getRequiredTestMethod()).thenReturn(methodMock);
        when(extensionContextMock.getRequiredTestClass()).thenReturn((Class) test.getClass());
        when(statementBuilderMock.build()).thenReturn(statementMock);

        interceptor.postProcessTestInstance(test, getParent(extensionContextMock));

        getSharedContext(extensionContextMock).setStatementBuilder(() -> statementBuilderMock);

        interceptor.interceptTestMethod(invocationMock, invocationContextMock, extensionContextMock);

        TestDetails testDetails = getTestContext(extensionContextMock);

        assertTrue(testDetails.getMeasurementsStartTimeMs() > 0);
        
        assertEquals(1, testDetails.getActiveReporters().size());
        assertEquals(SuiteSampleTest.config.getReportGenerators(), testDetails.getActiveReporters());
        
        EvaluationContext context = captureEvaluationContext();
        assertEquals(100, context.getConfiguredExecutionTarget());
        assertEquals(5, context.getConfiguredThreads());
        assertEquals(1, context.getRequiredThroughput());
        
    }

    @Test
    void whenAChildClassInheritsFromABaseClassWithReportingConfig_thenChildClassShouldHaveAccessToReportingConfig() throws Throwable {
        ExtensionContext extensionContextMock = mockTestContext();
        assertNull(getSharedContext(extensionContextMock));

        SampleChildTest test = new SampleChildTest();

        Method methodMock = test.getClass().getMethod("someTestMethod");
        PerformanceEvaluationStatement statementMock = mock(PerformanceEvaluationStatement.class);
        Invocation<Void> invocationMock = mock(Invocation.class);
        ReflectiveInvocationContext<Method> invocationContextMock = mock(ReflectiveInvocationContext.class);

        when(extensionContextMock.getRequiredTestMethod()).thenReturn(methodMock);
        when(extensionContextMock.getRequiredTestClass()).thenReturn((Class) test.getClass());
        when(statementBuilderMock.build()).thenReturn(statementMock);

        interceptor.postProcessTestInstance(test, getParent(extensionContextMock));

        getSharedContext(extensionContextMock).setStatementBuilder(() -> statementBuilderMock);

        interceptor.interceptTestMethod(invocationMock, invocationContextMock, extensionContextMock);

        TestDetails testDetails = getTestContext(extensionContextMock);

        assertTrue(testDetails.getMeasurementsStartTimeMs() > 0);

        assertEquals(1, testDetails.getActiveReporters().size());
        assertEquals(SampleBaseTest.config.getReportGenerators(), testDetails.getActiveReporters());

        EvaluationContext context = captureEvaluationContext();
        assertEquals(120, context.getConfiguredExecutionTarget());
        assertEquals(15, context.getConfiguredThreads());
        assertEquals(67, context.getRequiredThroughput());

    }

    @Test
    void whenProceedThrowsAnAssertionError_thenTestShouldNotFail() throws Throwable {
        ExtensionContext extensionContextMock = mockTestContext();
        assertNull(getSharedContext(extensionContextMock));

        SampleAnnotatedTest test = new SampleAnnotatedTest();

        Method methodMock = test.getClass().getMethod("someTestMethod");
        PerformanceEvaluationStatement statementMock = mock(PerformanceEvaluationStatement.class);
        Invocation<Void> invocationMock = mock(Invocation.class);
        ReflectiveInvocationContext<Method> invocationContextMock = mock(ReflectiveInvocationContext.class);

        when(extensionContextMock.getRequiredTestMethod()).thenReturn(methodMock);
        when(extensionContextMock.getRequiredTestClass()).thenReturn((Class) test.getClass());
        when(statementBuilderMock.build()).thenReturn(statementMock);

        when(invocationMock.proceed()).thenThrow(new AssertionError());

        interceptor.postProcessTestInstance(test, getParent(extensionContextMock));

        getSharedContext(extensionContextMock).setStatementBuilder(() -> statementBuilderMock);
        
        assertDoesNotThrow(() -> {
            interceptor.interceptTestMethod(invocationMock, invocationContextMock, extensionContextMock);
        });
    }
    
    @Test
    void whenProceedThrowsAnAssertionError_andTestIsNotAPerfTest_thenTestShouldFail() throws Throwable {
        ExtensionContext extensionContextMock = mockTestContext();
        assertNull(getSharedContext(extensionContextMock));

        SampleNoAnnotationsTest test = new SampleNoAnnotationsTest();

        Method methodMock = test.getClass().getMethod("someTestMethod");
        PerformanceEvaluationStatement statementMock = mock(PerformanceEvaluationStatement.class);
        Invocation<Void> invocationMock = mock(Invocation.class);
        ReflectiveInvocationContext<Method> invocationContextMock = mock(ReflectiveInvocationContext.class);

        when(extensionContextMock.getRequiredTestMethod()).thenReturn(methodMock);
        when(extensionContextMock.getRequiredTestClass()).thenReturn((Class) test.getClass());
        when(statementBuilderMock.build()).thenReturn(statementMock);

        when(invocationMock.proceed()).thenThrow(new AssertionError());

        interceptor.postProcessTestInstance(test, getParent(extensionContextMock));

        getSharedContext(extensionContextMock).setStatementBuilder(() -> statementBuilderMock);

        assertThrows(AssertionError.class, () -> {
            interceptor.interceptTestMethod(invocationMock, invocationContextMock, extensionContextMock);
        });
    }

    @Test
    void whenReportingConfigIsNonStatic_thenPostProcessTestInstanceShouldThrowAnException() throws Throwable {
        SampleNonStaticReporterConfigTest test = new SampleNonStaticReporterConfigTest();

        Method methodMock = test.getClass().getMethod("someTestMethod");
        ExtensionContext extensionContextMock = mockTestContext();

        when(extensionContextMock.getRequiredTestMethod()).thenReturn(methodMock);
        when(extensionContextMock.getRequiredTestClass()).thenReturn((Class) test.getClass());
        
        assertThrows(IllegalStateException.class, () -> interceptor.postProcessTestInstance(test, getParent(extensionContextMock)));
    }

    @Test
    void whenInterceptorSupportsParameterIsCalled_thenParameterTypeShouldBeChecked() throws NoSuchMethodException {
        assertTrue(interceptor.supportsParameter(mockTestContextSupplierParameterType(), null));
        assertFalse(interceptor.supportsParameter(mockStringParameterType(), null));
    }

    @Test
    void whenInterceptorResolveParameterIsCalled_thenTestContextSupplierShouldBeReturned() throws Exception {
        ExtensionContext extensionContextMock = mockTestContext();
        interceptor.postProcessTestInstance(this, getParent(extensionContextMock));
        assertTrue(interceptor.resolveParameter(null, extensionContextMock) instanceof TestContextSupplier);
    }
    
    private static void mockActiveSuite(ExtensionContext testMethodContext, Class<?> suiteClass) {
        ExtensionContext suiteRoot = mock(ExtensionContext.class);
        ExtensionContext parent = testMethodContext.getParent().get();
        when(parent.getRoot()).thenReturn(suiteRoot);
        when(testMethodContext.getRoot()).thenReturn(suiteRoot);
        when(suiteRoot.getUniqueId()).thenReturn(buildSuiteId(suiteClass));
    }

    private static String buildSuiteId(Class<?> clazz) {
        return "[engine:junit-platform-suite]/[suite:" + clazz.getName() + "]/[engine:junit-jupiter]";
    }

    private static ParameterContext mockTestContextSupplierParameterType() throws NoSuchMethodException {
        Method methodMock = SampleAsyncAnnotatedTest.class.getMethod("someTestMethod", TestContextSupplier.class);
        Parameter param = methodMock.getParameters()[0];
        ParameterContext parameterContextMock = mock(ParameterContext.class);
        when(parameterContextMock.getParameter()).thenReturn(param);
        return parameterContextMock;
    }

    private static ParameterContext mockStringParameterType() throws NoSuchMethodException {
        Method methodMock = SampleAsyncAnnotatedTest.class.getMethod("someOtherTestMethod", String.class);
        Parameter param = methodMock.getParameters()[0];
        ParameterContext parameterContextMock = mock(ParameterContext.class);
        when(parameterContextMock.getParameter()).thenReturn(param);
        return parameterContextMock;
    }

    private static List<Object> mockAsyncArgs() {
        return singletonList(new TestContextSupplier(0, null));
    }

    private EvaluationContext captureEvaluationContext() {
        ArgumentCaptor<EvaluationContext> captor = ArgumentCaptor.forClass(EvaluationContext.class);
        verify(statementBuilderMock).context(captor.capture());
        EvaluationContext context = captor.getValue();
        return context;
    }

    private static ExtensionContext mockTestContext() {
        ExtensionContext parent = mock(ExtensionContext.class);
        ExtensionContext test = mock(ExtensionContext.class);
        when(test.getUniqueId()).thenReturn("test:" + ThreadLocalRandom.current().nextInt());
        when(parent.getUniqueId()).thenReturn("class:" + ThreadLocalRandom.current().nextInt());
        when(test.getParent()).thenReturn(Optional.of(parent));
        return test;
    }

    private static SharedConfig getSharedContext(ExtensionContext context) {
        return JUnitPerfInterceptor.sharedContexts.get(context.getParent().get().getUniqueId());
    }

    private static TestDetails getTestContext(ExtensionContext context) {
        return JUnitPerfInterceptor.testContexts.get(context.getUniqueId());
    }

    private static ExtensionContext getParent(ExtensionContext extensionContextMock) {
        return extensionContextMock.getParent().get();
    }


    public static class SampleTestNoReportingOverrides {
    }

    public static class SampleTestWithReportingOverrides {
        public final static ReportGenerator REPORTER = new HtmlReportGenerator();
        @JUnitPerfTestActiveConfig
        private final static JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
                .reportGenerator(REPORTER)
                .build();
    }

    public static class SampleTestWithReportingAndStatisticsOverrides {
        public final static ReportGenerator REPORTER = new HtmlReportGenerator();
        public final static StatisticsCalculator CALCULATOR = mock(StatisticsCalculator.class);
        @JUnitPerfTestActiveConfig
        private final static JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
                .reportGenerator(REPORTER)
                .statisticsCalculatorSupplier(() -> CALCULATOR)
                .build();
    }

    public static class SampleTestWithReportingOverridesMissingAnnotation {
        public final static ReportGenerator REPORTER = new HtmlReportGenerator();
        private final static JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
                .reportGenerator(REPORTER)
                .build();
    }

    @Disabled
    public static class SampleNoAnnotationsTest {
        @Test
        public void someTestMethod() {
            assertTrue(true);
        }
    }

    @Disabled
    public static class SampleAnnotatedTest {
        @Test
        @JUnitPerfTest(threads = 1, durationMs = 1_000, maxExecutionsPerSecond = 1_000, warmUpMs = 100)
        public void someTestMethod() {
            assertTrue(true);
        }
    }

    
    @Disabled
    public static class SampleAsyncAnnotatedTest {
        @Test
        @JUnitPerfTest(threads = 1, durationMs = 1_000, maxExecutionsPerSecond = 1_000, warmUpMs = 100)
        public void someTestMethod(TestContextSupplier contextSupplier) {
            assertTrue(true);
        }

        @Test
        public void someOtherTestMethod(String param) {
            assertTrue(true);
        }
    }

    @Disabled
    @Suite
    @JUnitPerfTest( threads = 5, totalExecutions = 100)
    @JUnitPerfTestRequirement(executionsPerSec = 1)
    public static class SuiteSampleTest {
        @JUnitPerfTestActiveConfig
        public static JUnitPerfReportingConfig config = JUnitPerfReportingConfig.builder()
                .reportGenerator(new ConsoleReportGenerator())
                .build();
    }

    public static class SampleBaseTest {
        @JUnitPerfTestActiveConfig 
        public static final JUnitPerfReportingConfig config = JUnitPerfReportingConfig.builder()
                .reportGenerator(new HtmlReportGenerator())
                .build();
    }

    @Disabled
    @JUnitPerfTest( threads = 15, totalExecutions = 120)
    @JUnitPerfTestRequirement(executionsPerSec = 67)
    public static class SampleChildTest extends SampleBaseTest {
        @Test
        public void someTestMethod() {
            assertTrue(true);
        }
    }

    @Disabled
    public static class SampleNonStaticReporterConfigTest {
        @JUnitPerfTestActiveConfig
        public final JUnitPerfReportingConfig config = JUnitPerfReportingConfig.builder()
                .reportGenerator(new HtmlReportGenerator())
                .build();

        @Test
        public void someTestMethod() {
            assertTrue(true);
        }
    }
    
}