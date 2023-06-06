package com.github.noconnor.junitperf.examples;

import com.github.noconnor.junitperf.JUnitPerfInterceptor;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;
import com.github.noconnor.junitperf.statistics.providers.DescriptiveStatisticsCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import java.lang.annotation.Annotation;
import java.util.Collections;

import static com.github.noconnor.junitperf.examples.utils.ReportingUtils.newHtmlReporter;

@Suite
@SelectClasses({
        ExampleTestSuiteUsage.TestClassOne.class,
        ExampleTestSuiteUsage.TestClassTwo.class
})
public class ExampleTestSuiteUsage {
    
    private static final HtmlReportGenerator REPORTER = newHtmlReporter("suite_reporter.html");
    private static final DescriptiveStatisticsCalculator statisticsCalculator = new DescriptiveStatisticsCalculator();
    
    public static class JUnitPerfInterceptorSuite extends JUnitPerfInterceptor {

        @Override
        public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
            // This method will be called once before every test
            activeReporters = Collections.singletonList(REPORTER);
            activeStatisticsCalculator = statisticsCalculator;
            
            // Targets
            defaultRequirementsAnnotation = new JUnitPerfTestRequirement() {
                @Override
                public Class<? extends Annotation> annotationType() { return JUnitPerfTestRequirement.class; }
                @Override
                public String percentiles() { return ""; }
                @Override
                public int executionsPerSec() { return 100; }
                @Override
                public float allowedErrorPercentage() { return 0.1F; }
                @Override
                public float minLatency() { return -1; }
                @Override
                public float maxLatency() { return -1; }
                @Override
                public float meanLatency() { return -1; }
            };
            // Test set up
            defaultPerfTestAnnotation = new JUnitPerfTest() {
                @Override
                public Class<? extends Annotation> annotationType() { return JUnitPerfTest.class; }
                @Override
                public int threads() { return 1; }
                @Override
                public int durationMs() { return 3_000; }
                @Override
                public int warmUpMs() { return 0; }
                @Override
                public int maxExecutionsPerSecond() { return 1000; }
                @Override
                public int rampUpPeriodMs() { return 0; }
            };
            super.postProcessTestInstance(testInstance, context);
        }
        
    }
    
    
    public static class TestClassOne {
        @Test
        public void sample_test1_class1() throws InterruptedException {
            Thread.sleep(5);
        }
        
        @Test
        public void sample_test2_class1() throws InterruptedException {
            // Mock some processing logic
            Thread.sleep(1);
        }

    }
    
    public static class TestClassTwo {

        @Test
        public void sample_test1_class2() throws InterruptedException {
            // Mock some processing logic
            Thread.sleep(1);
        }
    }

    public static class NotPartOfSuite {
        @Test
        public void shouldNotRun() throws InterruptedException {
            // Mock some processing logic
            Thread.sleep(100);
        }
    }

}
