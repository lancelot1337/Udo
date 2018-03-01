package org.cocos2dx.cpp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import com.facebook.internal.NativeProtocol;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Games.GamesOptions;
import com.google.android.gms.games.request.GameRequest;
import com.google.android.gms.games.request.GameRequestBuffer;
import com.google.android.gms.games.request.OnRequestReceivedListener;
import com.google.android.gms.games.request.Requests.LoadRequestsResult;
import com.google.android.gms.games.request.Requests.UpdateRequestsResult;
import com.superking.iap.FabricAnswersInterface;
import com.unity3d.ads.metadata.MediationMetaData;
import cz.msebera.android.httpclient.protocol.HTTP;
import io.branch.referral.R;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.cocos2dx.lib.BuildConfig;
import org.cocos2dx.lib.Cocos2dxActivity;
import org.cocos2dx.lib.Cocos2dxHandler;
import org.json.JSONException;
import org.json.JSONObject;

public class GameServiceInterface implements ConnectionCallbacks, OnConnectionFailedListener {
    private static final int REQUEST_ACHIEVEMENTS = 1002;
    private static final int REQUEST_LEADERBOARD = 20004;
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private static final int RESULT_RECONNECT_REQUIRED = 10001;
    private static final int SEND_GIFT_CODE = 20002;
    private static final int SEND_REQUEST_CODE = 20003;
    private static final int SHOW_INBOX = 20001;
    private static final String TAG = "GameServiceInterface";
    private static int mGiftCount = 0;
    private static GoogleApiClient mGoogleApiClient = null;
    private static GameServiceInterface mStaticInstance = null;
    private boolean eatAutoLogin = false;
    private final ResultCallback<LoadRequestsResult> mLoadRequestsCallback = new ResultCallback<LoadRequestsResult>() {
        public void onResult(LoadRequestsResult result) {
            GameServiceInterface.mGiftCount = GameServiceInterface.this.countNotExpired(result.getRequests(1));
            final int tempCount = GameServiceInterface.mGiftCount;
            AppActivity app = AppActivity.getInstance();
            if (app != null) {
                app.runOnGLThread(new Runnable() {
                    public void run() {
                        Log.i(GameServiceInterface.TAG, "Count of Gifts: >>> " + tempCount);
                        GameServiceInterface.getInstance().nativeGplayGiftCountChanged(tempCount);
                    }
                });
            }
        }
    };
    private final OnRequestReceivedListener mRequestListener = new OnRequestReceivedListener() {
        public void onRequestReceived(GameRequest request) {
            String requestStringResource;
            switch (request.getType()) {
                case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                    requestStringResource = "New Gift Receieved";
                    break;
                case R.styleable.View_paddingStart /*2*/:
                    requestStringResource = "New Wish Received";
                    break;
                default:
                    return;
            }
            Log.d(GameServiceInterface.TAG, "New Gift received. Calling ui thread.");
            AppActivity app = AppActivity.getInstance();
            if (app != null) {
                app.runOnGLThread(new Runnable() {
                    public void run() {
                        GameServiceInterface.getInstance().nativeGplayNewGiftReceived();
                    }
                });
            }
            GameServiceInterface.this.updateRequestCounts();
        }

        public void onRequestRemoved(String requestId) {
            GameServiceInterface.this.updateRequestCounts();
        }
    };
    private boolean mResolvingError = false;

    private native void nativeGPlayCallback(String str);

    private native void nativeGPlayLoginSuccess(Boolean bool);

    private native void nativeGPlayLogoutSuccess();

    private native void nativeGplayGiftCountChanged(int i);

    private native void nativeGplayNewGiftReceived();

    private native void nativeGplayRedeemGift(String str, String str2);

    public static GameServiceInterface getInstance(Activity activity) {
        if (mStaticInstance == null) {
            mStaticInstance = new GameServiceInterface(activity);
        }
        return mStaticInstance;
    }

    public static GameServiceInterface getInstance() {
        return mStaticInstance;
    }

    public GameServiceInterface(Activity activity) {
        mGoogleApiClient = new Builder(activity, this, this).addApi(Games.API, GamesOptions.builder().build()).addScope(Games.SCOPE_GAMES).build();
    }

