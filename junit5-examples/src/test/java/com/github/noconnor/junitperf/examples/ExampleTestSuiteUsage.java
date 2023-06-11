package com.github.noconnor.junitperf.examples;

import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.github.noconnor.junitperf.examples.existing.TestClassOne;
import com.github.noconnor.junitperf.examples.existing.TestClassTwo;
import com.github.noconnor.junitperf.suite.JUnitPerfSuite;
import com.github.noconnor.junitperf.suite.JUnitPerfTestSetup;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.SelectClasses;
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
// Suite level Perf annotations 
// * JUnitPerfTest & JUnitPerfTestRequirement -> will be applied to ALL tests in SelectClasses
@JUnitPerfTest(totalExecutions = 1_00)
@JUnitPerfTestRequirement(allowedErrorPercentage = 0.01F)
// ConfigurationParameter: 
// * Enables extensions listed is resources/META-INF/services/org.junit.jupiter.api.extension.Extension for ALL tests
// * Reference: https://www.baeldung.com/junit-5-extensions#1-automatic-extension-registration
@ConfigurationParameter(key = "junit.jupiter.extensions.autodetection.enabled", value = "true")
public class ExampleTestSuiteUsage {
    
    @JUnitPerfTestSetup // must be a static public method
    static void suiteSetup() {
        JUnitPerfSuite.registerPerfTestSuite(
                ExampleTestSuiteUsage.class,
                singletonList(newHtmlReporter("suite_reporter.html"))
        );
    }

}
