package com.github.noconnor.junitperf.utils;

import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import lombok.Builder;

import java.lang.annotation.Annotation;

@Builder
public class JUnitPerfTestRequirements implements JUnitPerfTestRequirement {

    @Builder.Default
    private String percentiles = "";
    @Builder.Default
    private int executionsPerSec = 0;
    @Builder.Default
    private float allowedErrorPercentage = 0;
    @Builder.Default
    private float minLatency = -1;
    @Builder.Default
    private float maxLatency = -1;
    @Builder.Default
    private float meanLatency = -1;

    @Override
    public String percentiles() {
        return percentiles;
    }

    @Override
    public int executionsPerSec() {
        return executionsPerSec;
    }

    @Override
    public float allowedErrorPercentage() {
        return allowedErrorPercentage;
    }

    @Override
    public float minLatency() {
        return minLatency;
    }

    @Override
    public float maxLatency() {
        return maxLatency;
    }

    @Override
    public float meanLatency() {
        return meanLatency;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return JUnitPerfTestRequirement.class;
    }
}
