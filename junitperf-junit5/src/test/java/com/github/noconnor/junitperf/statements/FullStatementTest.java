package com.github.noconnor.junitperf.statements;

import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FullStatementTest {
    
    private FullStatement statement;
    
    private MockTestInstance testInstanceMock;
    
    
    @BeforeEach
    void setup() throws NoSuchMethodException {
        testInstanceMock = new MockTestInstance();
        Method testMethod = MockTestInstance.class.getDeclaredMethod("someMethod");
        statement = new FullStatement(testInstanceMock, testMethod, emptyList());
    }
    
    @Test
    void whenEvaluateIsCalled_thenMethodShouldBeInvoked() throws Throwable {
        statement.evaluate();
        assertEquals(1, testInstanceMock.getInvocationCount().get());
    }

    @Test
    void whenRunBeforeIsCalled_thenNoBeforeMethodsExist_thenNoExceptionsShouldBeThrown() {
        assertDoesNotThrow(() -> statement.runBefores());
    }

    @Test
    void whenRunAfterIsCalled_thenNoAfterMethodsExist_thenNoExceptionsShouldBeThrown() {
        assertDoesNotThrow(() -> statement.runAfters());
    }

    @Test
    void whenRunBeforeIsCalled_thenBeforeMethodsExist_thenMethodsShouldBeCalled() throws NoSuchMethodException {
        Method before1Method = MockTestInstance.class.getDeclaredMethod("before1");
        Method before2Method = MockTestInstance.class.getDeclaredMethod("before2");
        List<Method> beforeMethods = new ArrayList<>();
        beforeMethods.add(before1Method);
        beforeMethods.add(before2Method);
        
        statement.setBeforeEach(beforeMethods);
        assertDoesNotThrow(() -> statement.runBefores());
        assertEquals(1, testInstanceMock.getBefore1Count().get());
        assertEquals(1, testInstanceMock.getBefore2Count().get());
    }

    @Test
    void whenRunAfterIsCalled_thenAfterMethodsExist_thenMethodsShouldBeCalled() throws NoSuchMethodException {
        Method after1Method = MockTestInstance.class.getDeclaredMethod("after1");
        Method after2Method = MockTestInstance.class.getDeclaredMethod("after2");
        List<Method> afterMethods = new ArrayList<>();
        afterMethods.add(after1Method);
        afterMethods.add(after2Method);

        statement.setAfterEach(afterMethods);
        assertDoesNotThrow(() -> statement.runAfters());
        assertEquals(1, testInstanceMock.getAfter1Count().get());
        assertEquals(1, testInstanceMock.getAfter2Count().get());
    }


    public static class MockTestInstance {
        @Getter
        private final AtomicInteger invocationCount = new AtomicInteger();
        @Getter
        private final AtomicInteger before1Count = new AtomicInteger();
        @Getter
        private final AtomicInteger before2Count = new AtomicInteger();
        @Getter
        private final AtomicInteger after1Count = new AtomicInteger();
        @Getter
        private final AtomicInteger after2Count = new AtomicInteger();
        
        private void someMethod() {
            invocationCount.incrementAndGet();
        }

        private void before1() {
            before1Count.incrementAndGet();
        }

        private void before2() {
            before2Count.incrementAndGet();
        }

        private void after1() {
            after1Count.incrementAndGet();
        }

        private void after2() {
            after2Count.incrementAndGet();
        }
    }

}