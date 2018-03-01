package io.branch.referral;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import com.facebook.internal.NativeProtocol;
import com.facebook.share.internal.ShareConstants;
import cz.msebera.android.httpclient.HttpStatus;
import io.branch.referral.Defines.Jsonkey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import org.cocos2dx.lib.BuildConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PrefHelper {
    private static boolean BNC_App_Listing = true;
    private static boolean BNC_Dev_Debug = false;
    private static boolean BNC_Logging = false;
    private static String Branch_Key = null;
    private static final int INTERVAL_RETRY = 1000;
    private static final String KEY_ACTIONS = "bnc_actions";
    private static final String KEY_APP_LINK = "bnc_app_link";
    private static final String KEY_APP_VERSION = "bnc_app_version";
    private static final String KEY_BRANCH_ANALYTICAL_DATA = "bnc_branch_analytical_data";
    private static final String KEY_BRANCH_KEY = "bnc_branch_key";
    private static final String KEY_BRANCH_VIEW_NUM_OF_USE = "bnc_branch_view_use";
    private static final String KEY_BUCKETS = "bnc_buckets";
    private static final String KEY_CREDIT_BASE = "bnc_credit_base_";
    private static final String KEY_DEVICE_FINGERPRINT_ID = "bnc_device_fingerprint_id";
    private static final String KEY_EXTERNAL_INTENT_EXTRA = "bnc_external_intent_extra";
    private static final String KEY_EXTERNAL_INTENT_URI = "bnc_external_intent_uri";
    private static final String KEY_IDENTITY = "bnc_identity";
    private static final String KEY_IDENTITY_ID = "bnc_identity_id";
    private static final String KEY_INSTALL_PARAMS = "bnc_install_params";
    private static final String KEY_INSTALL_REFERRER = "bnc_install_referrer";
    private static final String KEY_IS_REFERRABLE = "bnc_is_referrable";
    private static final String KEY_IS_TRIGGERED_BY_FB_APP_LINK = "bnc_triggered_by_fb_app_link";
    private static final String KEY_LAST_READ_SYSTEM = "bnc_system_read_date";
    private static final String KEY_LAST_STRONG_MATCH_TIME = "bnc_branch_strong_match_time";
    private static final String KEY_LINK_CLICK_ID = "bnc_link_click_id";
    private static final String KEY_LINK_CLICK_IDENTIFIER = "bnc_link_click_identifier";
    private static final String KEY_PUSH_IDENTIFIER = "bnc_push_identifier";
    private static final String KEY_RETRY_COUNT = "bnc_retry_count";
    private static final String KEY_RETRY_INTERVAL = "bnc_retry_interval";
    private static final String KEY_SESSION_ID = "bnc_session_id";
    private static final String KEY_SESSION_PARAMS = "bnc_session_params";
    private static final String KEY_TIMEOUT = "bnc_timeout";
    private static final String KEY_TOTAL_BASE = "bnc_total_base_";
    private static final String KEY_UNIQUE_BASE = "bnc_balance_base_";
    private static final String KEY_USER_URL = "bnc_user_url";
    private static final int MAX_RETRIES = 3;
    public static final String NO_STRING_VALUE = "bnc_no_value";
    private static final String SHARED_PREF_FILE = "branch_referral_shared_pref";
    private static final int TIMEOUT = 5500;
    private static PrefHelper prefHelper_;
    private static JSONObject savedAnalyticsData_;
    private SharedPreferences appSharedPrefs_;
    private Context context_;
    private Editor prefsEditor_;
    private BranchRemoteInterface remoteInterface_;
    private JSONObject requestMetadata;

    public static class DebugNetworkCallback implements NetworkCallback {
        private int connectionStatus;

        public int getConnectionStatus() {
            return this.connectionStatus;
        }

        public void finished(ServerResponse serverResponse) {
            if (serverResponse != null) {
                try {
                    this.connectionStatus = serverResponse.getStatusCode();
                    String requestTag = serverResponse.getTag();
                    if (this.connectionStatus < HttpStatus.SC_BAD_REQUEST || this.connectionStatus >= HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                        if (this.connectionStatus == HttpStatus.SC_OK) {
                            return;
                        }
                        if (this.connectionStatus == RemoteInterface.NO_CONNECTIVITY_STATUS) {
                            Log.i("BranchSDK", "Branch API Error: poor network connectivity. Please try again later.");
                        } else {
                            Log.i("BranchSDK", "Trouble reaching server. Please try again in a few minutes.");
                        }
                    } else if (serverResponse.getObject() != null && serverResponse.getObject().has(NativeProtocol.BRIDGE_ARG_ERROR_BUNDLE) && serverResponse.getObject().getJSONObject(NativeProtocol.BRIDGE_ARG_ERROR_BUNDLE).has(ShareConstants.WEB_DIALOG_PARAM_MESSAGE)) {
                        Log.i("BranchSDK", "Branch API Error: " + serverResponse.getObject().getJSONObject(NativeProtocol.BRIDGE_ARG_ERROR_BUNDLE).getString(ShareConstants.WEB_DIALOG_PARAM_MESSAGE));
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private PrefHelper(Context context) {
        this.appSharedPrefs_ = context.getSharedPreferences(SHARED_PREF_FILE, 0);
        this.prefsEditor_ = this.appSharedPrefs_.edit();
        this.context_ = context;
        this.requestMetadata = new JSONObject();
    }

    public static PrefHelper getInstance(Context context) {
        if (prefHelper_ == null) {
            prefHelper_ = new PrefHelper(context);
        }
        return prefHelper_;
    }

    public String getAPIBaseUrl() {
        return "https://api.branch.io/";
    }

    public void setTimeout(int timeout) {
        setInteger(KEY_TIMEOUT, timeout);
    }

    public int getTimeout() {
        return getInteger(KEY_TIMEOUT, TIMEOUT);
    }

    public void setRetryCount(int retry) {
        setInteger(KEY_RETRY_COUNT, retry);
    }

    public int getRetryCount() {
        return getInteger(KEY_RETRY_COUNT, MAX_RETRIES);
    }

    public void setRetryInterval(int retryInt) {
        setInteger(KEY_RETRY_INTERVAL, retryInt);
    }

    public int getRetryInterval() {
        return getInteger(KEY_RETRY_INTERVAL, INTERVAL_RETRY);
    }

    public void setAppVersion(String version) {
        setString(KEY_APP_VERSION, version);
    }

    public String getAppVersion() {
        return getString(KEY_APP_VERSION);
    }

    public boolean setBranchKey(String key) {
        Branch_Key = key;
        String currentBranchKey = getString(KEY_BRANCH_KEY);
        if (key != null && currentBranchKey != null && currentBranchKey.equals(key)) {
            return false;
        }
        clearPrefOnBranchKeyChange();
        setString(KEY_BRANCH_KEY, key);
        return true;
    }

    public String getBranchKey() {
        if (Branch_Key == null) {
            Branch_Key = getString(KEY_BRANCH_KEY);
        }
        return Branch_Key;
    }

    public String readBranchKey(boolean isLive) {
        String branchKey = null;
        String metaDataKey = isLive ? "io.branch.sdk.BranchKey" : "io.branch.sdk.BranchKey.test";
        if (!isLive) {
            setExternDebug();
        }
        try {
            ApplicationInfo ai = this.context_.getPackageManager().getApplicationInfo(this.context_.getPackageName(), 128);
            if (ai.metaData != null) {
                branchKey = ai.metaData.getString(metaDataKey);
                if (branchKey == null && !isLive) {
                    branchKey = ai.metaData.getString("io.branch.sdk.BranchKey");
                }
            }
        } catch (NameNotFoundException e) {
        }
        if (TextUtils.isEmpty(branchKey)) {
            try {
                Resources resources = this.context_.getResources();
                branchKey = resources.getString(resources.getIdentifier(metaDataKey, "string", this.context_.getPackageName()));
            } catch (Exception e2) {
            }
        }
        if (branchKey == null) {
            return NO_STRING_VALUE;
        }
        return branchKey;
    }

    public void setDeviceFingerPrintID(String device_fingerprint_id) {
        setString(KEY_DEVICE_FINGERPRINT_ID, device_fingerprint_id);
    }

    public String getDeviceFingerPrintID() {
        return getString(KEY_DEVICE_FINGERPRINT_ID);
    }

    public void setSessionID(String session_id) {
        setString(KEY_SESSION_ID, session_id);
    }

    public String getSessionID() {
        return getString(KEY_SESSION_ID);
    }

    public void setIdentityID(String identity_id) {
        setString(KEY_IDENTITY_ID, identity_id);
    }

    public String getIdentityID() {
        return getString(KEY_IDENTITY_ID);
    }

    public void setIdentity(String identity) {
        setString(KEY_IDENTITY, identity);
    }

    public String getIdentity() {
        return getString(KEY_IDENTITY);
    }

    public void setLinkClickID(String link_click_id) {
        setString(KEY_LINK_CLICK_ID, link_click_id);
    }

    public String getLinkClickID() {
        return getString(KEY_LINK_CLICK_ID);
    }

    public void setIsAppLinkTriggeredInit(Boolean isAppLinkTriggered) {
        setBool(KEY_IS_TRIGGERED_BY_FB_APP_LINK, isAppLinkTriggered);
    }

    public boolean getIsAppLinkTriggeredInit() {
        return getBool(KEY_IS_TRIGGERED_BY_FB_APP_LINK);
    }

    public void setExternalIntentUri(String uri) {
        setString(KEY_EXTERNAL_INTENT_URI, uri);
    }

    public String getExternalIntentUri() {
        return getString(KEY_EXTERNAL_INTENT_URI);
    }

    public void setExternalIntentExtra(String extras) {
        setString(KEY_EXTERNAL_INTENT_EXTRA, extras);
    }

    public String getExternalIntentExtra() {
        return getString(KEY_EXTERNAL_INTENT_EXTRA);
    }

    public void setLinkClickIdentifier(String identifier) {
        setString(KEY_LINK_CLICK_IDENTIFIER, identifier);
    }

    public String getLinkClickIdentifier() {
        return getString(KEY_LINK_CLICK_IDENTIFIER);
    }

    public void setAppLink(String appLinkUrl) {
        setString(KEY_APP_LINK, appLinkUrl);
    }

    public String getAppLink() {
        return getString(KEY_APP_LINK);
    }

    public void setPushIdentifier(String pushIdentifier) {
        setString(KEY_PUSH_IDENTIFIER, pushIdentifier);
    }

    public String getPushIdentifier() {
        return getString(KEY_PUSH_IDENTIFIER);
    }

    public String getSessionParams() {
        return getString(KEY_SESSION_PARAMS);
    }

    public void setSessionParams(String params) {
        setString(KEY_SESSION_PARAMS, params);
    }

    public String getInstallParams() {
        return getString(KEY_INSTALL_PARAMS);
    }

    public void setInstallParams(String params) {
        setString(KEY_INSTALL_PARAMS, params);
    }

    public void setInstallReferrerParams(String params) {
        setString(KEY_INSTALL_REFERRER, params);
    }

    public String getInstallReferrerParams() {
        return getString(KEY_INSTALL_REFERRER);
    }

    public void setUserURL(String user_url) {
        setString(KEY_USER_URL, user_url);
    }

    public String getUserURL() {
        return getString(KEY_USER_URL);
    }

    public int getIsReferrable() {
        return getInteger(KEY_IS_REFERRABLE);
    }

    public void setIsReferrable() {
        setInteger(KEY_IS_REFERRABLE, 1);
    }

    public void clearIsReferrable() {
        setInteger(KEY_IS_REFERRABLE, 0);
    }

    public void clearSystemReadStatus() {
        setLong(KEY_LAST_READ_SYSTEM, Calendar.getInstance().getTimeInMillis() / 1000);
    }

    public void clearUserValues() {
        Iterator i$ = getBuckets().iterator();
        while (i$.hasNext()) {
            setCreditCount((String) i$.next(), 0);
        }
        setBuckets(new ArrayList());
        i$ = getActions().iterator();
        while (i$.hasNext()) {
            String action = (String) i$.next();
            setActionTotalCount(action, 0);
            setActionUniqueCount(action, 0);
        }
        setActions(new ArrayList());
    }

    private ArrayList<String> getBuckets() {
        String bucketList = getString(KEY_BUCKETS);
        if (bucketList.equals(NO_STRING_VALUE)) {
            return new ArrayList();
        }
        return deserializeString(bucketList);
    }

    private void setBuckets(ArrayList<String> buckets) {
        if (buckets.size() == 0) {
            setString(KEY_BUCKETS, NO_STRING_VALUE);
        } else {
            setString(KEY_BUCKETS, serializeArrayList(buckets));
        }
    }

    public void setCreditCount(int count) {
        setCreditCount(Jsonkey.DefaultBucket.getKey(), count);
    }

    public void setCreditCount(String bucket, int count) {
        ArrayList<String> buckets = getBuckets();
        if (!buckets.contains(bucket)) {
            buckets.add(bucket);
            setBuckets(buckets);
        }
        setInteger(KEY_CREDIT_BASE + bucket, count);
    }

    public int getCreditCount() {
        return getCreditCount(Jsonkey.DefaultBucket.getKey());
    }

    public int getCreditCount(String bucket) {
        return getInteger(KEY_CREDIT_BASE + bucket);
    }

    private ArrayList<String> getActions() {
        String actionList = getString(KEY_ACTIONS);
        if (actionList.equals(NO_STRING_VALUE)) {
            return new ArrayList();
        }
        return deserializeString(actionList);
    }

    private void setActions(ArrayList<String> actions) {
        if (actions.size() == 0) {
            setString(KEY_ACTIONS, NO_STRING_VALUE);
        } else {
            setString(KEY_ACTIONS, serializeArrayList(actions));
        }
    }

    public void setActionTotalCount(String action, int count) {
        ArrayList<String> actions = getActions();
        if (!actions.contains(action)) {
            actions.add(action);
            setActions(actions);
        }
        setInteger(KEY_TOTAL_BASE + action, count);
    }

    public void setActionUniqueCount(String action, int count) {
        setInteger(KEY_UNIQUE_BASE + action, count);
    }

    public int getActionTotalCount(String action) {
        return getInteger(KEY_TOTAL_BASE + action);
    }

    public int getActionUniqueCount(String action) {
        return getInteger(KEY_UNIQUE_BASE + action);
    }

    private String serializeArrayList(ArrayList<String> strings) {
        String retString = BuildConfig.FLAVOR;
        Iterator i$ = strings.iterator();
        while (i$.hasNext()) {
            retString = retString + ((String) i$.next()) + ",";
        }
        return retString.substring(0, retString.length() - 1);
    }

    private ArrayList<String> deserializeString(String list) {
        ArrayList<String> strings = new ArrayList();
        Collections.addAll(strings, list.split(","));
        return strings;
    }

    public int getInteger(String key) {
        return getInteger(key, 0);
    }

    public int getInteger(String key, int defaultValue) {
        return prefHelper_.appSharedPrefs_.getInt(key, defaultValue);
    }

    public long getLong(String key) {
        return prefHelper_.appSharedPrefs_.getLong(key, 0);
    }

    public float getFloat(String key) {
        return prefHelper_.appSharedPrefs_.getFloat(key, 0.0f);
    }

    public String getString(String key) {
        return prefHelper_.appSharedPrefs_.getString(key, NO_STRING_VALUE);
    }

    public boolean getBool(String key) {
        return prefHelper_.appSharedPrefs_.getBoolean(key, false);
    }

    public void setInteger(String key, int value) {
        prefHelper_.prefsEditor_.putInt(key, value);
        prefHelper_.prefsEditor_.apply();
    }

    public void setLong(String key, long value) {
        prefHelper_.prefsEditor_.putLong(key, value);
        prefHelper_.prefsEditor_.apply();
    }

    public void setFloat(String key, float value) {
        prefHelper_.prefsEditor_.putFloat(key, value);
        prefHelper_.prefsEditor_.apply();
    }

    public void setString(String key, String value) {
        prefHelper_.prefsEditor_.putString(key, value);
        prefHelper_.prefsEditor_.apply();
    }

    public void setBool(String key, Boolean value) {
        prefHelper_.prefsEditor_.putBoolean(key, value.booleanValue());
        prefHelper_.prefsEditor_.apply();
    }

    public void updateBranchViewUsageCount(String branchViewId) {
        setInteger("bnc_branch_view_use_" + branchViewId, getBranchViewUsageCount(branchViewId) + 1);
    }

    public int getBranchViewUsageCount(String branchViewId) {
        return getInteger("bnc_branch_view_use_" + branchViewId, 0);
    }

    public JSONObject getBranchAnalyticsData() {
        if (savedAnalyticsData_ != null) {
            return savedAnalyticsData_;
        }
        String savedAnalyticsData = getString(KEY_BRANCH_ANALYTICAL_DATA);
        JSONObject analyticsDataObject = new JSONObject();
        if (TextUtils.isEmpty(savedAnalyticsData) || savedAnalyticsData.equals(NO_STRING_VALUE)) {
            return analyticsDataObject;
        }
        try {
            return new JSONObject(savedAnalyticsData);
        } catch (JSONException e) {
            return analyticsDataObject;
        }
    }

    public void clearBranchAnalyticsData() {
        savedAnalyticsData_ = null;
        setString(KEY_BRANCH_ANALYTICAL_DATA, BuildConfig.FLAVOR);
    }

    public void saveBranchAnalyticsData(JSONObject analyticsData) {
        String sessionID = getSessionID();
        if (!sessionID.equals(NO_STRING_VALUE)) {
            if (savedAnalyticsData_ == null) {
                savedAnalyticsData_ = getBranchAnalyticsData();
            }
            try {
                JSONArray viewDataArray;
                if (savedAnalyticsData_.has(sessionID)) {
                    viewDataArray = savedAnalyticsData_.getJSONArray(sessionID);
                } else {
                    viewDataArray = new JSONArray();
                    savedAnalyticsData_.put(sessionID, viewDataArray);
                }
                viewDataArray.put(analyticsData);
                setString(KEY_BRANCH_ANALYTICAL_DATA, savedAnalyticsData_.toString());
            } catch (JSONException e) {
            }
        }
    }

    public void saveLastStrongMatchTime(long strongMatchCheckTime) {
        setLong(KEY_LAST_STRONG_MATCH_TIME, strongMatchCheckTime);
    }

    public long getLastStrongMatchTime() {
        return getLong(KEY_LAST_STRONG_MATCH_TIME);
    }

    private void clearPrefOnBranchKeyChange() {
        String linkClickID = getLinkClickID();
        String linkClickIdentifier = getLinkClickIdentifier();
        String appLink = getAppLink();
        String pushIdentifier = getPushIdentifier();
        this.prefsEditor_.clear();
        setLinkClickID(linkClickID);
        setLinkClickIdentifier(linkClickIdentifier);
        setAppLink(appLink);
        setPushIdentifier(pushIdentifier);
        prefHelper_.prefsEditor_.apply();
    }

    public void setExternDebug() {
        BNC_Dev_Debug = true;
    }

    public boolean getExternDebug() {
        return BNC_Dev_Debug;
    }

    public void setLogging(boolean logging) {
        BNC_Logging = logging;
    }

    public void disableExternAppListing() {
        BNC_App_Listing = false;
    }

    public boolean getExternAppListing() {
        return BNC_App_Listing;
    }

    public void setRequestMetadata(@NonNull String key, @NonNull String value) {
        if (key != null) {
            if (this.requestMetadata.has(key) && value == null) {
                this.requestMetadata.remove(key);
            }
            try {
                this.requestMetadata.put(key, value);
            } catch (JSONException e) {
            }
        }
    }

    public JSONObject getRequestMetadata() {
        return this.requestMetadata;
    }

    public void log(String tag, String message) {
        if (BNC_Dev_Debug || BNC_Logging) {
            Log.i(tag, message);
        }
    }

    public static void Debug(String tag, String message) {
        if (prefHelper_ != null) {
            prefHelper_.log(tag, message);
        } else if (BNC_Dev_Debug || BNC_Logging) {
            Log.i(tag, message);
        }
    }
}
