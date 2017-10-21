package com.noconnor.junitperf.statements;

import lombok.Builder;

import java.util.List;
import java.util.concurrent.ThreadFactory;
import org.junit.runners.model.Statement;
import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;

public class PerformanceEvaluationStatement extends Statement {

  private static final String THREAD_NAME_PATTERN = "perf-eval-thread-%d";
  private static final ThreadFactory FACTORY = new ThreadFactoryBuilder().setNameFormat(THREAD_NAME_PATTERN).build();

  private final int threadCount;
  private final int testDurationMs;
  private final int warmUpPeriodMs;
  private final ThreadFactory threadFactory;
  private final Statement baseStatement;
  private final RateLimiter rateLimiter;

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
    this.baseStatement = baseStatement;
    this.threadFactory = threadFactory;
    this.rateLimiter = rateLimitExecutionsPerSecond > 0 ? RateLimiter.create(rateLimitExecutionsPerSecond) : null;
  }

  @Override
  public void evaluate() throws Throwable {
    List<Thread> threads = newArrayList();
    try {
      for (int i = 0; i < threadCount; i++) {
        Thread t = threadFactory.newThread(new EvaluationTask(baseStatement, rateLimiter));
        threads.add(t);
        t.start();
      }
      Thread.sleep(testDurationMs);
    } finally {
      threads.forEach(Thread::interrupt);
    }
  }

}
