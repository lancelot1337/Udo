package org.cocos2dx.cpp;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import com.bugsnag.android.Bugsnag;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;
import com.superking.firebase.FirebaseMessageReceiver;
import com.superking.iap.PaymentInterface;
import com.superking.network.NetworkReceiver;
import io.branch.referral.Branch;
import io.branch.referral.Branch.BranchReferralInitListener;
import io.branch.referral.BranchError;
import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.Fabric.Builder;
import io.fabric.sdk.android.Kit;
import java.util.Date;
import org.cocos2dx.lib.BuildConfig;
import org.cocos2dx.lib.Cocos2dxActivity;
import org.cocos2dx.lib.Cocos2dxGLSurfaceView;
import org.cocos2dx.lib.Cocos2dxHelper;
import org.json.JSONObject;

public class AppActivity extends Cocos2dxActivity {
    private static final String TAG = "AppActivity";
    private static boolean appInForeground = false;
    private static GameServiceInterface mGPlayService = null;
    private static AppActivity mStaticInstance = null;
    private Cocos2dxGLSurfaceView mGlSurfaceView = null;
    private NetworkReceiver mNetworkReceiver;

    private native void nativeInitCrashlytics();

    public native void publishEventGL(String str, String str2);

    public static AppActivity getInstance() {
        return mStaticInstance;
    }

    public static boolean isAppInForeground() {
        return appInForeground;
    }

    public static void publishEvent(final String event, final String data) {
        if (mStaticInstance != null) {
            mStaticInstance.runOnGLThread(new Runnable() {
                public void run() {
                    if (AppActivity.mStaticInstance != null) {
                        AppActivity.mStaticInstance.publishEventGL(event, data);
                    }
                }
            });
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getGLSurfaceView().setMultipleTouchEnabled(false);
        PaymentInterface.getInstance().onCreate(this);
        mGPlayService = GameServiceInterface.getInstance(this);
        Bugsnag.init(this);
        Fabric.with(new Builder(this).debuggable(false).kits(new Kit[]{new Crashlytics(), new CrashlyticsNdk()}).build());
        FacebookInterface.initFacebook(this);
        this.mNetworkReceiver = new NetworkReceiver();
        registerReceiver(this.mNetworkReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        nativeInitCrashlytics();
        AdsInterface.init(this);
    }

    public Cocos2dxGLSurfaceView onCreateView() {
        this.mGlSurfaceView = super.onCreateView();
        this.mGlSurfaceView.setKeepScreenOn(true);
        mStaticInstance = this;
        return this.mGlSurfaceView;
    }

    protected void onStart() {
        super.onStart();
        final Branch branch = Branch.getInstance();
        if (branch != null) {
            branch.initSession(new BranchReferralInitListener() {
                public void onInitFinished(JSONObject initParam, BranchError error) {
                    if (error == null && AppActivity.mStaticInstance != null) {
                        JSONObject param = branch.getLatestReferringParams();
                        if (param != null && param.has("dataJson")) {
                            AppActivity.getInstance().checkBranchParams(param);
                        } else if (initParam == null || !initParam.has("dataJson")) {
                            AppActivity.getInstance().checkBranchParams(branch.getFirstReferringParams());
                        } else {
                            AppActivity.getInstance().checkBranchParams(initParam);
                        }
                    } else if (error != null && error.getMessage() != null) {
                        Log.d(AppActivity.TAG, error.getMessage());
                    }
                }
            }, getIntent().getData(), (Activity) this);
        }
    }

    protected void onResume() {
        super.onResume();
        AdsInterface.onResume();
        appInForeground = true;
        Intent intent = getIntent();
        if (intent != null) {
            Bundle data = intent.getExtras();
            intent.replaceExtras(new Bundle());
            if (!(data == null || data.isEmpty())) {
                String type = data.getString("TY");
                if (type != null && type.equalsIgnoreCase("IV")) {
                    String roomId = data.getString("RI");
                    String name = data.getString("NM");
                    String snuid = data.getString("fsnuid");
                    if (Cocos2dxHelper.getStringForKey("SK:user:game_centre_id", BuildConfig.FLAVOR).equals(data.getString("tsnuid"))) {
                        int roomType = 1;
                        try {
                            roomType = Integer.parseInt(data.getString("RT"));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                        int roomMode = 0;
                        if (data.containsKey("RM")) {
                            try {
                                roomMode = Integer.parseInt(data.getString("RM"));
                            } catch (NumberFormatException e2) {
                                e2.printStackTrace();
                            }
                        }
                        int gameMode = 3;
                        if (data.containsKey("GM")) {
                            try {
                                gameMode = Integer.parseInt(data.getString("GM"));
                            } catch (NumberFormatException e22) {
                                e22.printStackTrace();
                            }
                        }
                        long cost = 0;
                        if (data.containsKey("CS")) {
                            try {
                                cost = Long.parseLong(data.getString("CS"));
                            } catch (NumberFormatException e222) {
                                e222.printStackTrace();
                            }
                        }
                        String inviteId = BuildConfig.FLAVOR;
                        if (data.containsKey("ID")) {
                            inviteId = data.getString("ID");
                        }
                        if (!(roomId == null || roomId.isEmpty())) {
                            FirebaseMessageReceiver.nativeOnGameInvite(roomId, name, snuid, roomType, roomMode, gameMode, cost, inviteId);
                        }
                    }
                }
            }
        }
        try {
            ((NotificationManager) getSystemService("notification")).cancelAll();
        } catch (Exception e3) {
        }
        GameServiceInterface.getInstance().onResume();
    }

    protected void onPause() {
        super.onPause();
        appInForeground = false;
        AdsInterface.onPause();
    }

    protected void onStop() {
        super.onStop();
        appInForeground = false;
        GameServiceInterface.getInstance().onStop();
    }

    public void onDestroy() {
        super.onDestroy();
        appInForeground = false;
        mStaticInstance = null;
        PaymentInterface.getInstance().onDestroy();
        unregisterReceiver(this.mNetworkReceiver);
        AdsInterface.onDestroy();
    }

    protected void onActivityResult(int request, int response, Intent data) {
        if (!PaymentInterface.getInstance().handleActivityResult(request, response, data)) {
            super.onActivityResult(request, response, data);
            if (!FacebookInterface.callbackManager.onActivityResult(request, response, data)) {
                GameServiceInterface.getInstance().onActivityResult(request, response, data);
            }
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private void checkBranchParams(JSONObject param) {
        if (param != null && param.has("dataJson")) {
            long time = 0;
            try {
                time = Long.parseLong(param.optString("$exp_date"));
            } catch (NumberFormatException e) {
                Log.e(TAG, "Branch NumberFormatException" + e.getMessage());
            }
            if (time > new Date().getTime()) {
                String linkId = param.optString("~id");
                if (!Cocos2dxHelper.getStringForKey("SK:branch:lastId", BuildConfig.FLAVOR).equals(linkId)) {
                    Cocos2dxHelper.setStringForKey("SK:branch:lastId", linkId);
                    Cocos2dxHelper.setStringForKey("SK:branch:dataJson", param.optString("dataJson").replaceAll("\\\\", BuildConfig.FLAVOR));
                    Cocos2dxHelper.setDoubleForKey("SK:branch:expiry", (double) time);
                    publishEvent("eventBranchData", BuildConfig.FLAVOR);
                }
            }
        }
    }
}
