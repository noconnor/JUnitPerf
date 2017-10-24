package com.noconnor.junitperf.statistics;

import lombok.Builder;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class StatisticsValidator {

  private final int expectedThroughput;
  private final float allowedErrorsRate;
  private final Map<Integer, Float> expectedPercentiles;

  @Builder
  public StatisticsValidator(int expectedThroughput, float allowedErrorsRate, String percentiles) {
    this.expectedThroughput = expectedThroughput;
    this.allowedErrorsRate = allowedErrorsRate;
    this.expectedPercentiles = newHashMap();
  }

  public void validate(Statistics statistics) {

  }

}
