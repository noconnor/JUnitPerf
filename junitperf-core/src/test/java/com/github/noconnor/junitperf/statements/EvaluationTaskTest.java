package com.github.noconnor.junitperf.statements;

import com.github.noconnor.junitperf.BaseTest;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import com.github.noconnor.junitperf.statistics.providers.DescriptiveStatisticsCalculator;
import com.google.common.util.concurrent.RateLimiter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class EvaluationTaskTest extends BaseTest {

  private EvaluationTask task;

  @Mock
  private TestStatement statementMock;

  @Mock
  private RateLimiter rateLimiterMock;

  @Mock
  private Supplier<Boolean> terminatorMock;

  @Mock
  private StatisticsCalculator statsMock;

  @Before
  public void setup() {
    initialiseRateLimiterMock();
    task = new EvaluationTask(statementMock, rateLimiterMock, terminatorMock, statsMock, 0, 0);
  }

  @After
  public void teardown() {
    // clear thread interrupt
    Thread.interrupted();
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
    task = new EvaluationTask(statementMock, null, terminatorMock, statsMock, 0, 0);
    task.run();
    verify(statementMock, times(10)).evaluate();
  }

  @Test
  public void whenExecutionTargetIsReached_thenTestShouldFinishGracefully() throws Throwable {
    setExecutionCount(100);
    task = new EvaluationTask(statementMock, null, terminatorMock, new DescriptiveStatisticsCalculator(), 0, 9);
    task.run();
    verify(statementMock, times(9)).evaluate();
  }

  @Test
  public void whenWarmUpPeriodIsNonZero_thenNoMeasurementsShouldBeTaken() throws Throwable {
    setExecutionCount(10);
    task = new EvaluationTask(statementMock, null, terminatorMock, statsMock, 100, 0);
    task.run();
    verifyZeroInteractions(statsMock);
  }

  @Test
  public void whenWarmUpPeriodIsNonZero_andWarmupPeriodExpired_thenMeasurementsShouldBeTaken() throws Throwable {
    setExecutionCount(10000);
    task = new EvaluationTask(statementMock, null, terminatorMock, statsMock, 10, 0);
    task.run();
    verify(statsMock, atLeastOnce()).incrementEvaluationCount();
    verify(statsMock, atLeastOnce()).addLatencyMeasurement(anyLong());
  }

  @Test
  public void whenAnInterruptExceptionIsThrown_thenNoErrorsMetricsShouldBeCaptured() throws Throwable {
    setExecutionCount(10000);
    mockInterruptAfter(9995);
    task = new EvaluationTask(statementMock, null, terminatorMock, statsMock, 0, 0);
    task.run();
    verify(statsMock, times(9995)).incrementEvaluationCount();
    verify(statsMock, times(9995)).addLatencyMeasurement(anyLong());
    verify(statsMock, never()).incrementErrorCount();
  }

  @Test
  public void whenTerminationFlagIsSet_thenNoMoreExecutionsShouldBeEvaluated() throws Throwable {
    setExecutionCount(10000);
    mockTerminateAfter(9990);
    task = new EvaluationTask(statementMock, null, terminatorMock, statsMock, 0, 0);
    task.run();
    // termination flag will still finish executing current loop
    verify(statsMock, times(9991)).incrementEvaluationCount();
    verify(statsMock, times(9991)).addLatencyMeasurement(anyLong());
    verify(statsMock, never()).incrementErrorCount();
  }

  @Test
  public void whenRunning_andRunBeforesThrowsAnException_thenExceptionShouldBeThrown() throws Throwable {
    setExecutionCount(1);
    doThrow(new RuntimeException("test")).when(statementMock).runBefores();

    try {
      task.run();
      fail("Expected exception");
    } catch (IllegalStateException e){
      // expected
    } finally {
      verify(statementMock, never()).evaluate();
    }
  }

  @Test
  public void whenRunning_andRunBeforesThrowsAnInterruptedException_thenNoExceptionShouldBeThrown() throws Throwable {
    setExecutionCount(1);
    doThrow(new InterruptedException("test")).when(statementMock).runBefores();
    task.run();
    verify(statementMock).evaluate();
  }

  @Test
  public void whenRunning_andRunAftersThrowsAnInterruptedException_thenNoExceptionShouldBeThrown() throws Throwable {
    setExecutionCount(1);
    doThrow(new InterruptedException("test")).when(statementMock).runAfters();
    task.run();
    verify(statementMock).evaluate();
  }

  @Test
  public void whenRunning_andRunAftersThrowsAnException_thenExceptionShouldBeThrown() throws Throwable {
    setExecutionCount(1);
    doThrow(new RuntimeException("test")).when(statementMock).runAfters();

    try {
      task.run();
      fail("Expected exception");
    } catch (IllegalStateException e){
      // expected
    } finally {
      verify(statementMock).evaluate();
    }
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

  private void mockInterruptAfter(int desiredSuccessfulInvocations) throws Throwable {
    AtomicInteger executions = new AtomicInteger();
    doAnswer(invocation -> {
      if (executions.getAndIncrement() >= desiredSuccessfulInvocations) {
        throw new InterruptedException("mock exception");
      }
      return null;
    }).when(statementMock).evaluate();
  }

  private void mockTerminateAfter(int desiredSuccessfulInvocations) throws Throwable {
    AtomicInteger executions = new AtomicInteger();
    doAnswer(invocation -> {
      if (executions.getAndIncrement() >= desiredSuccessfulInvocations) {
        when(terminatorMock.get()).thenReturn(true);
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
