package com.github.noconnor.junitperf.reporting.providers;

import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.reporting.ReportGenerator;
import com.github.noconnor.junitperf.reporting.providers.utils.ViewData;
import com.github.noconnor.junitperf.reporting.providers.utils.ViewProcessor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import static java.lang.System.getProperty;
import static java.util.Objects.isNull;

@Slf4j
public class HtmlReportGenerator implements ReportGenerator {

    private static final String DEFAULT_REPORT_PATH = getProperty("user.dir") + "/build/reports/junitperf_report.html";

    private final String reportPath;
    private final Set<EvaluationContext> history;

    public HtmlReportGenerator() {
        this(DEFAULT_REPORT_PATH);
    }

    @SuppressWarnings("WeakerAccess")
    public HtmlReportGenerator(String reportPath) {
        this.reportPath = reportPath;
        this.history = new LinkedHashSet<>();
    }

    @Override
    public void generateReport(LinkedHashSet<EvaluationContext> testContexts) {
        history.addAll(testContexts);
        renderTemplate();
    }

    private void renderTemplate() {
        Path outputPath = Paths.get(reportPath);

        try {
            Files.createDirectories(outputPath.getParent());
            log.info("Rendering report to: " + outputPath);

            Map<String, StringBuilder> blocks = HtmlTemplateProcessor.parseTemplateBlocks();

            String root = blocks.get("root").toString();

            StringBuilder overviews = new StringBuilder();
            StringBuilder details = new StringBuilder();

            for (EvaluationContext context : history) {
                ViewData c = new ViewData(context);

                String overview = ViewProcessor.populateTemplate(c, "context", blocks.get("{% OVERVIEW_BLOCK %}").toString());
                String detail = ViewProcessor.populateTemplate(c, "context", blocks.get("{% DETAILED_BLOCK %}").toString());
                String percentileData = ViewProcessor.populateTemplate(
                        c.getRequiredPercentiles(),
                        "context.percentiles",
                        blocks.get("{% PERCENTILES_BLOCK %}").toString()
                );

                detail = detail.replaceAll("\\{% PERCENTILES_BLOCK %\\}", percentileData);

                overviews.append(overview).append("\n");
                details.append(detail).append("\n");
            }

            root = root.replaceAll("\\{% OVERVIEW_BLOCK %\\}", overviews.toString());
            root = root.replaceAll("\\{% DETAILED_BLOCK %\\}", details.toString());

            Files.write(outputPath, root.getBytes());

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }

    @Override
    public String getReportPath() {
        return reportPath;
    }

    @UtilityClass
    public static class HtmlTemplateProcessor {

        private static final String REPORT_TEMPLATE = "/templates/report.template";

        public static Map<String, StringBuilder> parseTemplateBlocks() {
            InputStream templateString = HtmlTemplateProcessor.class.getResourceAsStream(REPORT_TEMPLATE);
            if (isNull(templateString)) {
                throw new IllegalStateException("Report template is missing: " + REPORT_TEMPLATE);
            }

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
}
