package io.branch.referral;

import android.content.Context;
import android.util.Log;
import io.branch.referral.Branch.BranchReferralStateChangedListener;
import io.branch.referral.Defines.Jsonkey;
import io.branch.referral.Defines.RequestPath;
import org.json.JSONException;
import org.json.JSONObject;

class ServerRequestRedeemRewards extends ServerRequest {
    int actualNumOfCreditsToRedeem_ = 0;
    BranchReferralStateChangedListener callback_;

    public ServerRequestRedeemRewards(Context context, String bucketName, int numOfCreditsToRedeem, BranchReferralStateChangedListener callback) {
        super(context, RequestPath.RedeemRewards.getPath());
        this.callback_ = callback;
        int availableCredits = this.prefHelper_.getCreditCount(bucketName);
        this.actualNumOfCreditsToRedeem_ = numOfCreditsToRedeem;
        if (numOfCreditsToRedeem > availableCredits) {
            this.actualNumOfCreditsToRedeem_ = availableCredits;
            Log.i("BranchSDK", "Branch Warning: You're trying to redeem more credits than are available. Have you updated loaded rewards");
        }
        if (this.actualNumOfCreditsToRedeem_ > 0) {
            JSONObject post = new JSONObject();
            try {
                post.put(Jsonkey.IdentityID.getKey(), this.prefHelper_.getIdentityID());
                post.put(Jsonkey.DeviceFingerprintID.getKey(), this.prefHelper_.getDeviceFingerPrintID());
                post.put(Jsonkey.SessionID.getKey(), this.prefHelper_.getSessionID());
                if (!this.prefHelper_.getLinkClickID().equals(SystemObserver.BLANK)) {
                    post.put(Jsonkey.LinkClickID.getKey(), this.prefHelper_.getLinkClickID());
                }
                post.put(Jsonkey.Bucket.getKey(), bucketName);
                post.put(Jsonkey.Amount.getKey(), this.actualNumOfCreditsToRedeem_);
                setPost(post);
            } catch (JSONException ex) {
                ex.printStackTrace();
                this.constructError_ = true;
            }
        }
    }

    public ServerRequestRedeemRewards(String requestPath, JSONObject post, Context context) {
        super(requestPath, post, context);
    }

    public boolean handleErrors(Context context) {
        if (super.doesAppHasInternetPermission(context)) {
            if (this.actualNumOfCreditsToRedeem_ > 0) {
                return false;
            }
            if (this.callback_ == null) {
                return true;
            }
            this.callback_.onStateChanged(false, new BranchError("Trouble redeeming rewards.", BranchError.ERR_BRANCH_REDEEM_REWARD));
            return true;
        } else if (this.callback_ == null) {
            return true;
        } else {
            this.callback_.onStateChanged(false, new BranchError("Trouble redeeming rewards.", BranchError.ERR_NO_INTERNET_PERMISSION));
            return true;
        }
    }

    public void onRequestSucceeded(ServerResponse resp, Branch branch) {
        boolean isRedemptionSucceeded = false;
        JSONObject post = getPost();
        if (post != null && post.has(Jsonkey.Bucket.getKey()) && post.has(Jsonkey.Amount.getKey())) {
            try {
                int redeemedCredits = post.getInt(Jsonkey.Amount.getKey());
                String creditBucket = post.getString(Jsonkey.Bucket.getKey());
                isRedemptionSucceeded = redeemedCredits > 0;
                this.prefHelper_.setCreditCount(creditBucket, this.prefHelper_.getCreditCount(creditBucket) - redeemedCredits);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (this.callback_ != null) {
            this.callback_.onStateChanged(isRedemptionSucceeded, isRedemptionSucceeded ? null : new BranchError("Trouble redeeming rewards.", BranchError.ERR_BRANCH_REDEEM_REWARD));
        }
    }

    public void handleFailure(int statusCode, String causeMsg) {
        if (this.callback_ != null) {
            this.callback_.onStateChanged(false, new BranchError("Trouble redeeming rewards. " + causeMsg, statusCode));
        }
    }

    public boolean isGetRequest() {
        return false;
    }

    public void clearCallbacks() {
        this.callback_ = null;
    }
}
