package com.noconnor.junitperf;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import com.noconnor.junitperf.statements.PerformanceEvaluationStatement;
import com.noconnor.junitperf.statements.PerformanceEvaluationStatement.PerformanceEvaluationStatementBuilder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.Mockito.*;


public class JunitPerfRuleTest extends BaseTest {

  private static final int DURATION = 22_000;
  private static final int RATE_LIMIT = 1_000;
  private static final int THREADS = 50;
  private static final int WARM_UP = 20_000;

  private JunitPerfRule perfRule;

  @Mock
  private Statement statementMock;

  @Mock
  private PerformanceEvaluationStatement perfEvalStatement;

  @Mock
  private Description descriptionMock;

  @Mock
  private JUnitPerfTest perfTestAnnotationMock;

  @Mock(answer = RETURNS_SELF)
  private PerformanceEvaluationStatementBuilder perfEvalBuilderMock;

  @Before
  public void setup() {
    initialisePerfEvalBuilder();
    initialisePerfTestAnnotationMock();
    mockJunitPerfTestAnnotationPresent();
    perfRule = new JunitPerfRule(perfEvalBuilderMock);
  }

  @Test
  public void whenExecutingApply_andNoJunitPerfTestAnnotationIsPresent_thenTheBaseStatementShouldBeReturned() {
    mockJunitPerfTestAnnotationNotPresent();
    Statement statement = perfRule.apply(statementMock, descriptionMock);
    assertThat(statement, is(statementMock));
  }

  @Test
  public void whenExecutingApply_andJunitPerfTestAnnotationIsPresent_thenThePerformanceEvaluationStatementShouldBeReturned() {
    Statement statement = perfRule.apply(statementMock, descriptionMock);
    assertThat(statement, is(perfEvalStatement));
  }

  @Test
  public void whenExecutingApply_thenJunitPerfTestAnnotationAttributesShouldBeUsedWhenBuildingEvalStatement() {
    perfRule.apply(statementMock, descriptionMock);
    verify(perfEvalBuilderMock).rateLimitExecutionsPerSecond(RATE_LIMIT);
    verify(perfEvalBuilderMock).testDurationMs(DURATION);
    verify(perfEvalBuilderMock).threadCount(THREADS);
    verify(perfEvalBuilderMock).warmUpPeriodMs(WARM_UP);
    verify(perfEvalBuilderMock).baseStatement(statementMock);
    verify(perfEvalBuilderMock).build();
    verifyNoMoreInteractions(perfEvalBuilderMock);
  }

  private void initialisePerfEvalBuilder() {
    when(perfEvalBuilderMock.build()).thenReturn(perfEvalStatement);
  }

  private void mockJunitPerfTestAnnotationPresent() {
    when(descriptionMock.getAnnotation(JUnitPerfTest.class)).thenReturn(perfTestAnnotationMock);
  }

  private void mockJunitPerfTestAnnotationNotPresent() {
    when(descriptionMock.getAnnotation(JUnitPerfTest.class)).thenReturn(null);
  }

  private void initialisePerfTestAnnotationMock() {
    when(perfTestAnnotationMock.duration()).thenReturn(DURATION);
    when(perfTestAnnotationMock.rateLimit()).thenReturn(RATE_LIMIT);
    when(perfTestAnnotationMock.threads()).thenReturn(THREADS);
    when(perfTestAnnotationMock.warmUp()).thenReturn(WARM_UP);
  }

}
