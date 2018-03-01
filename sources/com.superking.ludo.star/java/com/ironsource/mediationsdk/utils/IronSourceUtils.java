package com.ironsource.mediationsdk.utils;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Base64;
import com.facebook.appevents.AppEventsConstants;
import com.ironsource.mediationsdk.AbstractAdapter;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.logger.ThreadExceptionHandler;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.cocos2dx.lib.BuildConfig;
import org.json.JSONException;
import org.json.JSONObject;

public class IronSourceUtils {
    private static final String ADAPTER_VERSION_KEY = "providerAdapterVersion";
    private static final String DEFAULT_IS_EVENTS_FORMATTER_TYPE = "default_is_events_formatter_type";
    private static final String DEFAULT_IS_EVENTS_URL = "default_is_events_url";
    private static final String DEFAULT_IS_OPT_OUT_EVENTS = "default_is_opt_out_events";
    private static final String DEFAULT_RV_EVENTS_FORMATTER_TYPE = "default_rv_events_formatter_type";
    private static final String DEFAULT_RV_EVENTS_URL = "default_rv_events_url";
    private static final String DEFAULT_RV_OPT_OUT_EVENTS = "default_rv_opt_out_events";
    private static final String GENERAL_PROPERTIES = "general_properties";
    private static final String LAST_RESPONSE = "last_response";
    private static final String PROVIDER_KEY = "provider";
    private static final String SDK_VERSION = "6.6.0";
    private static final String SDK_VERSION_KEY = "providerSDKVersion";
    private static final String SHARED_PREFERENCES_NAME = "Mediation_Shared_Preferences";

    public static String getMD5(String input) {
        try {
            String bigInteger = new BigInteger(1, MessageDigest.getInstance("MD5").digest(input.getBytes())).toString(16);
            while (bigInteger.length() < 32) {
                bigInteger = AppEventsConstants.EVENT_PARAM_VALUE_NO + bigInteger;
            }
            return bigInteger;
        } catch (Throwable e) {
            if (input == null) {
                IronSourceLoggerManager.getLogger().logException(IronSourceTag.NATIVE, "getMD5(input:null)", e);
            } else {
                IronSourceLoggerManager.getLogger().logException(IronSourceTag.NATIVE, "getMD5(input:" + input + ")", e);
            }
            return BuildConfig.FLAVOR;
        }
    }

    private static String getSHA256(String input) {
        try {
            return String.format("%064x", new Object[]{new BigInteger(1, MessageDigest.getInstance("SHA-256").digest(input.getBytes()))});
        } catch (NoSuchAlgorithmException e) {
            if (input == null) {
                IronSourceLoggerManager.getLogger().logException(IronSourceTag.NATIVE, "getSHA256(input:null)", e);
            } else {
                IronSourceLoggerManager.getLogger().logException(IronSourceTag.NATIVE, "getSHA256(input:" + input + ")", e);
            }
            return BuildConfig.FLAVOR;
        }
    }

    public static String getTransId(String strToTransId) {
        return getSHA256(strToTransId);
    }

    public static int getCurrentTimestamp() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    public static String getSDKVersion() {
        return SDK_VERSION;
    }

    public static void createAndStartWorker(Runnable runnable, String threadName) {
        Thread worker = new Thread(runnable, threadName);
        worker.setUncaughtExceptionHandler(new ThreadExceptionHandler());
        worker.start();
    }

    public static String getBase64Auth(String loginUsername, String loginPass) {
        return "Basic " + Base64.encodeToString((loginUsername + ":" + loginPass).getBytes(), 10);
    }

    private static String getDefaultEventsUrlByEventType(String eventType) {
        if (IronSourceConstants.INTERSTITIAL_EVENT_TYPE.equals(eventType)) {
            return DEFAULT_IS_EVENTS_URL;
        }
        if (IronSourceConstants.REWARDED_VIDEO_EVENT_TYPE.equals(eventType)) {
            return DEFAULT_RV_EVENTS_URL;
        }
        return BuildConfig.FLAVOR;
    }

