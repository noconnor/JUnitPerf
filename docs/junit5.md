Back to [index](../README.md) page.


## Junit5 Usage Instructions

[Install Instructions](#install-instructions)

[Synchronous Usage](#junit5-synchronous-usage)

[Asynchronous Usage](#junit5-asynchronous-usage)

[Reporter Binding Examples](#reporter-binding-examples)

[Overriding Statistic Capturing](#overriding-statistic-capturing)

[Test Suites](#test-suite-configuration)

<br />

### Install Instructions

`JUnitPerf` is available in [maven central](https://search.maven.org/artifact/com.github.noconnor/junitperf-junit5/)

```
<dependency>
  <groupId>com.github.noconnor</groupId>
  <artifactId>junitperf-junit5</artifactId>
  <version>VERSION</version>
</dependency>
```

For example test code, see [junit5 sample module](../junit5-examples)

To run examples using maven:
* From project root directory run: `mvn clean install -Dgpg.skip`
* From `junit5-examples` directory run: `mvn clean test -DskipTests=false`

**NOTE:** The example tests contain some example failure scenarios, so you should expect `mvn test` command to fail when running these examples. 


### Junit5 Synchronous Usage

This section contains details for usage of the `JUnitPerf` library in *synchronous* mode.
To see example test cases browse to the [junit45-examples/src/test/examples/](junit5-examples/src/test/java/com/github/noconnor/junitperf/examples) folder.

Add the `JUnitPerfInterceptor` interceptor to your test class

```
@ExtendWith(JUnitPerfInterceptor.class)
public class SomeTestClass {

  @Test
  ...
  
  @Test
  ...

} 
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

No statistical data will be captured during the warm-up period (10 seconds - 10,000ms)

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

<br />

### Junit5 Asynchronous Usage

This section contains details for usage of the `JUnitPerf` library in *asynchronous* mode.
To see example test cases browse to the [ExampleAsyncTests.java](junit5-examples/src/test/java/com/github/noconnor/junitperf/examples/ExampleAsyncTests.java).

Add the `JUnitPerfInterceptor` interceptor to your test class

```
@ExtendWith(JUnitPerfInterceptor.class)
public class SomeTestClass {

  @Test
  ...
  
  @Test
  ...

} 
```

Next add the `JUnitPerfTest` annotation to the unit test you would like to convert into a performance test **and**
specify a test parameter of type `TestContext`

```
@Test
@JUnitPerfTest(durationMs = 125_000, rampUpPeriodMs = 2_000, warmUpMs = 10_000, maxExecutionsPerSecond = 1000)
public void whenExecuting1Kqps_thenApiShouldNotCrash(TestContextSupplier contextSupplier){
   TestContext context = supplier.startMeasurement();
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
test execution starts and the timer is stopped when either `context.success()` or `context.fail()` is called.

No statistical data will be captured during the warm-up period (10 seconds - 10,000ms in teh example configuration above)

**NOTE: It is highly recommended to set a maxExecutionsPerSecond when running Async tests to prevent flooding the async client code with task submissions**


Optionally add the performance test requirement annotation (`JUnitPerfTestRequirement`).
The specified requirements will be applied to the statistics gathered during the performance test execution.
If thresholds are not met, test will fail.


```
@Test
@JUnitPerfTest(durationMs = 125_000, warmUpMs = 10_000, maxExecutionsPerSecond = 1000)
@JUnitPerfTestRequirement(percentiles = "90:7,95:7,98:7,99:8", executionsPerSec = 1000, allowedErrorPercentage = 0.10)
public void whenExecuting1Kqps_thenApiShouldNotCrash(TestContextSupplier contextSupplier){
   // Starts the task timer
   TestContext context = supplier.startMeasurement();
   threadPool.submit( () -> {
      ... EXECUTE ASYNC TASK ...
      ... THEN NOTIFY FRAMEWORK OF SUCCESS/FAILURE...
      // Stops the task timer and marks task as a success or failure
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

Finally, the example sets a number of latency thresholds on the 90th, 95th, 98th and 99th percentiles (i.e. if the
99th percentile latency is *greater* than 8ms then the test will fail).
The latency is a measurement of the time taken to execute one loop (not including statistics measurement calculations)

More information on statistic calculations can be found [here](../README.md#statistics)


### Reporter Binding Examples

By default, the JUnitPerf junit5 library will use a console reporter.

Default test reporting configurations can be overridden by specifying an instance of `JUnitPerfReportingConfig` and 
annotating that instance with the marker annotation `@JUnitPerfTestActiveConfig` 

For example, to generate an **HTML report** , the following configuration override can be specified:
```
@JUnitPerfTestActiveConfig
private final static JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
        .reportGenerator(new HtmlReportGenerator(getProperty("user.dir") + "/build/reports/" + fileName))
        .build();
```

**NOTE:** the `JUnitPerfReportingConfig` must be a **static** field instance to prevent a new/different instance being created for each `@Test`
instance

To generate an **CSV report**
just create an instance of the `CsvReportGenerator` class and pass a reference to this instance to the `JUnitPerfReportingConfig` instance,
for example:

```
@JUnitPerfTestActiveConfig
private final static JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
        .reportGenerator(new CsvReportGenerator())
        .build();
```

The reporter will generate a CSV file at the default location `${BUILD_DIR}/reports/junitperf_report.csv`.
It is possible to change this default location by constructing the `CsvReportGenerator` as follows:

```
@JUnitPerfTestActiveConfig
private final static JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
        .reportGenerator(new CsvReportGenerator("/some/custom/path/report.csv"))
        .build();
```


If further customisation is required, a custom implementation of the `ReportGenerator` interface can be passed to
the `JUnitPerfReportingConfig` insatnce:

```
@JUnitPerfTestActiveConfig
private final static JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
        .reportGenerator(new CustomReportGeneratorImpl())
        .build();
```

It is also possible to set *more* than one reporter as follows:

```
@JUnitPerfTestActiveConfig
private final static JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
        .reportGenerator(new CsvReportGenerator())
        .reportGenerator(new HtmlReportGenerator())
        .build();
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

    @JUnitPerfTestActiveConfig
    private final static JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
            .reportGenerator(ReportingUtils.htmlReporter)
            .build();
   
    // ... some test code ...
}

public class SomeOtherTestClass {

    @JUnitPerfTestActiveConfig
    private final static JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
            .reportGenerator(ReportingUtils.htmlReporter)
            .build();
               
    // ... some other test code ...

}
```

With this configuration it is possible to aggregate the results into a single report output.

<br />


### Overriding Statistic Capturing

To override the default statistics calculation class, a custom implementation of the `StatisticsCalculator` interface can
be passed to the `JUnitPerfReportingConfig` instance:

```
@JUnitPerfTestActiveConfig
private final static JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
        .statisticsCalculatorSupplier(() -> new CustomStatisticsCalculator())
        .build();
``` 

For each `@Test` instance, the `statisticsCalculatorSupplier` will be called to generate a new `StatisticsCalculator` instance

<br />

### Test Suite Configuration

JUnitPerf annotations can also be applied at the suite level. <br />
When running in a test suite, annotation resolution follows the following precedence order:

* Test method level annotations will take precedence over all other annotation configurations
* If no method level annotations are available, class level annotations will be considered next
* If no class level annotations are available, an attempt will be made to identify suite level annotations (if the test is currently running as part of a suite)

If order to apply the JUnitPerfTestInterceptor to all tests in a suite, jupiter global extensions must be enabled. <br />
This can be done by:
* Adding a run time VM arg: `-Djunit.jupiter.extensions.autodetection.enabled=true`
* Specifying the extensions param as a ConfigurationParameter at the suite level: i.e. add this annotation to your suite `@ConfigurationParameter(key = "junit.jupiter.extensions.autodetection.enabled", value = "true")`

The `junit-platform-suite-engine` dependency must also be added to your project:
```
<dependency>
    <groupId>org.junit.platform</groupId>
    <artifactId>junit-platform-suite-engine</artifactId>
    <version>LATEST_VERSION</version>
    <scope>test</scope>
</dependency>
```


An example test case can be found in the [junit5-examples](../junit5-examples/src/test/java/com/github/noconnor/junitperf/examples/ExampleTestSuiteUsage.java) module

To run the example suite test you can use the following maven commands
* from inside the junitperf project root directory:  `mvn clean install -Dgpg.skip`
  * This will install all junitperf snapshot dependencies to your local maven repo 
* from inside the `junit5-examples` folder: `mvn -Dtest=ExampleTestSuiteUsage -DskipTests=false test`
  * Location of test reports will be printed to console (default location is the `junit5-examples/build/reports/` directory)
