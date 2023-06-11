package com.github.noconnor.junitperf.examples;

import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.github.noconnor.junitperf.suite.JUnitPerfSuite;
import com.github.noconnor.junitperf.suite.JUnitPerfSuiteSetup;
import com.github.noconnor.junitperf.utils.JUnitPerfSuiteRequirements;
import com.github.noconnor.junitperf.utils.JUnitPerfSuiteTestSpec;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

import static com.github.noconnor.junitperf.examples.utils.ReportingUtils.newHtmlReporter;
import static java.util.Collections.singletonList;


//
// To run suite: mvn -Dtest=ExampleTestSuiteUsage -DskipTests=false test
//

@Suite
@SelectPackages({
        "com.github.noconnor.junitperf.examples.existing"
})
//@SelectClasses({
//        TestClassOne.class,
//        TestClassTwo.class
//})
// ConfigurationParameter: Required to enable Test Suite Interceptor Reference: https://www.baeldung.com/junit-5-extensions#1-automatic-extension-registration
@ConfigurationParameter(key = "junit.jupiter.extensions.autodetection.enabled", value = "true")
public class ExampleTestSuiteUsage {

    @JUnitPerfSuiteSetup // must be a static public method
    static void suiteSetup() {

        JUnitPerfTest spec = JUnitPerfSuiteTestSpec.builder()
                .totalExecutions(100) // Run *each* test in suite 100 times
                .durationMs(20_000)   // Run *each* test for a maximum duration of 10s (totalExecutions takes precedence over durationMs)
                .build();

        JUnitPerfTestRequirement requirements = JUnitPerfSuiteRequirements.builder()
                .allowedErrorPercentage(0.01F) // Allow 1% failure rate
                .build();
        
        JUnitPerfSuite.registerPerfTestSuite(
                ExampleTestSuiteUsage.class,
                spec,
                requirements,
                singletonList(newHtmlReporter("suite_reporter.html"))
        );
    }

}
