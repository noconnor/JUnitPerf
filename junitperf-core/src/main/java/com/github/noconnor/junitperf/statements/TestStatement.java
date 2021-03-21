package com.github.noconnor.junitperf.statements;

public interface TestStatement {

    void runBefores() throws Throwable;

    void evaluate() throws Throwable;

    void runAfters() throws Throwable;
}
