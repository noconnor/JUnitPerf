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
  int throughput() default 1;

  // Expected % of test failures. Failures are measured as test case exceptions
  float allowedErrorsRate() default 1;

}
