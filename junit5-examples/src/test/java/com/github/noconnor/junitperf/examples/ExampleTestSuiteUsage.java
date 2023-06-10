package com.github.noconnor.junitperf.examples;

import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.github.noconnor.junitperf.examples.existing.TestClassOne;
import com.github.noconnor.junitperf.examples.existing.TestClassTwo;
import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;
import com.github.noconnor.junitperf.suite.JunitPerfSuite;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import java.util.Collections;

import static com.github.noconnor.junitperf.examples.utils.ReportingUtils.newHtmlReporter;
import static java.util.Collections.singletonList;


//
// To run suite: mvn -Djunit.jupiter.extensions.autodetection.enabled=true -Dtest=ExampleTestSuiteUsage -DskipTests=false test
//

@Suite
@SelectClasses({
        TestClassOne.class,
        TestClassTwo.class
})
// Suite level Perf annotations 
// * JUnitPerfTest & JUnitPerfTestRequirement -> will be applied to ALL tests in SelectClasses
@JUnitPerfTest(durationMs = 1_000)
@JUnitPerfTestRequirement(allowedErrorPercentage = 0.01F)
// ConfigurationParameter: 
// * Enables extensions listed is resources/META-INF/services/org.junit.jupiter.api.extension.Extension for ALL tests
// * Reference: https://www.baeldung.com/junit-5-extensions#1-automatic-extension-registration
@ConfigurationParameter(key = "junit.jupiter.extensions.autodetection.enabled", value = "true")
public class ExampleTestSuiteUsage {
    
    //
    // Suite configuration must happen with @Test block
    // This test must run before other tests in the suite to initialise the suite
    //
    @Test
    void suiteSetup() {
        JunitPerfSuite.registerPerfTestSuite(
                ExampleTestSuiteUsage.class,
                singletonList(newHtmlReporter("suite_reporter.html"))
        );
    }
    
}
