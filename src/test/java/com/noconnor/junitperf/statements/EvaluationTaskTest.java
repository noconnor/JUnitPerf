package com.noconnor.junitperf.statements;

import java.util.function.Supplier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;
import com.google.common.util.concurrent.RateLimiter;
import com.noconnor.junitperf.BaseTest;
import com.noconnor.junitperf.statistics.StatisticsEvaluator;

import static org.mockito.Mockito.*;

public class EvaluationTaskTest extends BaseTest {

  private EvaluationTask task;

  @Mock
  private Statement statementMock;

  @Mock
  private RateLimiter rateLimiterMock;

  @Mock
  private Supplier<Boolean> terminatorMock;

  @Mock
  private StatisticsEvaluator statsMock;

  @Before
  public void setup() {
    initialiseRateLimiterMock();
    task = new EvaluationTask(statementMock, rateLimiterMock, terminatorMock, statsMock);
  }

  @Test
  public void whenRunning_thenTheTestStatementShouldBeEvaluated() throws Throwable {
    setExecutionCount(1);
    task.run();
    verify(statementMock).evaluate();
  }

  @Test
  public void whenRunning_thenStatsExecutionCounterShouldBeIncremented() throws Throwable {
    setExecutionCount(5);
    task.run();
    verify(statsMock, times(5)).incrementEvaluationCount();
  }

  @Test
  public void whenRunning_thenAnAttemptShouldBeMadeToRetrieveAPermit() {
    setExecutionCount(10);
    task.run();
    verify(rateLimiterMock, times(10)).acquire();
  }

  @Test
  public void whenRateLimiterIsNull_thenRateLimitingShouldBeSkipped() throws Throwable {
    setExecutionCount(10);
    task = new EvaluationTask(statementMock, null, terminatorMock, statsMock);
    task.run();
    verify(statementMock, times(10)).evaluate();
  }

  private void initialiseRateLimiterMock() {
    when(rateLimiterMock.tryAcquire()).thenReturn(true);
  }

  private void setExecutionCount(int loops) {
    OngoingStubbing<Boolean> stub = when(terminatorMock.get());
    for (int i = 0; i < loops; i++) {
      stub = stub.thenReturn(false);
    }
    stub.thenReturn(true);
  }

}
