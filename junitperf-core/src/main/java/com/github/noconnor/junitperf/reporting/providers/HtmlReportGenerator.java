package com.github.noconnor.junitperf.reporting.providers;

import com.github.noconnor.junitperf.reporting.providers.utils.SimpleFormatter;
import com.github.noconnor.junitperf.reporting.providers.utils.SimpleFormatter.ContextHtmlFormat;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.reporting.ReportGenerator;

import static java.lang.System.getProperty;

@Slf4j
public class HtmlReportGenerator implements ReportGenerator {

  private static final String DEFAULT_REPORT_PATH = getProperty("user.dir") + "/build/reports/junitperf_report.html";
  private static final String REPORT_TEMPLATE = "templates/report.twig";

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

      Map<String, StringBuilder> blocks = SimpleFormatter.parseTemplateBlocks();

      String root = blocks.get("root").toString();
      StringBuilder overviews = new StringBuilder();
      StringBuilder details = new StringBuilder();

      for (EvaluationContext context : history) {
        ContextHtmlFormat c = new ContextHtmlFormat(context);

        String overview = SimpleFormatter.ReportGenerator.populateTemplate(c, blocks.get("{% OVERVIEW_BLOCK %}").toString());
        String detail  = SimpleFormatter.ReportGenerator.populateTemplate(c, blocks.get("{% DETAILED_BLOCK %}").toString());
        String percentileData = SimpleFormatter.ReportGenerator.populatePercentilesOverview(c, blocks.get("{% PERCENTILES_BLOCK %}").toString());

        detail = detail.replaceAll("\\{% PERCENTILES_BLOCK %\\}", percentileData);

        overviews.append(overview);//.append("\n");
        details.append(detail);//.append("\n");
      };

      root = root.replaceAll("\\{% OVERVIEW_BLOCK %\\}", overviews.toString());
      root = root.replaceAll("\\{% DETAILED_BLOCK %\\}", details.toString());

      Files.write(outputPath, root.getBytes());

    } catch (Exception e) {
      throw new IllegalStateException(e);
    }

//    JtwigTemplate template = JtwigTemplate.classpathTemplate(REPORT_TEMPLATE);
//    JtwigModel model = JtwigModel.newModel().with("contextData", history);
//    try {
//      Files.createDirectories(outputPath.getParent());
//      log.info("Rendering report to: " + outputPath);
//      template.render(model, Files.newOutputStream(outputPath));
//    } catch (IOException e) {
//      throw new IllegalStateException(e);
//    }
  }

  @Override
  public String getReportPath() {
    return reportPath;
  }

}
