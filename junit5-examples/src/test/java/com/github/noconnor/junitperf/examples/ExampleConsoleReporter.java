package com.github.noconnor.junitperf.examples;

import com.github.noconnor.junitperf.JUnitPerfInterceptor;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestActiveConfig;
import com.github.noconnor.junitperf.JUnitPerfReportingConfig;
import com.github.noconnor.junitperf.reporting.providers.ConsoleReportGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JUnitPerfInterceptor.class)
public class ExampleConsoleReporter {


    // Should be static or new instance will be created for each @Test method
    @JUnitPerfTestActiveConfig
    private static final JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
            .reportGenerator(new ConsoleReportGenerator())
            .build();


    @Test
    @JUnitPerfTest(threads = 1, warmUpMs = 1_000, durationMs = 2_000)
    public void test1() throws InterruptedException {
        Thread.sleep(10);
    }

    @Test
    @JUnitPerfTest(threads = 1, warmUpMs = 1_000, durationMs = 2_000)
    public void test2() throws InterruptedException {
        Thread.sleep(10);
    }

    @Test
    @JUnitPerfTest(threads = 1, warmUpMs = 1_000, durationMs = 2_000)
    public void test3() throws InterruptedException {
        Thread.sleep(10);
    }
}
