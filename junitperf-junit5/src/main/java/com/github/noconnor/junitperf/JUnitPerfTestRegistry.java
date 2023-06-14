package com.github.noconnor.junitperf.suite;

import com.github.noconnor.junitperf.JUnitPerfReportingConfig;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;

import java.util.HashMap;
import java.util.Map;

public class JUnitPerfTestRegistry {

    private static final Map<Class<?>, JUnitPerfTest> suitePerfTestAnnotations = new HashMap<>();
    private static final Map<Class<?>, JUnitPerfTestRequirement> suiteRequirementTestAnnotations = new HashMap<>();
    private static final Map<Class<?>, JUnitPerfReportingConfig> suiteReporters = new HashMap<>();

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

    public static JUnitPerfReportingConfig getReporters(Class<?> clazz) {
        return suiteReporters.get(clazz);
    }

    public static JUnitPerfTest getSuiteJUnitPerfTestData(Class<?> clazz) {
        return suitePerfTestAnnotations.get(clazz);
    }

    public static JUnitPerfTestRequirement getSuiteJUnitPerfRequirements(Class<?> clazz) {
        return suiteRequirementTestAnnotations.get(clazz);
    }

}
