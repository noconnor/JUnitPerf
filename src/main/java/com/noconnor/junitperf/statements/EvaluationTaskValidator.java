package com.noconnor.junitperf.statements;

import lombok.Builder;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class EvaluationTaskValidator {

  private final int expectedThroughput;
  private final float allowedErrorsRate;
  private final Map<Integer, Float> expectedPercentiles;

  @Builder
  public EvaluationTaskValidator(int expectedThroughput, float allowedErrorsRate, String percentiles) {
    this.expectedThroughput = expectedThroughput;
    this.allowedErrorsRate = allowedErrorsRate;
    this.expectedPercentiles = newHashMap();
  }


}
