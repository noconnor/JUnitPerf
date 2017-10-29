package com.github.noconnor.junitperf.reporting.providers;

import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import com.github.noconnor.junitperf.reporting.BaseReportGeneratorTest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class HtmlReportGeneratorTest extends BaseReportGeneratorTest {

  private HtmlReportGenerator reportGenerator;

  @Before
  public void setup() throws IOException {
    reportFile = folder.newFile("report.html");
    reportGenerator = new HtmlReportGenerator(reportFile.getPath());
    initialisePerfTestAnnotationMock();
    initialisePerfTestRequirementAnnotationMock();
  }

  @Test
  public void whenGeneratingAReport_andAllTestsFailed_thenAppropriateReportShouldBeGenerated() throws IOException {
    reportGenerator.generateReport(generateAllFailureOrderedContexts());
    File expectedContents = getResourceFile("html/example_all_failed_report.html");
    assertThat(readFileContents(reportFile), is(readFileContents(expectedContents)));
  }

  @Test
  public void whenGeneratingAReport_andAllTestsPass_thenAppropriateReportShouldBeGenerated() throws IOException {
    reportGenerator.generateReport(generateAllPassedOrderedContexts());
    File expectedContents = getResourceFile("html/example_all_passed_report.html");
    assertThat(readFileContents(reportFile), is(readFileContents(expectedContents)));
  }

  @Test
  public void whenGeneratingAReport_andTestsContainsAMixOfPassAndFailures_thenAppropriateReportShouldBeGenerated() throws IOException {
    reportGenerator.generateReport(generateMixedOrderedContexts());
    File expectedContents = getResourceFile("html/example_mixed_report.html");
    assertThat(readFileContents(reportFile), is(readFileContents(expectedContents)));
  }

  @Test
  public void whenGeneratingAReport_andTestsContainsSomeFailures_thenAppropriateReportShouldBeGenerated() throws IOException {
    reportGenerator.generateReport(generateSomeFailuresContext());
    File expectedContents = getResourceFile("html/example_some_failures_report.html");
    assertThat(readFileContents(reportFile), is(readFileContents(expectedContents)));
  }

}
