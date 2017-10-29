package com.github.noconnor.junitperf.reporting.providers;

import java.util.Set;

import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.reporting.ReportGenerator;

import static java.lang.System.getProperty;

public class CsvReportGenerator implements ReportGenerator {

  private static final String DEFAULT_REPORT_PATH = getProperty("user.dir") + "/build/reports/junitperf_report.csv";

  private final String reportPath;

  public CsvReportGenerator() {this(DEFAULT_REPORT_PATH);}

  @SuppressWarnings("WeakerAccess")
  public CsvReportGenerator(String reportPath) {this.reportPath = reportPath;}


  @Override
  public void generateReport(final Set<EvaluationContext> testContexts) {

  }
}
