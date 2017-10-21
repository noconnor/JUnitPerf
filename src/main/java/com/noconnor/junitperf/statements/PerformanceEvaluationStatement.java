package com.noconnor.junitperf.statements;

import lombok.Builder;

import java.util.concurrent.ThreadFactory;
import org.junit.runners.model.Statement;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import static com.google.common.base.Preconditions.checkArgument;

public class PerformanceEvaluationStatement extends Statement {

  private static final String THREAD_NAME_PATTERN = "perf-eval-thread-%d";
  private static final ThreadFactory FACTORY = new ThreadFactoryBuilder().setNameFormat(THREAD_NAME_PATTERN).build();

  private final int threadCount;
  private final int testDurationMs;
  private final int warmUpPeriodMs;
  private final int rateLimitExecutionsPerSecond;
  private final ThreadFactory threadFactory;
  private final Statement baseStatement;

  @Builder(builderMethodName = "perfEvalBuilder")
  private PerformanceEvaluationStatement(int threadCount,
                                         int testDurationMs,
                                         int warmUpPeriodMs,
                                         int rateLimitExecutionsPerSecond,
                                         Statement baseStatement) {
    this(threadCount, testDurationMs, warmUpPeriodMs, rateLimitExecutionsPerSecond, baseStatement, FACTORY);
  }

  @Builder(builderMethodName = "perfEvalBuilderTest", builderClassName = "BuildTest")
  private PerformanceEvaluationStatement(int threadCount,
                                         int testDurationMs,
                                         int warmUpPeriodMs,
                                         int rateLimitExecutionsPerSecond,
                                         Statement baseStatement,
                                         ThreadFactory threadFactory) {

    checkArgument(threadCount >= 1, "Thread count must be >= 1");
    checkArgument(testDurationMs >= 1, "Test duration count must be >= 1");
    this.threadCount = threadCount;
    this.testDurationMs = testDurationMs;
    this.warmUpPeriodMs = warmUpPeriodMs;
    this.rateLimitExecutionsPerSecond = rateLimitExecutionsPerSecond;
    this.baseStatement = baseStatement;
    this.threadFactory = threadFactory;
  }

  @Override
  public void evaluate() throws Throwable {
    for (int i = 0; i < threadCount; i++) {
      threadFactory.newThread(new EvaluationTask(baseStatement)).start();
    }
  }


}
