package com.github.noconnor.junitperf;

import static java.lang.System.nanoTime;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.reporting.ReportGenerator;
import com.github.noconnor.junitperf.reporting.providers.ConsoleReportGenerator;
import com.github.noconnor.junitperf.statements.PerformanceEvaluationStatement;
import com.github.noconnor.junitperf.statements.SimpleTestStatement;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import com.github.noconnor.junitperf.statistics.providers.DescriptiveStatisticsCalculator;

public class JUnitPerfInterceptor implements InvocationInterceptor, TestInstancePostProcessor {

    private final Map<Class<?>, LinkedHashSet<EvaluationContext>> activeContexts = new HashMap<>();
    private final Set<ReportGenerator> activeReporters = new HashSet<>();
    private StatisticsCalculator activeStatisticsCalculator;

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        for (Field field : testInstance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(JUnitPerfTestActiveConfig.class)) {
                field.setAccessible(true);
                JUnitPerfTestConfig reportingConfig = (JUnitPerfTestConfig) field.get(testInstance);
                activeReporters.addAll(reportingConfig.getReportGenerators());
                activeStatisticsCalculator = reportingConfig.getStatisticsCalculator();
            }
        }
        // Defaults if no overrides provided
        if (activeReporters.isEmpty()) {
            activeReporters.add(new ConsoleReportGenerator());
        }
        if (isNull(activeStatisticsCalculator)) {
            activeStatisticsCalculator = new DescriptiveStatisticsCalculator();
        }
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {

        Method method = extensionContext.getRequiredTestMethod();
        JUnitPerfTest perfTestAnnotation = method.getAnnotation(JUnitPerfTest.class);
        JUnitPerfTestRequirement requirementsAnnotation = method.getAnnotation(JUnitPerfTestRequirement.class);

        if (nonNull(perfTestAnnotation)) {
            EvaluationContext context = createEvaluationContext(method);
            context.loadConfiguration(perfTestAnnotation);
            context.loadRequirements(requirementsAnnotation);

            activeContexts.putIfAbsent(extensionContext.getRequiredTestClass(), new LinkedHashSet<>());
            activeContexts.get(extensionContext.getRequiredTestClass()).add(context);

            SimpleTestStatement testStatement = () -> method.invoke(extensionContext.getRequiredTestInstance());
            PerformanceEvaluationStatement parallelExecution = PerformanceEvaluationStatement.builder()
                    .baseStatement(testStatement)
                    .statistics(activeStatisticsCalculator)
                    .context(context)
                    .listener(complete -> updateReport(method.getDeclaringClass()))
                    .build();

            parallelExecution.runParallelEvaluation();
        }
        invocation.proceed();
    }

    EvaluationContext createEvaluationContext(Method method) {
        return new EvaluationContext(method.getName(), nanoTime());
    }

    private synchronized void updateReport(Class<?> clazz) {
        activeReporters.forEach(r -> {
            r.generateReport(activeContexts.get(clazz));
        });
    }

}
