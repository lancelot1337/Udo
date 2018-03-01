package com.superking.firebase;

import android.util.Log;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import java.io.IOException;
import org.cocos2dx.cpp.AppActivity;
import org.cocos2dx.lib.Cocos2dxHelper;

public class FirebaseService extends FirebaseInstanceIdService {
    private final String TAG = "FirebaseService";

    private native void nativeOnTokenRefresh(String str);

    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        if (refreshedToken != null && !refreshedToken.isEmpty() && Cocos2dxHelper.getActivity() != null) {
            Cocos2dxHelper.setStringForKey("SK:user:deviceToken", refreshedToken);
            Log.d("FirebaseService", "Refreshed token: " + refreshedToken);
            if (AppActivity.isAppInForeground()) {
                nativeOnTokenRefresh(refreshedToken);
            }
        }
    }

    public static void logOut() {
        try {
            FirebaseInstanceId.getInstance().deleteInstanceId();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
