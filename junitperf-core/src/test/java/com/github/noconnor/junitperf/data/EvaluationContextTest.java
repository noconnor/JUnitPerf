package com.github.noconnor.junitperf.data;

import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import com.github.noconnor.junitperf.BaseTest;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.github.noconnor.junitperf.datetime.DatetimeUtils;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import com.google.common.collect.ImmutableMap;

import static java.lang.System.nanoTime;
import static java.util.Collections.emptyMap;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class EvaluationContextTest extends BaseTest {

  private static final String TEST_NAME = "UNITTEST";
  private static final String DATE_OVERRIDE = "2019-03-01 14:27:45";

  private EvaluationContext context;

  @Mock
  private StatisticsCalculator statisticsMock;

  @Mock
  private JUnitPerfTest perfTestAnnotation;

  @Mock
  private JUnitPerfTestRequirement perfTestRequirement;

  @Before
  public void setup() {
    initialisePerfTestAnnotation();
    initialisePerfTestRequirementAnnotation();
    initialiseStatisticsMockToPassValidation();
    DatetimeUtils.setOverride(DATE_OVERRIDE);
    context = new EvaluationContext(TEST_NAME, nanoTime());
  }

  @Test
  public void whenLoadingJUnitPerfTestSettings_thenAppropriateContextSettingsShouldBeUpdated() {
    context.loadConfiguration(perfTestAnnotation);
    assertThat(context.getConfiguredDuration(), is(perfTestAnnotation.durationMs()));
    assertThat(context.getConfiguredRateLimit(), is(perfTestAnnotation.maxExecutionsPerSecond()));
    assertThat(context.getConfiguredThreads(), is(perfTestAnnotation.threads()));
    assertThat(context.getConfiguredWarmUp(), is(perfTestAnnotation.warmUpMs()));
    assertThat(context.getConfiguredRampUpPeriodMs(), is(perfTestAnnotation.rampUpPeriodMs()));
  }

  @Test
  public void whenLoadingJUnitPerfTestSettings_thenDurationMsShouldBeSensible() {
    when(perfTestAnnotation.durationMs()).thenReturn(0);
    expectValidationError("DurationMs must be greater than 0ms");
    when(perfTestAnnotation.durationMs()).thenReturn(-50);
    expectValidationError("DurationMs must be greater than 0ms");
  }

  @Test
  public void whenLoadingJUnitPerfTestSettings_thenRampUpMsShouldBeGreaterThanZero() {
    when(perfTestAnnotation.rampUpPeriodMs()).thenReturn(-9);
    expectValidationError("RampUpPeriodMs must be >= 0ms");
  }

  @Test
  public void whenLoadingJUnitPerfTestSettings_thenRampUpMsShouldBeLessThanTestDuration() {
    when(perfTestAnnotation.rampUpPeriodMs()).thenReturn(60);
    when(perfTestAnnotation.durationMs()).thenReturn(50);
    expectValidationError("RampUpPeriodMs must be < DurationMs");
  }

  @Test
  public void whenLoadingJUnitPerfTestSettings_thenWarmUpMsShouldBeSensible() {
    when(perfTestAnnotation.warmUpMs()).thenReturn(-9);
    expectValidationError("WarmUpMs must be >= 0ms");
    when(perfTestAnnotation.durationMs()).thenReturn(50);
    when(perfTestAnnotation.warmUpMs()).thenReturn(55);
    expectValidationError("WarmUpMs must be < DurationMs");
  }

  @Test
  public void whenLoadingJUnitPerfTestSettings_thenThreadsShouldBeSensible() {
    when(perfTestAnnotation.threads()).thenReturn(-9);
    expectValidationError("Threads must be > 0");
    when(perfTestAnnotation.threads()).thenReturn(0);
    expectValidationError("Threads must be > 0");
  }

  @Test
  public void whenLoadingJUnitPerfTestSettings_thenMaxExecutionsPerSecondShouldBeSensible() {
    when(perfTestAnnotation.maxExecutionsPerSecond()).thenReturn(-9);
    expectValidationError("MaxExecutionsPerSecond must be > 0 or -1 (to disable)");
    when(perfTestAnnotation.maxExecutionsPerSecond()).thenReturn(0);
    expectValidationError("MaxExecutionsPerSecond must be > 0 or -1 (to disable)");
    // ALLOWED value
    when(perfTestAnnotation.maxExecutionsPerSecond()).thenReturn(-1);
    context.loadConfiguration(perfTestAnnotation);
  }

  @Test(expected = NullPointerException.class)
  public void whenLoadingJUnitPerfTestSettings_andSettingsAreNull_thenExceptionShouldBeThrown() {
    context.loadConfiguration(null);
  }

  @Test
  public void whenLoadingJUnitPerfTestRequirements_thenAppropriateContextSettingsShouldBeUpdated() {
    context.loadRequirements(perfTestRequirement);
    assertThat(context.getRequiredAllowedErrorsRate(), is(perfTestRequirement.allowedErrorPercentage()));
    assertThat(context.getRequiredThroughput(), is(perfTestRequirement.executionsPerSec()));
    assertThat(context.getRequiredPercentiles(), is(ImmutableMap.of(90, 0.5F, 95, 9F)));
  }

  @Test
  public void whenLoadingJUnitPerfTestRequirements_thenAllowedErrorPercentageShouldBeSensible() {
    when(perfTestRequirement.allowedErrorPercentage()).thenReturn(-1.6F);
    expectRequirementsValidationError("AllowedErrorPercentage must be >= 0");
  }

  @Test
  public void whenLoadingJUnitPerfTestRequirements_thenAExecutionsPerSecShouldBeSensible() {
    when(perfTestRequirement.executionsPerSec()).thenReturn(-1);
    expectRequirementsValidationError("ExecutionsPerSec must be >= 0");
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
    when(perfTestAnnotation.durationMs()).thenReturn(100);
    when(perfTestAnnotation.warmUpMs()).thenReturn(5);
    initialiseContext();
    context.runValidation();
    assertThat(context.getThroughputQps(), is(10526L));
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
    when(statisticsMock.getLatencyPercentile(90, MILLISECONDS)).thenReturn(100F);
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
    assertThat(context.isErrorThresholdAchieved(), is(true));
    assertThat(context.isThroughputAchieved(), is(true));
    assertThat(context.getPercentileResults(), is(anEmptyMap()));
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
    context.runValidation();
    long expected = (long)(statisticsMock.getEvaluationCount() / (float)(perfTestAnnotation.durationMs() - perfTestAnnotation
      .warmUpMs())) * 1000;
    assertThat(context.getThroughputQps(), is(expected));
  }

  @Test
  public void whenRunningEvaluation_thenMinLatencyRequirementsShouldBeChecked() {
    initialiseContext();
    context.runValidation();
    assertThat(context.isMinLatencyAchieved(), is(true));
    assertThat(context.isSuccessful(), is(true));
  }

  @Test
  public void whenRunningEvaluation_andMinLatencyCheckFails_thenIsSuccessfulShouldBeFalse() {
    when(statisticsMock.getMinLatency(MILLISECONDS)).thenReturn(60.9F);
    initialiseContext();
    context.runValidation();
    assertThat(context.isMinLatencyAchieved(), is(false));
    assertThat(context.isSuccessful(), is(false));
  }

  @Test
  public void whenRunningEvaluation_thenMaxLatencyRequirementsShouldBeChecked() {
    initialiseContext();
    context.runValidation();
    assertThat(context.isMaxLatencyAchieved(), is(true));
    assertThat(context.isSuccessful(), is(true));
  }

  @Test
  public void whenRunningEvaluation_andMaxLatencyCheckFails_thenIsSuccessfulShouldBeFalse() {
    when(statisticsMock.getMaxLatency(MILLISECONDS)).thenReturn(190.9F);
    initialiseContext();
    context.runValidation();
    assertThat(context.isMaxLatencyAchieved(), is(false));
    assertThat(context.isSuccessful(), is(false));
  }

  @Test
  public void whenRunningEvaluation_thenMeanLatencyRequirementsShouldBeChecked() {
    initialiseContext();
    context.runValidation();
    assertThat(context.isMeanLatencyAchieved(), is(true));
    assertThat(context.isSuccessful(), is(true));
  }

  @Test
  public void whenRunningEvaluation_andMeanLatencyCheckFails_thenIsSuccessfulShouldBeFalse() {
    when(statisticsMock.getMeanLatency(MILLISECONDS)).thenReturn(10.9F);
    initialiseContext();
    context.runValidation();
    assertThat(context.isMeanLatencyAchieved(), is(false));
    assertThat(context.isSuccessful(), is(false));
  }

  @Test
  public void whenSupplyingANanosecondStartTime_thenTheStartTimeShouldBeSet() {
    long now = nanoTime();
    context = new EvaluationContext(TEST_NAME, now);
    assertThat(context.getStartTimeNs(), is(now));
  }

  @Test
  public void whenCreatingDefaultContext_thenIsAsyncShouldBeFalse() {
    assertThat(context.isAsyncEvaluation(), is(false));
  }

  @Test
  public void whenSpecifyingAsyncFlag_thenIsAsyncShouldBeTrue() {
    context = new EvaluationContext(TEST_NAME, nanoTime(), true);
    assertThat(context.isAsyncEvaluation(), is(true));
  }

  private void initialiseContext() {
    context.loadConfiguration(perfTestAnnotation);
    context.loadRequirements(perfTestRequirement);
    context.setStatistics(statisticsMock);
  }

  private void initialisePerfTestAnnotation() {
    when(perfTestAnnotation.durationMs()).thenReturn(10);
    when(perfTestAnnotation.maxExecutionsPerSecond()).thenReturn(1_000);
    when(perfTestAnnotation.threads()).thenReturn(50);
    when(perfTestAnnotation.warmUpMs()).thenReturn(5);
    when(perfTestAnnotation.rampUpPeriodMs()).thenReturn(4);
  }

  private void initialisePerfTestRequirementAnnotation() {
    when(perfTestRequirement.executionsPerSec()).thenReturn(10_000);
    when(perfTestRequirement.allowedErrorPercentage()).thenReturn(0.5f);
    when(perfTestRequirement.percentiles()).thenReturn("90:0.5,95:9");
    when(perfTestRequirement.meanLatency()).thenReturn(4.8F);
    when(perfTestRequirement.minLatency()).thenReturn(1.6F);
    when(perfTestRequirement.maxLatency()).thenReturn(100.6F);
  }

  private void initialiseStatisticsMockToPassValidation() {
    when(statisticsMock.getEvaluationCount()).thenReturn(15_000L);
    when(statisticsMock.getErrorCount()).thenReturn(0L);
    when(statisticsMock.getMaxLatency(MILLISECONDS)).thenReturn(99.0F);
    when(statisticsMock.getMinLatency(MILLISECONDS)).thenReturn(1.1F);
    when(statisticsMock.getMeanLatency(MILLISECONDS)).thenReturn(3.9F);
    when(statisticsMock.getErrorPercentage()).thenReturn(0.0F);
    when(statisticsMock.getLatencyPercentile(90, MILLISECONDS)).thenReturn(0.2F);
    when(statisticsMock.getLatencyPercentile(95, MILLISECONDS)).thenReturn(4F);
  }

  private void loadPercentilesAndAssertParsedCorrectly(String percentiles, Map<Integer, Float> expected) {
    loadPercentiles(percentiles);
    assertThat(context.getRequiredPercentiles(), is(expected));
  }

  private void loadPercentiles(String percentiles) {
    when(perfTestRequirement.percentiles()).thenReturn(percentiles);
    context.loadRequirements(perfTestRequirement);
  }

  private void expectValidationError(String expectedMessage) {
    try {
      context.loadConfiguration(perfTestAnnotation);
      fail("Expected validation Exception");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage(), startsWith(expectedMessage));
    }
  }

  private void expectRequirementsValidationError(String expectedMessage) {
    try {
      context.loadRequirements(perfTestRequirement);
      fail("Expected requirements validation Exception");
    } catch (Exception e) {
      assertThat(e.getMessage(), startsWith(expectedMessage));
    }
  }

}
