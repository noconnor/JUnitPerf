package com.noconnor.junitperf;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import com.noconnor.junitperf.statements.PerformanceEvaluationStatement;
import com.noconnor.junitperf.statements.PerformanceEvaluationStatement.PerformanceEvaluationStatementBuilder;
import com.noconnor.junitperf.statistics.StatisticsValidator;
import com.noconnor.junitperf.statistics.StatisticsValidator.StatisticsValidatorBuilder;

import static java.util.Objects.nonNull;

public class JUnitPerfRule implements TestRule {

  private final PerformanceEvaluationStatementBuilder perEvalBuilder;
  private final StatisticsValidatorBuilder validatorBuilder;

  @SuppressWarnings("WeakerAccess")
  public JUnitPerfRule() {
    this(PerformanceEvaluationStatement.perfEvalBuilder(), StatisticsValidator.builder());
  }

  // Test only
  JUnitPerfRule(PerformanceEvaluationStatementBuilder perEvalBuilder, StatisticsValidatorBuilder validatorBuilder) {
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
        .validator(buildValidator(requirementsAnnotation, perfTestAnnotation.duration()))
        .build();
    }
    return activeStatement;
  }

  private StatisticsValidator buildValidator(JUnitPerfTestRequirement annotation, int testDurationMs) {
    StatisticsValidator validator = null;
    if (nonNull(annotation)) {
      validator = validatorBuilder.percentiles(annotation.percentiles())
        .expectedThroughput(annotation.throughput())
        .allowedErrorsRate(annotation.allowedErrorsRate())
        .durationMs(testDurationMs)
        .build();
    }
    return validator;
  }
}
