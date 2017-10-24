package com.noconnor.junitperf.statistics.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import com.noconnor.junitperf.BaseTest;
import com.noconnor.junitperf.statistics.Statistics;

import static com.noconnor.junitperf.statistics.utils.StatisticsUtils.calculatePercentageError;
import static com.noconnor.junitperf.statistics.utils.StatisticsUtils.calculateThroughputPerSecond;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class StatisticsUtilsTest extends BaseTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Mock
  private Statistics statistics;

  @Test
  public void whenCalculatingThroughput_thenValidThroughputShouldBeReturned() {
    when(statistics.getEvaluationCount()).thenReturn(1_000L);
    assertThat(calculateThroughputPerSecond(statistics, 1000), is(1_000F));
  }

  @Test
  public void whenCalculatingThroughput_andDurationIsZero_thenExceptionShouldBeThrown() {
    when(statistics.getEvaluationCount()).thenReturn(1_000L);
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Duration must be > 0 [duration:0]");
    assertThat(calculateThroughputPerSecond(statistics, 0), is(1_000F));
  }

  @Test
  public void whenCalculatingThroughput_andDurationIsNegative_thenExceptionShouldBeThrown() {
    when(statistics.getEvaluationCount()).thenReturn(1_000L);
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Duration must be > 0 [duration:-1]");
    assertThat(calculateThroughputPerSecond(statistics, -1), is(1_000F));
  }

  @Test
  public void whenCalculatingThroughput_andEvaluationCountIsZero_thenReturnZero() {
    when(statistics.getEvaluationCount()).thenReturn(0L);
    assertThat(calculateThroughputPerSecond(statistics, 1000), is(0F));
  }

  @Test
  public void whenCalculatingThroughput_andEvaluationCountIsNegative_thenExceptionShouldBeThrown() {
    when(statistics.getEvaluationCount()).thenReturn(-1L);
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Evaluation count must be > 0 [count:-1]");
    assertThat(calculateThroughputPerSecond(statistics, 1000), is(0F));
  }

  @Test
  public void whenCalculatingPercentageError_thenValidPercentageErrorShouldBeReturned() {
    when(statistics.getEvaluationCount()).thenReturn(1_000L);
    when(statistics.getErrorCount()).thenReturn(500L);
    assertThat(calculatePercentageError(statistics), is(0.5F));
  }

  @Test
  public void whenCalculatingPercentageError_andEvaluationCountIsZero_thenValidPercentageErrorShouldBeZero() {
    when(statistics.getEvaluationCount()).thenReturn(0L);
    when(statistics.getErrorCount()).thenReturn(0L);
    assertThat(calculatePercentageError(statistics), is(0F));
  }

  @Test
  public void whenCalculatingPercentageError_andEvaluationCountIsLessThanErrorCount_thenExceptionShouldBeThrown() {
    when(statistics.getEvaluationCount()).thenReturn(0L);
    when(statistics.getErrorCount()).thenReturn(500L);
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Evaluation count must be > error [count:0, error:500]");
    assertThat(calculatePercentageError(statistics), is(0F));
  }

}
