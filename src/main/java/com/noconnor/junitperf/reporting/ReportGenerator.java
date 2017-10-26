package com.noconnor.junitperf.reporting;

import java.util.Set;
import com.noconnor.junitperf.data.EvaluationContext;

public interface ReportGenerator {

  void generateReport(Set<EvaluationContext> testContexts);
  
}
