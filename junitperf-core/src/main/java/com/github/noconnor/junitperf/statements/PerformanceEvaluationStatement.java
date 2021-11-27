package com.github.noconnor.junitperf.statements;

import lombok.Builder;

import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import com.github.noconnor.junitperf.statistics.providers.NoOpStatisticsCollector;
import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.util.concurrent.RateLimiter.create;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class PerformanceEvaluationStatement {

  private static final String THREAD_NAME_PATTERN = "perf-eval-thread-%d";
  private static final ThreadFactory FACTORY = new ThreadFactoryBuilder().setNameFormat(THREAD_NAME_PATTERN).build();

  private final EvaluationContext context;
  private final ThreadFactory threadFactory;
  private final TestStatement baseStatement;
  private final StatisticsCalculator statistics;
  private final Consumer<Void> listener;

  private RateLimiter rateLimiter;

  @Builder
  private PerformanceEvaluationStatement(TestStatement baseStatement,
    StatisticsCalculator statistics,
    EvaluationContext context,
    ThreadFactory threadFactory,
    Consumer<Void> listener) {
    this.context = context;
    this.baseStatement = baseStatement;
    this.statistics = statistics;
    this.threadFactory = nonNull(threadFactory) ? threadFactory : FACTORY;
    this.rateLimiter = context.getConfiguredRateLimit() > 0 ? createRateLimiter(context) : null;
    this.listener = listener;
  }

  public void runParallelEvaluation() throws Throwable {
    statistics.reset();
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
    assertThat("Error threshold not achieved", context.isErrorThresholdAchieved(), true);
    assertThat("Test throughput threshold not achieved", context.isThroughputAchieved(), true);
    assertThat("Test min latency threshold not achieved", context.isMinLatencyAchieved(), true);
    assertThat("Test max latency threshold not achieved", context.isMaxLatencyAchieved(), true);
    assertThat("Test mean latency threshold not achieved", context.isMeanLatencyAchieved(), true);
    context.getPercentileResults().forEach((percentile, isAchieved) -> {
      assertThat(format("%dth Percentile has not achieved required threshold", percentile), isAchieved, true);
    });
  }

  private RateLimiter createRateLimiter(final EvaluationContext context) {
    int rampUp = context.getConfiguredRampUpPeriodMs();
    int rateLimit = context.getConfiguredRateLimit();
    return rampUp > 0 ? create(rateLimit, rampUp, MILLISECONDS) : create(rateLimit);
  }

  private void assertThat(String message, boolean actual, boolean expected){
    if (actual != expected){
      throw new AssertionError(message);
    }
  }


}
