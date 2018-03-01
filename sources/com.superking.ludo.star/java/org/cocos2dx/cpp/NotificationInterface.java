package org.cocos2dx.cpp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.facebook.share.internal.ShareConstants;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import cz.msebera.android.httpclient.cookie.ClientCookie;
import java.util.Calendar;
import org.cocos2dx.lib.GameControllerDelegate;

public class NotificationInterface {
    private static final String TAG = "NotificationInterface";

    public static void queueLocalNotif(int intentId, int notificationType, int secondsFromNow, String title, String action, String version, String data) {
        Log.d(TAG, "super1 JAVA queueLocalNotif Title:" + title + "type :" + notificationType + "Action:" + action + "Seconds:" + secondsFromNow + "data:" + data + " version: " + version);
        AppActivity app = AppActivity.getInstance();
        if (app != null) {
            Intent myIntent = new Intent(app, LocalNotificationReceiver.class);
            AlarmManager alarmManager = (AlarmManager) app.getSystemService("alarm");
            Bundle b = new Bundle();
            b.putInt("notificationType", notificationType);
            b.putString(ShareConstants.WEB_DIALOG_PARAM_TITLE, title);
            b.putString(ParametersKeys.ACTION, action);
            b.putBoolean("local", true);
            b.putString(ClientCookie.VERSION_ATTR, version);
            b.putString(EventEntry.COLUMN_NAME_DATA, data);
            myIntent.putExtras(b);
            myIntent.addFlags(268435456);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(app, intentId, myIntent, 134217728);
            alarmManager.cancel(pendingIntent);
            alarmManager.set(0, Calendar.getInstance().getTimeInMillis() + ((long) (secondsFromNow * GameControllerDelegate.THUMBSTICK_LEFT_X)), pendingIntent);
            Log.d(TAG, "super1 Notication set");
            return;
        }
        Log.d(TAG, "super1 App value return null");
    }

    public static void removeLocalNotif(int intentId) {
        Log.d(TAG, "super1 JAVA removeLocalNotif id:" + intentId);
        AppActivity app = AppActivity.getInstance();
        if (app != null) {
            ((AlarmManager) app.getSystemService("alarm")).cancel(PendingIntent.getBroadcast(app, intentId, new Intent(app, LocalNotificationReceiver.class), 134217728));
        }
    }
}
