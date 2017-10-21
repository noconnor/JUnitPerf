package com.noconnor.junitperf.statements;

import java.util.concurrent.ThreadFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import com.noconnor.junitperf.BaseTest;
import com.noconnor.junitperf.statements.PerformanceEvaluationStatement.BuildTest;

import static com.noconnor.junitperf.statements.PerformanceEvaluationStatement.perfEvalBuilderTest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PerformanceEvaluationStatementTest extends BaseTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Mock
  private Statement baseStatementMock;

  @Mock
  private ThreadFactory threadFactoryMock;

  @Mock
  private Thread threadMock;

  @Before
  public void setup() {
    initialiseThreadFactoryMock();
  }

  @Test
  public void whenCreatingAnEvaluationStatement_andThreadCountIsLessThanZero_thenExceptionShouldBeThrown() {
    exception.expectMessage("Thread count must be >= 1");
    exception.expect(IllegalArgumentException.class);
    perfEvalBuilderTest().baseStatement(baseStatementMock).threadFactory(threadFactoryMock).threadCount(-1).build();
  }

  @Test
  public void whenCreatingAnEvaluationStatement_andTestDurationIsLessThanZero_thenExceptionShouldBeThrown() {
    exception.expectMessage("Test duration count must be >= 1");
    exception.expect(IllegalArgumentException.class);
    perfEvalBuilderTest().baseStatement(baseStatementMock)
      .threadCount(1)
      .testDurationMs(-1)
      .threadFactory(threadFactoryMock)
      .build();
  }

  @Test
  public void whenEvaluatingABaseStatement_thenTheCorrectNumberOfThreadsShouldBeStarted() throws Throwable {
    basicPerformanceEvaluationBuilder().threadCount(10).build().evaluate();
    verify(threadFactoryMock, times(10)).newThread(any(EvaluationTask.class));
    verify(threadMock, times(10)).start();
  }

  private BuildTest basicPerformanceEvaluationBuilder() {
    return perfEvalBuilderTest().baseStatement(baseStatementMock)
      .threadFactory(threadFactoryMock)
      .threadCount(1)
      .testDurationMs(1);
  }

  private void initialiseThreadFactoryMock() {
    when(threadFactoryMock.newThread(any(EvaluationTask.class))).thenReturn(threadMock);
  }

}
