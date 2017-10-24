package com.noconnor.junitperf.statistics.utils;

import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import com.google.common.collect.ImmutableMap;
import com.noconnor.junitperf.BaseTest;
import com.noconnor.junitperf.statistics.Statistics;

import static com.noconnor.junitperf.statistics.utils.StatisticsUtils.*;
import static org.hamcrest.Matchers.anEmptyMap;
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

  @Test
  public void whenParsingPercentileLimits_thenValidLimitsShouldBeParsedCorrectly() {
    Map<Integer, Float> limits = parsePercentileLimits("90:2,95:5,99:6.7");
    Map<Integer, Float> expected = ImmutableMap.of(90, 2F, 95, 5F, 99, 6.7F);
    assertThat(limits, is(expected));
  }

  @Test
  public void whenParsingPercentileLimits_andPercentilesAreNullOrEmptyOrBlank_thenEmptyLimitsShouldBeReturned() {
    assertThat(parsePercentileLimits(""), is(anEmptyMap()));
    assertThat(parsePercentileLimits("  "), is(anEmptyMap()));
    assertThat(parsePercentileLimits(null), is(anEmptyMap()));
  }

  @Test
  public void whenParsingPercentileLimits_andEntriesAreInvalid_thenInvalidEntriesShouldBeFiltered() {
    assertThat(parsePercentileLimits("90:,95:5"), is(ImmutableMap.of(95, 5F)));
    assertThat(parsePercentileLimits("90:part,94:5"), is(ImmutableMap.of(94, 5F)));
    assertThat(parsePercentileLimits("90:1.2,ss:5"), is(ImmutableMap.of(90, 1.2F)));
    assertThat(parsePercentileLimits("90:dd,ss:5"), is(anEmptyMap()));
    assertThat(parsePercentileLimits("90.444:1.2,ss:5"), is(anEmptyMap()));
    assertThat(parsePercentileLimits("90.666:1.2,ss653:5"), is(anEmptyMap()));
    assertThat(parsePercentileLimits("90,,,,ss653:5,7:9"), is(ImmutableMap.of(7, 9F)));
  }

}
