package com.noconnor.junitperf.statements;

import lombok.Builder;

import java.util.List;
import java.util.concurrent.ThreadFactory;
import org.junit.runners.model.Statement;
import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.noconnor.junitperf.statistics.providers.DescriptiveStatisticsEvaluator;
import com.noconnor.junitperf.statistics.StatisticsEvaluator;

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
  private final EvaluationTaskValidator validator;

  @Builder(builderMethodName = "perfEvalBuilder")
  private PerformanceEvaluationStatement(int threadCount,
                                         int testDurationMs,
                                         int warmUpPeriodMs,
                                         int rateLimitExecutionsPerSecond,
                                         Statement baseStatement,
                                         EvaluationTaskValidator validator) {
    this(threadCount, testDurationMs, warmUpPeriodMs, rateLimitExecutionsPerSecond, baseStatement, FACTORY, validator);
  }

  @Builder(builderMethodName = "perfEvalBuilderTest", builderClassName = "BuildTest")
  private PerformanceEvaluationStatement(int threadCount,
                                         int testDurationMs,
                                         int warmUpPeriodMs,
                                         int rateLimitExecutionsPerSecond,
                                         Statement baseStatement,
                                         ThreadFactory threadFactory,
                                         EvaluationTaskValidator validator) {

    checkArgument(threadCount >= 1, "Thread count must be >= 1");
    checkArgument(testDurationMs >= 1, "Test duration count must be >= 1");
    this.threadCount = threadCount;
    this.testDurationMs = testDurationMs;
    this.warmUpPeriodMs = warmUpPeriodMs;
    this.baseStatement = baseStatement;
    this.threadFactory = threadFactory;
    this.rateLimiter = rateLimitExecutionsPerSecond > 0 ? RateLimiter.create(rateLimitExecutionsPerSecond) : null;
    this.validator = validator;
  }

  @Override
  public void evaluate() throws Throwable {
    List<Thread> threads = newArrayList();
    StatisticsEvaluator evaluator = new DescriptiveStatisticsEvaluator();
    try {
      for (int i = 0; i < threadCount; i++) {
        EvaluationTask task = new EvaluationTask(baseStatement, rateLimiter, evaluator);
        Thread t = threadFactory.newThread(task);
        threads.add(t);
        t.start();
      }
      Thread.sleep(testDurationMs);
    } finally {
      threads.forEach(Thread::interrupt);
    }
  }

}
