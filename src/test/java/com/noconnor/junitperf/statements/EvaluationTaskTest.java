package com.noconnor.junitperf.statements;

import java.util.function.Supplier;
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

  @Mock
  private Supplier<Boolean> terminatorMock;

  @Before
  public void setup() {
    initialiseRateLimiterMock();
    initialiseTerminatorMock();
    task = new EvaluationTask(statementMock, rateLimiterMock, terminatorMock);
  }

  @Test
  public void whenRunning_thenTheTestStatementShouldBeEvaluated() throws Throwable {
    task.run();
    verify(statementMock).evaluate();
  }

  @Test
  public void whenRunning_thenAnAttemptShouldBeMadeToRetrieveAPermit() {
    task.run();
    verify(rateLimiterMock).acquire();
  }

  @Test
  public void whenRateLimiterIsNull_thenRateLimitingShouldBeSkipped() throws Throwable {
    task = new EvaluationTask(statementMock, null, terminatorMock);
    task.run();
    verify(statementMock).evaluate();
  }

  private void initialiseRateLimiterMock() {
    when(rateLimiterMock.tryAcquire()).thenReturn(true);
  }

  private void initialiseTerminatorMock() {
    when(terminatorMock.get()).thenReturn(false).thenReturn(true);
  }

}
