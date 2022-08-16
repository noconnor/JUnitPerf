package com.github.noconnor.junitperf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JUnitPerfInterceptorIntegrationTest {


    @Test
    void someOtherTest() {
        System.out.println("Running someOtherTest");
        assertTrue(true);
    }

    @Test
    @ExtendWith(JUnitPerfInterceptor.class)
    void integrationTest() {
        System.out.println("Running integrationTest");
        JUnitPerfInterceptor.COUNT.incrementAndGet();
        assertTrue(true);
    }
}
