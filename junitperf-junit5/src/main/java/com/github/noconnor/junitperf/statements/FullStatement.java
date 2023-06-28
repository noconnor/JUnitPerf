package com.github.noconnor.junitperf.statements;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.List;

import static java.util.Collections.emptyList;

@RequiredArgsConstructor
public class FullStatement implements TestStatement {

    @Getter
    @Setter
    private List<Method> beforeEach = emptyList();
    @Getter
    @Setter
    private List<Method> afterEach = emptyList();

    private final Object testClass;
    private final Method testMethod;
    private final List<Object> args;

    @Override
    public void runBefores() throws Throwable {
        for (Method m : beforeEach) {
            m.setAccessible(true);
            m.invoke(testClass);
        }
    }

    @Override
    public void evaluate() throws Throwable {
        testMethod.setAccessible(true);
        testMethod.invoke(testClass, args.toArray());
    }

    @Override
    public void runAfters() throws Throwable {
        for (Method m : afterEach) {
            m.setAccessible(true);
            m.invoke(testClass);
        }
    }
}
