package com.github.noconnor.junitperf;

import com.github.noconnor.junitperf.data.EvaluationContext;
import com.github.noconnor.junitperf.reporting.ReportGenerator;
import com.github.noconnor.junitperf.reporting.providers.ConsoleReportGenerator;
import com.github.noconnor.junitperf.statements.PerformanceEvaluationStatement;
import com.github.noconnor.junitperf.statements.TestStatement;
import com.github.noconnor.junitperf.statistics.providers.DescriptiveStatisticsCalculator;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Method;
import java.util.*;

import static java.lang.System.nanoTime;
import static java.util.Objects.nonNull;

public class JUnitPerfInterceptor implements InvocationInterceptor {

    // TODO:
    // * Figure out is static ACTIVE_CONTEXTS is required (shouldn't be required)
    // * Figure out how to pass collection of "Reporter" instances when using @ExtendsWith (a configuration annotation??)
    // * Figure out how to inject "StatisticsCollector" class when using @ExtendsWith (a configuration annotation??)
    // * Maybe have a "default" console reporter that is used as a fallback when no reporters are configured

    private static final Map<Class<?>, LinkedHashSet<EvaluationContext>> ACTIVE_CONTEXTS = new HashMap<>();
    private final Set<ReportGenerator> reporters = new HashSet<>();

    public JUnitPerfInterceptor() {
        reporters.add(new ConsoleReportGenerator()); // TODO: remove this hardcoding
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {

        JUnitPerfTest perfTestAnnotation = extensionContext.getRequiredTestMethod().getAnnotation(JUnitPerfTest.class);
        JUnitPerfTestRequirement requirementsAnnotation = extensionContext.getRequiredTestMethod().getAnnotation(JUnitPerfTestRequirement.class);

        if (nonNull(perfTestAnnotation)) {
            EvaluationContext context = createEvaluationContext(extensionContext.getRequiredTestMethod());
            context.loadConfiguration(perfTestAnnotation);
            context.loadRequirements(requirementsAnnotation);

            // Group test contexts by test class
            ACTIVE_CONTEXTS.putIfAbsent(extensionContext.getTestClass().get(), new LinkedHashSet<>());
            ACTIVE_CONTEXTS.get(extensionContext.getTestClass().get()).add(context);

            // TODO: Move to outer class or move to junit-core
            TestStatement testStatement = new TestStatement() {
                @Override
                public void runBefores() throws Throwable {
                    // do nothing
                }

                @Override
                public void evaluate() throws Throwable {
                    extensionContext.getRequiredTestMethod().invoke(extensionContext.getRequiredTestInstance());
                }

                @Override
                public void runAfters() throws Throwable {
                    // do nothing
                }
            };

            PerformanceEvaluationStatement parallelExecution = PerformanceEvaluationStatement.builder()
                    .baseStatement(testStatement)
                    .statistics(new DescriptiveStatisticsCalculator()) // TODO: remove this hardcoding
                    .context(context)
                    .listener(complete -> updateReport(extensionContext.getRequiredTestMethod().getDeclaringClass()))
                    .build();

            parallelExecution.runParallelEvaluation();
        } else {
            System.out.println("No annotation, calling test once");
        }
        invocation.proceed();
    }

    EvaluationContext createEvaluationContext(Method method) {
        return new EvaluationContext(method.getName(), nanoTime());
    }

    private synchronized void updateReport(Class<?> clazz) {
        reporters.forEach(r -> {
            r.generateReport(ACTIVE_CONTEXTS.get(clazz));
        });
    }

}
