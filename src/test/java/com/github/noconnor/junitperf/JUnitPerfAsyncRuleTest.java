package com.github.noconnor.junitperf;

import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.data.NoOpTestContext;
import com.github.noconnor.junitperf.data.TestContext;
import com.github.noconnor.junitperf.reporting.ReportGenerator;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class JUnitPerfAsyncRuleTest {

  @Mock
  private StatisticsCalculator statsCollectorMock;

  @Mock
  private ReportGenerator reporterMock;

  @Mock
  private Description descriptionMock;

  @Mock
  private JUnitPerfTest perfTestAnnotationMock;

  private JUnitPerfAsyncRule rule;

  @Before
  public void setup() {
    initialisePerfTestAnnotationMock();
    rule = new JUnitPerfAsyncRule(statsCollectorMock, reporterMock);
  }

  @Test
  public void whenCallingNewTestContext_thenATestContextIsReturnedThatWrapsTheStatsCollector() {
    TestContext context = rule.newContext();
    context.success();
    verify(statsCollectorMock).incrementEvaluationCount();
  }

  @Test
  public void whenCallingNewTestContext_andApplyHasBeenCalled_andWarmupPeriodHasNotElapsed_thenANoOpTestContextShouldBeReturned() {
    mimicWarmUpPeriod(1_000);
    rule.apply(null, descriptionMock);
    TestContext context = rule.newContext();
    assertThat(context, is(NoOpTestContext.INSTANCE));
  }

  @Test
  public void whenCallingNewTestContext_andApplyHasBeenCalled_andWarmupPeriodHasElapsed_thenAValidTestContextShouldBeReturned() {
    mimicWarmUpPeriod(0);
    rule.apply(null, descriptionMock);
    TestContext context = rule.newContext();
    context.success();
    verify(statsCollectorMock).incrementEvaluationCount();
  }

  @Test
  public void whenCallingCreateEvaluationContext_thenContextShouldHaveAsyncFlagSetToTrue() {
    EvaluationContext context = rule.createEvaluationContext(descriptionMock);
    assertThat(context.isAsyncEvaluation(), is(true));
  }

  @Test
  public void whenCallingApply_andNoPerfTestAnnotationIsPresent_thenNoExceptionShouldBeThrown() {
    when(descriptionMock.getAnnotation(JUnitPerfTest.class)).thenReturn(null);
    rule.apply(null, descriptionMock);
  }

  private void mimicWarmUpPeriod(int periodMs) {
    when(perfTestAnnotationMock.warmUpMs()).thenReturn(periodMs);
  }

  private void initialisePerfTestAnnotationMock() {
    when(descriptionMock.getAnnotation(JUnitPerfTest.class)).thenReturn(perfTestAnnotationMock);
    when(perfTestAnnotationMock.warmUpMs()).thenReturn(0);
    when(perfTestAnnotationMock.durationMs()).thenReturn(10_000);
    when(perfTestAnnotationMock.threads()).thenReturn(1);
    when(perfTestAnnotationMock.maxExecutionsPerSecond()).thenReturn(-1);
  }
}
