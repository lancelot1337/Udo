package io.branch.referral;

import android.app.Activity;
import android.content.Context;
import io.branch.indexing.ContentDiscoverer;
import io.branch.indexing.ContentDiscoveryManifest;
import io.branch.referral.Branch.IBranchViewControl;
import io.branch.referral.Defines.Jsonkey;
import org.json.JSONException;
import org.json.JSONObject;

abstract class ServerRequestInitSession extends ServerRequest {
    protected static final String ACTION_INSTALL = "install";
    protected static final String ACTION_OPEN = "open";
    private final ContentDiscoveryManifest contentDiscoveryManifest_ = ContentDiscoveryManifest.getInstance(this.context_);
    private final Context context_;

    public abstract String getRequestActionName();

    public abstract boolean hasCallBack();

    public ServerRequestInitSession(Context context, String requestPath) {
        super(context, requestPath);
        this.context_ = context;
    }

    protected ServerRequestInitSession(String requestPath, JSONObject post, Context context) {
        super(requestPath, post, context);
        this.context_ = context;
    }

    public boolean isGAdsParamsRequired() {
        return true;
    }

    public static boolean isInitSessionAction(String actionName) {
        if (actionName != null) {
            return actionName.equalsIgnoreCase(ACTION_OPEN) || actionName.equalsIgnoreCase(ACTION_INSTALL);
        } else {
            return false;
        }
    }

    public boolean handleBranchViewIfAvailable(ServerResponse resp) {
        if (resp == null || resp.getObject() == null || !resp.getObject().has(Jsonkey.BranchViewData.getKey())) {
            return false;
        }
        try {
            JSONObject branchViewJsonObj = resp.getObject().getJSONObject(Jsonkey.BranchViewData.getKey());
            String actionName = getRequestActionName();
            if (Branch.getInstance().currentActivityReference_ == null || Branch.getInstance().currentActivityReference_.get() == null) {
                return BranchViewHandler.getInstance().markInstallOrOpenBranchViewPending(branchViewJsonObj, actionName);
            }
            Activity currentActivity = (Activity) Branch.getInstance().currentActivityReference_.get();
            boolean isActivityEnabledForBranchView = true;
            if (currentActivity instanceof IBranchViewControl) {
                isActivityEnabledForBranchView = !((IBranchViewControl) currentActivity).skipBranchViewsOnThisActivity();
            }
            if (isActivityEnabledForBranchView) {
                return BranchViewHandler.getInstance().showBranchView(branchViewJsonObj, actionName, currentActivity, Branch.getInstance());
            }
            return BranchViewHandler.getInstance().markInstallOrOpenBranchViewPending(branchViewJsonObj, actionName);
        } catch (JSONException e) {
            return false;
        }
    }

    public void onRequestSucceeded(ServerResponse response, Branch branch) {
        try {
            this.prefHelper_.setLinkClickIdentifier(SystemObserver.BLANK);
            this.prefHelper_.setExternalIntentUri(SystemObserver.BLANK);
            this.prefHelper_.setExternalIntentExtra(SystemObserver.BLANK);
            this.prefHelper_.setAppLink(SystemObserver.BLANK);
            this.prefHelper_.setPushIdentifier(SystemObserver.BLANK);
            this.prefHelper_.setIsAppLinkTriggeredInit(Boolean.valueOf(false));
            this.prefHelper_.setInstallReferrerParams(SystemObserver.BLANK);
            if (response.getObject() != null && response.getObject().has(Jsonkey.Data.getKey())) {
                new ExtendedAnswerProvider().provideData(this instanceof ServerRequestRegisterInstall ? ExtendedAnswerProvider.KIT_EVENT_INSTALL : ExtendedAnswerProvider.KIT_EVENT_OPEN, new JSONObject(response.getObject().getString(Jsonkey.Data.getKey())), this.prefHelper_.getIdentityID());
            }
        } catch (JSONException e) {
        }
    }

    protected void onInitSessionCompleted(ServerResponse response, Branch branch) {
        if (this.contentDiscoveryManifest_ != null) {
            this.contentDiscoveryManifest_.onBranchInitialised(response.getObject());
            if (branch.currentActivityReference_ != null) {
                try {
                    ContentDiscoverer.getInstance().onSessionStarted((Activity) branch.currentActivityReference_.get(), branch.sessionReferredLink_);
                } catch (Exception e) {
                }
            }
        }
    }

    public void updateLinkClickIdentifier() {
        if (!this.prefHelper_.getLinkClickIdentifier().equals(SystemObserver.BLANK)) {
            try {
                getPost().put(Jsonkey.LinkIdentifier.getKey(), this.prefHelper_.getLinkClickIdentifier());
            } catch (JSONException e) {
            }
        }
    }

    public void onPreExecute() {
        JSONObject post = getPost();
        try {
            if (!this.prefHelper_.getLinkClickIdentifier().equals(SystemObserver.BLANK)) {
                post.put(Jsonkey.LinkIdentifier.getKey(), this.prefHelper_.getLinkClickIdentifier());
            }
            if (!this.prefHelper_.getAppLink().equals(SystemObserver.BLANK)) {
                post.put(Jsonkey.AndroidAppLinkURL.getKey(), this.prefHelper_.getAppLink());
            }
            if (!this.prefHelper_.getPushIdentifier().equals(SystemObserver.BLANK)) {
                post.put(Jsonkey.AndroidPushIdentifier.getKey(), this.prefHelper_.getPushIdentifier());
            }
            if (!this.prefHelper_.getExternalIntentUri().equals(SystemObserver.BLANK)) {
                post.put(Jsonkey.External_Intent_URI.getKey(), this.prefHelper_.getExternalIntentUri());
            }
            if (!this.prefHelper_.getExternalIntentExtra().equals(SystemObserver.BLANK)) {
                post.put(Jsonkey.External_Intent_Extra.getKey(), this.prefHelper_.getExternalIntentExtra());
            }
            if (this.contentDiscoveryManifest_ != null) {
                JSONObject cdObj = new JSONObject();
                cdObj.put(ContentDiscoveryManifest.MANIFEST_VERSION_KEY, this.contentDiscoveryManifest_.getManifestVersion());
                cdObj.put(ContentDiscoveryManifest.PACKAGE_NAME_KEY, this.context_.getPackageName());
                post.put(ContentDiscoveryManifest.CONTENT_DISCOVER_KEY, cdObj);
            }
        } catch (JSONException e) {
        }
    }
}
