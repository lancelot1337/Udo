package com.bugsnag.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.facebook.share.internal.ShareConstants;
import io.branch.referral.R;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import org.cocos2dx.lib.Cocos2dxEditBox;
import org.cocos2dx.lib.Cocos2dxHandler;

public class Client extends Observable implements Observer {
    private static final boolean BLOCKING = true;
    private static final String SHARED_PREF_KEY = "com.bugsnag.android";
    private static final String USER_EMAIL_KEY = "user.email";
    private static final String USER_ID_KEY = "user.id";
    private static final String USER_NAME_KEY = "user.name";
    private final Context appContext;
    protected final AppData appData;
    final Breadcrumbs breadcrumbs;
    protected final Configuration config;
    protected final DeviceData deviceData;
    protected final ErrorStore errorStore;
    protected final User user;

    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$bugsnag$android$DeliveryStyle = new int[DeliveryStyle.values().length];

        static {
            try {
                $SwitchMap$com$bugsnag$android$DeliveryStyle[DeliveryStyle.SAME_THREAD.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$bugsnag$android$DeliveryStyle[DeliveryStyle.ASYNC.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$bugsnag$android$DeliveryStyle[DeliveryStyle.ASYNC_WITH_CACHE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    public Client(@NonNull Context androidContext) {
        this(androidContext, null, BLOCKING);
    }

    public Client(@NonNull Context androidContext, @Nullable String apiKey) {
        this(androidContext, apiKey, BLOCKING);
    }

    public Client(@NonNull Context androidContext, @Nullable String apiKey, boolean enableExceptionHandler) {
        this(androidContext, createNewConfiguration(androidContext, apiKey, enableExceptionHandler));
    }

    public Client(@NonNull Context androidContext, @NonNull Configuration configuration) {
        this.user = new User();
        this.appContext = androidContext.getApplicationContext();
        this.config = configuration;
        String buildUUID = null;
        try {
            buildUUID = this.appContext.getPackageManager().getApplicationInfo(this.appContext.getPackageName(), 128).metaData.getString("com.bugsnag.android.BUILD_UUID");
        } catch (Exception e) {
        }
        if (buildUUID != null) {
            this.config.setBuildUUID(buildUUID);
        }
        this.appData = new AppData(this.appContext, this.config);
        this.deviceData = new DeviceData(this.appContext);
        AppState.init();
        this.breadcrumbs = new Breadcrumbs();
        setProjectPackages(this.appContext.getPackageName());
        if (this.config.getPersistUserBetweenSessions()) {
            SharedPreferences sharedPref = this.appContext.getSharedPreferences(SHARED_PREF_KEY, 0);
            this.user.setId(sharedPref.getString(USER_ID_KEY, this.deviceData.getUserId()));
            this.user.setName(sharedPref.getString(USER_NAME_KEY, null));
            this.user.setEmail(sharedPref.getString(USER_EMAIL_KEY, null));
        } else {
            this.user.setId(this.deviceData.getUserId());
        }
        this.errorStore = new ErrorStore(this.config, this.appContext);
        if (this.config.getEnableExceptionHandler()) {
            enableExceptionHandler();
        }
        this.config.addObserver(this);
        this.errorStore.flush();
    }

    public void notifyBugsnagObservers(NotifyType type) {
        setChanged();
        super.notifyObservers(type.getValue());
    }

    public void update(Observable o, Object arg) {
        if (arg instanceof Integer) {
            NotifyType type = NotifyType.fromInt((Integer) arg);
            if (type != null) {
                notifyBugsnagObservers(type);
            }
        }
    }

    private static Configuration createNewConfiguration(@NonNull Context androidContext, String apiKey, boolean enableExceptionHandler) {
        Context appContext = androidContext.getApplicationContext();
        if (TextUtils.isEmpty(apiKey)) {
            try {
                apiKey = appContext.getPackageManager().getApplicationInfo(appContext.getPackageName(), 128).metaData.getString("com.bugsnag.android.API_KEY");
            } catch (Exception e) {
            }
        }
        if (apiKey == null) {
            throw new NullPointerException("You must provide a Bugsnag API key");
        }
        Configuration newConfig = new Configuration(apiKey);
        newConfig.setEnableExceptionHandler(enableExceptionHandler);
        return newConfig;
    }

    public void setAppVersion(String appVersion) {
        this.config.setAppVersion(appVersion);
    }

    public String getContext() {
        return this.config.getContext();
    }

    public void setContext(String context) {
        this.config.setContext(context);
    }

    public void setEndpoint(String endpoint) {
        this.config.setEndpoint(endpoint);
    }

    public void setBuildUUID(String buildUUID) {
        this.config.setBuildUUID(buildUUID);
    }

    public void setFilters(String... filters) {
        this.config.setFilters(filters);
    }

    public void setIgnoreClasses(String... ignoreClasses) {
        this.config.setIgnoreClasses(ignoreClasses);
    }

    public void setNotifyReleaseStages(String... notifyReleaseStages) {
        this.config.setNotifyReleaseStages(notifyReleaseStages);
    }

    public void setProjectPackages(String... projectPackages) {
        this.config.setProjectPackages(projectPackages);
    }

    public void setReleaseStage(String releaseStage) {
        this.config.setReleaseStage(releaseStage);
    }

    public void setSendThreads(boolean sendThreads) {
        this.config.setSendThreads(sendThreads);
    }

    public void setUser(String id, String email, String name) {
        setUserId(id);
        setUserEmail(email);
        setUserName(name);
    }

    public void clearUser() {
        this.user.setId(this.deviceData.getUserId());
        this.user.setEmail(null);
        this.user.setName(null);
        this.appContext.getSharedPreferences(SHARED_PREF_KEY, 0).edit().remove(USER_ID_KEY).remove(USER_EMAIL_KEY).remove(USER_NAME_KEY).commit();
        notifyBugsnagObservers(NotifyType.USER);
    }

    public void setUserId(String id) {
        setUserId(id, BLOCKING);
    }

    void setUserId(String id, boolean notify) {
        this.user.setId(id);
        if (this.config.getPersistUserBetweenSessions()) {
            storeInSharedPrefs(USER_ID_KEY, id);
        }
        if (notify) {
            notifyBugsnagObservers(NotifyType.USER);
        }
    }

    public void setUserEmail(String email) {
        setUserEmail(email, BLOCKING);
    }

    void setUserEmail(String email, boolean notify) {
        this.user.setEmail(email);
        if (this.config.getPersistUserBetweenSessions()) {
            storeInSharedPrefs(USER_EMAIL_KEY, email);
        }
        if (notify) {
            notifyBugsnagObservers(NotifyType.USER);
        }
    }

    public void setUserName(String name) {
        setUserName(name, BLOCKING);
    }

    void setUserName(String name, boolean notify) {
        this.user.setName(name);
        if (this.config.getPersistUserBetweenSessions()) {
            storeInSharedPrefs(USER_NAME_KEY, name);
        }
        if (notify) {
            notifyBugsnagObservers(NotifyType.USER);
        }
    }

    public void beforeNotify(BeforeNotify beforeNotify) {
        this.config.beforeNotify(beforeNotify);
    }

    public void notify(Throwable exception) {
        notify(new Error(this.config, exception), false);
    }

    public void notifyBlocking(Throwable exception) {
        notify(new Error(this.config, exception), (boolean) BLOCKING);
    }

    public void notify(Throwable exception, Callback callback) {
        notify(new Error(this.config, exception), DeliveryStyle.ASYNC, callback);
    }

    public void notifyBlocking(Throwable exception, Callback callback) {
        notify(new Error(this.config, exception), DeliveryStyle.SAME_THREAD, callback);
    }

    public void notify(String name, String message, StackTraceElement[] stacktrace, Callback callback) {
        notify(new Error(this.config, name, message, stacktrace), DeliveryStyle.ASYNC, callback);
    }

    public void notifyBlocking(String name, String message, StackTraceElement[] stacktrace, Callback callback) {
        notify(new Error(this.config, name, message, stacktrace), DeliveryStyle.SAME_THREAD, callback);
    }

    public void notify(Throwable exception, Severity severity) {
        Error error = new Error(this.config, exception);
        error.setSeverity(severity);
        notify(error, false);
    }

    public void notifyBlocking(Throwable exception, Severity severity) {
        Error error = new Error(this.config, exception);
        error.setSeverity(severity);
        notify(error, (boolean) BLOCKING);
    }

    public void addToTab(String tab, String key, Object value) {
        this.config.getMetaData().addToTab(tab, key, value);
    }

    public void clearTab(String tabName) {
        this.config.getMetaData().clearTab(tabName);
    }

    public MetaData getMetaData() {
        return this.config.getMetaData();
    }

    public void setMetaData(MetaData metaData) {
        this.config.setMetaData(metaData);
    }

    public void leaveBreadcrumb(String breadcrumb) {
        this.breadcrumbs.add(breadcrumb);
        notifyBugsnagObservers(NotifyType.BREADCRUMB);
    }

    public void leaveBreadcrumb(String name, BreadcrumbType type, Map<String, String> metadata) {
        leaveBreadcrumb(name, type, metadata, BLOCKING);
    }

    void leaveBreadcrumb(String name, BreadcrumbType type, Map<String, String> metadata, boolean notify) {
        this.breadcrumbs.add(name, type, metadata);
        if (notify) {
            notifyBugsnagObservers(NotifyType.BREADCRUMB);
        }
    }

    public void setMaxBreadcrumbs(int numBreadcrumbs) {
        this.breadcrumbs.setSize(numBreadcrumbs);
    }

    public void clearBreadcrumbs() {
        this.breadcrumbs.clear();
        notifyBugsnagObservers(NotifyType.BREADCRUMB);
    }

    public void enableExceptionHandler() {
        ExceptionHandler.enable(this);
    }

    public void disableExceptionHandler() {
        ExceptionHandler.disable(this);
    }

    private void notify(Error error, boolean blocking) {
        notify(error, blocking ? DeliveryStyle.SAME_THREAD : DeliveryStyle.ASYNC, null);
    }

    private void notify(Error error, DeliveryStyle style, Callback callback) {
        if (!error.shouldIgnoreClass() && this.config.shouldNotifyForReleaseStage(this.appData.getReleaseStage())) {
            error.setAppData(this.appData);
            error.setDeviceData(this.deviceData);
            error.setAppState(new AppState(this.appContext));
            error.setDeviceState(new DeviceState(this.appContext));
            error.setBreadcrumbs(this.breadcrumbs);
            error.setUser(this.user);
            if (runBeforeNotifyTasks(error)) {
                Report report = new Report(this.config.getApiKey(), error);
                if (callback != null) {
                    callback.beforeNotify(report);
                }
                switch (AnonymousClass2.$SwitchMap$com$bugsnag$android$DeliveryStyle[style.ordinal()]) {
                    case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                        deliver(report, error);
                        break;
                    case R.styleable.View_paddingStart /*2*/:
                        final Report finalReport = report;
                        final Error finalError = error;
                        Async.run(new Runnable() {
                            public void run() {
                                Client.this.deliver(finalReport, finalError);
                            }
                        });
                        break;
                    case Cocos2dxEditBox.kEndActionReturn /*3*/:
                        this.errorStore.write(error);
                        this.errorStore.flush();
                        break;
                }
                this.breadcrumbs.add(error.getExceptionName(), BreadcrumbType.ERROR, Collections.singletonMap(ShareConstants.WEB_DIALOG_PARAM_MESSAGE, error.getExceptionMessage()));
                return;
            }
            Logger.info("Skipping notification - beforeNotify task returned false");
        }
    }

    void deliver(Report report, Error error) {
        try {
            HttpClient.post(this.config.getEndpoint(), report);
            Logger.info(String.format(Locale.US, "Sent 1 new error to Bugsnag", new Object[0]));
        } catch (NetworkException e) {
            Logger.info("Could not send error(s) to Bugsnag, saving to disk to send later");
            this.errorStore.write(error);
        } catch (BadResponseException e2) {
            Logger.info("Bad response when sending data to Bugsnag");
        } catch (Exception e3) {
            Logger.warn("Problem sending error to Bugsnag", e3);
        }
    }

    void cacheAndNotify(Throwable exception, Severity severity) {
        Error error = new Error(this.config, exception);
        error.setSeverity(severity);
        notify(error, DeliveryStyle.ASYNC_WITH_CACHE, null);
    }

    private boolean runBeforeNotifyTasks(Error error) {
        for (BeforeNotify beforeNotify : this.config.getBeforeNotifyTasks()) {
            try {
                if (!beforeNotify.run(error)) {
                    return false;
                }
            } catch (Throwable ex) {
                Logger.warn("BeforeNotify threw an Exception", ex);
            }
        }
        return BLOCKING;
    }

    private boolean storeInSharedPrefs(String key, String value) {
        return this.appContext.getSharedPreferences(SHARED_PREF_KEY, 0).edit().putString(key, value).commit();
    }

    public void notify(Throwable exception, MetaData metaData) {
        Error error = new Error(this.config, exception);
        error.setMetaData(metaData);
        notify(error, false);
    }

    public void notifyBlocking(Throwable exception, MetaData metaData) {
        Error error = new Error(this.config, exception);
        error.setMetaData(metaData);
        notify(error, (boolean) BLOCKING);
    }

    @Deprecated
    public void notify(Throwable exception, Severity severity, MetaData metaData) {
        Error error = new Error(this.config, exception);
        error.setSeverity(severity);
        error.setMetaData(metaData);
        notify(error, false);
    }

    @Deprecated
    public void notifyBlocking(Throwable exception, Severity severity, MetaData metaData) {
        Error error = new Error(this.config, exception);
        error.setSeverity(severity);
        error.setMetaData(metaData);
        notify(error, (boolean) BLOCKING);
    }

    @Deprecated
    public void notify(String name, String message, StackTraceElement[] stacktrace, Severity severity, MetaData metaData) {
        Error error = new Error(this.config, name, message, stacktrace);
        error.setSeverity(severity);
        error.setMetaData(metaData);
        notify(error, false);
    }

    @Deprecated
    public void notifyBlocking(String name, String message, StackTraceElement[] stacktrace, Severity severity, MetaData metaData) {
        Error error = new Error(this.config, name, message, stacktrace);
        error.setSeverity(severity);
        error.setMetaData(metaData);
        notify(error, (boolean) BLOCKING);
    }

    @Deprecated
    public void notify(String name, String message, String context, StackTraceElement[] stacktrace, Severity severity, MetaData metaData) {
        Error error = new Error(this.config, name, message, stacktrace);
        error.setSeverity(severity);
        error.setMetaData(metaData);
        error.setContext(context);
        notify(error, false);
    }

    @Deprecated
    public void notifyBlocking(String name, String message, String context, StackTraceElement[] stacktrace, Severity severity, MetaData metaData) {
        Error error = new Error(this.config, name, message, stacktrace);
        error.setSeverity(severity);
        error.setMetaData(metaData);
        error.setContext(context);
        notify(error, (boolean) BLOCKING);
    }
}
