package com.asus.updatesdk.utility;

import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Ken1_Yu on 2015/7/14.
 */
final class ReflectionUtils {
    private static final String TAG = "ReflectionUtils";

    /**
     * Get a class by name
     * @param name The class name
     * @return Class object or null (not found)
     */
    @Nullable
    static Class<?> getClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            logReflectionException("Class: " + name , e);
        }
        return null;
    }

    /**
     * Call a method of specified object by Java reflection.
     * @param methodName The method name
     * @param obj        The caller object
     * @param paramType  The parameter type
     * @param arguments  The method input arguments
     * @return 1. null: void method or no such method.<BR>
     *         2. T   : return value of the method
     */
    @SuppressWarnings("unchecked")
    @Nullable static <T> T callFeatureMethod(String methodName, @Nullable Object obj,
            @Nullable Class<?>[] paramType, @Nullable Object[] arguments) {
        if (obj == null) return null;

        // Using "clazz" as class' instance naming
        // @see <a href="http://stackoverflow.com/questions/2529974/why-do-java-programmers-like-to-name-a-variable-clazz/">
        Class<?> clazz = (Class<?>) (obj instanceof Class ? obj : obj.getClass());
        if (Object.class.isAssignableFrom(clazz)) {
            try {
                Method method = clazz.getMethod(methodName, paramType);
                return (T) method.invoke(obj, arguments);
            } catch (NoSuchMethodException | IllegalArgumentException |
                    IllegalAccessException | InvocationTargetException e) {
                logReflectionException("Method: " + methodName , e);
            }
        }
        return null;
    }

    private static void logReflectionException(String message, Throwable throwable) {
        Log.v(TAG, message, throwable);
    }
}
