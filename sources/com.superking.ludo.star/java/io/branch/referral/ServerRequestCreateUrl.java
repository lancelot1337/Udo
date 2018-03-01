package io.branch.referral;

import android.content.Context;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import com.ironsource.sdk.utils.Constants.RequestParameters;
import io.branch.referral.Branch.BranchLinkCreateListener;
import io.branch.referral.Defines.Jsonkey;
import io.branch.referral.Defines.LinkParam;
import io.branch.referral.Defines.RequestPath;
import java.util.Collection;
import org.json.JSONException;
import org.json.JSONObject;

class ServerRequestCreateUrl extends ServerRequest {
    private static final String DEF_BASE_URL = "https://bnc.lt/a/";
    private BranchLinkCreateListener callback_;
    private boolean defaultToLongUrl_ = true;
    private boolean isAsync_ = true;
    private boolean isReqStartedFromBranchShareSheet_;
    private BranchLinkData linkPost_;

    public ServerRequestCreateUrl(Context context, String alias, int type, int duration, Collection<String> tags, String channel, String feature, String stage, String campaign, String params, BranchLinkCreateListener callback, boolean async, boolean defaultToLongUrl) {
        super(context, RequestPath.GetURL.getPath());
        this.callback_ = callback;
        this.isAsync_ = async;
        this.defaultToLongUrl_ = defaultToLongUrl;
        this.linkPost_ = new BranchLinkData();
        try {
            this.linkPost_.put(Jsonkey.IdentityID.getKey(), this.prefHelper_.getIdentityID());
            this.linkPost_.put(Jsonkey.DeviceFingerprintID.getKey(), this.prefHelper_.getDeviceFingerPrintID());
            this.linkPost_.put(Jsonkey.SessionID.getKey(), this.prefHelper_.getSessionID());
            if (!this.prefHelper_.getLinkClickID().equals(SystemObserver.BLANK)) {
                this.linkPost_.put(Jsonkey.LinkClickID.getKey(), this.prefHelper_.getLinkClickID());
            }
            this.linkPost_.putType(type);
            this.linkPost_.putDuration(duration);
            this.linkPost_.putTags(tags);
            this.linkPost_.putAlias(alias);
            this.linkPost_.putChannel(channel);
            this.linkPost_.putFeature(feature);
            this.linkPost_.putStage(stage);
            this.linkPost_.putCampaign(campaign);
            this.linkPost_.putParams(params);
            setPost(this.linkPost_);
        } catch (JSONException ex) {
            ex.printStackTrace();
            this.constructError_ = true;
        }
    }

    public ServerRequestCreateUrl(String requestPath, JSONObject post, Context context) {
        super(requestPath, post, context);
    }

    public BranchLinkData getLinkPost() {
        return this.linkPost_;
    }

    boolean isDefaultToLongUrl() {
        return this.defaultToLongUrl_;
    }

    public boolean handleErrors(Context context) {
        if (super.doesAppHasInternetPermission(context)) {
            if (this.isAsync_ || hasUser()) {
                return false;
            }
            return true;
        } else if (this.callback_ == null) {
            return true;
        } else {
            this.callback_.onLinkCreate(null, new BranchError("Trouble creating a URL.", BranchError.ERR_NO_INTERNET_PERMISSION));
            return true;
        }
    }

