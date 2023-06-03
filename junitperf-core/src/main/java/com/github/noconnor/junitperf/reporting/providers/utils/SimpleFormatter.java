package com.github.noconnor.junitperf.reporting.providers.utils;

import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.reporting.providers.utils.SimpleFormatter.ContextHtmlFormat.RequiredPercentilesData;
import com.github.noconnor.junitperf.statistics.providers.DescriptiveStatisticsCalculator;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
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
            for(Field f : fields){
                f.setAccessible(true);
                String target = "\\{\\{ " + prefix + "." + f.getName() + " \\}\\}";
                Object value = f.get(obj);
                temp = temp.replaceAll(target, value.toString());
            }
            return temp;
        }

        public static String populatePercentilesOverview(ContextHtmlFormat htmlContext, String template) throws IllegalAccessException {
            StringBuilder result = new StringBuilder();
            for ( RequiredPercentilesData data : htmlContext.requiredPercentiles) {
                String temp = populateTemplate(data, "context.percentiles", template);
                result.append(temp).append("\n");
            };
            return result.toString();
        }

    }

    public static class ContextHtmlFormat {

        public static final class RequiredPercentilesData {
            private String percentile;
            private String percentileColour;
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
                    "\"" + i + "% of executions ≤ +" + number_format(context.getLatencyPercentileMs(i), 2, ",") + "ms\""
                    + "]," + "\n"
            ).collect(Collectors.joining("\n\t\t\t\t\t\t\t\t"));
            this.csvData = IntStream.range(1, 101).mapToObj(i -> "[ " +
                    i + ", " +
                    context.getLatencyPercentileMs(i)
                    + " ]," + "\n"
            ).collect(Collectors.joining("\n\t\t\t\t\t\t\t\t"));
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
            this.requiredThroughput = number_format(context.getRequiredThroughput(), 0,  ",");
            this.minLatencyAchievedColour = context.isMinLatencyAchieved() ? SUCCESS_COLOUR : FAILED_COLOUR;
            this.requiredMinLatency = (context.getRequiredMinLatency() < 0) ? "N/A" :number_format(context.getRequiredMinLatency(), 2,  "");
            this.minLatency = number_format(context.getMinLatencyMs(), 2, " ");
            this.meanLatencyAchievedColour = context.isMeanLatencyAchieved() ? SUCCESS_COLOUR : FAILED_COLOUR;
            this.meanLatency = number_format(context.getMeanLatencyMs(), 2, " ");
            this.requiredMeanLatency = (context.getRequiredMeanLatency() < 0) ? "N/A" :number_format(context.getRequiredMeanLatency(), 2, "");
            this.maxLatencyAchievedColour = context.isMaxLatencyAchieved() ? SUCCESS_COLOUR : FAILED_COLOUR;
            this.maxLatency = number_format(context.getMaxLatencyMs(), 2, ",");
            this.requiredMaxLatency = (context.getRequiredMaxLatency() < 0) ? "N/A" :number_format(context.getRequiredMaxLatency(), 2,  "");
            this.requiredPercentiles = context.getRequiredPercentiles().entrySet()
                    .stream()
                    .map( entry -> {
                        Integer percentile = entry.getKey();
                        Float target = entry.getValue();
                        RequiredPercentilesData data = new RequiredPercentilesData();
                        data.percentile = percentile.toString();
                        data.percentileColour = context.getPercentileResults().get(percentile) ? SUCCESS_COLOUR : FAILED_COLOUR;
                        data.percentileLatency = number_format(context.getLatencyPercentileMs(percentile), 2, ",");
                        data.percentileTarget = number_format(target, 2, ",");
                        return data;
                    }).collect(Collectors.toList());
        }

        private String number_format(float value, int decimalPlaces, String thousandSeparator) {
            // DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.getDefault());
            // return df.format(value);
            return String.format("%" + thousandSeparator + "." + decimalPlaces + "f", value);
        }

    }

    public static Map<String, StringBuilder> parseTemplateBlocks() {
        InputStream templateString = SimpleFormatter.class.getResourceAsStream("/templates/report.twig");
        Scanner scanner = new Scanner(templateString);

        Map<String, StringBuilder> contextBlocks = new HashMap<>();

        Deque<StringBuilder> stack = new ArrayDeque<>();

        StringBuilder root = new StringBuilder();
        stack.push(root);
        contextBlocks.put("root", root);

        Set<String> expectedBlocks = new HashSet<>();
        expectedBlocks.add("{% OVERVIEW_BLOCK %}");
        expectedBlocks.add("{% DETAILED_BLOCK %}");
        expectedBlocks.add("{% PERCENTILES_BLOCK %}");

        while(scanner.hasNext()) {
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
        return contextBlocks;
    }


    public static void main(String[] args) throws IllegalAccessException, IOException {

        Map<String, StringBuilder> blocks = parseTemplateBlocks();

        List<EvaluationContext> contexts = new ArrayList<>();

        for (int i = 0; i< 3; i++ ){
            EvaluationContext context = new EvaluationContext("Dummy test " + i, System.nanoTime());
            context.loadRequirements(newRequirements());
            context.setStatistics(newStatistics());
            context.runValidation();
            contexts.add(context);
        }

        List<ContextHtmlFormat> htmlContexts = contexts.stream().map(ContextHtmlFormat::new).collect(Collectors.toList());

        String root = blocks.get("root").toString();

        StringBuilder overviews = new StringBuilder();
        StringBuilder details = new StringBuilder();

        for (ContextHtmlFormat c : htmlContexts) {

            String overview = ReportGenerator.populateTemplate(c, blocks.get("{% OVERVIEW_BLOCK %}").toString());
            String detail  = ReportGenerator.populateTemplate(c, blocks.get("{% DETAILED_BLOCK %}").toString());
            String percentileData = ReportGenerator.populatePercentilesOverview(c, blocks.get("{% PERCENTILES_BLOCK %}").toString());

            detail = detail.replaceAll("\\{% PERCENTILES_BLOCK %\\}", percentileData);

            overviews.append(overview).append("\n");
            details.append(detail).append("\n");
        };

        root = root.replaceAll("\\{% OVERVIEW_BLOCK %\\}", overviews.toString());
        root = root.replaceAll("\\{% DETAILED_BLOCK %\\}", details.toString());

        System.out.println(root);

        Files.write(Paths.get("test.html"), root.getBytes());
    }

    private static DescriptiveStatisticsCalculator newStatistics() {
        DescriptiveStatisticsCalculator statistics = new DescriptiveStatisticsCalculator();
        IntStream.range(0, 1_000).forEach( i -> {
            statistics.addLatencyMeasurement(ThreadLocalRandom.current().nextInt(1_000_000, 3_000_000));
        });
        return statistics;
    }

    private static JUnitPerfTestRequirement newRequirements() {
        return new JUnitPerfTestRequirement() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return JUnitPerfTestRequirement.class;
            }

            @Override
            public String percentiles() {
                return "98:3.3,99:32.6,100:46.9999";
            }

            @Override
            public int executionsPerSec() {
                return 10000;
            }

            @Override
            public float allowedErrorPercentage() {
                return 0.7F;
            }

            @Override
            public float minLatency() {
                return 0.98F;
            }

            @Override
            public float maxLatency() {
                return 89.0F;
            }

            @Override
            public float meanLatency() {
                return 0.67F;
            }
        };
    }
}
