package com.github.noconnor.junitperf.statements;

import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import com.google.common.util.concurrent.RateLimiter;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

import static com.github.noconnor.junitperf.statements.ExceptionsRegistry.reThrowIfAbort;
import static java.lang.System.nanoTime;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

@Slf4j
final class EvaluationTask implements Runnable {

    private final TestStatement statement;
    private final RateLimiter rateLimiter;
    private final Supplier<Boolean> terminator;
    private final StatisticsCalculator stats;
    private final long warmUpPeriodNs;
    private final long executionTarget;

    @Builder
    EvaluationTask(TestStatement statement,
                   RateLimiter rateLimiter,
                   StatisticsCalculator stats,
                   Supplier<Boolean> terminator,
                   int warmUpPeriodMs,
                   int executionTarget) {
        this(statement, rateLimiter, terminator, stats, warmUpPeriodMs, executionTarget);
    }

    // Test only
    EvaluationTask(TestStatement statement,
                   RateLimiter rateLimiter,
                   Supplier<Boolean> terminator,
                   StatisticsCalculator stats,
                   int warmUpPeriodMs,
                   int executionTarget) {
        this.statement = statement;
        this.rateLimiter = rateLimiter;
        this.terminator = terminator;
        this.stats = stats;
        this.warmUpPeriodNs = NANOSECONDS.convert(Math.max(warmUpPeriodMs, 0), MILLISECONDS);
        this.executionTarget = executionTarget;
    }

    @SneakyThrows
    @Override
    public void run() {
        long startTimeNs = nanoTime();
        long startMeasurements = startTimeNs + warmUpPeriodNs;
        while (terminationFlagNotSet() && threadNotInterrupted() && executionTargetNotMet()) {
            waitForPermit();
            evaluateStatement(startMeasurements);
        }
    }

    private boolean terminationFlagNotSet() {
        return !terminator.get();
    }

    private static boolean threadNotInterrupted() {
        return !Thread.currentThread().isInterrupted();
    }

    private boolean executionTargetNotMet() {
        return executionTarget <= 0 || stats.getEvaluationCount() < executionTarget;
    }

    private void evaluateStatement(long startMeasurements) throws Throwable {
        if (nanoTime() < startMeasurements) {
            try {
                statement.runBefores();
                statement.evaluate();
                statement.runAfters();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Throwable throwable) {
                log.trace("Warmup error", throwable);
            }
        } else {

            try {
                statement.runBefores();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Throwable throwable) {
                log.trace("Setup error", throwable);
                reThrowIfAbort(throwable);
                if (isTerminalException(throwable)) {
                    throw new IllegalStateException("Before method failed", throwable);
                }
            }

            long startTimeNs = nanoTime();
            try {
                statement.evaluate();
                stats.addLatencyMeasurement(nanoTime() - startTimeNs);
                stats.incrementEvaluationCount();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Throwable throwable) {
                log.trace("Execution error", throwable);
                reThrowIfAbort(throwable);
                checkForIgnorable(throwable);
                stats.addLatencyMeasurement(nanoTime() - startTimeNs);
            }

            try {
                statement.runAfters();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Throwable throwable) {
                log.trace("Teardown error", throwable);
                reThrowIfAbort(throwable);
                if (isTerminalException(throwable)) {
                    throw new IllegalStateException("After method failed", throwable);
                }
            }

        }
    }

    private void checkForIgnorable(Throwable throwable) {
        if (isIgnorableException(throwable)) {
            stats.incrementEvaluationCount();
        } else {
            stats.incrementEvaluationCount();
            stats.incrementErrorCount();
        }
    }

    private boolean isTerminalException(Throwable throwable) {
        return !isIgnorableException(throwable);
    }

    private boolean isIgnorableException(Throwable throwable) {
        return isIgnorable(throwable);
    }

    private boolean isIgnorable(Throwable throwable) {
        if (isNull(throwable)) {
            return false;
        }
        return ExceptionsRegistry.isIgnorable(throwable) || isIgnorable(throwable.getCause());
    }

    private void waitForPermit() {
        if (nonNull(rateLimiter)) {
            rateLimiter.acquire();
        }
    }

}
