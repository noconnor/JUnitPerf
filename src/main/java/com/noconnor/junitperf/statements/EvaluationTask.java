package com.noconnor.junitperf.statements;

import lombok.RequiredArgsConstructor;

import org.junit.runners.model.Statement;

@RequiredArgsConstructor
public class EvaluationTask implements Runnable {

  private final Statement statement;

  @Override
  public void run() {
    try {
      statement.evaluate();
    } catch (Throwable throwable) {
      // IGNORE
    }
  }
}
