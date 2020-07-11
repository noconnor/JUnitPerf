package com.github.noconnor.junitperf.examples;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.github.noconnor.junitperf.JUnitPerfRule;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;

import static com.github.noconnor.junitperf.examples.utils.ReportingUtils.newHtmlReporter;
import static org.junit.Assert.assertTrue;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  ExampleCommonReporter.TestClassOne.class,
  ExampleCommonReporter.TestClassTwo.class
})
public class ExampleCommonReporter {

  // Both test classes should report to the same HTML file
  private static final HtmlReportGenerator REPORTER = newHtmlReporter("common_reporter.html");

  public static class TestClassOne {
    @Rule
    public JUnitPerfRule perfRule = new JUnitPerfRule(REPORTER);

    @Test
    @JUnitPerfTest(threads = 10, durationMs = 10_000, warmUpMs = 1_000, rampUpPeriodMs = 2_000, maxExecutionsPerSecond = 100)
    public void whenNoRequirementsArePresent_thenTestShouldAlwaysPass() throws IOException {
      try (Socket socket = new Socket()) {
        socket.connect(new InetSocketAddress("www.google.com", 80), 1000);
        assertTrue(socket.isConnected());
      }
    }
  }

  public static class TestClassTwo {
    @Rule
    public JUnitPerfRule perfRule = new JUnitPerfRule(REPORTER);

    @Test(expected = AssertionError.class)
    @JUnitPerfTest(threads = 1, durationMs = 1_000, maxExecutionsPerSecond = 1_000)
    @JUnitPerfTestRequirement(executionsPerSec = 10_000)
    public void whenThroughputRequirementIsNotMet_thenTestShouldFail() throws InterruptedException {
      // Mock some processing logic
      Thread.sleep(1);
    }
  }


}
