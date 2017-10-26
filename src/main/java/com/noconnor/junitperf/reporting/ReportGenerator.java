package com.noconnor.junitperf.reporting;

import java.util.List;
import com.noconnor.junitperf.data.EvaluationContext;

public interface ReportGenerator {

  void generateReport(List<EvaluationContext> testContexts);
  
}
