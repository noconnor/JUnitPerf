package com.noconnor.junitperf;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import com.noconnor.junitperf.data.EvaluationContext;
import com.noconnor.junitperf.statements.PerformanceEvaluationStatement;
import com.noconnor.junitperf.statements.PerformanceEvaluationStatement.PerformanceEvaluationStatementBuilder;

import static java.util.Objects.nonNull;

public class JUnitPerfRule implements TestRule {

  private final PerformanceEvaluationStatementBuilder perEvalBuilder;

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
    JUnitPerfTestRequirement requirementsAnnotation = description.getAnnotation(JUnitPerfTestRequirement.class);

    if (nonNull(perfTestAnnotation)) {
      String startTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
      EvaluationContext context = new EvaluationContext(description.getMethodName(), startTime);
      context.loadConfiguration(perfTestAnnotation);
      context.loadRequirements(requirementsAnnotation);
      activeStatement = perEvalBuilder.baseStatement(base).context(context).build();
    }
    return activeStatement;
  }

}
