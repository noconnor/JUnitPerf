package com.github.noconnor.junitperf.reporting.providers;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.github.noconnor.junitperf.reporting.BaseReportGeneratorTest;

import static java.lang.System.getProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CsvReportGeneratorTest extends BaseReportGeneratorTest {

  private Locale defaultLocale;

  private CsvReportGenerator reportGenerator;

  private File reportFile;

  @Before
  public void setup() throws IOException {
    reportFile = folder.newFile("report.csv");
    reportGenerator = new CsvReportGenerator(reportFile.getPath());
    initialisePerfTestAnnotationMock();
    initialisePerfTestRequirementAnnotationMock();
    defaultLocale = Locale.getDefault();
    // set local to en-US as this test expects numbers to use "."
    // as a decimal separator and "," as a grouping separator, e.g. 1,337.42
    Locale.setDefault(Locale.US);
  }

  @After
  public void after() {
    // restore default locale
    Locale.setDefault(defaultLocale);
  }

  @Test
  public void whenCallingDefaultConstructor_thenNoExceptionShouldBeThrown() throws IOException {
    reportGenerator = new CsvReportGenerator();
  }

  @Test(expected = IllegalStateException.class)
  public void whenGeneratingAReport_andPathIsNotWritable_thenExceptionShouldBeThrown() throws IOException {
    reportGenerator = new CsvReportGenerator("///foo");
    reportGenerator.generateReport(generateAllFailureOrderedContexts());
  }

  @Test
  public void whenGeneratingAReport_andAllTestsFailed_thenAppropriateReportShouldBeGenerated() throws IOException {
    reportGenerator.generateReport(generateAllFailureOrderedContexts());
    File expectedContents = getResourceFile("csv/failed.csv");
    assertThat(readFileContents(reportFile), is(readFileContents(expectedContents)));
  }

  @Test
  public void whenGeneratingAReport_andAllTestsPass_thenAppropriateReportShouldBeGenerated() throws IOException {
    reportGenerator.generateReport(generateAllPassedOrderedContexts());
    File expectedContents = getResourceFile("csv/passed.csv");
    assertThat(readFileContents(reportFile), is(readFileContents(expectedContents)));
  }

  @Test
  public void whenGeneratingAReport_andTestsContainsAMixOfPassAndFailures_thenAppropriateReportShouldBeGenerated() throws IOException {
    reportGenerator.generateReport(generateMixedOrderedContexts());
    File expectedContents = getResourceFile("csv/mix.csv");
    assertThat(readFileContents(reportFile), is(readFileContents(expectedContents)));
  }

  @Test
  public void whenGeneratingAReport_andTestsContainsSomeFailures_thenAppropriateReportShouldBeGenerated() throws IOException {
    reportGenerator.generateReport(generateSomeFailuresContext());
    File expectedContents = getResourceFile("csv/some_failures.csv");
    assertThat(readFileContents(reportFile), is(readFileContents(expectedContents)));
  }

  @Test
  public void whenGeneratingAReport_andTestsContainsSomeAbortsAndFailures_thenAppropriateReportShouldBeGenerated() throws IOException {
    reportGenerator.generateReport(generateAbortedFailedAndSuccessContexts());
    File expectedContents = getResourceFile("csv/fail_abort_succeed.csv");
    assertEquals(readFileContents(expectedContents), readFileContents(reportFile));
  }

  @Test
  public void whenGeneratingAReport_andTestsContainsSomeAbortsAndFailures_andGenerateReportIsCalledMultipleTimes_thenAppropriateReportShouldBeGenerated() throws IOException {
    reportGenerator.generateReport(generateAbortedFailedAndSuccessContexts());
    reportGenerator.generateReport(generateAbortedFailedAndSuccessContexts());
    File expectedContents = getResourceFile("csv/fail_abort_succeed.csv");
    assertEquals(readFileContents(expectedContents), readFileContents(reportFile));
  }

  @Test
  public void whenCallingGetReportPath_andCustomPathHasBeenSpecified_thenCorrectPathShouldBeReturned() {
    assertThat(reportGenerator.getReportPath(), is(reportFile.getPath()));
  }

  @Test
  public void whenCallingGetReportPath_andDefaultPathHasBeenSpecified_thenCorrectPathShouldBeReturned() {
    reportGenerator = new CsvReportGenerator();
    assertThat(reportGenerator.getReportPath(), is(getProperty("user.dir") + "/build/reports/junitperf_report.csv"));
  }

}
