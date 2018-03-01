package com.bugsnag.android;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.bugsnag.android.JsonStream.Streamable;
import com.facebook.share.internal.ShareConstants;
import com.unity3d.ads.metadata.MediationMetaData;
import cz.msebera.android.httpclient.cookie.ClientCookie;
import java.io.IOException;

class AppData implements Streamable {
    protected final String appName;
    private final Configuration config;
    protected final String guessedReleaseStage;
    protected final String packageName;
    protected final Integer versionCode;
    protected final String versionName;

    AppData(@NonNull Context appContext, @NonNull Configuration config) {
        this.config = config;
        this.packageName = getPackageName(appContext);
        this.appName = getAppName(appContext);
        this.versionCode = getVersionCode(appContext);
        this.versionName = getVersionName(appContext);
        this.guessedReleaseStage = guessReleaseStage(appContext);
    }

    public void toStream(@NonNull JsonStream writer) throws IOException {
        writer.beginObject();
        writer.name(ShareConstants.WEB_DIALOG_PARAM_ID).value(this.packageName);
        writer.name(MediationMetaData.KEY_NAME).value(this.appName);
        writer.name("packageName").value(this.packageName);
        writer.name("versionName").value(this.versionName);
        writer.name("versionCode").value(this.versionCode);
        writer.name("buildUUID").value(this.config.getBuildUUID());
        writer.name(ClientCookie.VERSION_ATTR).value(getAppVersion());
        writer.name("releaseStage").value(getReleaseStage());
        writer.endObject();
    }

    @NonNull
    public String getReleaseStage() {
        if (this.config.getReleaseStage() != null) {
            return this.config.getReleaseStage();
        }
        return this.guessedReleaseStage;
    }

    @Nullable
    public String getAppVersion() {
        if (this.config.getAppVersion() != null) {
            return this.config.getAppVersion();
        }
        return this.versionName;
    }

    @NonNull
    private static String getPackageName(Context appContext) {
        return appContext.getPackageName();
    }

    @Nullable
    private static String getAppName(Context appContext) {
        try {
            PackageManager packageManager = appContext.getPackageManager();
            return (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(appContext.getPackageName(), 0));
        } catch (NameNotFoundException e) {
            Logger.warn("Could not get app name");
            return null;
        }
    }

    @Nullable
    private static Integer getVersionCode(Context appContext) {
        try {
            return Integer.valueOf(appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0).versionCode);
        } catch (NameNotFoundException e) {
            Logger.warn("Could not get versionCode");
            return null;
        }
    }

    @Nullable
    private static String getVersionName(Context appContext) {
        try {
            return appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            Logger.warn("Could not get versionName");
            return null;
        }
    }

    @NonNull
    private static String guessReleaseStage(Context appContext) {
        try {
            if ((appContext.getPackageManager().getApplicationInfo(appContext.getPackageName(), 0).flags & 2) != 0) {
                return "development";
            }
        } catch (NameNotFoundException e) {
            Logger.warn("Could not get releaseStage");
        }
        return "production";
    }
}
