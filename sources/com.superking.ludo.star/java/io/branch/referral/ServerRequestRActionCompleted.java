package io.branch.referral;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import io.branch.referral.BranchViewHandler.IBranchViewEvents;
import io.branch.referral.Defines.Jsonkey;
import io.branch.referral.Defines.RequestPath;
import io.branch.referral.util.CommerceEvent;
import org.cocos2dx.lib.BuildConfig;
import org.json.JSONException;
import org.json.JSONObject;

class ServerRequestRActionCompleted extends ServerRequest {
    private final IBranchViewEvents callback_;

    public ServerRequestRActionCompleted(Context context, CommerceEvent commerceEvent, JSONObject metadata, IBranchViewEvents callback) {
        super(context, RequestPath.CompletedAction.getPath());
        this.callback_ = callback;
        JSONObject post = new JSONObject();
        try {
            post.put(Jsonkey.IdentityID.getKey(), this.prefHelper_.getIdentityID());
            post.put(Jsonkey.DeviceFingerprintID.getKey(), this.prefHelper_.getDeviceFingerPrintID());
            post.put(Jsonkey.SessionID.getKey(), this.prefHelper_.getSessionID());
            if (!this.prefHelper_.getLinkClickID().equals(SystemObserver.BLANK)) {
                post.put(Jsonkey.LinkClickID.getKey(), this.prefHelper_.getLinkClickID());
            }
            post.put(Jsonkey.Event.getKey(), "purchase");
            if (metadata != null) {
                post.put(Jsonkey.Metadata.getKey(), metadata);
            }
            if (commerceEvent != null) {
                post.put(Jsonkey.CommerceData.getKey(), commerceEvent.getCommerceJSONObject());
            }
            setPost(post);
        } catch (JSONException ex) {
            ex.printStackTrace();
            this.constructError_ = true;
        }
    }

    public ServerRequestRActionCompleted(String requestPath, JSONObject post, Context context) {
        super(requestPath, post, context);
        this.callback_ = null;
    }

    public void onRequestSucceeded(ServerResponse resp, Branch branch) {
        if (resp.getObject() != null && resp.getObject().has(Jsonkey.BranchViewData.getKey()) && Branch.getInstance().currentActivityReference_ != null && Branch.getInstance().currentActivityReference_.get() != null) {
            String actionName = BuildConfig.FLAVOR;
            try {
                JSONObject post = getPost();
                if (post != null && post.has(Jsonkey.Event.getKey())) {
                    actionName = post.getString(Jsonkey.Event.getKey());
                }
                if (Branch.getInstance().currentActivityReference_ != null) {
                    Activity currentActivity = (Activity) Branch.getInstance().currentActivityReference_.get();
                    BranchViewHandler.getInstance().showBranchView(resp.getObject().getJSONObject(Jsonkey.BranchViewData.getKey()), actionName, currentActivity, this.callback_);
                }
            } catch (JSONException e) {
                if (this.callback_ != null) {
                    this.callback_.onBranchViewError(BranchViewHandler.BRANCH_VIEW_ERR_INVALID_VIEW, "Unable to show branch view. Branch view received is invalid ", actionName);
                }
            }
        }
    }

    public void handleFailure(int statusCode, String causeMsg) {
    }

    public boolean handleErrors(Context context) {
        if (super.doesAppHasInternetPermission(context)) {
            return false;
        }
        Log.i("BranchSDK", "Trouble executing your request. Please add 'android.permission.INTERNET' in your applications manifest file");
        return true;
    }

    public boolean isGetRequest() {
        return false;
    }

    public void clearCallbacks() {
    }

    public boolean shouldRetryOnFail() {
        return true;
    }
}
