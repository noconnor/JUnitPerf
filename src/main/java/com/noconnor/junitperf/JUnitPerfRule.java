package com.noconnor.junitperf;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import com.noconnor.junitperf.statements.PerformanceEvaluationStatement;
import com.noconnor.junitperf.statements.PerformanceEvaluationStatement.PerformanceEvaluationStatementBuilder;

import static java.util.Objects.nonNull;

public class JUnitPerfRule implements TestRule {

  private PerformanceEvaluationStatementBuilder perEvalBuilder;

  @SuppressWarnings("WeakerAccess")
  public JUnitPerfRule() {
    this(PerformanceEvaluationStatement.perfEvalBuilder());
  }

  // Test only
  JUnitPerfRule(PerformanceEvaluationStatementBuilder perEvalBuilder) {
    this.perEvalBuilder = perEvalBuilder;
  }

  @Override
  public Statement apply(Statement base, Description description) {
    Statement activeStatement = base;
    JUnitPerfTest perfTestAnnotation = description.getAnnotation(JUnitPerfTest.class);
    if (nonNull(perfTestAnnotation)) {
      activeStatement = perEvalBuilder.baseStatement(base)
        .rateLimitExecutionsPerSecond(perfTestAnnotation.rateLimit())
        .warmUpPeriodMs(perfTestAnnotation.warmUp())
        .threadCount(perfTestAnnotation.threads())
        .testDurationMs(perfTestAnnotation.duration())
        .build();
    }
    return activeStatement;
  }
}
