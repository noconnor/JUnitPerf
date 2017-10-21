package com.noconnor.junitperf;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import com.noconnor.junitperf.annotations.JUnitPerfTest;
import com.noconnor.junitperf.statements.PerformanceEvaluationStatement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;


public class JunitPerfRuleTest extends BaseTest {

  private JunitPerfRule perfRule;

  @Mock
  private Statement statementMock;

  @Mock
  private Description descriptionMock;

  @Mock
  private JUnitPerfTest perfTestAnnotationMock;

  @Before
  public void setup() {
    perfRule = new JunitPerfRule();
  }

  @Test
  public void whenExecutingApply_andNoJunitPerfTestAnnotationIsPresent_thenTheBaseStatementShouldBeReturned() {
    Statement statement = perfRule.apply(statementMock, descriptionMock);
    assertThat(statement, is(statementMock));
  }

  @Test
  public void whenExecutingApply_andJunitPerfTestAnnotationIsPresent_thenThePerformanceEvaluationStatementShouldBeReturned() {
    when(descriptionMock.getAnnotation(JUnitPerfTest.class)).thenReturn(perfTestAnnotationMock);
    Statement statement = perfRule.apply(statementMock, descriptionMock);
    assertThat(statement, instanceOf(PerformanceEvaluationStatement.class));
  }

}
