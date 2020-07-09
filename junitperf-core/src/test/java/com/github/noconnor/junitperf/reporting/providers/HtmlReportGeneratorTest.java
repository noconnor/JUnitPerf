package com.github.noconnor.junitperf.reporting.providers;

import java.io.File;
import java.io.IOException;

import com.github.noconnor.junitperf.datetime.DatetimeUtils;

import org.junit.Before;
import org.junit.Test;
import com.github.noconnor.junitperf.reporting.BaseReportGeneratorTest;

import static java.lang.System.getProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class HtmlReportGeneratorTest extends BaseReportGeneratorTest {

  private HtmlReportGenerator reportGenerator;

  @Before
  public void setup() throws IOException {
    reportFile = folder.newFile("report.html");
    reportGenerator = new HtmlReportGenerator(reportFile.getPath());
    DatetimeUtils.setOverride("unittest o'clock");
    initialisePerfTestAnnotationMock();
    initialisePerfTestRequirementAnnotationMock();
  }

  @Test
  public void whenCallingDefaultConstructor_thenNoExceptionShouldBeThrown() throws IOException {
    reportGenerator = new HtmlReportGenerator();
  }

  @Test(expected = IllegalStateException.class)
  public void whenGeneratingAReport_andPathIsNotWritable_thenExceptionShouldBeThrown() throws IOException {
    reportGenerator = new HtmlReportGenerator("///foo");
    reportGenerator.generateReport(generateAllFailureOrderedContexts());
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

  @Test
  public void whenCallingGetReportPath_andCustomPathHasBeenSpecified_thenCorrectPathShouldBeReturned() {
    assertThat(reportGenerator.getReportPath(), is(reportFile.getPath()));
  }

  @Test
  public void whenCallingGetReportPath_andDefaultPathHasBeenSpecified_thenCorrectPathShouldBeReturned() {
    reportGenerator = new HtmlReportGenerator();
    assertThat(reportGenerator.getReportPath(), is(getProperty("user.dir") + "/build/reports/junitperf_report.html"));
  }

}
