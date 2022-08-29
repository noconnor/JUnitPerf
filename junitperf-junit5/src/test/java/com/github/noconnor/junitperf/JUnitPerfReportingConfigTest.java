package com.github.noconnor.junitperf;

import com.github.noconnor.junitperf.reporting.ReportGenerator;
import com.github.noconnor.junitperf.reporting.providers.ConsoleReportGenerator;
import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import com.github.noconnor.junitperf.statistics.providers.DescriptiveStatisticsCalculator;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@MockitoSettings(strictness = Strictness.LENIENT)
class JUnitPerfReportingConfigTest {

    @Test
    void whenNoReportersAreAddedToConfig_thenConfigShouldContainEmptyReportersCollection() {
        JUnitPerfReportingConfig config = JUnitPerfReportingConfig.builder()
                .build();
        assertEquals(0, config.getReportGenerators().size());
    }

    @Test
    void whenReportersAreAddedToConfig_thenReportersShouldBeRetrievable() {
        ReportGenerator reporter1 = new ConsoleReportGenerator();
        ReportGenerator reporter2 = new HtmlReportGenerator();

        JUnitPerfReportingConfig config = JUnitPerfReportingConfig.builder()
                .reportGenerator(reporter1)
                .reportGenerator(reporter2)
                .build();

        assertEquals(2, config.getReportGenerators().size());
        assertTrue(config.getReportGenerators().contains(reporter1));
        assertTrue(config.getReportGenerators().contains(reporter2));
    }

    @SuppressWarnings("unchecked")
    @Test
    void whenStatisticsCalculatorSupplierIsSpecified_thenStatisticsCalculatorSupplierShouldBeCallable() {
        Supplier<StatisticsCalculator> calcMock = mock(Supplier.class);

        JUnitPerfReportingConfig config = JUnitPerfReportingConfig.builder()
                .statisticsCalculatorSupplier(calcMock)
                .build();

        assertNotNull(config.getStatisticsCalculatorSupplier());

        config.getStatisticsCalculatorSupplier().get();
        verify(calcMock).get();
    }

    @Test
    void whenNoStatisticsCalculatorSupplierIsSpecified_thenDefaultStatisticsCalculatorSupplierShouldBeCallable() {
        JUnitPerfReportingConfig config = JUnitPerfReportingConfig.builder()
                .build();

        assertNotNull(config.getStatisticsCalculatorSupplier());
        assertTrue(config.getStatisticsCalculatorSupplier().get() instanceof DescriptiveStatisticsCalculator);
    }

}