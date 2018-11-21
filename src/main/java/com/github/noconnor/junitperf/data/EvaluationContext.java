package com.github.noconnor.junitperf.data;

import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Maps.newTreeMap;
import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.IntStream.range;
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
  private Map<Integer, Float> requiredPercentiles = emptyMap();
  @Getter
  private int requiredThroughput = 0;
  @Getter
  private float requiredAllowedErrorsRate = 0;
  @Getter
  private float requiredMinLatency = -1;
  @Getter
  private float requiredMaxLatency = -1;
  @Getter
  private float requiredMeanLatency = -1;

  @Setter
  private StatisticsCalculator statistics;

  @Getter
  private boolean isThroughputAchieved;
  @Getter
  private boolean isMinLatencyAchieved;
  @Getter
  private boolean isMaxLatencyAchieved;
  @Getter
  private boolean isMeanLatencyAchieved;
  @Getter
  private boolean isErrorThresholdAchieved;
  @Getter
  private Map<Integer, Boolean> percentileResults;
  @Getter
  private boolean isSuccessful;

  @Getter
  private float[] percentiles = new float[101];
  @Getter
  private float minLatencyMs;
  @Getter
  private float maxLatencyMs;
  @Getter
  private float meanLatencyMs;
  @Getter
  private float errorPercentage;
  @Getter
  private long evaluationCount;
  @Getter
  private long errorCount;


  @SuppressWarnings("WeakerAccess")
  public long getThroughputQps() {
    return (long)((evaluationCount/ ((float)configuredDuration - configuredWarmUp)) * 1000);
  }

  public float getLatencyPercentileMs(int percentile) {
    return percentiles[percentile];
  }

  @Getter
  private final String testName;
  @Getter
  private final String startTime;

  public void loadConfiguration(JUnitPerfTest testSettings) {
    validateTestSettings(testSettings);
    configuredThreads = testSettings.threads();
    configuredDuration = testSettings.durationMs();
    configuredWarmUp = testSettings.warmUpMs();
    configuredRateLimit = testSettings.maxExecutionsPerSecond();
  }

  public void loadRequirements(JUnitPerfTestRequirement requirements) {
    if (nonNull(requirements)) {
      validateRequirements(requirements);
      requiredThroughput = requirements.executionsPerSec();
      requiredAllowedErrorsRate = requirements.allowedErrorPercentage();
      requiredPercentiles = parsePercentileLimits(requirements.percentiles());
      requiredMinLatency = requirements.minLatency();
      requiredMaxLatency = requirements.maxLatency();
      requiredMeanLatency = requirements.meanLatency();
    }
  }

  public void runValidation() {
    checkState(nonNull(statistics), "Statistics must be calculated before running validation");
    calculateAndCacheStatistics();
    isThroughputAchieved = getThroughputQps() >= requiredThroughput;
    isErrorThresholdAchieved = errorPercentage <= (requiredAllowedErrorsRate * 100);
    isMinLatencyAchieved = validateLatency(minLatencyMs, requiredMinLatency);
    isMaxLatencyAchieved = validateLatency(maxLatencyMs, requiredMaxLatency);
    isMeanLatencyAchieved = validateLatency(meanLatencyMs, requiredMeanLatency);
    percentileResults = evaluateLatencyPercentiles();

    isSuccessful = isThroughputAchieved &&
      isMaxLatencyAchieved &&
      isMinLatencyAchieved &&
      isMeanLatencyAchieved &&
      isErrorThresholdAchieved &&
      noLatencyPercentileFailures();
  }

  private boolean validateLatency(float actualMs, float requiredMs) {
    return requiredMaxLatency < 0 || actualMs <= requiredMs;
  }

  private boolean noLatencyPercentileFailures() {
    return percentileResults.values().stream().allMatch(e -> e);
  }

  private Map<Integer, Boolean> evaluateLatencyPercentiles() {
    Map<Integer, Boolean> results = newTreeMap();
    requiredPercentiles.forEach((percentile, thresholdMs) -> {
      boolean result = getLatencyPercentileMs(percentile) <= thresholdMs;
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

  private void validateTestSettings(JUnitPerfTest testSettings) {
    checkNotNull(testSettings, "Test settings must not be null");
    checkState(testSettings.durationMs() > 0, "DurationMs must be greater than 0ms");
    checkState(testSettings.warmUpMs() >= 0, "WarmUpMs must be >= 0ms");
    checkState(testSettings.warmUpMs() < testSettings.durationMs(), "WarmUpMs must be < DurationMs");
    checkState(testSettings.threads() > 0, "Threads must be > 0");
    checkState(testSettings.maxExecutionsPerSecond() > 0 || testSettings.maxExecutionsPerSecond() == -1,
      "MaxExecutionsPerSecond must be > 0 or -1 (to disable)");
  }

  private void validateRequirements(JUnitPerfTestRequirement requirements) {
    checkState(requirements.allowedErrorPercentage() >= 0, "AllowedErrorPercentage must be >= 0");
    checkState(requirements.executionsPerSec() >= 0, "ExecutionsPerSec must be >= 0");
  }

  private void calculateAndCacheStatistics() {
    // Statistics calculations (specifically percentile calculation) can be an expensive operation.
    // Therefore results should be calculated once and cached
    range(1, 101).forEach(i -> percentiles[i] = statistics.getLatencyPercentile(i, MILLISECONDS));
    minLatencyMs = statistics.getMinLatency(MILLISECONDS);
    maxLatencyMs = statistics.getMaxLatency(MILLISECONDS);
    meanLatencyMs = statistics.getMeanLatency(MILLISECONDS);
    errorPercentage = statistics.getErrorPercentage();
    errorCount = statistics.getErrorCount();
    evaluationCount = statistics.getEvaluationCount();
  }

}
