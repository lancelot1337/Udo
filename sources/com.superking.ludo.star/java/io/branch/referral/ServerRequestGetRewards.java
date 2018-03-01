package io.branch.referral;

import android.content.Context;
import io.branch.referral.Branch.BranchReferralStateChangedListener;
import io.branch.referral.Defines.RequestPath;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;

class ServerRequestGetRewards extends ServerRequest {
    BranchReferralStateChangedListener callback_;

    public ServerRequestGetRewards(Context context, BranchReferralStateChangedListener callback) {
        super(context, RequestPath.GetCredits.getPath());
        this.callback_ = callback;
    }

    public ServerRequestGetRewards(String requestPath, JSONObject post, Context context) {
        super(requestPath, post, context);
    }

    public String getRequestUrl() {
        return super.getRequestUrl() + this.prefHelper_.getIdentityID();
    }

    public void onRequestSucceeded(ServerResponse resp, Branch branch) {
        boolean updateListener = false;
        Iterator<?> keys = resp.getObject().keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            try {
                int credits = resp.getObject().getInt(key);
                if (credits != this.prefHelper_.getCreditCount(key)) {
                    updateListener = true;
                }
                this.prefHelper_.setCreditCount(key, credits);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (this.callback_ != null) {
            this.callback_.onStateChanged(updateListener, null);
        }
    }

    public void handleFailure(int statusCode, String causeMsg) {
        if (this.callback_ != null) {
            this.callback_.onStateChanged(false, new BranchError("Trouble retrieving user credits. " + causeMsg, statusCode));
        }
    }

    public boolean handleErrors(Context context) {
        if (super.doesAppHasInternetPermission(context)) {
            return false;
        }
        if (this.callback_ != null) {
            this.callback_.onStateChanged(false, new BranchError("Trouble retrieving user credits.", BranchError.ERR_NO_INTERNET_PERMISSION));
        }
        return true;
    }

    public boolean isGetRequest() {
        return true;
    }

    public void clearCallbacks() {
        this.callback_ = null;
    }
}
