package com.github.noconnor.junitperf;

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
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
class JUnitPerfInterceptorTest {

    private JUnitPerfInterceptor interceptor;

    @Mock(answer = Answers.RETURNS_SELF)
    private PerformanceEvaluationStatementBuilder statementBuilderMock;

    @BeforeEach
    void setup() {
        SuiteRegistry.clearRegistry();
        JUnitPerfInterceptor.ACTIVE_CONTEXTS.clear();
        interceptor = new JUnitPerfInterceptor();
    }
    
    @AfterEach
    void teardown() {
        SuiteRegistry.clearRegistry();
    }

    @Test
    void whenATestClassHasNoReportingOverrides_thenDefaultReportingConfigsShouldBeSet() throws Exception {
        assertNull(interceptor.activeStatisticsCalculator);
        assertNull(interceptor.activeReporters);

        interceptor.postProcessTestInstance(new SampleTestNoReportingOverrides(), null);

        assertTrue(interceptor.activeStatisticsCalculator instanceof DescriptiveStatisticsCalculator);
        assertEquals(1, interceptor.activeReporters.size());
        assertEquals(JUnitPerfInterceptor.DEFAULT_REPORTER, interceptor.activeReporters.toArray()[0]);
    }

    @SuppressWarnings("InstantiationOfUtilityClass")
    @Test
    void whenATestClassHasReportingOverrides_butOverridesAreMissingAnnotation_thenDefaultReportingConfigsShouldBeSet() throws Exception {
        assertNull(interceptor.activeStatisticsCalculator);
        assertNull(interceptor.activeReporters);

        interceptor.postProcessTestInstance(new SampleTestWithReportingOverridesMissingAnnotation(), null);

        assertTrue(interceptor.activeStatisticsCalculator instanceof DescriptiveStatisticsCalculator);
        assertEquals(1, interceptor.activeReporters.size());
        assertEquals(JUnitPerfInterceptor.DEFAULT_REPORTER, interceptor.activeReporters.toArray()[0]);
    }

    @SuppressWarnings("InstantiationOfUtilityClass")
    @Test
    void whenATestClassHasReportingOverrides_thenOverridesShouldBeAccepted() throws Exception {
        assertNull(interceptor.activeStatisticsCalculator);
        assertNull(interceptor.activeReporters);

        interceptor.postProcessTestInstance(new SampleTestWithReportingOverrides(), null);

        assertTrue(interceptor.activeStatisticsCalculator instanceof DescriptiveStatisticsCalculator);
        assertEquals(1, interceptor.activeReporters.size());
        assertEquals(SampleTestWithReportingOverrides.REPORTER, interceptor.activeReporters.toArray()[0]);
    }

    @SuppressWarnings("InstantiationOfUtilityClass")
    @Test
    void whenATestClassHasReportingAndStatsOverrides_thenOverridesShouldBeAccepted() throws Exception {
        assertNull(interceptor.activeStatisticsCalculator);
        assertNull(interceptor.activeReporters);

        interceptor.postProcessTestInstance(new SampleTestWithReportingAndStatisticsOverrides(), null);

        assertEquals(SampleTestWithReportingAndStatisticsOverrides.CALCULATOR, interceptor.activeStatisticsCalculator);
        assertEquals(1, interceptor.activeReporters.size());
        assertEquals(SampleTestWithReportingAndStatisticsOverrides.REPORTER, interceptor.activeReporters.toArray()[0]);
    }


    @SuppressWarnings("unchecked")
    @Test
    void whenTestHasNotBeenAnnotatedWithPerfAnnotations_thenTestWillBeExecutedOnce() throws Throwable {
        SampleNoAnnotationsTest test = new SampleNoAnnotationsTest();

        Method methodMock = test.getClass().getMethod("someTestMethod");
        Invocation<Void> invocationMock = mock(Invocation.class);
        ReflectiveInvocationContext<Method> invocationContextMock = mock(ReflectiveInvocationContext.class);
        ExtensionContext extensionContextMock = mockTestContext();
        when(extensionContextMock.getRequiredTestMethod()).thenReturn(methodMock);

        interceptor.postProcessTestInstance(test, null);
        interceptor.statementBuilder = statementBuilderMock;
        interceptor.interceptTestMethod(invocationMock, invocationContextMock, extensionContextMock);

        verify(invocationMock).proceed();
        verifyNoInteractions(statementBuilderMock);
    }

