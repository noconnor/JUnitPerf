package com.github.noconnor.junitperf;

import com.github.noconnor.junitperf.data.NoOpTestContext;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.*;

@MockitoSettings(strictness = Strictness.LENIENT)
class TestContextSupplierTest {

    private TestContextSupplier supplier;
    @Mock
    private StatisticsCalculator statsCalcMock;

    @Test
    void whenMeasurementsHaveNotStarted_thenNoOpContextShouldBeReturned() {
        supplier = new TestContextSupplier(currentTimeMillis() + 10_000, statsCalcMock);
        assertEquals(NoOpTestContext.INSTANCE, supplier.startMeasurement());
    }

    @Test
    void whenMeasurementsHaveStarted_thenNewTestContextShouldBeReturned() {
        supplier = new TestContextSupplier(currentTimeMillis() - 10_000, statsCalcMock);
        assertNotNull(supplier.startMeasurement());
        assertNotEquals(NoOpTestContext.INSTANCE, supplier.startMeasurement());
    }

}