package com.noconnor.junitperf;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.ClassRule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import com.noconnor.junitperf.data.EvaluationContext;
import com.noconnor.junitperf.reporting.ReportGenerator;
import com.noconnor.junitperf.reporting.providers.HtmlReportGenerator;
import com.noconnor.junitperf.statements.PerformanceEvaluationStatement;
import com.noconnor.junitperf.statements.PerformanceEvaluationStatement.PerformanceEvaluationStatementBuilder;

import static com.google.common.collect.Sets.newConcurrentHashSet;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Objects.nonNull;

@Slf4j
public class JUnitPerfRule implements TestRule {

  private static final Set<EvaluationContext> ACTIVE_CONTEXTS = newHashSet();

  private final PerformanceEvaluationStatementBuilder perEvalBuilder;
  private final ReportGenerator reporter;

  @SuppressWarnings("WeakerAccess")
  public JUnitPerfRule() {
    this(PerformanceEvaluationStatement.perfEvalBuilder(), new HtmlReportGenerator());
  }

  // Test only
  JUnitPerfRule(PerformanceEvaluationStatementBuilder perEvalBuilder, ReportGenerator reporter) {
    this.perEvalBuilder = perEvalBuilder;
    this.reporter = reporter;
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
      ACTIVE_CONTEXTS.add(context);
      activeStatement = perEvalBuilder.baseStatement(base).context(context).listener(complete -> generateReport()).build();
    }
    return activeStatement;
  }

  public static void generateReport() {
    new HtmlReportGenerator().generateReport(ACTIVE_CONTEXTS);
  }

}
