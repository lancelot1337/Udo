package org.cocos2dx.lib;

import android.util.Log;
import com.facebook.internal.NativeProtocol;
import java.lang.reflect.InvocationTargetException;

public class Cocos2dxReflectionHelper {
    public static <T> T getConstantValue(Class aClass, String constantName) {
        T t = null;
        try {
            t = aClass.getDeclaredField(constantName).get(null);
        } catch (NoSuchFieldException e) {
            Log.e(NativeProtocol.BRIDGE_ARG_ERROR_BUNDLE, "can not find " + constantName + " in " + aClass.getName());
        } catch (IllegalAccessException e2) {
            Log.e(NativeProtocol.BRIDGE_ARG_ERROR_BUNDLE, constantName + " is not accessible");
        } catch (IllegalArgumentException e3) {
            Log.e(NativeProtocol.BRIDGE_ARG_ERROR_BUNDLE, "arguments error when get " + constantName);
        } catch (Exception e4) {
            Log.e(NativeProtocol.BRIDGE_ARG_ERROR_BUNDLE, "can not get constant" + constantName);
        }
        return t;
    }

    public static <T> T invokeInstanceMethod(Object instance, String methodName, Class[] parameterTypes, Object[] parameters) {
        Class aClass = instance.getClass();
        try {
            return aClass.getMethod(methodName, parameterTypes).invoke(instance, parameters);
        } catch (NoSuchMethodException e) {
            Log.e(NativeProtocol.BRIDGE_ARG_ERROR_BUNDLE, "can not find " + methodName + " in " + aClass.getName());
            return null;
        } catch (IllegalAccessException e2) {
            Log.e(NativeProtocol.BRIDGE_ARG_ERROR_BUNDLE, methodName + " is not accessible");
            return null;
        } catch (IllegalArgumentException e3) {
            Log.e(NativeProtocol.BRIDGE_ARG_ERROR_BUNDLE, "arguments are error when invoking " + methodName);
            return null;
        } catch (InvocationTargetException e4) {
            Log.e(NativeProtocol.BRIDGE_ARG_ERROR_BUNDLE, "an exception was thrown by the invoked method when invoking " + methodName);
            return null;
        }
    }
}
