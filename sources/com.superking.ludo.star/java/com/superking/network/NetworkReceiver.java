package com.superking.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import org.cocos2dx.cpp.AppActivity;
import org.cocos2dx.lib.BuildConfig;

public class NetworkReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkReceiver";
    private int mConnectionType = -1;

    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "action: " + intent.getAction());
        Log.d(TAG, "component: " + intent.getComponent());
        Bundle extras = intent.getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                Log.d(TAG, "key [" + key + "]: " + extras.get(key));
            }
        } else {
            Log.d(TAG, "no extras");
        }
        Log.d(TAG, "FailOver: " + Boolean.toString(intent.getBooleanExtra("isFailover", false)));
        Log.d(TAG, "NoConnectivity: " + Boolean.toString(intent.getBooleanExtra("noConnectivity", false)));
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.getType() != this.mConnectionType) {
                this.mConnectionType = networkInfo.getType();
                if (this.mConnectionType == 0 || this.mConnectionType == 1) {
                    AppActivity.publishEvent("eventConnectionTypeChange", BuildConfig.FLAVOR);
                }
            }
        }
    }
}
