package com.noconnor.junitperf.statements;

import lombok.Builder;

import org.junit.runners.model.Statement;

@Builder(builderMethodName = "perfEvalBuilder")
public class PerformanceEvaluationStatement extends Statement {

  private final int threadCount;
  private final int testDurationMs;
  private final int warmUpPeriodMs;
  private final int rateLimitExecutionsPerSecond;

  private final Statement baseStatement;

  @Override
  public void evaluate() throws Throwable {
    baseStatement.evaluate();
  }

}
