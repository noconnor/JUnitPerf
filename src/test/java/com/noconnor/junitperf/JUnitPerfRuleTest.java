package com.noconnor.junitperf;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import com.noconnor.junitperf.statements.EvaluationTaskValidator;
import com.noconnor.junitperf.statements.EvaluationTaskValidator.EvaluationTaskValidatorBuilder;
import com.noconnor.junitperf.statements.PerformanceEvaluationStatement;
import com.noconnor.junitperf.statements.PerformanceEvaluationStatement.PerformanceEvaluationStatementBuilder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.Mockito.*;


public class JUnitPerfRuleTest extends BaseTest {

  private static final int DURATION = 22_000;
  private static final int RATE_LIMIT = 1_000;
  private static final int THREADS = 50;
  private static final int WARM_UP = 20_000;
  private static final float ALLOWED_ERRORS = 0.1f;
  private static final String PERCENTILES = "98:1.5,99:3.4";
  private static final int THROUGHPUT = 1_000;

  private JUnitPerfRule perfRule;

  @Mock
  private Statement statementMock;

  @Mock
  private PerformanceEvaluationStatement perfEvalStatement;

  @Mock
  private EvaluationTaskValidator validatorMock;

  @Mock
  private Description descriptionMock;

  @Mock
  private JUnitPerfTest perfTestAnnotationMock;

  @Mock
  private JUnitPerfTestRequirement requirementAnnotationMock;

  @Mock(answer = RETURNS_SELF)
  private PerformanceEvaluationStatementBuilder perfEvalBuilderMock;

  @Mock(answer = RETURNS_SELF)
  private EvaluationTaskValidatorBuilder validatorBuilderMock;

  @Before
  public void setup() {
    initialisePerfEvalBuilderMock();
    initialiseValidatorBuilderMock();
    initialisePerfTestAnnotationMock();
    initialisePerfTestRequirementAnnotationMock();
    mockJunitPerfTestAnnotationPresent();
    mockJunitPerfTestRequirementAnnotationPresent();
    perfRule = new JUnitPerfRule(perfEvalBuilderMock, validatorBuilderMock);
  }

  @Test
  public void whenExecutingApply_andNoJunitPerfTestAnnotationIsPresent_thenTheBaseStatementShouldBeReturned() {
    mockJunitPerfTestAnnotationNotPresent();
    Statement statement = perfRule.apply(statementMock, descriptionMock);
    assertThat(statement, is(statementMock));
  }

  @Test
  public void whenExecutingApply_andNoJunitPerfTestRequirementsAnnotationIsPresent_thenNoValidatorShouldBeBuilt() {
    mockJunitPerfTestRequirementAnnotationNotPresent();
    perfRule.apply(statementMock, descriptionMock);
    verifyZeroInteractions(validatorBuilderMock);
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
    verify(perfEvalBuilderMock).validator(validatorMock);
    verify(perfEvalBuilderMock).build();
    verifyNoMoreInteractions(perfEvalBuilderMock);
  }

  @Test
  public void whenExecutingApply_thenJunitPerfTestRequirementAnnotationAttributesShouldBeUsedWhenBuildingValidator() {
    perfRule.apply(statementMock, descriptionMock);
    verify(validatorBuilderMock).allowedErrorsRate(ALLOWED_ERRORS);
    verify(validatorBuilderMock).expectedThroughput(THROUGHPUT);
    verify(validatorBuilderMock).percentiles(PERCENTILES);
    verify(validatorBuilderMock).build();
    verifyNoMoreInteractions(validatorBuilderMock);
  }

  private void initialisePerfEvalBuilderMock() {
    when(perfEvalBuilderMock.build()).thenReturn(perfEvalStatement);
  }

  private void initialiseValidatorBuilderMock() {
    when(validatorBuilderMock.build()).thenReturn(validatorMock);
  }

  private void mockJunitPerfTestAnnotationPresent() {
    when(descriptionMock.getAnnotation(JUnitPerfTest.class)).thenReturn(perfTestAnnotationMock);
  }

  private void mockJunitPerfTestRequirementAnnotationPresent() {
    when(descriptionMock.getAnnotation(JUnitPerfTestRequirement.class)).thenReturn(requirementAnnotationMock);
  }

  private void mockJunitPerfTestAnnotationNotPresent() {
    when(descriptionMock.getAnnotation(JUnitPerfTest.class)).thenReturn(null);
  }

  private void mockJunitPerfTestRequirementAnnotationNotPresent() {
    when(descriptionMock.getAnnotation(JUnitPerfTestRequirement.class)).thenReturn(null);
  }

  private void initialisePerfTestAnnotationMock() {
    when(perfTestAnnotationMock.duration()).thenReturn(DURATION);
    when(perfTestAnnotationMock.rateLimit()).thenReturn(RATE_LIMIT);
    when(perfTestAnnotationMock.threads()).thenReturn(THREADS);
    when(perfTestAnnotationMock.warmUp()).thenReturn(WARM_UP);
  }

  private void initialisePerfTestRequirementAnnotationMock() {
    when(requirementAnnotationMock.allowedErrorsRate()).thenReturn(ALLOWED_ERRORS);
    when(requirementAnnotationMock.percentiles()).thenReturn(PERCENTILES);
    when(requirementAnnotationMock.throughput()).thenReturn(THROUGHPUT);
  }

}
