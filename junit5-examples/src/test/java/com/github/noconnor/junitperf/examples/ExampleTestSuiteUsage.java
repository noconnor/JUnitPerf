package com.github.noconnor.junitperf.examples;

import com.github.noconnor.junitperf.JUnitPerfReportingConfig;
import com.github.noconnor.junitperf.JUnitPerfRule;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestActiveConfig;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.github.noconnor.junitperf.examples.existing.JUnit4Tests;
import com.github.noconnor.junitperf.examples.existing.TestClassOne;
import com.github.noconnor.junitperf.examples.existing.TestClassTwo;
import com.github.noconnor.junitperf.reporting.ReportGenerator;
import org.junit.Ignore;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.github.noconnor.junitperf.examples.utils.ReportingUtils.newHtmlReporter;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;


//
// To run suite: mvn -Dtest=ExampleTestSuiteUsage -DskipTests=false test
//

@Suite
@SelectClasses({
        TestClassOne.class,
        TestClassTwo.class,
        JUnit4Tests.class
})
// ConfigurationParameter: Required to enable Test Suite Interceptor Reference: https://www.baeldung.com/junit-5-extensions#1-automatic-extension-registration
@ConfigurationParameter(key = "junit.jupiter.extensions.autodetection.enabled", value = "true")
@JUnitPerfTest(totalExecutions = 100)
@JUnitPerfTestRequirement(allowedErrorPercentage = 0.01F)
@RunWith(ExampleTestSuiteUsage.MySuite.class)
public class ExampleTestSuiteUsage {

    private static final ReportGenerator reporter = newHtmlReporter("suite.html");

    @JUnitPerfTestActiveConfig
    public static JUnitPerfReportingConfig config = JUnitPerfReportingConfig.builder()
            .reportGenerator(reporter)
            .build();

    
    
    
    

    public static class MySuite extends org.junit.runners.Suite {
        // copied from Suite
        private static Class<?>[] getAnnotatedClasses(Class<?> klass) throws InitializationError {
            SelectClasses annotation = klass.getAnnotation(SelectClasses.class);
            if (annotation == null) {
                throw new InitializationError(String.format("class '%s' must have a SuiteClasses annotation", klass.getName()));
            }
            return annotation.value();
        }

        private static JUnitPerfReportingConfig getConfig(Class<?> klass) throws InitializationError {
            if (isNull(klass)) {
                return null;
            }
            for (Field field : klass.getDeclaredFields()) {
                if (field.isAnnotationPresent(JUnitPerfTestActiveConfig.class)) {
                    field.setAccessible(true);
                    try {
                        return (JUnitPerfReportingConfig) field.get(null);
                    } catch (IllegalAccessException e) {
                        throw new InitializationError(e);
                    }
                }
            }
            return null;
        }

        // copied from Suite
        public MySuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
            super(null, getRunners(
                    getAnnotatedClasses(klass),
                    klass.getAnnotation(JUnitPerfTest.class),
                    klass.getAnnotation(JUnitPerfTestRequirement.class),
                    getConfig(klass))
            );
        }

        public static List<Runner> getRunners(Class<?>[] classes,
                                              JUnitPerfTest perfTest,
                                              JUnitPerfTestRequirement requirements,
                                              JUnitPerfReportingConfig config
        ) {
            List<Runner> runners = new LinkedList<Runner>();

            for (Class<?> klazz : classes) {
                try {
                    runners.add(new Junit4Runner(klazz, perfTest, requirements, config));
                } catch (InitializationError e) {
                    // skip
                }
            }

            return runners;
        }
    }

    public static class Junit4Runner extends BlockJUnit4ClassRunner {

        private final JUnitPerfTest perfTest;
        private final JUnitPerfTestRequirement requirements;

        private final JUnitPerfReportingConfig config;

        public Junit4Runner(Class<?> klass,
                            JUnitPerfTest perfTest,
                            JUnitPerfTestRequirement requirements,
                            JUnitPerfReportingConfig config
        ) throws InitializationError {
            super(klass);
            this.perfTest = perfTest;
            this.requirements = requirements;
            this.config = nonNull(config) ? config : JUnitPerfReportingConfig.builder().build();
        }

        @Override
        protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
            Description description = describeChild(method);
            if (method.getAnnotation(Ignore.class) != null) {
                notifier.fireTestIgnored(description);
            } else {
                JUnitPerfRule rule = new JUnitPerfRule(
                        config.getStatisticsCalculatorSupplier().get(),
                        config.getReportGenerators().toArray(new ReportGenerator[0])
                );
                rule.setDefaultPerfTestAnnotation(perfTest);
                rule.setDefaultRequirementsAnnotation(requirements);
                RunRules runRules = new RunRules(methodBlock(method), Arrays.asList(new TestRule[]{rule}), description);
                runLeaf(runRules, description, notifier);
            }
        }
    }
}
