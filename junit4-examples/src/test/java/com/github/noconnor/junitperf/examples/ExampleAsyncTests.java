package com.github.noconnor.junitperf.examples;

import com.github.noconnor.junitperf.JUnitPerfAsyncRule;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.data.TestContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import static com.github.noconnor.junitperf.examples.utils.ReportingUtils.newHtmlReporter;

public class ExampleAsyncTests {

  @Rule
  public JUnitPerfAsyncRule rule = new JUnitPerfAsyncRule(newHtmlReporter("async_test.html"));

  private static ExecutorService pool;

  @BeforeClass
  public static void setup() {
    pool = Executors.newFixedThreadPool(100);
  }

  @AfterClass
  public static void teardown() {
    pool.shutdownNow();
  }

  @Test
  @JUnitPerfTest(durationMs = 10_000, warmUpMs = 1_000, maxExecutionsPerSecond = 100)
  public void whenTestExecutesAsynchronously_thenMeasurementsCanStillBeCaptured() {
    TestContext context = rule.newContext();
    pool.submit(() -> {
      someProcessingDelay();
      if (isSuccessful()) {
        context.success();
      } else {
        context.fail();
      }
    });
  }

  private boolean isSuccessful() {
    return ThreadLocalRandom.current().nextInt(0, 100) > 50;
  }

  private void someProcessingDelay() {
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      // IGNORE
    }
  }

}
