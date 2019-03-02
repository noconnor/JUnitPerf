package com.github.noconnor.junitperf.data;

import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.lang.Thread.sleep;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class TestContextTest {

  @Mock
  private StatisticsCalculator calculatorMock;

  private TestContext context;

  @Before
  public void setup(){
    context = new TestContext(calculatorMock);
  }

  @Test
  public void whenSuccessIsCalled_thenStatsEvaluationCountShouldBeUpdated(){
    context.success();
    verify(calculatorMock).incrementEvaluationCount();
  }

  @Test
  public void whenSuccessIsCalled_thenStatsAddLatencyMeasurementShouldBeUpdatedWithTheCorrectTime() throws InterruptedException {
    sleep(10);
    context.success();
    ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
    verify(calculatorMock).addLatencyMeasurement(captor.capture());
    assertThat(captor.getValue(), is(greaterThanOrEqualTo(10L)));
  }

  @Test
  public void whenFailIsCalled_thenStatsEvaluationCountShouldBeUpdated(){
    context.fail();
    verify(calculatorMock).incrementEvaluationCount();
  }

  @Test
  public void whenFailIsCalled_thenStatsAddLatencyMeasurementShouldBeUpdatedWithTheCorrectTime() throws InterruptedException {
    sleep(5);
    context.fail();
    ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
    verify(calculatorMock).addLatencyMeasurement(captor.capture());
    assertThat(captor.getValue(), is(greaterThanOrEqualTo(5L)));
  }

  @Test
  public void whenFailIsCalled_thenStatsEvaluationErrorCountShouldBeUpdated(){
    context.fail();
    verify(calculatorMock).incrementErrorCount();
  }

}
