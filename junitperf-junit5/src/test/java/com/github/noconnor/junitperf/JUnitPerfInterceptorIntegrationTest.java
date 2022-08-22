package com.github.noconnor.junitperf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.noconnor.junitperf.reporting.ReportGenerator;
import com.github.noconnor.junitperf.reporting.providers.ConsoleReportGenerator;
import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import com.github.noconnor.junitperf.statistics.providers.DescriptiveStatisticsCalculator;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(JUnitPerfInterceptor.class)
public class JUnitPerfInterceptorIntegrationTest {

    @ActiveReporter
    private ReportGenerator htmlReporter = new HtmlReportGenerator();

    @ActiveReporter
    private ReportGenerator consoleReporter = new ConsoleReportGenerator();

    @ActiveStatisticsCollector
    private StatisticsCalculator statisticsCalculator = new DescriptiveStatisticsCalculator();

    @Test
    @JUnitPerfTest(threads = 1, rampUpPeriodMs = 100, durationMs = 1_000)
    void someOtherTest() {
        assertTrue(true);
    }

    @Test
    @JUnitPerfTest(threads = 10, rampUpPeriodMs = 100, durationMs = 1_000)
    void integrationTest() {
        assertTrue(true);
    }
}
