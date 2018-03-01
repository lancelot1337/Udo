package com.unity3d.ads.api;

import com.ironsource.environment.ConnectivityService;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import com.unity3d.ads.device.Device;
import com.unity3d.ads.device.DeviceError;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.webview.bridge.WebViewCallback;
import com.unity3d.ads.webview.bridge.WebViewExposed;
import cz.msebera.android.httpclient.entity.ContentLengthStrategy;
import cz.msebera.httpclient.android.BuildConfig;
import io.branch.referral.R;
import java.io.File;
import java.util.Locale;
import java.util.TimeZone;
import org.cocos2dx.lib.Cocos2dxHandler;
import org.json.JSONArray;

public class DeviceInfo {

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$unity3d$ads$api$DeviceInfo$StorageType = new int[StorageType.values().length];

        static {
            try {
                $SwitchMap$com$unity3d$ads$api$DeviceInfo$StorageType[StorageType.INTERNAL.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$unity3d$ads$api$DeviceInfo$StorageType[StorageType.EXTERNAL.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public enum StorageType {
        EXTERNAL,
        INTERNAL
    }

    @WebViewExposed
    public static void getAndroidId(WebViewCallback callback) {
        callback.invoke(Device.getAndroidId());
    }

    @WebViewExposed
    public static void getAdvertisingTrackingId(WebViewCallback callback) {
        callback.invoke(Device.getAdvertisingTrackingId());
    }

    @WebViewExposed
    public static void getLimitAdTrackingFlag(WebViewCallback callback) {
        callback.invoke(Boolean.valueOf(Device.isLimitAdTrackingEnabled()));
    }

    @WebViewExposed
    public static void getApiLevel(WebViewCallback callback) {
        callback.invoke(Integer.valueOf(Device.getApiLevel()));
    }

    @WebViewExposed
    public static void getOsVersion(WebViewCallback callback) {
        callback.invoke(Device.getOsVersion());
    }

    @WebViewExposed
    public static void getManufacturer(WebViewCallback callback) {
        callback.invoke(Device.getManufacturer());
    }

    @WebViewExposed
    public static void getModel(WebViewCallback callback) {
        callback.invoke(Device.getModel());
    }

    @WebViewExposed
    public static void getScreenLayout(WebViewCallback callback) {
        callback.invoke(Integer.valueOf(Device.getScreenLayout()));
    }

    @WebViewExposed
    public static void getScreenDensity(WebViewCallback callback) {
        callback.invoke(Integer.valueOf(Device.getScreenDensity()));
    }

    @WebViewExposed
    public static void getScreenWidth(WebViewCallback callback) {
        callback.invoke(Integer.valueOf(Device.getScreenWidth()));
    }

    @WebViewExposed
    public static void getScreenHeight(WebViewCallback callback) {
        callback.invoke(Integer.valueOf(Device.getScreenHeight()));
    }

    @WebViewExposed
    public static void getTimeZone(Boolean dst, WebViewCallback callback) {
        callback.invoke(TimeZone.getDefault().getDisplayName(dst.booleanValue(), 0, Locale.US));
    }

    @WebViewExposed
    public static void getConnectionType(WebViewCallback callback) {
        String connectionType;
        if (Device.isUsingWifi()) {
            connectionType = ConnectivityService.NETWORK_TYPE_WIFI;
        } else if (Device.isActiveNetworkConnected()) {
            connectionType = "cellular";
        } else {
            connectionType = ParametersKeys.ORIENTATION_NONE;
        }
        callback.invoke(connectionType);
    }

    @WebViewExposed
    public static void getNetworkType(WebViewCallback callback) {
        callback.invoke(Integer.valueOf(Device.getNetworkType()));
    }

    @WebViewExposed
    public static void getNetworkOperator(WebViewCallback callback) {
        callback.invoke(Device.getNetworkOperator());
    }

    @WebViewExposed
    public static void getNetworkOperatorName(WebViewCallback callback) {
        callback.invoke(Device.getNetworkOperatorName());
    }

    @WebViewExposed
    public static void isAppInstalled(String pkgname, WebViewCallback callback) {
        callback.invoke(Boolean.valueOf(Device.isAppInstalled(pkgname)));
    }

    @WebViewExposed
    public static void isRooted(WebViewCallback callback) {
        callback.invoke(Boolean.valueOf(Device.isRooted()));
    }

    @WebViewExposed
    public static void getInstalledPackages(boolean md5, WebViewCallback callback) {
        callback.invoke(new JSONArray(Device.getInstalledPackages(md5)));
    }

    @WebViewExposed
    public static void getUniqueEventId(WebViewCallback callback) {
        callback.invoke(Device.getUniqueEventId());
    }

    @WebViewExposed
    public static void getHeadset(WebViewCallback callback) {
        callback.invoke(Boolean.valueOf(Device.isWiredHeadsetOn()));
    }

    @WebViewExposed
    public static void getSystemProperty(String propertyName, String defaultValue, WebViewCallback callback) {
        callback.invoke(Device.getSystemProperty(propertyName, defaultValue));
    }

    @WebViewExposed
    public static void getRingerMode(WebViewCallback callback) {
        int ringerMode = Device.getRingerMode();
        if (ringerMode > -1) {
            callback.invoke(Integer.valueOf(ringerMode));
            return;
        }
        switch (ringerMode) {
            case ContentLengthStrategy.CHUNKED /*-2*/:
                callback.error(DeviceError.AUDIOMANAGER_NULL, Integer.valueOf(ringerMode));
                return;
            case BuildConfig.VERSION_CODE /*-1*/:
                callback.error(DeviceError.APPLICATION_CONTEXT_NULL, Integer.valueOf(ringerMode));
                return;
            default:
                DeviceLog.error("Unhandled ringerMode error: " + ringerMode);
                return;
        }
    }

    @WebViewExposed
    public static void getSystemLanguage(WebViewCallback callback) {
        callback.invoke(Locale.getDefault().toString());
    }

    @WebViewExposed
    public static void getDeviceVolume(Integer streamType, WebViewCallback callback) {
        callback.invoke(Integer.valueOf(Device.getStreamVolume(streamType.intValue())));
    }

    @WebViewExposed
    public static void getScreenBrightness(WebViewCallback callback) {
        int screenBrightness = Device.getScreenBrightness();
        if (screenBrightness > -1) {
            callback.invoke(Integer.valueOf(screenBrightness));
            return;
        }
        switch (screenBrightness) {
            case BuildConfig.VERSION_CODE /*-1*/:
                callback.error(DeviceError.APPLICATION_CONTEXT_NULL, Integer.valueOf(screenBrightness));
                return;
            default:
                DeviceLog.error("Unhandled screenBrightness error: " + screenBrightness);
                return;
        }
    }

    private static StorageType getStorageTypeFromString(String storageType) {
        try {
            return StorageType.valueOf(storageType);
        } catch (IllegalArgumentException e) {
            DeviceLog.exception("Illegal argument: " + storageType, e);
            return null;
        }
    }

    private static File getFileForStorageType(StorageType storageType) {
        switch (AnonymousClass1.$SwitchMap$com$unity3d$ads$api$DeviceInfo$StorageType[storageType.ordinal()]) {
            case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                return ClientProperties.getApplicationContext().getCacheDir();
            case R.styleable.View_paddingStart /*2*/:
                return ClientProperties.getApplicationContext().getExternalCacheDir();
            default:
                DeviceLog.error("Unhandled storagetype: " + storageType);
                return null;
        }
    }

    @WebViewExposed
    public static void getFreeSpace(String storageType, WebViewCallback callback) {
        StorageType storage = getStorageTypeFromString(storageType);
        if (storage == null) {
            callback.error(DeviceError.INVALID_STORAGETYPE, storageType);
            return;
        }
        if (Device.getFreeSpace(getFileForStorageType(storage)) > -1) {
            callback.invoke(Long.valueOf(Device.getFreeSpace(getFileForStorageType(storage))));
            return;
        }
        callback.error(DeviceError.COULDNT_GET_STORAGE_LOCATION, Long.valueOf(space));
    }

    @WebViewExposed
    public static void getTotalSpace(String storageType, WebViewCallback callback) {
        StorageType storage = getStorageTypeFromString(storageType);
        if (storage == null) {
            callback.error(DeviceError.INVALID_STORAGETYPE, storageType);
            return;
        }
        if (Device.getTotalSpace(getFileForStorageType(storage)) > -1) {
            callback.invoke(Long.valueOf(Device.getTotalSpace(getFileForStorageType(storage))));
            return;
        }
        callback.error(DeviceError.COULDNT_GET_STORAGE_LOCATION, Long.valueOf(space));
    }

    @WebViewExposed
    public static void getBatteryLevel(WebViewCallback callback) {
        callback.invoke(Float.valueOf(Device.getBatteryLevel()));
    }

    @WebViewExposed
    public static void getBatteryStatus(WebViewCallback callback) {
        callback.invoke(Integer.valueOf(Device.getBatteryStatus()));
    }

    @WebViewExposed
    public static void getFreeMemory(WebViewCallback callback) {
        callback.invoke(Long.valueOf(Device.getFreeMemory()));
    }

    @WebViewExposed
    public static void getTotalMemory(WebViewCallback callback) {
        callback.invoke(Long.valueOf(Device.getTotalMemory()));
    }
}
