package com.noconnor.junitperf.statements;

import lombok.Builder;

import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import org.junit.runners.model.Statement;
import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.noconnor.junitperf.data.EvaluationContext;
import com.noconnor.junitperf.statistics.Statistics;
import com.noconnor.junitperf.statistics.providers.ApacheDescriptiveStatistics;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.util.concurrent.RateLimiter.create;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PerformanceEvaluationStatement extends Statement {

  private static final String THREAD_NAME_PATTERN = "perf-eval-thread-%d";
  private static final ThreadFactory FACTORY = new ThreadFactoryBuilder().setNameFormat(THREAD_NAME_PATTERN).build();

  private final EvaluationContext context;
  private final ThreadFactory threadFactory;
  private final Statement baseStatement;
  private final RateLimiter rateLimiter;
  private Consumer<Boolean> listener;

  @Builder(builderMethodName = "perfEvalBuilder")
  private PerformanceEvaluationStatement(Statement baseStatement, EvaluationContext context, Consumer<Boolean> listener) {
    this(baseStatement, context, FACTORY, listener);
  }

  @Builder(builderMethodName = "perfEvalBuilderTest", builderClassName = "BuildTest")
  private PerformanceEvaluationStatement(Statement baseStatement,
                                         EvaluationContext context,
                                         ThreadFactory threadFactory,
                                         Consumer<Boolean> listener) {
    this.context = context;
    this.baseStatement = baseStatement;
    this.threadFactory = threadFactory;
    this.rateLimiter = context.getConfiguredRateLimit() > 0 ? create(context.getConfiguredRateLimit()) : null;
    this.listener = listener;
  }

  @Override
  public void evaluate() throws Throwable {
    List<Thread> threads = newArrayList();
    Statistics statistics = new ApacheDescriptiveStatistics();
    try {
      for (int i = 0; i < context.getConfiguredThreads(); i++) {
        EvaluationTask task = new EvaluationTask(baseStatement, rateLimiter, statistics, context.getConfiguredWarmUp());
        Thread t = threadFactory.newThread(task);
        threads.add(t);
        t.start();
      }
      Thread.sleep(context.getConfiguredDuration());
    } finally {
      threads.forEach(Thread::interrupt);
    }
    context.setStatistics(statistics);
    context.runValidation();
    listener.accept(true);
    assertThresholdsMet();
  }

  private void assertThresholdsMet() {
    assertThat("Error threshold not achieved", context.isErrorThresholdAchieved(), is(true));
    assertThat("Test throughput threshold not achieved", context.isThroughputAchieved(), is(true));
    context.getPercentileResults().forEach((percentile, isAchieved) -> {
      assertThat(format("%dth Percentile has not achieved required threshold", percentile), isAchieved, is(true));
    });
  }

}
