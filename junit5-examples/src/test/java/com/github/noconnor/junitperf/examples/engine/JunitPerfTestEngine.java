//package com.github.noconnor.junitperf.examples.engine;
//
//import org.junit.platform.commons.support.AnnotationSupport;
//import org.junit.platform.engine.EngineDiscoveryRequest;
//import org.junit.platform.engine.ExecutionRequest;
//import org.junit.platform.engine.TestDescriptor;
//import org.junit.platform.engine.TestEngine;
//import org.junit.platform.engine.UniqueId;
//import org.junit.platform.engine.support.descriptor.EngineDescriptor;
//import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver;
//import org.junit.platform.engine.support.discovery.SelectorResolver;
//import org.junit.platform.suite.engine.ClassSelectorResolver;
//import org.junit.platform.suite.engine.SuiteTestDescriptor;
//import org.junit.platform.suite.engine.SuiteTestEngine;
//
//import java.lang.reflect.Constructor;
//import java.util.Optional;
//
//public class JunitPerfTestEngine implements TestEngine {
//
//    private final SuiteTestEngine suiteTestEngine;
//
//    public JunitPerfTestEngine() {
//        this.suiteTestEngine = new SuiteTestEngine();
//    }
//
//    public String getId() {
//        return "junitperf-suite";
//    }
//
//    public Optional<String> getGroupId() {
//        return Optional.of("com.github.noconnor.junitperf");
//    }
//
//    public Optional<String> getArtifactId() {
//        return Optional.of("junitperf-suite-engine");
//    }
//
//
//    public void execute(ExecutionRequest request) {
//        // Hook in here and run measurement code
//        System.out.println(request);
//        for (int i = 0; i < 2; i++) {
//            System.out.println("Loop " + i + " starting...");
//            suiteTestEngine.execute(request);
//            System.out.println("Loop " + i + " done");
//        }
//    }
//
//    public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
//        System.out.println(discoveryRequest);
//        EngineDescriptor engineDescriptor = (EngineDescriptor) createPackagePrivateClass(
//                "org.junit.platform.suite.engine.SuiteEngineDescriptor", 
//                uniqueId
//        );
//        (new DiscoverySelectorResolver()).resolveSelectors(discoveryRequest, engineDescriptor);
//        return engineDescriptor;
//    }
//
//
//    class DiscoverySelectorResolver {
//
//        // @formatter:off
//        private final EngineDiscoveryRequestResolver<EngineDescriptor> resolver = EngineDiscoveryRequestResolver.<EngineDescriptor>builder()
//                .addClassContainerSelectorResolver(classCandidate -> AnnotationSupport.isAnnotated(classCandidate, JunitPerfSuite.class))
//                .addSelectorResolver(context -> (SelectorResolver) createPackagePrivateClass(
//                        "org.junit.platform.suite.engine.ClassSelectorResolver",
//                        context.getClassNameFilter(),
//                        context.getEngineDescriptor(),
//                        context.getDiscoveryRequest().getConfigurationParameters()))
//                .build();
//        // @formatter:on
//
//        private void discoverSuites(EngineDescriptor engineDescriptor) {
//            // @formatter:off
//            engineDescriptor.getChildren().stream()
//                    .map(SuiteTestDescriptor.class::cast)
//                    .forEach(SuiteTestDescriptor::discover);
//            // @formatter:on
//        }
//
//        void resolveSelectors(EngineDiscoveryRequest request, EngineDescriptor engineDescriptor) {
//            resolver.resolve(request, engineDescriptor);
//            discoverSuites(engineDescriptor);
//            engineDescriptor.accept(TestDescriptor::prune);
//        }
//    }
//
//    private static Object createPackagePrivateClass(String clazz, Object... args) {
//        try {
//            Class<?> c = Class.forName(clazz);
//            Constructor<?> constructor = c.getDeclaredConstructor();
//            constructor.setAccessible(true);
//            return constructor.newInstance(args);
//        } catch (Exception e) {
//            throw new IllegalStateException(e);
//        }
//    }
//
//}
