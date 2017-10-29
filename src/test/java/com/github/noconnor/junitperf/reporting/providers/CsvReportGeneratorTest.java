package com.github.noconnor.junitperf.reporting.providers;

import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.reporting.BaseReportGeneratorTest;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CsvReportGeneratorTest extends BaseReportGeneratorTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Mock
  private JUnitPerfTest perfTestAnnotationMock;

  @Mock
  private JUnitPerfTestRequirement perfTestRequirementAnnotationMock;


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
//    File expectedContents = getResourceFile("example_all_failed_report.html");
//    assertThat(readFileContents(reportFile), is(readFileContents(expectedContents)));
    saveContentToFile("/Users/noconnor/Desktop/failed.csv", reportFile);
  }

  @Test
  public void whenGeneratingAReport_andAllTestsPass_thenAppropriateReportShouldBeGenerated() throws IOException {
    reportGenerator.generateReport(generateAllPassedOrderedContexts());
//    File expectedContents = getResourceFile("example_all_passed_report.html");
//    assertThat(readFileContents(reportFile), is(readFileContents(expectedContents)));
    saveContentToFile("/Users/noconnor/Desktop/passed.csv", reportFile);
  }

  @Test
  public void whenGeneratingAReport_andTestsContainsAMixOfPassAndFailures_thenAppropriateReportShouldBeGenerated() throws IOException {
    reportGenerator.generateReport(generateMixedOrderedContexts());
//    File expectedContents = getResourceFile("example_mixed_report.html");
//    assertThat(readFileContents(reportFile), is(readFileContents(expectedContents)));
    saveContentToFile("/Users/noconnor/Desktop/mix.csv", reportFile);
  }

  @Test
  public void whenGeneratingAReport_andTestsContainsSomeFailures_thenAppropriateReportShouldBeGenerated() throws IOException {
    reportGenerator.generateReport(generateSomeFailuresContext());
//    File expectedContents = getResourceFile("example_some_failures_report.html");
//    assertThat(readFileContents(reportFile), is(readFileContents(expectedContents)));
    saveContentToFile("/Users/noconnor/Desktop/some_failures.csv", reportFile);
  }


}
