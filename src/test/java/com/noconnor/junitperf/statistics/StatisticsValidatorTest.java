package com.noconnor.junitperf.statistics;

import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import com.google.common.collect.ImmutableMap;
import com.noconnor.junitperf.BaseTest;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

public class StatisticsValidatorTest extends BaseTest {

  private static final int THROUGHPUT = 1_000;
  private static final float ALLOWED_ERRORS = 0.1F;
  private static final int DURATION_MS = 1_000;
  private static final ImmutableMap<Integer, Float> PERCENTILE_REQUIREMENTS = ImmutableMap.of(90, 5F, 99, 10F);

  private StatisticsValidator validator;

  @Mock
  private Statistics statisticsMock;

  @Before
  public void setup() {
    validator = new StatisticsValidator();
  }

  @Test
  public void whenValidatingStatistics_andThroughputIsAboveThreshold_thenValidationShouldPass() {
    when(statisticsMock.getEvaluationCount()).thenReturn((long)THROUGHPUT * DURATION_MS);
    assertThat(validator.isThroughputTargetAchieved(statisticsMock, DURATION_MS, THROUGHPUT), is(true));
  }

  @Test
  public void whenValidatingStatistics_andThroughputIsBelowThreshold_thenValidationShouldFail() {
    when(statisticsMock.getEvaluationCount()).thenReturn(20L);
    assertThat(validator.isThroughputTargetAchieved(statisticsMock, DURATION_MS, THROUGHPUT), is(false));
  }

  @Test
  public void whenValidatingStatistics_andErrorRateIsAboveThreshold_thenValidationShouldFail() {
    when(statisticsMock.getEvaluationCount()).thenReturn(1_000L);
    when(statisticsMock.getErrorCount()).thenReturn(500L);
    assertThat(validator.isErrorThresholdTargetAchieved(statisticsMock, ALLOWED_ERRORS), is(false));
  }

  @Test
  public void whenValidatingStatistics_andErrorRateIsBelowThreshold_thenValidationShouldPass() {
    when(statisticsMock.getEvaluationCount()).thenReturn(1_000L);
    when(statisticsMock.getErrorCount()).thenReturn(100L);
    assertThat(validator.isErrorThresholdTargetAchieved(statisticsMock, ALLOWED_ERRORS), is(true));
  }

  @Test
  public void whenValidatingStatistics_andPercentileLimitsHavenBeenMet_thenValidationShouldPass() {
    when(statisticsMock.getLatencyPercentile(90)).thenReturn(NANOSECONDS.convert(5, MILLISECONDS));
    when(statisticsMock.getLatencyPercentile(99)).thenReturn(NANOSECONDS.convert(10, MILLISECONDS));
    Map<Integer, Boolean> results = validator.evaluateLatencyPercentiles(statisticsMock, PERCENTILE_REQUIREMENTS);
    assertThat(results.get(90), is(true));
    assertThat(results.get(99), is(true));
  }

  @Test
  public void whenValidatingStatistics_andSomePercentileLimitsHavenBeenMet_thenValidationShouldPassAndFail() {
    when(statisticsMock.getLatencyPercentile(90)).thenReturn(NANOSECONDS.convert(5, MILLISECONDS));
    when(statisticsMock.getLatencyPercentile(99)).thenReturn(NANOSECONDS.convert(20, MILLISECONDS));
    Map<Integer, Boolean> results = validator.evaluateLatencyPercentiles(statisticsMock, PERCENTILE_REQUIREMENTS);
    assertThat(results.get(90), is(true));
    assertThat(results.get(99), is(false));
  }

}
