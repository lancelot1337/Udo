package com.unity3d.ads.api;

import android.app.Activity;
import android.net.Uri;
import com.facebook.applinks.AppLinkData;
import com.facebook.share.internal.ShareConstants;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.webview.bridge.WebViewCallback;
import com.unity3d.ads.webview.bridge.WebViewExposed;
import org.json.JSONArray;
import org.json.JSONObject;

public class Intent {

    public enum IntentError {
        COULDNT_PARSE_EXTRAS,
        COULDNT_PARSE_CATEGORIES,
        INTENT_WAS_NULL,
        ACTIVITY_WAS_NULL
    }

    @WebViewExposed
    public static void launch(JSONObject intentData, WebViewCallback callback) {
        android.content.Intent intent;
        String className = (String) intentData.opt("className");
        String packageName = (String) intentData.opt("packageName");
        String action = (String) intentData.opt(ParametersKeys.ACTION);
        String uri = (String) intentData.opt(ShareConstants.MEDIA_URI);
        String mimeType = (String) intentData.opt("mimeType");
        JSONArray categories = (JSONArray) intentData.opt("categories");
        Integer flags = (Integer) intentData.opt("flags");
        JSONArray extras = (JSONArray) intentData.opt(AppLinkData.ARGUMENTS_EXTRAS_KEY);
        if (packageName != null && className == null && action == null && mimeType == null) {
            intent = ClientProperties.getApplicationContext().getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent != null && flags.intValue() > -1) {
                intent.addFlags(flags.intValue());
            }
        } else {
            intent = new android.content.Intent();
            if (!(className == null || packageName == null)) {
                intent.setClassName(packageName, className);
            }
            if (action != null) {
                intent.setAction(action);
            }
            if (uri != null) {
                intent.setData(Uri.parse(uri));
            }
            if (mimeType != null) {
                intent.setType(mimeType);
            }
            if (flags != null && flags.intValue() > -1) {
                intent.setFlags(flags.intValue());
            }
            if (!setCategories(intent, categories)) {
                callback.error(IntentError.COULDNT_PARSE_CATEGORIES, categories);
            }
            if (!setExtras(intent, extras)) {
                callback.error(IntentError.COULDNT_PARSE_EXTRAS, extras);
            }
        }
        if (intent == null) {
            callback.error(IntentError.INTENT_WAS_NULL, new Object[0]);
        } else if (getStartingActivity() != null) {
            getStartingActivity().startActivity(intent);
            callback.invoke(new Object[0]);
        } else {
            callback.error(IntentError.ACTIVITY_WAS_NULL, new Object[0]);
        }
    }

    private static boolean setCategories(android.content.Intent intent, JSONArray categories) {
        if (categories != null && categories.length() > 0) {
            int i = 0;
            while (i < categories.length()) {
                try {
                    intent.addCategory(categories.getString(i));
                    i++;
                } catch (Exception e) {
                    DeviceLog.exception("Couldn't parse categories for intent", e);
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean setExtras(android.content.Intent intent, JSONArray extras) {
        if (extras != null) {
            int i = 0;
            while (i < extras.length()) {
                try {
                    JSONObject item = extras.getJSONObject(i);
                    if (!setExtra(intent, item.getString(ParametersKeys.KEY), item.get(ParametersKeys.VALUE))) {
                        return false;
                    }
                    i++;
                } catch (Exception e) {
                    DeviceLog.exception("Couldn't parse extras", e);
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean setExtra(android.content.Intent intent, String key, Object value) {
        if (value instanceof String) {
            intent.putExtra(key, (String) value);
        } else if (value instanceof Integer) {
            intent.putExtra(key, ((Integer) value).intValue());
        } else if (value instanceof Double) {
            intent.putExtra(key, ((Double) value).doubleValue());
        } else if (value instanceof Boolean) {
            intent.putExtra(key, ((Boolean) value).booleanValue());
        } else {
            DeviceLog.error("Unable to parse launch intent extra " + key);
            return false;
        }
        return true;
    }

    private static Activity getStartingActivity() {
        if (AdUnit.getAdUnitActivity() != null) {
            return AdUnit.getAdUnitActivity();
        }
        if (ClientProperties.getActivity() != null) {
            return ClientProperties.getActivity();
        }
        return null;
    }
}
