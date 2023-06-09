package com.github.noconnor.junitperf.examples.engine;

import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver;
import org.junit.platform.engine.support.discovery.SelectorResolver;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.EngineDiscoveryOrchestrator;
import org.junit.platform.launcher.core.EngineExecutionOrchestrator;
import org.junit.platform.launcher.core.LauncherDiscoveryResult;
import org.junit.platform.launcher.core.ServiceLoaderTestEngineRegistry;
import org.junit.platform.suite.commons.SuiteLauncherDiscoveryRequestBuilder;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;
import static org.junit.platform.engine.support.discovery.SelectorResolver.Resolution.unresolved;
import static org.junit.platform.suite.commons.SuiteLauncherDiscoveryRequestBuilder.request;

public class JunitPerfTestEngine implements TestEngine {

    public JunitPerfTestEngine() {
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


    public void execute(ExecutionRequest request) {
        // Called to execute the whole test suite
        JunitPerfEngineDescriptor suiteEngineDescriptor = (JunitPerfEngineDescriptor) request.getRootTestDescriptor();
        EngineExecutionListener engineExecutionListener = request.getEngineExecutionListener();

        engineExecutionListener.executionStarted(suiteEngineDescriptor);
        suiteEngineDescriptor
                .getChildren()
                .stream()
                .map(JunitPerfTestDescriptor.class::cast)
                .forEach(suiteTestDescriptor -> {
                    
                    System.out.println("Running suite in a loop...");
                    for(int i=0; i < 1000; i++) {
                        suiteTestDescriptor.execute(engineExecutionListener);
                    }
                    System.out.println("Suite loop complete");
                });
        engineExecutionListener.executionFinished(suiteEngineDescriptor, TestExecutionResult.successful());
    }

    public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
        JunitPerfEngineDescriptor engineDescriptor = new JunitPerfEngineDescriptor(uniqueId);
        return new DiscoverySelectorResolver().resolveSelectors(discoveryRequest, engineDescriptor);
    }


    public static final class JunitPerfEngineDescriptor extends EngineDescriptor {
        JunitPerfEngineDescriptor(UniqueId uniqueId) {
            super(uniqueId, "JUnit Perf Suite");
        }

        public TestDescriptor.Type getType() {
            return Type.CONTAINER;
        }
    }

    public static class JunitPerfSuiteLauncher {
        private final EngineExecutionOrchestrator executionOrchestrator = new EngineExecutionOrchestrator();
        private final EngineDiscoveryOrchestrator discoveryOrchestrator;

        public static JunitPerfSuiteLauncher create() {
            Set<TestEngine> engines = new LinkedHashSet<>();
            new ServiceLoaderTestEngineRegistry().loadTestEngines().forEach(engines::add);
            return new JunitPerfSuiteLauncher(engines);
        }

        public JunitPerfSuiteLauncher(Set<TestEngine> testEngines) {
            this.discoveryOrchestrator = new EngineDiscoveryOrchestrator(testEngines, emptyList());
        }

        public LauncherDiscoveryResult discover(LauncherDiscoveryRequest discoveryRequest, UniqueId parentId) {
            return discoveryOrchestrator.discover(discoveryRequest, EngineDiscoveryOrchestrator.Phase.DISCOVERY, parentId);
        }

        public void execute(LauncherDiscoveryResult discoveryResult, EngineExecutionListener listener) {
            executionOrchestrator.execute(discoveryResult, listener);
        }

    }

    public static final class JunitPerfTestDescriptor extends AbstractTestDescriptor {

        private final ConfigurationParameters configurationParameters;
        private final SuiteLauncherDiscoveryRequestBuilder discoveryRequestBuilder = request();
        private JunitPerfSuiteLauncher launcher;
        private LauncherDiscoveryResult launcherDiscoveryResult;

        public JunitPerfTestDescriptor(UniqueId id, Class<?> suiteClass, ConfigurationParameters configurationParameters) {
            super(id, suiteClass.getSimpleName(), ClassSource.from(suiteClass));
            this.configurationParameters = configurationParameters;
        }

        JunitPerfTestDescriptor addDiscoveryRequestFrom(Class<?> suiteClass) {
            discoveryRequestBuilder.suite(suiteClass);
            return this;
        }

        JunitPerfTestDescriptor addDiscoveryRequestFrom(UniqueId uniqueId) {
            discoveryRequestBuilder.selectors(DiscoverySelectors.selectUniqueId(uniqueId));
            return this;
        }

        public void discover() {
            if (launcherDiscoveryResult != null) {
                return;
            }

            LauncherDiscoveryRequest request = discoveryRequestBuilder
                    .filterStandardClassNamePatterns(true)
                    .enableImplicitConfigurationParameters(false)
                    .parentConfigurationParameters(configurationParameters)
                    .build();

            this.launcher = JunitPerfSuiteLauncher.create();
            this.launcherDiscoveryResult = launcher.discover(request, getUniqueId());
            this.launcherDiscoveryResult.getTestEngines()
                    .stream()
                    .map(testEngine -> launcherDiscoveryResult.getEngineTestDescriptor(testEngine))
                    .forEach(this::addChild);
        }

