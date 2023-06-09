package com.github.noconnor.junitperf.examples;

import com.github.noconnor.junitperf.examples.existing.TestClassOne;
import com.github.noconnor.junitperf.examples.existing.TestClassTwo;
import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;
import com.github.noconnor.junitperf.statistics.providers.DescriptiveStatisticsCalculator;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.suite.api.ExcludeEngines;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.engine.DiscoverySelectorResolver;
import org.junit.platform.suite.engine.SuiteEngineDescriptor;
import org.junit.platform.suite.engine.SuiteTestEngine;

import java.util.Optional;

import static com.github.noconnor.junitperf.examples.utils.ReportingUtils.newHtmlReporter;


// 
// Reference: https://www.baeldung.com/junit-5-extensions#1-automatic-extension-registration
// Required: resources/META-INF/services/org.junit.jupiter.api.extension.Extension
// Required: vm arg: -Djunit.jupiter.extensions.autodetection.enabled=true
// Required: vm arg: -DskipTests=false
// Example mvn command: mvn -Djunit.jupiter.extensions.autodetection.enabled=true -Dtest=ExampleTestSuiteUsage -DskipTests=false test
//
@ExcludeEngines( { "junit-platform-suite" })
@Suite
@SelectClasses({
        TestClassOne.class,
        TestClassTwo.class
})
public class ExampleTestSuiteUsage {

    private static final HtmlReportGenerator REPORTER = newHtmlReporter("suite_reporter.html");
    private static final DescriptiveStatisticsCalculator statisticsCalculator = new DescriptiveStatisticsCalculator();


//    public static void main(String[] args) {
//        // keep pulling test classes off the queue until its empty
//        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
//                .selectors(
//                        selectClass(TestClassOne.class.getName()),
//                        selectClass(TestClassTwo.class.getName())
//                ).build();
//
//        LauncherConfig launcherConfig = LauncherConfig.builder()
//                .enableTestEngineAutoRegistration(false)
//                .enableLauncherDiscoveryListenerAutoRegistration(true)
//                .enableLauncherSessionListenerAutoRegistration(true)
//                .enableTestExecutionListenerAutoRegistration(true)
//                .addTestEngines(new JunitPerfTestEngine())
//                .build();
//
//        SummaryGeneratingListener listener = new SummaryGeneratingListener();
//        try (LauncherSession session = LauncherFactory.openSession(launcherConfig)) {
//            Launcher launcher = session.getLauncher();
//            launcher.registerTestExecutionListeners(listener);
//            TestPlan testPlan = launcher.discover(request);
//            launcher.execute(testPlan);
//        }
//    }
    

    public static class JunitPerfTestEngine implements TestEngine {

        private final SuiteTestEngine suiteTestEngine;

        public JunitPerfTestEngine() {
            this.suiteTestEngine = new SuiteTestEngine();
        }

        public String getId() {
            return "junitperf-suite";
        }

        public Optional<String> getGroupId() {
            return Optional.of("com.github.noconnor.junitperf");
        }

        public Optional<String> getArtifactId() {
            return Optional.of("junitperf-suite-engine");
        }

        public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
            return suiteTestEngine.discover(discoveryRequest, uniqueId);
        }

        public void execute(ExecutionRequest request) {
            // Hook in here and run measurement code
            System.out.println(request);
            for (int i = 0; i < 2; i++) {
                System.out.println("Loop " + i + " starting...");
                suiteTestEngine.execute(request);
                System.out.println("Loop " + i + " done");
            }
        }
        
        
        
    }

}
