package com.github.noconnor.junitperf.reporting.providers.utils;

import com.github.noconnor.junitperf.data.EvaluationContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
public class ViewData {

    static final String SUCCESS_COLOUR = "#2b67a4";
    static final String FAILED_COLOUR = "#d9534f";

    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    public static final class RequiredPercentilesData {
        private String percentile;
        private String percentileResultColour;
        private String percentileLatency;
        private String percentileTarget;
    }

    private final String testName;
    private final String testNameColour;
    private final String chartData;
    private final String csvData;
    private final String startTime;
    private final String totalInvocations;
    private final String successfulInvocations;
    private final String errorThresholdColour;
    private final String errorCount;
    private final String errorPercentage;
    private final String configuredThreads;
    private final String configuredWarmUp;
    private final String configuredRampUpPeriodMs;
    private final String testDurationFormatted;
    private final String throughputAchievedColour;
    private final String throughputQps;
    private final String requiredThroughput;
    private final String minLatencyAchievedColour;
    private final String requiredMinLatency;
    private final String minLatency;
    private final String meanLatencyAchievedColour;
    private final String meanLatency;
    private final String requiredMeanLatency;
    private final String maxLatencyAchievedColour;
    private final String maxLatency;
    private final String requiredMaxLatency;
    private final List<RequiredPercentilesData> requiredPercentiles;

    public ViewData(EvaluationContext context) {
        this.testName = context.getTestName();
        this.testNameColour = context.isSuccessful() ? SUCCESS_COLOUR : FAILED_COLOUR;
        this.chartData = buildChartData(context);
        this.csvData = buildCsvData(context);
        this.startTime = context.getStartTime();
        this.totalInvocations = formatNumber(context.getEvaluationCount(), 0, ",");
        this.successfulInvocations = formatNumber(context.getEvaluationCount() - context.getErrorCount(), 0, ",");
        this.errorThresholdColour = context.isErrorThresholdAchieved() ? SUCCESS_COLOUR : FAILED_COLOUR;
        this.errorCount = formatNumber(context.getErrorCount(), 0, ",");
        this.errorPercentage = formatNumber(context.getErrorPercentage(), 2, ",");
        this.configuredThreads = String.valueOf(context.getConfiguredThreads());
        this.configuredWarmUp = formatNumber(context.getConfiguredWarmUp(), 0, ",");
        this.configuredRampUpPeriodMs = formatNumber(context.getConfiguredRampUpPeriodMs(), 0, ",");
        this.testDurationFormatted = context.getTestDurationFormatted();
        this.throughputAchievedColour = context.isThroughputAchieved() ? SUCCESS_COLOUR : FAILED_COLOUR;
        this.throughputQps = formatNumber(context.getThroughputQps(), 0, ",");
        this.requiredThroughput = formatNumber(context.getRequiredThroughput(), 0, ",");
        this.minLatencyAchievedColour = context.isMinLatencyAchieved() ? SUCCESS_COLOUR : FAILED_COLOUR;
        this.requiredMinLatency = (context.getRequiredMinLatency() < 0) ? "N/A" : formatNumber(context.getRequiredMinLatency(), 2, "");
        this.minLatency = formatNumber(context.getMinLatencyMs(), 2, " ");
        this.meanLatencyAchievedColour = context.isMeanLatencyAchieved() ? SUCCESS_COLOUR : FAILED_COLOUR;
        this.meanLatency = formatNumber(context.getMeanLatencyMs(), 2, " ");
        this.requiredMeanLatency = (context.getRequiredMeanLatency() < 0) ? "N/A" : formatNumber(context.getRequiredMeanLatency(), 2, "");
        this.maxLatencyAchievedColour = context.isMaxLatencyAchieved() ? SUCCESS_COLOUR : FAILED_COLOUR;
        this.maxLatency = formatNumber(context.getMaxLatencyMs(), 2, ",");
        this.requiredMaxLatency = (context.getRequiredMaxLatency() < 0) ? "N/A" : formatNumber(context.getRequiredMaxLatency(), 2, "");
        this.requiredPercentiles = buildRequiredPercentileData(context);
    }

    private List<RequiredPercentilesData> buildRequiredPercentileData(EvaluationContext context) {
        return context.getRequiredPercentiles().entrySet()
                .stream()
                .map(entry -> {
                    Integer percentile = entry.getKey();
                    Float target = entry.getValue();
                    RequiredPercentilesData data = new RequiredPercentilesData();
                    data.percentile = percentile.toString();
                    data.percentileResultColour = context.getPercentileResults().get(percentile) ? SUCCESS_COLOUR : FAILED_COLOUR;
                    data.percentileLatency = formatNumber(context.getLatencyPercentileMs(percentile), 2, ",");
                    data.percentileTarget = formatNumber(target, 2, ",");
                    return data;
                }).collect(Collectors.toList());
    }

    private static String buildCsvData(EvaluationContext context) {
        return IntStream.range(1, 101).mapToObj(i -> "[ " +
                i + ", " +
                context.getLatencyPercentileMs(i)
                + " ],"
        ).collect(Collectors.joining("\n"));
    }

    private static String buildChartData(EvaluationContext context) {
        return IntStream.range(1, 100).mapToObj(i -> "[ " +
                i + ", " +
                context.getLatencyPercentileMs(i) + ", " +
                "\"" + i + "% of executions ≤ " + formatNumber(context.getLatencyPercentileMs(i), 2, ",") + "ms\""
                + "],"
        ).collect(Collectors.joining("\n"));
    }

    private static String formatNumber(float value, int decimalPlaces, String thousandSeparator) {
        return String.format("%" + thousandSeparator + "." + decimalPlaces + "f", value).trim();
    }

}
