package com.github.noconnor.junitperf.suite;

import com.github.noconnor.junitperf.JUnitPerfReportingConfig;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestActiveConfig;
import com.github.noconnor.junitperf.JUnitPerfTestRegistry;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import lombok.experimental.UtilityClass;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SelectPackages;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.junit.platform.commons.util.ReflectionUtils.findAllClassesInPackage;

@UtilityClass
public class JUnitPerfTestRegistryUtils {
    
    public static void registerSuite(Class<?> clazz) {
        JUnitPerfTest testSpec = clazz.getAnnotation(JUnitPerfTest.class);
        JUnitPerfTestRequirement requirements = clazz.getAnnotation(JUnitPerfTestRequirement.class);
        JUnitPerfReportingConfig reportingConfig = Arrays.stream(clazz.getFields())
                .filter(f -> f.isAnnotationPresent(JUnitPerfTestActiveConfig.class))
                .map(f -> {
                    if (!Modifier.isStatic(f.getModifiers())) {
                        throw new IllegalStateException("JUnitPerfReportingConfig must be static");
                    }
                    f.setAccessible(true);
                    try {
                        return (JUnitPerfReportingConfig) f.get(null);
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException(e);
                    }
                }).findFirst().orElse(null);
        
        registerSuite(clazz, testSpec, requirements, reportingConfig);
    }

    public static void registerSuite(Class<?> clazz,
                                     JUnitPerfTest testSpec,
                                     JUnitPerfTestRequirement requirements,
                                     JUnitPerfReportingConfig reportingConfig) {

        SelectClasses suiteClasses = clazz.getAnnotation(SelectClasses.class);
        SelectPackages suitePackages = clazz.getAnnotation(SelectPackages.class);

        if (nonNull(suiteClasses)) {
            for (Class<?> suiteClazz : suiteClasses.value()) {
                JUnitPerfTestRegistry.registerPerfTest(suiteClazz, testSpec, requirements, reportingConfig);
            }
        }

        if (nonNull(suitePackages)) {
            for (String packagePattern : suitePackages.value()) {
                List<Class<?>> classes = findAllClassesInPackage(packagePattern, ClassFilter.of(packageClazz -> true));
                for (Class<?> suiteClazz : classes) {
                    JUnitPerfTestRegistry.registerPerfTest(suiteClazz, testSpec, requirements, reportingConfig);
                }
            }
        }

    }
}
