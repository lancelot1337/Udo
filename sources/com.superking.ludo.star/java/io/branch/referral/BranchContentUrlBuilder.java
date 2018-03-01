package io.branch.referral;

import android.content.Context;
import io.branch.referral.Branch.BranchLinkCreateListener;

public class BranchContentUrlBuilder extends BranchUrlBuilder<BranchContentUrlBuilder> {
    public BranchContentUrlBuilder(Context context, String channel) {
        super(context);
        this.channel_ = channel;
        this.type_ = 0;
        this.feature_ = Branch.FEATURE_TAG_SHARE;
    }

    public String getContentUrl() {
        return getUrl();
    }

    public void generateContentUrl(BranchLinkCreateListener callback) {
        super.generateUrl(callback);
    }
}
