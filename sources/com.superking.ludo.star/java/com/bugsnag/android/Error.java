package com.bugsnag.android;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.bugsnag.android.JsonStream.Streamable;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import java.io.IOException;

public class Error implements Streamable {
    private static final String PAYLOAD_VERSION = "3";
    private AppData appData;
    private AppState appState;
    private Breadcrumbs breadcrumbs;
    final Configuration config;
    private String context;
    private DeviceData deviceData;
    private DeviceState deviceState;
    private Throwable exception;
    private String groupingHash;
    private MetaData metaData = new MetaData();
    private Severity severity = Severity.WARNING;
    private User user;

    Error(Configuration config, Throwable exception) {
        this.config = config;
        this.exception = exception;
    }

    Error(Configuration config, String name, String message, StackTraceElement[] frames) {
        this.config = config;
        this.exception = new BugsnagException(name, message, frames);
    }

    public void toStream(@NonNull JsonStream writer) throws IOException {
        int i = 0;
        Streamable mergedMetaData = MetaData.merge(this.config.getMetaData(), this.metaData);
        writer.beginObject();
        writer.name("payloadVersion").value(PAYLOAD_VERSION);
        writer.name("context").value(getContext());
        writer.name("severity").value(this.severity);
        writer.name("metaData").value(mergedMetaData);
        if (this.config.getProjectPackages() != null) {
            writer.name("projectPackages").beginArray();
            String[] projectPackages = this.config.getProjectPackages();
            int length = projectPackages.length;
            while (i < length) {
                writer.value(projectPackages[i]);
                i++;
            }
            writer.endArray();
        }
        writer.name("exceptions").value(new Exceptions(this.config, this.exception));
        writer.name("user").value(this.user);
        writer.name("app").value(this.appData);
        writer.name("appState").value(this.appState);
        writer.name(ParametersKeys.ORIENTATION_DEVICE).value(this.deviceData);
        writer.name("deviceState").value(this.deviceState);
        writer.name("breadcrumbs").value(this.breadcrumbs);
        writer.name("groupingHash").value(this.groupingHash);
        if (this.config.getSendThreads()) {
            writer.name("threads").value(new ThreadState(this.config));
        }
        writer.endObject();
    }

    public void setContext(String context) {
        this.context = context;
    }

    @Nullable
    public String getContext() {
        if (this.context != null && !TextUtils.isEmpty(this.context)) {
            return this.context;
        }
        if (this.config.getContext() != null) {
            return this.config.getContext();
        }
        if (this.appState != null) {
            return AppState.getActiveScreenClass(this.context);
        }
        return null;
    }

    public void setGroupingHash(String groupingHash) {
        this.groupingHash = groupingHash;
    }

    public void setSeverity(Severity severity) {
        if (severity != null) {
            this.severity = severity;
        }
    }

    public Severity getSeverity() {
        return this.severity;
    }

    public void setUser(String id, String email, String name) {
        this.user = new User(id, email, name);
    }

    public User getUser() {
        return this.user;
    }

    public void setUserId(String id) {
        this.user = new User(this.user);
        this.user.setId(id);
    }

    public void setUserEmail(String email) {
        this.user = new User(this.user);
        this.user.setEmail(email);
    }

    public void setUserName(String name) {
        this.user = new User(this.user);
        this.user.setName(name);
    }

    public void addToTab(String tabName, String key, Object value) {
        this.metaData.addToTab(tabName, key, value);
    }

    public void clearTab(String tabName) {
        this.metaData.clearTab(tabName);
    }

    public MetaData getMetaData() {
        return this.metaData;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }

    public String getExceptionName() {
        if (this.exception instanceof BugsnagException) {
            return ((BugsnagException) this.exception).getName();
        }
        return this.exception.getClass().getName();
    }

    public String getExceptionMessage() {
        return this.exception.getLocalizedMessage();
    }

    public Throwable getException() {
        return this.exception;
    }

    void setAppData(AppData appData) {
        this.appData = appData;
    }

    void setDeviceData(DeviceData deviceData) {
        this.deviceData = deviceData;
    }

    void setAppState(AppState appState) {
        this.appState = appState;
    }

    void setDeviceState(DeviceState deviceState) {
        this.deviceState = deviceState;
    }

    void setUser(User user) {
        this.user = user;
    }

    void setBreadcrumbs(Breadcrumbs breadcrumbs) {
        this.breadcrumbs = breadcrumbs;
    }

    boolean shouldIgnoreClass() {
        return this.config.shouldIgnoreClass(getExceptionName());
    }
}