    public void onRequestSucceeded(ServerResponse resp, Branch branch) {
        try {
            String url = resp.getObject().getString(ParametersKeys.URL);
            if (this.callback_ != null) {
                this.callback_.onLinkCreate(url, null);
            }
            updateShareEventToFabric(url);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onUrlAvailable(String url) {
        if (this.callback_ != null) {
            this.callback_.onLinkCreate(url, null);
        }
        updateShareEventToFabric(url);
    }

    public void handleFailure(int statusCode, String causeMsg) {
        if (this.callback_ != null) {
            String failedUrl = null;
            if (this.defaultToLongUrl_) {
                failedUrl = getLongUrl();
            }
            this.callback_.onLinkCreate(failedUrl, new BranchError("Trouble creating a URL. " + causeMsg, statusCode));
        }
    }

    public String getLongUrl() {
        if (this.prefHelper_.getUserURL().equals(SystemObserver.BLANK)) {
            return generateLongUrlWithParams(DEF_BASE_URL + this.prefHelper_.getBranchKey());
        }
        return generateLongUrlWithParams(this.prefHelper_.getUserURL());
    }

    public void handleDuplicateURLError() {
        if (this.callback_ != null) {
            this.callback_.onLinkCreate(null, new BranchError("Trouble creating a URL.", BranchError.ERR_BRANCH_DUPLICATE_URL));
        }
    }

    private boolean hasUser() {
        return !this.prefHelper_.getIdentityID().equals(SystemObserver.BLANK);
    }

    public boolean isGetRequest() {
        return false;
    }

    public void clearCallbacks() {
        this.callback_ = null;
    }

    public boolean isAsync() {
        return this.isAsync_;
    }

    private String generateLongUrlWithParams(String baseUrl) {
        String longUrl = baseUrl + "?";
        Collection<String> tags = this.linkPost_.getTags();
        if (tags != null) {
            for (String tag : tags) {
                if (tag != null && tag.length() > 0) {
                    longUrl = longUrl + LinkParam.Tags + RequestParameters.EQUAL + tag + RequestParameters.AMPERSAND;
                }
            }
        }
        String alias = this.linkPost_.getAlias();
        if (alias != null && alias.length() > 0) {
            longUrl = longUrl + LinkParam.Alias + RequestParameters.EQUAL + alias + RequestParameters.AMPERSAND;
        }
        String channel = this.linkPost_.getChannel();
        if (channel != null && channel.length() > 0) {
            longUrl = longUrl + LinkParam.Channel + RequestParameters.EQUAL + channel + RequestParameters.AMPERSAND;
        }
        String feature = this.linkPost_.getFeature();
        if (feature != null && feature.length() > 0) {
            longUrl = longUrl + LinkParam.Feature + RequestParameters.EQUAL + feature + RequestParameters.AMPERSAND;
        }
        String stage = this.linkPost_.getStage();
        if (stage != null && stage.length() > 0) {
            longUrl = longUrl + LinkParam.Stage + RequestParameters.EQUAL + stage + RequestParameters.AMPERSAND;
        }
        String campaign = this.linkPost_.getCampaign();
        if (campaign != null && campaign.length() > 0) {
            longUrl = longUrl + LinkParam.Campaign + RequestParameters.EQUAL + campaign + RequestParameters.AMPERSAND;
        }
        long duration = (long) this.linkPost_.getDuration();
        longUrl = (longUrl + LinkParam.Type + RequestParameters.EQUAL + ((long) this.linkPost_.getType()) + RequestParameters.AMPERSAND) + LinkParam.Duration + RequestParameters.EQUAL + duration + RequestParameters.AMPERSAND;
        String params = this.linkPost_.getParams();
        if (params == null || params.length() <= 0) {
            return longUrl;
        }
        return longUrl + "source=android&data=" + Base64.encodeToString(params.getBytes(), 2);
    }

    void setIsReqStartedFromBranchShareSheet(boolean startedByShareSheet) {
        this.isReqStartedFromBranchShareSheet_ = startedByShareSheet;
    }

    boolean isReqStartedFromBranchShareSheet() {
        return this.isReqStartedFromBranchShareSheet_;
    }

    private void updateShareEventToFabric(String url) {
        JSONObject linkDataJsonObj = this.linkPost_.getLinkDataJsonObject();
        if (isReqStartedFromBranchShareSheet() && linkDataJsonObj != null) {
            new ExtendedAnswerProvider().provideData(ExtendedAnswerProvider.KIT_EVENT_SHARE, linkDataJsonObj, this.prefHelper_.getIdentityID());
        }
    }
}
