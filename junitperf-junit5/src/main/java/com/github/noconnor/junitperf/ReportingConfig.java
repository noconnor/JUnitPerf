package com.github.noconnor.junitperf;

import java.util.Collection;

import com.github.noconnor.junitperf.reporting.ReportGenerator;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class ReportingConfig {
    @Singular
    Collection<ReportGenerator> reportGenerators;
    StatisticsCalculator statisticsCalculator;
}
