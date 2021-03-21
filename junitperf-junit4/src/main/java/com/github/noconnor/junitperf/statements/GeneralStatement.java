package com.github.noconnor.junitperf.statements;

import org.junit.runners.model.Statement;

public class GeneralStatement implements TestStatement {

    private final Statement statement;

    public GeneralStatement(Statement statement) {
        this.statement = statement;
    }

    @Override
    public void runBefores() {
        // do nothing
    }

    @Override
    public void evaluate() throws Throwable {
        statement.evaluate();
    }

    @Override
    public void runAfters() throws Throwable {
        // do nothing
    }
}
