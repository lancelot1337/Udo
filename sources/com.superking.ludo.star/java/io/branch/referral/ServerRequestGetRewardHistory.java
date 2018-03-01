package io.branch.referral;

import android.content.Context;
import io.branch.referral.Branch.BranchListResponseListener;
import io.branch.referral.Branch.CreditHistoryOrder;
import io.branch.referral.Defines.Jsonkey;
import io.branch.referral.Defines.RequestPath;
import org.json.JSONException;
import org.json.JSONObject;

class ServerRequestGetRewardHistory extends ServerRequest {
    BranchListResponseListener callback_;

    public ServerRequestGetRewardHistory(Context context, String bucket, String afterId, int length, CreditHistoryOrder order, BranchListResponseListener callback) {
        super(context, RequestPath.GetCreditHistory.getPath());
        this.callback_ = callback;
        JSONObject getCreditHistoryPost = new JSONObject();
        try {
            getCreditHistoryPost.put(Jsonkey.IdentityID.getKey(), this.prefHelper_.getIdentityID());
            getCreditHistoryPost.put(Jsonkey.DeviceFingerprintID.getKey(), this.prefHelper_.getDeviceFingerPrintID());
            getCreditHistoryPost.put(Jsonkey.SessionID.getKey(), this.prefHelper_.getSessionID());
            if (!this.prefHelper_.getLinkClickID().equals(SystemObserver.BLANK)) {
                getCreditHistoryPost.put(Jsonkey.LinkClickID.getKey(), this.prefHelper_.getLinkClickID());
            }
            getCreditHistoryPost.put(Jsonkey.Length.getKey(), length);
            getCreditHistoryPost.put(Jsonkey.Direction.getKey(), order.ordinal());
            if (bucket != null) {
                getCreditHistoryPost.put(Jsonkey.Bucket.getKey(), bucket);
            }
            if (afterId != null) {
                getCreditHistoryPost.put(Jsonkey.BeginAfterID.getKey(), afterId);
            }
            setPost(getCreditHistoryPost);
        } catch (JSONException ex) {
            ex.printStackTrace();
            this.constructError_ = true;
        }
    }

    public ServerRequestGetRewardHistory(String requestPath, JSONObject post, Context context) {
        super(requestPath, post, context);
    }

    public void onRequestSucceeded(ServerResponse resp, Branch branch) {
        if (this.callback_ != null) {
            this.callback_.onReceivingResponse(resp.getArray(), null);
        }
    }

    public void handleFailure(int statusCode, String causeMsg) {
        if (this.callback_ != null) {
            this.callback_.onReceivingResponse(null, new BranchError("Trouble retrieving user credit history. " + causeMsg, statusCode));
        }
    }

    public boolean handleErrors(Context context) {
        if (super.doesAppHasInternetPermission(context)) {
            return false;
        }
        if (this.callback_ != null) {
            this.callback_.onReceivingResponse(null, new BranchError("Trouble retrieving user credit history.", BranchError.ERR_NO_INTERNET_PERMISSION));
        }
        return true;
    }

    public boolean isGetRequest() {
        return false;
    }

    public void clearCallbacks() {
        this.callback_ = null;
    }
}
