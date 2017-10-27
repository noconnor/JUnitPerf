package com.noconnor.junitperf.reporting.providers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import com.noconnor.junitperf.BaseTest;
import com.noconnor.junitperf.JUnitPerfTest;
import com.noconnor.junitperf.JUnitPerfTestRequirement;
import com.noconnor.junitperf.data.EvaluationContext;
import com.noconnor.junitperf.statistics.Statistics;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HtmlReportGeneratorTest extends BaseTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  private HtmlReportGenerator reportGenerator;

  private File reportFile;

  @Mock
  private JUnitPerfTest perfTestAnnotationMock;

  @Mock
  private JUnitPerfTestRequirement perfTestRequirementAnnotationMock;

  @Before
  public void setup() throws IOException {
    reportFile = folder.newFile("report.html");
    reportGenerator = new HtmlReportGenerator(reportFile.getPath());
    initialisePerfTestAnnotationMock();
    initialisePerfTestRequirementAnnotationMock();
  }

  @Test
  public void whenGeneratingAReport_andAllTestsFailed_thenAppropriateReportShouldBeGenerated() throws IOException {
    EvaluationContext context1 = createdFailedEvaluationContext("unittest1");
    EvaluationContext context2 = createdFailedEvaluationContext("unittest2");
    verifyAllValidationFailed(context1);
    verifyAllValidationFailed(context2);
    reportGenerator.generateReport(newLinkedHashSet(newArrayList(context1, context2)));
    // Are context as expected
    File expectedContents = getResourceFile("example_all_failed_report.html");
    assertThat(readFileContents(reportFile), is(readFileContents(expectedContents)));
  }

  @Test
  public void whenGeneratingAReport_andAllTestsPass_thenAppropriateReportShouldBeGenerated() throws IOException {
    EvaluationContext context1 = createdSuccessfulEvaluationContext("unittest1");
    EvaluationContext context2 = createdSuccessfulEvaluationContext("unittest2");
    verifyAllValidationPassed(context1);
    verifyAllValidationPassed(context2);
    reportGenerator.generateReport(newLinkedHashSet(newArrayList(context1, context2)));
    // Are context as expected
    File expectedContents = getResourceFile("example_all_passed_report.html");
    assertThat(readFileContents(reportFile), is(readFileContents(expectedContents)));
  }

  @Test
  public void whenGeneratingAReport_andTestsContainsAMixOfPassAndFailures_thenAppropriateReportShouldBeGenerated() throws IOException {
    EvaluationContext context1 = createdFailedEvaluationContext("unittest1");
    EvaluationContext context2 = createdSuccessfulEvaluationContext("unittest2");
    verifyAllValidationFailed(context1);
    verifyAllValidationPassed(context2);
    reportGenerator.generateReport(newLinkedHashSet(newArrayList(context1, context2)));
    // Are context as expected
    File expectedContents = getResourceFile("example_mixed_report.html");
    assertThat(readFileContents(reportFile), is(readFileContents(expectedContents)));
  }

  @Test
  public void whenGeneratingAReport_andTestsContainsSomeFailures_thenAppropriateReportShouldBeGenerated() throws IOException {
    EvaluationContext context = createdSomeFailuresEvaluationContext("unittest1");
    reportGenerator.generateReport(newLinkedHashSet(newArrayList(context)));
    // Are context as expected
    File expectedContents = getResourceFile("example_some_failures_report.html");
    assertThat(readFileContents(reportFile), is(readFileContents(expectedContents)));
  }

  private File getResourceFile(String fileName) {
    ClassLoader classLoader = getClass().getClassLoader();
    return new File(classLoader.getResource(fileName).getFile());
  }

  private String readFileContents(final File file) throws IOException {
    return new String(Files.readAllBytes(file.toPath()), Charset.forName("utf-8")).replaceAll("\\s+", "");
  }

  private void verifyAllValidationFailed(EvaluationContext context) {
    assertFalse(context.isErrorThresholdAchieved());
    assertFalse(context.isThroughputAchieved());
    assertTrue(context.getPercentileResults().values().stream().noneMatch(e -> e));
    assertFalse(context.isSuccessful());
  }

  private void verifyAllValidationPassed(EvaluationContext context) {
    assertTrue(context.isErrorThresholdAchieved());
    assertTrue(context.isThroughputAchieved());
    assertFalse(context.getPercentileResults().values().stream().noneMatch(e -> e));
    assertTrue(context.isSuccessful());
  }

  private EvaluationContext createdFailedEvaluationContext(String name) {
    EvaluationContext context = new EvaluationContext(name, "unittest o'clock");
    context.loadConfiguration(perfTestAnnotationMock);
    context.loadRequirements(perfTestRequirementAnnotationMock);
    context.setStatistics(createAllFailureMock());
    context.runValidation();
    return context;
  }

  private EvaluationContext createdSuccessfulEvaluationContext(String name) {
    EvaluationContext context = new EvaluationContext(name, "unittest o'clock");
    context.loadConfiguration(perfTestAnnotationMock);
    context.loadRequirements(perfTestRequirementAnnotationMock);
    context.setStatistics(createAllSuccessMock());
    context.runValidation();
    return context;
  }

  private EvaluationContext createdSomeFailuresEvaluationContext(String name) {
    EvaluationContext context = new EvaluationContext(name, "unittest o'clock");
    context.loadConfiguration(perfTestAnnotationMock);
    context.loadRequirements(perfTestRequirementAnnotationMock);
    context.setStatistics(createSomeFailuresMock());
    context.runValidation();
    return context;
  }

  private Statistics createAllSuccessMock() {
    Statistics statisticsMock = mock(Statistics.class);
    when(statisticsMock.getErrorPercentage()).thenReturn(20.0F);
    when(statisticsMock.getLatencyPercentile(98, NANOSECONDS)).thenReturn(1636367F);
    when(statisticsMock.getLatencyPercentile(99, NANOSECONDS)).thenReturn(28343467F);
    when(statisticsMock.getLatencyPercentile(100, NANOSECONDS)).thenReturn(38548467F);
    when(statisticsMock.getLatencyPercentile(98, MILLISECONDS)).thenReturn(1636367F / 1_000_000);
    when(statisticsMock.getLatencyPercentile(99, MILLISECONDS)).thenReturn(28343467F / 1_000_000);
    when(statisticsMock.getLatencyPercentile(100, MILLISECONDS)).thenReturn(38548467F / 1_000_000);
    when(statisticsMock.getEvaluationCount()).thenReturn(130_000L);
    when(statisticsMock.getErrorCount()).thenReturn(26_000L);
    when(statisticsMock.getMaxLatency(NANOSECONDS)).thenReturn(38548467F);
    when(statisticsMock.getMinLatency(NANOSECONDS)).thenReturn(1636367F);
    when(statisticsMock.getMeanLatency(NANOSECONDS)).thenReturn(17540000F);
    when(statisticsMock.getMaxLatency(MILLISECONDS)).thenReturn(38548467F / 1_000_000);
    when(statisticsMock.getMinLatency(MILLISECONDS)).thenReturn(17540000F / 1_000_000);
    when(statisticsMock.getMeanLatency(MILLISECONDS)).thenReturn(28343467F / 1_000_000);
    return statisticsMock;
  }

  private Statistics createSomeFailuresMock() {
    Statistics statisticsMock = mock(Statistics.class);
    when(statisticsMock.getErrorPercentage()).thenReturn(60.0F);
    when(statisticsMock.getLatencyPercentile(98, NANOSECONDS)).thenReturn(1636367F);
    when(statisticsMock.getLatencyPercentile(99, NANOSECONDS)).thenReturn(28343467F);
    when(statisticsMock.getLatencyPercentile(100, NANOSECONDS)).thenReturn(98548467F);
    when(statisticsMock.getLatencyPercentile(98, MILLISECONDS)).thenReturn(1636367F / 1_000_000);
    when(statisticsMock.getLatencyPercentile(99, MILLISECONDS)).thenReturn(28343467F / 1_000_000);
    when(statisticsMock.getLatencyPercentile(100, MILLISECONDS)).thenReturn(98548467F / 1_000_000);
    when(statisticsMock.getEvaluationCount()).thenReturn(130_000L);
    when(statisticsMock.getErrorCount()).thenReturn(78_000L);
    when(statisticsMock.getMaxLatency(NANOSECONDS)).thenReturn(38548467F);
    when(statisticsMock.getMinLatency(NANOSECONDS)).thenReturn(1636367F);
    when(statisticsMock.getMeanLatency(NANOSECONDS)).thenReturn(17540000F);
    when(statisticsMock.getMaxLatency(MILLISECONDS)).thenReturn(38548467F / 1_000_000);
    when(statisticsMock.getMinLatency(MILLISECONDS)).thenReturn(17540000F / 1_000_000);
    when(statisticsMock.getMeanLatency(MILLISECONDS)).thenReturn(28343467F / 1_000_000);
    return statisticsMock;
  }

  private Statistics createAllFailureMock() {
    Statistics statisticsMock = mock(Statistics.class);
    when(statisticsMock.getErrorPercentage()).thenReturn(40.0F);
    when(statisticsMock.getLatencyPercentile(98, NANOSECONDS)).thenReturn(4636367F);
    when(statisticsMock.getLatencyPercentile(99, NANOSECONDS)).thenReturn(48343467F);
    when(statisticsMock.getLatencyPercentile(100, NANOSECONDS)).thenReturn(58548467F);
    when(statisticsMock.getLatencyPercentile(98, MILLISECONDS)).thenReturn(4636367F / 1_000_000);
    when(statisticsMock.getLatencyPercentile(99, MILLISECONDS)).thenReturn(48343467F / 1_000_000);
    when(statisticsMock.getLatencyPercentile(100, MILLISECONDS)).thenReturn(58548467F / 1_000_000);
    when(statisticsMock.getEvaluationCount()).thenReturn(1000L);
    when(statisticsMock.getErrorCount()).thenReturn(400L);
    when(statisticsMock.getMaxLatency(NANOSECONDS)).thenReturn(100002F);
    when(statisticsMock.getMinLatency(NANOSECONDS)).thenReturn(500000F);
    when(statisticsMock.getMeanLatency(NANOSECONDS)).thenReturn(600000F);
    when(statisticsMock.getMaxLatency(MILLISECONDS)).thenReturn(100002F / 1_000_000);
    when(statisticsMock.getMinLatency(MILLISECONDS)).thenReturn(500000F / 1_000_000);
    when(statisticsMock.getMeanLatency(MILLISECONDS)).thenReturn(600000F / 1_000_000);
    return statisticsMock;
  }

  private void initialisePerfTestAnnotationMock() {
    when(perfTestAnnotationMock.duration()).thenReturn(10_000);
    when(perfTestAnnotationMock.warmUp()).thenReturn(100);
    when(perfTestAnnotationMock.threads()).thenReturn(50);
    when(perfTestAnnotationMock.rateLimit()).thenReturn(11_000);
  }

  private void initialisePerfTestRequirementAnnotationMock() {
    when(perfTestRequirementAnnotationMock.percentiles()).thenReturn("98:3.3,99:32.6,100:46.9999");
    when(perfTestRequirementAnnotationMock.allowedErrorsRate()).thenReturn(0.3F);
    when(perfTestRequirementAnnotationMock.throughput()).thenReturn(13_000);
  }

  @SuppressWarnings("unused")
  private void saveContentToFile(String outputFile, File file) throws IOException {
    // Utility method to save updated report files to resources directory
    try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "utf-8"))) {
      writer.write(new String(Files.readAllBytes(file.toPath())));
    }
  }

}
