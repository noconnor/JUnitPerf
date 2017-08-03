# JUnitPerf

API performance testing framework built using JUnit

JUnitPerf provides an extension to the JUnit4 framework, allowing simple unittests to be extended to operation as 
performance evaluation tests. 

Using a custom [Junit Rule](https://github.com/junit-team/junit4/wiki/Rules) and by 
applying custom java annotations to a basic unittests, it is possible to execute this annotated test multiple times 
while gathering statistical information. 

Statistical information includes:
* execution latency distributions, 
* test throughput (executions per second)
* error percentage (exceptional test executions as a percentage of successful test executions)

It also possible to apply threshold requirements to the gathered stats using the `@JUnitPerfRequirement(...)` annotation

In addition to failing a unittest is thresholds are not meet, the framework will also generate a HTML report 
containing a statistical analysis of the test run.


This library interface was heavily influenced by the interface in the deprecated 
[Contiperf library](https://github.com/lucaspouzac/contiperf) developed by [Lucas Pouzac](https://github.com/lucaspouzac)

## Usage instructions

* Add the JUnitPerf Rule to your test class

`public JunitPerfRule perfTestRule = new JunitPerfRule();`

* Add test setup annotation to your performance test 

```
@Test
@JUnitPerfTest(threads = 50, duration = 125_000, warmUp = 80_000, rampUp = 500, rateLimit = 11_000)
public void whenExecuting11Kqps_thenApiShouldNotCrash(){
  ...
}
``` 

* Optionally add performance test requirements annotation to your performance test. These requirements will be 
applied to the statistics gathered during the performance test. If thresholds are not met, test will fail


```
@Test
@JUnitPerfTest(threads = 50, duration = 125_000, warmUp = 80_000, rampUp = 500, rateLimit = 11_000)
@JUnitPerfRequirement(percentiles = "90:7,95:7,98:7,99:8", throughput = 10_000, allowedErrorsRate = 0.018)
public void whenExecuting11Kqps_thenApiShouldNotCrash(){
  ...
}
``` 


## IDE settings

**Intellij 14**

To run/add to this project using intellij you will require the following plugins:

* [Lombok](https://plugins.jetbrains.com/plugin/6317)
* CodeStyle Formatter
<br />
To configure your IntelliJ settings to use this formatter:
    * IntelliJ IDEA > Preferences > Editor > Code Style > Scheme > Project (Apply Settings)

To resolve issues with lombok annotations not being compiled during a module make try setting teh following preference:

* Go to the preferences (settings) menu
* Search for the "Compiler" section in the dialog window and then go to the "Annotation Processors" subsection
* Tick the checkbox reading "Enable annotation processing"

<br />
