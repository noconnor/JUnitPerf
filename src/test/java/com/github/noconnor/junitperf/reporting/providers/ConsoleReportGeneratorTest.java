package com.github.noconnor.junitperf.reporting.providers;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
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
  public void whenGeneratingAReport_andAllTestsFailed_thenAppropriateReportShouldBeGenerated() throws IOException {
    reportGenerator.generateReport(generateAllFailureOrderedContexts());
  }

  @Test
  public void whenGeneratingAReport_andAllTestsPass_thenAppropriateReportShouldBeGenerated() throws IOException {
    reportGenerator.generateReport(generateAllPassedOrderedContexts());
  }

  @Test
  public void whenGeneratingAReport_andTestsContainsAMixOfPassAndFailures_thenAppropriateReportShouldBeGenerated() throws IOException {
    reportGenerator.generateReport(generateMixedOrderedContexts());
  }

  @Test
  public void whenGeneratingAReport_andTestsContainsSomeFailures_thenAppropriateReportShouldBeGenerated() throws IOException {
    reportGenerator.generateReport(generateSomeFailuresContext());
  }

  @Test
  public void whenCallingGetReportPath_thenNullShouldBeReturned() throws IOException {
    assertNull(reportGenerator.getReportPath());
  }
}
