package com.noconnor.junitperf.statistics;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

import static com.noconnor.junitperf.statistics.utils.StatisticsUtils.calculatePercentageError;
import static com.noconnor.junitperf.statistics.utils.StatisticsUtils.calculateThroughputPerSecond;

public class StatisticsValidator {

  private final int expectedThroughput;
  private final float allowedErrorsRate;
  private final String percentiles;
  private final int durationMs;

  @Builder
  public StatisticsValidator(int expectedThroughput, float allowedErrorsRate, String percentiles, int durationMs) {
    this.expectedThroughput = expectedThroughput;
    this.allowedErrorsRate = allowedErrorsRate;
    this.percentiles = percentiles;
    this.durationMs = durationMs;
  }
  
  public ValidationResult validate(Statistics statistics) {
    return ValidationResult.builder()
      .isThroughputAchieved(calculateThroughputPerSecond(statistics, durationMs) >= expectedThroughput)
      .isErrorThresholdAchieved(calculatePercentageError(statistics) <= allowedErrorsRate)
      .percentileResults(null)
      .build();
  }

  @Value
  @Builder
  public static class ValidationResult {
    boolean isThroughputAchieved;
    boolean isErrorThresholdAchieved;
    Map<Integer, Boolean> percentileResults;
  }

}
