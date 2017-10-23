package com.noconnor.junitperf.statistics.providers;

import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.descriptive.SynchronizedDescriptiveStatistics;
import com.noconnor.junitperf.statistics.StatisticsEvaluator;

public class DescriptiveStatisticsEvaluator implements StatisticsEvaluator {

  private final DescriptiveStatistics statistics;
  private final AtomicLong errorCount = new AtomicLong();
  private final AtomicLong evaluationCount = new AtomicLong();

  // http://commons.apache.org/proper/commons-math/userguide/stat.html#a1.2_Descriptive_statistics
  public DescriptiveStatisticsEvaluator() {
    this(new SynchronizedDescriptiveStatistics());
  }

  DescriptiveStatisticsEvaluator(DescriptiveStatistics statistics) {
    this.statistics = statistics;
  }

  @Override
  public void addLatencyMeasurement(long executionTimeNs) {
    statistics.addValue(executionTimeNs);
  }

  @Override
  public void incrementErrorCount() {
    errorCount.incrementAndGet();
  }

  @Override
  public void incrementEvaluationCount() {
    evaluationCount.incrementAndGet();
  }

  @Override
  public long getErrorCount() {
    return errorCount.get();
  }

  @Override
  public long getEvaluationCount() {
    return evaluationCount.get();
  }

  @Override
  public long getLatencyPercentile(int percentile) {
    return (long)statistics.getPercentile((double)(percentile));
  }

  @Override
  public long getMaxLatency() {
    return (long)statistics.getMax();
  }

  @Override
  public long getMinLatency() {
    return (long)statistics.getMin();
  }

  @Override
  public long getMeanLatency() {
    return (long)statistics.getMean();
  }

}
