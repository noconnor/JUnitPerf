package com.github.noconnor.junitperf;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class JUnitPerfInterceptor implements InvocationInterceptor {

    public static AtomicInteger COUNT = new AtomicInteger();

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {
        AtomicInteger exceptionCount = new AtomicInteger();
        List<Thread> threads = new ArrayList<>();

        // 1000 invocations
        for (int k = 0; k < 10; k++) {
            Thread t = new Thread(() -> {
                for (int i = 0; i < 100; i++) {
                    try {
                        extensionContext.getRequiredTestMethod().invoke(extensionContext.getRequiredTestInstance());
                    } catch (Exception e) {
                        exceptionCount.incrementAndGet();
                    }
                }
            });
            threads.add(t);
            t.start();

        }

        threads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });

        try {
            // extra invocation (so total will be 1001 invocations)
            invocation.proceed();
        } catch (Exception e) {
            exceptionCount.incrementAndGet();
        }

        System.out.println("Test executed: " + COUNT.get());
    }
}
