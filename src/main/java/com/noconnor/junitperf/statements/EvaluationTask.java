package com.noconnor.junitperf.statements;

import java.util.function.Supplier;
import org.junit.runners.model.Statement;
import com.google.common.util.concurrent.RateLimiter;

import static java.util.Objects.nonNull;

public class EvaluationTask implements Runnable {

  private final Statement statement;
  private final RateLimiter rateLimiter;
  private final Supplier<Boolean> terminator;

  EvaluationTask(Statement statement, RateLimiter rateLimiter) {
    this(statement, rateLimiter, () -> Thread.currentThread().isInterrupted());
  }

  // Test only
  EvaluationTask(Statement statement, RateLimiter rateLimiter, Supplier<Boolean> terminator) {
    this.statement = statement;
    this.rateLimiter = rateLimiter;
    this.terminator = terminator;
  }

  @Override
  public void run() {
    try {
      while (!terminator.get()) {
        waitForPermit();
        // evaluate statistics here only!
        statement.evaluate();
      }
    } catch (Throwable throwable) {
      // IGNORE
    }

  }

  private void waitForPermit() {
    if (nonNull(rateLimiter)) {
      rateLimiter.acquire();
    }
  }

}
