package com.github.noconnor.junitperf.statistics.providers;

import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

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
    float value = (float) statistics.getPercentile((double) (percentile));
    return value > 0 ? value / unit.toNanos(1) : 0;
  }

  @Override
  public float getMaxLatency(TimeUnit unit) {
    float max = (float) statistics.getMax();
    return max > 0 ? max / unit.toNanos(1) : 0;
  }

  @Override
  public float getMinLatency(TimeUnit unit) {
    float min = (float) statistics.getMin();
    return min > 0 ? min / unit.toNanos(1) : 0;
  }

  @Override
  public float getMeanLatency(TimeUnit unit) {
    float mean = (float) statistics.getMean();
    return mean > 0 ? mean / unit.toNanos(1) : 0;
  }

  @Override
  public float getErrorPercentage() {
    float evalCount = evaluationCount.get();
    float errCount = errorCount.get();
    return evalCount > 0 ? (errCount / evalCount) * 100 : 0;
  }

}
