package com.noconnor.junitperf.statements;

import org.junit.Before;
import org.junit.Test;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import com.noconnor.junitperf.BaseTest;

import static org.mockito.Mockito.verify;

public class PerformanceEvaluationStatementTest extends BaseTest {

  private PerformanceEvaluationStatement statement;

  @Mock
  private Statement baseStatementMock;

  @Before
  public void setup() {
    statement = basicPerformanceEvaluationStatement();
  }

  @Test
  public void whenEvaluatingAPerformanceEvaluationStatement_thenTheBaseStatementShouldBeEvaluated() throws Throwable {
    statement.evaluate();
    verify(baseStatementMock).evaluate();
  }

  private PerformanceEvaluationStatement basicPerformanceEvaluationStatement() {
    return PerformanceEvaluationStatement.builder().baseStatement(baseStatementMock).build();
  }

}
