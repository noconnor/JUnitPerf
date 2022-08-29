package com.github.noconnor.junitperf;

import com.github.noconnor.junitperf.data.NoOpTestContext;
import com.github.noconnor.junitperf.data.TestContext;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import lombok.RequiredArgsConstructor;

import static java.lang.System.currentTimeMillis;

@RequiredArgsConstructor
public class TestContextSupplier {

    private final long measurementsStartTimeMs;
    private final StatisticsCalculator statsCalculator;

    public TestContext startMeasurement() {
        return hasMeasurementStarted() ? new TestContext(statsCalculator) : NoOpTestContext.INSTANCE;
    }

    private boolean hasMeasurementStarted() {
        return currentTimeMillis() >= measurementsStartTimeMs;
    }
}
