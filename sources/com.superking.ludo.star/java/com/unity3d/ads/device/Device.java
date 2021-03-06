package com.unity3d.ads.device;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Build.VERSION;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import com.google.firebase.analytics.FirebaseAnalytics.Param;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.metadata.MediationMetaData;
import com.unity3d.ads.misc.Utilities;
import com.unity3d.ads.properties.ClientProperties;
import io.branch.referral.R;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cocos2dx.lib.BuildConfig;
import org.cocos2dx.lib.Cocos2dxHandler;

public class Device {

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$unity3d$ads$device$Device$MemoryInfoType = new int[MemoryInfoType.values().length];

        static {
            try {
                $SwitchMap$com$unity3d$ads$device$Device$MemoryInfoType[MemoryInfoType.TOTAL_MEMORY.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$unity3d$ads$device$Device$MemoryInfoType[MemoryInfoType.FREE_MEMORY.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public enum MemoryInfoType {
        TOTAL_MEMORY,
        FREE_MEMORY
    }

    public static int getApiLevel() {
        return VERSION.SDK_INT;
    }

    public static String getOsVersion() {
        return VERSION.RELEASE;
    }

    public static String getManufacturer() {
        return Build.MANUFACTURER;
    }

    public static String getModel() {
        return Build.MODEL;
    }

    public static int getScreenLayout() {
        if (ClientProperties.getApplicationContext() != null) {
            return ClientProperties.getApplicationContext().getResources().getConfiguration().screenLayout;
        }
        return -1;
    }

    @SuppressLint({"DefaultLocale"})
    public static String getAndroidId() {
        String androidID = null;
        try {
            androidID = Secure.getString(ClientProperties.getApplicationContext().getContentResolver(), "android_id");
        } catch (Exception e) {
            DeviceLog.exception("Problems fetching androidId", e);
        }
        return androidID;
    }

    public static String getAdvertisingTrackingId() {
        return AdvertisingId.getAdvertisingTrackingId();
    }

    public static boolean isLimitAdTrackingEnabled() {
        return AdvertisingId.getLimitedAdTracking();
    }

    public static boolean isUsingWifi() {
        boolean z = true;
        if (ClientProperties.getApplicationContext() == null) {
            return false;
        }
        ConnectivityManager mConnectivity = (ConnectivityManager) ClientProperties.getApplicationContext().getSystemService("connectivity");
        if (mConnectivity == null) {
            return false;
        }
        TelephonyManager mTelephony = (TelephonyManager) ClientProperties.getApplicationContext().getSystemService("phone");
        NetworkInfo info = mConnectivity.getActiveNetworkInfo();
        if (info == null || !mConnectivity.getBackgroundDataSetting() || !mConnectivity.getActiveNetworkInfo().isConnected() || mTelephony == null) {
            return false;
        }
        if (!(info.getType() == 1 && info.isConnected())) {
            z = false;
        }
        return z;
    }

    public static int getNetworkType() {
        if (ClientProperties.getApplicationContext() != null) {
            return ((TelephonyManager) ClientProperties.getApplicationContext().getSystemService("phone")).getNetworkType();
        }
        return -1;
    }

    public static String getNetworkOperator() {
        if (ClientProperties.getApplicationContext() != null) {
            return ((TelephonyManager) ClientProperties.getApplicationContext().getSystemService("phone")).getNetworkOperator();
        }
        return BuildConfig.FLAVOR;
    }

    public static String getNetworkOperatorName() {
        if (ClientProperties.getApplicationContext() != null) {
            return ((TelephonyManager) ClientProperties.getApplicationContext().getSystemService("phone")).getNetworkOperatorName();
        }
        return BuildConfig.FLAVOR;
    }

    public static int getScreenDensity() {
        if (ClientProperties.getApplicationContext() != null) {
            return ClientProperties.getApplicationContext().getResources().getDisplayMetrics().densityDpi;
        }
        return -1;
    }

    public static int getScreenWidth() {
        if (ClientProperties.getApplicationContext() != null) {
            return ClientProperties.getApplicationContext().getResources().getDisplayMetrics().widthPixels;
        }
        return -1;
    }

    public static int getScreenHeight() {
        if (ClientProperties.getApplicationContext() != null) {
            return ClientProperties.getApplicationContext().getResources().getDisplayMetrics().heightPixels;
        }
        return -1;
    }

    public static boolean isActiveNetworkConnected() {
        if (ClientProperties.getApplicationContext() == null) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) ClientProperties.getApplicationContext().getSystemService("connectivity");
        if (cm == null) {
            return false;
        }
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null || !activeNetwork.isConnected()) {
            return false;
        }
        return true;
    }

    public static boolean isAppInstalled(String pkgname) {
        if (ClientProperties.getApplicationContext() == null) {
            return false;
        }
        try {
            PackageInfo pkgInfo = ClientProperties.getApplicationContext().getPackageManager().getPackageInfo(pkgname, 0);
            if (pkgInfo == null || pkgInfo.packageName == null || !pkgname.equals(pkgInfo.packageName)) {
                return false;
            }
            return true;
        } catch (NameNotFoundException e) {
            DeviceLog.exception("Couldn't find package: " + pkgname, e);
            return false;
        }
    }

    public static List<Map<String, Object>> getInstalledPackages(boolean hash) {
        List<Map<String, Object>> returnList = new ArrayList();
        if (ClientProperties.getApplicationContext() != null) {
            PackageManager pm = ClientProperties.getApplicationContext().getPackageManager();
            for (PackageInfo pkg : pm.getInstalledPackages(0)) {
                HashMap<String, Object> packageEntry = new HashMap();
                if (hash) {
                    packageEntry.put(MediationMetaData.KEY_NAME, Utilities.Sha256(pkg.packageName));
                } else {
                    packageEntry.put(MediationMetaData.KEY_NAME, pkg.packageName);
                }
                if (pkg.firstInstallTime > 0) {
                    packageEntry.put("time", Long.valueOf(pkg.firstInstallTime));
                }
                String installer = pm.getInstallerPackageName(pkg.packageName);
                if (!(installer == null || installer.isEmpty())) {
                    packageEntry.put("installer", installer);
                }
                returnList.add(packageEntry);
            }
        }
        return returnList;
    }

    public static String getUniqueEventId() {
        return UUID.randomUUID().toString();
    }

    public static boolean isWiredHeadsetOn() {
        if (ClientProperties.getApplicationContext() != null) {
            return ((AudioManager) ClientProperties.getApplicationContext().getSystemService("audio")).isWiredHeadsetOn();
        }
        return false;
    }

    public static String getSystemProperty(String propertyName, String defaultValue) {
        if (defaultValue != null) {
            return System.getProperty(propertyName, defaultValue);
        }
        return System.getProperty(propertyName);
    }

    public static int getRingerMode() {
        if (ClientProperties.getApplicationContext() == null) {
            return -1;
        }
        AudioManager am = (AudioManager) ClientProperties.getApplicationContext().getSystemService("audio");
        if (am != null) {
            return am.getRingerMode();
        }
        return -2;
    }

    public static int getStreamVolume(int streamType) {
        if (ClientProperties.getApplicationContext() == null) {
            return -1;
        }
        AudioManager am = (AudioManager) ClientProperties.getApplicationContext().getSystemService("audio");
        if (am != null) {
            return am.getStreamVolume(streamType);
        }
        return -2;
    }

    public static int getScreenBrightness() {
        if (ClientProperties.getApplicationContext() != null) {
            return System.getInt(ClientProperties.getApplicationContext().getContentResolver(), "screen_brightness", -1);
        }
        return -1;
    }

    public static long getFreeSpace(File file) {
        if (file == null || !file.exists()) {
            return -1;
        }
        return (long) Math.round((float) (file.getFreeSpace() / 1024));
    }

    public static long getTotalSpace(File file) {
        if (file == null || !file.exists()) {
            return -1;
        }
        return (long) Math.round((float) (file.getTotalSpace() / 1024));
    }

    public static float getBatteryLevel() {
        if (ClientProperties.getApplicationContext() != null) {
            Intent i = ClientProperties.getApplicationContext().registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
            if (i != null) {
                return ((float) i.getIntExtra(Param.LEVEL, -1)) / ((float) i.getIntExtra("scale", -1));
            }
        }
        return -1.0f;
    }

    public static int getBatteryStatus() {
        if (ClientProperties.getApplicationContext() == null) {
            return -1;
        }
        Intent i = ClientProperties.getApplicationContext().registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        if (i != null) {
            return i.getIntExtra(ParametersKeys.VIDEO_STATUS, -1);
        }
        return -1;
    }

    public static long getTotalMemory() {
        return getMemoryInfo(MemoryInfoType.TOTAL_MEMORY);
    }

    public static long getFreeMemory() {
        return getMemoryInfo(MemoryInfoType.FREE_MEMORY);
    }

    private static long getMemoryInfo(MemoryInfoType infoType) {
        IOException e;
        Throwable th;
        int lineNumber = -1;
        switch (AnonymousClass1.$SwitchMap$com$unity3d$ads$device$Device$MemoryInfoType[infoType.ordinal()]) {
            case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                lineNumber = 1;
                break;
            case R.styleable.View_paddingStart /*2*/:
                lineNumber = 2;
                break;
        }
        RandomAccessFile reader = null;
        String line = null;
        try {
            RandomAccessFile reader2 = new RandomAccessFile("/proc/meminfo", "r");
            int i = 0;
            while (i < lineNumber) {
                try {
                    line = reader2.readLine();
                    i++;
                } catch (IOException e2) {
                    e = e2;
                    reader = reader2;
                } catch (Throwable th2) {
                    th = th2;
                    reader = reader2;
                }
            }
            long memoryValueFromString = getMemoryValueFromString(line);
            try {
                reader2.close();
            } catch (IOException e3) {
                DeviceLog.exception("Error closing RandomAccessFile", e3);
            }
            reader = reader2;
            return memoryValueFromString;
        } catch (IOException e4) {
            e3 = e4;
            try {
                DeviceLog.exception("Error while reading memory info: " + infoType, e3);
                try {
                    reader.close();
                } catch (IOException e32) {
                    DeviceLog.exception("Error closing RandomAccessFile", e32);
                }
                return -1;
            } catch (Throwable th3) {
                th = th3;
                try {
                    reader.close();
                } catch (IOException e322) {
                    DeviceLog.exception("Error closing RandomAccessFile", e322);
                }
                throw th;
            }
        }
    }

    private static long getMemoryValueFromString(String memVal) {
        if (memVal == null) {
            return -1;
        }
        Matcher m = Pattern.compile("(\\d+)").matcher(memVal);
        String value = BuildConfig.FLAVOR;
        while (m.find()) {
            value = m.group(1);
        }
        return Long.parseLong(value);
    }

    public static boolean isRooted() {
        try {
            return searchPathForBinary("su");
        } catch (Exception e) {
            DeviceLog.exception("Rooted check failed", e);
            return false;
        }
    }

    private static boolean searchPathForBinary(String binary) {
        for (String path : System.getenv("PATH").split(":")) {
            File pathDir = new File(path);
            if (pathDir.exists() && pathDir.isDirectory()) {
                File[] pathDirFiles = pathDir.listFiles();
                if (pathDirFiles != null) {
                    for (File fileInPath : pathDirFiles) {
                        if (fileInPath.getName().equals(binary)) {
                            return true;
                        }
                    }
                    continue;
                } else {
                    continue;
                }
            }
        }
        return false;
    }
}
