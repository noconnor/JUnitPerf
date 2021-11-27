package com.github.noconnor.junitperf.statements;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.runners.model.FrameworkMethod;

public class MeasurableStatementTest {

    private ExampleTestClass exampleTestClass;

    @Before
    public void setup() {
        exampleTestClass = new ExampleTestClass();
    }

    @Test
    public void whenMeasurableStatementIsCreated_thenTestStatementBeforesShouldBeDecomposed() throws Throwable {
        FrameworkMethod before = new FrameworkMethod(ExampleTestClass.class.getMethod("setup"));
        FrameworkMethod after = new FrameworkMethod(ExampleTestClass.class.getMethod("teardown"));
        FrameworkMethod test1 = new FrameworkMethod(ExampleTestClass.class.getMethod("test1"));

        InvokeMethod invokeTest = new InvokeMethod(test1, exampleTestClass);
        RunBefores befores = new RunBefores(invokeTest, singletonList(before), exampleTestClass);
        RunAfters afters = new RunAfters(befores, singletonList(after), exampleTestClass);

        MeasurableStatement measurableStatement = new MeasurableStatement(afters);

        measurableStatement.runBefores();
        assertEquals(1, exampleTestClass.beforeExecutedCount);
        assertEquals(0, exampleTestClass.testStatementsExecutedCount);
        assertEquals(0, exampleTestClass.afterExecutedCount);
    }

    @Test
    public void whenMeasurableStatementIsCreated_andThereAreNoAfters_thenTestStatementBeforesShouldBeDecomposed() throws Throwable {
        FrameworkMethod before = new FrameworkMethod(ExampleTestClass.class.getMethod("setup"));
        FrameworkMethod test1 = new FrameworkMethod(ExampleTestClass.class.getMethod("test1"));

        InvokeMethod invokeTest = new InvokeMethod(test1, exampleTestClass);
        RunBefores befores = new RunBefores(invokeTest, singletonList(before), exampleTestClass);

        MeasurableStatement measurableStatement = new MeasurableStatement(befores);

        measurableStatement.runBefores();
        assertEquals(1, exampleTestClass.beforeExecutedCount);
        assertEquals(0, exampleTestClass.testStatementsExecutedCount);
        assertEquals(0, exampleTestClass.afterExecutedCount);
    }

    @Test
    public void whenMeasurableStatementIsCreated_andStatementHasMultipleBefores_thenTestStatementBeforesShouldBeDecomposed() throws Throwable {
        FrameworkMethod before = new FrameworkMethod(ExampleTestClass.class.getMethod("setup"));
        FrameworkMethod after = new FrameworkMethod(ExampleTestClass.class.getMethod("teardown"));
        FrameworkMethod test1 = new FrameworkMethod(ExampleTestClass.class.getMethod("test1"));

        InvokeMethod invokeTest = new InvokeMethod(test1, exampleTestClass);
        RunBefores befores = new RunBefores(invokeTest, Arrays.asList(before, before, before), exampleTestClass);
        RunAfters afters = new RunAfters(befores, singletonList(after), exampleTestClass);

        MeasurableStatement measurableStatement = new MeasurableStatement(afters);

        measurableStatement.runBefores();
        assertEquals(3, exampleTestClass.beforeExecutedCount);
        assertEquals(0, exampleTestClass.testStatementsExecutedCount);
        assertEquals(0, exampleTestClass.afterExecutedCount);
    }

    @Test
    public void whenMeasurableStatementIsCreated_thenTestStatementAftersShouldBeDecomposed() throws Throwable {
        FrameworkMethod before = new FrameworkMethod(ExampleTestClass.class.getMethod("setup"));
        FrameworkMethod after = new FrameworkMethod(ExampleTestClass.class.getMethod("teardown"));
        FrameworkMethod test1 = new FrameworkMethod(ExampleTestClass.class.getMethod("test1"));

        InvokeMethod invokeTest = new InvokeMethod(test1, exampleTestClass);
        RunBefores befores = new RunBefores(invokeTest, singletonList(before), exampleTestClass);
        RunAfters afters = new RunAfters(befores, singletonList(after), exampleTestClass);

        MeasurableStatement measurableStatement = new MeasurableStatement(afters);

        measurableStatement.runAfters();
        assertEquals(0, exampleTestClass.beforeExecutedCount);
        assertEquals(0, exampleTestClass.testStatementsExecutedCount);
        assertEquals(1, exampleTestClass.afterExecutedCount);
    }

