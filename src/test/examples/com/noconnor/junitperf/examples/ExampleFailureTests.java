package com.noconnor.junitperf.examples;

import org.junit.Rule;
import org.junit.Test;
import com.noconnor.junitperf.JUnitPerfRule;
import com.noconnor.junitperf.JUnitPerfTest;
import com.noconnor.junitperf.JUnitPerfTestRequirement;

public class ExampleFailureTests {

  @Rule
  public JUnitPerfRule perfRule = new JUnitPerfRule();

  @Test
  @JUnitPerfTest(threads = 10, duration = 1_000, rateLimit = 1_000)
  @JUnitPerfTestRequirement(throughput = 10_000, percentiles = "99:110")
  public void whenThroughputRequirementIsNotMet_thenTestShouldFailOnThroughputRequirementNotMet() throws InterruptedException {
    // Mock some processing logic
    Thread.sleep(1);
  }

}
