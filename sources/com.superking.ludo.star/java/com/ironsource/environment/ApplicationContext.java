package com.ironsource.environment;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import java.io.File;
import org.cocos2dx.lib.BuildConfig;

public class ApplicationContext {
    public static String getPackageName(Context context) {
        return context.getPackageName();
    }

    public static int getAppOrientation(Activity a) {
        return a.getRequestedOrientation();
    }

    public static String getDiskCacheDirPath(Context context) {
        File internalFile = context.getCacheDir();
        if (internalFile != null) {
            return internalFile.getPath();
        }
        return null;
    }

    public static boolean isPermissionGranted(Context context, String permission) {
        return context.checkCallingOrSelfPermission(permission) == 0;
    }

    static PackageInfo getAppPackageInfo(Context context) throws NameNotFoundException {
        return context.getPackageManager().getPackageInfo(getPackageName(context), 0);
    }

    public static long getFirstInstallTime(Context context) {
        try {
            return getAppPackageInfo(context).firstInstallTime;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static long getLastUpdateTime(Context context) {
        try {
            return getAppPackageInfo(context).lastUpdateTime;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static String getApplicationVersionName(Context context) {
        try {
            return getAppPackageInfo(context).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return BuildConfig.FLAVOR;
        }
    }
}
