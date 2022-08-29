package com.github.noconnor.junitperf.statements;

public interface SimpleTestStatement extends TestStatement {
    
    @Override
    default void runBefores() { 
        // Do nothing
    }

    @Override
    default void runAfters() {
        // Do nothing
    }
}
