package com.noconnor.junitperf.statistics;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static com.noconnor.junitperf.statistics.utils.StatisticsUtils.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class StatisticsValidator {

  private final int expectedThroughput;
  private final float allowedErrorsRate;
  private final String percentiles;
  private final int durationMs;

  @Builder
  StatisticsValidator(int expectedThroughput, float allowedErrorsRate, String percentiles, int durationMs) {
    this.expectedThroughput = expectedThroughput;
    this.allowedErrorsRate = allowedErrorsRate;
    this.percentiles = percentiles;
    this.durationMs = durationMs;
  }

  public ValidationResult validate(Statistics statistics) {
    return ValidationResult.builder()
      .isThroughputAchieved(calculateThroughputPerSecond(statistics, durationMs) >= expectedThroughput)
      .isErrorThresholdAchieved(calculatePercentageError(statistics) <= allowedErrorsRate)
      .percentileResults(evaluateLatencyPercentiles(statistics))
      .build();
  }

  private Map<Integer, Boolean> evaluateLatencyPercentiles(Statistics statistics) {
    Map<Integer, Boolean> results = newHashMap();
    parsePercentileLimits(percentiles).forEach((percentile, thresholdMs) -> {
      long thresholdNs = (long)(thresholdMs * MILLISECONDS.toNanos(1));
      boolean result = statistics.getLatencyPercentile(percentile) <= thresholdNs;
      results.put(percentile, result);
    });
    return results;
  }

  @Value
  @Builder
  public static class ValidationResult {
    boolean isThroughputAchieved;
    boolean isErrorThresholdAchieved;
    Map<Integer, Boolean> percentileResults;
  }

}
