package com.superking.firebase;

import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Map;
import org.cocos2dx.cpp.AppActivity;
import org.cocos2dx.lib.BuildConfig;
import org.cocos2dx.lib.Cocos2dxHelper;

public class FirebaseMessageReceiver extends FirebaseMessagingService {
    private static final String TAG = "FCMReceiver";

    public static native void nativeOnGameInvite(String str, String str2, String str3, int i, int i2, int i3, long j, String str4);

    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        Log.d(TAG, data.toString());
        if (data != null && !data.isEmpty() && AppActivity.isAppInForeground() && AppActivity.getInstance() != null) {
            String type = (String) data.get("TY");
            if (type != null && type.equalsIgnoreCase("IV")) {
                String roomId = (String) data.get("RI");
                String name = (String) data.get("NM");
                String snuid = (String) data.get("fsnuid");
                if (Cocos2dxHelper.getStringForKey("SK:user:game_centre_id", BuildConfig.FLAVOR).equals((String) data.get("tsnuid"))) {
                    int roomType = 1;
                    try {
                        roomType = Integer.parseInt((String) data.get("RT"));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    int roomMode = 0;
                    if (data.containsKey("RM")) {
                        try {
                            roomMode = Integer.parseInt((String) data.get("RM"));
                        } catch (NumberFormatException e2) {
                            e2.printStackTrace();
                        }
                    }
                    int gameMode = 3;
                    if (data.containsKey("GM")) {
                        try {
                            gameMode = Integer.parseInt((String) data.get("GM"));
                        } catch (NumberFormatException e22) {
                            e22.printStackTrace();
                        }
                    }
                    long cost = 0;
                    if (data.containsKey("CS")) {
                        try {
                            cost = Long.parseLong((String) data.get("CS"));
                        } catch (NumberFormatException e222) {
                            e222.printStackTrace();
                        }
                    }
                    String inviteId = BuildConfig.FLAVOR;
                    if (data.containsKey("ID")) {
                        inviteId = (String) data.get("ID");
                    }
                    if (roomId != null && !roomId.isEmpty()) {
                        nativeOnGameInvite(roomId, name, snuid, roomType, roomMode, gameMode, cost, inviteId);
                    }
                }
            }
        }
    }
}
