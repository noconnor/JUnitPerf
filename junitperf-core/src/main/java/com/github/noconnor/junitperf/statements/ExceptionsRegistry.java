package com.github.noconnor.junitperf.statements;

import lombok.experimental.UtilityClass;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class ExceptionsRegistry {

    private static final Set<Class<?>> IGNORABLE_EXCEPTIONS_REGISTRY = new HashSet<>();
    private static final Set<Class<?>> ABORT_EXCEPTIONS_REGISTRY = new HashSet<>();

    public static void registerIgnorable(Class<? extends Throwable> exception) {
        IGNORABLE_EXCEPTIONS_REGISTRY.add(exception);
    }

    public static void registerAbort(Class<? extends Throwable> exception) {
        ABORT_EXCEPTIONS_REGISTRY.add(exception);
    }

    public static boolean isIgnorable(Throwable throwable) {
        return IGNORABLE_EXCEPTIONS_REGISTRY.contains(throwable.getClass());
    }

    public static void reThrowIfAbort(Throwable throwable) throws Throwable {
        Throwable targetException = throwable;
        if (throwable instanceof InvocationTargetException) {
            targetException = ((InvocationTargetException) throwable).getTargetException();
        }
        if (ABORT_EXCEPTIONS_REGISTRY.contains(targetException.getClass())) {
            // re-throw abortable exceptions
            throw targetException;
        }
    }
    
    public static Set<Class<?>> ignorables() {
        return Collections.unmodifiableSet(IGNORABLE_EXCEPTIONS_REGISTRY);
    }

    public static Set<Class<?>> abortables() {
        return Collections.unmodifiableSet(ABORT_EXCEPTIONS_REGISTRY);
    }

    static void clearRegistry() {
        IGNORABLE_EXCEPTIONS_REGISTRY.clear();
        ABORT_EXCEPTIONS_REGISTRY.clear();
    }
}
