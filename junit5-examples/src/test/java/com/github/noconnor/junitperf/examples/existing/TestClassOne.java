package com.github.noconnor.junitperf.examples.existing;

import org.junit.jupiter.api.Test;

public class TestClassOne {
    @Test
    public void sample_test1_class1() throws InterruptedException {
        Thread.sleep(5);
    }

    @Test
    public void sample_test2_class1() throws InterruptedException {
        // Mock some processing logic
        Thread.sleep(1);
    }

}
