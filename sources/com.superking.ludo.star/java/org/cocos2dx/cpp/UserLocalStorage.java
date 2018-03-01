package org.cocos2dx.cpp;

import android.util.Log;
import org.cocos2dx.lib.BuildConfig;
import org.cocos2dx.lib.Cocos2dxHelper;
import org.json.JSONException;
import org.json.JSONObject;

public class UserLocalStorage {
    private static final String SK_PID_LAST_GIFT_TIME = "SK:user:pid_last_gift_time";
    private static final String TAG = "UserLocalStorageJava";

    public static boolean canReceieveGiftFrom(String pid) {
        try {
            if (EMHelpers.secondsNow() - new JSONObject(Cocos2dxHelper.getStringForKey(SK_PID_LAST_GIFT_TIME, BuildConfig.FLAVOR)).optLong(pid, 0) < 86400) {
                return false;
            }
        } catch (JSONException e) {
            Log.d(TAG, "Problem parsing data received");
        }
        return true;
    }
}
