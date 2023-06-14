package com.github.noconnor.junitperf;

import java.util.HashMap;
import java.util.Map;

public class JUnitPerfTestRegistry {

    private static final Map<Class<?>, JUnitPerfTest> suitePerfTestAnnotations = new HashMap<>();
    private static final Map<Class<?>, JUnitPerfTestRequirement> suiteRequirementTestAnnotations = new HashMap<>();
    private static final Map<Class<?>, JUnitPerfReportingConfig> suiteReporters = new HashMap<>();

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
        suitePerfTestAnnotations.put(clazz, spec);
        suiteRequirementTestAnnotations.put(clazz, requirements);
        suiteReporters.put(clazz, reportingConfig);
    }

    public static JUnitPerfReportingConfig getReportingConfig(Class<?> clazz) {
        return suiteReporters.get(clazz);
    }

    public static JUnitPerfTest getPerfTestData(Class<?> clazz) {
        return suitePerfTestAnnotations.get(clazz);
    }

    public static JUnitPerfTestRequirement getPerfRequirements(Class<?> clazz) {
        return suiteRequirementTestAnnotations.get(clazz);
    }

}
