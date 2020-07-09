package com.github.noconnor.junitperf.examples;

import org.junit.Rule;
import org.junit.Test;
import com.github.noconnor.junitperf.JUnitPerfRule;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.reporting.providers.ConsoleReportGenerator;

public class ExampleConsoleReporter {

    @Rule
    public JUnitPerfRule jUnitPerfRule = new JUnitPerfRule(new ConsoleReportGenerator());

    @Test
    @JUnitPerfTest(threads = 1, warmUpMs = 1_000, durationMs = 2_000)
    public void test1() throws InterruptedException {
      Thread.sleep(10);
    }

    @Test
    @JUnitPerfTest(threads = 1, warmUpMs = 1_000, durationMs = 2_000)
    public void test2() throws InterruptedException {
      Thread.sleep(10);
    }

    @Test
    @JUnitPerfTest(threads = 1, warmUpMs = 1_000, durationMs = 2_000)
    public void test3() throws InterruptedException {
      Thread.sleep(10);
    }
}