    @SuppressWarnings("unchecked")
    @Test
    void whenTestHasBeenAnnotatedWithPerfAnnotations_thenTestStatementShouldBeBuilt() throws Throwable {
        SampleAnnotatedTest test = new SampleAnnotatedTest();

        Method methodMock = test.getClass().getMethod("someTestMethod");
        PerformanceEvaluationStatement statementMock = mock(PerformanceEvaluationStatement.class);
        Invocation<Void> invocationMock = mock(Invocation.class);
        ReflectiveInvocationContext<Method> invocationContextMock = mock(ReflectiveInvocationContext.class);
        ExtensionContext extensionContextMock = mockTestContext();

        when(extensionContextMock.getRequiredTestMethod()).thenReturn(methodMock);
        when(extensionContextMock.getRequiredTestClass()).thenReturn((Class) test.getClass());
        when(statementBuilderMock.build()).thenReturn(statementMock);

        interceptor.postProcessTestInstance(test, null);
        interceptor.statementBuilder = statementBuilderMock;
        interceptor.interceptTestMethod(invocationMock, invocationContextMock, extensionContextMock);

        verify(invocationMock).proceed();
        verify(statementMock).runParallelEvaluation();

        assertEquals(1, JUnitPerfInterceptor.ACTIVE_CONTEXTS.get(extensionContextMock.getUniqueId()).size());
        EvaluationContext context = captureEvaluationContext();
        assertFalse(context.isAsyncEvaluation());
    }

