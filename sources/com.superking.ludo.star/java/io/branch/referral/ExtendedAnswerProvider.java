package io.branch.referral;

import android.text.TextUtils;
import com.crashlytics.android.answers.shim.AnswersOptionalLogger;
import com.crashlytics.android.answers.shim.KitEvent;
import io.branch.referral.Defines.Jsonkey;
import java.util.Iterator;
import org.cocos2dx.lib.BuildConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class ExtendedAnswerProvider {
    private static final String CTRL_PARAM_NOTATION = "~";
    private static final String EXTRA_PARAM_NOTATION = "+";
    private static final String INNER_PARAM_NOTATION = ".";
    public static final String KIT_EVENT_INSTALL = "Branch Install";
    public static final String KIT_EVENT_OPEN = "Branch Open";
    public static final String KIT_EVENT_SHARE = "Branch Share";

    ExtendedAnswerProvider() {
    }

    public void provideData(String eventName, JSONObject eventData, String identityID) {
        try {
            KitEvent kitEvent = new KitEvent(eventName);
            if (eventData != null) {
                addJsonObjectToKitEvent(kitEvent, eventData, BuildConfig.FLAVOR);
                kitEvent.putAttribute(Jsonkey.BranchIdentity.getKey(), identityID);
                AnswersOptionalLogger.get().logKitEvent(kitEvent);
            }
        } catch (Throwable th) {
        }
    }

    private void addJsonObjectToKitEvent(KitEvent kitEvent, JSONObject jsonData, String keyPathPrepend) throws JSONException {
        Iterator<String> keyIterator = jsonData.keys();
        while (keyIterator.hasNext()) {
            String key = (String) keyIterator.next();
            Object value = jsonData.get(key);
            if (!key.startsWith(EXTRA_PARAM_NOTATION)) {
                if (value instanceof JSONObject) {
                    addJsonObjectToKitEvent(kitEvent, (JSONObject) value, keyPathPrepend + key + INNER_PARAM_NOTATION);
                } else if (value instanceof JSONArray) {
                    addJsonArrayToKitEvent(kitEvent, (JSONArray) value, key + INNER_PARAM_NOTATION);
                } else {
                    addBranchAttributes(kitEvent, keyPathPrepend, key, jsonData.getString(key));
                }
            }
        }
    }

    private void addJsonArrayToKitEvent(KitEvent kitEvent, JSONArray jsonArray, String keyPathPrepend) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            addBranchAttributes(kitEvent, keyPathPrepend, CTRL_PARAM_NOTATION + Integer.toString(i), jsonArray.getString(i));
        }
    }

    private void addBranchAttributes(KitEvent kitEvent, String keyPathPrepend, String key, String value) {
        if (!TextUtils.isEmpty(value)) {
            if (key.startsWith(CTRL_PARAM_NOTATION)) {
                kitEvent.putAttribute(keyPathPrepend.replaceFirst(CTRL_PARAM_NOTATION, BuildConfig.FLAVOR) + key.replaceFirst(CTRL_PARAM_NOTATION, BuildConfig.FLAVOR), value);
            } else if (key.equals("$" + Jsonkey.IdentityID.getKey())) {
                kitEvent.putAttribute(Jsonkey.ReferringBranchIdentity.getKey(), value);
            }
        }
    }
}
