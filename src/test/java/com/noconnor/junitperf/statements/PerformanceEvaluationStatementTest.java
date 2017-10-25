package com.noconnor.junitperf.statements;

import java.util.concurrent.ThreadFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import com.noconnor.junitperf.BaseTest;
import com.noconnor.junitperf.data.EvaluationContext;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
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
  private EvaluationContext contextMock;

  @Mock
  private Thread threadMock;

  private PerformanceEvaluationStatement statement;

  @Before
  public void setup() {
    initialiseThreadFactoryMock();
    initialiseContext();
    statement = PerformanceEvaluationStatement.perfEvalBuilderTest()
      .baseStatement(baseStatementMock)
      .threadFactory(threadFactoryMock)
      .context(contextMock)
      .build();
  }

  @Test
  public void whenEvaluatingABaseStatement_thenTheCorrectNumberOfThreadsShouldBeStarted() throws Throwable {
    when(contextMock.getConfiguredThreads()).thenReturn(10);
    statement.evaluate();
    verify(threadFactoryMock, times(10)).newThread(any(EvaluationTask.class));
    verify(threadMock, times(10)).start();
  }

  @Test
  public void whenEvaluatingABaseStatement_thenTheTestShouldEndWhenTheTestDurationExpires() throws Throwable {
    when(contextMock.getConfiguredDuration()).thenReturn(100);
    long starTimeNs = currentTimeMillis();
    statement.evaluate();
    assertThat((currentTimeMillis() - starTimeNs), is(greaterThan(95L)));
    assertThat((currentTimeMillis() - starTimeNs), is(lessThan(3 * 100L)));
    verify(threadMock, times(1)).interrupt();
  }

  private void initialiseThreadFactoryMock() {
    when(threadFactoryMock.newThread(any(EvaluationTask.class))).thenReturn(threadMock);
  }

  private void initialiseContext() {
    when(contextMock.getConfiguredThreads()).thenReturn(1);
    when(contextMock.getConfiguredDuration()).thenReturn(1000);
    when(contextMock.isErrorThresholdAchieved()).thenReturn(true);
    when(contextMock.isThroughputAchieved()).thenReturn(true);
    when(contextMock.getPercentileResults()).thenReturn(emptyMap());
  }

}
