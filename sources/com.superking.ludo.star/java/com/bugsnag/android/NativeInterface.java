package com.bugsnag.android;

import android.os.Build;
import android.os.Build.VERSION;
import java.util.HashMap;
import java.util.Map;
import java.util.Observer;

public class NativeInterface {
    private static Client client;

    private static Client getClient() {
        if (client != null) {
            return client;
        }
        return Bugsnag.getClient();
    }

    public static void setClient(Client client) {
        client = client;
        configureClientObservers(client);
    }

    public static void configureClientObservers(Client client) {
        try {
            client.addObserver((Observer) Class.forName("com.bugsnag.android.ndk.BugsnagObserver").newInstance());
        } catch (ClassNotFoundException e) {
            Logger.info("Failed to find NDK observer");
        } catch (InstantiationException e2) {
            Logger.warn("Failed to instantiate NDK observer", e2);
        } catch (IllegalAccessException e3) {
            Logger.warn("Could not access NDK observer", e3);
        }
        client.notifyBugsnagObservers(NotifyType.ALL);
    }

    public static String getContext() {
        return getClient().getContext();
    }

    public static String getErrorStorePath() {
        return getClient().errorStore.path;
    }

    public static String getUserId() {
        return getClient().user.getId();
    }

    public static String getUserEmail() {
        return getClient().user.getEmail();
    }

    public static String getUserName() {
        return getClient().user.getName();
    }

    public static String getPackageName() {
        return getClient().appData.packageName;
    }

    public static String getAppName() {
        return getClient().appData.appName;
    }

    public static String getVersionName() {
        return getClient().appData.versionName;
    }

    public static int getVersionCode() {
        return getClient().appData.versionCode.intValue();
    }

    public static String getBuildUUID() {
        return getClient().config.getBuildUUID();
    }

    public static String getAppVersion() {
        return getClient().appData.getAppVersion();
    }

    public static String getReleaseStage() {
        return getClient().appData.getReleaseStage();
    }

    public static String getDeviceId() {
        return getClient().deviceData.id;
    }

    public static String getDeviceLocale() {
        return getClient().deviceData.locale;
    }

    public static double getDeviceTotalMemory() {
        return (double) getClient().deviceData.totalMemory.longValue();
    }

    public static Boolean getDeviceRooted() {
        return getClient().deviceData.rooted;
    }

    public static float getDeviceScreenDensity() {
        return getClient().deviceData.screenDensity.floatValue();
    }

    public static int getDeviceDpi() {
        return getClient().deviceData.dpi.intValue();
    }

    public static String getDeviceScreenResolution() {
        return getClient().deviceData.screenResolution;
    }

    public static String getDeviceManufacturer() {
        return Build.MANUFACTURER;
    }

    public static String getDeviceBrand() {
        return Build.BRAND;
    }

    public static String getDeviceModel() {
        return Build.MODEL;
    }

    public static String getDeviceOsVersion() {
        return VERSION.RELEASE;
    }

    public static String getDeviceOsBuild() {
        return Build.DISPLAY;
    }

    public static int getDeviceApiLevel() {
        return VERSION.SDK_INT;
    }

    public static String[] getDeviceCpuAbi() {
        return getClient().deviceData.cpuAbi;
    }

    public static Map<String, Object> getMetaData() {
        return getClient().getMetaData().store;
    }

    public static Object[] getBreadcrumbs() {
        return getClient().breadcrumbs.store.toArray();
    }

    public static String[] getFilters() {
        return getClient().config.getFilters();
    }

    public static String[] getReleaseStages() {
        return getClient().config.getNotifyReleaseStages();
    }

    public static void setUser(String id, String email, String name) {
        getClient().setUserId(id, false);
        getClient().setUserEmail(email, false);
        getClient().setUserName(name, false);
    }

    public static void leaveBreadcrumb(String name, BreadcrumbType type) {
        getClient().leaveBreadcrumb(name, type, new HashMap(), false);
    }

    public static void addToTab(String tab, String key, Object value) {
        getClient().config.getMetaData().addToTab(tab, key, value, false);
    }

    public static void notify(String name, String message, final Severity severity, StackTraceElement[] stacktrace, final Map<String, Object> metaData) {
        getClient().notify(name, message, stacktrace, new Callback() {
            public void beforeNotify(Report report) {
                report.getError().setSeverity(severity);
                report.getError().config.defaultExceptionType = "c";
                for (String tab : metaData.keySet()) {
                    Map value = metaData.get(tab);
                    if (value instanceof Map) {
                        Map map = value;
                        for (Object key : map.keySet()) {
                            report.getError().getMetaData().addToTab(tab, key.toString(), map.get(key));
                        }
                    } else {
                        report.getError().getMetaData().addToTab("custom", tab, value);
                    }
                }
            }
        });
    }
}
