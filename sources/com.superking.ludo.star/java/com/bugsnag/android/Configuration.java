package com.bugsnag.android;

import android.support.annotation.NonNull;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

public class Configuration extends Observable implements Observer {
    static final String DEFAULT_ENDPOINT = "https://notify.bugsnag.com";
    private final String apiKey;
    private String appVersion;
    private final Collection<BeforeNotify> beforeNotifyTasks = new LinkedList();
    private String buildUUID;
    private String context;
    String defaultExceptionType = "android";
    private boolean enableExceptionHandler = true;
    private String endpoint = DEFAULT_ENDPOINT;
    private String[] filters = new String[]{"password"};
    private String[] ignoreClasses;
    private MetaData metaData;
    private String[] notifyReleaseStages = null;
    private boolean persistUserBetweenSessions = false;
    private String[] projectPackages;
    private String releaseStage;
    private boolean sendThreads = true;

    public Configuration(@NonNull String apiKey) {
        this.apiKey = apiKey;
        this.metaData = new MetaData();
        this.metaData.addObserver(this);
    }

    public String getApiKey() {
        return this.apiKey;
    }

    public String getAppVersion() {
        return this.appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
        notifyBugsnagObservers(NotifyType.APP);
    }

    public String getContext() {
        return this.context;
    }

    public void setContext(String context) {
        this.context = context;
        notifyBugsnagObservers(NotifyType.CONTEXT);
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getBuildUUID() {
        return this.buildUUID;
    }

    public void setBuildUUID(String buildUUID) {
        this.buildUUID = buildUUID;
        notifyBugsnagObservers(NotifyType.APP);
    }

    public String[] getFilters() {
        return this.filters;
    }

    public void setFilters(String[] filters) {
        this.filters = filters;
        this.metaData.setFilters(filters);
    }

    public String[] getIgnoreClasses() {
        return this.ignoreClasses;
    }

    public void setIgnoreClasses(String[] ignoreClasses) {
        this.ignoreClasses = ignoreClasses;
    }

    public String[] getNotifyReleaseStages() {
        return this.notifyReleaseStages;
    }

    public void setNotifyReleaseStages(String[] notifyReleaseStages) {
        this.notifyReleaseStages = notifyReleaseStages;
        notifyBugsnagObservers(NotifyType.RELEASE_STAGES);
    }

    public String[] getProjectPackages() {
        return this.projectPackages;
    }

    public void setProjectPackages(String[] projectPackages) {
        this.projectPackages = projectPackages;
    }

    public String getReleaseStage() {
        return this.releaseStage;
    }

    public void setReleaseStage(String releaseStage) {
        this.releaseStage = releaseStage;
        notifyBugsnagObservers(NotifyType.APP);
    }

    public boolean getSendThreads() {
        return this.sendThreads;
    }

    public void setSendThreads(boolean sendThreads) {
        this.sendThreads = sendThreads;
    }

    public boolean getEnableExceptionHandler() {
        return this.enableExceptionHandler;
    }

    public void setEnableExceptionHandler(boolean enableExceptionHandler) {
        this.enableExceptionHandler = enableExceptionHandler;
    }

    protected MetaData getMetaData() {
        return this.metaData;
    }

    protected void setMetaData(MetaData metaData) {
        this.metaData.deleteObserver(this);
        this.metaData = metaData;
        this.metaData.addObserver(this);
        notifyBugsnagObservers(NotifyType.META);
    }

    protected Collection<BeforeNotify> getBeforeNotifyTasks() {
        return this.beforeNotifyTasks;
    }

    public boolean getPersistUserBetweenSessions() {
        return this.persistUserBetweenSessions;
    }

    public void setPersistUserBetweenSessions(boolean persistUserBetweenSessions) {
        this.persistUserBetweenSessions = persistUserBetweenSessions;
    }

    protected boolean shouldNotifyForReleaseStage(String releaseStage) {
        if (this.notifyReleaseStages == null) {
            return true;
        }
        return Arrays.asList(this.notifyReleaseStages).contains(releaseStage);
    }

    protected boolean shouldIgnoreClass(String className) {
        if (this.ignoreClasses == null) {
            return false;
        }
        return Arrays.asList(this.ignoreClasses).contains(className);
    }

    protected void beforeNotify(BeforeNotify beforeNotify) {
        this.beforeNotifyTasks.add(beforeNotify);
    }

    protected boolean inProject(String className) {
        if (this.projectPackages == null) {
            return false;
        }
        for (String packageName : this.projectPackages) {
            if (packageName != null && className.startsWith(packageName)) {
                return true;
            }
        }
        return false;
    }

    private void notifyBugsnagObservers(NotifyType type) {
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
}