    @SuppressWarnings("unchecked")
    @Test
    void whenTestHasBeenAnnotatedWithPerfAnnotations_thenMeasurementStartMsShouldBeCaptured() throws Throwable {
        SampleAnnotatedTest test = new SampleAnnotatedTest();

        Method methodMock = test.getClass().getMethod("someTestMethod");
        PerformanceEvaluationStatement statementMock = mock(PerformanceEvaluationStatement.class);
        Invocation<Void> invocationMock = mock(Invocation.class);
        ReflectiveInvocationContext<Method> invocationContextMock = mock(ReflectiveInvocationContext.class);
        ExtensionContext extensionContextMock = mockTestContext();

        when(extensionContextMock.getRequiredTestMethod()).thenReturn(methodMock);
        when(extensionContextMock.getRequiredTestClass()).thenReturn((Class) test.getClass());
        when(statementBuilderMock.build()).thenReturn(statementMock);

        interceptor.postProcessTestInstance(test, null);
        interceptor.statementBuilder = statementBuilderMock;
        interceptor.interceptTestMethod(invocationMock, invocationContextMock, extensionContextMock);

        assertTrue(interceptor.measurementsStartTimeMs > 0);
        assertTrue(interceptor.measurementsStartTimeMs <= currentTimeMillis() + 100); // see warmUpMs in annotation
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void whenAsyncTestHasBeenAnnotatedWithPerfAnnotations_thenContextShouldBeMarkedAsAsync() throws Throwable {
        SampleAsyncAnnotatedTest test = new SampleAsyncAnnotatedTest();

        Method methodMock = test.getClass().getMethod("someTestMethod", TestContextSupplier.class);
        PerformanceEvaluationStatement statementMock = mock(PerformanceEvaluationStatement.class);
        Invocation<Void> invocationMock = mock(Invocation.class);
        ReflectiveInvocationContext<Method> invocationContextMock = mock(ReflectiveInvocationContext.class);
        ExtensionContext extensionContextMock = mockTestContext();

        when(invocationContextMock.getArguments()).thenReturn(mockAsyncArgs());
        when(extensionContextMock.getRequiredTestMethod()).thenReturn(methodMock);
        when(extensionContextMock.getUniqueId()).thenReturn(test.getClass().getSimpleName());
        when(statementBuilderMock.build()).thenReturn(statementMock);

        interceptor.postProcessTestInstance(test, null);
        interceptor.statementBuilder = statementBuilderMock;
        interceptor.interceptTestMethod(invocationMock, invocationContextMock, extensionContextMock);

        EvaluationContext context = captureEvaluationContext();
        assertTrue(context.isAsyncEvaluation());
    }

    @Test
    void whenASuiteAnnotationsAreAvailable_thenSuiteAnnotationsShouldBeUsed() throws Throwable {
        SampleNoAnnotationsTest test = new SampleNoAnnotationsTest();
        
        Method methodMock = test.getClass().getMethod("someTestMethod");
        PerformanceEvaluationStatement statementMock = mock(PerformanceEvaluationStatement.class);
        Invocation<Void> invocationMock = mock(Invocation.class);
        ReflectiveInvocationContext<Method> invocationContextMock = mock(ReflectiveInvocationContext.class);
        ExtensionContext extensionContextMock = mockTestContext();
        mockActiveSuite(extensionContextMock, SuiteSampleTest.class);


        when(extensionContextMock.getRequiredTestMethod()).thenReturn(methodMock);
        when(extensionContextMock.getRequiredTestClass()).thenReturn((Class) test.getClass());
        when(statementBuilderMock.build()).thenReturn(statementMock);

        interceptor.postProcessTestInstance(test, extensionContextMock);
        interceptor.statementBuilder = statementBuilderMock;
        interceptor.interceptTestMethod(invocationMock, invocationContextMock, extensionContextMock);

        assertTrue(interceptor.measurementsStartTimeMs > 0);
        
        assertEquals(1, interceptor.activeReporters.size());
        assertEquals(SuiteSampleTest.config.getReportGenerators(), interceptor.activeReporters);
        
        EvaluationContext context = captureEvaluationContext();
        assertEquals(100, context.getConfiguredExecutionTarget());
        assertEquals(5, context.getConfiguredThreads());
        assertEquals(1, context.getRequiredThroughput());
        
    }

    @Test
    void whenAChildClassInheritsFromABaseClassWithReportingConfig_thenChildClassShouldHaveAccessToReportingConfig() throws Throwable {
        SampleChildTest test = new SampleChildTest();

        Method methodMock = test.getClass().getMethod("someTestMethod");
        PerformanceEvaluationStatement statementMock = mock(PerformanceEvaluationStatement.class);
        Invocation<Void> invocationMock = mock(Invocation.class);
        ReflectiveInvocationContext<Method> invocationContextMock = mock(ReflectiveInvocationContext.class);
        ExtensionContext extensionContextMock = mockTestContext();

        when(extensionContextMock.getRequiredTestMethod()).thenReturn(methodMock);
        when(extensionContextMock.getRequiredTestClass()).thenReturn((Class) test.getClass());
        when(statementBuilderMock.build()).thenReturn(statementMock);

        interceptor.postProcessTestInstance(test, extensionContextMock);
        interceptor.statementBuilder = statementBuilderMock;
        interceptor.interceptTestMethod(invocationMock, invocationContextMock, extensionContextMock);

        assertTrue(interceptor.measurementsStartTimeMs > 0);

        assertEquals(1, interceptor.activeReporters.size());
        assertEquals( SampleBaseTest.config.getReportGenerators(), interceptor.activeReporters);

        EvaluationContext context = captureEvaluationContext();
        assertEquals(120, context.getConfiguredExecutionTarget());
        assertEquals(15, context.getConfiguredThreads());
        assertEquals(67, context.getRequiredThroughput());

    }

    @Test
    void whenProceedThrowsAnAssertionError_thenTestShouldNotFail() throws Throwable {
        SampleAnnotatedTest test = new SampleAnnotatedTest();

        Method methodMock = test.getClass().getMethod("someTestMethod");
        PerformanceEvaluationStatement statementMock = mock(PerformanceEvaluationStatement.class);
        Invocation<Void> invocationMock = mock(Invocation.class);
        ReflectiveInvocationContext<Method> invocationContextMock = mock(ReflectiveInvocationContext.class);
        ExtensionContext extensionContextMock = mockTestContext();

        when(extensionContextMock.getRequiredTestMethod()).thenReturn(methodMock);
        when(extensionContextMock.getRequiredTestClass()).thenReturn((Class) test.getClass());
        when(statementBuilderMock.build()).thenReturn(statementMock);

        when(invocationMock.proceed()).thenThrow(new AssertionError());
        
        interceptor.postProcessTestInstance(test, extensionContextMock);
        interceptor.statementBuilder = statementBuilderMock;
        
        assertDoesNotThrow(() -> {
            interceptor.interceptTestMethod(invocationMock, invocationContextMock, extensionContextMock);
        });
    }

    @Test
    void whenProceedThrowsAnAssertionError_andTestIsNotAPerfTest_thenTestShouldFail() throws Throwable {
        SampleNoAnnotationsTest test = new SampleNoAnnotationsTest();

        Method methodMock = test.getClass().getMethod("someTestMethod");
        PerformanceEvaluationStatement statementMock = mock(PerformanceEvaluationStatement.class);
        Invocation<Void> invocationMock = mock(Invocation.class);
        ReflectiveInvocationContext<Method> invocationContextMock = mock(ReflectiveInvocationContext.class);
        ExtensionContext extensionContextMock = mockTestContext();

        when(extensionContextMock.getRequiredTestMethod()).thenReturn(methodMock);
        when(extensionContextMock.getRequiredTestClass()).thenReturn((Class) test.getClass());
        when(statementBuilderMock.build()).thenReturn(statementMock);

        when(invocationMock.proceed()).thenThrow(new AssertionError());

        interceptor.postProcessTestInstance(test, extensionContextMock);
        interceptor.statementBuilder = statementBuilderMock;

        assertThrows(AssertionError.class, () -> {
            interceptor.interceptTestMethod(invocationMock, invocationContextMock, extensionContextMock);
        });
    }

    @Test
    void whenInterceptorSupportsParameterIsCalled_thenParameterTypeShouldBeChecked() throws NoSuchMethodException {
        assertTrue(interceptor.supportsParameter(mockTestContextSupplierParameterType(), null));
        assertFalse(interceptor.supportsParameter(mockStringParameterType(), null));
    }

    @Test
    void whenInterceptorResolveParameterIsCalled_thenTestContextSupplierShouldBeReturned() {
        assertTrue(interceptor.resolveParameter(null, null) instanceof TestContextSupplier);
    }
    
    private static void mockActiveSuite(ExtensionContext extensionContextMock, Class<?> suiteClass) {
        when(extensionContextMock.getRoot()).thenReturn(extensionContextMock);
        when(extensionContextMock.getUniqueId()).thenReturn(buildSuiteId(suiteClass));
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
        ExtensionContext ctxt = mock(ExtensionContext.class);
        when(ctxt.getUniqueId()).thenReturn("unitest" + ThreadLocalRandom.current().nextInt());
        return ctxt;
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
    
}