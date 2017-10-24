package com.noconnor.junitperf.statements;

import java.util.function.Supplier;
import org.junit.runners.model.Statement;
import com.google.common.util.concurrent.RateLimiter;
import com.noconnor.junitperf.statistics.StatisticsEvaluator;

import static java.lang.System.nanoTime;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class EvaluationTask implements Runnable {

  private final Statement statement;
  private final RateLimiter rateLimiter;
  private final Supplier<Boolean> terminator;
  private final StatisticsEvaluator stats;
  private final long warmUpPeriodNs;

  EvaluationTask(Statement statement, RateLimiter rateLimiter, StatisticsEvaluator stats, int warmUpPeriodMs) {
    this(statement, rateLimiter, () -> Thread.currentThread().isInterrupted(), stats, warmUpPeriodMs);
  }

  // Test only
  EvaluationTask(Statement statement,
                 RateLimiter rateLimiter,
                 Supplier<Boolean> terminator,
                 StatisticsEvaluator stats,
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
      } catch (Throwable throwable) {
        stats.incrementErrorCount();
      } finally {
        stats.incrementEvaluationCount();
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