    private static String getDefaultOptOutEventsByEventType(String eventType) {
        if (IronSourceConstants.INTERSTITIAL_EVENT_TYPE.equals(eventType)) {
            return DEFAULT_IS_OPT_OUT_EVENTS;
        }
        if (IronSourceConstants.REWARDED_VIDEO_EVENT_TYPE.equals(eventType)) {
            return DEFAULT_RV_OPT_OUT_EVENTS;
        }
        return BuildConfig.FLAVOR;
    }

    private static String getDefaultFormatterTypeByEventType(String eventType) {
        if (IronSourceConstants.INTERSTITIAL_EVENT_TYPE.equals(eventType)) {
            return DEFAULT_IS_EVENTS_FORMATTER_TYPE;
        }
        if (IronSourceConstants.REWARDED_VIDEO_EVENT_TYPE.equals(eventType)) {
            return DEFAULT_RV_EVENTS_FORMATTER_TYPE;
        }
        return BuildConfig.FLAVOR;
    }

    public static synchronized void saveDefaultEventsURL(Context context, String eventType, String eventsUrl) {
        synchronized (IronSourceUtils.class) {
            try {
                Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0).edit();
                editor.putString(getDefaultEventsUrlByEventType(eventType), eventsUrl);
                editor.commit();
            } catch (Exception e) {
                IronSourceLoggerManager.getLogger().logException(IronSourceTag.NATIVE, "IronSourceUtils:saveDefaultEventsURL(eventType: " + eventType + ", eventsUrl:" + eventsUrl + ")", e);
            }
        }
    }

    public static synchronized void saveDefaultOptOutEvents(Context context, String eventType, int[] optOutEvents) {
        synchronized (IronSourceUtils.class) {
            try {
                Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0).edit();
                String optOutEventsString = null;
                if (optOutEvents != null) {
                    StringBuilder str = new StringBuilder();
                    for (int append : optOutEvents) {
                        str.append(append).append(",");
                    }
                    optOutEventsString = str.toString();
                }
                editor.putString(getDefaultOptOutEventsByEventType(eventType), optOutEventsString);
                editor.commit();
            } catch (Exception e) {
                IronSourceLoggerManager.getLogger().logException(IronSourceTag.NATIVE, "IronSourceUtils:saveDefaultOptOutEvents(eventType: " + eventType + ", optOutEvents:" + optOutEvents + ")", e);
            }
        }
    }

    public static synchronized void saveDefaultEventsFormatterType(Context context, String eventType, String formatterType) {
        synchronized (IronSourceUtils.class) {
            try {
                Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0).edit();
                editor.putString(getDefaultFormatterTypeByEventType(eventType), formatterType);
                editor.commit();
            } catch (Exception e) {
                IronSourceLoggerManager.getLogger().logException(IronSourceTag.NATIVE, "IronSourceUtils:saveDefaultEventsFormatterType(eventType: " + eventType + ", formatterType:" + formatterType + ")", e);
            }
        }
    }

    public static synchronized String getDefaultEventsFormatterType(Context context, String eventType, String defaultFormatterType) {
        String formatterType;
        synchronized (IronSourceUtils.class) {
            formatterType = defaultFormatterType;
            try {
                formatterType = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0).getString(getDefaultFormatterTypeByEventType(eventType), defaultFormatterType);
            } catch (Exception e) {
                IronSourceLoggerManager.getLogger().logException(IronSourceTag.NATIVE, "IronSourceUtils:getDefaultEventsFormatterType(eventType: " + eventType + ", defaultFormatterType:" + defaultFormatterType + ")", e);
            }
        }
        return formatterType;
    }

    public static synchronized String getDefaultEventsURL(Context context, String eventType, String defaultEventsURL) {
        String serverUrl;
        synchronized (IronSourceUtils.class) {
            serverUrl = defaultEventsURL;
            try {
                serverUrl = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0).getString(getDefaultEventsUrlByEventType(eventType), defaultEventsURL);
            } catch (Exception e) {
                IronSourceLoggerManager.getLogger().logException(IronSourceTag.NATIVE, "IronSourceUtils:getDefaultEventsURL(eventType: " + eventType + ", defaultEventsURL:" + defaultEventsURL + ")", e);
            }
        }
        return serverUrl;
    }

    public static synchronized int[] getDefaultOptOutEvents(Context context, String eventType) {
        int[] optOutEvents;
        synchronized (IronSourceUtils.class) {
            optOutEvents = null;
            try {
                String optOutEventsString = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0).getString(getDefaultOptOutEventsByEventType(eventType), null);
                if (!TextUtils.isEmpty(optOutEventsString)) {
                    StringTokenizer stringTokenizer = new StringTokenizer(optOutEventsString, ",");
                    ArrayList<Integer> result = new ArrayList();
                    while (stringTokenizer.hasMoreTokens()) {
                        result.add(Integer.valueOf(Integer.parseInt(stringTokenizer.nextToken())));
                    }
                    optOutEvents = new int[result.size()];
                    for (int i = 0; i < optOutEvents.length; i++) {
                        optOutEvents[i] = ((Integer) result.get(i)).intValue();
                    }
                }
            } catch (Exception e) {
                IronSourceLoggerManager.getLogger().logException(IronSourceTag.NATIVE, "IronSourceUtils:getDefaultOptOutEvents(eventType: " + eventType + ")", e);
            }
        }
        return optOutEvents;
    }

    public static synchronized void saveLastResponse(Context context, String response) {
        synchronized (IronSourceUtils.class) {
            Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0).edit();
            editor.putString(LAST_RESPONSE, response);
            editor.apply();
        }
    }

    public static String getLastResponse(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0).getString(LAST_RESPONSE, BuildConfig.FLAVOR);
    }

    static synchronized void saveGeneralProperties(Context context, JSONObject properties) {
        synchronized (IronSourceUtils.class) {
            if (!(context == null || properties == null)) {
                Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0).edit();
                editor.putString(GENERAL_PROPERTIES, properties.toString());
                editor.apply();
            }
        }
    }

    public static synchronized JSONObject getGeneralProperties(Context context) {
        Object result;
        synchronized (IronSourceUtils.class) {
            JSONObject result2 = new JSONObject();
            if (context == null) {
                result = result2;
            } else {
                try {
                    result2 = new JSONObject(context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0).getString(GENERAL_PROPERTIES, result2.toString()));
                } catch (JSONException e) {
                }
                JSONObject result3 = result2;
            }
        }
        return result;
    }

    public static boolean isNetworkConnected(Context context) {
        NetworkInfo activeNetwork = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetwork == null) {
            return false;
        }
        return activeNetwork.isConnected();
    }

    public static long getTimeStamp() {
        return System.currentTimeMillis();
    }

    public static JSONObject getProviderAdditionalData(AbstractAdapter adapter) {
        JSONObject data = new JSONObject();
        try {
            data.put(PROVIDER_KEY, adapter.getProviderName());
            data.put(SDK_VERSION_KEY, adapter.getCoreSDKVersion());
            data.put(ADAPTER_VERSION_KEY, adapter.getVersion());
        } catch (JSONException e) {
        }
        return data;
    }

    public static JSONObject getMediationAdditionalData() {
        JSONObject data = new JSONObject();
        try {
            data.put(PROVIDER_KEY, IronSourceConstants.MEDIATION_PROVIDER_NAME);
        } catch (JSONException e) {
        }
        return data;
    }

    public static void saveStringToSharedPrefs(Context context, String key, String value) {
        Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getStringFromSharedPrefs(Context context, String key, String defaultValue) {
        return context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0).getString(key, defaultValue);
    }

    public static void saveBooleanToSharedPrefs(Context context, String key, boolean value) {
        Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0).edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static boolean getBooleanFromSharedPrefs(Context context, String key, boolean defaultValue) {
        return context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0).getBoolean(key, defaultValue);
    }

    public static void saveIntToSharedPrefs(Context context, String key, int value) {
        Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0).edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static int getIntFromSharedPrefs(Context context, String key, int defaultValue) {
        return context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0).getInt(key, defaultValue);
    }

    public static void saveLongToSharedPrefs(Context context, String key, long value) {
        Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0).edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public static long getLongFromSharedPrefs(Context context, String key, long defaultValue) {
        return context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0).getLong(key, defaultValue);
    }
}
