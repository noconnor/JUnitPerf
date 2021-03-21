package com.github.noconnor.junitperf.statements;

import java.lang.reflect.Field;
import java.util.List;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class BeforeAfterStatement implements TestStatement {

    private final Statement statement;
    private final RunBefores befores;
    private final RunAfters afters;

    public BeforeAfterStatement(RunBefores befores) {
        try {
            Statement statement = captureStatement(befores);

            this.befores = captureBefores(befores);
            if (statement instanceof RunAfters) {
                this.afters = captureAfters((RunAfters) statement);
                this.statement = captureStatement((RunAfters) statement);
            } else {
                this.afters = null;
                this.statement = statement;
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }


    public BeforeAfterStatement(RunAfters afters) {
        try {
            Statement statement = captureStatement(afters);
            this.afters = captureAfters(afters);
            if (statement instanceof RunBefores) {
                this.befores = captureBefores((RunBefores) statement);
                this.statement = captureStatement((RunBefores) statement);
            } else {
                this.befores = null;
                this.statement = statement;
            }

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void runBefores() throws Throwable {
        befores.evaluate();
    }

    @Override
    public void evaluate() throws Throwable {
        statement.evaluate();
    }

    @Override
    public void runAfters() throws Throwable {
        afters.evaluate();
    }

    private static class EmptyStatement extends Statement {

        @Override
        public void evaluate() throws Throwable {
            // do nothing
        }
    }

    @SuppressWarnings("unchecked")
    private RunBefores captureBefores(RunBefores befores) throws NoSuchFieldException, IllegalAccessException {
        Field beforesField = RunBefores.class.getDeclaredField("befores");
        Field targetField = RunBefores.class.getDeclaredField("target");
        beforesField.setAccessible(true);
        targetField.setAccessible(true);
        return new RunBefores(
            new EmptyStatement(),
            (List<FrameworkMethod>) beforesField.get(befores),
            targetField.get(befores)
        );
    }

    @SuppressWarnings("unchecked")
    private RunAfters captureAfters(RunAfters afters) throws NoSuchFieldException, IllegalAccessException {
        Field aftersField = RunAfters.class.getDeclaredField("afters");
        Field targetField = RunAfters.class.getDeclaredField("target");
        aftersField.setAccessible(true);
        targetField.setAccessible(true);
        return new RunAfters(
            new EmptyStatement(),
            (List<FrameworkMethod>) aftersField.get(afters),
            targetField.get(afters)
        );
    }

    private Statement captureStatement(RunAfters afters) throws NoSuchFieldException, IllegalAccessException {
        Field nextField = RunAfters.class.getDeclaredField("next");
        nextField.setAccessible(true);
        return (Statement) nextField.get(afters);
    }

    private Statement captureStatement(RunBefores befores) throws NoSuchFieldException, IllegalAccessException {
        Field nextField = RunBefores.class.getDeclaredField("next");
        nextField.setAccessible(true);
        return (Statement) nextField.get(befores);
    }

}
