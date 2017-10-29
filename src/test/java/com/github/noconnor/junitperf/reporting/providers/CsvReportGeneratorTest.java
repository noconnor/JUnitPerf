package com.github.noconnor.junitperf.reporting.providers;

import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import com.github.noconnor.junitperf.reporting.BaseReportGeneratorTest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CsvReportGeneratorTest extends BaseReportGeneratorTest {

  private CsvReportGenerator reportGenerator;

  private File reportFile;

  @Before
  public void setup() throws IOException {
    reportFile = folder.newFile("report.csv");
    reportGenerator = new CsvReportGenerator(reportFile.getPath());
    initialisePerfTestAnnotationMock();
    initialisePerfTestRequirementAnnotationMock();
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


}
