package com.noconnor.junitperf.annotations;

public @interface JUnitPerfTest {

  // Total number of threads to use during the test evaluations
  int threads() default 50;

  // Total test duration (milliseconds) after which no more evaluations will take place
  int duration() default 125_000;

  // During the warm up period (milliseconds) test execution results will be ignored and will not be considered in test result evaluations
  int warmUp() default 80_000;

  // Test will execute no more that specified "rateLimit" executions per second
  // Default value is no limit
  int rateLimit() default -1;

}
