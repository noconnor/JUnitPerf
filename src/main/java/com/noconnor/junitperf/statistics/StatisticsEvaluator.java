package com.noconnor.junitperf.statistics;

public interface StatisticsEvaluator {

  void addLatencyMeasurement(long executionTimeNs);

  void incrementErrorCount();

  void incrementEvaluationCount();

}
