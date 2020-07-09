package com.github.noconnor.junitperf.statements;

import lombok.Builder;

import java.util.function.Supplier;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import com.google.common.util.concurrent.RateLimiter;

import static java.lang.System.nanoTime;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

final class EvaluationTask implements Runnable {

  private final TestStatement statement;
  private final RateLimiter rateLimiter;
  private final Supplier<Boolean> terminator;
  private final StatisticsCalculator stats;
  private final long warmUpPeriodNs;

  @Builder
  EvaluationTask(TestStatement statement, RateLimiter rateLimiter, StatisticsCalculator stats, int warmUpPeriodMs) {
    this(statement, rateLimiter, () -> Thread.currentThread().isInterrupted(), stats, warmUpPeriodMs);
  }

  // Test only
  EvaluationTask(TestStatement statement,
                 RateLimiter rateLimiter,
                 Supplier<Boolean> terminator,
                 StatisticsCalculator stats,
                 int warmUpPeriodMs) {
    this.statement = statement;
    this.rateLimiter = rateLimiter;
    this.terminator = terminator;
    this.stats = stats;
    this.warmUpPeriodNs = NANOSECONDS.convert(warmUpPeriodMs > 0 ? warmUpPeriodMs : 0, MILLISECONDS);
  }

  @Override
  public void run() {
    long startTimeNs = nanoTime();
    long startMeasurements = startTimeNs + warmUpPeriodNs;
    while (!terminator.get()) {
      waitForPermit();
      evaluateStatement(startMeasurements);
    }
  }

  private void evaluateStatement(long startMeasurements) {
    if (nanoTime() < startMeasurements) {
      try {
        statement.evaluate();
      } catch (Throwable throwable) {
        // IGNORE
      }
    } else {
      long startTimeNs = nanoTime();
      try {
        statement.evaluate();
        stats.addLatencyMeasurement(nanoTime() - startTimeNs);
        stats.incrementEvaluationCount();
      } catch (InterruptedException e) { // NOSONAR
        // IGNORE - no metrics
      } catch (Throwable throwable) {
        stats.incrementEvaluationCount();
        stats.incrementErrorCount();
        stats.addLatencyMeasurement(nanoTime() - startTimeNs);
      }
    }
  }

  private void waitForPermit() {
    if (nonNull(rateLimiter)) {
      rateLimiter.acquire();
    }
  }

}
