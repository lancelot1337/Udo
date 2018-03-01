package com.ironsource.mediationsdk.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.google.firebase.analytics.FirebaseAnalytics.Param;
import com.ironsource.environment.DeviceStatus;
import com.ironsource.mediationsdk.IronSourceObject;
import com.ironsource.mediationsdk.config.ConfigFile;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.sdk.GeneralProperties;
import com.ironsource.mediationsdk.utils.IronSourceConstants.Gender;
import com.ironsource.sdk.utils.Constants;
import com.ironsource.sdk.utils.Constants.RequestParameters;
import io.branch.referral.R;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import org.cocos2dx.lib.BuildConfig;
import org.cocos2dx.lib.Cocos2dxEditBox;
import org.cocos2dx.lib.Cocos2dxHandler;
import org.cocos2dx.lib.GameControllerDelegate;

public class GeneralPropertiesWorker implements Runnable {
    private static final String CONNECTION_CELLULAR = "MOBILE";
    private static final String CONNECTION_CELLULAR_2G = "2g";
    private static final String CONNECTION_CELLULAR_3G = "3g";
    private static final String CONNECTION_CELLULAR_4G_LTE = "4g/lte";
    private static final String CONNECTION_ETHERNET = "ETHERNET";
    private static final String CONNECTION_ETHERNET_INT = "ethernet";
    private static final String CONNECTION_NONE_INT = "none";
    private static final String CONNECTION_WIFI = "WIFI";
    private static final String CONNECTION_WIFI_INT = "wifi";
    private static final String CONNECTION_WIMAX = "WIMAX";
    private static final String CONNECTION_WIMAX_INT = "wimax";
    private static final int MAX_MINUTES_OFFSET = 840;
    private static final int MINUTES_OFFSET_STEP = 15;
    private static final int MIN_MINUTES_OFFSET = -720;
    public static final String SDK_VERSION = "sdkVersion";
    private static final String UUID_TYPE = "UUID";
    private final String ADVERTISING_ID = "advertisingId";
    private final String ADVERTISING_ID_IS_LIMIT_TRACKING = RequestParameters.isLAT;
    private final String ADVERTISING_ID_TYPE = "advertisingIdType";
    private final String ANDROID_OS_VERSION = "osVersion";
    private final String APPLICATION_KEY = ServerResponseWrapper.APP_KEY_FIELD;
    private final String BATTERY_LEVEL = "battery";
    private final String BUNDLE_ID = RequestParameters.PACKAGE_NAME;
    private final String CONNECTION_TYPE = RequestParameters.CONNECTION_TYPE;
    private final String DEVICE_MODEL = RequestParameters.DEVICE_MODEL;
    private final String DEVICE_OEM = RequestParameters.DEVICE_OEM;
    private final String DEVICE_OS = "deviceOS";
    private final String EXTERNAL_FREE_MEMORY = "externalFreeMemory";
    private final String GMT_MINUTES_OFFSET = "gmtMinutesOffset";
    private final String INTERNAL_FREE_MEMORY = "internalFreeMemory";
    private final String KEY_IS_ROOT = "jb";
    private final String KEY_PLUGIN_FW_VERSION = "plugin_fw_v";
    private final String KEY_PLUGIN_TYPE = "pluginType";
    private final String KEY_PLUGIN_VERSION = "pluginVersion";
    private final String KEY_SESSION_ID = "sessionId";
    private final String LANGUAGE = "language";
    private final String LOCATION_LAT = "lat";
    private final String LOCATION_LON = "lon";
    private final String MEDIATION_TYPE = "mt";
    private final String MOBILE_CARRIER = RequestParameters.MOBILE_CARRIER;
    private final String PUBLISHER_APP_VERSION = RequestParameters.APPLICATION_VERSION_NAME;
    private final String TAG = getClass().getSimpleName();
    private Context mContext;

    private GeneralPropertiesWorker() {
    }

    public GeneralPropertiesWorker(Context ctx) {
        this.mContext = ctx.getApplicationContext();
    }

