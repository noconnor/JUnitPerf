package com.github.noconnor.junitperf.statements;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.rules.ExpectedException;
import org.junit.runners.model.FrameworkMethod;

public class MeasurableStatementTest {


    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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