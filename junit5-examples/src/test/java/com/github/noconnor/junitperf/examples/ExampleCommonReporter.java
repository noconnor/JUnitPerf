package com.github.noconnor.junitperf.examples;

import com.github.noconnor.junitperf.*;
import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import static com.github.noconnor.junitperf.examples.utils.ReportingUtils.newHtmlReporter;
import static org.junit.Assert.assertTrue;

// Suite tests require the junit-platform-suite-engine dependency (see pom file)
@Suite
@SelectClasses({
        ExampleCommonReporter.TestClassOne.class,
        ExampleCommonReporter.TestClassTwo.class
})
public class ExampleCommonReporter {

    // Both test classes should report to the same HTML file
    private static final HtmlReportGenerator REPORTER = newHtmlReporter("common_reporter.html");

    @ExtendWith(JUnitPerfInterceptor.class)
    public static class TestClassOne {

        // Should be static or new instance will be created for each @Test method
        @JUnitPerfTestActiveConfig
        private static final JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
                .reportGenerator(REPORTER)
                .build();

        @Test
        @JUnitPerfTest(threads = 10, durationMs = 10_000, warmUpMs = 1_000, rampUpPeriodMs = 2_000, maxExecutionsPerSecond = 100)
        public void whenNoRequirementsArePresent_thenTestShouldAlwaysPass() throws IOException {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("www.google.com", 80), 1000);
                assertTrue(socket.isConnected());
            }
        }
    }

    @ExtendWith(JUnitPerfInterceptor.class)
    public static class TestClassTwo {

        // Should be static or new instance will be created for each @Test method
        @JUnitPerfTestActiveConfig
        private static final JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
                .reportGenerator(REPORTER)
                .build();

        @Test
        @JUnitPerfTest(threads = 1, durationMs = 1_000, maxExecutionsPerSecond = 1_000)
        @JUnitPerfTestRequirement(executionsPerSec = 10_000)
        public void whenThroughputRequirementIsNotMet_thenTestShouldFail() throws InterruptedException {
            // Mock some processing logic
            Thread.sleep(1);
        }
    }

}
