package com.github.noconnor.junitperf.reporting;

import java.util.Set;
import com.github.noconnor.junitperf.data.EvaluationContext;

public interface ReportGenerator {

  void generateReport(Set<EvaluationContext> testContexts);

  String getReportPath();

}