    @Test
    public void whenMeasurableStatementIsCreated_andStatementHasMultipleAfters_thenTestStatementAftersShouldBeDecomposed() throws Throwable {
        FrameworkMethod before = new FrameworkMethod(ExampleTestClass.class.getMethod("setup"));
        FrameworkMethod after = new FrameworkMethod(ExampleTestClass.class.getMethod("teardown"));
        FrameworkMethod test1 = new FrameworkMethod(ExampleTestClass.class.getMethod("test1"));

        InvokeMethod invokeTest = new InvokeMethod(test1, exampleTestClass);
        RunBefores befores = new RunBefores(invokeTest, singletonList(before), exampleTestClass);
        RunAfters afters = new RunAfters(befores, Arrays.asList(after, after, after), exampleTestClass);

        MeasurableStatement measurableStatement = new MeasurableStatement(afters);

        measurableStatement.runAfters();
        assertEquals(0, exampleTestClass.beforeExecutedCount);
        assertEquals(0, exampleTestClass.testStatementsExecutedCount);
        assertEquals(3, exampleTestClass.afterExecutedCount);
    }

    @Test
    public void whenMeasurableStatementIsCreated_thenTestStatementShouldBeDecomposed() throws Throwable {
        FrameworkMethod before = new FrameworkMethod(ExampleTestClass.class.getMethod("setup"));
        FrameworkMethod after = new FrameworkMethod(ExampleTestClass.class.getMethod("teardown"));
        FrameworkMethod test1 = new FrameworkMethod(ExampleTestClass.class.getMethod("test1"));

        InvokeMethod invokeTest = new InvokeMethod(test1, exampleTestClass);
        RunBefores befores = new RunBefores(invokeTest, singletonList(before), exampleTestClass);
        RunAfters afters = new RunAfters(befores, singletonList(after), exampleTestClass);

        MeasurableStatement measurableStatement = new MeasurableStatement(afters);

        measurableStatement.evaluate();
        assertEquals(0, exampleTestClass.beforeExecutedCount);
        assertEquals(1, exampleTestClass.testStatementsExecutedCount);
        assertEquals(0, exampleTestClass.afterExecutedCount);
    }

    @Test
    public void whenMeasurableStatementIsCreated_andStatementHasNoBeforeOrAfters_thenTestStatementShouldBeDecomposed() throws Throwable {
        FrameworkMethod test1 = new FrameworkMethod(ExampleTestClass.class.getMethod("test1"));
        InvokeMethod invokeTest = new InvokeMethod(test1, exampleTestClass);
        MeasurableStatement measurableStatement = new MeasurableStatement(invokeTest);

        measurableStatement.runBefores();
        measurableStatement.evaluate();
        measurableStatement.runAfters();
        assertEquals(0, exampleTestClass.beforeExecutedCount);
        assertEquals(1, exampleTestClass.testStatementsExecutedCount);
        assertEquals(0, exampleTestClass.afterExecutedCount);
    }

    @Test
    public void whenMeasurableStatementIsCreated_andBeforeStatementTriggersARunAfter_thenTestStatementShouldBeDecomposed() throws Throwable {
        FrameworkMethod before = new FrameworkMethod(ExampleTestClass.class.getMethod("setup"));
        FrameworkMethod after = new FrameworkMethod(ExampleTestClass.class.getMethod("teardown"));
        FrameworkMethod test1 = new FrameworkMethod(ExampleTestClass.class.getMethod("test1"));

        InvokeMethod invokeTest = new InvokeMethod(test1, exampleTestClass);
        RunAfters afters = new RunAfters(invokeTest, singletonList(after), exampleTestClass);
        RunBefores befores = new RunBefores(afters, singletonList(before), exampleTestClass);

        MeasurableStatement measurableStatement = new MeasurableStatement(befores);

        measurableStatement.runBefores();
        measurableStatement.evaluate();
        measurableStatement.runAfters();
        assertEquals(1, exampleTestClass.beforeExecutedCount);
        assertEquals(1, exampleTestClass.testStatementsExecutedCount);
        assertEquals(1, exampleTestClass.afterExecutedCount);
    }

    @Test
    public void whenMeasurableStatementIsCreated_andAfterHasNoBefore_thenStatementANdAfterShouldBeRunnable() throws Throwable {

        FrameworkMethod after = new FrameworkMethod(ExampleTestClass.class.getMethod("teardown"));
        FrameworkMethod test1 = new FrameworkMethod(ExampleTestClass.class.getMethod("test1"));

        InvokeMethod invokeTest1 = new InvokeMethod(test1, exampleTestClass);
        RunAfters afters1 = new RunAfters(invokeTest1, singletonList(after), exampleTestClass);

        MeasurableStatement measurableStatement = new MeasurableStatement(afters1);

        measurableStatement.runBefores();
        measurableStatement.evaluate();
        measurableStatement.runAfters();

        assertEquals(0, exampleTestClass.beforeExecutedCount);
        assertEquals(1, exampleTestClass.testStatementsExecutedCount);
        assertEquals(1, exampleTestClass.afterExecutedCount);
    }


    public static class ExampleTestClass {

        int beforeExecutedCount;
        int afterExecutedCount;
        int testStatementsExecutedCount;

        public void setup() {
            beforeExecutedCount += 1;
        }

        public void teardown() {
            afterExecutedCount += 1;
        }

        public void test1() {
            testStatementsExecutedCount += 1;
        }

        public void test2() {
            testStatementsExecutedCount += 1;
        }

    }
}