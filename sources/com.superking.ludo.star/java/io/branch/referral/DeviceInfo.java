package io.branch.referral;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import io.branch.referral.Defines.Jsonkey;
import org.json.JSONException;
import org.json.JSONObject;

class DeviceInfo {
    private static DeviceInfo thisInstance_ = null;
    private final String appVersion_;
    private final String brandName_;
    private final String countryCode_;
    private final String hardwareID_;
    private final boolean isHardwareIDReal_;
    private final boolean isWifiConnected_;
    private final String languageCode_;
    private final String localIpAddr_;
    private final String modelName_;
    private final String osName_;
    private final int osVersion_;
    private final String packageName_;
    private final int screenDensity_;
    private final int screenHeight_;
    private final int screenWidth_;

    public static DeviceInfo getInstance(boolean isExternalDebug, SystemObserver sysObserver, boolean disableAndroidIDFetch) {
        if (thisInstance_ == null) {
            thisInstance_ = new DeviceInfo(isExternalDebug, sysObserver, disableAndroidIDFetch);
        }
        return thisInstance_;
    }

    public static DeviceInfo getInstance() {
        return thisInstance_;
    }

    private DeviceInfo(boolean isExternalDebug, SystemObserver sysObserver, boolean disableAndroidIDFetch) {
        if (disableAndroidIDFetch) {
            this.hardwareID_ = sysObserver.getUniqueID(true);
        } else {
            this.hardwareID_ = sysObserver.getUniqueID(isExternalDebug);
        }
        this.isHardwareIDReal_ = sysObserver.hasRealHardwareId();
        this.brandName_ = sysObserver.getPhoneBrand();
        this.modelName_ = sysObserver.getPhoneModel();
        DisplayMetrics dMetrics = sysObserver.getScreenDisplay();
        this.screenDensity_ = dMetrics.densityDpi;
        this.screenHeight_ = dMetrics.heightPixels;
        this.screenWidth_ = dMetrics.widthPixels;
        this.isWifiConnected_ = sysObserver.getWifiConnected();
        this.localIpAddr_ = SystemObserver.getLocalIPAddress();
        this.osName_ = sysObserver.getOS();
        this.osVersion_ = sysObserver.getOSVersion();
        this.packageName_ = sysObserver.getPackageName();
        this.appVersion_ = sysObserver.getAppVersion();
        this.countryCode_ = sysObserver.getISO2CountryCode();
        this.languageCode_ = sysObserver.getISO2LanguageCode();
    }

    public void updateRequestWithDeviceParams(JSONObject requestObj) {
        try {
            if (!this.hardwareID_.equals(SystemObserver.BLANK)) {
                requestObj.put(Jsonkey.HardwareID.getKey(), this.hardwareID_);
                requestObj.put(Jsonkey.IsHardwareIDReal.getKey(), this.isHardwareIDReal_);
            }
            if (!this.brandName_.equals(SystemObserver.BLANK)) {
                requestObj.put(Jsonkey.Brand.getKey(), this.brandName_);
            }
            if (!this.modelName_.equals(SystemObserver.BLANK)) {
                requestObj.put(Jsonkey.Model.getKey(), this.modelName_);
            }
            requestObj.put(Jsonkey.ScreenDpi.getKey(), this.screenDensity_);
            requestObj.put(Jsonkey.ScreenHeight.getKey(), this.screenHeight_);
            requestObj.put(Jsonkey.ScreenWidth.getKey(), this.screenWidth_);
            requestObj.put(Jsonkey.WiFi.getKey(), this.isWifiConnected_);
            if (!this.osName_.equals(SystemObserver.BLANK)) {
                requestObj.put(Jsonkey.OS.getKey(), this.osName_);
            }
            requestObj.put(Jsonkey.OSVersion.getKey(), this.osVersion_);
            if (!TextUtils.isEmpty(this.countryCode_)) {
                requestObj.put(Jsonkey.Country.getKey(), this.countryCode_);
            }
            if (!TextUtils.isEmpty(this.languageCode_)) {
                requestObj.put(Jsonkey.Language.getKey(), this.languageCode_);
            }
            if (!TextUtils.isEmpty(this.localIpAddr_)) {
                requestObj.put(Jsonkey.LocalIP.getKey(), this.localIpAddr_);
            }
        } catch (JSONException e) {
        }
    }

    public String getPackageName() {
        return this.packageName_;
    }

    public String getAppVersion() {
        return this.appVersion_;
    }

    public boolean isHardwareIDReal() {
        return this.isHardwareIDReal_;
    }

    public String getHardwareID() {
        return this.hardwareID_.equals(SystemObserver.BLANK) ? null : this.hardwareID_;
    }

    public String getOsName() {
        return this.osName_;
    }
}
