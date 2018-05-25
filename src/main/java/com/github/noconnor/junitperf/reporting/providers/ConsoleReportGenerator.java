package com.github.noconnor.junitperf.reporting.providers;

import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.reporting.ReportGenerator;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;

import static com.github.noconnor.junitperf.reporting.utils.FormatterUtils.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Slf4j
public class ConsoleReportGenerator implements ReportGenerator {

  private static final String PASSED = "PASSED";
  private static final String FAILED = "FAILED!!";

  @Override
  public void generateReport(Set<EvaluationContext> testContexts) {
    testContexts.forEach(context -> {
      StatisticsCalculator statistics = context.getStatistics();
      String throughputStatus = context.isThroughputAchieved() ? PASSED : FAILED;
      String errorRateStatus = context.isErrorThresholdAchieved() ? PASSED : FAILED;

      log.info("Test Name:    {}", context.getTestName());
      log.info("Started at:   {}", context.getStartTime());
      log.info("Invocations:  {}", statistics.getEvaluationCount());
      log.info("  - Success:  {}", statistics.getEvaluationCount() - statistics.getErrorCount());
      log.info("  - Errors:   {}", statistics.getErrorCount());
      log.info("  - Errors:   {}% - {}", statistics.getErrorPercentage(), errorRateStatus);
      log.info("");
      log.info("Thread Count: {}", context.getConfiguredThreads());
      log.info("Warm up:      {} ms", context.getConfiguredWarmUp());
      log.info("");
      log.info("Execution time: {} ms", context.getConfiguredDuration());
      log.info("Throughput:     {}/s (Required: {}/s) - {}",
        context.getThroughputQps(),
        context.getRequiredThroughput(),
        throughputStatus);
      log.info("Min. latency:   {} ms (Required: {}ms) - {}",
        statistics.getMinLatency(MILLISECONDS),
        format(context.getRequiredMinLatency()));
      log.info("Max. latency:    {} ms (Required: {}ms) - {}",
        statistics.getMaxLatency(MILLISECONDS),
        format(context.getRequiredMaxLatency()));
      log.info("Ave. latency:    {} ms (Required: {}ms) - {}",
        statistics.getMeanLatency(MILLISECONDS),
        format(context.getRequiredMeanLatency()));
      context.getRequiredPercentiles().forEach((percentile, threshold) -> {
        String percentileStatus = context.getPercentileResults().get(percentile) ? PASSED : FAILED;
        log.info("{}:    {}ms (Required: {} ms) - {}",
          percentile,
          statistics.getLatencyPercentile(percentile, MILLISECONDS),
          format(threshold),
          percentileStatus);
      });
      log.info("");
      log.info("");
    });
  }

  @Override
  public String getReportPath() {
    return null;
  }

}
