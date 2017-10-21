package com.noconnor.junitperf.statements;

import lombok.RequiredArgsConstructor;

import org.junit.runners.model.Statement;
import com.google.common.util.concurrent.RateLimiter;

import static java.util.Objects.nonNull;

@RequiredArgsConstructor
public class EvaluationTask implements Runnable {

  private final Statement statement;
  private final RateLimiter rateLimiter;

  @Override
  public void run() {
    try {
      if (executionPermitAvailable()) {
        statement.evaluate();
      }
    } catch (Throwable throwable) {
      // IGNORE
    }
  }

  private boolean executionPermitAvailable() {
    return !nonNull(rateLimiter) || rateLimiter.tryAcquire();
  }

}
