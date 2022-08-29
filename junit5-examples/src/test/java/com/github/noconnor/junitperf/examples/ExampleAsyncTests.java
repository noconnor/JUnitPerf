package com.github.noconnor.junitperf.examples;

import com.github.noconnor.junitperf.*;
import com.github.noconnor.junitperf.data.TestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import static com.github.noconnor.junitperf.examples.utils.ReportingUtils.newHtmlReporter;

@ExtendWith(JUnitPerfInterceptor.class)
public class ExampleAsyncTests {

    // Should be static or new instance will be created for each @Test method
    @JUnitPerfTestActiveConfig
    private final static JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
            .reportGenerator(newHtmlReporter("async_test.html"))
            .build();
    private static ExecutorService pool;

    @BeforeAll
    public static void setup() {
        pool = Executors.newFixedThreadPool(100);
    }

    @AfterAll
    public static void teardown() {
        pool.shutdownNow();
    }

    @Test
    @JUnitPerfTest(durationMs = 10_000, warmUpMs = 1_000, maxExecutionsPerSecond = 100)
    public void whenTestExecutesAsynchronously_thenMeasurementsCanStillBeCaptured(TestContextSupplier supplier) {
        // Starts the task timer
        TestContext context = supplier.startMeasurement();
        pool.submit(() -> {
            someProcessingDelay();
            if (isSuccessful()) {
                // marks task as successful and stops the task time measurement
                context.success();
            } else {
                // marks task as failure and stops the task time measurement
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
