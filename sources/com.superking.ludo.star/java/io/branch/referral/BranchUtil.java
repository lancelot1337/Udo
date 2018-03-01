package io.branch.referral;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import com.google.firebase.analytics.FirebaseAnalytics.Param;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;

class BranchUtil {
    static boolean isCustomDebugEnabled_ = false;

    BranchUtil() {
    }

    public static boolean isTestModeEnabled(Context context) {
        if (isCustomDebugEnabled_) {
            return isCustomDebugEnabled_;
        }
        String testModeKey = "io.branch.sdk.TestMode";
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), 128);
            if (ai.metaData != null && ai.metaData.containsKey(testModeKey)) {
                return ai.metaData.getBoolean(testModeKey, false);
            }
            Resources resources = context.getResources();
            return Boolean.parseBoolean(resources.getString(resources.getIdentifier(testModeKey, "string", context.getPackageName())));
        } catch (Exception e) {
            return false;
        }
    }

    public static String formatAndStringifyLinkParam(JSONObject params) {
        return stringifyAndAddSource(filterOutBadCharacters(params));
    }

    public static String stringifyAndAddSource(JSONObject params) {
        if (params == null) {
            params = new JSONObject();
        }
        try {
            params.put(Param.SOURCE, "android");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return params.toString();
    }

    public static JSONObject filterOutBadCharacters(JSONObject inputObj) {
        JSONObject filteredObj = new JSONObject();
        if (inputObj != null) {
            Iterator<String> keys = inputObj.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                try {
                    if (inputObj.has(key) && inputObj.get(key).getClass().equals(String.class)) {
                        filteredObj.put(key, inputObj.getString(key).replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\\\""));
                    } else if (inputObj.has(key)) {
                        filteredObj.put(key, inputObj.get(key));
                    }
                } catch (JSONException e) {
                }
            }
        }
        return filteredObj;
    }

    public static Drawable getDrawable(@NonNull Context context, @DrawableRes int drawableID) {
        if (VERSION.SDK_INT >= 21) {
            return context.getResources().getDrawable(drawableID, context.getTheme());
        }
        return context.getResources().getDrawable(drawableID);
    }
}
