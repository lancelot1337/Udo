package com.bugsnag.android;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings.Secure;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.bugsnag.android.JsonStream.Streamable;
import com.google.firebase.analytics.FirebaseAnalytics.Param;
import com.ironsource.environment.ConnectivityService;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import com.ironsource.sdk.utils.Constants.RequestParameters;
import com.unity3d.ads.adunit.AdUnitActivity;
import io.branch.referral.R;
import java.io.IOException;
import java.util.Date;
import org.cocos2dx.lib.Cocos2dxHandler;

class DeviceState implements Streamable {
    private final Float batteryLevel;
    private final Boolean charging;
    private final Long freeDisk;
    private final Long freeMemory = getFreeMemory();
    private final String locationStatus;
    private final String networkAccess;
    private final String orientation;
    private final String time;

    DeviceState(@NonNull Context appContext) {
        this.orientation = getOrientation(appContext);
        this.batteryLevel = getBatteryLevel(appContext);
        this.freeDisk = getFreeDisk();
        this.charging = isCharging(appContext);
        this.locationStatus = getLocationStatus(appContext);
        this.networkAccess = getNetworkAccess(appContext);
        this.time = getTime();
    }

    public void toStream(@NonNull JsonStream writer) throws IOException {
        writer.beginObject();
        writer.name("freeMemory").value(this.freeMemory);
        writer.name(AdUnitActivity.EXTRA_ORIENTATION).value(this.orientation);
        writer.name(RequestParameters.BATTERY_LEVEL).value(this.batteryLevel);
        writer.name("freeDisk").value(this.freeDisk);
        writer.name("charging").value(this.charging);
        writer.name("locationStatus").value(this.locationStatus);
        writer.name("networkAccess").value(this.networkAccess);
        writer.name("time").value(this.time);
        writer.endObject();
    }

    @NonNull
    private static Long getFreeMemory() {
        if (Runtime.getRuntime().maxMemory() != Long.MAX_VALUE) {
            return Long.valueOf((Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()) + Runtime.getRuntime().freeMemory());
        }
        return Long.valueOf(Runtime.getRuntime().freeMemory());
    }

    @Nullable
    private static String getOrientation(Context appContext) {
        switch (appContext.getResources().getConfiguration().orientation) {
            case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                return ParametersKeys.ORIENTATION_PORTRAIT;
            case R.styleable.View_paddingStart /*2*/:
                return ParametersKeys.ORIENTATION_LANDSCAPE;
            default:
                return null;
        }
    }

    @Nullable
    private static Float getBatteryLevel(Context appContext) {
        Float f = null;
        try {
            Intent batteryStatus = appContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
            f = Float.valueOf(((float) batteryStatus.getIntExtra(Param.LEVEL, -1)) / ((float) batteryStatus.getIntExtra("scale", -1)));
        } catch (Exception e) {
            Logger.warn("Could not get batteryLevel");
        }
        return f;
    }

    @Nullable
    private static Long getFreeDisk() {
        try {
            StatFs externalStat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long externalBytesAvailable = ((long) externalStat.getBlockSize()) * ((long) externalStat.getBlockCount());
            StatFs internalStat = new StatFs(Environment.getDataDirectory().getPath());
            return Long.valueOf(Math.min(((long) internalStat.getBlockSize()) * ((long) internalStat.getBlockCount()), externalBytesAvailable));
        } catch (Exception e) {
            Logger.warn("Could not get freeDisk");
            return null;
        }
    }

    @Nullable
    private static Boolean isCharging(Context appContext) {
        Boolean bool = null;
        try {
            int status = appContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED")).getIntExtra(ParametersKeys.VIDEO_STATUS, -1);
            boolean z = status == 2 || status == 5;
            bool = Boolean.valueOf(z);
        } catch (Exception e) {
            Logger.warn("Could not get charging status");
        }
        return bool;
    }

    @Nullable
    private static String getLocationStatus(Context appContext) {
        try {
            String providersAllowed = Secure.getString(appContext.getContentResolver(), "location_providers_allowed");
            if (providersAllowed == null || providersAllowed.length() <= 0) {
                return "disallowed";
            }
            return "allowed";
        } catch (Exception e) {
            Logger.warn("Could not get locationStatus");
            return null;
        }
    }

    @Nullable
    private static String getNetworkAccess(Context appContext) {
        try {
            NetworkInfo activeNetwork = ((ConnectivityManager) appContext.getSystemService("connectivity")).getActiveNetworkInfo();
            if (activeNetwork == null || !activeNetwork.isConnectedOrConnecting()) {
                return ParametersKeys.ORIENTATION_NONE;
            }
            if (activeNetwork.getType() == 1) {
                return ConnectivityService.NETWORK_TYPE_WIFI;
            }
            if (activeNetwork.getType() == 9) {
                return "ethernet";
            }
            return "cellular";
        } catch (Exception e) {
            Logger.warn("Could not get network access information, we recommend granting the 'android.permission.ACCESS_NETWORK_STATE' permission");
            return null;
        }
    }

    @NonNull
    private String getTime() {
        return DateUtils.toISO8601(new Date());
    }
}
