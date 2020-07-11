package com.github.noconnor.junitperf.reporting;

import com.github.noconnor.junitperf.data.EvaluationContext;

import java.util.LinkedHashSet;

public interface ReportGenerator {

  void generateReport(LinkedHashSet<EvaluationContext> testContexts);

  String getReportPath();

}
