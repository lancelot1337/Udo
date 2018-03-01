package io.branch.referral;

import android.content.Context;
import io.branch.referral.Branch.BranchLinkCreateListener;

public class BranchReferralUrlBuilder extends BranchUrlBuilder<BranchReferralUrlBuilder> {
    public BranchReferralUrlBuilder(Context context, String channel) {
        super(context);
        this.channel_ = channel;
        this.type_ = 0;
        this.feature_ = Branch.FEATURE_TAG_REFERRAL;
    }

    public String getReferralUrl() {
        return super.getUrl();
    }

    public void generateReferralUrl(BranchLinkCreateListener callback) {
        super.generateUrl(callback);
    }
}
