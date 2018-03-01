package org.cocos2dx.lib;

import java.util.ArrayList;

public class GameControllerAdapter {
    private static ArrayList<Runnable> sRunnableFrameStartList = null;

    private static native void nativeControllerAxisEvent(String str, int i, int i2, float f, boolean z);

    private static native void nativeControllerButtonEvent(String str, int i, int i2, boolean z, float f, boolean z2);

    private static native void nativeControllerConnected(String str, int i);

    private static native void nativeControllerDisconnected(String str, int i);

    public static void addRunnableToFrameStartList(Runnable runnable) {
        if (sRunnableFrameStartList == null) {
            sRunnableFrameStartList = new ArrayList();
        }
        sRunnableFrameStartList.add(runnable);
    }

    public static void removeRunnableFromFrameStartList(Runnable runnable) {
        if (sRunnableFrameStartList != null) {
            sRunnableFrameStartList.remove(runnable);
        }
    }

    public static void onDrawFrameStart() {
        if (sRunnableFrameStartList != null) {
            int size = sRunnableFrameStartList.size();
            for (int i = 0; i < size; i++) {
                ((Runnable) sRunnableFrameStartList.get(i)).run();
            }
        }
    }

    public static void onConnected(final String vendorName, final int controller) {
        Cocos2dxHelper.runOnGLThread(new Runnable() {
            public void run() {
                GameControllerAdapter.nativeControllerConnected(vendorName, controller);
            }
        });
    }

    public static void onDisconnected(final String vendorName, final int controller) {
        Cocos2dxHelper.runOnGLThread(new Runnable() {
            public void run() {
                GameControllerAdapter.nativeControllerDisconnected(vendorName, controller);
            }
        });
    }

    public static void onButtonEvent(String vendorName, int controller, int button, boolean isPressed, float value, boolean isAnalog) {
        final String str = vendorName;
        final int i = controller;
        final int i2 = button;
        final boolean z = isPressed;
        final float f = value;
        final boolean z2 = isAnalog;
        Cocos2dxHelper.runOnGLThread(new Runnable() {
            public void run() {
                GameControllerAdapter.nativeControllerButtonEvent(str, i, i2, z, f, z2);
            }
        });
    }

    public static void onAxisEvent(String vendorName, int controller, int axisID, float value, boolean isAnalog) {
        final String str = vendorName;
        final int i = controller;
        final int i2 = axisID;
        final float f = value;
        final boolean z = isAnalog;
        Cocos2dxHelper.runOnGLThread(new Runnable() {
            public void run() {
                GameControllerAdapter.nativeControllerAxisEvent(str, i, i2, f, z);
            }
        });
    }
}
