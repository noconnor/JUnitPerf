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
import com.noconnor.junitperf.statistics.StatisticsValidator;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class EvaluationContextTest extends BaseTest {

  private EvaluationContext context;

  @Mock
  private StatisticsValidator validatorMock;

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
    context = new EvaluationContext(validatorMock);
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
    when(validatorMock.isThroughputTargetAchieved(eq(statisticsMock), anyInt(), anyInt())).thenReturn(true);
    initialiseContext();
    context.runValidation();
    assertThat(context.isThroughputAchieved(), is(true));
  }

  @Test
  public void whenRunningEvaluation_thenAllowedErrorsRequirementsShouldBeChecked() {
    when(validatorMock.isErrorThresholdTargetAchieved(eq(statisticsMock), anyFloat())).thenReturn(true);
    initialiseContext();
    context.runValidation();
    assertThat(context.isErrorThresholdAchieved(), is(true));
  }

  @Test
  public void whenRunningEvaluation_thenPercentileRequirementsShouldBeChecked() {
    Map<Integer, Boolean> validationResults = ImmutableMap.of(90, true);
    when(validatorMock.evaluateLatencyPercentiles(eq(statisticsMock), anyMap())).thenReturn(validationResults);
    initialiseContext();
    context.runValidation();
    assertThat(context.getPercentileResults(), is(validationResults));
  }

  @Test
  public void whenRunningEvaluation_andNoValidationIsRequired_thenNoValidationsShouldBeReturned() {
    initialiseContext();
    context.loadConfiguration(perfTestAnnotation);
    context.setStatistics(statisticsMock);
    verifyZeroInteractions(validatorMock);
  }

  private void initialiseContext() {
    context.loadConfiguration(perfTestAnnotation);
    context.loadRequirements(perfTestRequirement);
    context.setStatistics(statisticsMock);
  }

  private void initialisePerfTestAnnotation() {
    when(perfTestAnnotation.duration()).thenReturn(1);
    when(perfTestAnnotation.rateLimit()).thenReturn(1_000);
    when(perfTestAnnotation.threads()).thenReturn(50);
    when(perfTestAnnotation.warmUp()).thenReturn(500);
  }

  private void initialisePerfTestRequirementAnnotation() {
    when(perfTestRequirement.throughput()).thenReturn(10_000);
    when(perfTestRequirement.allowedErrorsRate()).thenReturn(0.5f);
    when(perfTestRequirement.percentiles()).thenReturn("90:0.5,95:9");
  }

}
