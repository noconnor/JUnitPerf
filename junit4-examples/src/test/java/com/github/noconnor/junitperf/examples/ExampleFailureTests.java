package com.github.noconnor.junitperf.examples;

import org.junit.Rule;
import org.junit.Test;
import com.github.noconnor.junitperf.JUnitPerfRule;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;

import static com.github.noconnor.junitperf.examples.utils.ReportingUtils.newHtmlReporter;

public class ExampleFailureTests {

  @Rule
  public JUnitPerfRule perfRule = new JUnitPerfRule(newHtmlReporter("failures.html"));

  @Test(expected = AssertionError.class)
  @JUnitPerfTest(threads = 1, durationMs = 1_000, maxExecutionsPerSecond = 1_000)
  @JUnitPerfTestRequirement(executionsPerSec = 10_000)
  public void whenThroughputRequirementIsNotMet_thenTestShouldFail() throws InterruptedException {
    // Mock some processing logic
    Thread.sleep(1);
  }

  @Test
  @JUnitPerfTest(threads = 1, durationMs = 1_000, maxExecutionsPerSecond = 1_000)
  @JUnitPerfTestRequirement(executionsPerSec = 10, percentiles = "99:1")
  public void whenLatencyRequirementIsNotMet_thenTestShouldFail() throws InterruptedException {
    // Mock some processing logic
    Thread.sleep(2);
  }

  @Test
  @JUnitPerfTest(threads = 1, durationMs = 1_000, maxExecutionsPerSecond = 1_000)
  public void whenNoRequirementsAreSpecified_andExceptionIsThrown_thenTestShouldFail() throws InterruptedException {
    // Mock some processing logic
    Thread.sleep(2);
    throw new IllegalStateException("testing failure");
  }

}
