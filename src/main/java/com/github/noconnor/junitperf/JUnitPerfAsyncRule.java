package com.github.noconnor.junitperf;

import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.data.NoOpTestContext;
import com.github.noconnor.junitperf.data.TestContext;
import com.github.noconnor.junitperf.reporting.ReportGenerator;
import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import com.github.noconnor.junitperf.statistics.providers.DescriptiveStatisticsCalculator;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;
import static java.util.Objects.nonNull;

@SuppressWarnings("WeakerAccess")
public class JUnitPerfAsyncRule extends JUnitPerfRule {

  private long measurementsStartTimeMs;

  public JUnitPerfAsyncRule() {
    this(new DescriptiveStatisticsCalculator(), new HtmlReportGenerator());
  }

  public JUnitPerfAsyncRule(ReportGenerator... reportGenerator) {
    this(new DescriptiveStatisticsCalculator(), reportGenerator);
  }

  public JUnitPerfAsyncRule(StatisticsCalculator statisticsCalculator) {
    this(statisticsCalculator, new HtmlReportGenerator());
  }

  public JUnitPerfAsyncRule(StatisticsCalculator statisticsCalculator, ReportGenerator... reportGenerator) {
    super(statisticsCalculator, reportGenerator);
  }

  public TestContext newContext() {
    return hasMeasurementPeriodStarted() ? new TestContext(statisticsCalculator) : NoOpTestContext.INSTANCE;
  }

  @Override
  public Statement apply(Statement base, Description description) {
    setMeasurementsStartTime(description.getAnnotation(JUnitPerfTest.class));
    return super.apply(base, description);
  }

  @Override
  EvaluationContext createEvaluationContext(Description description) {
    return new EvaluationContext(description.getMethodName(), nanoTime(), true);
  }

  private void setMeasurementsStartTime(JUnitPerfTest perfTestAnnotation) {
    if (nonNull(perfTestAnnotation)) {
      measurementsStartTimeMs = currentTimeMillis() + perfTestAnnotation.warmUpMs();
    }
  }

  private boolean hasMeasurementPeriodStarted() {
    return currentTimeMillis() >= measurementsStartTimeMs;
  }

}
