package com.noconnor.junitperf.statements;

import org.junit.Before;
import org.junit.Test;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import com.google.common.util.concurrent.RateLimiter;
import com.noconnor.junitperf.BaseTest;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EvaluationTaskTest extends BaseTest {

  private EvaluationTask task;

  @Mock
  private Statement statementMock;

  @Mock
  private RateLimiter rateLimiterMock;

  @Before
  public void setup() {
    initialiseRateLimiterMock();
    task = new EvaluationTask(statementMock, rateLimiterMock);
  }

  @Test
  public void whenRunning_thenTheTestStatementShouldBeEvaluated() throws Throwable {
    task.run();
    verify(statementMock).evaluate();
  }

  @Test
  public void whenRunning_thenAnAttemptShouldBeMadeToRetrieveAPermit() {
    task.run();
    verify(rateLimiterMock).tryAcquire();
  }

  @Test
  public void whenRateLimiterIsNull_thenRateLimitingShouldBeSkipped() throws Throwable {
    task = new EvaluationTask(statementMock, null);
    task.run();
    verify(statementMock).evaluate();
  }

  private void initialiseRateLimiterMock() {
    when(rateLimiterMock.tryAcquire()).thenReturn(true);
  }

}
