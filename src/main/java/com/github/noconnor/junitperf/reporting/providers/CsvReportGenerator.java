package com.github.noconnor.junitperf.reporting.providers;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.reporting.ReportGenerator;
import com.google.common.base.Joiner;

import static java.lang.System.getProperty;
import static java.util.stream.Collectors.toList;

@Slf4j
public class CsvReportGenerator implements ReportGenerator {

  private static final String DEFAULT_REPORT_PATH = getProperty("user.dir") + "/build/reports/junitperf_report.csv";

  private final String reportPath;

  public CsvReportGenerator() {this(DEFAULT_REPORT_PATH);}

  @SuppressWarnings("WeakerAccess")
  public CsvReportGenerator(String reportPath) {this.reportPath = reportPath;}

  @Override
  public void generateReport(final Set<EvaluationContext> testContexts) {

    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(reportPath),
      "utf-8"))) {
      writer.write("testName,duration,threadCount,throughput,minLatencyNs,maxLatencyNs,meanLatencyNs,percentileData");
      writer.newLine();
      testContexts.forEach(context -> {

        List<String> percentileData = IntStream.range(1, 100).mapToObj(i -> {
          float latency = context.getStatistics().getLatencyPercentile(i, TimeUnit.NANOSECONDS);
          return String.format("%d:%.1f", i, latency);
        }).collect(toList());

        String record = String.format("%s,%d,%d,%d,%.1f,%.1f,%.1f,%s",
          context.getTestName(),
          context.getConfiguredDuration(),
          context.getConfiguredThreads(),
          context.getThroughputQps(),
          context.getStatistics().getMinLatency(TimeUnit.NANOSECONDS),
          context.getStatistics().getMaxLatency(TimeUnit.NANOSECONDS),
          context.getStatistics().getMeanLatency(TimeUnit.NANOSECONDS),
          Joiner.on(";").skipNulls().join(percentileData));
        try {
          writer.write(record);
          writer.newLine();
        } catch (IOException e) {
          log.error("Unable to write record {}", record);
        }
      });
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }

  }
}
