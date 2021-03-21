package com.github.noconnor.junitperf.statements;

import static java.util.Objects.nonNull;

import java.lang.reflect.Field;
import java.util.List;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class MeasurableStatement implements TestStatement {

    private RunBefores befores;
    private Statement statement;
    private RunAfters afters;

    public MeasurableStatement(Statement statement) {
        if (statement instanceof RunAfters) {
            decompose((RunAfters) statement);
        } else if (statement instanceof RunBefores) {
            decompose((RunBefores) statement);
        } else {
            this.befores = null;
            this.statement = statement;
            this.afters = null;
        }
    }

    @Override
    public void runBefores() throws Throwable {
        if (nonNull(befores)) {
            befores.evaluate();
        }
    }

    @Override
    public void evaluate() throws Throwable {
        statement.evaluate();
    }

    @Override
    public void runAfters() throws Throwable {
        if (nonNull(afters)) {
            afters.evaluate();
        }
    }

    private void decompose(RunBefores befores) {
        try {
            Statement statement = captureStatement(befores);
            this.befores = decomposeBefores(befores);
            if (statement instanceof RunAfters) {
                this.afters = decomposeAfters((RunAfters) statement);
                this.statement = captureStatement((RunAfters) statement);
            } else {
                this.afters = null;
                this.statement = statement;
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void decompose(RunAfters afters) {
        try {
            Statement statement = captureStatement(afters);
            this.afters = decomposeAfters(afters);
            if (statement instanceof RunBefores) {
                this.befores = decomposeBefores((RunBefores) statement);
                this.statement = captureStatement((RunBefores) statement);
            } else {
                this.befores = null;
                this.statement = statement;
            }

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private RunBefores decomposeBefores(RunBefores befores) throws NoSuchFieldException, IllegalAccessException {
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
    private RunAfters decomposeAfters(RunAfters afters) throws NoSuchFieldException, IllegalAccessException {
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
