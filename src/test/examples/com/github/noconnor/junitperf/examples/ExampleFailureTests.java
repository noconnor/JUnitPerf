package com.github.noconnor.junitperf.examples;

import org.junit.Rule;
import org.junit.Test;
import com.github.noconnor.junitperf.JUnitPerfRule;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;

public class ExampleFailureTests {

  @Rule
  public JUnitPerfRule perfRule = new JUnitPerfRule();

  @Test
  @JUnitPerfTest(threads = 1, duration = 1_000, rateLimit = 1_000)
  @JUnitPerfTestRequirement(throughput = 10_000)
  public void whenThroughputRequirementIsNotMet_thenTestShouldFail() throws InterruptedException {
    // Mock some processing logic
    Thread.sleep(1);
  }

  @Test
  @JUnitPerfTest(threads = 1, duration = 1_000, rateLimit = 1_000)
  @JUnitPerfTestRequirement(throughput = 10_000, percentiles = "99:1")
  public void whenLatencyRequirementIsNotMet_thenTestShouldFail() throws InterruptedException {
    // Mock some processing logic
    Thread.sleep(2);
  }

}
