package com.github.noconnor.junitperf;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.reporting.ReportGenerator;
import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;
import com.github.noconnor.junitperf.statements.PerformanceEvaluationStatement;
import com.github.noconnor.junitperf.statements.PerformanceEvaluationStatement.PerformanceEvaluationStatementBuilder;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import com.github.noconnor.junitperf.statistics.providers.DescriptiveStatisticsCalculator;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.lang.System.nanoTime;
import static java.util.Objects.nonNull;

@Slf4j
@SuppressWarnings("WeakerAccess")
public class JUnitPerfRule implements TestRule {

  static final Map<Class, LinkedHashSet<EvaluationContext>> ACTIVE_CONTEXTS = newHashMap();

  private final Set<ReportGenerator> reporters;

  StatisticsCalculator statisticsCalculator;
  PerformanceEvaluationStatementBuilder perEvalBuilder;

  public JUnitPerfRule() {
    this(new DescriptiveStatisticsCalculator(), new HtmlReportGenerator());
  }

  public JUnitPerfRule(ReportGenerator... reportGenerator) {
    this(new DescriptiveStatisticsCalculator(), reportGenerator);
  }

  public JUnitPerfRule(StatisticsCalculator statisticsCalculator) {
    this(statisticsCalculator, new HtmlReportGenerator());
  }

  public JUnitPerfRule(StatisticsCalculator statisticsCalculator, ReportGenerator... reportGenerator) {
    this.perEvalBuilder = PerformanceEvaluationStatement.builder();
    this.statisticsCalculator = statisticsCalculator;
    this.reporters = newHashSet(reportGenerator);
  }

  @Override
  public Statement apply(Statement base, Description description) {
    Statement activeStatement = base;

    JUnitPerfTest perfTestAnnotation = description.getAnnotation(JUnitPerfTest.class);
    JUnitPerfTestRequirement requirementsAnnotation = description.getAnnotation(JUnitPerfTestRequirement.class);

    if (nonNull(perfTestAnnotation)) {
      EvaluationContext context = createEvaluationContext(description);
      context.loadConfiguration(perfTestAnnotation);
      context.loadRequirements(requirementsAnnotation);

      // Group test contexts by test class
      ACTIVE_CONTEXTS.putIfAbsent(description.getTestClass(), newLinkedHashSet());
      ACTIVE_CONTEXTS.get(description.getTestClass()).add(context);

      activeStatement = perEvalBuilder.baseStatement(base)
        .statistics(statisticsCalculator)
        .context(context)
        .listener(complete -> updateReport(description.getTestClass()))
        .build();
    }
    return activeStatement;
  }

  EvaluationContext createEvaluationContext(Description description) {
    return new EvaluationContext(description.getMethodName(), nanoTime());
  }

  private synchronized void updateReport(Class<?> testClass) {
    reporters.forEach(r -> {
      r.generateReport(ACTIVE_CONTEXTS.get(testClass));
    });
  }

}
