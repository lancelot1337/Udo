package io.branch.referral;

import android.content.Context;
import com.facebook.internal.AnalyticsEvents;
import io.branch.referral.Branch.BranchReferralInitListener;
import io.branch.referral.Defines.Jsonkey;
import io.branch.referral.Defines.RequestPath;
import org.json.JSONException;
import org.json.JSONObject;

class ServerRequestRegisterOpen extends ServerRequestInitSession {
    BranchReferralInitListener callback_;
    final SystemObserver systemObserver_;

    public ServerRequestRegisterOpen(Context context, BranchReferralInitListener callback, SystemObserver sysObserver) {
        super(context, RequestPath.RegisterOpen.getPath());
        this.systemObserver_ = sysObserver;
        this.callback_ = callback;
        JSONObject openPost = new JSONObject();
        try {
            openPost.put(Jsonkey.DeviceFingerprintID.getKey(), this.prefHelper_.getDeviceFingerPrintID());
            openPost.put(Jsonkey.IdentityID.getKey(), this.prefHelper_.getIdentityID());
            openPost.put(Jsonkey.IsReferrable.getKey(), this.prefHelper_.getIsReferrable());
            if (!sysObserver.getAppVersion().equals(SystemObserver.BLANK)) {
                openPost.put(Jsonkey.AppVersion.getKey(), sysObserver.getAppVersion());
            }
            openPost.put(Jsonkey.FaceBookAppLinkChecked.getKey(), this.prefHelper_.getIsAppLinkTriggeredInit());
            openPost.put(Jsonkey.Update.getKey(), sysObserver.getUpdateState());
            openPost.put(Jsonkey.Debug.getKey(), this.prefHelper_.getExternDebug());
            setPost(openPost);
        } catch (JSONException ex) {
            ex.printStackTrace();
            this.constructError_ = true;
        }
    }

    public ServerRequestRegisterOpen(String requestPath, JSONObject post, Context context) {
        super(requestPath, post, context);
        this.systemObserver_ = new SystemObserver(context);
    }

    public void onRequestSucceeded(ServerResponse resp, Branch branch) {
        super.onRequestSucceeded(resp, branch);
        try {
            if (resp.getObject().has(Jsonkey.LinkClickID.getKey())) {
                this.prefHelper_.setLinkClickID(resp.getObject().getString(Jsonkey.LinkClickID.getKey()));
            } else {
                this.prefHelper_.setLinkClickID(SystemObserver.BLANK);
            }
            if (resp.getObject().has(Jsonkey.Data.getKey())) {
                JSONObject dataObj = new JSONObject(resp.getObject().getString(Jsonkey.Data.getKey()));
                if (dataObj.has(Jsonkey.Clicked_Branch_Link.getKey()) && dataObj.getBoolean(Jsonkey.Clicked_Branch_Link.getKey()) && this.prefHelper_.getInstallParams().equals(SystemObserver.BLANK) && this.prefHelper_.getIsReferrable() == 1) {
                    this.prefHelper_.setInstallParams(resp.getObject().getString(Jsonkey.Data.getKey()));
                }
            }
            if (resp.getObject().has(Jsonkey.Data.getKey())) {
                this.prefHelper_.setSessionParams(resp.getObject().getString(Jsonkey.Data.getKey()));
            } else {
                this.prefHelper_.setSessionParams(SystemObserver.BLANK);
            }
            if (this.callback_ != null) {
                this.callback_.onInitFinished(branch.getLatestReferringParams(), null);
            }
            this.prefHelper_.setAppVersion(this.systemObserver_.getAppVersion());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        onInitSessionCompleted(resp, branch);
    }

    public void setInitFinishedCallback(BranchReferralInitListener callback) {
        if (callback != null) {
            this.callback_ = callback;
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
            this.callback_.onInitFinished(obj, new BranchError("Trouble initializing Branch. " + causeMsg, statusCode));
        }
    }

    public boolean handleErrors(Context context) {
        if (super.doesAppHasInternetPermission(context)) {
            return false;
        }
        if (this.callback_ != null) {
            this.callback_.onInitFinished(null, new BranchError("Trouble initializing Branch.", BranchError.ERR_NO_INTERNET_PERMISSION));
        }
        return true;
    }

    public boolean isGetRequest() {
        return false;
    }

    public void clearCallbacks() {
        this.callback_ = null;
    }

    public boolean hasCallBack() {
        return this.callback_ != null;
    }

    public String getRequestActionName() {
        return "open";
    }
}
