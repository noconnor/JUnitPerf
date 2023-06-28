package com.github.noconnor.junitperf.examples;

import com.github.noconnor.junitperf.JUnitPerfInterceptor;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestActiveConfig;
import com.github.noconnor.junitperf.JUnitPerfReportingConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import static com.github.noconnor.junitperf.examples.utils.ReportingUtils.newHtmlReporter;

@ExtendWith(JUnitPerfInterceptor.class)
public class ExampleSuccessTests {

    // Should be static or new instance will be created for each @Test method
    @JUnitPerfTestActiveConfig
    private final static JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
            .reportGenerator(newHtmlReporter("success.html"))
            .build();

    @BeforeEach
    public void setup() throws InterruptedException {
        Thread.sleep(10);
    }

    @AfterEach
    public void teardown() throws InterruptedException {
        Thread.sleep(10);
    }

    @Test
    @JUnitPerfTest(threads = 10, durationMs = 10_000, warmUpMs = 1_000, rampUpPeriodMs = 2_000, maxExecutionsPerSecond = 100)
    public void whenNoRequirementsArePresent_thenTestShouldAlwaysPass() throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("www.google.com", 80), 1000);
        }
    }

    @Test
    @JUnitPerfTest(threads = 10, durationMs = 10_000, warmUpMs = 1_000, rampUpPeriodMs = 2_000, totalExecutions = 50)
    public void whenTotalNumberOfExecutionsIsSet_thenTotalExecutionsShouldOverrideDurationMs() throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("www.google.com", 80), 1000);
        }
    }
}
