package com.github.noconnor.junitperf.suite;

import com.github.noconnor.junitperf.JUnitPerfReportingConfig;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestActiveConfig;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.suite.api.Suite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SuiteRegistryTest {
    
    @BeforeEach
    void setup() {
        SuiteRegistry.clearRegistry();
    }

    @Test
    void whenNoTestSuiteClassIsConfigured_thenSuiteShouldBeIdentified() {
        ExtensionContext context = createMockExtensionContext("[engine:junit-jupiter]");
        SuiteRegistry.scanForSuiteDetails(context);
        assertNull(SuiteRegistry.getPerfTestData(context));
        assertNull(SuiteRegistry.getPerfRequirements(context));
        assertNull(SuiteRegistry.getReportingConfig(context));
    }

    @Test
    void whenInvalidTestSuiteClassIsConfigured_thenSuiteShouldNotBeIdentified() {
        ExtensionContext context = createMockExtensionContext(buildSuiteId("com.does.not.Exist"));
        SuiteRegistry.scanForSuiteDetails(context);
        assertNull(SuiteRegistry.getPerfTestData(context));
        assertNull(SuiteRegistry.getPerfRequirements(context));
        assertNull(SuiteRegistry.getReportingConfig(context));
    }

    @Test
    void whenTestSuiteClassIsConfigured_butSuiteHasNoAnnotations_thenSuiteShouldBeIdentifiedButNoPerfDataShouldBeAvailable() {
        ExtensionContext context = createMockExtensionContext(buildSuiteId(DummySuiteNoAnnotations.class));
        SuiteRegistry.scanForSuiteDetails(context);
        assertNull(SuiteRegistry.getPerfTestData(context));
        assertNull(SuiteRegistry.getPerfRequirements(context));
        assertNull(SuiteRegistry.getReportingConfig(context));
    }

    @Test
    void whenTestSuiteClassIsConfigured_andSuiteHasPerfAnnotation_thenSuitePerfDataShouldBeAvailable() {
        ExtensionContext context = createMockExtensionContext(buildSuiteId(DummySuitePerfTestAnnotation.class));
        SuiteRegistry.scanForSuiteDetails(context);
        JUnitPerfTest testSpec = SuiteRegistry.getPerfTestData(context);

        assertNotNull(testSpec);
        assertNull(SuiteRegistry.getPerfRequirements(context));
        assertNull(SuiteRegistry.getReportingConfig(context));
        assertEquals(40, testSpec.totalExecutions());
    }

    @Test
    void whenTestSuiteClassIsConfigured_andSuiteHasAllPerfAnnotations_thenSuitePerfDataShouldBeAvailable() {
        ExtensionContext context = createMockExtensionContext(buildSuiteId(DummySuitePerfTestAllAnnotations.class));
        SuiteRegistry.scanForSuiteDetails(context);
        JUnitPerfTest testSpec = SuiteRegistry.getPerfTestData(context);
        JUnitPerfTestRequirement requirements = SuiteRegistry.getPerfRequirements(context);

        assertNotNull(testSpec);
        assertNotNull(requirements);
        assertNull(SuiteRegistry.getReportingConfig(context));

        assertEquals(3, testSpec.totalExecutions());
        assertEquals(0.03F, requirements.allowedErrorPercentage());
    }

    @Test
    void whenTestSuiteClassIsConfigured_andSuiteHasAllPerfAnnotationsAndReportingConfig_thenSuitePerfDataShouldBeAvailable() {
        ExtensionContext context = createMockExtensionContext(buildSuiteId(DummySuiteAllConfigs.class));
        SuiteRegistry.scanForSuiteDetails(context);
        JUnitPerfTest testSpec = SuiteRegistry.getPerfTestData(context);
        JUnitPerfTestRequirement requirements = SuiteRegistry.getPerfRequirements(context);
        JUnitPerfReportingConfig reportConfig = SuiteRegistry.getReportingConfig(context);

        assertNotNull(testSpec);
        assertNotNull(requirements);
        assertNotNull(reportConfig);

        assertEquals(53, testSpec.totalExecutions());
        assertEquals(0.13F, requirements.allowedErrorPercentage());
        assertEquals(DummySuiteAllConfigs.config, reportConfig);
    }

    @Test
    void whenTestSuiteClassIsConfigured_andSuiteHasBadReporterConfig_thenSuitePerfDataShouldBeAvailable_butReporterConfigWillBeMissing() {
        ExtensionContext context = createMockExtensionContext(buildSuiteId(DummySuiteBadReporterConfigs.class));
        SuiteRegistry.scanForSuiteDetails(context);
        JUnitPerfTest testSpec = SuiteRegistry.getPerfTestData(context);
        JUnitPerfTestRequirement requirements = SuiteRegistry.getPerfRequirements(context);
        JUnitPerfReportingConfig reportConfig = SuiteRegistry.getReportingConfig(context);

        assertNotNull(testSpec);
        assertNotNull(requirements);
        assertNull(reportConfig);

        assertEquals(345, testSpec.totalExecutions());
        assertEquals(0.168F, requirements.allowedErrorPercentage());
    }

    private static String buildSuiteId(Class<?> clazz) {
        return buildSuiteId(clazz.getName());
    }

    private static String buildSuiteId(String clazz) {
        return "[engine:junit-platform-suite]/[suite:" + clazz + "]/[engine:junit-jupiter]";
    }

    private static ExtensionContext createMockExtensionContext(String rootId) {
        ExtensionContext childContext = mock(ExtensionContext.class);
        ExtensionContext rootContext = mock(ExtensionContext.class);
        when(childContext.getRoot()).thenReturn(rootContext);
        when(rootContext.getUniqueId()).thenReturn(rootId);
        return childContext;
    }


    @Disabled
    @Suite
    public static class DummySuiteNoAnnotations {
    }

    @Disabled
    @Suite
    @JUnitPerfTest(totalExecutions = 40)
    public static class DummySuitePerfTestAnnotation {
    }

    @Disabled
    @Suite
    @JUnitPerfTest(totalExecutions = 3)
    @JUnitPerfTestRequirement(allowedErrorPercentage = 0.03F)
    public static class DummySuitePerfTestAllAnnotations {
    }

    @Disabled
    @Suite
    @JUnitPerfTest(totalExecutions = 53)
    @JUnitPerfTestRequirement(allowedErrorPercentage = 0.13F)
    public static class DummySuiteAllConfigs {
        @JUnitPerfTestActiveConfig
        public static JUnitPerfReportingConfig config = JUnitPerfReportingConfig.builder().build();
    }

    @Disabled
    @Suite
    @JUnitPerfTest(totalExecutions = 345)
    @JUnitPerfTestRequirement(allowedErrorPercentage = 0.168F)
    public static class DummySuiteBadReporterConfigs {
        @JUnitPerfTestActiveConfig // not static - should be dropped
        public JUnitPerfReportingConfig config = JUnitPerfReportingConfig.builder().build();
    }
}