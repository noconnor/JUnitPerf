# JUnitPerf [![Build Status](https://travis-ci.org/noconnor/JUnitPerf.svg?branch=master)](https://travis-ci.org/noconnor/JUnitPerf) [![codecov](https://codecov.io/gh/noconnor/JUnitPerf/branch/master/graph/badge.svg)](https://codecov.io/gh/noconnor/JUnitPerf) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.noconnor/junitperf/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.noconnor/junitperf)


API performance testing framework built using JUnit

JUnitPerf provides an extension to the JUnit4 framework, allowing unittests to be extended to operate as 
performance evaluation tests. 

This library is best suited for testing remote API endpoints or component/integration testing. 
If attempting to benchmark code blocks with nanosecond latency then you should consider using [JMH](http://openjdk.java.net/projects/code-tools/jmh/)   

Using a custom [Junit Rule](https://github.com/junit-team/junit4/wiki/Rules) and by 
applying custom java annotations to a basic unittests, it is possible to execute this annotated test multiple times 
while gathering statistical information. 

This library interface was heavily influenced by the interface in the deprecated 
[Contiperf library](https://github.com/lucaspouzac/contiperf) developed by [Lucas Pouzac](https://github.com/lucaspouzac)

## Contents

[Install Instructions](#install-instructions)

[Usage Instructions](#usage-instructions)

[Reports](#reports)

[Statistics](#statistics)

[Build Instructions](#build-instructions)

## Install Instructions 

`JUnitPerf` is available in [maven central](http://bit.ly/2idQDvA)

To add the latest version of `JunitPerf` to your gradle project add the following line to your build.gradle files dependencies:

`compile 'com.github.noconnor:junitperf:+'`


## Usage Instructions

This section contains usage details for the `JUnitPerf` library. To see example test cases browse to the [src/test/examples/](src/test/examples/) folder. 

Add the JUnitPerf Rule to your test class

```
@Rule
public JUnitPerfRule perfTestRule = new JUnitPerfRule();
```

Next add the `JUnitPerfTest` annotation to the unit test you would like to convert into a performance test  

```
@Test
@JUnitPerfTest(threads = 50, durationMs = 125_000, warmUpMs = 10_000, maxExecutionsPerSecond = 11_000)
public void whenExecuting11Kqps_thenApiShouldNotCrash(){
  ... EXECUTE TIME SENSITIVE TASK ...
}
``` 

In the example above, the unittest `whenExecuting11Kqps_thenApiShouldNotCrash` will be executed in a loop for 
125 secs (125,000ms) using 50 threads. 

The executions will be rate limited to 11K loop executions per second. 

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

Finally the example sets a number of latency thresholds on the 90th, 95th, 98th and 99th percentiles (i.e. if the 
99th percentile latency is *greater* than 8ms then the test will fail). 
The latency is a measurement of the time taken to execute one loop (not including statistics measurement calculations) 

More information on statistic calculations can be found [here](#statistics)

`@JUnitPerfTest` has the following configuration parameters:

| Property               | Definition                                                                                                                        | Default value  |
| ---------------------: |:---------------------------------------------------------------------------------------------------------------------------------:| --------------:|
| threads                | The total number of threads to use during test execution                                                                          |        1       |
| durationMs             | Total time to run the test in millisecs (ms) (includes warmup period)                                                             |      60,000    |
| warmUpMs               | Warm up period in ms, test logic will be executed during warm up, but results will not be considered during statistics evaluation |        0       |
| maxExecutionsPerSecond | Sets the maximum number of iteration per second (disabled by default)                                                             |       -1       |

<br />

`@JUnitPerfTestRequirement` has the following configuration parameters:

| Property               | Definition                                                                                                                  | Default value  |
| ----------------------:|:---------------------------------------------------------------------------------------------------------------------------:| --------------:|
| percentiles            | Comma separated list of percentile targets, format: percentile1:limit,percentile2:limit (ie. 90:3.3,99:6.8)                 |        ""      |
| executionsPerSec       | Target executions per second                                                                                                |        1       |
| allowedErrorPercentage | Allowed % of errors (uncaught exceptions) during test execution (value between 0 and 1, where 1 = 100% errors allowed)      |        1       |


## Reports

[HTML Reports](#html-reports)

[Console Reporting](#console-reporting)

[CSV Reporting](#csv-reporting)

[Custom Reporting](#custom-reporting)

[Multiple Reports](#multiple-reports)


#### HTML Reports

By default, the JUnitPerf library will generate a HTML performance report under `${BUILD_DIR}/reports/junitperf_report.html`

An example report can be seen below

![HTML Report](https://raw.githubusercontent.com/noconnor/JUnitPerf/master/src/common/images/example_report.png "Example JUnitPerf html report")

Hovering over the datapoints on the percentile latency graph will provide latency/percentile information. 
It is possible to override the default output path by constructing the `JUnitPerfRule` in the following way:
```
@Rule
public JUnitPerfRule perfTestRule = new JUnitPerfRule(new HtmlReportGenerator("/some/custom/path/report.html"));
```

HTML reports are generated using the [jtwig library](http://jtwig.org/). The jtwig report template can be found under `src/main/resources/templates/report.twig`.
It is possible to override this template by placing a customised `templates/report.twig` file on the classpath ahead of the default template.


#### Console Reporting

It is also possible to use one of the other built-in reporters, the console reporter. To change from the default HTML reporter to the console reporter
just create an instance of the `ConsoleReportGenerator` class and pass a reference to this instance to the `JUnitPerfRule` constructor, 
for example: 

```
@Rule
public JUnitPerfRule perfTestRule = new JUnitPerfRule(new ConsoleReportGenerator());
```

Example output:

```
15:55:06.575 [main] INFO  c.g.n.j.r.p.ConsoleReportGenerator - Started at:   2017-10-28 15:55:05
15:55:06.580 [main] INFO  c.g.n.j.r.p.ConsoleReportGenerator - Invocations:  765
15:55:06.580 [main] INFO  c.g.n.j.r.p.ConsoleReportGenerator -   - Success:  765
15:55:06.580 [main] INFO  c.g.n.j.r.p.ConsoleReportGenerator -   - Errors:   0
15:55:06.580 [main] INFO  c.g.n.j.r.p.ConsoleReportGenerator -   - Errors:   0.0% - PASSED
15:55:06.581 [main] INFO  c.g.n.j.r.p.ConsoleReportGenerator - 
15:55:06.581 [main] INFO  c.g.n.j.r.p.ConsoleReportGenerator - Thread Count: 1
15:55:06.581 [main] INFO  c.g.n.j.r.p.ConsoleReportGenerator - Warm up:      0ms
15:55:06.581 [main] INFO  c.g.n.j.r.p.ConsoleReportGenerator - 
15:55:06.581 [main] INFO  c.g.n.j.r.p.ConsoleReportGenerator - Execution time: 1000ms
15:55:06.581 [main] INFO  c.g.n.j.r.p.ConsoleReportGenerator - Throughput:     766/s (Required: 10000/s) - FAILED!!
15:55:06.581 [main] INFO  c.g.n.j.r.p.ConsoleReportGenerator - Min. latency:   1.012392ms
15:55:06.582 [main] INFO  c.g.n.j.r.p.ConsoleReportGenerator - Max latency:    3.74209ms
15:55:06.582 [main] INFO  c.g.n.j.r.p.ConsoleReportGenerator - Ave latency:    1.2975845ms
15:55:06.583 [main] INFO  c.g.n.j.r.p.ConsoleReportGenerator - 
```

#### CSV Reporting

It is also possible to use the built-in CSV reporter. 

To change from the default HTML reporter to the CSV reporter
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

The CSV output will have the following format:

```
testName,duration,threadCount,throughput,minLatencyNs,maxLatencyNs,meanLatencyNs,percentileData
unittest1,10000,50,101,500000.0,100002.0,600000.0,1:0.0;2:0.0;3:0.0;4:0.0;5:0.0; ... ;98:4636367.0;99:48343468.0
```

NOTE: the percentileData is formatted as ```percentile1:latency;percentile2:latency; ...```



#### Custom Reporting

If further customisation is required, a custom implementation of the `ReportGenerator` interface can be passed to the the `JunitPerRule` constructor:

```
@Rule
public JUnitPerfRule perfTestRule = new JUnitPerfRule(new CustomReportGeneratorImpl());
```

#### Multiple Reports

It is possible to set *more* than one reporter. This can be done at rule construction time:

```
@Rule
public JUnitPerfRule perfTestRule = new JUnitPerfRule(new CsvReportGenerator(), new HtmlReportGenerator());
```

With this configuration a HTML report **AND** a CSV report will be generated




## Statistics

By default, statistics are captured and calculated using the apache [Descriptive Statistics library](http://commons.apache.org/proper/commons-math/userguide/stat.html#a1.2_Descriptive_statistics).
See [DescriptiveStatisticsCalculator](src/main/java/com/github/noconnor/junitperf/statistics/providers/DescriptiveStatisticsCalculator.java) for more details.

To override the default statistics calculation class, a custom implementation of the `StatisticsCalculator` interface can 
be passed to the `JUnitPerfRule` constructor:

```
@Rule
public JUnitPerfRule perfTestRule = new JUnitPerfRule(new CustomStatisticsCalculatorImpl());
``` 


## Build Instructions

To compile this project and run tests execute the following command from the root project directory: `./gradlew clean test`

To generate a library jar execute: `./gradlew clean assemble -x sign` or `./gradlew clean jar` 

**Intellij 14 Setup**

To run/add to this project using intellij you will require the following plugins:

* [Lombok](https://plugins.jetbrains.com/plugin/6317)
* CodeStyle Formatter
<br />
To configure your IntelliJ settings to use this formatter:
    * IntelliJ IDEA > Preferences > Editor > Code Style > Scheme > Project (Apply Settings)

To resolve issues with lombok annotations not being compiled during a module make try setting the following preference:

* Go to the preferences (settings) menu
* Search for the "Compiler" section in the dialog window and then go to the "Annotation Processors" subsection
* Tick the checkbox reading "Enable annotation processing"

<br />

