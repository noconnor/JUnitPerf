package com.github.noconnor.junitperf.suite;

import com.github.noconnor.junitperf.JUnitPerfInterceptor;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.reporting.ReportGenerator;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.suite.api.Suite;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.noconnor.junitperf.suite.JUnitPerfSuite.getSuiteJUnitPerfRequirements;
import static com.github.noconnor.junitperf.suite.JUnitPerfSuite.getSuiteJUnitPerfTestData;
import static java.util.Objects.isNull;
import static org.junit.platform.commons.util.ReflectionUtils.findAllClassesInClasspathRoot;
import static org.junit.platform.commons.util.ReflectionUtils.findMethods;
import static org.junit.platform.commons.util.ReflectionUtils.getAllClasspathRootDirectories;

public class JUnitPerfSuiteInterceptor extends JUnitPerfInterceptor {

    private static final AtomicBoolean isInitialised = new AtomicBoolean(); 
    
    @Override
    public synchronized void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        
        if (isInitialised.compareAndSet(false, true)) {
            // TODO: tidy this code up and move to utility class
            getAllClasspathRootDirectories().forEach(path -> {
                findAllClassesInClasspathRoot(
                        path.toUri(),
                        ClassFilter.of(clazz -> clazz.isAnnotationPresent(Suite.class))
                ).forEach(clazz -> {
                    findMethods(clazz, method -> method.isAnnotationPresent(JUnitPerfTestSetup.class))
                            .forEach(method -> {
                                if (!Modifier.isStatic(method.getModifiers())) {
                                    throw new IllegalStateException("JUnitPerfTestSetup method must be static: " + method.getName());
                                }
                                try {
                                    method.setAccessible(true);
                                    method.invoke(null);
                                } catch (Exception e) {
                                    throw new RuntimeException("Unable to invoke JUnitPerfTestSetup: " + method.getName(), e);
                                }
                            });
                });
            });
        }
        
        super.postProcessTestInstance(testInstance, context);
    }

    @Override
    protected JUnitPerfTest getJUnitPerfTestDetails(Method method) {
        JUnitPerfTest methodLevelAnnotation = super.getJUnitPerfTestDetails(method);
        JUnitPerfTest suiteLevelAnnotation = getSuiteJUnitPerfTestData(method.getDeclaringClass());
        return isNull(methodLevelAnnotation) ? suiteLevelAnnotation : methodLevelAnnotation;
    }

    @Override
    protected JUnitPerfTestRequirement getJUnitPerfTestRequirementDetails(Method method) {
        JUnitPerfTestRequirement methodLevelAnnotation = super.getJUnitPerfTestRequirementDetails(method);
        JUnitPerfTestRequirement suiteLevelAnnotation = getSuiteJUnitPerfRequirements(method.getDeclaringClass());
        return isNull(methodLevelAnnotation) ? suiteLevelAnnotation : methodLevelAnnotation;
    }

    @Override
    protected Collection<ReportGenerator> getActiveReporters(Method method) {
        return JUnitPerfSuite.getReporters(method.getDeclaringClass());
    }

    @Override
    protected EvaluationContext createEvaluationContext(Method method, boolean isAsync) {
        EvaluationContext ctxt =  super.createEvaluationContext(method, isAsync);
        ctxt.setGroupName(method.getDeclaringClass().getSimpleName());
        return ctxt;
    }
}
