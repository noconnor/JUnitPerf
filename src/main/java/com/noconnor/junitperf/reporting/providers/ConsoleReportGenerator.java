package com.noconnor.junitperf.reporting.providers;

import java.util.List;
import com.noconnor.junitperf.data.EvaluationContext;
import com.noconnor.junitperf.reporting.ReportGenerator;

public class ConsoleReportGenerator implements ReportGenerator {

  @Override
  public void generateReport(List<EvaluationContext> testContexts) {
    throw new UnsupportedOperationException();
  }

}
