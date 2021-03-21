package com.github.noconnor.junitperf.statements;

import org.junit.runners.model.Statement;

class EmptyStatement extends Statement {

    @Override
    public void evaluate() throws Throwable {
        // do nothing
    }
}
