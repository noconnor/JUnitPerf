package com.github.noconnor.junitperf.examples;

import com.github.noconnor.junitperf.JUnitPerfReportingConfig;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestActiveConfig;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.github.noconnor.junitperf.examples.existing.TestClassOne;
import com.github.noconnor.junitperf.examples.existing.TestClassTwo;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import static com.github.noconnor.junitperf.examples.utils.ReportingUtils.newHtmlReporter;


//
// To run suite: mvn -Dtest=ExampleTestSuiteUsage -DskipTests=false test
//

@Suite
//@SelectPackages({
//        "com.github.noconnor.junitperf.examples.existing"
//})
@SelectClasses({
        TestClassOne.class,
        TestClassTwo.class
})
// ConfigurationParameter: Required to enable Test Suite Interceptor Reference: https://www.baeldung.com/junit-5-extensions#1-automatic-extension-registration
@ConfigurationParameter(key = "junit.jupiter.extensions.autodetection.enabled", value = "true")
@JUnitPerfTest(totalExecutions = 100)
@JUnitPerfTestRequirement(allowedErrorPercentage = 0.01F)
public class ExampleTestSuiteUsage {

    @JUnitPerfTestActiveConfig
    public static JUnitPerfReportingConfig config = JUnitPerfReportingConfig.builder()
            .reportGenerator(newHtmlReporter("suite_reporter.html"))
            .build();

}
