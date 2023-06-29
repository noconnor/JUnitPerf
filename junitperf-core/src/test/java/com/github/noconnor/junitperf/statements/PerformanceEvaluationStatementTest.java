package com.github.noconnor.junitperf.statements;

import com.github.noconnor.junitperf.BaseTest;
import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import com.google.common.collect.ImmutableMap;
import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PerformanceEvaluationStatementTest extends BaseTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Mock
  private TestStatement baseStatementMock;

  @Mock
  private ThreadFactory threadFactoryMock;

  @Mock
  private EvaluationContext contextMock;

  @Mock
  private Thread threadMock;

  @Mock
  private Consumer<Void> listenerMock;

  private PerformanceEvaluationStatement statement;

  @Mock
  private StatisticsCalculator statisticsCalculatorMock;

  @Before
  public void setup() {
    ExceptionsRegistry.clearRegistry();
    initialiseThreadFactoryMock();
    initialiseContext();
    statement = PerformanceEvaluationStatement.builder()
      .baseStatement(baseStatementMock)
      .statistics(statisticsCalculatorMock)
      .threadFactory(threadFactoryMock)
      .context(contextMock)
      .listener(listenerMock)
      .build();
  }

  @Test
  public void whenEvaluatingABaseStatement_thenTheCorrectNumberOfThreadsShouldBeStarted() throws Throwable {
    when(contextMock.getConfiguredThreads()).thenReturn(10);
    statement.runParallelEvaluation();
    verify(threadFactoryMock, times(10)).newThread(any(Runnable.class));
    verify(threadMock, times(10)).start();
  }

  @Test
  public void whenEvaluatingABaseStatement_thenTheTestShouldEndWhenTheTestDurationExpires() throws Throwable {
    when(contextMock.getConfiguredDuration()).thenReturn(100);
    long starTimeNs = currentTimeMillis();
    statement.runParallelEvaluation();
    assertThat((currentTimeMillis() - starTimeNs), is(greaterThan(95L)));
    assertThat((currentTimeMillis() - starTimeNs), is(lessThan(3 * 100L)));
    verify(threadMock, times(1)).interrupt();
  }

  @Test
  public void whenEvaluationCompletes_thenTheContextShouldBeUpdatedWithStatistics() throws Throwable {
    statement.runParallelEvaluation();
    verify(contextMock).setStatistics(any(StatisticsCalculator.class));
  }

  @Test
  public void whenEvaluationCompletes_thenTheContextShouldBeUpdatedWithFinishTime() throws Throwable {
    statement.runParallelEvaluation();
    verify(contextMock).setFinishTimeNs(anyLong());
  }

  @Test
  public void whenEvaluationCompletes_thenTheContextValidationShouldBeTriggered() throws Throwable {
    statement.runParallelEvaluation();
    verify(contextMock).runValidation();
  }

  @Test
  public void whenEvaluationCompletes_thenTheListenerShouldBeNotified() throws Throwable {
    statement.runParallelEvaluation();
    verify(listenerMock).accept(null);
  }

  @Test
  public void whenEvaluationCompletes_andValidationFails_thenTheListenerShouldStillBeNotified() throws Throwable {
    when(contextMock.isThroughputAchieved()).thenReturn(false);
    try {
      statement.runParallelEvaluation();
      fail("Assertion expected during validation");
    } catch (Error e) {
      assertThat(e.getMessage(), startsWith("Test throughput threshold not achieved"));
    }
    verify(listenerMock).accept(null);
  }

  @Test
  public void whenEvaluationCompletes_andThroughputValidationFails_thenAssertionShouldBeGenerated() throws Throwable {
    when(contextMock.isThroughputAchieved()).thenReturn(false);
    try {
      statement.runParallelEvaluation();
      fail("Assertion expected during validation");
    } catch (Error e) {
      assertThat(e.getMessage(), startsWith("Test throughput threshold not achieved"));
    }
  }

  @Test
  public void whenEvaluationCompletes_andMinLatencyValidationFails_thenAssertionShouldBeGenerated() throws Throwable {
    when(contextMock.isMinLatencyAchieved()).thenReturn(false);
    try {
      statement.runParallelEvaluation();
      fail("Assertion expected during validation");
    } catch (Error e) {
      assertThat(e.getMessage(), startsWith("Test min latency threshold not achieved"));
    }
  }

  @Test
  public void whenEvaluationCompletes_andMeanLatencyValidationFails_thenAssertionShouldBeGenerated() throws Throwable {
    when(contextMock.isMeanLatencyAchieved()).thenReturn(false);
    try {
      statement.runParallelEvaluation();
      fail("Assertion expected during validation");
    } catch (Error e) {
      assertThat(e.getMessage(), startsWith("Test mean latency threshold not achieved"));
    }
  }

  @Test
  public void whenEvaluationCompletes_andMaxLatencyValidationFails_thenAssertionShouldBeGenerated() throws Throwable {
    when(contextMock.isMaxLatencyAchieved()).thenReturn(false);
    try {
      statement.runParallelEvaluation();
      fail("Assertion expected during validation");
    } catch (Error e) {
      assertThat(e.getMessage(), startsWith("Test max latency threshold not achieved"));
    }
  }

  @Test
  public void whenEvaluationCompletes_andErrorValidationFails_thenAssertionShouldBeGenerated() throws Throwable {
    when(contextMock.isErrorThresholdAchieved()).thenReturn(false);
    try {
      statement.runParallelEvaluation();
      fail("Assertion expected during validation");
    } catch (Error e) {
      assertThat(e.getMessage(), startsWith("Error threshold not achieved"));
    }
  }

  @Test
  public void whenEvaluationCompletes_andPercentileLatencyValidationFails_thenAssertionShouldBeGenerated() throws Throwable {
    when(contextMock.getPercentileResults()).thenReturn(ImmutableMap.of(90, true, 95, false));
    try {
      statement.runParallelEvaluation();
      fail("Assertion expected during validation");
    } catch (Error e) {
      assertThat(e.getMessage(), startsWith("95th Percentile has not achieved required threshold"));
    }
  }

  @Test
  public void whenCreatingEvaluationTasks_thenIsAsyncEvaluationShouldBeChecked() throws Throwable {
    statement.runParallelEvaluation();
    verify(contextMock).isAsyncEvaluation();
  }

  @Test
  public void whenCreatingEvaluationTasks_andIsAsyncEvaluationIsTrue_thenTaskThreadShouldBeCreated() throws Throwable {
    when(contextMock.isAsyncEvaluation()).thenReturn(true);
    statement.runParallelEvaluation();
    verify(threadFactoryMock).newThread(any(Runnable.class));
  }

  @Test
  public void whenRunningEvaluation_thenStatisticsShouldBeReset() throws Throwable {
    statement.runParallelEvaluation();
    statement.runParallelEvaluation();
    statement.runParallelEvaluation();
    verify(statisticsCalculatorMock, times(3)).reset();
  }

  @Test
  public void whenBaseStatementThrowsAnAbortException_thenExceptionShouldBeReThrown() throws Throwable {

    ExceptionsRegistry.registerAbort(AssumptionViolatedException.class);

    ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
    when(threadFactoryMock.newThread(captor.capture())).thenReturn(threadMock);
    doAnswer((invocation) -> {
      captor.getValue().run();
      return null;
    }).when(threadMock).start();
    
    AssumptionViolatedException abort = new AssumptionViolatedException("unittest"); 
    doThrow(abort).when(baseStatementMock).evaluate();
    when(contextMock.isAborted()).thenReturn(true);
    when(contextMock.getAbortedException()).thenReturn(abort);
    
    try {
      statement.runParallelEvaluation();
    } catch (AssumptionViolatedException e) {
      // expected
    } catch (Throwable t) {
      fail("Unexpected exception");
    } finally {
      verify(contextMock).setAbortedException(abort);
      verify(listenerMock).accept(null);
      verify(contextMock, never()).runValidation();
    }
  }

  private void initialiseThreadFactoryMock() {
    when(threadFactoryMock.newThread(any(Runnable.class))).thenReturn(threadMock);
  }

  private void initialiseContext() {
    when(contextMock.getConfiguredThreads()).thenReturn(1);
    when(contextMock.getConfiguredDuration()).thenReturn(100);
    when(contextMock.isErrorThresholdAchieved()).thenReturn(true);
    when(contextMock.isThroughputAchieved()).thenReturn(true);
    when(contextMock.isMaxLatencyAchieved()).thenReturn(true);
    when(contextMock.isMinLatencyAchieved()).thenReturn(true);
    when(contextMock.isMeanLatencyAchieved()).thenReturn(true);
    when(contextMock.getPercentileResults()).thenReturn(emptyMap());
  }

}
