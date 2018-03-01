package io.branch.referral;

import android.content.Context;
import android.util.Log;
import io.branch.referral.Branch.BranchLinkCreateListener;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

abstract class BranchUrlBuilder<T extends BranchUrlBuilder> {
    protected String alias_;
    protected Branch branchReferral_ = Branch.getInstance();
    protected String campaign_;
    protected String channel_;
    private final Context context_;
    private boolean defaultToLongUrl_;
    protected int duration_ = 0;
    protected String feature_;
    protected JSONObject params_;
    protected String stage_;
    protected ArrayList<String> tags_;
    protected int type_ = 0;

    protected BranchUrlBuilder(Context context) {
        this.context_ = context.getApplicationContext();
        this.defaultToLongUrl_ = true;
    }

    public T addTag(String tag) {
        if (this.tags_ == null) {
            this.tags_ = new ArrayList();
        }
        this.tags_.add(tag);
        return this;
    }

    public T addTags(List<String> tags) {
        if (this.tags_ == null) {
            this.tags_ = new ArrayList();
        }
        this.tags_.addAll(tags);
        return this;
    }

    public T addParameters(String key, String value) {
        try {
            if (this.params_ == null) {
                this.params_ = new JSONObject();
            }
            this.params_.put(key, value);
        } catch (JSONException e) {
        }
        return this;
    }

    public T addParameters(String key, JSONArray value) {
        try {
            if (this.params_ == null) {
                this.params_ = new JSONObject();
            }
            this.params_.put(key, value);
        } catch (JSONException e) {
        }
        return this;
    }

    public T setDefaultToLongUrl(boolean defaultToLongUrl) {
        this.defaultToLongUrl_ = defaultToLongUrl;
        return this;
    }

    protected String getUrl() {
        if (this.branchReferral_ == null) {
            return null;
        }
        return this.branchReferral_.generateShortLinkInternal(new ServerRequestCreateUrl(this.context_, this.alias_, this.type_, this.duration_, this.tags_, this.channel_, this.feature_, this.stage_, this.campaign_, BranchUtil.formatAndStringifyLinkParam(this.params_), null, false, this.defaultToLongUrl_));
    }

    protected void generateUrl(BranchLinkCreateListener callback) {
        generateUrlInternal(callback, false);
    }

    protected void generateUrlInternal(BranchLinkCreateListener callback, boolean isFromShareSheet) {
        if (this.branchReferral_ != null) {
            ServerRequestCreateUrl req = new ServerRequestCreateUrl(this.context_, this.alias_, this.type_, this.duration_, this.tags_, this.channel_, this.feature_, this.stage_, this.campaign_, BranchUtil.formatAndStringifyLinkParam(this.params_), callback, true, this.defaultToLongUrl_);
            req.setIsReqStartedFromBranchShareSheet(isFromShareSheet);
            this.branchReferral_.generateShortLinkInternal(req);
            return;
        }
        if (callback != null) {
            callback.onLinkCreate(null, new BranchError("session has not been initialized", BranchError.ERR_NO_SESSION));
        }
        Log.i("BranchSDK", "Branch Warning: User session has not been initialized");
    }
}
