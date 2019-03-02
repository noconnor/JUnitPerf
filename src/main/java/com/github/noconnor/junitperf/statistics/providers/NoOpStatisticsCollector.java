package com.github.noconnor.junitperf.statistics.providers;

import com.github.noconnor.junitperf.statistics.StatisticsCalculator;

import java.util.concurrent.TimeUnit;

public class NoOpStatisticsCollector implements StatisticsCalculator {

  public static final NoOpStatisticsCollector INSTANCE = new NoOpStatisticsCollector();

  @Override
  public void addLatencyMeasurement(long executionTimeNs) {
  }

  @Override
  public void incrementErrorCount() {
  }

  @Override
  public void incrementEvaluationCount() {
  }

  @Override
  public long getErrorCount() {
    return 0;
  }

  @Override
  public long getEvaluationCount() {
    return 0;
  }

  @Override
  public float getLatencyPercentile(int percentile, TimeUnit unit) {
    return 0;
  }

  @Override
  public float getMaxLatency(TimeUnit unit) {
    return 0;
  }

  @Override
  public float getMinLatency(TimeUnit unit) {
    return 0;
  }

  @Override
  public float getMeanLatency(TimeUnit unit) {
    return 0;
  }

  @Override
  public float getErrorPercentage() {
    return 0;
  }
}
