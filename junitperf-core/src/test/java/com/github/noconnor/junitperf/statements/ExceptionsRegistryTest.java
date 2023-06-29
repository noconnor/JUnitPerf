package com.github.noconnor.junitperf.statements;

import org.junit.After;
import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ExceptionsRegistryTest {

    @Before
    public void setup() {
        ExceptionsRegistry.registerIgnorable(InterruptedException.class);
        ExceptionsRegistry.registerAbort(AssumptionViolatedException.class);
    }
    
    @After
    public void teardown() {
        ExceptionsRegistry.clearRegistry();
    }

    @Test
    public void testRegistry() {
        assertEquals(1, ExceptionsRegistry.ignorables().size());
        assertEquals(1, ExceptionsRegistry.abortables().size());
        assertTrue(ExceptionsRegistry.ignorables().contains(InterruptedException.class));
        assertTrue(ExceptionsRegistry.abortables().contains(AssumptionViolatedException.class));
    }
    
    @Test
    public void ifIgnoreExceptionIsRegistered_thenTestingForIgnoreExceptionShouldReturnTrue() {
        assertTrue(ExceptionsRegistry.isIgnorable(new InterruptedException()));
    }

    @Test
    public void ifIgnoreExceptionIsNotRegistered_thenTestingForIgnoreExceptionShouldReturnFalse() {
        assertFalse(ExceptionsRegistry.isIgnorable(new IllegalStateException()));
    }

    @Test
    public void ifAbortExceptionIsRegistered_thenTestingForAbortExceptionShouldRethrowException() {
        AssumptionViolatedException abort = new AssumptionViolatedException("unittest");
        try {
            ExceptionsRegistry.reThrowIfAbort(abort);
            fail("Expected exception to be re-thrown");
        } catch (AssumptionViolatedException e) {
            // expected
        } catch (Throwable t) {
            fail("Unexpected exception thrown");
        }
    }

    @Test
    public void ifAbortExceptionIsNotRegistered_thenTestingForAbortExceptionShouldNotRethrowException() {
        IllegalStateException exception = new IllegalStateException("unittest");
        try {
            ExceptionsRegistry.reThrowIfAbort(exception);
        } catch (Throwable t) {
            fail("Unexpected exception thrown");
        }
    }
}