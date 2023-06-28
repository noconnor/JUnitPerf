package com.github.noconnor.junitperf.utils;

import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.List;

@UtilityClass
public class TestReflectionUtils {
    
    public static List<Method> findBeforeEach(Object testClass) {
        return ReflectionUtils.findMethods(testClass.getClass(), m -> m.isAnnotationPresent(BeforeEach.class));
    }

    public static List<Method> findAfterEach(Object testClass) {
        return ReflectionUtils.findMethods(testClass.getClass(), m -> m.isAnnotationPresent(AfterEach.class));
    }
}
