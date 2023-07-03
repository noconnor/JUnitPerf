package com.github.noconnor.junitperf;

import static java.lang.System.nanoTime;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;

import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.reporting.ReportGenerator;
import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;
import com.github.noconnor.junitperf.statements.DefaultStatement;
import com.github.noconnor.junitperf.statements.ExceptionsRegistry;
import com.github.noconnor.junitperf.statements.MeasurableStatement;
import com.github.noconnor.junitperf.statements.PerformanceEvaluationStatement;
import com.github.noconnor.junitperf.statements.PerformanceEvaluationStatement.PerformanceEvaluationStatementBuilder;
import com.github.noconnor.junitperf.statements.TestStatement;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import com.github.noconnor.junitperf.statistics.providers.DescriptiveStatisticsCalculator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import lombok.Setter;
import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

@SuppressWarnings("WeakerAccess")
public class JUnitPerfRule implements TestRule {

  static final Map<Class<?>, LinkedHashSet<EvaluationContext>> ACTIVE_CONTEXTS = new HashMap<>();
  
  static {
    ExceptionsRegistry.registerIgnorable(InterruptedException.class);
    ExceptionsRegistry.registerAbort(AssumptionViolatedException.class);
  }

  private final Set<ReportGenerator> reporters;

  StatisticsCalculator statisticsCalculator;
  PerformanceEvaluationStatementBuilder perEvalBuilder;
  boolean excludeBeforeAndAfters;

  @Setter
  private JUnitPerfTest defaultPerfTestAnnotation;
  @Setter
  private JUnitPerfTestRequirement defaultRequirementsAnnotation;
  
  public JUnitPerfRule() {
    this(false);
  }

  public JUnitPerfRule(boolean excludeBeforeAndAfters) {
    this(excludeBeforeAndAfters, new DescriptiveStatisticsCalculator(), new HtmlReportGenerator());
  }

  public JUnitPerfRule(ReportGenerator... reportGenerator) {
    this(false, reportGenerator);
  }

  public JUnitPerfRule(boolean excludeBeforeAndAfters, ReportGenerator... reportGenerator) {
    this(excludeBeforeAndAfters, new DescriptiveStatisticsCalculator(), reportGenerator);
  }

  public JUnitPerfRule(StatisticsCalculator statisticsCalculator) {
    this(false, statisticsCalculator);
  }

  public JUnitPerfRule(boolean excludeBeforeAndAfters, StatisticsCalculator statisticsCalculator) {
    this(excludeBeforeAndAfters, statisticsCalculator, new HtmlReportGenerator());
  }

  public JUnitPerfRule(StatisticsCalculator statisticsCalculator, ReportGenerator... reportGenerator) {
    this(false, statisticsCalculator, reportGenerator);
  }

  public JUnitPerfRule(boolean excludeBeforeAndAfters, StatisticsCalculator statisticsCalculator, ReportGenerator... reportGenerator) {
    this.perEvalBuilder = PerformanceEvaluationStatement.builder();
    this.statisticsCalculator = statisticsCalculator;
    this.reporters = Arrays.stream(reportGenerator).collect(toSet());
    this.excludeBeforeAndAfters = excludeBeforeAndAfters;
  }

  @Override
  public Statement apply(Statement base, Description description) {
    Statement activeStatement = base;

    JUnitPerfTest perfTestAnnotation = description.getAnnotation(JUnitPerfTest.class);
    JUnitPerfTestRequirement requirementsAnnotation = description.getAnnotation(JUnitPerfTestRequirement.class);

    perfTestAnnotation = nonNull(perfTestAnnotation) ? perfTestAnnotation : defaultPerfTestAnnotation;
    requirementsAnnotation = nonNull(requirementsAnnotation) ? requirementsAnnotation : defaultRequirementsAnnotation;
            
    if (nonNull(perfTestAnnotation)) {
      EvaluationContext context = createEvaluationContext(description);
      context.setGroupName(description.getTestClass().getName());
      context.loadConfiguration(perfTestAnnotation);
      context.loadRequirements(requirementsAnnotation);

      // Group test contexts by test class
      ACTIVE_CONTEXTS.putIfAbsent(description.getTestClass(), new LinkedHashSet<>());
      ACTIVE_CONTEXTS.get(description.getTestClass()).add(context);

      TestStatement testStatement = excludeBeforeAndAfters ? new MeasurableStatement(base) : new DefaultStatement(base);

      PerformanceEvaluationStatement parallelExecution = perEvalBuilder.baseStatement(testStatement)
        .statistics(statisticsCalculator)
        .context(context)
        .listener(complete -> updateReport(description.getTestClass()))
        .build();

      activeStatement = new Statement() {
        @Override
        public void evaluate() throws Throwable {
          parallelExecution.runParallelEvaluation();
        }
      };
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