    public void run() {
        try {
            GeneralProperties.getProperties().putKeys(collectInformation());
            IronSourceUtils.saveGeneralProperties(this.mContext, GeneralProperties.getProperties().toJSON());
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceTag.NATIVE, "Thread name = " + getClass().getSimpleName(), e);
        }
    }

    private Map<String, Object> collectInformation() {
        Map<String, Object> result = new HashMap();
        String strVal = generateUUID();
        if (!TextUtils.isEmpty(strVal)) {
            result.put("sessionId", strVal);
        }
        strVal = getBundleId();
        if (!TextUtils.isEmpty(strVal)) {
            result.put(RequestParameters.PACKAGE_NAME, strVal);
            String publAppVersion = getPublisherApplicationVersion(strVal);
            if (!TextUtils.isEmpty(publAppVersion)) {
                result.put(RequestParameters.APPLICATION_VERSION_NAME, publAppVersion);
            }
        }
        result.put(ServerResponseWrapper.APP_KEY_FIELD, getApplicationKey());
        String advertisingId = BuildConfig.FLAVOR;
        String advertisingIdType = BuildConfig.FLAVOR;
        boolean isLimitAdTrackingEnabled = false;
        try {
            String[] advertisingIdInfo = DeviceStatus.getAdvertisingIdInfo(this.mContext);
            if (advertisingIdInfo != null && advertisingIdInfo.length == 2) {
                if (!TextUtils.isEmpty(advertisingIdInfo[0])) {
                    advertisingId = advertisingIdInfo[0];
                }
                isLimitAdTrackingEnabled = Boolean.valueOf(advertisingIdInfo[1]).booleanValue();
            }
        } catch (Exception e) {
        }
        if (TextUtils.isEmpty(advertisingId)) {
            advertisingId = DeviceStatus.getOrGenerateOnceUniqueIdentifier(this.mContext);
            if (!TextUtils.isEmpty(advertisingId)) {
                advertisingIdType = UUID_TYPE;
            }
        } else {
            advertisingIdType = IronSourceConstants.TYPE_GAID;
        }
        if (!TextUtils.isEmpty(advertisingId)) {
            result.put("advertisingId", advertisingId);
            result.put("advertisingIdType", advertisingIdType);
            result.put(RequestParameters.isLAT, Boolean.valueOf(isLimitAdTrackingEnabled));
        }
        result.put("deviceOS", getDeviceOS());
        if (!TextUtils.isEmpty(getAndroidVersion())) {
            result.put("osVersion", getAndroidVersion());
        }
        strVal = getConnectionType();
        if (!TextUtils.isEmpty(strVal)) {
            result.put(RequestParameters.CONNECTION_TYPE, strVal);
        }
        result.put(SDK_VERSION, getSDKVersion());
        strVal = getLanguage();
        if (!TextUtils.isEmpty(strVal)) {
            result.put("language", strVal);
        }
        strVal = getDeviceOEM();
        if (!TextUtils.isEmpty(strVal)) {
            result.put(RequestParameters.DEVICE_OEM, strVal);
        }
        strVal = getDeviceModel();
        if (!TextUtils.isEmpty(strVal)) {
            result.put(RequestParameters.DEVICE_MODEL, strVal);
        }
        strVal = getMobileCarrier();
        if (!TextUtils.isEmpty(strVal)) {
            result.put(RequestParameters.MOBILE_CARRIER, strVal);
        }
        result.put("internalFreeMemory", Long.valueOf(getInternalStorageFreeSize()));
        result.put("externalFreeMemory", Long.valueOf(getExternalStorageFreeSize()));
        result.put("battery", Integer.valueOf(getBatteryLevel()));
        if (IronSourceUtils.getBooleanFromSharedPrefs(this.mContext, GeneralProperties.ALLOW_LOCATION_SHARED_PREFS_KEY, false)) {
            double[] lastKnownLocation = getLastKnownLocation();
            if (lastKnownLocation != null && lastKnownLocation.length == 2) {
                result.put("lat", Double.valueOf(lastKnownLocation[0]));
                result.put("lon", Double.valueOf(lastKnownLocation[1]));
            }
        }
        int gmtMinutesOffset = getGmtMinutesOffset();
        if (validateGmtMinutesOffset(gmtMinutesOffset)) {
            result.put("gmtMinutesOffset", Integer.valueOf(gmtMinutesOffset));
        }
        strVal = getPluginType();
        if (!TextUtils.isEmpty(strVal)) {
            result.put("pluginType", strVal);
        }
        strVal = getPluginVersion();
        if (!TextUtils.isEmpty(strVal)) {
            result.put("pluginVersion", strVal);
        }
        strVal = getPluginFrameworkVersion();
        if (!TextUtils.isEmpty(strVal)) {
            result.put("plugin_fw_v", strVal);
        }
        strVal = String.valueOf(DeviceStatus.isRootedDevice());
        if (!TextUtils.isEmpty(strVal)) {
            result.put("jb", strVal);
        }
        strVal = getMediationType();
        if (!TextUtils.isEmpty(strVal)) {
            result.put("mt", strVal);
        }
        return result;
    }

    private String getPublisherApplicationVersion(String packageName) {
        String result = BuildConfig.FLAVOR;
        try {
            return this.mContext.getPackageManager().getPackageInfo(packageName, 0).versionName;
        } catch (Exception e) {
            return result;
        }
    }

    private String getPluginType() {
        String result = BuildConfig.FLAVOR;
        try {
            result = ConfigFile.getConfigFile().getPluginType();
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceTag.NATIVE, "getPluginType()", e);
        }
        return result;
    }

    private String getPluginVersion() {
        String result = BuildConfig.FLAVOR;
        try {
            result = ConfigFile.getConfigFile().getPluginVersion();
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceTag.NATIVE, "getPluginVersion()", e);
        }
        return result;
    }

    private String getPluginFrameworkVersion() {
        String result = BuildConfig.FLAVOR;
        try {
            result = ConfigFile.getConfigFile().getPluginFrameworkVersion();
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceTag.NATIVE, "getPluginFrameworkVersion()", e);
        }
        return result;
    }

    private String getBundleId() {
        try {
            return this.mContext.getPackageName();
        } catch (Exception e) {
            return BuildConfig.FLAVOR;
        }
    }

    private String getApplicationKey() {
        return IronSourceObject.getInstance().getIronSourceAppKey();
    }

    private String getDeviceOS() {
        return Constants.JAVASCRIPT_INTERFACE_NAME;
    }

    private String getAndroidVersion() {
        try {
            return BuildConfig.FLAVOR + VERSION.SDK_INT + "(" + VERSION.RELEASE + ")";
        } catch (Exception e) {
            return BuildConfig.FLAVOR;
        }
    }

    private String getConnectionType() {
        if (this.mContext == null) {
            return Gender.UNKNOWN;
        }
        ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (cm == null) {
            return Gender.UNKNOWN;
        }
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getTypeName().equalsIgnoreCase(CONNECTION_CELLULAR)) {
                switch (info.getSubtype()) {
                    case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                    case R.styleable.View_paddingStart /*2*/:
                    case R.styleable.View_theme /*4*/:
                    case R.styleable.Toolbar_contentInsetLeft /*7*/:
                    case R.styleable.Toolbar_popupTheme /*11*/:
                        return CONNECTION_CELLULAR_2G;
                    case Cocos2dxEditBox.kEndActionReturn /*3*/:
                    case R.styleable.Toolbar_contentInsetStart /*5*/:
                    case R.styleable.Toolbar_contentInsetEnd /*6*/:
                    case R.styleable.Toolbar_contentInsetRight /*8*/:
                    case R.styleable.TextAppearance_textAllCaps /*9*/:
                    case R.styleable.SwitchCompat_switchMinWidth /*10*/:
                    case R.styleable.Toolbar_titleTextAppearance /*12*/:
                    case R.styleable.Toolbar_titleMargin /*14*/:
                    case MINUTES_OFFSET_STEP /*15*/:
                        return CONNECTION_CELLULAR_3G;
                    case R.styleable.Toolbar_subtitleTextAppearance /*13*/:
                        return CONNECTION_CELLULAR_4G_LTE;
                }
            }
            if (info.getTypeName().equalsIgnoreCase(CONNECTION_WIFI)) {
                return CONNECTION_WIFI_INT;
            }
            if (info.getTypeName().equalsIgnoreCase(CONNECTION_WIMAX)) {
                return CONNECTION_WIMAX_INT;
            }
            if (info.getTypeName().equalsIgnoreCase(CONNECTION_ETHERNET)) {
                return CONNECTION_ETHERNET_INT;
            }
        }
        return CONNECTION_NONE_INT;
    }

    private String getSDKVersion() {
        return IronSourceUtils.getSDKVersion();
    }

    private String getLanguage() {
        try {
            return Locale.getDefault().getLanguage();
        } catch (Exception e) {
            return BuildConfig.FLAVOR;
        }
    }

    private String getDeviceOEM() {
        try {
            return Build.MANUFACTURER;
        } catch (Exception e) {
            return BuildConfig.FLAVOR;
        }
    }

    private String getDeviceModel() {
        try {
            return Build.MODEL;
        } catch (Exception e) {
            return BuildConfig.FLAVOR;
        }
    }

    private String getMobileCarrier() {
        String ret = BuildConfig.FLAVOR;
        try {
            TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
            if (telephonyManager == null) {
                return ret;
            }
            String operatorName = telephonyManager.getNetworkOperatorName();
            if (operatorName.equals(BuildConfig.FLAVOR)) {
                return ret;
            }
            return operatorName;
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceTag.NATIVE, this.TAG + ":getMobileCarrier()", e);
            return ret;
        }
    }

    private boolean isExternalStorageAbvailable() {
        try {
            return Environment.getExternalStorageState().equals("mounted");
        } catch (Exception e) {
            return false;
        }
    }

    private long getInternalStorageFreeSize() {
        try {
            StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
            return (((long) stat.getAvailableBlocks()) * ((long) stat.getBlockSize())) / 1048576;
        } catch (Exception e) {
            return -1;
        }
    }

    private long getExternalStorageFreeSize() {
        if (!isExternalStorageAbvailable()) {
            return -1;
        }
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        return (((long) stat.getAvailableBlocks()) * ((long) stat.getBlockSize())) / 1048576;
    }

    private int getBatteryLevel() {
        int scale = 0;
        try {
            int level;
            Intent batteryIntent = this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
            if (batteryIntent != null) {
                level = batteryIntent.getIntExtra(Param.LEVEL, -1);
            } else {
                level = 0;
            }
            if (batteryIntent != null) {
                scale = batteryIntent.getIntExtra("scale", -1);
            }
            if (level == -1 || scale == -1) {
                return -1;
            }
            return (int) ((((float) level) / ((float) scale)) * 100.0f);
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceTag.NATIVE, this.TAG + ":getBatteryLevel()", e);
            return -1;
        }
    }

    @SuppressLint({"MissingPermission"})
    private double[] getLastKnownLocation() {
        double[] result = new double[0];
        long bestLocationTime = Long.MIN_VALUE;
        try {
            if (!locationPermissionGranted()) {
                return result;
            }
            LocationManager locationManager = (LocationManager) this.mContext.getApplicationContext().getSystemService(Param.LOCATION);
            Location bestLocation = null;
            for (String provider : locationManager.getAllProviders()) {
                Location location = locationManager.getLastKnownLocation(provider);
                if (location != null && location.getTime() > bestLocationTime) {
                    bestLocation = location;
                    bestLocationTime = bestLocation.getTime();
                }
            }
            if (bestLocation == null) {
                return result;
            }
            double lat = bestLocation.getLatitude();
            double lon = bestLocation.getLongitude();
            return new double[]{lat, lon};
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceTag.NATIVE, this.TAG + ":getLastLocation()", e);
            return new double[0];
        }
    }

    private boolean locationPermissionGranted() {
        try {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.ACCESS_FINE_LOCATION") == 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private int getGmtMinutesOffset() {
        int result = 0;
        try {
            TimeZone tz = TimeZone.getDefault();
            result = (tz.getOffset(GregorianCalendar.getInstance(tz).getTimeInMillis()) / GameControllerDelegate.THUMBSTICK_LEFT_X) / 60;
            return Math.round((float) (result / MINUTES_OFFSET_STEP)) * MINUTES_OFFSET_STEP;
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceTag.NATIVE, this.TAG + ":getGmtMinutesOffset()", e);
            return result;
        }
    }

    private boolean validateGmtMinutesOffset(int offset) {
        return offset <= MAX_MINUTES_OFFSET && offset >= MIN_MINUTES_OFFSET && offset % MINUTES_OFFSET_STEP == 0;
    }

    private String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", BuildConfig.FLAVOR) + IronSourceUtils.getTimeStamp();
    }

    private String getMediationType() {
        return IronSourceObject.getInstance().getMediationType();
    }
}
