package com.bugsnag.android;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.bugsnag.android.JsonStream.Streamable;
import java.io.IOException;
import java.util.List;

class AppState implements Streamable {
    private static final Long startTime = Long.valueOf(SystemClock.elapsedRealtime());
    private final String activeScreen;
    private final Long duration = getDuration();
    private final Boolean inForeground;
    private final Boolean lowMemory;
    private final Long memoryUsage;

    static void init() {
    }

    AppState(@NonNull Context appContext) {
        this.inForeground = isInForeground(appContext);
        this.activeScreen = getActiveScreen(appContext);
        this.memoryUsage = getMemoryUsage();
        this.lowMemory = isLowMemory(appContext);
    }

    public void toStream(@NonNull JsonStream writer) throws IOException {
        writer.beginObject();
        writer.name("duration").value(this.duration);
        writer.name("inForeground").value(this.inForeground);
        writer.name("activeScreen").value(this.activeScreen);
        writer.name("memoryUsage").value(this.memoryUsage);
        writer.name("lowMemory").value(this.lowMemory);
        writer.endObject();
    }

    @Nullable
    public static String getActiveScreenClass(@Nullable String activeScreen) {
        if (activeScreen != null) {
            return activeScreen.substring(activeScreen.lastIndexOf(46) + 1);
        }
        return null;
    }

    @NonNull
    private static Long getMemoryUsage() {
        return Long.valueOf(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
    }

    @Nullable
    private static Boolean isLowMemory(Context appContext) {
        try {
            ActivityManager activityManager = (ActivityManager) appContext.getSystemService("activity");
            MemoryInfo memInfo = new MemoryInfo();
            activityManager.getMemoryInfo(memInfo);
            return Boolean.valueOf(memInfo.lowMemory);
        } catch (Exception e) {
            Logger.warn("Could not check lowMemory status");
            return null;
        }
    }

    @Nullable
    private static String getActiveScreen(Context appContext) {
        try {
            return ((RunningTaskInfo) ((ActivityManager) appContext.getSystemService("activity")).getRunningTasks(1).get(0)).topActivity.getClassName();
        } catch (Exception e) {
            Logger.warn("Could not get active screen information, we recommend granting the 'android.permission.GET_TASKS' permission");
            return null;
        }
    }

    @Nullable
    private static Boolean isInForeground(Context appContext) {
        try {
            List<RunningTaskInfo> tasks = ((ActivityManager) appContext.getSystemService("activity")).getRunningTasks(1);
            if (tasks.isEmpty()) {
                return Boolean.valueOf(false);
            }
            return Boolean.valueOf(((RunningTaskInfo) tasks.get(0)).topActivity.getPackageName().equalsIgnoreCase(appContext.getPackageName()));
        } catch (Exception e) {
            Logger.warn("Could not check if app is in the foreground, we recommend granting the 'android.permission.GET_TASKS' permission");
            return null;
        }
    }

    @NonNull
    private static Long getDuration() {
        return Long.valueOf(SystemClock.elapsedRealtime() - startTime.longValue());
    }
}
