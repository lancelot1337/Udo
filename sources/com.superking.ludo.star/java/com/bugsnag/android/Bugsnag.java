package com.bugsnag.android;

import android.content.Context;
import java.util.Map;

public final class Bugsnag {
    static Client client;

    private Bugsnag() {
    }

    public static Client init(Context androidContext) {
        client = new Client(androidContext);
        NativeInterface.configureClientObservers(client);
        return client;
    }

    public static Client init(Context androidContext, String apiKey) {
        client = new Client(androidContext, apiKey);
        NativeInterface.configureClientObservers(client);
        return client;
    }

    public static Client init(Context androidContext, String apiKey, boolean enableExceptionHandler) {
        client = new Client(androidContext, apiKey, enableExceptionHandler);
        NativeInterface.configureClientObservers(client);
        return client;
    }

    public static Client init(Context androidContext, Configuration config) {
        client = new Client(androidContext, config);
        NativeInterface.configureClientObservers(client);
        return client;
    }

    public static void setAppVersion(String appVersion) {
        getClient().setAppVersion(appVersion);
    }

    public static String getContext() {
        return getClient().getContext();
    }

    public static void setContext(String context) {
        getClient().setContext(context);
    }

    @Deprecated
    public static void setEndpoint(String endpoint) {
        getClient().setEndpoint(endpoint);
    }

    public static void setBuildUUID(String buildUUID) {
        getClient().setBuildUUID(buildUUID);
    }

    public static void setFilters(String... filters) {
        getClient().setFilters(filters);
    }

    public static void setIgnoreClasses(String... ignoreClasses) {
        getClient().setIgnoreClasses(ignoreClasses);
    }

    public static void setNotifyReleaseStages(String... notifyReleaseStages) {
        getClient().setNotifyReleaseStages(notifyReleaseStages);
    }

    public static void setProjectPackages(String... projectPackages) {
        getClient().setProjectPackages(projectPackages);
    }

    public static void setReleaseStage(String releaseStage) {
        getClient().setReleaseStage(releaseStage);
    }

    public static void setSendThreads(boolean sendThreads) {
        getClient().setSendThreads(sendThreads);
    }

    public static void setUser(String id, String email, String name) {
        getClient().setUser(id, email, name);
    }

    public static void clearUser() {
        getClient().clearUser();
    }

    public static void setUserId(String id) {
        getClient().setUserId(id);
    }

    public static void setUserEmail(String email) {
        getClient().setUserEmail(email);
    }

    public static void setUserName(String name) {
        getClient().setUserName(name);
    }

    public static void beforeNotify(BeforeNotify beforeNotify) {
        getClient().beforeNotify(beforeNotify);
    }

    public static void notify(Throwable exception) {
        getClient().notify(exception);
    }

    public static void notify(Throwable exception, Callback callback) {
        getClient().notify(exception, callback);
    }

    public static void notify(String name, String message, StackTraceElement[] stacktrace, Callback callback) {
        getClient().notify(name, message, stacktrace, callback);
    }

    public static void notify(Throwable exception, Severity severity) {
        getClient().notify(exception, severity);
    }

    public static void notify(Throwable exception, final MetaData metaData) {
        getClient().notify(exception, new Callback() {
            public void beforeNotify(Report report) {
                report.getError().setMetaData(metaData);
            }
        });
    }

    @Deprecated
    public static void notify(Throwable exception, final Severity severity, final MetaData metaData) {
        getClient().notify(exception, new Callback() {
            public void beforeNotify(Report report) {
                report.getError().setSeverity(severity);
                report.getError().setMetaData(metaData);
            }
        });
    }

    @Deprecated
    public static void notify(String name, String message, StackTraceElement[] stacktrace, Severity severity, MetaData metaData) {
        final Severity finalSeverity = severity;
        final MetaData finalMetaData = metaData;
        getClient().notify(name, message, stacktrace, new Callback() {
            public void beforeNotify(Report report) {
                report.getError().setSeverity(finalSeverity);
                report.getError().setMetaData(finalMetaData);
            }
        });
    }

    @Deprecated
    public static void notify(String name, String message, String context, StackTraceElement[] stacktrace, Severity severity, MetaData metaData) {
        final String finalContext = context;
        final Severity finalSeverity = severity;
        final MetaData finalMetaData = metaData;
        getClient().notify(name, message, stacktrace, new Callback() {
            public void beforeNotify(Report report) {
                report.getError().setSeverity(finalSeverity);
                report.getError().setMetaData(finalMetaData);
                report.getError().setContext(finalContext);
            }
        });
    }

    public static void addToTab(String tab, String key, Object value) {
        getClient().addToTab(tab, key, value);
    }

    public static void clearTab(String tabName) {
        getClient().clearTab(tabName);
    }

    public static MetaData getMetaData() {
        return getClient().getMetaData();
    }

    public static void setMetaData(MetaData metaData) {
        getClient().setMetaData(metaData);
    }

    public static void leaveBreadcrumb(String message) {
        getClient().leaveBreadcrumb(message);
    }

    public static void leaveBreadcrumb(String name, BreadcrumbType type, Map<String, String> metadata) {
        getClient().leaveBreadcrumb(name, type, metadata);
    }

    public static void setMaxBreadcrumbs(int numBreadcrumbs) {
        getClient().setMaxBreadcrumbs(numBreadcrumbs);
    }

    public static void clearBreadcrumbs() {
        getClient().clearBreadcrumbs();
    }

    public static void enableExceptionHandler() {
        getClient().enableExceptionHandler();
    }

    public static void disableExceptionHandler() {
        getClient().disableExceptionHandler();
    }

    public static Client getClient() {
        if (client != null) {
            return client;
        }
        throw new IllegalStateException("You must call Bugsnag.init before any other Bugsnag methods");
    }
}
