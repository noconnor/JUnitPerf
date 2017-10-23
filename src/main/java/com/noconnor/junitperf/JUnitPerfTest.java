package com.noconnor.junitperf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface JUnitPerfTest {

  // Total number of threads to use during the test evaluations
  int threads() default 1;

  // Total test duration (milliseconds) after which no more evaluations will take place
  int duration() default 60_000;

  // During the warm up period (milliseconds) test execution results will be ignored and will not be considered in test result evaluations
  int warmUp() default 10_000;

  // Test will execute no more that specified "rateLimit" executions per second
  // Default value is no limit
  int rateLimit() default -1;

}
