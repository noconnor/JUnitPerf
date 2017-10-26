package com.noconnor.junitperf.data;

import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import com.google.common.collect.ImmutableMap;
import com.noconnor.junitperf.BaseTest;
import com.noconnor.junitperf.JUnitPerfTest;
import com.noconnor.junitperf.JUnitPerfTestRequirement;
import com.noconnor.junitperf.statistics.Statistics;

import static java.util.Collections.emptyMap;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class EvaluationContextTest extends BaseTest {

  private static final String TEST_NAME = "UNITTEST";

  private EvaluationContext context;

  @Mock
  private Statistics statisticsMock;

  @Mock
  private JUnitPerfTest perfTestAnnotation;

  @Mock
  private JUnitPerfTestRequirement perfTestRequirement;

  @Before
  public void setup() {
    initialisePerfTestAnnotation();
    initialisePerfTestRequirementAnnotation();
    initialiseStatisticsMockToPassValidation();
    context = new EvaluationContext(TEST_NAME, "TEST");
  }

  @Test
  public void whenLoadingJUnitPerfTestSettings_thenAppropriateContextSettingsShouldBeUpdated() {
    context.loadConfiguration(perfTestAnnotation);
    assertThat(context.getConfiguredDuration(), is(perfTestAnnotation.duration()));
    assertThat(context.getConfiguredRateLimit(), is(perfTestAnnotation.rateLimit()));
    assertThat(context.getConfiguredThreads(), is(perfTestAnnotation.threads()));
    assertThat(context.getConfiguredWarmUp(), is(perfTestAnnotation.warmUp()));
  }

  @Test(expected = NullPointerException.class)
  public void whenLoadingJUnitPerfTestSettings_andSettingsAreNull_thenExceptionShouldBeThrown() {
    context.loadConfiguration(null);
  }

  @Test
  public void whenLoadingJUnitPerfTestRequirements_thenAppropriateContextSettingsShouldBeUpdated() {
    context.loadRequirements(perfTestRequirement);
    assertThat(context.isValidationRequired(), is(true));
    assertThat(context.getRequiredAllowedErrorsRate(), is(perfTestRequirement.allowedErrorsRate()));
    assertThat(context.getRequiredThroughput(), is(perfTestRequirement.throughput()));
    assertThat(context.getRequiredPercentiles(), is(ImmutableMap.of(90, 0.5F, 95, 9F)));
  }

  @Test
  public void whenLoadingJUnitPerfTestRequirements_andRequirementsAreNull_thenValidationShouldNotBeRequired() {
    context.loadRequirements(null);
    assertThat(context.isValidationRequired(), is(false));
  }

  @Test(expected = IllegalStateException.class)
  public void whenRunningEvaluation_andStatisticsAreNull_thenIllegalStateExceptionShouldBeThrown() {
    context.runValidation();
  }

  @Test
  public void whenRunningEvaluation_thenThroughputRequirementsShouldBeChecked() {
    initialiseContext();
    context.runValidation();
    assertThat(context.isThroughputAchieved(), is(true));
    assertThat(context.isSuccessful(), is(true));
  }

  @Test
  public void whenRunningEvaluation_andThroughputIsBelowThreshold_thenThroughputAchievedFlagShouldBeFalse() {
    when(statisticsMock.getEvaluationCount()).thenReturn(10L);
    initialiseContext();
    context.runValidation();
    assertThat(context.isThroughputAchieved(), is(false));
    assertThat(context.isSuccessful(), is(false));
  }

  @Test
  public void whenRunningEvaluation_andEvaluationCountIsGreaterThanDuration_thenThroughputShouldBeCalculatedCorrectly() {
    when(statisticsMock.getEvaluationCount()).thenReturn(1000L);
    when(perfTestAnnotation.duration()).thenReturn(100);
    initialiseContext();
    context.runValidation();
    assertThat(context.getThroughputQps(), is(10_000L));
  }

  @Test
  public void whenRunningEvaluation_thenAllowedErrorsRequirementsShouldBeChecked() {
    initialiseContext();
    context.runValidation();
    assertThat(context.isErrorThresholdAchieved(), is(true));
    assertThat(context.isSuccessful(), is(true));
  }

  @Test
  public void whenRunningEvaluation_andAllowedErrorRateFails_thenIsSuccessfulShouldBeFalse() {
    when(statisticsMock.getErrorPercentage()).thenReturn(60F);
    initialiseContext();
    context.runValidation();
    assertThat(context.isErrorThresholdAchieved(), is(false));
    assertThat(context.isSuccessful(), is(false));
  }

  @Test
  public void whenRunningEvaluation_thenPercentileRequirementsShouldBeChecked() {
    Map<Integer, Boolean> validationResults = ImmutableMap.of(90, true, 95, true);
    initialiseContext();
    context.runValidation();
    assertThat(context.getPercentileResults(), is(validationResults));
  }

  @Test
  public void whenRunningEvaluation_andAPercentileThresholdIsNotMet_thenIsSuccessfulShouldReturnFalse() {
    when(statisticsMock.getLatencyPercentile(90, NANOSECONDS)).thenReturn(1000000F);
    Map<Integer, Boolean> validationResults = ImmutableMap.of(90, false, 95, true);
    initialiseContext();
    context.runValidation();
    assertThat(context.getPercentileResults(), is(validationResults));
    assertThat(context.isSuccessful(), is(false));
  }

  @Test
  public void whenRunningEvaluation_andAllThresholdsAreMet_thenIsSuccessfulShouldBeTrue() {
    initialiseContext();
    context.runValidation();
    assertThat(context.isSuccessful(), is(true));
  }

  @Test
  public void whenRunningEvaluation_andNoValidationIsRequired_thenNoValidationsShouldBeReturned() {
    context.loadConfiguration(perfTestAnnotation);
    context.setStatistics(statisticsMock);
    context.runValidation();
    assertThat(context.isSuccessful(), is(true));
  }

  @Test
  public void whenParsingPercentileLimits_thenValidLimitsShouldBeParsedCorrectly() {
    context.loadConfiguration(perfTestAnnotation);
    loadPercentilesAndAssertParsedCorrectly("90:2,95:5,99:6.7", ImmutableMap.of(90, 2F, 95, 5F, 99, 6.7F));
  }

  @Test
  public void whenParsingPercentileLimits_andPercentilesAreNullOrEmptyOrBlank_thenEmptyLimitsShouldBeReturned() {
    context.loadConfiguration(perfTestAnnotation);
    loadPercentilesAndAssertParsedCorrectly("", emptyMap());
    loadPercentilesAndAssertParsedCorrectly("  ", emptyMap());
    loadPercentilesAndAssertParsedCorrectly(null, emptyMap());
  }

  @Test
  public void whenParsingPercentileLimits_andEntriesAreInvalid_thenInvalidEntriesShouldBeFiltered() {
    context.loadConfiguration(perfTestAnnotation);
    loadPercentiles("");
    loadPercentilesAndAssertParsedCorrectly("90:,95:5", ImmutableMap.of(95, 5F));
    loadPercentilesAndAssertParsedCorrectly("90:part,94:5", ImmutableMap.of(94, 5F));
    loadPercentilesAndAssertParsedCorrectly("90:1.2,ss:5", ImmutableMap.of(90, 1.2F));
    loadPercentilesAndAssertParsedCorrectly("90:dd,ss:5", emptyMap());
    loadPercentilesAndAssertParsedCorrectly("90.444:1.2,ss:5", emptyMap());
    loadPercentilesAndAssertParsedCorrectly("90.666:1.2,ss653:5", emptyMap());
    loadPercentilesAndAssertParsedCorrectly("90,,,,ss653:5,7:9", ImmutableMap.of(7, 9F));
  }

  @Test
  public void whenCalculatingThroughputQps_thenCorrectValueShouldBeCalculated() {
    initialiseContext();
    long expected = (statisticsMock.getEvaluationCount() / perfTestAnnotation.duration()) * 1000;
    assertThat(context.getThroughputQps(), is(expected));
  }

  private void initialiseContext() {
    context.loadConfiguration(perfTestAnnotation);
    context.loadRequirements(perfTestRequirement);
    context.setStatistics(statisticsMock);
  }

  private void initialisePerfTestAnnotation() {
    when(perfTestAnnotation.duration()).thenReturn(10);
    when(perfTestAnnotation.rateLimit()).thenReturn(1_000);
    when(perfTestAnnotation.threads()).thenReturn(50);
    when(perfTestAnnotation.warmUp()).thenReturn(500);
  }

  private void initialisePerfTestRequirementAnnotation() {
    when(perfTestRequirement.throughput()).thenReturn(10_000);
    when(perfTestRequirement.allowedErrorsRate()).thenReturn(0.5f);
    when(perfTestRequirement.percentiles()).thenReturn("90:0.5,95:9");
  }

  private void initialiseStatisticsMockToPassValidation() {
    when(statisticsMock.getEvaluationCount()).thenReturn(15_000L);
    when(statisticsMock.getErrorCount()).thenReturn(1L);
    when(statisticsMock.getErrorPercentage()).thenReturn(1.0F);
    when(statisticsMock.getLatencyPercentile(90, NANOSECONDS)).thenReturn(2000F);
    when(statisticsMock.getLatencyPercentile(95, NANOSECONDS)).thenReturn(4000F);
  }

  private void loadPercentilesAndAssertParsedCorrectly(String percentiles, Map<Integer, Float> expected) {
    loadPercentiles(percentiles);
    assertThat(context.getRequiredPercentiles(), is(expected));
  }

  private void loadPercentiles(String percentiles) {
    when(perfTestRequirement.percentiles()).thenReturn(percentiles);
    context.loadRequirements(perfTestRequirement);
  }

}
