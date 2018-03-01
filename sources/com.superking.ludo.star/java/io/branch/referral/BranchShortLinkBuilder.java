package io.branch.referral;

import android.content.Context;
import io.branch.referral.Branch.BranchLinkCreateListener;
import org.json.JSONObject;

public class BranchShortLinkBuilder extends BranchUrlBuilder<BranchShortLinkBuilder> {
    public BranchShortLinkBuilder(Context context) {
        super(context);
    }

    public BranchShortLinkBuilder setAlias(String alias) {
        this.alias_ = alias;
        return this;
    }

    public BranchShortLinkBuilder setChannel(String channel) {
        this.channel_ = channel;
        return this;
    }

    public BranchShortLinkBuilder setDuration(int duration) {
        this.duration_ = duration;
        return this;
    }

    public BranchShortLinkBuilder setFeature(String feature) {
        this.feature_ = feature;
        return this;
    }

    public BranchShortLinkBuilder setParameters(JSONObject parameters) {
        this.params_ = parameters;
        return this;
    }

    public BranchShortLinkBuilder setStage(String stage) {
        this.stage_ = stage;
        return this;
    }

    public BranchShortLinkBuilder setCampaign(String campaign) {
        this.campaign_ = campaign;
        return this;
    }

    public BranchShortLinkBuilder setType(int type) {
        this.type_ = type;
        return this;
    }

    public String getShortUrl() {
        return super.getUrl();
    }

    public void generateShortUrl(BranchLinkCreateListener callback) {
        super.generateUrl(callback);
    }

    void generateShortUrlInternal(BranchLinkCreateListener callback, boolean isFromShareSheet) {
        super.generateUrlInternal(callback, isFromShareSheet);
    }
}
