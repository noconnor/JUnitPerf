package com.github.noconnor.junitperf.suite;

import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.github.noconnor.junitperf.reporting.ReportGenerator;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SelectPackages;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;

public class JunitPerfSuite {

    private static final Map<Class<?>, JUnitPerfTest> suitePerfTestAnnotations = new HashMap<>();
    private static final Map<Class<?>, JUnitPerfTestRequirement> suiteRequirementTestAnnotations = new HashMap<>();
    private static final Map<Class<?>, List<ReportGenerator>> suiteReporters = new HashMap<>();
    
    public static void registerPerfTestSuite(Class<?> clazz) {
        registerPerfTestSuite(clazz, Collections.emptyList());
    }

    public static void registerPerfTestSuite(Class<?> clazz, List<ReportGenerator> reportGenerator) {
        // TODO: update to use AnnotationUtils.findAnnotation()
        JUnitPerfTest perfTestAnnotation = clazz.getAnnotation(JUnitPerfTest.class);
        JUnitPerfTestRequirement requirementTestAnnotation = clazz.getAnnotation(JUnitPerfTestRequirement.class);
        // TODO: add support for SelectPackages etc to identify suite classes
        SelectClasses suiteClasses = clazz.getAnnotation(SelectClasses.class);
        SelectPackages suitePackages = clazz.getAnnotation(SelectPackages.class);
        
        if (nonNull(suiteClasses)) {
            for (Class<?> suiteClazz : suiteClasses.value()) {
                suitePerfTestAnnotations.put(suiteClazz, perfTestAnnotation);
                suiteRequirementTestAnnotations.put(suiteClazz, requirementTestAnnotation);
                suiteReporters.put(suiteClazz, reportGenerator);
            }
        }
        
        if (nonNull(suitePackages)) {
            for (String packagePattern : suitePackages.value()) {
                List<Class<?>> classes = ReflectionUtils.findAllClassesInPackage(packagePattern, ClassFilter.of(packageClazz -> true));
                for (Class<?> suiteClazz : classes) {
                    suitePerfTestAnnotations.put(suiteClazz, perfTestAnnotation);
                    suiteRequirementTestAnnotations.put(suiteClazz, requirementTestAnnotation);
                    suiteReporters.put(suiteClazz, reportGenerator);
                }
            }
        }

    }

    public static Collection<ReportGenerator> getReporters(Class<?> clazz) {
        return suiteReporters.get(clazz);
    }
    
    public static JUnitPerfTest getSuiteJUnitPerfTestData(Class<?> clazz) {
        return suitePerfTestAnnotations.get(clazz);
    }

    public static JUnitPerfTestRequirement getSuiteJUnitPerfRequirements(Class<?> clazz) {
        return suiteRequirementTestAnnotations.get(clazz);
    }
    
}