        public void execute(EngineExecutionListener listener) {
            listener.executionStarted(this);
            launcher.execute(launcherDiscoveryResult, listener);
            listener.executionFinished(this, TestExecutionResult.successful());
        }

        @Override
        public Type getType() {
            return Type.CONTAINER;
        }
    }

    static final class JunitPerfSelectorResolver implements SelectorResolver {

        private final Predicate<String> classNameFilter;
        private final EngineDescriptor suiteEngineDescriptor;
        private final ConfigurationParameters configurationParameters;

        JunitPerfSelectorResolver(Predicate<String> classNameFilter,
                                  EngineDescriptor suiteEngineDescriptor,
                                  ConfigurationParameters configurationParameters) {
            this.classNameFilter = classNameFilter;
            this.suiteEngineDescriptor = suiteEngineDescriptor;
            this.configurationParameters = configurationParameters;
        }

        @Override
        public Resolution resolve(ClassSelector selector, Context context) {
            Class<?> testClass = selector.getJavaClass();
            if (AnnotationSupport.isAnnotated(testClass, JunitPerfSuite.class)) {
                if (classNameFilter.test(testClass.getName())) {
                    // @formatter:off
                    Optional<JunitPerfTestDescriptor> suiteWithDiscoveryRequest = context
                            .addToParent(parent -> newSuiteDescriptor(testClass, parent))
                            .map(suite -> suite.addDiscoveryRequestFrom(testClass));
                    return toResolution(suiteWithDiscoveryRequest);
                    // @formatter:on
                }
            }
            return unresolved();
        }

        @Override
        public Resolution resolve(UniqueIdSelector selector, Context context) {
            UniqueId uniqueId = selector.getUniqueId();
            UniqueId engineId = suiteEngineDescriptor.getUniqueId();
            List<UniqueId.Segment> resolvedSegments = engineId.getSegments();
            
            System.out.println("Resolved segments:" + resolvedSegments);
            
            return uniqueId.getSegments()
                    .stream()
                    .skip(resolvedSegments.size())
                    .findFirst()
                    .filter(suiteSegment -> "suite".equals(suiteSegment.getType()))
                    .flatMap(JunitPerfSelectorResolver::tryLoadSuiteClass)
                    .filter(clazz -> AnnotationSupport.isAnnotated(clazz, JunitPerfSuite.class))
                    .map(suiteClass -> context
                            .addToParent(parent -> newSuiteDescriptor(suiteClass, parent))
                            .map(suite -> uniqueId.equals(suite.getUniqueId())
                                    // The uniqueId selector either targeted a class annotated with @JunitPerfSuite;
                                    ? suite.addDiscoveryRequestFrom(suiteClass)
                                    // or a specific test in that suite
                                    : suite.addDiscoveryRequestFrom(uniqueId)))
                    .map(JunitPerfSelectorResolver::toResolution)
                    .orElseGet(Resolution::unresolved);
            
        }

        private static Optional<Class<?>> tryLoadSuiteClass(UniqueId.Segment segment) {
            return ReflectionUtils.tryToLoadClass(segment.getValue()).toOptional();
        }

        private static Resolution toResolution(Optional<JunitPerfTestDescriptor> suite) {
            return suite.map(Match::exact).map(Resolution::match).orElseGet(Resolution::unresolved);
        }

        private Optional<JunitPerfTestDescriptor> newSuiteDescriptor(Class<?> suiteClass, TestDescriptor parent) {
            UniqueId id = parent.getUniqueId().append("suite", suiteClass.getName());
            return Optional.of(new JunitPerfTestDescriptor(id, suiteClass, configurationParameters));
        }
    }

    static class DiscoverySelectorResolver {

        private final EngineDiscoveryRequestResolver<EngineDescriptor> resolver = EngineDiscoveryRequestResolver.<EngineDescriptor>builder()
                .addClassContainerSelectorResolver(classCandidate -> AnnotationSupport.isAnnotated(classCandidate, JunitPerfSuite.class))
                .addSelectorResolver(context -> new JunitPerfSelectorResolver(
                        context.getClassNameFilter(),
                        context.getEngineDescriptor(),
                        context.getDiscoveryRequest().getConfigurationParameters()
                )).build();

        public void discoverSuites(EngineDescriptor engineDescriptor) {
            engineDescriptor.getChildren()
                    .stream()
                    .map(JunitPerfTestDescriptor.class::cast)
                    .forEach(JunitPerfTestDescriptor::discover);
        }

        public EngineDescriptor resolveSelectors(EngineDiscoveryRequest request, EngineDescriptor engineDescriptor) {
            resolver.resolve(request, engineDescriptor);
            discoverSuites(engineDescriptor);
            engineDescriptor.accept(TestDescriptor::prune);
            return engineDescriptor;
        }
    }

}
