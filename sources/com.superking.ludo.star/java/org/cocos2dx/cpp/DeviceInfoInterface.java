package org.cocos2dx.cpp;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build.VERSION;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import com.google.firebase.analytics.FirebaseAnalytics.Param;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import java.net.InetAddress;
import java.util.Currency;
import java.util.UUID;
import org.cocos2dx.lib.BuildConfig;
import org.cocos2dx.lib.Cocos2dxHelper;

public class DeviceInfoInterface {
    public static final String EMULATOR_ID = "9774d56d682e549c";
    public static final String LS_KEY_DEVICE = "EM:device:id";
    public static String mIP = BuildConfig.FLAVOR;
    public static boolean mIPFetchDone = false;

    public static String getUniqueDeviceId() {
        String deviceId = Cocos2dxHelper.getStringForKey(LS_KEY_DEVICE, BuildConfig.FLAVOR);
        if (deviceId == null || deviceId.isEmpty()) {
            deviceId = Secure.getString(AppActivity.getInstance().getApplicationContext().getContentResolver(), "android_id");
            if (EMULATOR_ID.equals(deviceId) || deviceId == null || deviceId.isEmpty()) {
                deviceId = UUID.randomUUID().toString();
            }
            Cocos2dxHelper.setStringForKey(LS_KEY_DEVICE, deviceId);
        }
        return deviceId;
    }

    public static boolean isConnected() {
        Context context = AppActivity.getInstance();
        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null) {
                    return activeNetwork.isConnectedOrConnecting();
                }
                return false;
            }
        }
        return true;
    }

    public static String chargingStatus() {
        boolean z = true;
        if (AppActivity.getInstance() != null) {
            Intent batteryStatus = AppActivity.getInstance().getApplicationContext().registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
            if (batteryStatus != null) {
                boolean isCharging;
                boolean z2;
                int status = batteryStatus.getIntExtra(ParametersKeys.VIDEO_STATUS, -1);
                if (status == 2 || status == 5) {
                    isCharging = true;
                } else {
                    isCharging = false;
                }
                int chargePlugType = batteryStatus.getIntExtra(ParametersKeys.VIDEO_STATUS, -1);
                if (chargePlugType == 1) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                Boolean acCharge = Boolean.valueOf(z2);
                if (chargePlugType != 2) {
                    z = false;
                }
                Boolean usbCharge = Boolean.valueOf(z);
                if (isCharging) {
                    if (acCharge.booleanValue()) {
                        return "AC_CHARGING";
                    }
                    if (usbCharge.booleanValue()) {
                        return "USB_CHARGING";
                    }
                }
                int batteryLevel = batteryStatus.getIntExtra(Param.LEVEL, -1);
                int batteryPct = (batteryLevel * 100) / batteryStatus.getIntExtra("scale", -1);
                if (batteryPct < 20) {
                    return "NOT_CHARGING_CRITICAL";
                }
                if (batteryPct < 50) {
                    return "NOT_CHARGING_LOW";
                }
            }
        }
        return "NOT_CHARGING_NORMAL";
    }

    public static String getCurrencyCode() {
        try {
            if (AppActivity.getInstance() != null) {
                Currency currency = Currency.getInstance(AppActivity.getInstance().getResources().getConfiguration().locale);
                if (currency != null) {
                    return currency.getCurrencyCode();
                }
            }
        } catch (Exception e) {
        }
        return "INR";
    }

    public static String getCountryCode() {
        String countryCode = BuildConfig.FLAVOR;
        try {
            countryCode = ((TelephonyManager) AppActivity.getInstance().getSystemService("phone")).getSimCountryIso();
            if (countryCode.equals(BuildConfig.FLAVOR)) {
                countryCode = AppActivity.getInstance().getResources().getConfiguration().locale.getCountry();
            }
        } catch (Exception e) {
        }
        return countryCode.toUpperCase();
    }

    public static String getIP() {
        if (mIPFetchDone) {
            return mIP;
        }
        mIPFetchDone = true;
        try {
            mIP = InetAddress.getByName("ludo.superkinglabs.com").getHostAddress();
        } catch (Exception e) {
            mIP = BuildConfig.FLAVOR;
        }
        return mIP;
    }

    public static int getBuildVersion() {
        return VERSION.SDK_INT;
    }
}
