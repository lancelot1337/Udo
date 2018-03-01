package io.branch.referral;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch.BranchReferralInitListener;
import io.branch.referral.Branch.BranchUniversalReferralInitListener;
import io.branch.referral.util.LinkProperties;
import org.json.JSONObject;

class BranchUniversalReferralInitWrapper implements BranchReferralInitListener {
    private final BranchUniversalReferralInitListener universalReferralInitListener_;

    public BranchUniversalReferralInitWrapper(BranchUniversalReferralInitListener universalReferralInitListener) {
        this.universalReferralInitListener_ = universalReferralInitListener;
    }

    public void onInitFinished(JSONObject referringParams, BranchError error) {
        if (this.universalReferralInitListener_ == null) {
            return;
        }
        if (error != null) {
            this.universalReferralInitListener_.onInitFinished(null, null, error);
            return;
        }
        this.universalReferralInitListener_.onInitFinished(BranchUniversalObject.getReferredBranchUniversalObject(), LinkProperties.getReferredLinkProperties(), error);
    }
}
