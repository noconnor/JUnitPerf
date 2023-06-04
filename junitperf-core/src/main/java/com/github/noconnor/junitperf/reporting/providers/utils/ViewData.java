package com.github.noconnor.junitperf.reporting.providers.utils;

import com.github.noconnor.junitperf.data.EvaluationContext;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
public class ViewData {

    private static final String SUCCESS_COLOUR = "#2b67a4";
    private static final String FAILED_COLOUR = "#d9534f";

    @Getter
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
        this.totalInvocations = number_format(context.getEvaluationCount(), 0, ",");
        this.successfulInvocations = number_format(context.getEvaluationCount() - context.getErrorCount(), 0, ",");
        this.errorThresholdColour = context.isErrorThresholdAchieved() ? SUCCESS_COLOUR : FAILED_COLOUR;
        this.errorCount = number_format(context.getErrorCount(), 0, ",");
        this.errorPercentage = number_format(context.getErrorPercentage(), 2, ",");
        this.configuredThreads = String.valueOf(context.getConfiguredThreads());
        this.configuredWarmUp = number_format(context.getConfiguredWarmUp(), 0, ",");
        this.configuredRampUpPeriodMs = number_format(context.getConfiguredRampUpPeriodMs(), 0, ",");
        this.testDurationFormatted = context.getTestDurationFormatted();
        this.throughputAchievedColour = context.isThroughputAchieved() ? SUCCESS_COLOUR : FAILED_COLOUR;
        this.throughputQps = number_format(context.getThroughputQps(), 0, ",");
        this.requiredThroughput = number_format(context.getRequiredThroughput(), 0, ",");
        this.minLatencyAchievedColour = context.isMinLatencyAchieved() ? SUCCESS_COLOUR : FAILED_COLOUR;
        this.requiredMinLatency = (context.getRequiredMinLatency() < 0) ? "N/A" : number_format(context.getRequiredMinLatency(), 2, "");
        this.minLatency = number_format(context.getMinLatencyMs(), 2, " ");
        this.meanLatencyAchievedColour = context.isMeanLatencyAchieved() ? SUCCESS_COLOUR : FAILED_COLOUR;
        this.meanLatency = number_format(context.getMeanLatencyMs(), 2, " ");
        this.requiredMeanLatency = (context.getRequiredMeanLatency() < 0) ? "N/A" : number_format(context.getRequiredMeanLatency(), 2, "");
        this.maxLatencyAchievedColour = context.isMaxLatencyAchieved() ? SUCCESS_COLOUR : FAILED_COLOUR;
        this.maxLatency = number_format(context.getMaxLatencyMs(), 2, ",");
        this.requiredMaxLatency = (context.getRequiredMaxLatency() < 0) ? "N/A" : number_format(context.getRequiredMaxLatency(), 2, "");
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
                    data.percentileLatency = number_format(context.getLatencyPercentileMs(percentile), 2, ",");
                    data.percentileTarget = number_format(target, 2, ",");
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

    private String buildChartData(EvaluationContext context) {
        return IntStream.range(1, 100).mapToObj(i -> "[ " +
                i + ", " +
                context.getLatencyPercentileMs(i) + ", " +
                "\"" + i + "% of executions ≤ " + number_format(context.getLatencyPercentileMs(i), 2, ",") + "ms\""
                + "],"
        ).collect(Collectors.joining("\n"));
    }

    private String number_format(float value, int decimalPlaces, String thousandSeparator) {
        return String.format("%" + thousandSeparator + "." + decimalPlaces + "f", value).trim();
    }

}
