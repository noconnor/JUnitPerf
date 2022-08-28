Back to [index](../README.md) page.


## Junit4 Usage Instructions

[Install Instructions](#install-instructions)

[Synchronous Usage](#junit4-synchronous-usage)

[Asynchronous Usage](#junit4-asynchronous-usage)

[Reporter Binding Examples](#reporter-binding-examples)

[Overriding Statistic Capturing](#overriding-statistic-capturing)

<br />

### Install Instructions

`JUnitPerf` is available in [maven central](https://search.maven.org/artifact/com.github.noconnor/junitperf/)

```
<dependency>
  <groupId>com.github.noconnor</groupId>
  <artifactId>junitperf</artifactId>
  <version>VERSION</version>
</dependency>
```

For example test code, see [junit4 sample module](../junit4-examples)


### Junit4 Synchronous Usage

This section contains details for usage of the `JUnitPerf` library in *synchronous* mode.
To see example test cases browse to the [junit4-examples/src/test/examples/](junit4-examples/src/test/java/com/github/noconnor/junitperf/examples) folder.

Add the JUnitPerf Rule to your test class

```
@Rule
public JUnitPerfRule perfTestRule = new JUnitPerfRule();
```

Next add the `JUnitPerfTest` annotation to the unit test you would like to convert into a performance test

```
@Test
@JUnitPerfTest(threads = 50, durationMs = 125_000, rampUpPeriodMs = 2_000, warmUpMs = 10_000, maxExecutionsPerSecond = 11_000)
public void whenExecuting11Kqps_thenApiShouldNotCrash(){
  ... EXECUTE TIME SENSITIVE TASK ...
}
``` 

In the example above, the unittest `whenExecuting11Kqps_thenApiShouldNotCrash` will be executed in a loop for
125 secs (125,000ms) using 50 threads.

The executions will be rate limited to 11K loop executions per second. The execution rate will ramp up smoothly to 11K
over a period of 2 seconds (rampUpPeriodMs).

No statistical data will be captured during the warm up period (10 seconds - 10,000ms)

Optionally add the performance test requirement annotation (`JUnitPerfTestRequirement`).
The specified requirements will be applied to the statistics gathered during the performance test execution.
If thresholds are not met, test will fail.


```
@Test
@JUnitPerfTest(threads = 50, durationMs = 125_000, warmUpMs = 10_000, maxExecutionsPerSecond = 11_000)
@JUnitPerfTestRequirement(percentiles = "90:7,95:7,98:7,99:8", executionsPerSec = 10_000, allowedErrorPercentage = 0.10)
public void whenExecuting11Kqps_thenApiShouldNotCrash(){
  ... EXECUTE TIME SENSITIVE TASK ...
}
``` 

In the example above, the `JUnitPerfTestRequirement` annotation will apply a number of threshold constraints to the performance test.

The tests calculated throughput (executions per second) will be compared to the `executionsPerSec` requirement.
If the test throughput is *less* than the target throughput then the test will fail.

This example test also contains a requirement that the execution error rate be no more than 10% (`allowedErrorPercentage = 0.10`).
An error is an uncaught exception thrown during unittest execution.
If the specified `allowedErrorPercentage` is not met then the test will fail.

Finally, the example sets a number of latency thresholds on the 90th, 95th, 98th and 99th percentiles (i.e. if the
99th percentile latency is *greater* than 8ms then the test will fail).
The latency is a measurement of the time taken to execute one loop (not including statistics measurement calculations)

More information on statistic calculations can be found [here](../README.md#statistics)

**NOTE:** By default statistic calculations (including latency measurements) include any methods marked as `@Before` or `@After`.
To exclude methods annotated with `@Before` or `@After`, create the JunitPerfRule as follows:

```
@Rule
public JUnitPerfRule perfTestRule = new JUnitPerfRule(true);
```

<br />

### Junit4 Asynchronous Usage

This section contains details for usage of the `JUnitPerf` library in *asynchronous* mode.
To see example test cases browse to the [ExampleAsyncTests.java](junit4-examples/src/test/java/com/github/noconnor/junitperf/examples/ExampleAsyncTests.java).

Add the async JUnitPerf Rule to your test class

```
@Rule
public JUnitPerfAsyncRule rule = new JUnitPerfAsyncRule();
```

Next add the `JUnitPerfTest` annotation to the unit test you would like to convert into a performance test

```
@Test
@JUnitPerfTest(durationMs = 125_000, rampUpPeriodMs = 2_000, warmUpMs = 10_000, maxExecutionsPerSecond = 1000)
public void whenExecuting1Kqps_thenApiShouldNotCrash(){
   TestContext context = rule.newContext();
   threadPool.submit( () -> {
      ... EXECUTE ASYNC TASK ...
      ... THEN NOTIFY FRAMEWORK OF SUCCESS/FAILURE...
      context.success();
      // OR
      context.Fail();
  }
}
``` 

In the example above, the unittest `whenExecuting1Kqps_thenApiShouldNotCrash` will be executed in a loop for
125 secs (125,000ms).

Async tasks will be rate limited to 1,000 task submissions per second. The execution rate will ramp up smoothly to 1K
over a period of 2 seconds (rampUpPeriodMs).

The `TestContext` instance is used to capture latency and error stats during the duration of the test. A timer is started when
`rule.newContext()` is called and the timer is stopped when either `context.success()` or `context.fail()` is called.

No statistical data will be captured during the warm up period (10 seconds - 10,000ms)

**NOTE: It is highly recommended to set a maxExecutionsPerSecond when running Async tests to prevent flooding the async client code with task submissions**


Optionally add the performance test requirement annotation (`JUnitPerfTestRequirement`).
The specified requirements will be applied to the statistics gathered during the performance test execution.
If thresholds are not met, test will fail.


```
@Test
@JUnitPerfTest(durationMs = 125_000, warmUpMs = 10_000, maxExecutionsPerSecond = 1000)
@JUnitPerfTestRequirement(percentiles = "90:7,95:7,98:7,99:8", executionsPerSec = 1000, allowedErrorPercentage = 0.10)
public void whenExecuting1Kqps_thenApiShouldNotCrash(){
   TestContext context = rule.newContext();
   threadPool.submit( () -> {
      ... EXECUTE ASYNC TASK ...
      ... THEN NOTIFY FRAMEWORK OF SUCCESS/FAILURE...
      context.success();
      // OR
      context.Fail();
  }
}
``` 


In the example above, the `JUnitPerfTestRequirement` annotation will apply a number of threshold constraints to the performance test.

The tests calculated throughput (executions per second) will be compared to the `executionsPerSec` requirement.
If the test throughput is *less* than the target throughput then the test will fail.

This example test also contains a requirement that the execution error rate be no more than 10% (`allowedErrorPercentage = 0.10`).
An error is an uncaught exception thrown during unittest execution.
If the specified `allowedErrorPercentage` is not met then the test will fail.

Finally the example sets a number of latency thresholds on the 90th, 95th, 98th and 99th percentiles (i.e. if the
99th percentile latency is *greater* than 8ms then the test will fail).
The latency is a measurement of the time taken to execute one loop (not including statistics measurement calculations)

More information on statistic calculations can be found [here](../README.md#statistics)


### Reporter Binding Examples

By default, the JUnitPerf junit4 library will generate an HTML performance report under `${BUILD_DIR}/reports/junitperf_report.html`

**Html reporting** using `JUnitPerfRule` can be configured as follows
```
@Rule
public JUnitPerfRule perfTestRule = new JUnitPerfRule(new HtmlReportGenerator("/some/custom/path/report.html"));
```

To change from the default HTML reporter to the **console reporter**
just create an instance of the `ConsoleReportGenerator` class and pass a reference to this instance to the `JUnitPerfRule` constructor, for example:

```
@Rule
public JUnitPerfRule perfTestRule = new JUnitPerfRule(new ConsoleReportGenerator());
```

To change from the default HTML reporter to the **CSV reporter**
just create an instance of the `CsvReportGenerator` class and pass a reference to this instance to the `JUnitPerfRule` constructor,
for example:

```
@Rule
public JUnitPerfRule perfTestRule = new JUnitPerfRule(new CsvReportGenerator());
```

The reporter will generate a CSV file at the default location `${BUILD_DIR}/reports/junitperf_report.csv`.
It is possible to change this default location by constructing the `CsvReportGenerator` as follows:

```
@Rule
public JUnitPerfRule perfTestRule = new JUnitPerfRule(new CsvReportGenerator("/some/custom/path/report.csv")));
```


If further customisation is required, a custom implementation of the `ReportGenerator` interface can be passed to 
the `JunitPerRule` constructor:

```
@Rule
public JUnitPerfRule perfTestRule = new JUnitPerfRule(new CustomReportGeneratorImpl());
```

It is also possible to set *more* than one reporter. This can be done at rule construction time:

```
@Rule
public JUnitPerfRule perfTestRule = new JUnitPerfRule(new CsvReportGenerator(), new HtmlReportGenerator());
```

With this configuration a HTML report **AND** a CSV report will be generated


<br />

It is possible to generate a report that groups the results of many test classes into one report.
This is helpful if you want to generate for example just one report for each project or if you want to group them for topics or something similar.

In order to do this, a single reporter can be created and shared across JUnitPerfRule instances. For example:

```
public class ReportingUtils {
    public static final HtmlReportGenerator htmlReporter = new HtmlReportGenerator("src/test/resources/reports/performance_report.html");
}

public class SomeTestClass {

    @Rule 
    public JUnitPerfRule perfTestRule = new JUnitPerfRule(ReportingUtils.htmlReporter);
   
    // ... some test code ...
}

public class SomeOtherTestClass {

    @Rule 
    public JUnitPerfRule perfTestRule = new JUnitPerfRule(ReportingUtils.htmlReporter);
   
    // ... some other test code ...

}
```

With this configuration it is possible to aggregate the results into a single report.

<br />

### Overriding Statistic Capturing

To override the default statistics calculation class, a custom implementation of the `StatisticsCalculator` interface can
be passed to the `JUnitPerfRule` constructor:

```
@Rule
public JUnitPerfRule perfTestRule = new JUnitPerfRule(new CustomStatisticsCalculatorImpl());
``` 
