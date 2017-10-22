package com.noconnor.junitperf.statements;

import java.util.function.Supplier;
import org.junit.runners.model.Statement;
import com.google.common.util.concurrent.RateLimiter;
import com.noconnor.junitperf.statistics.StatisticsEvaluator;

import static java.lang.System.nanoTime;
import static java.util.Objects.nonNull;

public class EvaluationTask implements Runnable {

  private final Statement statement;
  private final RateLimiter rateLimiter;
  private final Supplier<Boolean> terminator;
  private final StatisticsEvaluator stats;

  EvaluationTask(Statement statement, RateLimiter rateLimiter, StatisticsEvaluator stats) {
    this(statement, rateLimiter, () -> Thread.currentThread().isInterrupted(), stats);
  }

  // Test only
  EvaluationTask(Statement statement,
                 RateLimiter rateLimiter,
                 Supplier<Boolean> terminator,
                 StatisticsEvaluator stats) {
    this.statement = statement;
    this.rateLimiter = rateLimiter;
    this.terminator = terminator;
    this.stats = stats;
  }

  @Override
  public void run() {
    while (!terminator.get()) {
      try {
        waitForPermit();
        // evaluate statistics AFTER permit acquired
        stats.incrementEvaluationCount();
        evaluateStatement();
      } catch (Throwable throwable) {
        stats.incrementErrorCount();
      }
    }
  }

  private void evaluateStatement() throws Throwable {
    long startTimeNs = nanoTime();
    try {
      statement.evaluate();
    } finally {
      stats.addLatencyMeasurement(nanoTime() - startTimeNs);
    }
  }

  private void waitForPermit() {
    if (nonNull(rateLimiter)) {
      rateLimiter.acquire();
    }
  }

}
