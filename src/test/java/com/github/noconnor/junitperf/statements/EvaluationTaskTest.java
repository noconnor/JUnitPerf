package com.github.noconnor.junitperf.statements;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;
import com.google.common.util.concurrent.RateLimiter;
import com.github.noconnor.junitperf.BaseTest;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;

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
  private StatisticsCalculator statsMock;

  @Before
  public void setup() {
    initialiseRateLimiterMock();
    task = new EvaluationTask(statementMock, rateLimiterMock, terminatorMock, statsMock, 0);
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
  public void whenRunning_andStatementEvaluationThrowsAnException_thenStatsErrorCounterShouldBeIncremented() throws Throwable {
    setExecutionCount(10);
    mockEvaluationFailures(5);
    task.run();
    verify(statsMock, times(10)).incrementEvaluationCount();
    verify(statsMock, times(5)).incrementErrorCount();
  }

  @Test
  public void whenRunning_andStatementEvaluationThrowsAnException_thenLatencyMeasurementShouldBeTaken() throws Throwable {
    setExecutionCount(10);
    mockEvaluationFailures(5);
    task.run();
    verify(statsMock, times(10)).addLatencyMeasurement(anyLong());
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
    task = new EvaluationTask(statementMock, null, terminatorMock, statsMock, 0);
    task.run();
    verify(statementMock, times(10)).evaluate();
  }

  @Test
  public void whenWarmUpPeriodIsNonZero_thenNoMeasurementsShouldBeTaken() throws Throwable {
    setExecutionCount(10);
    task = new EvaluationTask(statementMock, null, terminatorMock, statsMock, 100);
    task.run();
    verifyZeroInteractions(statsMock);
  }

  @Test
  public void whenWarmUpPeriodIsNonZero_andWarmupPeriodExpired_thenMeasurementsShouldBeTaken() throws Throwable {
    setExecutionCount(10000);
    task = new EvaluationTask(statementMock, null, terminatorMock, statsMock, 10);
    task.run();
    verify(statsMock, atLeastOnce()).incrementEvaluationCount();
    verify(statsMock, atLeastOnce()).addLatencyMeasurement(anyLong());
  }

  @Test
  public void whenAnInterruptExceptionIsThrown_thenNoErrorsMetricsShouldBeCaptured() throws Throwable {
    setExecutionCount(10000);
    mockInterruptEvaluationFailures(5);
    task = new EvaluationTask(statementMock, null, terminatorMock, statsMock, 0);
    task.run();
    verify(statsMock, times(9995)).incrementEvaluationCount();
    verify(statsMock, times(9995)).addLatencyMeasurement(anyLong());
    verify(statsMock, never()).incrementErrorCount();
  }

  private void mockEvaluationFailures(int desiredFailureCount) throws Throwable {
    AtomicInteger executions = new AtomicInteger();
    doAnswer(invocation -> {
      if (executions.getAndIncrement() < desiredFailureCount) {
        throw new RuntimeException("mock exception");
      }
      return null;
    }).when(statementMock).evaluate();

  }

  private void mockInterruptEvaluationFailures(int desiredFailureCount) throws Throwable {
    AtomicInteger executions = new AtomicInteger();
    doAnswer(invocation -> {
      if (executions.getAndIncrement() < desiredFailureCount) {
        throw new InterruptedException("mock exception");
      }
      return null;
    }).when(statementMock).evaluate();

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
