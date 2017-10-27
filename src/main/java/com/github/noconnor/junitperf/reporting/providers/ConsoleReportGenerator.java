package com.github.noconnor.junitperf.reporting.providers;

import java.util.Set;
import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.reporting.ReportGenerator;

public class ConsoleReportGenerator implements ReportGenerator {

  @Override
  public void generateReport(Set<EvaluationContext> testContexts) {
    throw new UnsupportedOperationException();
  }

}
