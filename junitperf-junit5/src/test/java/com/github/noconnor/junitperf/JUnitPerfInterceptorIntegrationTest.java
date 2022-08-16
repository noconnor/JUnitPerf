package com.github.noconnor.junitperf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(JUnitPerfInterceptor.class)
public class JUnitPerfInterceptorIntegrationTest {


    @Test
    @JUnitPerfTest(threads = 1, rampUpPeriodMs = 100, durationMs = 1_000)
    void someOtherTest() {
        assertTrue(true);
    }

    @Test
    @JUnitPerfTest(threads = 10, rampUpPeriodMs = 100, durationMs = 1_000)
    void integrationTest() {
        assertTrue(true);
    }
}
