package com.github.noconnor.junitperf.utils;

import com.github.noconnor.junitperf.JUnitPerfTest;
import lombok.Builder;

import java.lang.annotation.Annotation;

@Builder
public class JUnitPerfTestSpec implements JUnitPerfTest {
    @Builder.Default
    private int threads = 1;
    @Builder.Default
    private int durationMs = 60_000;
    @Builder.Default
    private int warmUpMs = 0;
    @Builder.Default
    private int maxExecutionsPerSecond = -1;
    @Builder.Default
    private int rampUpPeriodMs = 0;
    @Builder.Default
    private int totalExecutions = -1;

    @Override
    public int threads() {
        return threads;
    }

    @Override
    public int durationMs() {
        return durationMs;
    }

    @Override
    public int warmUpMs() {
        return warmUpMs;
    }

    @Override
    public int maxExecutionsPerSecond() {
        return maxExecutionsPerSecond;
    }

    @Override
    public int rampUpPeriodMs() {
        return rampUpPeriodMs;
    }

    @Override
    public int totalExecutions() {
        return totalExecutions;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return JUnitPerfTest.class;
    }
}
