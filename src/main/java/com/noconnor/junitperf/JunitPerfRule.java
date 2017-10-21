package com.noconnor.junitperf;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import com.noconnor.junitperf.annotations.JUnitPerfTest;

import static com.noconnor.junitperf.statements.PerformanceEvaluationStatement.perfEvalBuilder;
import static java.util.Objects.nonNull;

public class JunitPerfRule implements TestRule {

  @Override
  public Statement apply(Statement base, Description description) {
    Statement activeStatement = base;
    JUnitPerfTest perfTestAnnotation = description.getAnnotation(JUnitPerfTest.class);
    if (nonNull(perfTestAnnotation)) {
      activeStatement = perfEvalBuilder().baseStatement(base).build();
    }
    return activeStatement;
  }
}
