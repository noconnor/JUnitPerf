package com.noconnor.junitperf;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import com.noconnor.junitperf.statements.EvaluationTaskValidator;
import com.noconnor.junitperf.statements.EvaluationTaskValidator.EvaluationTaskValidatorBuilder;
import com.noconnor.junitperf.statements.PerformanceEvaluationStatement;
import com.noconnor.junitperf.statements.PerformanceEvaluationStatement.PerformanceEvaluationStatementBuilder;

import static java.util.Objects.nonNull;

public class JUnitPerfRule implements TestRule {

  private final PerformanceEvaluationStatementBuilder perEvalBuilder;
  private final EvaluationTaskValidatorBuilder validatorBuilder;

  @SuppressWarnings("WeakerAccess")
  public JUnitPerfRule() {
    this(PerformanceEvaluationStatement.perfEvalBuilder(), EvaluationTaskValidator.builder());
  }

  // Test only
  JUnitPerfRule(PerformanceEvaluationStatementBuilder perEvalBuilder, EvaluationTaskValidatorBuilder validatorBuilder) {
    this.perEvalBuilder = perEvalBuilder;
    this.validatorBuilder = validatorBuilder;
  }

  @Override
  public Statement apply(Statement base, Description description) {
    Statement activeStatement = base;
    JUnitPerfTest perfTestAnnotation = description.getAnnotation(JUnitPerfTest.class);
    JUnitPerfTestRequirement requirementsAnnotation = description.getAnnotation(JUnitPerfTestRequirement.class);
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
