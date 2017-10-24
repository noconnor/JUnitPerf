package com.noconnor.junitperf.statistics;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import com.noconnor.junitperf.BaseTest;
import com.noconnor.junitperf.statistics.StatisticsValidator.ValidationResult;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

public class StatisticsValidatorTest extends BaseTest {

  private static final int THROUGHPUT = 1_000;
  private static final float ALLOWED_ERRORS = 0.1F;
  private static final String PERCENTILES = "90:5,99:10";
  private static final int DURATION_MS = 1_000;

  private StatisticsValidator validator;

  @Mock
  private Statistics statisticsMock;

  @Before
  public void setup() {
    validator = new StatisticsValidator(THROUGHPUT, ALLOWED_ERRORS, PERCENTILES, DURATION_MS);
  }

  @Test
  public void whenValidatingStatistics_andThroughputIsAboveThreshold_thenValidationShouldPass() {
    when(statisticsMock.getEvaluationCount()).thenReturn((long)THROUGHPUT * DURATION_MS);
    ValidationResult result = validator.validate(statisticsMock);
    assertThat(result.isThroughputAchieved(), is(true));
  }

  @Test
  public void whenValidatingStatistics_andThroughputIsBelowThreshold_thenValidationShouldFail() {
    when(statisticsMock.getEvaluationCount()).thenReturn(20L);
    ValidationResult result = validator.validate(statisticsMock);
    assertThat(result.isThroughputAchieved(), is(false));
  }

  @Test
  public void whenValidatingStatistics_andErrorRateIsAboveThreshold_thenValidationFail() {
    when(statisticsMock.getEvaluationCount()).thenReturn(1_000L);
    when(statisticsMock.getErrorCount()).thenReturn(500L);
    ValidationResult result = validator.validate(statisticsMock);
    assertThat(result.isErrorThresholdAchieved(), is(false));
  }

  @Test
  public void whenValidatingStatistics_andErrorRateIsBelowThreshold_thenValidationPass() {
    when(statisticsMock.getEvaluationCount()).thenReturn(1_000L);
    when(statisticsMock.getErrorCount()).thenReturn(100L);
    ValidationResult result = validator.validate(statisticsMock);
    assertThat(result.isErrorThresholdAchieved(), is(true));
  }

}
