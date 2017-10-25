package com.noconnor.junitperf.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Map;
import com.noconnor.junitperf.JUnitPerfTest;
import com.noconnor.junitperf.JUnitPerfTestRequirement;
import com.noconnor.junitperf.statistics.Statistics;
import com.noconnor.junitperf.statistics.StatisticsValidator;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.noconnor.junitperf.statistics.utils.StatisticsUtils.parsePercentileLimits;
import static java.util.Objects.nonNull;

@RequiredArgsConstructor
public class EvaluationContext {

  @Getter
  private int configuredThreads;
  @Getter
  private int configuredDuration;
  @Getter
  private int configuredWarmUp;
  @Getter
  private int configuredRateLimit;

  @Getter
  private Map<Integer, Float> requiredPercentiles;
  @Getter
  private int requiredThroughput;
  @Getter
  private float requiredAllowedErrorsRate;
  @Getter
  private boolean validationRequired;

  @Getter
  @Setter
  private Statistics statistics;

  @Getter
  private boolean isThroughputAchieved;
  @Getter
  private boolean isErrorThresholdAchieved;
  @Getter
  private Map<Integer, Boolean> percentileResults;

  private final StatisticsValidator validator;

  public void loadConfiguration(JUnitPerfTest testSettings) {
    checkNotNull(testSettings, "Test settings must not be null");
    configuredThreads = testSettings.threads();
    configuredDuration = testSettings.duration();
    configuredWarmUp = testSettings.warmUp();
    configuredRateLimit = testSettings.rateLimit();
  }

  public void loadRequirements(JUnitPerfTestRequirement requirements) {
    validationRequired = nonNull(requirements);
    if (validationRequired) {
      validationRequired = true;
      requiredThroughput = requirements.throughput();
      requiredAllowedErrorsRate = requirements.allowedErrorsRate();
      requiredPercentiles = parsePercentileLimits(requirements.percentiles());
    }
  }

  public void runValidation() {
    checkState(nonNull(statistics));
    if (validationRequired) {
      isThroughputAchieved = validator.isThroughputTargetAchieved(statistics, configuredDuration, requiredThroughput);
      isErrorThresholdAchieved = validator.isErrorThresholdTargetAchieved(statistics, requiredAllowedErrorsRate);
      percentileResults = validator.evaluateLatencyPercentiles(statistics, requiredPercentiles);
    }
  }

}
