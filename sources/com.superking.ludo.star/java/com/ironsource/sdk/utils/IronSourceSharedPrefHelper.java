package com.ironsource.sdk.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import com.ironsource.sdk.data.SSABCParameters;
import com.ironsource.sdk.data.SSAEnums.BackButtonState;
import com.ironsource.sdk.data.SSAEnums.ProductType;
import com.ironsource.sdk.data.SSAObj;
import com.ironsource.sdk.data.SSASession;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import io.branch.referral.R;
import java.util.ArrayList;
import java.util.List;
import org.cocos2dx.lib.Cocos2dxEditBox;
import org.cocos2dx.lib.Cocos2dxHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class IronSourceSharedPrefHelper {
    private static final String APPLICATION_KEY_IS = "application_key_is";
    private static final String APPLICATION_KEY_OW = "application_key_ow";
    private static final String APPLICATION_KEY_RV = "application_key_rv";
    private static final String BACK_BUTTON_STATE = "back_button_state";
    private static final String IS_REPORTED = "is_reported";
    private static final String REGISTER_SESSIONS = "register_sessions";
    private static final String SEARCH_KEYS = "search_keys";
    private static final String SESSIONS = "sessions";
    private static final String SSA_RV_PARAMETER_CONNECTION_RETRIES = "ssa_rv_parameter_connection_retries";
    private static final String SSA_SDK_DOWNLOAD_URL = "ssa_sdk_download_url";
    private static final String SSA_SDK_LOAD_URL = "ssa_sdk_load_url";
    private static final String SUPERSONIC_SHARED_PREF = "supersonic_shared_preferen";
    private static final String UNIQUE_ID_IS = "unique_id_is";
    private static final String UNIQUE_ID_OW = "unique_id_ow";
    private static final String UNIQUE_ID_RV = "unique_id_rv";
    private static final String USER_ID_IS = "user_id_is";
    private static final String USER_ID_OW = "user_id_ow";
    private static final String USER_ID_RV = "user_id_rv";
    private static final String VERSION = "version";
    private static IronSourceSharedPrefHelper mInstance;
    private SharedPreferences mSharedPreferences;

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$ironsource$sdk$data$SSAEnums$ProductType = new int[ProductType.values().length];

        static {
            try {
                $SwitchMap$com$ironsource$sdk$data$SSAEnums$ProductType[ProductType.RewardedVideo.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$ironsource$sdk$data$SSAEnums$ProductType[ProductType.OfferWall.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$ironsource$sdk$data$SSAEnums$ProductType[ProductType.Interstitial.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    private IronSourceSharedPrefHelper(Context context) {
        this.mSharedPreferences = context.getSharedPreferences(SUPERSONIC_SHARED_PREF, 0);
    }

    public static synchronized IronSourceSharedPrefHelper getSupersonicPrefHelper(Context context) {
        IronSourceSharedPrefHelper ironSourceSharedPrefHelper;
        synchronized (IronSourceSharedPrefHelper.class) {
            if (mInstance == null) {
                mInstance = new IronSourceSharedPrefHelper(context);
            }
            ironSourceSharedPrefHelper = mInstance;
        }
        return ironSourceSharedPrefHelper;
    }

    public static synchronized IronSourceSharedPrefHelper getSupersonicPrefHelper() {
        IronSourceSharedPrefHelper ironSourceSharedPrefHelper;
        synchronized (IronSourceSharedPrefHelper.class) {
            ironSourceSharedPrefHelper = mInstance;
        }
        return ironSourceSharedPrefHelper;
    }

    public String getConnectionRetries() {
        return this.mSharedPreferences.getString(SSA_RV_PARAMETER_CONNECTION_RETRIES, "3");
    }

    public void setSSABCParameters(SSABCParameters object) {
        Editor editor = this.mSharedPreferences.edit();
        editor.putString(SSA_RV_PARAMETER_CONNECTION_RETRIES, object.getConnectionRetries());
        editor.commit();
    }

    public void setBackButtonState(String value) {
        Editor editor = this.mSharedPreferences.edit();
        editor.putString(BACK_BUTTON_STATE, value);
        editor.commit();
    }

    public BackButtonState getBackButtonState() {
        int state = Integer.parseInt(this.mSharedPreferences.getString(BACK_BUTTON_STATE, "2"));
        if (state == 0) {
            return BackButtonState.None;
        }
        if (state == 1) {
            return BackButtonState.Device;
        }
        if (state == 2) {
            return BackButtonState.Controller;
        }
        return BackButtonState.Controller;
    }

    public void setSearchKeys(String value) {
        Editor editor = this.mSharedPreferences.edit();
        editor.putString(SEARCH_KEYS, value);
        editor.commit();
    }

    public List<String> getSearchKeys() {
        String value = this.mSharedPreferences.getString(SEARCH_KEYS, null);
        ArrayList<String> keys = new ArrayList();
        if (value != null) {
            SSAObj ssaObj = new SSAObj(value);
            if (ssaObj.containsKey(ParametersKeys.SEARCH_KEYS)) {
                try {
                    keys.addAll(ssaObj.toList((JSONArray) ssaObj.get(ParametersKeys.SEARCH_KEYS)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return keys;
    }

    public JSONArray getSessions() {
        String value = this.mSharedPreferences.getString(SESSIONS, null);
        if (value == null) {
            return new JSONArray();
        }
        JSONArray jsArr;
        try {
            jsArr = new JSONArray(value);
        } catch (JSONException e) {
            jsArr = new JSONArray();
        }
        return jsArr;
    }

    public void deleteSessions() {
        Editor editor = this.mSharedPreferences.edit();
        editor.putString(SESSIONS, null);
        editor.commit();
    }

    public void addSession(SSASession session) {
        if (getShouldRegisterSessions()) {
            JSONObject jsObj = new JSONObject();
            try {
                jsObj.put("sessionStartTime", session.getSessionStartTime());
                jsObj.put("sessionEndTime", session.getSessionEndTime());
                jsObj.put("sessionType", session.getSessionType());
                jsObj.put("connectivity", session.getConnectivity());
            } catch (JSONException e) {
            }
            JSONArray jsArr = getSessions();
            if (jsArr == null) {
                jsArr = new JSONArray();
            }
            jsArr.put(jsObj);
            Editor editor = this.mSharedPreferences.edit();
            editor.putString(SESSIONS, jsArr.toString());
            editor.commit();
        }
    }

    private boolean getShouldRegisterSessions() {
        return this.mSharedPreferences.getBoolean(REGISTER_SESSIONS, true);
    }

    public void setShouldRegisterSessions(boolean value) {
        Editor editor = this.mSharedPreferences.edit();
        editor.putBoolean(REGISTER_SESSIONS, value);
        editor.commit();
    }

    public boolean setUserData(String key, String value) {
        Editor editor = this.mSharedPreferences.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    public String getUserData(String key) {
        String value = this.mSharedPreferences.getString(key, null);
        return value != null ? value : "{}";
    }

    public String getApplicationKey(ProductType type) {
        String applicationKey = "EMPTY_APPLICATION_KEY";
        switch (AnonymousClass1.$SwitchMap$com$ironsource$sdk$data$SSAEnums$ProductType[type.ordinal()]) {
            case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                return this.mSharedPreferences.getString(APPLICATION_KEY_RV, null);
            case R.styleable.View_paddingStart /*2*/:
                return this.mSharedPreferences.getString(APPLICATION_KEY_OW, null);
            case Cocos2dxEditBox.kEndActionReturn /*3*/:
                return this.mSharedPreferences.getString(APPLICATION_KEY_IS, null);
            default:
                return applicationKey;
        }
    }

    public void setApplicationKey(String value, ProductType type) {
        Editor editor = this.mSharedPreferences.edit();
        switch (AnonymousClass1.$SwitchMap$com$ironsource$sdk$data$SSAEnums$ProductType[type.ordinal()]) {
            case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                editor.putString(APPLICATION_KEY_RV, value);
                break;
            case R.styleable.View_paddingStart /*2*/:
                editor.putString(APPLICATION_KEY_OW, value);
                break;
            case Cocos2dxEditBox.kEndActionReturn /*3*/:
                editor.putString(APPLICATION_KEY_IS, value);
                break;
        }
        editor.commit();
    }

    public String getUniqueId(String type) {
        String userUniqueId = "EMPTY_UNIQUE_ID";
        if (type.equalsIgnoreCase(ProductType.RewardedVideo.toString())) {
            return this.mSharedPreferences.getString(UNIQUE_ID_RV, null);
        }
        if (type.equalsIgnoreCase(ProductType.OfferWall.toString())) {
            return this.mSharedPreferences.getString(UNIQUE_ID_OW, null);
        }
        if (type.equalsIgnoreCase(ProductType.Interstitial.toString())) {
            return this.mSharedPreferences.getString(UNIQUE_ID_IS, null);
        }
        return userUniqueId;
    }

    public boolean setUniqueId(String value, String type) {
        Editor editor = this.mSharedPreferences.edit();
        if (type.equalsIgnoreCase(ProductType.RewardedVideo.toString())) {
            editor.putString(UNIQUE_ID_RV, value);
        } else if (type.equalsIgnoreCase(ProductType.OfferWall.toString())) {
            editor.putString(UNIQUE_ID_OW, value);
        } else if (type.equalsIgnoreCase(ProductType.Interstitial.toString())) {
            editor.putString(UNIQUE_ID_IS, value);
        }
        return editor.commit();
    }

    public String getCurrentSDKVersion() {
        return this.mSharedPreferences.getString(VERSION, "UN_VERSIONED");
    }

    public void setCurrentSDKVersion(String sdkVersion) {
        Editor editor = this.mSharedPreferences.edit();
        editor.putString(VERSION, sdkVersion);
        editor.commit();
    }

    public String getSDKDownloadUrl() {
        return this.mSharedPreferences.getString(SSA_SDK_DOWNLOAD_URL, null);
    }

    public String getCampaignLastUpdate(String campaign) {
        return this.mSharedPreferences.getString(campaign, null);
    }

    public void setCampaignLastUpdate(String campaign, String lastUpdate) {
        Editor editor = this.mSharedPreferences.edit();
        editor.putString(campaign, lastUpdate);
        editor.commit();
    }

    public void setUserID(String value, ProductType type) {
        Editor editor = this.mSharedPreferences.edit();
        switch (AnonymousClass1.$SwitchMap$com$ironsource$sdk$data$SSAEnums$ProductType[type.ordinal()]) {
            case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                editor.putString(USER_ID_RV, value);
                break;
            case R.styleable.View_paddingStart /*2*/:
                editor.putString(USER_ID_OW, value);
                break;
            case Cocos2dxEditBox.kEndActionReturn /*3*/:
                editor.putString(USER_ID_IS, value);
                break;
        }
        editor.commit();
    }

    public String getUniqueId(ProductType type) {
        String userUniqueId = "EMPTY_UNIQUE_ID";
        switch (AnonymousClass1.$SwitchMap$com$ironsource$sdk$data$SSAEnums$ProductType[type.ordinal()]) {
            case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                return this.mSharedPreferences.getString(UNIQUE_ID_RV, null);
            case R.styleable.View_paddingStart /*2*/:
                return this.mSharedPreferences.getString(UNIQUE_ID_OW, null);
            case Cocos2dxEditBox.kEndActionReturn /*3*/:
                return this.mSharedPreferences.getString(UNIQUE_ID_IS, null);
            default:
                return userUniqueId;
        }
    }

    public boolean setLatestCompeltionsTime(String timestamp, String applicationKey, String userId) {
        boolean z = false;
        String value = this.mSharedPreferences.getString("ssaUserData", null);
        if (!TextUtils.isEmpty(value)) {
            try {
                JSONObject ssaUserDataJson = new JSONObject(value);
                if (!ssaUserDataJson.isNull(applicationKey)) {
                    JSONObject applicationKeyJson = ssaUserDataJson.getJSONObject(applicationKey);
                    if (!applicationKeyJson.isNull(userId)) {
                        applicationKeyJson.getJSONObject(userId).put(EventEntry.COLUMN_NAME_TIMESTAMP, timestamp);
                        Editor editor = this.mSharedPreferences.edit();
                        editor.putString("ssaUserData", ssaUserDataJson.toString());
                        z = editor.commit();
                    }
                }
            } catch (JSONException e) {
                new IronSourceAsyncHttpRequestTask().execute(new String[]{Constants.NATIVE_EXCEPTION_BASE_URL + e.getStackTrace()[0].getMethodName()});
            }
        }
        return z;
    }

    public void setReportAppStarted(boolean value) {
        Editor editor = this.mSharedPreferences.edit();
        editor.putBoolean(IS_REPORTED, value);
        editor.apply();
    }

    public boolean getReportAppStarted() {
        return this.mSharedPreferences.getBoolean(IS_REPORTED, false);
    }
}
