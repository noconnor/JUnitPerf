package com.noconnor.junitperf.statistics;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static com.noconnor.junitperf.statistics.utils.StatisticsUtils.calculatePercentageError;
import static com.noconnor.junitperf.statistics.utils.StatisticsUtils.calculateThroughputPerSecond;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class StatisticsValidator {

  public boolean isThroughputTargetAchieved(Statistics statistics, int durationMs, int expectedThroughput) {
    return calculateThroughputPerSecond(statistics, durationMs) >= expectedThroughput;
  }

  public boolean isErrorThresholdTargetAchieved(Statistics statistics, float allowedErrorsRate) {
    return calculatePercentageError(statistics) <= allowedErrorsRate;
  }

  public Map<Integer, Boolean> evaluateLatencyPercentiles(Statistics statistics, Map<Integer, Float> percentiles) {
    Map<Integer, Boolean> results = newHashMap();
    percentiles.forEach((percentile, thresholdMs) -> {
      long thresholdNs = (long)(thresholdMs * MILLISECONDS.toNanos(1));
      boolean result = statistics.getLatencyPercentile(percentile) <= thresholdNs;
      results.put(percentile, result);
    });
    return results;
  }

}
