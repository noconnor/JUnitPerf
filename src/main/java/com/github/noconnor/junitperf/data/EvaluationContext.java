package com.github.noconnor.junitperf.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Maps.newTreeMap;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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
  private StatisticsCalculator statistics;

  @Getter
  private boolean isThroughputAchieved;
  @Getter
  private boolean isErrorThresholdAchieved;
  @Getter
  private Map<Integer, Boolean> percentileResults;
  @Getter
  private boolean isSuccessful;

  @SuppressWarnings("WeakerAccess")
  public long getThroughputQps() {
    return (long)(((float)statistics.getEvaluationCount() / ((float)configuredDuration - configuredWarmUp)) * 1000);
  }

  @Getter
  private final String testName;
  @Getter
  private final String startTime;

  public void loadConfiguration(JUnitPerfTest testSettings) {
    // TODO: validate annotation attributes
    checkNotNull(testSettings, "Test settings must not be null");
    configuredThreads = testSettings.threads();
    configuredDuration = testSettings.durationMs();
    configuredWarmUp = testSettings.warmUpMs();
    configuredRateLimit = testSettings.maxExecutionsPerSecond();
  }

  public void loadRequirements(JUnitPerfTestRequirement requirements) {
    validationRequired = nonNull(requirements);
    if (validationRequired) {
      // TODO: validate annotation attributes
      validationRequired = true;
      requiredThroughput = requirements.executionsPerSec();
      requiredAllowedErrorsRate = requirements.allowedErrorPercentage();
      requiredPercentiles = parsePercentileLimits(requirements.percentiles());
    }
  }

  public void runValidation() {
    checkState(nonNull(statistics), "Statistics must be calculated before running validation");
    if (validationRequired) {
      isThroughputAchieved = getThroughputQps() >= requiredThroughput;
      isErrorThresholdAchieved = statistics.getErrorPercentage() <= (requiredAllowedErrorsRate * 100);
      percentileResults = evaluateLatencyPercentiles();
      isSuccessful = isThroughputAchieved && isErrorThresholdAchieved && noLatencyPercentileFailures();
    } else {
      isSuccessful = true;
    }
  }

  private boolean noLatencyPercentileFailures() {
    return percentileResults.values().stream().allMatch(e -> e);
  }

  private Map<Integer, Boolean> evaluateLatencyPercentiles() {
    Map<Integer, Boolean> results = newTreeMap();
    requiredPercentiles.forEach((percentile, thresholdMs) -> {
      long thresholdNs = (long)(thresholdMs * MILLISECONDS.toNanos(1));
      boolean result = statistics.getLatencyPercentile(percentile, NANOSECONDS) <= thresholdNs;
      results.put(percentile, result);
    });
    return results;
  }

  private static Map<Integer, Float> parsePercentileLimits(String percentileLimits) {
    Map<Integer, Float> limits = newTreeMap();
    if (isNotBlank(percentileLimits)) {
      // go from 90:2,95:5 -> map of integer to float
      Stream.of(percentileLimits.split(","))
        .map(entry -> entry.split(":"))
        .filter(entry -> entry.length == 2)
        .map(entry -> ImmutablePair.of(Ints.tryParse(entry[0]), Floats.tryParse(entry[1])))
        .filter(entry -> nonNull(entry.getLeft()) && nonNull(entry.getRight()))
        .forEach(entry -> limits.put(entry.getLeft(), entry.getRight()));
    }
    return limits;
  }

}
