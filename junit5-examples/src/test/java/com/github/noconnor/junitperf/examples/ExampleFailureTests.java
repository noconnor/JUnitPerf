package com.github.noconnor.junitperf.examples;

import com.github.noconnor.junitperf.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.github.noconnor.junitperf.examples.utils.ReportingUtils.newHtmlReporter;

@ExtendWith(JUnitPerfInterceptor.class)
public class ExampleFailureTests {

    // Should be static or new instance will be created for each @Test method
    @JUnitPerfTestActiveConfig
    private final static JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
            .reportGenerator(newHtmlReporter("failures.html"))
            .build();

    @Test
    @JUnitPerfTest(threads = 1, durationMs = 1_000, maxExecutionsPerSecond = 1_000)
    @JUnitPerfTestRequirement(executionsPerSec = 10_000)
    public void whenThroughputRequirementIsNotMet_thenTestShouldFail() throws InterruptedException {
        // Mock some processing logic
        Thread.sleep(1);
    }

    @Test
    @JUnitPerfTest(threads = 1, durationMs = 1_000, maxExecutionsPerSecond = 1_000)
    @JUnitPerfTestRequirement(executionsPerSec = 10, percentiles = "99:1")
    public void whenLatencyRequirementIsNotMet_thenTestShouldFail() throws InterruptedException {
        // Mock some processing logic
        Thread.sleep(2);
    }

    @Test
    @JUnitPerfTest(threads = 1, durationMs = 1_000, maxExecutionsPerSecond = 1_000)
    public void whenNoRequirementsAreSpecified_andExceptionIsThrown_thenTestShouldFail() throws InterruptedException {
        // Mock some processing logic
        Thread.sleep(2);
        throw new IllegalStateException("testing failure");
    }

}
