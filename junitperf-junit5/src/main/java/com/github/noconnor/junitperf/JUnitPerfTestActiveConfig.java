package com.github.noconnor.junitperf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface JUnitPerfTestActiveConfig {
    // Marker interface for ReportingConfig instance
}
