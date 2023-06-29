package com.github.noconnor.junitperf.examples;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.github.noconnor.junitperf.JUnitPerfRule;
import com.github.noconnor.junitperf.JUnitPerfTest;

import static com.github.noconnor.junitperf.examples.utils.ReportingUtils.newHtmlReporter;
import static org.junit.Assume.assumeFalse;

public class ExampleSuccessTests {

  @Rule
  public JUnitPerfRule perfRule = new JUnitPerfRule( true, newHtmlReporter("success.html"));

  @Before
  public void setup() throws InterruptedException {
    Thread.sleep(1_000);
  }

  @After
  public void teardown() throws InterruptedException {
    Thread.sleep(1_000);
  }

  @Test
  @JUnitPerfTest(threads = 10, durationMs = 10_000, warmUpMs = 1_000, rampUpPeriodMs = 2_000, maxExecutionsPerSecond = 100)
  public void whenNoRequirementsArePresent_thenTestShouldAlwaysPass() throws IOException {
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress("www.google.com", 80), 1000);
    }
  }

  @Test
  @JUnitPerfTest(threads = 10, durationMs = 10_000, warmUpMs = 1_000, rampUpPeriodMs = 2_000, totalExecutions = 100)
  public void whenAssumptionFails_thenTestShouldBeSkipped() throws IOException {
    //noinspection DataFlowIssue
    assumeFalse(true); // dummy test to illustrate skipped tests
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress("www.google.com", 80), 1000);
    }
  }

}
