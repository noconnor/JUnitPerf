package com.github.noconnor.junitperf.reporting.providers;

import java.util.LinkedHashSet;
import org.junit.Before;
import org.junit.Test;
import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.reporting.BaseReportGeneratorTest;

import static org.junit.Assert.assertNull;

public class ConsoleReportGeneratorTest extends BaseReportGeneratorTest {

  private ConsoleReportGenerator reportGenerator;

  @Before
  public void setup() {
    reportGenerator = new ConsoleReportGenerator();
    initialisePerfTestAnnotationMock();
    initialisePerfTestRequirementAnnotationMock();
  }

  @Test
  public void whenGeneratingAReport_andAllTestsFailed_thenAppropriateReportShouldBeGenerated() {
    reportGenerator.generateReport(generateAllFailureOrderedContexts());
  }

  @Test
  public void whenGeneratingAReport_andAllTestsPass_thenAppropriateReportShouldBeGenerated() {
    reportGenerator.generateReport(generateAllPassedOrderedContexts());
  }

  @Test
  public void whenGeneratingAReport_andTestsContainsAMixOfPassAndFailures_thenAppropriateReportShouldBeGenerated() {
    reportGenerator.generateReport(generateMixedOrderedContexts());
  }

  @Test
  public void whenGeneratingAReport_andTestsContainsSomeFailures_thenAppropriateReportShouldBeGenerated() {
    reportGenerator.generateReport(generateSomeFailuresContext());
  }

  @Test
  public void whenGeneratingAReport_andGenerateIsCalledMultipleTimes_thenOnlyNewResultsShouldBePrinted() {
    LinkedHashSet<EvaluationContext> contexts = generateSomeFailuresContext();
    reportGenerator.generateReport(contexts);
    reportGenerator.generateReport(contexts);
  }

  @Test
  public void whenCallingGetReportPath_thenNullShouldBeReturned() {
    assertNull(reportGenerator.getReportPath());
  }
}
