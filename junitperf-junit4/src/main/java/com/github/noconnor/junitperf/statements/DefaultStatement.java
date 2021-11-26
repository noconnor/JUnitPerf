package com.github.noconnor.junitperf.statements;

import org.junit.runners.model.Statement;

public class DefaultStatement implements TestStatement {

    private final Statement statement;

    public DefaultStatement(Statement statement) {
        this.statement = statement;
    }

    @Override
    public void runBefores() {
        // do nothing
    }

    @Override
    public void evaluate() throws Throwable {
        // Underlying statement will run before and after and all latencies/stats will be measured
        statement.evaluate();
    }

    @Override
    public void runAfters() {
        // do nothing
    }
}
