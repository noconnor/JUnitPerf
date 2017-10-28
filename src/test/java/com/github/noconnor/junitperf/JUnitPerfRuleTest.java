package com.github.noconnor.junitperf;

import java.util.Set;
import java.util.function.Consumer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.reporting.ReportGenerator;
import com.github.noconnor.junitperf.statements.PerformanceEvaluationStatement;
import com.github.noconnor.junitperf.statements.PerformanceEvaluationStatement.PerformanceEvaluationStatementBuilder;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


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
  private Description descriptionMock;

  @Mock
  private JUnitPerfTest perfTestAnnotationMock;

  @Mock
  private JUnitPerfTestRequirement requirementAnnotationMock;

  @Mock(answer = RETURNS_SELF)
  private PerformanceEvaluationStatementBuilder perfEvalBuilderMock;

  @Mock
  private ReportGenerator reporterMock;

  @Mock
  private StatisticsCalculator statisticsCalculatorMock;

  @Before
  @SuppressWarnings("ResultOfMethodCallIgnored")
  public void setup() throws Exception {
    initialisePerfEvalBuilderMock();
    initialisePerfTestAnnotationMock();
    initialisePerfTestRequirementAnnotationMock();
    mockJunitPerfTestAnnotationPresent();
    mockJunitPerfTestRequirementAnnotationPresent();
    initialiseDescriptionMock();
    perfRule = new JUnitPerfRule(reporterMock, statisticsCalculatorMock);
    perfRule.perEvalBuilder = perfEvalBuilderMock;
  }

  @After
  public void teardown() {
    JUnitPerfRule.ACTIVE_CONTEXTS.clear();
  }

  @Test
  public void whenExecutingApply_andNoJunitPerfTestAnnotationIsPresent_thenTheBaseStatementShouldBeReturned() {
    mockJunitPerfTestAnnotationNotPresent();
    Statement statement = perfRule.apply(statementMock, descriptionMock);
    assertThat(statement, is(statementMock));
  }

  @Test
  public void whenExecutingApply_andNoJunitPerfTestRequirementAnnotationIsPresent_thenThePerformanceEvaluationStatementShouldBeReturned() {
    mockJunitPerfTestRequirementAnnotationNotPresent();
    Statement statement = perfRule.apply(statementMock, descriptionMock);
    assertThat(statement, is(perfEvalStatement));
  }

  @Test
  public void whenExecutingApply_andJunitPerfTestAnnotationIsPresent_thenThePerformanceEvaluationStatementShouldBeReturned() {
    Statement statement = perfRule.apply(statementMock, descriptionMock);
    assertThat(statement, is(perfEvalStatement));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void whenExecutingApply_thenJunitPerfTestAnnotationAttributesShouldBeUsedWhenBuildingEvalStatement() {
    perfRule.apply(statementMock, descriptionMock);
    verify(perfEvalBuilderMock).baseStatement(statementMock);
    verify(perfEvalBuilderMock).statistics(statisticsCalculatorMock);
    verify(perfEvalBuilderMock).context(any(EvaluationContext.class));
    verify(perfEvalBuilderMock).listener(any(Consumer.class));
    verify(perfEvalBuilderMock).build();
    verifyNoMoreInteractions(perfEvalBuilderMock);
  }

  @Test
  public void whenExecutingApply_thenTestsShouldBeGroupedByTestClass() {
    mockDescriptionTestClass(String.class);
    perfRule.apply(statementMock, descriptionMock);
    perfRule.apply(statementMock, descriptionMock);
    perfRule.apply(statementMock, descriptionMock);
    triggerReportGeneration();
    assertThat(captureReportContexts(), hasSize(3));

    mockDescriptionTestClass(Integer.class);
    perfRule.apply(statementMock, descriptionMock);
    triggerReportGeneration();
    assertThat(captureReportContexts(), hasSize(1));
  }

  private void triggerReportGeneration() {
    Consumer<Void> listener = captureListener();
    listener.accept(null);
  }

  @SuppressWarnings("unchecked")
  private Set<EvaluationContext> captureReportContexts() {
    ArgumentCaptor<Set<EvaluationContext>> captor = ArgumentCaptor.forClass(Set.class);
    verify(reporterMock, atLeastOnce()).generateReport(captor.capture());
    return captor.getValue();
  }

  @SuppressWarnings("unchecked")
  private Consumer<Void> captureListener() {
    ArgumentCaptor<Consumer<Void>> captor = ArgumentCaptor.forClass(Consumer.class);
    verify(perfEvalBuilderMock, atLeastOnce()).listener(captor.capture());
    return captor.getValue();
  }

  private void initialisePerfEvalBuilderMock() {
    when(perfEvalBuilderMock.build()).thenReturn(perfEvalStatement);
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

  private void initialiseDescriptionMock() {
    mockDescriptionTestClass(JUnitPerfRuleTest.class);
  }
  private void mockDescriptionTestClass(Class<?> clazz) {
    Mockito.<Class<?>>when(descriptionMock.getTestClass()).thenReturn(clazz);
  }

}
