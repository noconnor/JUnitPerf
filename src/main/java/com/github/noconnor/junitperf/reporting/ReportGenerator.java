package com.github.noconnor.junitperf.reporting;

import java.util.LinkedHashSet;
import com.github.noconnor.junitperf.data.EvaluationContext;

public interface ReportGenerator {

  void generateReport(LinkedHashSet<EvaluationContext> testContexts);

  String getReportPath();

}
