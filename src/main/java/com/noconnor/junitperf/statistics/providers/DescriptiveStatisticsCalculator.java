package com.noconnor.junitperf.statistics.providers;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.descriptive.SynchronizedDescriptiveStatistics;
import com.noconnor.junitperf.statistics.StatisticsCalculator;

public class DescriptiveStatisticsCalculator implements StatisticsCalculator {

  private final DescriptiveStatistics statistics;
  private final AtomicLong errorCount = new AtomicLong();
  private final AtomicLong evaluationCount = new AtomicLong();

  // http://commons.apache.org/proper/commons-math/userguide/stat.html#a1.2_Descriptive_statistics
  public DescriptiveStatisticsCalculator() {
    this(new SynchronizedDescriptiveStatistics());
  }

  DescriptiveStatisticsCalculator(DescriptiveStatistics statistics) {
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
  public float getLatencyPercentile(int percentile, TimeUnit unit) {
    return (float)statistics.getPercentile((double)(percentile)) / unit.toNanos(1);
  }

  @Override
  public float getMaxLatency(TimeUnit unit) {
    return (float)statistics.getMax() / unit.toNanos(1);
  }

  @Override
  public float getMinLatency(TimeUnit unit) {
    return (float)statistics.getMin() / unit.toNanos(1);
  }

  @Override
  public float getMeanLatency(TimeUnit unit) {
    return (float)statistics.getMean() / unit.toNanos(1);
  }

  @Override
  public float getErrorPercentage() {
    return ((float)errorCount.get() / (float)evaluationCount.get()) * 100;
  }

}
