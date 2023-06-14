package com.github.noconnor.junitperf.suite;

import com.github.noconnor.junitperf.JUnitPerfTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.suite.api.Suite;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.platform.commons.util.ReflectionUtils.findMethods;

public class JunitPerfTestEngine implements TestEngine {

    @Override
    public String getId() {
        return "junitperf-platform-suite";
    }

    @Override
    public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
        
        // Identify test suites
        Set<Class<?>> testSuites = discoveryRequest.getSelectorsByType(ClassSelector.class)
                .stream()
                .map(ClassSelector::getJavaClass)
                .filter(JunitPerfTestEngine::isTestSuite)
                .collect(Collectors.toSet());

        // Call @BeforeAll for all test suite classes to allow suites to programmatically initialise test registry
        testSuites.stream()
                .map(clazz -> findMethods(clazz, isBeforeAllAnnotatedMethod()))
                .flatMap(Collection::stream)
                .forEach(JunitPerfTestEngine::invokeMethod);

        // Check is test suite is annotated with @JUnitPerfTest or @JUnitPerfTestRequirement and add to test registry
        testSuites.stream()
                .filter(clazz -> clazz.isAnnotationPresent(JUnitPerfTest.class))
                .forEach(JUnitPerfTestRegistryUtils::registerSuite);

        return new EngineDescriptor(uniqueId, "junitperf");
    }

    @Override
    public void execute(ExecutionRequest request) {
        // Do nothing, delegate to junit-platform-suite-engine
    }

    private static void invokeMethod(Method method) {
        try {
            method.setAccessible(true);
            method.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException("Unable to invoke: " + method.getName(), e);
        }
    }
    
    private static Predicate<Method> isBeforeAllAnnotatedMethod() {
        return method -> method.isAnnotationPresent(BeforeAll.class);
    }

    private static boolean isTestSuite(Class<?> clazz) {
        return clazz.isAnnotationPresent(Suite.class);
    }
}