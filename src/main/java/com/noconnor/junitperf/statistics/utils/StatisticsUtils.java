package com.noconnor.junitperf.statistics.utils;

import lombok.experimental.UtilityClass;

import java.util.Map;
import com.noconnor.junitperf.statistics.Statistics;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;

@UtilityClass
public class StatisticsUtils {

  public static float calculateThroughputPerSecond(Statistics statistics, long durationMs) {
    float evaluationCount = statistics.getEvaluationCount();
    checkArgument(durationMs > 0, format("Duration must be > 0 [duration:%d]", durationMs));
    checkArgument(evaluationCount >= 0, format("Evaluation count must be > 0 [count:%.0f]", evaluationCount));
    return evaluationCount / (durationMs / 1_000F);
  }

  public static float calculatePercentageError(Statistics statistics) {
    float evaluationCount = statistics.getEvaluationCount();
    float errorCount = statistics.getErrorCount();
    checkArgument(evaluationCount >= errorCount,
      format("Evaluation count must be > error [count:%.0f, error:%.0f]", evaluationCount, errorCount));
    return evaluationCount <= 0 ? 0 : errorCount / evaluationCount;
  }

  public static Map<Integer, Float> parsePercentileLimits(String percentileLimits) {
    return null;
  }

}
