package com.github.noconnor.junitperf.utils;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestReflectionUtilsTest {

    @Test
    void whenClassContainsNoBeforeAndAfterMethods_thenEmptyListShouldBeReturned() {
        NoBeforeAfters test = new NoBeforeAfters();
        assertEquals(emptyList(), TestReflectionUtils.findBeforeEach(test));
        assertEquals(emptyList(), TestReflectionUtils.findAfterEach(test));
    }

    @Test
    void whenClassContainsBeforeEach_thenNonEmptyBeforeEachListShouldBeReturned() {
        BeforeEachClass test = new BeforeEachClass();
        assertEquals(emptyList(), TestReflectionUtils.findAfterEach(test));
        List<Method> beforeMethods = TestReflectionUtils.findBeforeEach(test);
        assertEquals(1, beforeMethods.size());
        assertEquals("setupBeforeEachClass", beforeMethods.get(0).getName());
        assertDoesNotThrow(() -> beforeMethods.get(0).invoke(test));
    }

    @Test
    void whenClassContainsAfterEach_thenNonEmptyAfterEachListShouldBeReturned() {
        AfterEachClass test = new AfterEachClass();
        assertEquals(emptyList(), TestReflectionUtils.findBeforeEach(test));
        List<Method> afterMethods = TestReflectionUtils.findAfterEach(test);
        assertEquals(1, afterMethods.size());
        assertEquals("tearDownAfterEachClass", afterMethods.get(0).getName());
        assertDoesNotThrow(() -> afterMethods.get(0).invoke(test));
    }

    @Test
    void whenClassContainsBeforeAndAfterEach_thenNonEmptyBeforeAndAfterEachListShouldBeReturned() {
        BeforeAndAfterClass test = new BeforeAndAfterClass();
        List<Method> beforeMethods = TestReflectionUtils.findBeforeEach(test);
        List<Method> afterMethods = TestReflectionUtils.findAfterEach(test);
        assertEquals(1, beforeMethods.size());
        assertEquals(1, afterMethods.size());
        assertEquals("setupBeforeAndAfterClass", beforeMethods.get(0).getName());
        assertEquals("tearDownBeforeAndAfterClass", afterMethods.get(0).getName());
        assertDoesNotThrow(() -> beforeMethods.get(0).invoke(test));
        assertDoesNotThrow(() -> afterMethods.get(0).invoke(test));
    }

    @Test
    void whenClassContainsMultipleBeforeAndAfterEach_thenNonEmptyBeforeAndAfterEachListShouldBeReturned() {
        MultipleBeforeAndAfterClass test = new MultipleBeforeAndAfterClass();
        List<Method> beforeMethods = TestReflectionUtils.findBeforeEach(test);
        List<Method> afterMethods = TestReflectionUtils.findAfterEach(test);
        assertEquals(2, beforeMethods.size());
        assertEquals(2, afterMethods.size());
    }

    @Disabled
    public static class NoBeforeAfters {
    }

    @Disabled
    public static class BeforeEachClass {
        @BeforeEach
        void setupBeforeEachClass(){
        }
    }

    @Disabled
    public static class AfterEachClass {
        @AfterEach
        void tearDownAfterEachClass(){
        }
    }

    @Disabled
    public static class BeforeAndAfterClass {
        @BeforeEach
        void setupBeforeAndAfterClass(){
        }
        @AfterEach
        void tearDownBeforeAndAfterClass(){
        }
    }

    @Disabled
    public static class MultipleBeforeAndAfterClass {
        @BeforeEach
        void setup1BeforeAndAfterClass(){
        }
        @BeforeEach
        void setup2BeforeAndAfterClass(){
        }
        @AfterEach
        void tearDown1BeforeAndAfterClass(){
        }
        @AfterEach
        void tearDown2BeforeAndAfterClass(){
        }
    }
}