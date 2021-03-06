package com.unity3d.ads.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.WebViewEventCategory;
import org.cocos2dx.lib.BuildConfig;
import org.json.JSONException;
import org.json.JSONObject;

public class BroadcastEventReceiver extends BroadcastReceiver {
    private String _name;

    public BroadcastEventReceiver(String name) {
        this._name = name;
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            String data = BuildConfig.FLAVOR;
            if (intent.getDataString() != null) {
                data = intent.getDataString();
            }
            JSONObject extras = new JSONObject();
            try {
                if (intent.getExtras() != null) {
                    Bundle bundle = intent.getExtras();
                    for (String key : bundle.keySet()) {
                        extras.put(key, bundle.get(key));
                    }
                }
            } catch (JSONException e) {
                DeviceLog.debug("JSONException when composing extras for broadcast action " + action + ": " + e.getMessage());
            }
            WebViewApp webViewApp = WebViewApp.getCurrentApp();
            if (webViewApp != null && webViewApp.isWebAppLoaded()) {
                webViewApp.sendEvent(WebViewEventCategory.BROADCAST, BroadcastEvent.ACTION, this._name, action, data, extras);
            }
        }
    }
}
