package com.github.noconnor.junitperf.statements;

import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import com.github.noconnor.junitperf.statistics.providers.NoOpStatisticsCollector;
import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Builder;
import org.junit.runners.model.Statement;

import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.util.concurrent.RateLimiter.create;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PerformanceEvaluationStatement extends Statement {

  private static final String THREAD_NAME_PATTERN = "perf-eval-thread-%d";
  private static final ThreadFactory FACTORY = new ThreadFactoryBuilder().setNameFormat(THREAD_NAME_PATTERN).build();

  private final EvaluationContext context;
  private final ThreadFactory threadFactory;
  private final Statement baseStatement;
  private final StatisticsCalculator statistics;
  private final Consumer<Void> listener;

  private RateLimiter rateLimiter;

  @Builder
  private PerformanceEvaluationStatement(Statement baseStatement,
    StatisticsCalculator statistics,
    EvaluationContext context,
    ThreadFactory threadFactory,
    Consumer<Void> listener) {
    this.context = context;
    this.baseStatement = baseStatement;
    this.statistics = statistics;
    this.threadFactory = nonNull(threadFactory) ? threadFactory : FACTORY;
    this.rateLimiter = context.getConfiguredRateLimit() > 0 ? create(context.getConfiguredRateLimit()) : null;
    this.listener = listener;
  }

  @Override
  public void evaluate() throws Throwable {
    List<Thread> threads = newArrayList();
    try {
      for (int i = 0; i < context.getConfiguredThreads(); i++) {
        Thread t = threadFactory.newThread(createTask());
        threads.add(t);
        t.start();
      }
      Thread.sleep(context.getConfiguredDuration());
    } finally {
      threads.forEach(Thread::interrupt);
    }
    context.setStatistics(statistics);
    context.runValidation();
    listener.accept(null);
    assertThresholdsMet();
  }

  private EvaluationTask createTask() {
    StatisticsCalculator stats = context.isAsyncEvaluation() ? NoOpStatisticsCollector.INSTANCE : statistics;
    return EvaluationTask.builder()
      .statement(baseStatement)
      .rateLimiter(rateLimiter)
      .stats(stats)
      .warmUpPeriodMs(context.getConfiguredWarmUp())
      .build();
  }

  private void assertThresholdsMet() {
    assertThat("Error threshold not achieved", context.isErrorThresholdAchieved(), is(true));
    assertThat("Test throughput threshold not achieved", context.isThroughputAchieved(), is(true));
    context.getPercentileResults().forEach((percentile, isAchieved) -> {
      assertThat(format("%dth Percentile has not achieved required threshold", percentile), isAchieved, is(true));
    });
  }

}
