package com.noconnor.junitperf.statistics;

public interface Statistics {

  void addLatencyMeasurement(long executionTimeNs);

  void incrementErrorCount();

  void incrementEvaluationCount();

  long getErrorCount();

  long getEvaluationCount();

  long getLatencyPercentile(int percentile);

  long getMaxLatency();

  long getMinLatency();

  long getMeanLatency();

}
