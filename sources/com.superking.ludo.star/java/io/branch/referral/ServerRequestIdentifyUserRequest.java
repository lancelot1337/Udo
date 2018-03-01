package io.branch.referral;

import android.content.Context;
import com.facebook.internal.AnalyticsEvents;
import io.branch.referral.Branch.BranchReferralInitListener;
import io.branch.referral.Defines.Jsonkey;
import io.branch.referral.Defines.RequestPath;
import org.json.JSONException;
import org.json.JSONObject;

class ServerRequestIdentifyUserRequest extends ServerRequest {
    BranchReferralInitListener callback_;
    String userId_ = null;

    public ServerRequestIdentifyUserRequest(Context context, BranchReferralInitListener callback, String userId) {
        super(context, RequestPath.IdentifyUser.getPath());
        this.callback_ = callback;
        this.userId_ = userId;
        JSONObject post = new JSONObject();
        try {
            post.put(Jsonkey.IdentityID.getKey(), this.prefHelper_.getIdentityID());
            post.put(Jsonkey.DeviceFingerprintID.getKey(), this.prefHelper_.getDeviceFingerPrintID());
            post.put(Jsonkey.SessionID.getKey(), this.prefHelper_.getSessionID());
            if (!this.prefHelper_.getLinkClickID().equals(SystemObserver.BLANK)) {
                post.put(Jsonkey.LinkClickID.getKey(), this.prefHelper_.getLinkClickID());
            }
            post.put(Jsonkey.Identity.getKey(), userId);
            setPost(post);
        } catch (JSONException ex) {
            ex.printStackTrace();
            this.constructError_ = true;
        }
    }

    public ServerRequestIdentifyUserRequest(String requestPath, JSONObject post, Context context) {
        super(requestPath, post, context);
    }

    public void onRequestSucceeded(ServerResponse resp, Branch branch) {
        try {
            if (getPost() != null && getPost().has(Jsonkey.Identity.getKey())) {
                this.prefHelper_.setIdentity(getPost().getString(Jsonkey.Identity.getKey()));
            }
            this.prefHelper_.setIdentityID(resp.getObject().getString(Jsonkey.IdentityID.getKey()));
            this.prefHelper_.setUserURL(resp.getObject().getString(Jsonkey.Link.getKey()));
            if (resp.getObject().has(Jsonkey.ReferringData.getKey())) {
                this.prefHelper_.setInstallParams(resp.getObject().getString(Jsonkey.ReferringData.getKey()));
            }
            if (this.callback_ != null) {
                this.callback_.onInitFinished(branch.getFirstReferringParams(), null);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void handleFailure(int statusCode, String causeMsg) {
        if (this.callback_ != null) {
            JSONObject obj = new JSONObject();
            try {
                obj.put(AnalyticsEvents.PARAMETER_SHARE_ERROR_MESSAGE, "Trouble reaching server. Please try again in a few minutes");
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            this.callback_.onInitFinished(obj, new BranchError("Trouble setting the user alias. " + causeMsg, statusCode));
        }
    }

    public boolean handleErrors(Context context) {
        if (super.doesAppHasInternetPermission(context)) {
            try {
                String userId = getPost().getString(Jsonkey.Identity.getKey());
                if (userId == null || userId.length() == 0 || userId.equals(this.prefHelper_.getIdentity())) {
                    return true;
                }
                return false;
            } catch (JSONException e) {
                return true;
            }
        } else if (this.callback_ == null) {
            return true;
        } else {
            this.callback_.onInitFinished(null, new BranchError("Trouble setting the user alias.", BranchError.ERR_NO_INTERNET_PERMISSION));
            return true;
        }
    }

    public boolean isGetRequest() {
        return false;
    }

    public boolean isExistingID() {
        try {
            String userId = getPost().getString(Jsonkey.Identity.getKey());
            if (userId == null || !userId.equals(this.prefHelper_.getIdentity())) {
                return false;
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void handleUserExist(Branch branch) {
        if (this.callback_ != null) {
            this.callback_.onInitFinished(branch.getFirstReferringParams(), null);
        }
    }

    public void clearCallbacks() {
        this.callback_ = null;
    }

    public boolean shouldRetryOnFail() {
        return true;
    }
}
