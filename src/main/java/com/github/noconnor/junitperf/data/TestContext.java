package com.github.noconnor.junitperf.data;

import com.github.noconnor.junitperf.statistics.StatisticsCalculator;

import static java.lang.System.nanoTime;

public class TestContext {

  private final StatisticsCalculator stats;
  private final long startTimeNs;

  public TestContext(StatisticsCalculator stats) {
    this.stats = stats;
    this.startTimeNs = nanoTime();
  }

  public void success() {
    stats.incrementEvaluationCount();
    stats.addLatencyMeasurement(nanoTime() - startTimeNs);
  }

  public void fail() {
    stats.incrementEvaluationCount();
    stats.incrementErrorCount();
    stats.addLatencyMeasurement(nanoTime() - startTimeNs);
  }
}
