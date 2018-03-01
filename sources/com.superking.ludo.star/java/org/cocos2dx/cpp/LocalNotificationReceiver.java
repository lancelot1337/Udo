package org.cocos2dx.cpp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LocalNotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "LocalNotificationReceiver";

    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "super1 onReceive LocalNotificationreceiver");
        if (intent != null && intent.getExtras() != null) {
            Log.d(TAG, "super1 onReceive LocalNotificationreceiver inside");
            Intent myIntent = new Intent(context, LocalNotificationService.class);
            myIntent.putExtras(intent.getExtras());
            context.startService(myIntent);
        }
    }
}
