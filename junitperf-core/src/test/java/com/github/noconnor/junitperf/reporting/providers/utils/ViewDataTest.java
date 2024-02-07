package com.github.noconnor.junitperf.reporting.providers.utils;

import com.github.noconnor.junitperf.BaseTest;
import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static com.github.noconnor.junitperf.reporting.providers.utils.ViewData.FAILED_COLOUR;
import static com.github.noconnor.junitperf.reporting.providers.utils.ViewData.SUCCESS_COLOUR;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ViewDataTest extends BaseTest {

    private Locale defaultLocale;

    private HtmlReportGenerator reportGenerator;

    @Before
    public void setup() throws IOException {
        defaultLocale = Locale.getDefault();
        // set local to en-US as this test expects numbers to use "."
        // as a decimal separator and "," as a grouping separator, e.g. 1,337.42
        Locale.setDefault(Locale.US);
    }

    @After
    public void after() {
        // restore default locale
        Locale.setDefault(defaultLocale);
    }

    @Test
    public void whenSuccessfulEvaluationContextIsProvided_thenViewDataShouldBeMappedCorrectly() {
        float dummyLatency = ThreadLocalRandom.current().nextFloat();
        EvaluationContext context = buildMockContext(dummyLatency, true);
        ViewData viewData = new ViewData(context);

        assertEquals("Unittest", viewData.getTestName());
        assertEquals(SUCCESS_COLOUR, viewData.getTestNameColour());
        assertEquals(buildExpectedChartData(dummyLatency), viewData.getChartData());
        assertEquals(buildExpectedCsvData(dummyLatency), viewData.getCsvData());
        assertEquals("today", viewData.getStartTime());
        assertEquals("12,034", viewData.getTotalInvocations());
        assertEquals("11,689", viewData.getSuccessfulInvocations());
        assertEquals(SUCCESS_COLOUR, viewData.getErrorThresholdColour());
        assertEquals("345", viewData.getErrorCount());
        assertEquals("0.02", viewData.getErrorPercentage());
        assertEquals("10", viewData.getConfiguredThreads());
        assertEquals("234", viewData.getConfiguredWarmUp());
        assertEquals("455", viewData.getConfiguredRampUpPeriodMs());
        assertEquals("3234", viewData.getTestDurationFormatted());
        assertEquals(SUCCESS_COLOUR, viewData.getThroughputAchievedColour());
        assertEquals("34,556", viewData.getThroughputQps());
        assertEquals("1,000", viewData.getRequiredThroughput());
        assertEquals(SUCCESS_COLOUR, viewData.getMinLatencyAchievedColour());
        assertEquals("1.00", viewData.getMinLatency());
        assertEquals("4.45", viewData.getRequiredMinLatency());
        assertEquals(SUCCESS_COLOUR, viewData.getMeanLatencyAchievedColour());
        assertEquals("3.00", viewData.getMeanLatency());
        assertEquals("34.00", viewData.getRequiredMeanLatency());
        assertEquals(SUCCESS_COLOUR, viewData.getMaxLatencyAchievedColour());
        assertEquals("5.90", viewData.getMaxLatency());
        assertEquals("6.00", viewData.getRequiredMaxLatency());
        assertEquals(buildExpectedRequiredPercentiles(context), viewData.getRequiredPercentiles());
    }

    @Test
    public void whenFailureEvaluationContextIsProvided_thenViewDataShouldBeMappedCorrectly() {
        float dummyLatency = ThreadLocalRandom.current().nextFloat();
        EvaluationContext context = buildMockContext(dummyLatency, false);
        ViewData viewData = new ViewData(context);

        assertEquals("Unittest", viewData.getTestName());
        assertEquals(FAILED_COLOUR, viewData.getTestNameColour());
        assertEquals(buildExpectedChartData(dummyLatency), viewData.getChartData());
        assertEquals(buildExpectedCsvData(dummyLatency), viewData.getCsvData());
        assertEquals("today", viewData.getStartTime());
        assertEquals("12,034", viewData.getTotalInvocations());
        assertEquals("11,689", viewData.getSuccessfulInvocations());
        assertEquals(FAILED_COLOUR, viewData.getErrorThresholdColour());
        assertEquals("345", viewData.getErrorCount());
        assertEquals("0.02", viewData.getErrorPercentage());
        assertEquals("10", viewData.getConfiguredThreads());
        assertEquals("234", viewData.getConfiguredWarmUp());
        assertEquals("455", viewData.getConfiguredRampUpPeriodMs());
        assertEquals("3234", viewData.getTestDurationFormatted());
        assertEquals(FAILED_COLOUR, viewData.getThroughputAchievedColour());
        assertEquals("34,556", viewData.getThroughputQps());
        assertEquals("1,000", viewData.getRequiredThroughput());
        assertEquals(FAILED_COLOUR, viewData.getMinLatencyAchievedColour());
        assertEquals("1.00", viewData.getMinLatency());
        assertEquals("4.45", viewData.getRequiredMinLatency());
        assertEquals(FAILED_COLOUR, viewData.getMeanLatencyAchievedColour());
        assertEquals("3.00", viewData.getMeanLatency());
        assertEquals("34.00", viewData.getRequiredMeanLatency());
        assertEquals(FAILED_COLOUR, viewData.getMaxLatencyAchievedColour());
        assertEquals("5.90", viewData.getMaxLatency());
        assertEquals("6.00", viewData.getRequiredMaxLatency());
        assertEquals(buildExpectedRequiredPercentiles(context), viewData.getRequiredPercentiles());
    }

    @Test
    public void whenRequiredThresholdsAreNotSet_thenViewDataShouldBeMappedCorrectly() {
        float dummyLatency = ThreadLocalRandom.current().nextFloat();
        EvaluationContext context = buildMockContext(dummyLatency, false);
        when(context.getRequiredMinLatency()).thenReturn(-1F);
        when(context.getRequiredMaxLatency()).thenReturn(-1F);
        when(context.getRequiredMeanLatency()).thenReturn(-1F);
        
        
        ViewData viewData = new ViewData(context);

        assertEquals("Unittest", viewData.getTestName());
        assertEquals(FAILED_COLOUR, viewData.getTestNameColour());
        assertEquals(buildExpectedChartData(dummyLatency), viewData.getChartData());
        assertEquals(buildExpectedCsvData(dummyLatency), viewData.getCsvData());
        assertEquals("today", viewData.getStartTime());
        assertEquals("12,034", viewData.getTotalInvocations());
        assertEquals("11,689", viewData.getSuccessfulInvocations());
        assertEquals(FAILED_COLOUR, viewData.getErrorThresholdColour());
        assertEquals("345", viewData.getErrorCount());
        assertEquals("0.02", viewData.getErrorPercentage());
        assertEquals("10", viewData.getConfiguredThreads());
        assertEquals("234", viewData.getConfiguredWarmUp());
        assertEquals("455", viewData.getConfiguredRampUpPeriodMs());
        assertEquals("3234", viewData.getTestDurationFormatted());
        assertEquals(FAILED_COLOUR, viewData.getThroughputAchievedColour());
        assertEquals("34,556", viewData.getThroughputQps());
        assertEquals("1,000", viewData.getRequiredThroughput());
        assertEquals(FAILED_COLOUR, viewData.getMinLatencyAchievedColour());
        assertEquals("1.00", viewData.getMinLatency());
        assertEquals("N/A", viewData.getRequiredMinLatency());
        assertEquals(FAILED_COLOUR, viewData.getMeanLatencyAchievedColour());
        assertEquals("3.00", viewData.getMeanLatency());
        assertEquals("N/A", viewData.getRequiredMeanLatency());
        assertEquals(FAILED_COLOUR, viewData.getMaxLatencyAchievedColour());
        assertEquals("5.90", viewData.getMaxLatency());
        assertEquals("N/A", viewData.getRequiredMaxLatency());
        assertEquals(buildExpectedRequiredPercentiles(context), viewData.getRequiredPercentiles());
    }

    @Test
    public void whenRequiredPercentilesIsNotSet_thenViewDataShouldBeMappedCorrectly() {
        float dummyLatency = ThreadLocalRandom.current().nextFloat();
        EvaluationContext context = buildMockContext(dummyLatency, true);
        when(context.getRequiredPercentiles()).thenReturn(Collections.emptyMap());
        ViewData viewData = new ViewData(context);
        assertEquals(Collections.emptyList(), viewData.getRequiredPercentiles());
    }

    @Test
    public void whenGroupNameIsSet_thenTestNameShouldBeScopedByGroupName() {
        EvaluationContext context = buildMockContext(1234F, true);
        when(context.getGroupName()).thenReturn("ClassName");
        ViewData viewData = new ViewData(context);
        assertEquals("ClassName : Unittest", viewData.getTestName());
    }

    private EvaluationContext buildMockContext(float dummyLatency, boolean isSuccessful) {
        EvaluationContext context = mock(EvaluationContext.class);
        when(context.getTestName()).thenReturn("Unittest");
        when(context.isSuccessful()).thenReturn(isSuccessful);
        when(context.getStartTime()).thenReturn("today");
        when(context.getEvaluationCount()).thenReturn(12034L);
        when(context.getErrorCount()).thenReturn(345L);
        when(context.getLatencyPercentileMs(anyInt())).thenReturn(dummyLatency);
        when(context.isErrorThresholdAchieved()).thenReturn(isSuccessful);
        when(context.getErrorPercentage()).thenReturn(0.02F);
        when(context.getConfiguredThreads()).thenReturn(10);
        when(context.getConfiguredWarmUp()).thenReturn(234);
        when(context.getConfiguredRampUpPeriodMs()).thenReturn(455);
        when(context.getTestDurationFormatted()).thenReturn("3234");
        when(context.isThroughputAchieved()).thenReturn(isSuccessful);
        when(context.getThroughputQps()).thenReturn(34556L);
        when(context.getRequiredThroughput()).thenReturn(1000);
        when(context.isMinLatencyAchieved()).thenReturn(isSuccessful);
        when(context.getRequiredMinLatency()).thenReturn(4.45F);
        when(context.getMinLatencyMs()).thenReturn(1F);
        when(context.isMeanLatencyAchieved()).thenReturn(isSuccessful);
        when(context.getMeanLatencyMs()).thenReturn(3.0F);
        when(context.getRequiredMeanLatency()).thenReturn(34.0F);
        when(context.isMaxLatencyAchieved()).thenReturn(isSuccessful);
        when(context.getMaxLatencyMs()).thenReturn(5.9F);
        when(context.getRequiredMaxLatency()).thenReturn(6F);
        when(context.getRequiredPercentiles()).thenReturn(buildRequiredPercentiles());
        when(context.getPercentileResults()).thenReturn(buildPercentileResults());
        return context;
    }

    private List<ViewData.RequiredPercentilesData> buildExpectedRequiredPercentiles(EvaluationContext context) {
        Map<Integer, Boolean> percentileResults = context.getPercentileResults();
        Map<Integer, Float> requiredPercentiles = context.getRequiredPercentiles();
        
        List<ViewData.RequiredPercentilesData> result = new ArrayList<>();
        requiredPercentiles.forEach((key, value) -> {
            Boolean percentileResult = percentileResults.get(key);
            ViewData.RequiredPercentilesData data = new ViewData.RequiredPercentilesData();
            data.setPercentile(String.valueOf(key));
            data.setPercentileTarget(String.format("%.2f", value));
            data.setPercentileLatency(String.format("%.2f", context.getLatencyPercentileMs(key)));
            data.setPercentileResultColour(percentileResult ? SUCCESS_COLOUR : FAILED_COLOUR);
            result.add(data);
        });
        return result;
    }

    private String buildExpectedCsvData(float latency) {
        StringBuilder expected = new StringBuilder();
        IntStream.range(1,101).forEach( i-> {
            expected.append("[ ")
                    .append(i)
                    .append(", ")
                    .append(latency)
                    .append(" ],")
                    .append("\n");
        });
        return expected.toString().trim();
    }

    private String buildExpectedChartData(float latency) {
        StringBuilder expected = new StringBuilder();
        String latencyFormatted = String.format("%.2f", latency); 
        IntStream.range(1,100).forEach( i-> {
            expected.append("[ ")
                    .append(i)
                    .append(", ")
                    .append(latency)
                    .append(", \"")
                    .append(i)
                    .append("% of executions ≤ ")
                    .append(latencyFormatted)
                    .append("ms\"],")
                    .append("\n");
        });
        return expected.toString().trim();
    }

    private Map<Integer, Boolean> buildPercentileResults() {
        Map<Integer, Boolean> results = new HashMap<>();
        IntStream.range(1, 101).forEach(i -> {
            results.put(i, ThreadLocalRandom.current().nextBoolean());
        });
        return results;
    }

    private Map<Integer, Float> buildRequiredPercentiles() {
        Map<Integer, Float> requiredPercentiles = new HashMap<>();
        requiredPercentiles.put(98, 7.45F);
        requiredPercentiles.put(95, 5.1F);
        requiredPercentiles.put(90, 3.6F);
        return requiredPercentiles;
    }

}