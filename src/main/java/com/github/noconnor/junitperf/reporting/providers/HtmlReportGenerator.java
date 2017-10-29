package com.github.noconnor.junitperf.reporting.providers;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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

  public HtmlReportGenerator() {this(DEFAULT_REPORT_PATH);}

  @SuppressWarnings("WeakerAccess")
  public HtmlReportGenerator(String reportPath) {this.reportPath = reportPath;}

  @Override
  public void generateReport(Set<EvaluationContext> testContexts) {
    Path outputPath = Paths.get(reportPath);
    JtwigTemplate template = JtwigTemplate.classpathTemplate(REPORT_TEMPLATE);
    JtwigModel model = JtwigModel.newModel()
      .with("contextData", testContexts)
      .with("milliseconds", TimeUnit.MILLISECONDS);
    try {
      Files.createDirectories(outputPath.getParent());
      log.info("Rendering report to: " + outputPath);
      template.render(model, Files.newOutputStream(outputPath));
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

}
