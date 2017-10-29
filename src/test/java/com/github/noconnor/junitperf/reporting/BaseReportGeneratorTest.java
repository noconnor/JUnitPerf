package com.github.noconnor.junitperf.reporting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Set;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import com.github.noconnor.junitperf.BaseTest;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseReportGeneratorTest extends BaseTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  protected File reportFile;

  @Mock
  private JUnitPerfTest perfTestAnnotationMock;

  @Mock
  private JUnitPerfTestRequirement perfTestRequirementAnnotationMock;

  @SuppressWarnings("ConstantConditions")
  protected File getResourceFile(String fileName) {
    ClassLoader classLoader = getClass().getClassLoader();
    return new File(classLoader.getResource(fileName).getFile());
  }

  protected String readFileContents(final File file) throws IOException {
    return new String(Files.readAllBytes(file.toPath()), Charset.forName("utf-8")).replaceAll("\\s+", "");
  }

  protected void verifyAllValidationFailed(EvaluationContext context) {
    assertFalse(context.isErrorThresholdAchieved());
    assertFalse(context.isThroughputAchieved());
    assertTrue(context.getPercentileResults().values().stream().noneMatch(e -> e));
    assertFalse(context.isSuccessful());
  }

  protected void verifyAllValidationPassed(EvaluationContext context) {
    assertTrue(context.isErrorThresholdAchieved());
    assertTrue(context.isThroughputAchieved());
    assertFalse(context.getPercentileResults().values().stream().noneMatch(e -> e));
    assertTrue(context.isSuccessful());
  }

  protected EvaluationContext createdFailedEvaluationContext(String name) {
    EvaluationContext context = new EvaluationContext(name, "unittest o'clock");
    context.loadConfiguration(perfTestAnnotationMock);
    context.loadRequirements(perfTestRequirementAnnotationMock);
    context.setStatistics(createAllFailureMock());
    context.runValidation();
    return context;
  }

  protected EvaluationContext createdSuccessfulEvaluationContext(String name) {
    EvaluationContext context = new EvaluationContext(name, "unittest o'clock");
    context.loadConfiguration(perfTestAnnotationMock);
    context.loadRequirements(perfTestRequirementAnnotationMock);
    context.setStatistics(createAllSuccessMock());
    context.runValidation();
    return context;
  }

  protected EvaluationContext createdSomeFailuresEvaluationContext(String name) {
    EvaluationContext context = new EvaluationContext(name, "unittest o'clock");
    context.loadConfiguration(perfTestAnnotationMock);
    context.loadRequirements(perfTestRequirementAnnotationMock);
    context.setStatistics(createSomeFailuresMock());
    context.runValidation();
    return context;
  }

  private StatisticsCalculator createAllSuccessMock() {
    StatisticsCalculator statisticsMock = mock(StatisticsCalculator.class);
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

  private StatisticsCalculator createSomeFailuresMock() {
    StatisticsCalculator statisticsMock = mock(StatisticsCalculator.class);
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

  private StatisticsCalculator createAllFailureMock() {
    StatisticsCalculator statisticsMock = mock(StatisticsCalculator.class);
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

  protected void initialisePerfTestAnnotationMock() {
    when(perfTestAnnotationMock.duration()).thenReturn(10_000);
    when(perfTestAnnotationMock.warmUp()).thenReturn(100);
    when(perfTestAnnotationMock.threads()).thenReturn(50);
    when(perfTestAnnotationMock.rateLimit()).thenReturn(11_000);
  }

  protected void initialisePerfTestRequirementAnnotationMock() {
    when(perfTestRequirementAnnotationMock.percentiles()).thenReturn("98:3.3,99:32.6,100:46.9999");
    when(perfTestRequirementAnnotationMock.allowedErrorsRate()).thenReturn(0.3F);
    when(perfTestRequirementAnnotationMock.throughput()).thenReturn(13_000);
  }

  @SuppressWarnings("unused")
  protected void saveContentToFile(String outputFile, File file) throws IOException {
    // Utility method to save updated report files to resources directory
    try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "utf-8"))) {
      writer.write(new String(Files.readAllBytes(file.toPath())));
    }
  }

  protected Set<EvaluationContext> generateAllFailureOrderedContexts() {
    EvaluationContext context1 = createdFailedEvaluationContext("unittest1");
    EvaluationContext context2 = createdFailedEvaluationContext("unittest2");
    verifyAllValidationFailed(context1);
    verifyAllValidationFailed(context2);
    return newLinkedHashSet(newArrayList(context1, context2));
  }

  protected Set<EvaluationContext> generateAllPassedOrderedContexts() {
    EvaluationContext context1 = createdSuccessfulEvaluationContext("unittest1");
    EvaluationContext context2 = createdSuccessfulEvaluationContext("unittest2");
    verifyAllValidationPassed(context1);
    verifyAllValidationPassed(context2);
    return newLinkedHashSet(newArrayList(context1, context2));
  }

  protected Set<EvaluationContext> generateMixedOrderedContexts() {
    EvaluationContext context1 = createdFailedEvaluationContext("unittest1");
    EvaluationContext context2 = createdSuccessfulEvaluationContext("unittest2");
    verifyAllValidationFailed(context1);
    verifyAllValidationPassed(context2);
    return newLinkedHashSet(newArrayList(context1, context2));
  }

  protected Set<EvaluationContext> generateSomeFailuresContext() {
    EvaluationContext context = createdSomeFailuresEvaluationContext("unittest1");
    return newLinkedHashSet(newArrayList(context));
  }
}