    public void onStartCheckLogin() {
        this.eatAutoLogin = true;
        mGoogleApiClient.connect();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        this.mResolvingError = false;
        if (requestCode == SHOW_INBOX) {
            Log.i(TAG, "super1 onActivityResult showInbox");
            if (resultCode != -1 || intent == null) {
                updateRequestCounts();
                Log.e(TAG, "super1 onActivityResult Failed to process inbox result");
                return;
            }
            handleRequests(Games.Requests.getGameRequestsFromInboxResponse(intent));
        } else if (requestCode == SEND_REQUEST_CODE) {
            Log.i(TAG, "super1 onActivityResult sendRequestCode");
            if (resultCode == 10007) {
                Log.i(TAG, "super1 onActivityResult failedTo send Request");
            }
        } else if (requestCode == SEND_GIFT_CODE) {
            Log.i(TAG, "super1 onActivityResult sendGiftCode");
            if (resultCode == 10007) {
                Log.i(TAG, "super1 onActivityResult failedTo send Gift Code");
            }
        } else if (resultCode == -1) {
            Log.d(TAG, "super1 Result OK");
            if (mGoogleApiClient != null && !mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }
        } else if (requestCode == REQUEST_ACHIEVEMENTS) {
            Log.d(TAG, "super1 REQUEST_ACHIEVEMENTS");
            if (resultCode == RESULT_RECONNECT_REQUIRED && mGoogleApiClient != null) {
                mGoogleApiClient.disconnect();
                AppActivity.getInstance().runOnGLThread(new Runnable() {
                    public void run() {
                        GameServiceInterface.this.nativeGPlayCallback("disconnect");
                        GameServiceInterface.this.nativeGPlayLogoutSuccess();
                    }
                });
            }
        } else if (resultCode == RESULT_RECONNECT_REQUIRED) {
            mGoogleApiClient.connect();
        } else if (resultCode == 0) {
            nativeGPlayLoginSuccess(Boolean.valueOf(false));
        } else {
            nativeGPlayLoginSuccess(Boolean.valueOf(false));
        }
    }

    public static void logout() {
        Log.d(TAG, "super1 logout");
        AppActivity.getInstance().runOnGLThread(new Runnable() {
            public void run() {
                if (GameServiceInterface.mGoogleApiClient.isConnected()) {
                    GameServiceInterface.mGoogleApiClient.disconnect();
                    GameServiceInterface.getInstance().nativeGPlayLoginSuccess(Boolean.valueOf(false));
                }
            }
        });
    }

    public static boolean isLoggedIn() {
        Log.d(TAG, "super1 check is LoggedIn");
        if (mGoogleApiClient != null) {
            Log.d(TAG, "super1 mGoogleApiClient is not null");
            if (mGoogleApiClient.isConnected()) {
                Log.d(TAG, "super1 mGoogleApiClient is connected true");
            }
        }
        return mGoogleApiClient != null && mGoogleApiClient.isConnected();
    }

    public static void login(boolean autoOpenAchievements) {
        AppActivity app = AppActivity.getInstance();
        if (app != null) {
            app.runOnGLThread(new Runnable() {
                public void run() {
                    if (GameServiceInterface.isLoggedIn()) {
                        GameServiceInterface.getInstance().nativeGPlayLoginSuccess(Boolean.valueOf(true));
                    } else {
                        GameServiceInterface.mGoogleApiClient.connect();
                    }
                }
            });
        }
    }

