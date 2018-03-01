package org.cocos2dx.cpp;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.CallbackManager.Factory;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookRequestError;
import com.facebook.FacebookSdkNotInitializedException;
import com.facebook.GraphRequest;
import com.facebook.GraphRequest.Callback;
import com.facebook.GraphRequest.GraphJSONArrayCallback;
import com.facebook.GraphRequest.GraphJSONObjectCallback;
import com.facebook.GraphRequestBatch;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.internal.ServerProtocol;
import com.facebook.internal.WebDialog;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer.Result;
import com.facebook.share.internal.ShareConstants;
import com.facebook.share.model.AppInviteContent.Builder;
import com.facebook.share.model.GameRequestContent;
import com.facebook.share.model.GameRequestContent.Filters;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.AppInviteDialog;
import com.facebook.share.widget.GameRequestDialog;
import com.ironsource.sdk.utils.Constants.ControllerParameters;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import com.superking.iap.FabricAnswersInterface;
import com.unity3d.ads.metadata.MediationMetaData;
import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch.BranchLinkCreateListener;
import io.branch.referral.BranchError;
import io.branch.referral.util.LinkProperties;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.cocos2dx.lib.BuildConfig;
import org.cocos2dx.lib.Cocos2dxHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FacebookInterface {
    private static final String TAG = "FacebookInterface";
    private static AppEventsLogger appEventLogger;
    private static AppInviteDialog appInviteDialog;
    public static CallbackManager callbackManager;
    private static GameRequestDialog gameRequestDialog;
    private static AccessTokenTracker mAccessTokenTracker;
    public static WebDialog mDialog;
    private static int mInviteType = 0;
    public static Bundle mParams;
    private static ProfileTracker mProfileTracker;
    private static int mfriendType = 0;
    private static FacebookCallback<Result> shareCallback = new FacebookCallback<Result>() {
        public void onCancel() {
            Log.d(FacebookInterface.TAG, "Facebook sharing cancel");
            AppActivity.publishEvent("eventFBSharingFailed", BuildConfig.FLAVOR);
        }

        public void onError(FacebookException error) {
            Log.d(FacebookInterface.TAG, "Facebook sharing Error: " + error.toString());
            AppActivity.publishEvent("eventFBSharingFailed", BuildConfig.FLAVOR);
        }

        public void onSuccess(Result result) {
            Log.d(FacebookInterface.TAG, "Facebook sharing Success: " + result.getPostId());
            Log.d(FacebookInterface.TAG, "Ac: " + AccessToken.getCurrentAccessToken());
            AppActivity.publishEvent("eventFBSharingDone", BuildConfig.FLAVOR);
        }
    };

    private static native void nativeGameRequestSuccess(int i, int i2, String str);

    public static void initFacebook(AppActivity activity) {
        callbackManager = Factory.create();
        initRequestFlow(activity, callbackManager);
        registerLoginCallback();
        mAccessTokenTracker = new AccessTokenTracker() {
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
            }
        };
        mProfileTracker = new ProfileTracker() {
            protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
            }
        };
        appEventLogger = AppEventsLogger.newLogger(activity);
    }

    public static void initRequestFlow(Activity activity, CallbackManager callbackManager) {
        appInviteDialog = new AppInviteDialog(activity);
        appInviteDialog.registerCallback(callbackManager, new FacebookCallback<AppInviteDialog.Result>() {
            public void onSuccess(AppInviteDialog.Result result) {
                Log.d(FacebookInterface.TAG, "Got callback from request " + result.toString());
            }

            public void onCancel() {
                Log.d(FacebookInterface.TAG, "Request cancelled");
            }

            public void onError(FacebookException error) {
                Log.d(FacebookInterface.TAG, "Got error " + error.toString());
            }
        });
        gameRequestDialog = new GameRequestDialog(activity);
        gameRequestDialog.registerCallback(callbackManager, new FacebookCallback<GameRequestDialog.Result>() {
            public void onSuccess(GameRequestDialog.Result result) {
                List<String> receipients = result.getRequestRecipients();
                if (receipients == null || receipients.size() <= 0) {
                    FacebookInterface.mfriendType = 0;
                    FacebookInterface.mInviteType = 0;
                    return;
                }
                GraphRequestBatch graphRequests = new GraphRequestBatch();
                String list = BuildConfig.FLAVOR;
                Bundle parameters = new Bundle();
                parameters.putString(GraphRequest.FIELDS_PARAM, "id,name,first_name,last_name");
                final JSONArray jsonArray = new JSONArray();
                for (String id : result.getRequestRecipients()) {
                    if (FacebookInterface.mfriendType == 3) {
                        GraphRequest request = GraphRequest.newGraphPathRequest(AccessToken.getCurrentAccessToken(), "/" + id, new Callback() {
                            public void onCompleted(GraphResponse response) {
                                if (response.getError() == null) {
                                    JSONObject info = response.getJSONObject();
                                    if (info != null) {
                                        jsonArray.put(info);
                                    }
                                }
                            }
                        });
                        request.setParameters(parameters);
                        graphRequests.add(request);
                    }
                    list = list + id + ",";
                }
                final String toList = list.substring(0, list.length() - 1);
                AppActivity app = AppActivity.getInstance();
                if (app != null) {
                    app.runOnGLThread(new Runnable() {
                        public void run() {
                            FacebookInterface.nativeGameRequestSuccess(FacebookInterface.mfriendType, FacebookInterface.mInviteType, toList);
                            FacebookInterface.mfriendType = 0;
                            FacebookInterface.mInviteType = 0;
                        }
                    });
                }
                if (FacebookInterface.mfriendType == 3) {
                    graphRequests.addCallback(new GraphRequestBatch.Callback() {
                        public void onBatchCompleted(GraphRequestBatch batch) {
                            FacebookInterface.saveInvitableFriendsToFile(jsonArray);
                        }
                    });
                    graphRequests.executeAsync();
                }
            }

            public void onCancel() {
                AppActivity.publishEvent("eventGameRequestFail", BuildConfig.FLAVOR);
                FacebookInterface.mfriendType = 0;
                FacebookInterface.mInviteType = 0;
            }

            public void onError(FacebookException error) {
                Log.d(FacebookInterface.TAG, error.getMessage());
                AppActivity.publishEvent("eventGameRequestFail", BuildConfig.FLAVOR);
                FacebookInterface.mfriendType = 0;
                FacebookInterface.mInviteType = 0;
            }
        });
    }

    public static void registerLoginCallback() {
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            public void onSuccess(LoginResult loginResult) {
                Log.d(FacebookInterface.TAG, "Login Callback " + loginResult.toString());
                GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphJSONObjectCallback() {
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        FacebookInterface.onGraphResponse(object, response);
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString(GraphRequest.FIELDS_PARAM, "id,first_name,last_name,email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            public void onCancel() {
                Log.d(FacebookInterface.TAG, "Facebook Canceled");
                AppActivity.publishEvent("eventFBLoginFailed", BuildConfig.FLAVOR);
            }

            public void onError(FacebookException exception) {
                Log.d(FacebookInterface.TAG, "Facebook Login Failed" + exception.toString());
                AppActivity.publishEvent("eventFBLoginFailed", BuildConfig.FLAVOR);
            }
        });
    }

    public static void login(boolean minimal) {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            String token = accessToken.getToken();
            if (!(token == null || token.isEmpty() || !minimal)) {
                Profile profile = Profile.getCurrentProfile();
                if (profile != null) {
                    JSONObject data = new JSONObject();
                    try {
                        data.put(ShareConstants.WEB_DIALOG_PARAM_ID, profile.getId());
                        data.put("first_name", profile.getFirstName());
                        data.put("last_name", profile.getLastName());
                        data.put(ServerProtocol.DIALOG_PARAM_ACCESS_TOKEN, token);
                        AppActivity.publishEvent("eventFBLoginSuccess", data.toString());
                        getFriends();
                        return;
                    } catch (JSONException e) {
                        Log.d(TAG, e.getMessage());
                    }
                }
                AppActivity.publishEvent("eventFBLoginSuccess", BuildConfig.FLAVOR);
                getFriends();
                return;
            }
        }
        triggerLogin();
    }

    public static void triggerLogin() {
        AppActivity app = AppActivity.getInstance();
        if (app != null) {
            app.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        LoginManager.getInstance().logInWithReadPermissions(AppActivity.getInstance(), Arrays.asList(new String[]{"public_profile", "email", "user_friends"}));
                    } catch (FacebookSdkNotInitializedException ex) {
                        Log.d(FacebookInterface.TAG, ex.getMessage());
                        AppActivity.publishEvent("eventFBLoginFailed", BuildConfig.FLAVOR);
                    }
                }
            });
        }
    }

    public static void onGraphResponse(JSONObject object, GraphResponse response) {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (object == null || accessToken == null) {
            AppActivity.publishEvent("eventFBLoginFailed", BuildConfig.FLAVOR);
            return;
        }
        try {
            object.put(ServerProtocol.DIALOG_PARAM_ACCESS_TOKEN, accessToken.getToken());
            AppActivity.publishEvent("eventFBLoginSuccess", object.toString());
            getFriends();
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
            AppActivity.publishEvent("eventFBLoginFailed", BuildConfig.FLAVOR);
        }
    }

    public static void logOut() {
        AppActivity.getInstance().runOnUiThread(new Runnable() {
            public void run() {
                LoginManager.getInstance().logOut();
            }
        });
    }

    public static boolean hasGivenFriendsPermission() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            return accessToken.getPermissions().contains("user_friends");
        }
        return false;
    }

    public static void getFriends() {
        if (hasGivenFriendsPermission()) {
            GraphRequest friendsRequest = GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken(), new GraphJSONArrayCallback() {
                public void onCompleted(JSONArray users, GraphResponse response) {
                    FacebookRequestError error = response.getError();
                    if (error != null) {
                        if (error.getErrorMessage() != null) {
                            Log.d(FacebookInterface.TAG, error.getErrorMessage());
                        }
                        AppActivity.publishEvent("eventFBFriendUpdated", BuildConfig.FLAVOR);
                        return;
                    }
                    FacebookInterface.saveAppFriendsToFile(users);
                }
            });
            Bundle params = new Bundle();
            params.putString(GraphRequest.FIELDS_PARAM, "id, name, first_name, last_name");
            params.putString("limit", "3000");
            friendsRequest.setParameters(params);
            GraphRequestBatch batch = new GraphRequestBatch(friendsRequest);
            String path = Cocos2dxHelper.getCocos2dxWritablePath();
            if (path.length() > 0) {
                path = path + "/fb_friends.json";
            } else {
                path = "fb_friends.json";
            }
            if (new File(path).exists()) {
                batch.setTimeout(ControllerParameters.LOAD_RUNTIME);
            } else {
                batch.setTimeout(30000);
            }
            batch.executeAsync();
        }
    }

    public static void sendAppRequest(String title, String message, String userId, String dataJson) {
        try {
            new BranchUniversalObject().setCanonicalIdentifier("user/" + userId).setTitle(title).setContentDescription(message).addContentMetadata("dataJson", new JSONObject(dataJson).toString()).setContentExpiration(new Date(System.currentTimeMillis() + 1800000)).generateShortUrl(AppActivity.getInstance(), new LinkProperties().setChannel("Facebook").setFeature("AppInvite"), new BranchLinkCreateListener() {
                public void onLinkCreate(String url, BranchError error) {
                    if (error == null && AppInviteDialog.canShow()) {
                        FacebookInterface.appInviteDialog.show(new Builder().setApplinkUrl(url).setPreviewImageUrl("http://ludo.superkinglabs.com/cover.png").build());
                    }
                }
            });
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public static void logFbEvent(String eventName, String jsonString) {
        Log.d(TAG, "super1 Loggin event {" + eventName + "} with data => " + jsonString);
        appEventLogger.logEvent(eventName, EMHelpers.jsonStringToBundle(jsonString));
    }

    public static void shareFacebookPost(String path, String message, boolean willReward) {
        Log.d(TAG, "About to share Post on Facebook " + path + " m: " + message + " Will Reward ? " + willReward);
        File file = new File(path);
        if (file.exists()) {
            SharePhotoContent content = new SharePhotoContent.Builder().addPhoto(new SharePhoto.Builder().setBitmap(BitmapFactory.decodeFile(file.getAbsolutePath())).build()).build();
            if (hasPublishPermission()) {
                ShareApi.share(content, shareCallback);
                return;
            } else {
                requestPublishAction();
                return;
            }
        }
        Toast.makeText(AppActivity.getInstance(), "Not Enough memory! Try Later", 1).show();
    }

    public static boolean hasPublishPermission() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null && accessToken.getPermissions().contains("publish_actions");
    }

    public static void requestPublishAction() {
        AppActivity.getInstance().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    LoginManager.getInstance().logInWithPublishPermissions(AppActivity.getInstance(), Arrays.asList(new String[]{"publish_actions"}));
                } catch (FacebookSdkNotInitializedException ex) {
                    Log.d(FacebookInterface.TAG, "Request Publish action: " + ex.getMessage());
                }
            }
        });
    }

    public static void sendGameRequest(String title, String message, String pids, String dataJson, int friendType, int inviteType) {
        if (mInviteType != 0) {
            Log.d(TAG, "Request already in progress");
            return;
        }
        mfriendType = friendType;
        mInviteType = inviteType;
        GameRequestContent.Builder builder = new GameRequestContent.Builder();
        builder.setData(dataJson);
        builder.setMessage(message);
        builder.setTitle(title);
        int count = 0;
        if (pids.isEmpty()) {
            builder.setFilters(Filters.APP_NON_USERS);
        } else {
            ArrayList<String> receipients = new ArrayList(Arrays.asList(pids.split(",")));
            count = receipients.size();
            builder.setRecipients(receipients);
        }
        FabricAnswersInterface.logEvent(title, String.valueOf(mfriendType), count);
        GameRequestContent gameRequestContent = builder.build();
        if (gameRequestDialog.canShow(gameRequestContent)) {
            gameRequestDialog.show(gameRequestContent);
        }
    }

    private static void saveAppFriendsToFile(JSONArray friends) {
        String path = Cocos2dxHelper.getCocos2dxWritablePath();
        if (path.length() > 0) {
            path = path + "/fb_friends.json";
        } else {
            path = "fb_friends.json";
        }
        JSONObject friendsObject = new JSONObject();
        for (int i = 0; i < friends.length(); i++) {
            JSONObject user = friends.optJSONObject(i);
            if (user != null) {
                try {
                    JSONObject j = new JSONObject();
                    j.put(MediationMetaData.KEY_NAME, user.optString(MediationMetaData.KEY_NAME, BuildConfig.FLAVOR));
                    j.put("username", user.optString("username", BuildConfig.FLAVOR));
                    j.put("firstname", user.optString("first_name", BuildConfig.FLAVOR));
                    j.put("lastname", user.optString("last_name", BuildConfig.FLAVOR));
                    j.put(ParametersKeys.URL, BuildConfig.FLAVOR);
                    friendsObject.put(user.optString(ShareConstants.WEB_DIALOG_PARAM_ID), j);
                } catch (JSONException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        }
        try {
            FileWriter file = new FileWriter(path, false);
            file.write(friendsObject.toString());
            file.flush();
            file.close();
        } catch (IOException e2) {
            Log.d(TAG, e2.getMessage());
        }
        AppActivity.publishEvent("eventFBFriendUpdated", BuildConfig.FLAVOR);
    }

    private static void saveInvitableFriendsToFile(JSONArray friends) {
        JSONObject friendsObject;
        String path = Cocos2dxHelper.getCocos2dxWritablePath();
        if (path.length() > 0) {
            path = path + "/invitable_friends.json";
        } else {
            path = "invitable_friends.json";
        }
        try {
            FileReader fileReader = new FileReader(path);
            StringBuffer content = new StringBuffer();
            char[] buffer = new char[1024];
            while (true) {
                int num = fileReader.read(buffer);
                if (num <= 0) {
                    break;
                }
                content.append(buffer, 0, num);
            }
            fileReader.close();
            friendsObject = new JSONObject(content.toString());
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            friendsObject = new JSONObject();
        }
        boolean dirty = false;
        for (int i = 0; i < friends.length(); i++) {
            JSONObject user = friends.optJSONObject(i);
            if (user != null) {
                try {
                    if (!friendsObject.has(user.getString(ShareConstants.WEB_DIALOG_PARAM_ID))) {
                        dirty = true;
                        JSONObject j = new JSONObject();
                        j.put(MediationMetaData.KEY_NAME, user.optString(MediationMetaData.KEY_NAME, BuildConfig.FLAVOR));
                        j.put("username", user.optString("username", BuildConfig.FLAVOR));
                        j.put("firstname", user.optString("first_name", BuildConfig.FLAVOR));
                        j.put("lastname", user.optString("last_name", BuildConfig.FLAVOR));
                        j.put(ParametersKeys.URL, BuildConfig.FLAVOR);
                        friendsObject.put(user.optString(ShareConstants.WEB_DIALOG_PARAM_ID), j);
                    }
                } catch (JSONException e2) {
                    Log.d(TAG, e2.getMessage());
                }
            }
        }
        if (dirty) {
            try {
                FileWriter file = new FileWriter(path, false);
                file.write(friendsObject.toString());
                file.flush();
                file.close();
                AppActivity.publishEvent("eventFBInvitableFriendUpdated", BuildConfig.FLAVOR);
            } catch (IOException e3) {
                Log.d(TAG, e3.getMessage());
            }
        }
    }
}
