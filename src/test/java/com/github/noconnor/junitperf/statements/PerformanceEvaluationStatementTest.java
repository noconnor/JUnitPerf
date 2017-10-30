package com.github.noconnor.junitperf.statements;

import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import com.github.noconnor.junitperf.BaseTest;
import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import com.google.common.collect.ImmutableMap;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

  @Mock
  private Consumer<Void> listenerMock;

  private PerformanceEvaluationStatement statement;

  @Mock
  private StatisticsCalculator statisticsCalculatorMock;

  @Before
  public void setup() {
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

  @Test
  public void whenEvaluationCompletes_thenTheContextShouldBeUpdatedWithStatistics() throws Throwable {
    statement.evaluate();
    verify(contextMock).setStatistics(any(StatisticsCalculator.class));
  }

  @Test
  public void whenEvaluationCompletes_thenTheContextValidationShouldBeTriggered() throws Throwable {
    statement.evaluate();
    verify(contextMock).runValidation();
  }

  @Test
  public void whenEvaluationCompletes_thenTheListenerShouldBeNotified() throws Throwable {
    statement.evaluate();
    verify(listenerMock).accept(null);
  }

  @Test
  public void whenEvaluationCompletes_andValidationFails_thenTheListenerShouldStillBeNotified() throws Throwable {
    when(contextMock.isThroughputAchieved()).thenReturn(false);
    try {
      statement.evaluate();
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
      statement.evaluate();
      fail("Assertion expected during validation");
    } catch (Error e) {
      assertThat(e.getMessage(), startsWith("Test throughput threshold not achieved"));
    }
  }

  @Test
  public void whenEvaluationCompletes_andErrorValidationFails_thenAssertionShouldBeGenerated() throws Throwable {
    when(contextMock.isErrorThresholdAchieved()).thenReturn(false);
    try {
      statement.evaluate();
      fail("Assertion expected during validation");
    } catch (Error e) {
      assertThat(e.getMessage(), startsWith("Error threshold not achieved"));
    }
  }

  @Test
  public void whenEvaluationCompletes_andPercentileLatencyValidationFails_thenAssertionShouldBeGenerated() throws Throwable {
    when(contextMock.getPercentileResults()).thenReturn(ImmutableMap.of(90, true, 95, false));
    try {
      statement.evaluate();
      fail("Assertion expected during validation");
    } catch (Error e) {
      assertThat(e.getMessage(), startsWith("95th Percentile has not achieved required threshold"));
    }
  }

  private void initialiseThreadFactoryMock() {
    when(threadFactoryMock.newThread(any(EvaluationTask.class))).thenReturn(threadMock);
  }

  private void initialiseContext() {
    when(contextMock.getConfiguredThreads()).thenReturn(1);
    when(contextMock.getConfiguredDuration()).thenReturn(100);
    when(contextMock.isErrorThresholdAchieved()).thenReturn(true);
    when(contextMock.isThroughputAchieved()).thenReturn(true);
    when(contextMock.getPercentileResults()).thenReturn(emptyMap());
  }

}
