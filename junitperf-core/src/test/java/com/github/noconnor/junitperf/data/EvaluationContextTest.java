package com.github.noconnor.junitperf.data;

import static com.github.noconnor.junitperf.data.EvaluationContext.JUNITPERF_DURATION_MS;
import static com.github.noconnor.junitperf.data.EvaluationContext.JUNITPERF_MAX_EXECUTIONS_PER_SECOND;
import static com.github.noconnor.junitperf.data.EvaluationContext.JUNITPERF_RAMP_UP_PERIOD_MS;
import static com.github.noconnor.junitperf.data.EvaluationContext.JUNITPERF_THREADS;
import static com.github.noconnor.junitperf.data.EvaluationContext.JUNITPERF_WARM_UP_MS;
import static java.lang.System.nanoTime;
import static java.util.Collections.emptyMap;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import com.github.noconnor.junitperf.BaseTest;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.github.noconnor.junitperf.datetime.DatetimeUtils;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

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

  @After
  public void tearDown(){
    System.clearProperty(JUNITPERF_THREADS);
    System.clearProperty(JUNITPERF_DURATION_MS);
    System.clearProperty(JUNITPERF_WARM_UP_MS);
    System.clearProperty(JUNITPERF_MAX_EXECUTIONS_PER_SECOND);
    System.clearProperty(JUNITPERF_RAMP_UP_PERIOD_MS);
  }

  @Test
  public void whenLoadingJUnitPerfTestSettings_thenAppropriateContextSettingsShouldBeUpdated() {
    context.loadConfiguration(perfTestAnnotation);
    assertEquals(perfTestAnnotation.durationMs(), context.getConfiguredDuration());
    assertEquals(perfTestAnnotation.maxExecutionsPerSecond(), context.getConfiguredRateLimit());
    assertEquals(perfTestAnnotation.threads(), context.getConfiguredThreads());
    assertEquals(perfTestAnnotation.warmUpMs(), context.getConfiguredWarmUp());
    assertEquals(perfTestAnnotation.rampUpPeriodMs(), context.getConfiguredRampUpPeriodMs());
  }

  @Test
  public void whenLoadingJUnitPerfTestSettings_andEnvironmentHasOverrides_thenAppropriateContextSettingsShouldBeUpdated() {
    System.setProperty(JUNITPERF_THREADS, "45");
    System.setProperty(JUNITPERF_DURATION_MS, "60000");
    System.setProperty(JUNITPERF_WARM_UP_MS, "2000");
    System.setProperty(JUNITPERF_MAX_EXECUTIONS_PER_SECOND, "55");
    System.setProperty(JUNITPERF_RAMP_UP_PERIOD_MS, "1000");
    context.loadConfiguration(perfTestAnnotation);
    assertEquals(60000, context.getConfiguredDuration());
    assertEquals(55, context.getConfiguredRateLimit());
    assertEquals(45, context.getConfiguredThreads());
    assertEquals(2000, context.getConfiguredWarmUp());
    assertEquals(1000, context.getConfiguredRampUpPeriodMs());
  }

  @Test
  public void whenLoadingJUnitPerfTestSettings_thenDurationMsShouldBeSensible() {
    when(perfTestAnnotation.durationMs()).thenReturn(0);
    expectValidationError("DurationMs must be greater than 0ms");
    when(perfTestAnnotation.durationMs()).thenReturn(-50);
    expectValidationError("DurationMs must be greater than 0ms");
  }

  @Test
  public void whenLoadingJUnitPerfTestSettings_andEnvOverridesAreSet_thenDurationMsShouldBeSensible() {
    System.setProperty(JUNITPERF_DURATION_MS, "0");
    expectValidationError("DurationMs must be greater than 0ms");
    System.setProperty(JUNITPERF_DURATION_MS, "-98");
    expectValidationError("DurationMs must be greater than 0ms");
  }

  @Test
  public void whenLoadingJUnitPerfTestSettings_thenRampUpMsShouldBeGreaterThanZero() {
    when(perfTestAnnotation.rampUpPeriodMs()).thenReturn(-9);
    expectValidationError("RampUpPeriodMs must be >= 0ms");
  }

  @Test
  public void whenLoadingJUnitPerfTestSettings_andEnvOverridesAreSet_thenRampUpMsShouldBeGreaterThanZero() {
    System.setProperty(JUNITPERF_RAMP_UP_PERIOD_MS, "-8");
    expectValidationError("RampUpPeriodMs must be >= 0ms");
  }

  @Test
  public void whenLoadingJUnitPerfTestSettings_thenRampUpMsShouldBeLessThanTestDuration() {
    when(perfTestAnnotation.rampUpPeriodMs()).thenReturn(60);
    when(perfTestAnnotation.durationMs()).thenReturn(50);
    expectValidationError("RampUpPeriodMs must be < DurationMs");
  }

  @Test
  public void whenLoadingJUnitPerfTestSettings_andEnvOverridesAreSet_thenRampUpMsShouldBeLessThanTestDuration() {
    System.setProperty(JUNITPERF_RAMP_UP_PERIOD_MS, "67");
    System.setProperty(JUNITPERF_DURATION_MS, "55");
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
  public void whenLoadingJUnitPerfTestSettings_andEnvOverridesAreSet_thenWarmUpMsShouldBeSensible() {
    System.setProperty(JUNITPERF_WARM_UP_MS, "-88");
    expectValidationError("WarmUpMs must be >= 0ms");
    System.setProperty(JUNITPERF_DURATION_MS, "38");
    System.setProperty(JUNITPERF_WARM_UP_MS, "44");
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
  public void whenLoadingJUnitPerfTestSettings_andEnvOverridesAreSet_thenThreadsShouldBeSensible() {
    System.setProperty(JUNITPERF_THREADS, "-88");
    expectValidationError("Threads must be > 0");
    System.setProperty(JUNITPERF_THREADS, "0");
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

  @Test
  public void whenLoadingJUnitPerfTestSettings_andEnvOverridesAreSet_thenMaxExecutionsPerSecondShouldBeSensible() {
    System.setProperty(JUNITPERF_MAX_EXECUTIONS_PER_SECOND, "-8");
    expectValidationError("MaxExecutionsPerSecond must be > 0 or -1 (to disable)");
    System.setProperty(JUNITPERF_MAX_EXECUTIONS_PER_SECOND, "0");
    expectValidationError("MaxExecutionsPerSecond must be > 0 or -1 (to disable)");
    // ALLOWED value
    System.setProperty(JUNITPERF_MAX_EXECUTIONS_PER_SECOND, "-1");
    context.loadConfiguration(perfTestAnnotation);
  }

  @Test(expected = NullPointerException.class)
  public void whenLoadingJUnitPerfTestSettings_andSettingsAreNull_thenExceptionShouldBeThrown() {
    context.loadConfiguration(null);
  }

  @Test
  public void whenLoadingJUnitPerfTestRequirements_thenAppropriateContextSettingsShouldBeUpdated() {
    context.loadRequirements(perfTestRequirement);
    assertEquals(perfTestRequirement.allowedErrorPercentage(), context.getRequiredAllowedErrorsRate(),0);
    assertEquals(perfTestRequirement.executionsPerSec(), context.getRequiredThroughput());
    assertEquals(ImmutableMap.of(90, 0.5F, 95, 9F), context.getRequiredPercentiles());
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
    assertTrue(context.isThroughputAchieved());
    assertTrue(context.isSuccessful());
  }

  @Test
  public void whenRunningEvaluation_andThroughputIsBelowThreshold_thenThroughputAchievedFlagShouldBeFalse() {
    when(statisticsMock.getEvaluationCount()).thenReturn(10L);
    initialiseContext();
    context.runValidation();
    assertFalse(context.isThroughputAchieved());
    assertFalse(context.isSuccessful());
  }

  @Test
  public void whenRunningEvaluation_andEvaluationCountIsGreaterThanDuration_thenThroughputShouldBeCalculatedCorrectly() {
    when(statisticsMock.getEvaluationCount()).thenReturn(1000L);
    when(perfTestAnnotation.durationMs()).thenReturn(100);
    when(perfTestAnnotation.warmUpMs()).thenReturn(5);
    initialiseContext();
    context.runValidation();
    assertEquals(10526L, context.getThroughputQps());
  }

  @Test
  public void whenRunningEvaluation_thenAllowedErrorsRequirementsShouldBeChecked() {
    initialiseContext();
    context.runValidation();
    assertTrue(context.isErrorThresholdAchieved());
    assertTrue(context.isSuccessful());
  }

  @Test
  public void whenRunningEvaluation_andAllowedErrorRateFails_thenIsSuccessfulShouldBeFalse() {
    when(statisticsMock.getErrorPercentage()).thenReturn(60F);
    initialiseContext();
    context.runValidation();
    assertFalse(context.isErrorThresholdAchieved());
    assertFalse(context.isSuccessful());
  }

  @Test
  public void whenRunningEvaluation_thenPercentileRequirementsShouldBeChecked() {
    Map<Integer, Boolean> validationResults = ImmutableMap.of(90, true, 95, true);
    initialiseContext();
    context.runValidation();
    assertEquals(validationResults, context.getPercentileResults());
  }

  @Test
  public void whenRunningEvaluation_andAPercentileThresholdIsNotMet_thenIsSuccessfulShouldReturnFalse() {
    when(statisticsMock.getLatencyPercentile(90, MILLISECONDS)).thenReturn(100F);
    Map<Integer, Boolean> validationResults = ImmutableMap.of(90, false, 95, true);
    initialiseContext();
    context.runValidation();
    assertEquals(validationResults, context.getPercentileResults());
    assertFalse(context.isSuccessful());
  }

  @Test
  public void whenRunningEvaluation_andAllThresholdsAreMet_thenIsSuccessfulShouldBeTrue() {
    initialiseContext();
    context.runValidation();
    assertTrue(context.isSuccessful());
  }

  @Test
  public void whenRunningEvaluation_andNoValidationIsRequired_thenNoValidationsShouldBeReturned() {
    context.loadConfiguration(perfTestAnnotation);
    context.setStatistics(statisticsMock);
    context.runValidation();
    assertTrue(context.isSuccessful());
    assertTrue(context.isErrorThresholdAchieved());
    assertTrue(context.isThroughputAchieved());
    assertEquals(emptyMap(), context.getPercentileResults());
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
    assertEquals(expected, context.getThroughputQps());
  }

  @Test
  public void whenRunningEvaluation_thenMinLatencyRequirementsShouldBeChecked() {
    initialiseContext();
    context.runValidation();
    assertTrue(context.isMinLatencyAchieved());
    assertTrue(context.isSuccessful());
  }

  @Test
  public void whenRunningEvaluation_andMinLatencyCheckFails_thenIsSuccessfulShouldBeFalse() {
    when(statisticsMock.getMinLatency(MILLISECONDS)).thenReturn(60.9F);
    initialiseContext();
    context.runValidation();
    assertFalse(context.isMinLatencyAchieved());
    assertFalse(context.isSuccessful());
  }

  @Test
  public void whenRunningEvaluation_thenMaxLatencyRequirementsShouldBeChecked() {
    initialiseContext();
    context.runValidation();
    assertTrue(context.isMaxLatencyAchieved());
    assertTrue(context.isSuccessful());
  }

  @Test
  public void whenRunningEvaluation_andMaxLatencyCheckFails_thenIsSuccessfulShouldBeFalse() {
    when(statisticsMock.getMaxLatency(MILLISECONDS)).thenReturn(190.9F);
    initialiseContext();
    context.runValidation();
    assertFalse(context.isMaxLatencyAchieved());
    assertFalse(context.isSuccessful());
  }

  @Test
  public void whenRunningEvaluation_thenMeanLatencyRequirementsShouldBeChecked() {
    initialiseContext();
    context.runValidation();
    assertTrue(context.isMeanLatencyAchieved());
    assertTrue(context.isSuccessful());
  }

  @Test
  public void whenRunningEvaluation_andMeanLatencyCheckFails_thenIsSuccessfulShouldBeFalse() {
    when(statisticsMock.getMeanLatency(MILLISECONDS)).thenReturn(10.9F);
    initialiseContext();
    context.runValidation();
    assertFalse(context.isMeanLatencyAchieved());
    assertFalse(context.isSuccessful());
  }

  @Test
  public void whenSupplyingANanosecondStartTime_thenTheStartTimeShouldBeSet() {
    long now = nanoTime();
    context = new EvaluationContext(TEST_NAME, now);
    assertEquals(now, context.getStartTimeNs());
  }

  @Test
  public void whenCreatingDefaultContext_thenIsAsyncShouldBeFalse() {
    assertFalse(context.isAsyncEvaluation());
  }

  @Test
  public void whenSpecifyingAsyncFlag_thenIsAsyncShouldBeTrue() {
    context = new EvaluationContext(TEST_NAME, nanoTime(), true);
    assertTrue(context.isAsyncEvaluation());
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
    assertEquals(expected, context.getRequiredPercentiles());
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
      assertTrue(e.getMessage().startsWith(expectedMessage));
    }
  }

  private void expectRequirementsValidationError(String expectedMessage) {
    try {
      context.loadRequirements(perfTestRequirement);
      fail("Expected requirements validation Exception");
    } catch (Exception e) {
      assertTrue(e.getMessage().startsWith(expectedMessage));
    }
  }

}
