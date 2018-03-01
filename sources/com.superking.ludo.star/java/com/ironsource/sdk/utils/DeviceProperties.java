package com.ironsource.sdk.utils;

import android.content.Context;
import com.ironsource.environment.DeviceStatus;

public class DeviceProperties {
    private static DeviceProperties mInstance = null;
    private String mDeviceCarrier;
    private String mDeviceModel = DeviceStatus.getDeviceModel();
    private String mDeviceOem = DeviceStatus.getDeviceOEM();
    private String mDeviceOsType = DeviceStatus.getDeviceOs();
    private int mDeviceOsVersion = DeviceStatus.getAndroidAPIVersion();

    private DeviceProperties(Context context) {
        this.mDeviceCarrier = DeviceStatus.getMobileCarrier(context);
    }

    public static DeviceProperties getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DeviceProperties(context);
        }
        return mInstance;
    }

    public String getDeviceOem() {
        return this.mDeviceOem;
    }

    public String getDeviceModel() {
        return this.mDeviceModel;
    }

    public String getDeviceOsType() {
        return this.mDeviceOsType;
    }

    public int getDeviceOsVersion() {
        return this.mDeviceOsVersion;
    }

    public String getDeviceCarrier() {
        return this.mDeviceCarrier;
    }

    public static String getSupersonicSdkVersion() {
        return Constants.SDK_VERSION;
    }

    public static void release() {
        mInstance = null;
    }

    public float getDeviceVolume(Context context) {
        return DeviceStatus.getSystemVolumePercent(context);
    }
}
