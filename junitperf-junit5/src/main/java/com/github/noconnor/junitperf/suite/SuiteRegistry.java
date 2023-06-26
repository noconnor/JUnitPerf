package com.github.noconnor.junitperf.suite;

import com.github.noconnor.junitperf.JUnitPerfReportingConfig;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestActiveConfig;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
public class SuiteRegistry {

    private static final Map<String, SuiteSettings> settingsCache = new HashMap<>();
    private static final Pattern suiteClassPattern = Pattern.compile("\\[suite:([^\\]]*)\\]");

    public static void scanForSuiteDetails(ExtensionContext context) {

        String rootUniqueId = getRootId(context);
        Class<?> clazz = getTopLevelSuiteClass(rootUniqueId);

        if (isNull(clazz) || settingsCache.containsKey(rootUniqueId)) {
            return;
        }

        JUnitPerfTest testSpec = clazz.getAnnotation(JUnitPerfTest.class);
        JUnitPerfTestRequirement requirements = clazz.getAnnotation(JUnitPerfTestRequirement.class);
        JUnitPerfReportingConfig reportingConfig = Arrays.stream(clazz.getFields())
                .filter(f -> f.isAnnotationPresent(JUnitPerfTestActiveConfig.class))
                .map(f -> {
                    warnIfNonStatic(f);
                    return getFieldValue(f);
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        SuiteSettings suiteSettings = SuiteSettings.builder()
                .perfTestSpec(testSpec)
                .requirements(requirements)
                .reportingConfig(reportingConfig)
                .build();

        settingsCache.put(rootUniqueId, suiteSettings);
    }

    public static void clearRegistry() {
        settingsCache.clear();
    }
    
    public static JUnitPerfReportingConfig getReportingConfig(ExtensionContext context) {
        SuiteSettings s = settingsCache.get(getRootId(context));
        return nonNull(s) ? s.getReportingConfig() : null;
    }

    public static JUnitPerfTest getPerfTestData(ExtensionContext context) {
        SuiteSettings s = settingsCache.get(getRootId(context));
        return nonNull(s) ? s.getPerfTestSpec() : null;
    }

    public static JUnitPerfTestRequirement getPerfRequirements(ExtensionContext context) {
        SuiteSettings s = settingsCache.get(getRootId(context));
        return nonNull(s) ? s.getRequirements() : null;
    }
    
    private static String getRootId(ExtensionContext context) {
        if (nonNull(context) && nonNull(context.getRoot())) {
            return context.getRoot().getUniqueId();
        }
        return "";
    }

    private static Class<?> getTopLevelSuiteClass(String rootUniqueId) {
        Matcher m = suiteClassPattern.matcher(rootUniqueId);
        if (m.find()) { // find first match - root suite
            try {
                return Class.forName(m.group(1));
            } catch (ClassNotFoundException e) {
                log.warn("Suite class not found: {}", rootUniqueId);
            }
        }
        return null;
    }

    private static JUnitPerfReportingConfig getFieldValue(Field f) {
        try {
            f.setAccessible(true);
            return (JUnitPerfReportingConfig) f.get(null);
        } catch (Exception e) {
            log.error("Unable to access JUnitPerfReportingConfig, make sure config is a static variable", e);
        }
        return null;
    }

    private static void warnIfNonStatic(Field f) {
        if (!Modifier.isStatic(f.getModifiers())) {
            log.warn("JUnitPerfReportingConfig must be static for test suites");
        }
    }

    @Value
    @Builder
    private static class SuiteSettings {
        JUnitPerfTest perfTestSpec;
        JUnitPerfTestRequirement requirements;
        JUnitPerfReportingConfig reportingConfig;
    }
}
