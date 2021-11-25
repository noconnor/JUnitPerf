package com.github.noconnor.junitperf.statistics;

import java.util.concurrent.TimeUnit;

public interface StatisticsCalculator {

  void addLatencyMeasurement(long executionTimeNs);

  void incrementErrorCount();

  void incrementEvaluationCount();

  long getErrorCount();

  long getEvaluationCount();

  float getLatencyPercentile(int percentile, TimeUnit unit);

  float getMaxLatency(TimeUnit unit);

  float getMinLatency(TimeUnit unit);

  float getMeanLatency(TimeUnit unit);

  float getErrorPercentage();

  void reset();

}
