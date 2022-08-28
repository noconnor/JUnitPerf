package com.github.noconnor.junitperf;

import com.github.noconnor.junitperf.data.TestContext;
import com.github.noconnor.junitperf.statistics.StatisticsCalculator;
import lombok.Setter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static java.util.Objects.nonNull;

class TestInvoker {
    private final Object[] args;
    private final Method method;
    private int asyncArgIndex = -1;
    @Setter
    private long measurementsStartTimeMs;
    @Setter
    private StatisticsCalculator statsCalculator;

    public TestInvoker(Method method, List<Object> args) {
        for (int i = 0; i < args.size(); i++) {
            if (args.get(i) instanceof TestContext) {
                asyncArgIndex = i;
                break;
            }
        }
        this.args = args.toArray();
        this.method = method;
    }

    public boolean isAsyncTest() {
        return asyncArgIndex >= 0;
    }

    public void invoke(Object testInstance) throws InvocationTargetException, IllegalAccessException {
        if (isAsyncTest() && hasMeasurementStarted() && nonNull(statsCalculator)) {
            args[asyncArgIndex] = new TestContext(statsCalculator);
        }
        method.invoke(testInstance, args);
    }

    private boolean hasMeasurementStarted() {
        return currentTimeMillis() >= measurementsStartTimeMs;
    }

}
