package com.github.noconnor.junitperf.reporting.providers.utils;

import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.reporting.providers.utils.SimpleFormatter.ContextHtmlFormat.RequiredPercentilesData;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SimpleFormatter {

    private static final String SUCCESS_COLOUR = "#2b67a4";
    private static final String FAILED_COLOUR = "#d9534f";


    public static class ReportGenerator {


        public static String populateTemplate(ContextHtmlFormat htmlContext, String template) throws IllegalAccessException {
            return populateTemplate(htmlContext, "context", template);
        }

        public static String populateTemplate(Object obj, String prefix, String template) throws IllegalAccessException {
            String temp = template;
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field f : fields) {
                f.setAccessible(true);
                String target = "\\{\\{ " + prefix + "." + f.getName() + " \\}\\}";
                Object value = f.get(obj);
                temp = temp.replaceAll(target, value.toString());
            }
            return temp;
        }

        public static String populatePercentilesOverview(ContextHtmlFormat htmlContext, String template) throws IllegalAccessException {
            StringBuilder result = new StringBuilder();
            for (RequiredPercentilesData data : htmlContext.requiredPercentiles) {
                String temp = populateTemplate(data, "context.percentiles", template);
                result.append(temp).append("\n");
            }
            ;
            return result.toString();
        }

    }

    public static class ContextHtmlFormat {

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

        public ContextHtmlFormat(EvaluationContext context) {
            this.testName = context.getTestName();
            this.testNameColour = context.isSuccessful() ? SUCCESS_COLOUR : FAILED_COLOUR;
            this.chartData = IntStream.range(1, 100).mapToObj(i -> "[ " +
                    i + ", " +
                    context.getLatencyPercentileMs(i) + ", " +
                    "\"" + i + "% of executions ≤ " + number_format(context.getLatencyPercentileMs(i), 2, ",") + "ms\""
                    + "],"
            ).collect(Collectors.joining("\n"));
            this.csvData = IntStream.range(1, 101).mapToObj(i -> "[ " +
                    i + ", " +
                    context.getLatencyPercentileMs(i)
                    + " ],"
            ).collect(Collectors.joining("\n"));
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
            this.requiredPercentiles = context.getRequiredPercentiles().entrySet()
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

        private String number_format(float value, int decimalPlaces, String thousandSeparator) {
            return String.format("%" + thousandSeparator + "." + decimalPlaces + "f", value).trim();
        }

    }

    public static Map<String, StringBuilder> parseTemplateBlocks() {
        InputStream templateString = SimpleFormatter.class.getResourceAsStream("/templates/report.twig");

        Map<String, StringBuilder> contextBlocks = new HashMap<>();

        Deque<StringBuilder> stack = new ArrayDeque<>();

        StringBuilder root = new StringBuilder();
        stack.push(root);
        contextBlocks.put("root", root);

        Set<String> expectedBlocks = new HashSet<>();
        expectedBlocks.add("{% OVERVIEW_BLOCK %}");
        expectedBlocks.add("{% DETAILED_BLOCK %}");
        expectedBlocks.add("{% PERCENTILES_BLOCK %}");

        try (Scanner scanner = new Scanner(templateString)) {
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                String trimmed = line.trim();

                if (expectedBlocks.contains(trimmed)) {

                    // Keep the marker
                    stack.getFirst().append(line).append("\n");

                    StringBuilder newBlock = new StringBuilder();

                    contextBlocks.put(trimmed, newBlock);
                    stack.push(newBlock);

                } else if (trimmed.equals("{% END %}")) {
                    stack.pop();

                } else {
                    stack.getFirst().append(line).append("\n");
                }
            }
        }
        return contextBlocks;
    }

}
