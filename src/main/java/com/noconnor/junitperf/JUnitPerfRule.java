package com.noconnor.junitperf;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import com.noconnor.junitperf.data.EvaluationContext;
import com.noconnor.junitperf.reporting.ReportGenerator;
import com.noconnor.junitperf.reporting.providers.HtmlReportGenerator;
import com.noconnor.junitperf.statements.PerformanceEvaluationStatement;
import com.noconnor.junitperf.statements.PerformanceEvaluationStatement.PerformanceEvaluationStatementBuilder;
import com.noconnor.junitperf.statistics.StatisticsCalculator;
import com.noconnor.junitperf.statistics.providers.DescriptiveStatisticsCalculator;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Objects.nonNull;

@Slf4j
public class JUnitPerfRule implements TestRule {

  static final Map<Class, Set<EvaluationContext>> ACTIVE_CONTEXTS = newHashMap();

  private final PerformanceEvaluationStatementBuilder perEvalBuilder;
  private final StatisticsCalculator statisticsCalculator;
  private final ReportGenerator reporter;

  @SuppressWarnings("WeakerAccess")
  public JUnitPerfRule() {
    this(PerformanceEvaluationStatement.perfEvalBuilder(),
      new HtmlReportGenerator(),
      new DescriptiveStatisticsCalculator());
  }

  @SuppressWarnings("WeakerAccess")
  public JUnitPerfRule(ReportGenerator reportGenerator) {
    this(PerformanceEvaluationStatement.perfEvalBuilder(), reportGenerator, new DescriptiveStatisticsCalculator());
  }

  @SuppressWarnings("WeakerAccess")
  public JUnitPerfRule(StatisticsCalculator statisticsCalculator) {
    this(PerformanceEvaluationStatement.perfEvalBuilder(), new HtmlReportGenerator(), statisticsCalculator);
  }

  @SuppressWarnings("WeakerAccess")
  public JUnitPerfRule(ReportGenerator reportGenerator, StatisticsCalculator statisticsCalculator) {
    this(PerformanceEvaluationStatement.perfEvalBuilder(), reportGenerator, statisticsCalculator);
  }

  // Test only
  JUnitPerfRule(PerformanceEvaluationStatementBuilder perEvalBuilder,
                ReportGenerator reporter,
                StatisticsCalculator statisticsCalculator) {
    this.perEvalBuilder = perEvalBuilder;
    this.statisticsCalculator = statisticsCalculator;
    this.reporter = reporter;
  }

  @Override
  public Statement apply(Statement base, Description description) {
    Statement activeStatement = base;
    JUnitPerfTest perfTestAnnotation = description.getAnnotation(JUnitPerfTest.class);
    JUnitPerfTestRequirement requirementsAnnotation = description.getAnnotation(JUnitPerfTestRequirement.class);

    if (nonNull(perfTestAnnotation)) {
      // Group test contexts by test class
      ACTIVE_CONTEXTS.putIfAbsent(description.getTestClass(), newHashSet());

      EvaluationContext context = new EvaluationContext(description.getMethodName(), generateTestStartTime());
      context.loadConfiguration(perfTestAnnotation);
      context.loadRequirements(requirementsAnnotation);
      ACTIVE_CONTEXTS.get(description.getTestClass()).add(context);
      activeStatement = perEvalBuilder.baseStatement(base)
        .statistics(statisticsCalculator)
        .context(context)
        .listener(complete -> updateReport(description.getTestClass()))
        .build();
    }
    return activeStatement;
  }

  private void updateReport(Class<?> testClass) {
    reporter.generateReport(ACTIVE_CONTEXTS.get(testClass));
  }

  private String generateTestStartTime() {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }

}
