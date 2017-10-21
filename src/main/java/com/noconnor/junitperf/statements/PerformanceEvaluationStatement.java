package com.noconnor.junitperf.statements;

import lombok.Builder;

import org.junit.runners.model.Statement;

@Builder( builderMethodName = "perfEvalBuilder")
public class PerformanceEvaluationStatement extends Statement {

  @Builder.Default
  private int threadCount = 1;
  @Builder.Default
  private int testDurationMs = 60_000;
  @Builder.Default
  private int warmUpPeriodMs = 0;
  @Builder.Default
  private int rateLimitExecutionsPerSecond = -1;

  private final Statement baseStatement;

  @Override
  public void evaluate() throws Throwable {
    baseStatement.evaluate();
  }

}
