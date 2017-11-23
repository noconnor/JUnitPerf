package com.github.noconnor.junitperf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface JUnitPerfTestRequirement {

  // Expected target percentile distribution in the format "percentile1:expected_value_ms,percentile2:expected_value_ms,..."
  String percentiles() default "";

  // Expected test throughput (executions per second)
  int executionsPerSec() default 0;

  // Expected % of test failures. Failures are measured as test case exceptions, default 0% errors allowed
  float allowedErrorPercentage() default 0;

  float minLatency() default -1;

  float maxLatency() default -1;

  float meanLatency() default -1;

}
