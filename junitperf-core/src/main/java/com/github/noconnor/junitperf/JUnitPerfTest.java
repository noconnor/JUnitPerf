package com.github.noconnor.junitperf;

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
  int durationMs() default 60_000;

  // During the warm up period (milliseconds) test execution results will be ignored and will not be considered in test result evaluations
  int warmUpMs() default 0;

  // Test will execute no more that specified "rateLimit" executions per second
  // Default value is no limit
  int maxExecutionsPerSecond() default -1;

  // The duration of the period where the framework ramps up its executions per second,
  // before reaching its stable (maxExecutionsPerSecond) rate
  // If maxExecutionsPerSecond is not set, this attribute will have no effect
  int rampUpPeriodMs() default 0;

  // Test will execute totalExecutions number of iterations & complete
  // This is a best effort target, test will execute for at least this number of executions.
  // If durationMs & totalExecutions are set, totalExecutions will take precedence over test duration 
  // Default value is no limit
  int totalExecutions() default -1;
}
