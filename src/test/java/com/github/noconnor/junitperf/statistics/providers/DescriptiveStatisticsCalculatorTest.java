package com.github.noconnor.junitperf.statistics.providers;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Spy;
import com.github.noconnor.junitperf.BaseTest;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

public class DescriptiveStatisticsCalculatorTest extends BaseTest {

  private DescriptiveStatisticsCalculator evaluator;

  @Spy
  private DescriptiveStatistics statsMock;

  @Before
  public void setup() {
    evaluator = new DescriptiveStatisticsCalculator(statsMock);
  }

  @Test
  public void whenCallingAddLatencyMeasurement_thenStatsShouldBeUpdated() {
    evaluator.addLatencyMeasurement(20345L);
    verify(statsMock).addValue(20345L);
  }

  @Test
  public void whenCallingGetErrorCount_thenValidErrorCountShouldBeReturned() {
    evaluator.incrementErrorCount();
    evaluator.incrementErrorCount();
    assertThat(evaluator.getErrorCount(), is(2L));
  }

  @Test
  public void whenCallingGetEvaluationCount_thenValidEvaluationCountShouldBeReturned() {
    evaluator.incrementEvaluationCount();
    evaluator.incrementEvaluationCount();
    assertThat(evaluator.getEvaluationCount(), is(2L));
  }

  @Test
  public void whenCallingGetMaxLatency_thenMaxLatencyShouldBeReturned() {
    evaluator.addLatencyMeasurement(10);
    evaluator.addLatencyMeasurement(1000);
    assertThat(evaluator.getMaxLatency(NANOSECONDS), is(1000F));
  }

  @Test
  public void whenCallingGetMaxLatency_thenMaxLatencyShouldBeAdjustedToMatchSpecifiedUnits() {
    evaluator.addLatencyMeasurement(10);
    evaluator.addLatencyMeasurement(1000);
    assertThat(evaluator.getMaxLatency(NANOSECONDS), is(1000F));
    assertThat(evaluator.getMaxLatency(MILLISECONDS), is(0.001000F));
  }

  @Test
  public void whenCallingGetMinLatency_thenMinLatencyShouldBeReturned() {
    evaluator.addLatencyMeasurement(10);
    evaluator.addLatencyMeasurement(1000);
    assertThat(evaluator.getMinLatency(NANOSECONDS), is(10F));
  }

  @Test
  public void whenCallingGetMinLatency_thenMinLatencyShouldBeAdjustedToMatchSpecifiedUnits() {
    evaluator.addLatencyMeasurement(10);
    evaluator.addLatencyMeasurement(1000);
    assertThat(evaluator.getMinLatency(NANOSECONDS), is(10F));
    assertThat(evaluator.getMinLatency(MILLISECONDS), is(0.00001F));
  }

  @Test
  public void whenCallingGetMeanLatency_thenMeanLatencyShouldBeReturned() {
    evaluator.addLatencyMeasurement(10);
    evaluator.addLatencyMeasurement(1000);
    assertThat(evaluator.getMeanLatency(NANOSECONDS), is(505F));
  }

  @Test
  public void whenCallingGetMeanLatency_thenMeanLatencyShouldBeAdjustedToMatchSpecifiedUnits() {
    evaluator.addLatencyMeasurement(10);
    evaluator.addLatencyMeasurement(1000);
    assertThat(evaluator.getMeanLatency(NANOSECONDS), is(505F));
    assertThat(evaluator.getMeanLatency(MILLISECONDS), is(0.000505F));
  }

  @Test
  public void whenCallingGetPercentile_thenValidPercentileShouldBeReturned() {
    evaluator.addLatencyMeasurement(20345L);
    assertThat(evaluator.getLatencyPercentile(99, NANOSECONDS), is(20345F));
  }

  @Test
  public void whenCallingGetPercentile_thenValidPercentileShouldBeAdjustedToMatchSpecifiedUnits() {
    evaluator.addLatencyMeasurement(20345L);
    assertThat(evaluator.getLatencyPercentile(99, NANOSECONDS), is(20345F));
    assertThat(evaluator.getLatencyPercentile(99, MILLISECONDS), is(0.020345F));
  }

  @Test
  public void whenCallingGetPercentageError_thenPercentageErrorShouldBeCalculated() {
    evaluator.incrementErrorCount();
    evaluator.incrementEvaluationCount();
    evaluator.incrementEvaluationCount();
    assertThat(evaluator.getErrorPercentage(), is(50F));
  }

  @Test
  public void whenEvaluationCountIsZero_andGetPercentageErrorIsCalled_thenZeroShouldBeReturned() {
    assertThat(evaluator.getErrorPercentage(), is(0F));
  }

  @Test
  public void whenMeanLatencyIsZero_andGetMeanLatencyIsCalled_thenZeroShouldBeReturned() {
    assertThat(evaluator.getMeanLatency(MILLISECONDS), is(0F));
  }

  @Test
  public void whenMinLatencyIsZero_andGetMinLatencyIsCalled_thenZeroShouldBeReturned() {
    assertThat(evaluator.getMinLatency(MILLISECONDS), is(0F));
  }

  @Test
  public void whenMaxLatencyIsZero_andGetMaxLatencyIsCalled_thenZeroShouldBeReturned() {
    assertThat(evaluator.getMaxLatency(MILLISECONDS), is(0F));
  }

  @Test
  public void whenLatencyPercentilesAreZero_andGetLatencyPercentileIsCalled_thenZeroShouldBeReturned() {
    assertThat(evaluator.getLatencyPercentile(90, MILLISECONDS), is(0F));
  }

}
