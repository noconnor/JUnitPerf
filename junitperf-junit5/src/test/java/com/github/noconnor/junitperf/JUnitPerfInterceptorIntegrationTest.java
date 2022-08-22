package com.github.noconnor.junitperf;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.noconnor.junitperf.reporting.providers.ConsoleReportGenerator;
import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;
import com.github.noconnor.junitperf.statistics.providers.DescriptiveStatisticsCalculator;

@ExtendWith(JUnitPerfInterceptor.class)
public class JUnitPerfInterceptorIntegrationTest {

    @JUnitPerfReporingConfig
    private ReportingConfig config = ReportingConfig.builder()
            .reportGenerator(new ConsoleReportGenerator())
            .reportGenerator(new HtmlReportGenerator())
            .statisticsCalculator(new DescriptiveStatisticsCalculator())
            .build();

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
