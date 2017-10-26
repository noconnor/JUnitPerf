package com.noconnor.junitperf.reporting.providers;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import com.noconnor.junitperf.data.EvaluationContext;
import com.noconnor.junitperf.reporting.ReportGenerator;

import static com.google.common.collect.Lists.newArrayList;

@Slf4j
public class HtmlReportGenerator implements ReportGenerator {

  @Override
  public void generateReport(List<EvaluationContext> testContexts) {

    Path outputPath = Paths.get(System.getProperty("user.dir") + "/build/reports/junit_report.html");
    JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/report.twig");
    JtwigModel model = JtwigModel.newModel().with("contextData", testContexts).with("milliseconds", TimeUnit.MILLISECONDS);

    try {
      Files.createDirectories(outputPath.getParent());
      log.info("Rendering report to: " + outputPath);
      template.render(model, Files.newOutputStream(outputPath));
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public static void main(String[] args) {
    EvaluationContext context = new EvaluationContext(null, "unittest_with_long_name");
    new HtmlReportGenerator().generateReport(newArrayList(context));
  }

}
