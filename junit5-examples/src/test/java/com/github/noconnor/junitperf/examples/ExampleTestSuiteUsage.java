package com.github.noconnor.junitperf.examples;

import com.github.noconnor.junitperf.examples.engine.JunitPerfSuite;
import com.github.noconnor.junitperf.examples.existing.TestClassOne;
import com.github.noconnor.junitperf.examples.existing.TestClassTwo;
import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;
import com.github.noconnor.junitperf.statistics.providers.DescriptiveStatisticsCalculator;
import org.junit.platform.commons.annotation.Testable;
import org.junit.platform.suite.api.SelectClasses;

import static com.github.noconnor.junitperf.examples.utils.ReportingUtils.newHtmlReporter;


// 
// Reference: https://www.baeldung.com/junit-5-extensions#1-automatic-extension-registration
// Required: resources/META-INF/services/org.junit.jupiter.api.extension.Extension
// Required: vm arg: -Djunit.jupiter.extensions.autodetection.enabled=true
// Required: vm arg: -DskipTests=false
// Example mvn command: mvn -Djunit.jupiter.extensions.autodetection.enabled=true -Dtest=ExampleTestSuiteUsage -DskipTests=false test
//

@Testable
public class ExampleTestSuiteUsage {

    @JunitPerfSuite
    @SelectClasses({
            TestClassOne.class,
            TestClassTwo.class
    })
    public static class ExampleTestSuite1 {
    }

    @JunitPerfSuite
    @SelectClasses({
            TestClassOne.class,
    })
    public static class ExampleTestSuite2 {
    }
}
