package com.github.noconnor.junitperf;

import java.util.Collection;
import java.util.function.Supplier;

import com.github.noconnor.junitperf.reporting.ReportGenerator;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;

import com.github.noconnor.junitperf.statistics.providers.DescriptiveStatisticsCalculator;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class JUnitPerfReportingConfig {
    @Singular
    Collection<ReportGenerator> reportGenerators;
    @Builder.Default
    Supplier<StatisticsCalculator> statisticsCalculatorSupplier = DescriptiveStatisticsCalculator::new;
}
