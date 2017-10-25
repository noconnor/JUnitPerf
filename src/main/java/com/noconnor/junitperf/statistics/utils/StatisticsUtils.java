package com.noconnor.junitperf.statistics.utils;

import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.noconnor.junitperf.statistics.Statistics;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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

  // TODO: Move this, doesn't belong here
  public static Map<Integer, Float> parsePercentileLimits(String percentileLimits) {
    Map<Integer, Float> limits = newHashMap();
    if (isNotBlank(percentileLimits)) {
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
