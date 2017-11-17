package com.github.noconnor.junitperf.reporting.providers;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.reporting.ReportGenerator;
import com.google.common.base.Joiner;

import static java.lang.System.getProperty;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
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

    try (BufferedWriter writer = newBufferedWriter()) {

      String header = buildHeader();

      writer.write(header);
      writer.newLine();
      testContexts.forEach(context -> {

        String record = String.format("%s,%d,%d,%d,%.4f,%.4f,%.4f,%s",
          context.getTestName(),
          context.getConfiguredDuration(),
          context.getConfiguredThreads(),
          context.getThroughputQps(),
          context.getStatistics().getMinLatency(MILLISECONDS),
          context.getStatistics().getMaxLatency(MILLISECONDS),
          context.getStatistics().getMeanLatency(MILLISECONDS),
          Joiner.on(",").skipNulls().join(generateFormattedPercentileData(context)));
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

  @Override
  public String getReportPath() {
    return reportPath;
  }

  private List<String> generateFormattedPercentileData(final EvaluationContext context) {
    return IntStream.range(1, 101).mapToObj(i -> {
      return String.format("%.4f", context.getStatistics().getLatencyPercentile(i, MILLISECONDS));
    }).collect(toList());
  }

  private BufferedWriter newBufferedWriter() throws UnsupportedEncodingException, FileNotFoundException {
    return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(reportPath), "utf-8"));
  }

  private String buildHeader() {
    String header = "testName,duration,threadCount,throughput,minLatencyNs,maxLatencyNs,meanLatencyNs,<PERCENTILES>";
    List<String> percentiles = IntStream.range(1, 101).mapToObj(CsvReportGenerator::ordinal).collect(toList());
    header = header.replace("<PERCENTILES>", Joiner.on(",").join(percentiles));
    return header;
  }

  // https://stackoverflow.com/questions/6810336/is-there-a-way-in-java-to-convert-an-integer-to-its-ordinal
  private static String ordinal(int i) {
    int mod100 = i % 100;
    int mod10 = i % 10;
    if (mod10 == 1 && mod100 != 11) {
      return i + "st";
    } else if (mod10 == 2 && mod100 != 12) {
      return i + "nd";
    } else if (mod10 == 3 && mod100 != 13) {
      return i + "rd";
    } else {
      return i + "th";
    }
  }
}
