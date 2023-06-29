package com.github.noconnor.junitperf.reporting.providers;

import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.reporting.ReportGenerator;
import com.github.noconnor.junitperf.reporting.providers.utils.ViewData;
import com.github.noconnor.junitperf.reporting.providers.utils.ViewProcessor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
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
import java.util.stream.Collectors;

import static java.lang.System.getProperty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
public class HtmlReportGenerator implements ReportGenerator {

    private static final String DEFAULT_REPORT_PATH = String.join(
            File.separator,
            getProperty("user.dir") ,
            "build",
            "reports",
            "junitperf_report.html"
    );
    
    private static final String REPORT_TEMPLATE = "/templates/report.template";

    private static final String OVERVIEW_MARKER = "{% OVERVIEW_BLOCK %}";
    private static final String DETAILS_MARKER = "{% DETAILED_BLOCK %}";
    private static final String PERCENTILE_TARGETS_MARKER = "{% PERCENTILES_BLOCK %}";
    

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
    public synchronized void generateReport(LinkedHashSet<EvaluationContext> testContexts) {
        history.addAll(testContexts);
        renderTemplate();
    }

    @Override
    public String getReportPath() {
        return reportPath;
    }

    private void renderTemplate() {
        try {
            Path outputPath = Paths.get(reportPath);
            
            Files.createDirectories(outputPath.getParent());
            log.info("Rendering report to: " + outputPath);

            Map<String, String> blocks = HtmlTemplateProcessor.parseTemplateBlocks();

            String root = blocks.get("root");

            StringBuilder overviews = new StringBuilder();
            StringBuilder details = new StringBuilder();

            for (EvaluationContext context : history) {
                ViewData c = new ViewData(context);

                String overview = ViewProcessor.populateTemplate(c, "context", blocks.get(OVERVIEW_MARKER));
                overviews.append(overview).append("\n");
                
                if (isNull(context.getAbortedException())) {
                    String detail = ViewProcessor.populateTemplate(c, "context", blocks.get(DETAILS_MARKER));
                    String percentileData = ViewProcessor.populateTemplate(
                            c.getRequiredPercentiles(),
                            "context.percentiles",
                            blocks.get(PERCENTILE_TARGETS_MARKER)
                    );

                    detail = detail.replaceAll(asRegex(PERCENTILE_TARGETS_MARKER), percentileData);
                    details.append(detail).append("\n");
                }
            }

            root = root.replaceAll(asRegex(OVERVIEW_MARKER), overviews.toString());
            root = root.replaceAll(asRegex(DETAILS_MARKER), details.toString());

            Files.write(outputPath, root.getBytes());

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    
    private String asRegex(String marker) {
        return marker.replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}");
    }
    
    
    @UtilityClass
    public class HtmlTemplateProcessor {
        
        public static Map<String, String> parseTemplateBlocks() {
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
            expectedBlocks.add(OVERVIEW_MARKER);
            expectedBlocks.add(DETAILS_MARKER);
            expectedBlocks.add(PERCENTILE_TARGETS_MARKER);

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
            return contextBlocks.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
        }

    }
}