    public static void unlockAchievement(String name) {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Games.Achievements.unlock(mGoogleApiClient, name);
        }
    }

    public static void incrementAchievement(String name) {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Games.Achievements.increment(mGoogleApiClient, name, 1);
        }
    }

    public static void submitLeaderboardScore(String leaderboardId, int point) {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            FabricAnswersInterface.logEvent("Submit Score", leaderboardId, point);
            Games.Leaderboards.submitScore(mGoogleApiClient, leaderboardId, (long) point);
        }
    }

    public void onConnected(@Nullable Bundle connectionHint) {
        if (isLoggedIn()) {
            Log.d(TAG, "super1 onConnected called.");
            try {
                this.mResolvingError = false;
                Games.Requests.registerRequestListener(mGoogleApiClient, this.mRequestListener);
                if (connectionHint != null) {
                    ArrayList<GameRequest> requests = Games.Requests.getGameRequestsFromBundle(connectionHint);
                    if (!requests.isEmpty()) {
                        Log.d(TAG, "onConnected: connection hint has " + requests.size() + " request(s)");
                    }
                    Log.d(TAG, "===========\nRequests count " + requests.size());
                    handleRequests(requests);
                }
                AppActivity.getInstance().runOnGLThread(new Runnable() {
                    public void run() {
                        Log.d(GameServiceInterface.TAG, "super1 onConnected inner");
                        GameServiceInterface.this.nativeGPlayLoginSuccess(Boolean.valueOf(true));
                    }
                });
            } catch (Exception e) {
            }
            updateRequestCounts();
        }
    }

    public void onConnectionSuspended(int i) {
        Log.d(TAG, "super1 onConnectionSuspended");
        mGoogleApiClient.connect();
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "super1 gplay ERROR CODE " + connectionResult.getErrorCode() + " Resolving " + this.mResolvingError);
        nativeGPlayLogoutSuccess();
        if (this.eatAutoLogin && (connectionResult.getErrorCode() == 4 || connectionResult.getErrorCode() == 6)) {
            this.eatAutoLogin = false;
            return;
        }
        if (this.mResolvingError) {
            this.mResolvingError = false;
        }
        if (connectionResult.hasResolution()) {
            try {
                this.mResolvingError = true;
                connectionResult.startResolutionForResult(AppActivity.getInstance(), REQUEST_RESOLVE_ERROR);
                return;
            } catch (SendIntentException e) {
                mGoogleApiClient.connect();
                return;
            }
        }
        Cocos2dxActivity activity = AppActivity.getInstance();
        if (activity != null) {
            activity.runOnGLThread(new Runnable() {
                public void run() {
                    GameServiceInterface.this.nativeGPlayCallback(NativeProtocol.BRIDGE_ARG_ERROR_BUNDLE);
                }
            });
        }
        this.mResolvingError = false;
    }

    public void onStop() {
        Log.i(TAG, "super1 onStop");
        if (mGoogleApiClient.isConnected()) {
            Log.i(TAG, "super1 mGoogleApiClient disconnecting...");
            mGoogleApiClient.disconnect();
        }
    }

    public void onResume() {
        Log.i(TAG, "super1 onResume");
        onStartCheckLogin();
    }

    public static int getGiftInboxCount() {
        return mGiftCount;
    }

    public static void showLeaderboard() {
        Log.i(TAG, "super1 showLeaderboard");
        AppActivity app = AppActivity.getInstance();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && app != null) {
            app.startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(mGoogleApiClient), REQUEST_LEADERBOARD);
        }
    }

    public static void showGPlayAchievements() {
        Log.i(TAG, "super1 showGPlayAchievement");
        AppActivity app = AppActivity.getInstance();
        if (app != null) {
            app.startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), REQUEST_ACHIEVEMENTS);
        }
    }

    public static void showGiftSender(String senderName, String senderPid) throws JSONException, UnsupportedEncodingException {
        Log.i(TAG, "super1 showGiftSender");
        Bitmap icon = BitmapFactory.decodeResource(AppActivity.getInstance().getResources(), com.superking.ludo.star.R.drawable.gift);
        JSONObject data = new JSONObject();
        data.put(MediationMetaData.KEY_NAME, senderName);
        data.put("pid", senderPid);
        String str = data.toString();
        Log.i(TAG, "super1 will set data >>>> " + str);
        AppActivity.getInstance().startActivityForResult(Games.Requests.getSendIntent(mGoogleApiClient, 1, str.getBytes(HTTP.UTF_8), 1, icon, AppActivity.getInstance().getString(com.superking.ludo.star.R.string.send_free_coins)), SEND_GIFT_CODE);
        FabricAnswersInterface.logEvent("Open Gift Sender", senderPid + "_" + senderName, 0);
    }

    public static void showGiftInbox() {
        Log.i(TAG, "super1 showGiftInbox");
        AppActivity.getInstance().startActivityForResult(Games.Requests.getInboxIntent(mGoogleApiClient), SHOW_INBOX);
        FabricAnswersInterface.logEvent("Open Gift Inbox", BuildConfig.FLAVOR, mGiftCount);
    }

    private int countNotExpired(GameRequestBuffer buf) {
        if (buf == null) {
            return 0;
        }
        int giftCount = 0;
        Iterator it = buf.iterator();
        while (it.hasNext()) {
            if (((GameRequest) it.next()).getExpirationTimestamp() > System.currentTimeMillis()) {
                giftCount++;
            }
        }
        return giftCount;
    }

    private void updateRequestCounts() {
        Log.i(TAG, "updateRequestCounts");
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            try {
                Games.Requests.loadRequests(mGoogleApiClient, 0, 65535, 0).setResultCallback(this.mLoadRequestsCallback);
            } catch (SecurityException e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }

    private void handleRequests(ArrayList<GameRequest> requests) {
        String message;
        String positiveBtn;
        String negavtiveBtn;
        boolean redeemGift;
        final String _name;
        final String _pid;
        AlertDialog.Builder builder;
        final boolean canRedeem;
        final ArrayList<GameRequest> arrayList;
        if (requests != null) {
            byte[] bytes = ((GameRequest) requests.get(0)).getData();
            String dataString = BuildConfig.FLAVOR;
            String name = BuildConfig.FLAVOR;
            String pid = BuildConfig.FLAVOR;
            try {
                String dataString2 = new String(bytes, HTTP.UTF_8);
                try {
                    JSONObject jSONObject = new JSONObject(dataString2);
                    name = jSONObject.optString(MediationMetaData.KEY_NAME, BuildConfig.FLAVOR);
                    pid = jSONObject.optString("pid", BuildConfig.FLAVOR);
                } catch (JSONException e) {
                    try {
                        Log.e(TAG, "Problem parsing data received");
                    } catch (UnsupportedEncodingException e2) {
                        dataString = dataString2;
                        Log.e(TAG, "Exception");
                        message = BuildConfig.FLAVOR;
                        positiveBtn = BuildConfig.FLAVOR;
                        negavtiveBtn = BuildConfig.FLAVOR;
                        redeemGift = true;
                        if (!pid.equals(BuildConfig.FLAVOR)) {
                            if (UserLocalStorage.canReceieveGiftFrom(pid)) {
                                message = AppActivity.getInstance().getString(com.superking.ludo.star.R.string.cannot_accept);
                                positiveBtn = AppActivity.getInstance().getString(com.superking.ludo.star.R.string.yes);
                                negavtiveBtn = AppActivity.getInstance().getString(com.superking.ludo.star.R.string.later);
                                redeemGift = false;
                            } else {
                                message = AppActivity.getInstance().getString(com.superking.ludo.star.R.string.want_to_accept_gift) + " " + name + " ?";
                                positiveBtn = AppActivity.getInstance().getString(com.superking.ludo.star.R.string.yes);
                                negavtiveBtn = AppActivity.getInstance().getString(com.superking.ludo.star.R.string.no);
                            }
                        }
                        _name = name;
                        _pid = pid;
                        builder = new AlertDialog.Builder(AppActivity.getInstance(), 5);
                        builder.setTitle(com.superking.ludo.star.R.string.free_coins);
                        builder.setIcon(com.superking.ludo.star.R.drawable.gift);
                        canRedeem = redeemGift;
                        arrayList = requests;
                        builder.setMessage(message).setPositiveButton(positiveBtn, new OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (canRedeem) {
                                    AppActivity.getInstance().runOnGLThread(new Runnable() {
                                        public void run() {
                                            GameServiceInterface.getInstance().nativeGplayRedeemGift(_name, _pid);
                                        }
                                    });
                                    FabricAnswersInterface.logEvent("Gift Accepted", _pid + _name, 0);
                                } else {
                                    FabricAnswersInterface.logEvent("Eating Gift", _pid + _name, 0);
                                }
                                ArrayList<GameRequest> temp = new ArrayList();
                                temp.add(arrayList.get(0));
                                GameServiceInterface.this.acceptRequests(temp);
                            }
                        }).setNegativeButton(negavtiveBtn, new OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        builder.create().show();
                    }
                }
                dataString = dataString2;
            } catch (UnsupportedEncodingException e3) {
                Log.e(TAG, "Exception");
                message = BuildConfig.FLAVOR;
                positiveBtn = BuildConfig.FLAVOR;
                negavtiveBtn = BuildConfig.FLAVOR;
                redeemGift = true;
                if (pid.equals(BuildConfig.FLAVOR)) {
                    if (UserLocalStorage.canReceieveGiftFrom(pid)) {
                        message = AppActivity.getInstance().getString(com.superking.ludo.star.R.string.want_to_accept_gift) + " " + name + " ?";
                        positiveBtn = AppActivity.getInstance().getString(com.superking.ludo.star.R.string.yes);
                        negavtiveBtn = AppActivity.getInstance().getString(com.superking.ludo.star.R.string.no);
                    } else {
                        message = AppActivity.getInstance().getString(com.superking.ludo.star.R.string.cannot_accept);
                        positiveBtn = AppActivity.getInstance().getString(com.superking.ludo.star.R.string.yes);
                        negavtiveBtn = AppActivity.getInstance().getString(com.superking.ludo.star.R.string.later);
                        redeemGift = false;
                    }
                }
                _name = name;
                _pid = pid;
                builder = new AlertDialog.Builder(AppActivity.getInstance(), 5);
                builder.setTitle(com.superking.ludo.star.R.string.free_coins);
                builder.setIcon(com.superking.ludo.star.R.drawable.gift);
                canRedeem = redeemGift;
                arrayList = requests;
                builder.setMessage(message).setPositiveButton(positiveBtn, /* anonymous class already generated */).setNegativeButton(negavtiveBtn, /* anonymous class already generated */);
                builder.create().show();
            }
            message = BuildConfig.FLAVOR;
            positiveBtn = BuildConfig.FLAVOR;
            negavtiveBtn = BuildConfig.FLAVOR;
            redeemGift = true;
            if (pid.equals(BuildConfig.FLAVOR)) {
                if (UserLocalStorage.canReceieveGiftFrom(pid)) {
                    message = AppActivity.getInstance().getString(com.superking.ludo.star.R.string.want_to_accept_gift) + " " + name + " ?";
                    positiveBtn = AppActivity.getInstance().getString(com.superking.ludo.star.R.string.yes);
                    negavtiveBtn = AppActivity.getInstance().getString(com.superking.ludo.star.R.string.no);
                } else {
                    message = AppActivity.getInstance().getString(com.superking.ludo.star.R.string.cannot_accept);
                    positiveBtn = AppActivity.getInstance().getString(com.superking.ludo.star.R.string.yes);
                    negavtiveBtn = AppActivity.getInstance().getString(com.superking.ludo.star.R.string.later);
                    redeemGift = false;
                }
            }
            _name = name;
            _pid = pid;
            try {
                builder = new AlertDialog.Builder(AppActivity.getInstance(), 5);
            } catch (NoSuchMethodError e4) {
                builder = new AlertDialog.Builder(AppActivity.getInstance());
            }
            builder.setTitle(com.superking.ludo.star.R.string.free_coins);
            builder.setIcon(com.superking.ludo.star.R.drawable.gift);
            canRedeem = redeemGift;
            arrayList = requests;
            builder.setMessage(message).setPositiveButton(positiveBtn, /* anonymous class already generated */).setNegativeButton(negavtiveBtn, /* anonymous class already generated */);
            builder.create().show();
        }
    }

    private void acceptRequests(ArrayList<GameRequest> requests) {
        ArrayList<String> requestIds = new ArrayList();
        final HashMap<String, GameRequest> gameRequestMap = new HashMap();
        Iterator it = requests.iterator();
        while (it.hasNext()) {
            GameRequest request = (GameRequest) it.next();
            String requestId = request.getRequestId();
            requestIds.add(requestId);
            gameRequestMap.put(requestId, request);
            Log.d(TAG, "Processing request " + requestId);
        }
        Games.Requests.acceptRequests(mGoogleApiClient, requestIds).setResultCallback(new ResultCallback<UpdateRequestsResult>() {
            public void onResult(UpdateRequestsResult result) {
                int numGifts = 0;
                int numRequests = 0;
                for (String requestId : result.getRequestIds()) {
                    if (gameRequestMap.containsKey(requestId) && result.getRequestOutcome(requestId) == 0) {
                        switch (((GameRequest) gameRequestMap.get(requestId)).getType()) {
                            case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                                numGifts++;
                                break;
                            case R.styleable.View_paddingStart /*2*/:
                                numRequests++;
                                break;
                            default:
                                break;
                        }
                    }
                }
                if (numGifts != 0 || numRequests != 0) {
                    GameServiceInterface.this.updateRequestCounts();
                }
            }
        });
    }
}
