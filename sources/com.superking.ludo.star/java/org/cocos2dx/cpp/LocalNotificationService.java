package org.cocos2dx.cpp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import com.facebook.share.internal.ShareConstants;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import com.superking.ludo.star.R;
import cz.msebera.android.httpclient.cookie.ClientCookie;
import org.json.JSONException;
import org.json.JSONObject;

public class LocalNotificationService extends Service {
    private static final String TAG = "NotificationReceiver";
    private NotificationManager mManager;

    public void onCreate() {
        Log.d(TAG, "super1 LocalNotificationService onCreate");
        super.onCreate();
    }

    public void onStart(Intent intent, int startId) {
        Log.d(TAG, "super1 Service on start");
        if (intent != null) {
            Bundle b = intent.getExtras();
            String title = b.getString(ShareConstants.WEB_DIALOG_PARAM_TITLE);
            String action = b.getString(ParametersKeys.ACTION);
            Boolean local = Boolean.valueOf(b.getBoolean("local"));
            int notificationType = b.getInt("notificationType");
            String version = b.getString(ClientCookie.VERSION_ATTR);
            String data = b.getString(EventEntry.COLUMN_NAME_DATA);
            super.onStart(intent, startId);
            Log.d(TAG, "super1 Service on start - 2");
            this.mManager = (NotificationManager) getApplicationContext().getSystemService("notification");
            Intent intent1 = new Intent(getApplicationContext(), AppActivity.class);
            JSONObject paramList = new JSONObject();
            try {
                paramList.put("local", local);
                paramList.put(ClientCookie.VERSION_ATTR, version);
                paramList.put("notificationType", notificationType);
                paramList.put(EventEntry.COLUMN_NAME_DATA, data);
            } catch (JSONException e) {
                Log.e(TAG, "super1 Service on start - inside catch");
                e.printStackTrace();
            }
            intent1.setData(Uri.parse(paramList.toString()));
            Builder mNotifyBuilder = new Builder(this).setContentTitle(title).setContentText(action).setSmallIcon(R.mipmap.ic_launcher);
            mNotifyBuilder.setAutoCancel(true);
            mNotifyBuilder.setDefaults(-1);
            intent1.addFlags(603979776);
            mNotifyBuilder.setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, intent1, 134217728));
            AppActivity app = AppActivity.getInstance();
            if (app == null || !(app == null || AppActivity.isAppInForeground())) {
                this.mManager.notify(0, mNotifyBuilder.build());
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }
}
