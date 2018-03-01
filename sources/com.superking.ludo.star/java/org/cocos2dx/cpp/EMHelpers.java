package org.cocos2dx.cpp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import cz.msebera.android.httpclient.protocol.HTTP;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;

public class EMHelpers {
    public static final int ONE_DAY = 86400;

    public static Bundle jsonStringToBundle(String jsonString) {
        try {
            return jsonToBundle(toJsonObject(jsonString));
        } catch (JSONException e) {
            return null;
        }
    }

    public static JSONObject toJsonObject(String jsonString) throws JSONException {
        return new JSONObject(jsonString);
    }

    public static Bundle jsonToBundle(JSONObject jsonObject) throws JSONException {
        Bundle bundle = new Bundle();
        Iterator iter = jsonObject.keys();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            bundle.putString(key, jsonObject.getString(key));
        }
        return bundle;
    }

    public static String loadJSONFromAsset(Activity activity, String path) {
        try {
            InputStream is = activity.getAssets().open(path);
            try {
                byte[] buffer = new byte[is.available()];
                is.read(buffer);
                is.close();
                String result = new String(buffer, HTTP.UTF_8);
                return result;
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }
        } catch (FileNotFoundException ex2) {
            ex2.printStackTrace();
            return null;
        }
    }

    public static long secondsNow() {
        return System.currentTimeMillis() / 1000;
    }

    public static String getIronSrcAppId() {
        if (AppActivity.getInstance().getApplicationContext().getPackageName().contentEquals("com.superking.parchisi.star")) {
            Log.d("super1", "Returning IRON SRC APP KEY: PARCHISI STAR");
            return "624c8745";
        }
        Log.d("super1", "Returning IRON SRC APP KEY: LUDO STAR");
        return "6261a735";
    }
}
