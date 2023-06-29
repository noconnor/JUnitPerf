package com.github.noconnor.junitperf.reporting.providers;

import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.LinkedHashSet;
import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.reporting.ReportGenerator;

import static com.github.noconnor.junitperf.reporting.utils.FormatterUtils.format;

@Slf4j
public class ConsoleReportGenerator implements ReportGenerator {

  private static final String PASSED = "PASSED";
  private static final String FAILED = "FAILED!!";


  @Override
  public void generateReport(LinkedHashSet<EvaluationContext> testContexts) {
    // Only output the last context
    final Iterator<EvaluationContext> itr = testContexts.iterator();
    EvaluationContext context = itr.next();
    while (itr.hasNext()) {
      context = itr.next();
    }
    updateReport(context);
  }

  public void updateReport(EvaluationContext context) {
    if (context.isAborted()) {
      log.info("Test {} was SKIPPED", context.getTestName());
    } else {

      String throughputStatus = context.isThroughputAchieved() ? PASSED : FAILED;
      String errorRateStatus = context.isErrorThresholdAchieved() ? PASSED : FAILED;

      log.info("Test Name:    {}", context.getTestName());
      log.info("Started at:   {}", context.getStartTime());
      log.info("Invocations:  {}", context.getEvaluationCount());
      log.info("  - Success:  {}", context.getEvaluationCount() - context.getErrorCount());
      log.info("  - Errors:   {}", context.getErrorCount());
      log.info("  - Errors:   {}% - {}", context.getErrorPercentage(), errorRateStatus);
      log.info("");
      log.info("Thread Count: {}", context.getConfiguredThreads());
      log.info("Warm up:      {} ms", context.getConfiguredWarmUp());
      log.info("Ramp up:      {} ms", context.getConfiguredRampUpPeriodMs());
      log.info("");
      log.info("Execution time: {}", context.getTestDurationFormatted());
      log.info("Throughput:     {}/s (Required: {}/s) - {}",
              context.getThroughputQps(),
              context.getRequiredThroughput(),
              throughputStatus);
      log.info("Min. latency:   {} ms (Required: {}ms) - {}",
              context.getMinLatencyMs(),
              format(context.getRequiredMinLatency()));
      log.info("Max. latency:    {} ms (Required: {}ms) - {}",
              context.getMaxLatencyMs(),
              format(context.getRequiredMaxLatency()));
      log.info("Ave. latency:    {} ms (Required: {}ms) - {}",
              context.getMeanLatencyMs(),
              format(context.getRequiredMeanLatency()));
      context.getRequiredPercentiles().forEach((percentile, threshold) -> {
        String percentileStatus = context.getPercentileResults().get(percentile) ? PASSED : FAILED;
        log.info("{}:    {}ms (Required: {} ms) - {}",
                percentile,
                context.getLatencyPercentileMs(percentile),
                format(threshold),
                percentileStatus);
      });
      log.info("");
      log.info("");
    }
  }

  @Override
  public String getReportPath() {
    return null;
  }

}
