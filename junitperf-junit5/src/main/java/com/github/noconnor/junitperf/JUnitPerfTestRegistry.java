package com.github.noconnor.junitperf;

import java.util.HashMap;
import java.util.Map;

public class JUnitPerfTestRegistry {

    private static final Map<Class<?>, JUnitPerfTest> registeredPerfTestAnnotations = new HashMap<>();
    private static final Map<Class<?>, JUnitPerfTestRequirement> registeredRequirementTestAnnotations = new HashMap<>();
    private static final Map<Class<?>, JUnitPerfReportingConfig> registeredReporterConfigs = new HashMap<>();

    //
    // May need to scope by suite:
    // See JUnitPerfInterceptor::interceptTestMethod:-> extensionContext.getRoot().getUniqueId()
    // [engine:junit-platform-suite]/[suite:com.github.noconnor.junitperf.examples.ExampleTestSuiteUsage]/[engine:junit-jupiter]
    // For non suite test cases:
    // [engine:junit-jupiter]
    public static void registerPerfTest(
            Class<?> clazz,
            JUnitPerfTest spec,
            JUnitPerfTestRequirement requirements,
            JUnitPerfReportingConfig reportingConfig
    ) {
        registeredPerfTestAnnotations.put(clazz, spec);
        registeredRequirementTestAnnotations.put(clazz, requirements);
        registeredReporterConfigs.put(clazz, reportingConfig);
    }

    public static JUnitPerfReportingConfig getReportingConfig(Class<?> clazz) {
        return registeredReporterConfigs.get(clazz);
    }

    public static JUnitPerfTest getPerfTestData(Class<?> clazz) {
        return registeredPerfTestAnnotations.get(clazz);
    }

    public static JUnitPerfTestRequirement getPerfRequirements(Class<?> clazz) {
        return registeredRequirementTestAnnotations.get(clazz);
    }

}
