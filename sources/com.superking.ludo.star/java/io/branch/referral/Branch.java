package io.branch.referral;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import com.facebook.internal.ServerProtocol;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import com.ironsource.sdk.utils.Constants.RequestParameters;
import cz.msebera.android.httpclient.HttpHost;
import cz.msebera.android.httpclient.HttpStatus;
import io.branch.indexing.BranchUniversalObject;
import io.branch.indexing.BranchUniversalObject.RegisterViewStatusListener;
import io.branch.indexing.ContentDiscoverer;
import io.branch.referral.BranchViewHandler.IBranchViewEvents;
import io.branch.referral.DeferredAppLinkDataHandler.AppLinkFetchEvents;
import io.branch.referral.Defines.Jsonkey;
import io.branch.referral.SharingHelper.SHARE_WITH;
import io.branch.referral.util.CommerceEvent;
import io.branch.referral.util.LinkProperties;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.cocos2dx.lib.BuildConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Branch implements IBranchViewEvents, IInstallReferrerEvents, GAdsParamsFetchEvents {
    public static final String ALWAYS_DEEPLINK = "$always_deeplink";
    private static final String AUTO_DEEP_LINKED = "io.branch.sdk.auto_linked";
    private static final String AUTO_DEEP_LINK_DISABLE = "io.branch.sdk.auto_link_disable";
    private static final String AUTO_DEEP_LINK_KEY = "io.branch.sdk.auto_link_keys";
    private static final String AUTO_DEEP_LINK_PATH = "io.branch.sdk.auto_link_path";
    private static final String AUTO_DEEP_LINK_REQ_CODE = "io.branch.sdk.auto_link_request_code";
    public static final String DEEPLINK_PATH = "$deeplink_path";
    private static final int DEF_AUTO_DEEP_LINK_REQ_CODE = 1501;
    private static final String[] EXTERNAL_INTENT_EXTRA_KEY_WHITE_LIST;
    private static final String FABRIC_BRANCH_API_KEY = "io.branch.apiKey";
    public static final String FEATURE_TAG_DEAL = "deal";
    public static final String FEATURE_TAG_GIFT = "gift";
    public static final String FEATURE_TAG_INVITE = "invite";
    public static final String FEATURE_TAG_REFERRAL = "referral";
    public static final String FEATURE_TAG_SHARE = "share";
    private static int LATCH_WAIT_UNTIL = 2500;
    public static final int LINK_TYPE_ONE_TIME_USE = 1;
    public static final int LINK_TYPE_UNLIMITED_USE = 0;
    public static final String OG_APP_ID = "$og_app_id";
    public static final String OG_DESC = "$og_description";
    public static final String OG_IMAGE_URL = "$og_image_url";
    public static final String OG_TITLE = "$og_title";
    public static final String OG_URL = "$og_url";
    public static final String OG_VIDEO = "$og_video";
    private static long PLAYSTORE_REFERRAL_FETCH_WAIT_FOR = 5000;
    private static final int PREVENT_CLOSE_TIMEOUT = 500;
    public static final String REDEEM_CODE = "$redeem_code";
    public static final String REDIRECT_ANDROID_URL = "$android_url";
    public static final String REDIRECT_BLACKBERRY_URL = "$blackberry_url";
    public static final String REDIRECT_DESKTOP_URL = "$desktop_url";
    public static final String REDIRECT_FIRE_URL = "$fire_url";
    public static final String REDIRECT_IOS_URL = "$ios_url";
    public static final String REDIRECT_IPAD_URL = "$ipad_url";
    public static final String REDIRECT_WINDOWS_PHONE_URL = "$windows_phone_url";
    public static final String REFERRAL_BUCKET_DEFAULT = "default";
    public static final String REFERRAL_CODE = "referral_code";
    public static final int REFERRAL_CODE_AWARD_UNIQUE = 0;
    public static final int REFERRAL_CODE_AWARD_UNLIMITED = 1;
    public static final int REFERRAL_CODE_LOCATION_BOTH = 3;
    public static final int REFERRAL_CODE_LOCATION_REFERREE = 0;
    public static final int REFERRAL_CODE_LOCATION_REFERRING_USER = 2;
    public static final String REFERRAL_CODE_TYPE = "credit";
    public static final int REFERRAL_CREATION_SOURCE_SDK = 2;
    private static final int SESSION_KEEPALIVE = 2000;
    private static final String TAG = "BranchSDK";
    private static Branch branchReferral_;
    private static boolean checkInstallReferrer_ = false;
    private static String cookieBasedMatchDomain_ = "app.link";
    private static CUSTOM_REFERRABLE_SETTINGS customReferrableSettings_ = CUSTOM_REFERRABLE_SETTINGS.USE_DEFAULT;
    private static boolean disableDeviceIDFetch_;
    private static boolean isActivityLifeCycleCallbackRegistered_ = false;
    private static boolean isAutoSessionMode_ = false;
    private static boolean isLogging_ = false;
    private static boolean isSimulatingInstalls_;
    private ScheduledFuture<?> appListingSchedule_;
    private Context context_;
    WeakReference<Activity> currentActivityReference_;
    private JSONObject deeplinkDebugParams_;
    private boolean enableFacebookAppLinkCheck_ = false;
    private List<String> externalUriWhiteList_;
    private CountDownLatch getFirstReferringParamsLatch = null;
    private CountDownLatch getLatestReferringParamsLatch = null;
    private boolean handleDelayedNewIntents_ = false;
    private boolean hasNetwork_;
    private SESSION_STATE initState_ = SESSION_STATE.UNINITIALISED;
    private final ConcurrentHashMap<String, String> instrumentationExtraData_;
    private INTENT_STATE intentState_ = INTENT_STATE.PENDING;
    private boolean isGAParamsFetchInProgress_ = false;
    private boolean isInitReportedThroughCallBack = false;
    private BranchRemoteInterface kRemoteInterface_;
    private Map<BranchLinkData, String> linkCache_;
    final Object lock;
    private int networkCount_;
    private boolean performCookieBasedStrongMatchingOnGAIDAvailable = false;
    private PrefHelper prefHelper_;
    private ServerRequestQueue requestQueue_;
    private Semaphore serverSema_;
    String sessionReferredLink_;
    private ShareLinkManager shareLinkManager_;
    private List<String> skipExternalUriHosts_;
    private final SystemObserver systemObserver_;

    public interface BranchLinkShareListener {
        void onChannelSelected(String str);

        void onLinkShareResponse(String str, String str2, BranchError branchError);

        void onShareLinkDialogDismissed();

        void onShareLinkDialogLaunched();
    }

    @TargetApi(14)
    private class BranchActivityLifeCycleObserver implements ActivityLifecycleCallbacks {
        private int activityCnt_;

        private BranchActivityLifeCycleObserver() {
            this.activityCnt_ = Branch.REFERRAL_CODE_LOCATION_REFERREE;
        }

        public void onActivityCreated(Activity activity, Bundle bundle) {
            Branch.this.intentState_ = Branch.this.handleDelayedNewIntents_ ? INTENT_STATE.PENDING : INTENT_STATE.READY;
            if (BranchViewHandler.getInstance().isInstallOrOpenBranchViewPending(activity.getApplicationContext())) {
                BranchViewHandler.getInstance().showPendingBranchView(activity);
            }
        }

        public void onActivityStarted(Activity activity) {
            Branch.this.intentState_ = Branch.this.handleDelayedNewIntents_ ? INTENT_STATE.PENDING : INTENT_STATE.READY;
            if (Branch.this.initState_ == SESSION_STATE.INITIALISED) {
                try {
                    ContentDiscoverer.getInstance().discoverContent(activity, Branch.this.sessionReferredLink_);
                } catch (Exception e) {
                }
            }
            if (this.activityCnt_ < Branch.REFERRAL_CODE_AWARD_UNLIMITED) {
                if (Branch.this.initState_ == SESSION_STATE.INITIALISED) {
                    Branch.this.initState_ = SESSION_STATE.UNINITIALISED;
                }
                if (BranchUtil.isTestModeEnabled(Branch.this.context_)) {
                    Branch.this.prefHelper_.setExternDebug();
                }
                Branch.this.prefHelper_.setLogging(Branch.getIsLogging());
                Branch.this.startSession(activity);
            } else if (Branch.this.checkIntentForSessionRestart(activity.getIntent())) {
                Branch.this.initState_ = SESSION_STATE.UNINITIALISED;
                Branch.this.startSession(activity);
            }
            this.activityCnt_ += Branch.REFERRAL_CODE_AWARD_UNLIMITED;
        }

        public void onActivityResumed(Activity activity) {
            if (Branch.this.checkIntentForSessionRestart(activity.getIntent())) {
                Branch.this.initState_ = SESSION_STATE.UNINITIALISED;
                Branch.this.startSession(activity);
            }
            Branch.this.currentActivityReference_ = new WeakReference(activity);
            if (Branch.this.handleDelayedNewIntents_) {
                Branch.this.intentState_ = INTENT_STATE.READY;
                Branch.this.onIntentReady(activity);
            }
        }

        public void onActivityPaused(Activity activity) {
            if (Branch.this.shareLinkManager_ != null) {
                Branch.this.shareLinkManager_.cancelShareLinkDialog(true);
            }
        }

        public void onActivityStopped(Activity activity) {
            ContentDiscoverer.getInstance().onActivityStopped(activity);
            this.activityCnt_--;
            if (this.activityCnt_ < Branch.REFERRAL_CODE_AWARD_UNLIMITED) {
                Branch.this.closeSessionInternal();
            }
        }

        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        }

        public void onActivityDestroyed(Activity activity) {
            if (Branch.this.currentActivityReference_ != null && Branch.this.currentActivityReference_.get() == activity) {
                Branch.this.currentActivityReference_.clear();
            }
            BranchViewHandler.getInstance().onCurrentActivityDestroyed(activity);
        }
    }

    public interface BranchLinkCreateListener {
        void onLinkCreate(String str, BranchError branchError);
    }

    public interface BranchListResponseListener {
        void onReceivingResponse(JSONArray jSONArray, BranchError branchError);
    }

    private class BranchPostTask extends BranchAsyncTask<Void, Void, ServerResponse> {
        ServerRequest thisReq_;
        int timeOut_ = Branch.REFERRAL_CODE_LOCATION_REFERREE;

        public BranchPostTask(ServerRequest request) {
            this.thisReq_ = request;
            this.timeOut_ = Branch.this.prefHelper_.getTimeout();
        }

        protected void onPreExecute() {
            super.onPreExecute();
            this.thisReq_.onPreExecute();
        }

        protected ServerResponse doInBackground(Void... voids) {
            if (this.thisReq_ instanceof ServerRequestInitSession) {
                ((ServerRequestInitSession) this.thisReq_).updateLinkClickIdentifier();
            }
            Branch.this.addExtraInstrumentationData(this.thisReq_.getRequestPath() + "-" + Jsonkey.Queue_Wait_Time.getKey(), String.valueOf(this.thisReq_.getQueueWaitTime()));
            if (this.thisReq_.isGAdsParamsRequired()) {
                this.thisReq_.updateGAdsParams(Branch.this.systemObserver_);
            }
            if (this.thisReq_.isGetRequest()) {
                return Branch.this.kRemoteInterface_.make_restful_get(this.thisReq_.getRequestUrl(), this.thisReq_.getGetParams(), this.thisReq_.getRequestPath(), this.timeOut_);
            }
            return Branch.this.kRemoteInterface_.make_restful_post(this.thisReq_.getPostWithInstrumentationValues(Branch.this.instrumentationExtraData_), this.thisReq_.getRequestUrl(), this.thisReq_.getRequestPath(), this.timeOut_);
        }

        protected void onPostExecute(ServerResponse serverResponse) {
            super.onPostExecute(serverResponse);
            if (serverResponse != null) {
                try {
                    int status = serverResponse.getStatusCode();
                    Branch.this.hasNetwork_ = true;
                    if (status != HttpStatus.SC_OK) {
                        if (this.thisReq_ instanceof ServerRequestInitSession) {
                            Branch.this.initState_ = SESSION_STATE.UNINITIALISED;
                        }
                        if (status == HttpStatus.SC_CONFLICT) {
                            Branch.this.requestQueue_.remove(this.thisReq_);
                            if (this.thisReq_ instanceof ServerRequestCreateUrl) {
                                ((ServerRequestCreateUrl) this.thisReq_).handleDuplicateURLError();
                            } else {
                                Log.i(Branch.TAG, "Branch API Error: Conflicting resource error code from API");
                                Branch.this.handleFailure((int) Branch.REFERRAL_CODE_LOCATION_REFERREE, status);
                            }
                        } else {
                            ServerRequest req;
                            Branch.this.hasNetwork_ = false;
                            ArrayList<ServerRequest> requestToFail = new ArrayList();
                            for (int i = Branch.REFERRAL_CODE_LOCATION_REFERREE; i < Branch.this.requestQueue_.getSize(); i += Branch.REFERRAL_CODE_AWARD_UNLIMITED) {
                                requestToFail.add(Branch.this.requestQueue_.peekAt(i));
                            }
                            Iterator i$ = requestToFail.iterator();
                            while (i$.hasNext()) {
                                req = (ServerRequest) i$.next();
                                if (req == null || !req.shouldRetryOnFail()) {
                                    Branch.this.requestQueue_.remove(req);
                                }
                            }
                            Branch.this.networkCount_ = Branch.REFERRAL_CODE_LOCATION_REFERREE;
                            i$ = requestToFail.iterator();
                            while (i$.hasNext()) {
                                req = (ServerRequest) i$.next();
                                if (req != null) {
                                    req.handleFailure(status, serverResponse.getFailReason());
                                    if (req.shouldRetryOnFail()) {
                                        req.clearCallbacks();
                                    }
                                }
                            }
                        }
                    } else {
                        Branch.this.hasNetwork_ = true;
                        if (this.thisReq_ instanceof ServerRequestCreateUrl) {
                            if (serverResponse.getObject() != null) {
                                Branch.this.linkCache_.put(((ServerRequestCreateUrl) this.thisReq_).getLinkPost(), serverResponse.getObject().getString(ParametersKeys.URL));
                            }
                        } else if (this.thisReq_ instanceof ServerRequestLogout) {
                            Branch.this.linkCache_.clear();
                            Branch.this.requestQueue_.clear();
                        }
                        Branch.this.requestQueue_.dequeue();
                        if ((this.thisReq_ instanceof ServerRequestInitSession) || (this.thisReq_ instanceof ServerRequestIdentifyUserRequest)) {
                            JSONObject respJson = serverResponse.getObject();
                            if (respJson != null) {
                                boolean updateRequestsInQueue = false;
                                if (respJson.has(Jsonkey.SessionID.getKey())) {
                                    Branch.this.prefHelper_.setSessionID(respJson.getString(Jsonkey.SessionID.getKey()));
                                    updateRequestsInQueue = true;
                                }
                                if (respJson.has(Jsonkey.IdentityID.getKey())) {
                                    if (!Branch.this.prefHelper_.getIdentityID().equals(respJson.getString(Jsonkey.IdentityID.getKey()))) {
                                        Branch.this.linkCache_.clear();
                                        Branch.this.prefHelper_.setIdentityID(respJson.getString(Jsonkey.IdentityID.getKey()));
                                        updateRequestsInQueue = true;
                                    }
                                }
                                if (respJson.has(Jsonkey.DeviceFingerprintID.getKey())) {
                                    Branch.this.prefHelper_.setDeviceFingerPrintID(respJson.getString(Jsonkey.DeviceFingerprintID.getKey()));
                                    updateRequestsInQueue = true;
                                }
                                if (updateRequestsInQueue) {
                                    Branch.this.updateAllRequestsInQueue();
                                }
                                if (this.thisReq_ instanceof ServerRequestInitSession) {
                                    Branch.this.initState_ = SESSION_STATE.INITIALISED;
                                    this.thisReq_.onRequestSucceeded(serverResponse, Branch.branchReferral_);
                                    Branch.this.isInitReportedThroughCallBack = ((ServerRequestInitSession) this.thisReq_).hasCallBack();
                                    if (!((ServerRequestInitSession) this.thisReq_).handleBranchViewIfAvailable(serverResponse)) {
                                        Branch.this.checkForAutoDeepLinkConfiguration();
                                    }
                                    if (Branch.this.getLatestReferringParamsLatch != null) {
                                        Branch.this.getLatestReferringParamsLatch.countDown();
                                    }
                                    if (Branch.this.getFirstReferringParamsLatch != null) {
                                        Branch.this.getFirstReferringParamsLatch.countDown();
                                    }
                                } else {
                                    this.thisReq_.onRequestSucceeded(serverResponse, Branch.branchReferral_);
                                }
                            }
                        } else {
                            this.thisReq_.onRequestSucceeded(serverResponse, Branch.branchReferral_);
                        }
                    }
                    Branch.this.networkCount_ = Branch.REFERRAL_CODE_LOCATION_REFERREE;
                    if (Branch.this.hasNetwork_ && Branch.this.initState_ != SESSION_STATE.UNINITIALISED) {
                        Branch.this.processNextQueueItem();
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public interface BranchReferralInitListener {
        void onInitFinished(JSONObject jSONObject, BranchError branchError);
    }

    public interface BranchReferralStateChangedListener {
        void onStateChanged(boolean z, BranchError branchError);
    }

    public interface BranchUniversalReferralInitListener {
        void onInitFinished(BranchUniversalObject branchUniversalObject, LinkProperties linkProperties, BranchError branchError);
    }

    private enum CUSTOM_REFERRABLE_SETTINGS {
        USE_DEFAULT,
        REFERRABLE,
        NON_REFERRABLE
    }

    public enum CreditHistoryOrder {
        kMostRecentFirst,
        kLeastRecentFirst
    }

    public interface IBranchViewControl {
        boolean skipBranchViewsOnThisActivity();
    }

    public interface IChannelProperties {
        String getSharingMessageForChannel(String str);

        String getSharingTitleForChannel(String str);
    }

    private enum INTENT_STATE {
        PENDING,
        READY
    }

    public interface LogoutStatusListener {
        void onLogoutFinished(boolean z, BranchError branchError);
    }

    private enum SESSION_STATE {
        INITIALISED,
        INITIALISING,
        UNINITIALISED
    }

    public static class ShareLinkBuilder {
        private final Activity activity_;
        private final Branch branch_;
        private BranchLinkShareListener callback_;
        private IChannelProperties channelPropertiesCallback_;
        private String copyURlText_;
        private Drawable copyUrlIcon_;
        private String defaultURL_;
        private int dividerHeight;
        private List<String> excludeFromShareSheet;
        private List<String> includeInShareSheet;
        private Drawable moreOptionIcon_;
        private String moreOptionText_;
        private ArrayList<SHARE_WITH> preferredOptions_;
        private boolean setFullWidthStyle_;
        private String shareMsg_;
        private String shareSub_;
        private String sharingTitle;
        private View sharingTitleView;
        BranchShortLinkBuilder shortLinkBuilder_;
        private int styleResourceID_;
        private String urlCopiedMessage_;

        public ShareLinkBuilder(Activity activity, JSONObject parameters) {
            this.callback_ = null;
            this.channelPropertiesCallback_ = null;
            this.dividerHeight = -1;
            this.sharingTitle = null;
            this.sharingTitleView = null;
            this.includeInShareSheet = new ArrayList();
            this.excludeFromShareSheet = new ArrayList();
            this.activity_ = activity;
            this.branch_ = Branch.branchReferral_;
            this.shortLinkBuilder_ = new BranchShortLinkBuilder(activity);
            try {
                Iterator<String> keys = parameters.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    this.shortLinkBuilder_.addParameters(key, (String) parameters.get(key));
                }
            } catch (Exception e) {
            }
            this.shareMsg_ = BuildConfig.FLAVOR;
            this.callback_ = null;
            this.channelPropertiesCallback_ = null;
            this.preferredOptions_ = new ArrayList();
            this.defaultURL_ = null;
            this.moreOptionIcon_ = BranchUtil.getDrawable(activity.getApplicationContext(), 17301573);
            this.moreOptionText_ = "More...";
            this.copyUrlIcon_ = BranchUtil.getDrawable(activity.getApplicationContext(), 17301582);
            this.copyURlText_ = "Copy link";
            this.urlCopiedMessage_ = "Copied link to clipboard!";
        }

        public ShareLinkBuilder(Activity activity, BranchShortLinkBuilder shortLinkBuilder) {
            this(activity, new JSONObject());
            this.shortLinkBuilder_ = shortLinkBuilder;
        }

        public ShareLinkBuilder setMessage(String message) {
            this.shareMsg_ = message;
            return this;
        }

        public ShareLinkBuilder setSubject(String subject) {
            this.shareSub_ = subject;
            return this;
        }

        public ShareLinkBuilder addTag(String tag) {
            this.shortLinkBuilder_.addTag(tag);
            return this;
        }

        public ShareLinkBuilder addTags(ArrayList<String> tags) {
            this.shortLinkBuilder_.addTags(tags);
            return this;
        }

        public ShareLinkBuilder setFeature(String feature) {
            this.shortLinkBuilder_.setFeature(feature);
            return this;
        }

        public ShareLinkBuilder setStage(String stage) {
            this.shortLinkBuilder_.setStage(stage);
            return this;
        }

        public ShareLinkBuilder setCallback(BranchLinkShareListener callback) {
            this.callback_ = callback;
            return this;
        }

        public ShareLinkBuilder setChannelProperties(IChannelProperties channelPropertiesCallback) {
            this.channelPropertiesCallback_ = channelPropertiesCallback;
            return this;
        }

        public ShareLinkBuilder addPreferredSharingOption(SHARE_WITH preferredOption) {
            this.preferredOptions_.add(preferredOption);
            return this;
        }

        public ShareLinkBuilder addPreferredSharingOptions(ArrayList<SHARE_WITH> preferredOptions) {
            this.preferredOptions_.addAll(preferredOptions);
            return this;
        }

        public ShareLinkBuilder addParam(String key, String value) {
            try {
                this.shortLinkBuilder_.addParameters(key, value);
            } catch (Exception e) {
            }
            return this;
        }

        public ShareLinkBuilder setDefaultURL(String url) {
            this.defaultURL_ = url;
            return this;
        }

        public ShareLinkBuilder setMoreOptionStyle(Drawable icon, String label) {
            this.moreOptionIcon_ = icon;
            this.moreOptionText_ = label;
            return this;
        }

        public ShareLinkBuilder setMoreOptionStyle(int drawableIconID, int stringLabelID) {
            this.moreOptionIcon_ = BranchUtil.getDrawable(this.activity_.getApplicationContext(), drawableIconID);
            this.moreOptionText_ = this.activity_.getResources().getString(stringLabelID);
            return this;
        }

        public ShareLinkBuilder setCopyUrlStyle(Drawable icon, String label, String message) {
            this.copyUrlIcon_ = icon;
            this.copyURlText_ = label;
            this.urlCopiedMessage_ = message;
            return this;
        }

        public ShareLinkBuilder setCopyUrlStyle(int drawableIconID, int stringLabelID, int stringMessageID) {
            this.copyUrlIcon_ = BranchUtil.getDrawable(this.activity_.getApplicationContext(), drawableIconID);
            this.copyURlText_ = this.activity_.getResources().getString(stringLabelID);
            this.urlCopiedMessage_ = this.activity_.getResources().getString(stringMessageID);
            return this;
        }

        public ShareLinkBuilder setAlias(String alias) {
            this.shortLinkBuilder_.setAlias(alias);
            return this;
        }

        public ShareLinkBuilder setMatchDuration(int matchDuration) {
            this.shortLinkBuilder_.setDuration(matchDuration);
            return this;
        }

        public ShareLinkBuilder setAsFullWidthStyle(boolean setFullWidthStyle) {
            this.setFullWidthStyle_ = setFullWidthStyle;
            return this;
        }

        public ShareLinkBuilder setDividerHeight(int height) {
            this.dividerHeight = height;
            return this;
        }

        public ShareLinkBuilder setSharingTitle(String title) {
            this.sharingTitle = title;
            return this;
        }

        public ShareLinkBuilder setSharingTitle(View titleView) {
            this.sharingTitleView = titleView;
            return this;
        }

        public ShareLinkBuilder excludeFromShareSheet(@NonNull String packageName) {
            this.excludeFromShareSheet.add(packageName);
            return this;
        }

        public ShareLinkBuilder excludeFromShareSheet(@NonNull String[] packageName) {
            this.excludeFromShareSheet.addAll(Arrays.asList(packageName));
            return this;
        }

        public ShareLinkBuilder excludeFromShareSheet(@NonNull List<String> packageNames) {
            this.excludeFromShareSheet.addAll(packageNames);
            return this;
        }

        public ShareLinkBuilder includeInShareSheet(@NonNull String packageName) {
            this.includeInShareSheet.add(packageName);
            return this;
        }

        public ShareLinkBuilder includeInShareSheet(@NonNull String[] packageName) {
            this.includeInShareSheet.addAll(Arrays.asList(packageName));
            return this;
        }

        public ShareLinkBuilder includeInShareSheet(@NonNull List<String> packageNames) {
            this.includeInShareSheet.addAll(packageNames);
            return this;
        }

        public void setStyleResourceID(@StyleRes int resourceID) {
            this.styleResourceID_ = resourceID;
        }

        public void setShortLinkBuilderInternal(BranchShortLinkBuilder shortLinkBuilder) {
            this.shortLinkBuilder_ = shortLinkBuilder;
        }

        public void shareLink() {
            Branch.branchReferral_.shareLink(this);
        }

        public Activity getActivity() {
            return this.activity_;
        }

        public ArrayList<SHARE_WITH> getPreferredOptions() {
            return this.preferredOptions_;
        }

        List<String> getExcludedFromShareSheet() {
            return this.excludeFromShareSheet;
        }

        List<String> getIncludedInShareSheet() {
            return this.includeInShareSheet;
        }

        public Branch getBranch() {
            return this.branch_;
        }

        public String getShareMsg() {
            return this.shareMsg_;
        }

        public String getShareSub() {
            return this.shareSub_;
        }

        public BranchLinkShareListener getCallback() {
            return this.callback_;
        }

        public IChannelProperties getChannelPropertiesCallback() {
            return this.channelPropertiesCallback_;
        }

        public String getDefaultURL() {
            return this.defaultURL_;
        }

        public Drawable getMoreOptionIcon() {
            return this.moreOptionIcon_;
        }

        public String getMoreOptionText() {
            return this.moreOptionText_;
        }

        public Drawable getCopyUrlIcon() {
            return this.copyUrlIcon_;
        }

        public String getCopyURlText() {
            return this.copyURlText_;
        }

        public String getUrlCopiedMessage() {
            return this.urlCopiedMessage_;
        }

        public BranchShortLinkBuilder getShortLinkBuilder() {
            return this.shortLinkBuilder_;
        }

        public boolean getIsFullWidthStyle() {
            return this.setFullWidthStyle_;
        }

        public int getDividerHeight() {
            return this.dividerHeight;
        }

        public String getSharingTitle() {
            return this.sharingTitle;
        }

        public View getSharingTitleView() {
            return this.sharingTitleView;
        }

        public int getStyleResourceID() {
            return this.styleResourceID_;
        }
    }

    private class getShortLinkTask extends AsyncTask<ServerRequest, Void, ServerResponse> {
        private getShortLinkTask() {
        }

        protected ServerResponse doInBackground(ServerRequest... serverRequests) {
            return Branch.this.kRemoteInterface_.createCustomUrlSync(serverRequests[Branch.REFERRAL_CODE_LOCATION_REFERREE].getPost());
        }
    }

    static {
        String[] strArr = new String[REFERRAL_CODE_AWARD_UNLIMITED];
        strArr[REFERRAL_CODE_LOCATION_REFERREE] = "extra_launch_uri";
        EXTERNAL_INTENT_EXTRA_KEY_WHITE_LIST = strArr;
    }

    private Branch(@NonNull Context context) {
        this.prefHelper_ = PrefHelper.getInstance(context);
        this.kRemoteInterface_ = new BranchRemoteInterface(context);
        this.systemObserver_ = new SystemObserver(context);
        this.requestQueue_ = ServerRequestQueue.getInstance(context);
        this.serverSema_ = new Semaphore(REFERRAL_CODE_AWARD_UNLIMITED);
        this.lock = new Object();
        this.networkCount_ = REFERRAL_CODE_LOCATION_REFERREE;
        this.hasNetwork_ = true;
        this.linkCache_ = new HashMap();
        this.instrumentationExtraData_ = new ConcurrentHashMap();
        this.isGAParamsFetchInProgress_ = this.systemObserver_.prefetchGAdsParams(this);
        InstallListener.setListener(this);
        if (VERSION.SDK_INT >= 15) {
            this.handleDelayedNewIntents_ = true;
            this.intentState_ = INTENT_STATE.PENDING;
        } else {
            this.handleDelayedNewIntents_ = false;
            this.intentState_ = INTENT_STATE.READY;
        }
        this.externalUriWhiteList_ = new ArrayList();
        this.skipExternalUriHosts_ = new ArrayList();
    }

    public static void enableTestMode() {
        BranchUtil.isCustomDebugEnabled_ = true;
    }

    public static void disableTestMode() {
        BranchUtil.isCustomDebugEnabled_ = false;
    }

    public void setDebug() {
        enableTestMode();
    }

    public static void enablePlayStoreReferrer(long delay) {
        checkInstallReferrer_ = true;
        PLAYSTORE_REFERRAL_FETCH_WAIT_FOR = delay;
    }

    static boolean checkPlayStoreReferrer() {
        return checkInstallReferrer_;
    }

    public static long getReferralFetchWaitTime() {
        return PLAYSTORE_REFERRAL_FETCH_WAIT_FOR;
    }

    @TargetApi(14)
    public static Branch getInstance() {
        if (branchReferral_ == null) {
            Log.e(TAG, "Branch instance is not created yet. Make sure you have initialised Branch. [Consider Calling getInstance(Context ctx) if you still have issue.]");
        } else if (isAutoSessionMode_ && !isActivityLifeCycleCallbackRegistered_) {
            Log.e(TAG, "Branch instance is not properly initialised. Make sure your Application class is extending BranchApp class. If you are not extending BranchApp class make sure you are initialising Branch in your Applications onCreate()");
        }
        return branchReferral_;
    }

    public static Branch getInstance(@NonNull Context context, @NonNull String branchKey) {
        if (branchReferral_ == null) {
            branchReferral_ = initInstance(context);
        }
        branchReferral_.context_ = context.getApplicationContext();
        if (!branchKey.startsWith("key_")) {
            Log.e(TAG, "Branch Key is invalid.Please check your BranchKey");
        } else if (branchReferral_.prefHelper_.setBranchKey(branchKey)) {
            branchReferral_.linkCache_.clear();
            branchReferral_.requestQueue_.clear();
        }
        return branchReferral_;
    }

    private static Branch getBranchInstance(@NonNull Context context, boolean isLive) {
        if (branchReferral_ == null) {
            boolean isNewBranchKeySet;
            branchReferral_ = initInstance(context);
            String branchKey = branchReferral_.prefHelper_.readBranchKey(isLive);
            if (branchKey == null || branchKey.equalsIgnoreCase(SystemObserver.BLANK)) {
                String fabricBranchApiKey = null;
                try {
                    Resources resources = context.getResources();
                    fabricBranchApiKey = resources.getString(resources.getIdentifier(FABRIC_BRANCH_API_KEY, "string", context.getPackageName()));
                } catch (Exception e) {
                }
                if (TextUtils.isEmpty(fabricBranchApiKey)) {
                    Log.i(TAG, "Branch Warning: Please enter your branch_key in your project's Manifest file!");
                    isNewBranchKeySet = branchReferral_.prefHelper_.setBranchKey(SystemObserver.BLANK);
                } else {
                    isNewBranchKeySet = branchReferral_.prefHelper_.setBranchKey(fabricBranchApiKey);
                }
            } else {
                isNewBranchKeySet = branchReferral_.prefHelper_.setBranchKey(branchKey);
            }
            if (isNewBranchKeySet) {
                branchReferral_.linkCache_.clear();
                branchReferral_.requestQueue_.clear();
            }
            branchReferral_.context_ = context.getApplicationContext();
            if (context instanceof Application) {
                isAutoSessionMode_ = true;
                branchReferral_.setActivityLifeCycleObserver((Application) context);
            }
        }
        return branchReferral_;
    }

    public static Branch getInstance(@NonNull Context context) {
        return getBranchInstance(context, true);
    }

    public static Branch getTestInstance(@NonNull Context context) {
        return getBranchInstance(context, false);
    }

    @TargetApi(14)
    public static Branch getAutoInstance(@NonNull Context context) {
        boolean isLive = true;
        isAutoSessionMode_ = true;
        customReferrableSettings_ = CUSTOM_REFERRABLE_SETTINGS.USE_DEFAULT;
        if (BranchUtil.isTestModeEnabled(context)) {
            isLive = false;
        }
        getBranchInstance(context, isLive);
        return branchReferral_;
    }

    @TargetApi(14)
    public static Branch getAutoInstance(@NonNull Context context, boolean isReferrable) {
        isAutoSessionMode_ = true;
        customReferrableSettings_ = isReferrable ? CUSTOM_REFERRABLE_SETTINGS.REFERRABLE : CUSTOM_REFERRABLE_SETTINGS.NON_REFERRABLE;
        getBranchInstance(context, !BranchUtil.isTestModeEnabled(context));
        return branchReferral_;
    }

    @TargetApi(14)
    public static Branch getAutoInstance(@NonNull Context context, @NonNull String branchKey) {
        boolean isLive = true;
        isAutoSessionMode_ = true;
        customReferrableSettings_ = CUSTOM_REFERRABLE_SETTINGS.USE_DEFAULT;
        if (BranchUtil.isTestModeEnabled(context)) {
            isLive = false;
        }
        getBranchInstance(context, isLive);
        if (!branchKey.startsWith("key_")) {
            Log.e(TAG, "Branch Key is invalid.Please check your BranchKey");
        } else if (branchReferral_.prefHelper_.setBranchKey(branchKey)) {
            branchReferral_.linkCache_.clear();
            branchReferral_.requestQueue_.clear();
        }
        return branchReferral_;
    }

    @TargetApi(14)
    public static Branch getAutoTestInstance(@NonNull Context context) {
        isAutoSessionMode_ = true;
        customReferrableSettings_ = CUSTOM_REFERRABLE_SETTINGS.USE_DEFAULT;
        getBranchInstance(context, false);
        return branchReferral_;
    }

    @TargetApi(14)
    public static Branch getAutoTestInstance(@NonNull Context context, boolean isReferrable) {
        isAutoSessionMode_ = true;
        customReferrableSettings_ = isReferrable ? CUSTOM_REFERRABLE_SETTINGS.REFERRABLE : CUSTOM_REFERRABLE_SETTINGS.NON_REFERRABLE;
        getBranchInstance(context, false);
        return branchReferral_;
    }

    private static Branch initInstance(@NonNull Context context) {
        return new Branch(context.getApplicationContext());
    }

    public void resetUserSession() {
        this.initState_ = SESSION_STATE.UNINITIALISED;
    }

    public void setRetryCount(int retryCount) {
        if (this.prefHelper_ != null && retryCount >= 0) {
            this.prefHelper_.setRetryCount(retryCount);
        }
    }

    public void setRetryInterval(int retryInterval) {
        if (this.prefHelper_ != null && retryInterval > 0) {
            this.prefHelper_.setRetryInterval(retryInterval);
        }
    }

    public void setNetworkTimeout(int timeout) {
        if (this.prefHelper_ != null && timeout > 0) {
            this.prefHelper_.setTimeout(timeout);
        }
    }

    public static void disableDeviceIDFetch(Boolean deviceIdFetch) {
        disableDeviceIDFetch_ = deviceIdFetch.booleanValue();
    }

    public static boolean isDeviceIDFetchDisabled() {
        return disableDeviceIDFetch_;
    }

    public void setDeepLinkDebugMode(JSONObject debugParams) {
        this.deeplinkDebugParams_ = debugParams;
    }

    public void disableAppList() {
        this.prefHelper_.disableExternAppListing();
    }

    public void enableFacebookAppLinkCheck() {
        this.enableFacebookAppLinkCheck_ = true;
    }

    public void setRequestMetadata(@NonNull String key, @NonNull String value) {
        this.prefHelper_.setRequestMetadata(key, value);
    }

    public boolean initSession(BranchUniversalReferralInitListener callback) {
        return initSession(callback, (Activity) null);
    }

    public boolean initSession(BranchReferralInitListener callback) {
        return initSession(callback, (Activity) null);
    }

    public boolean initSession(BranchUniversalReferralInitListener callback, Activity activity) {
        if (customReferrableSettings_ == CUSTOM_REFERRABLE_SETTINGS.USE_DEFAULT) {
            initUserSessionInternal(callback, activity, true);
        } else {
            initUserSessionInternal(callback, activity, customReferrableSettings_ == CUSTOM_REFERRABLE_SETTINGS.REFERRABLE);
        }
        return true;
    }

    public boolean initSession(BranchReferralInitListener callback, Activity activity) {
        if (customReferrableSettings_ == CUSTOM_REFERRABLE_SETTINGS.USE_DEFAULT) {
            initUserSessionInternal(callback, activity, true);
        } else {
            initUserSessionInternal(callback, activity, customReferrableSettings_ == CUSTOM_REFERRABLE_SETTINGS.REFERRABLE);
        }
        return true;
    }

    public boolean initSession(BranchUniversalReferralInitListener callback, @NonNull Uri data) {
        return initSession(callback, data, null);
    }

    public boolean initSession(BranchReferralInitListener callback, @NonNull Uri data) {
        return initSession(callback, data, null);
    }

    public boolean initSession(BranchUniversalReferralInitListener callback, @NonNull Uri data, Activity activity) {
        readAndStripParam(data, activity);
        initSession(callback, activity);
        return true;
    }

    public boolean initSession(BranchReferralInitListener callback, @NonNull Uri data, Activity activity) {
        readAndStripParam(data, activity);
        return initSession(callback, activity);
    }

    public boolean initSession() {
        return initSession((Activity) null);
    }

    public boolean initSession(Activity activity) {
        return initSession((BranchReferralInitListener) null, activity);
    }

    public boolean initSessionWithData(@NonNull Uri data) {
        return initSessionWithData(data, null);
    }

    public boolean initSessionWithData(Uri data, Activity activity) {
        readAndStripParam(data, activity);
        return initSession((BranchReferralInitListener) null, activity);
    }

    public boolean initSession(boolean isReferrable) {
        return initSession((BranchReferralInitListener) null, isReferrable, (Activity) REFERRAL_CODE_LOCATION_REFERREE);
    }

    public boolean initSession(boolean isReferrable, @NonNull Activity activity) {
        return initSession((BranchReferralInitListener) null, isReferrable, activity);
    }

    public boolean initSession(BranchUniversalReferralInitListener callback, boolean isReferrable, Uri data) {
        return initSession(callback, isReferrable, data, null);
    }

    public boolean initSession(BranchReferralInitListener callback, boolean isReferrable, @NonNull Uri data) {
        return initSession(callback, isReferrable, data, null);
    }

    public boolean initSession(BranchUniversalReferralInitListener callback, boolean isReferrable, @NonNull Uri data, Activity activity) {
        readAndStripParam(data, activity);
        return initSession(callback, isReferrable, activity);
    }

    public boolean initSession(BranchReferralInitListener callback, boolean isReferrable, @NonNull Uri data, Activity activity) {
        readAndStripParam(data, activity);
        return initSession(callback, isReferrable, activity);
    }

    public boolean initSession(BranchUniversalReferralInitListener callback, boolean isReferrable) {
        return initSession(callback, isReferrable, (Activity) null);
    }

    public boolean initSession(BranchReferralInitListener callback, boolean isReferrable) {
        return initSession(callback, isReferrable, (Activity) null);
    }

    public boolean initSession(BranchUniversalReferralInitListener callback, boolean isReferrable, Activity activity) {
        initUserSessionInternal(callback, activity, isReferrable);
        return true;
    }

    public boolean initSession(BranchReferralInitListener callback, boolean isReferrable, Activity activity) {
        initUserSessionInternal(callback, activity, isReferrable);
        return true;
    }

    private void initUserSessionInternal(BranchUniversalReferralInitListener callback, Activity activity, boolean isReferrable) {
        initUserSessionInternal(new BranchUniversalReferralInitWrapper(callback), activity, isReferrable);
    }

    private void initUserSessionInternal(BranchReferralInitListener callback, Activity activity, boolean isReferrable) {
        if (activity != null) {
            this.currentActivityReference_ = new WeakReference(activity);
        }
        if (!hasUser() || !hasSession() || this.initState_ != SESSION_STATE.INITIALISED) {
            if (isReferrable) {
                this.prefHelper_.setIsReferrable();
            } else {
                this.prefHelper_.clearIsReferrable();
            }
            if (this.initState_ != SESSION_STATE.INITIALISING) {
                this.initState_ = SESSION_STATE.INITIALISING;
                initializeSession(callback);
            } else if (callback != null) {
                this.requestQueue_.setInstallOrOpenCallback(callback);
            }
        } else if (callback == null) {
        } else {
            if (!isAutoSessionMode_) {
                callback.onInitFinished(new JSONObject(), null);
            } else if (this.isInitReportedThroughCallBack) {
                callback.onInitFinished(new JSONObject(), null);
            } else {
                callback.onInitFinished(getLatestReferringParams(), null);
                this.isInitReportedThroughCallBack = true;
            }
        }
    }

    public void closeSession() {
        Log.w(TAG, "closeSession() method is deprecated from SDK v1.14.6.Session is  automatically handled by Branch.In case you need to handle sessions manually inorder to support minimum sdk version less than 14 please consider using  SDK version 1.14.5");
    }

    private void closeSessionInternal() {
        executeClose();
        this.sessionReferredLink_ = null;
        if (this.prefHelper_.getExternAppListing() && this.appListingSchedule_ == null) {
            scheduleListOfApps();
        }
    }

    public static void enableCookieBasedMatching(String cookieMatchDomain) {
        cookieBasedMatchDomain_ = cookieMatchDomain;
    }

    private void executeClose() {
        if (this.initState_ != SESSION_STATE.UNINITIALISED) {
            if (!this.hasNetwork_) {
                ServerRequest req = this.requestQueue_.peek();
                if ((req != null && (req instanceof ServerRequestRegisterInstall)) || (req instanceof ServerRequestRegisterOpen)) {
                    this.requestQueue_.dequeue();
                }
            } else if (!this.requestQueue_.containsClose()) {
                handleNewRequest(new ServerRequestRegisterClose(this.context_));
            }
            this.initState_ = SESSION_STATE.UNINITIALISED;
        }
    }

    private boolean readAndStripParam(Uri data, Activity activity) {
        if (this.intentState_ == INTENT_STATE.READY) {
            if (data != null) {
                boolean skipThisHost = false;
                try {
                    boolean foundSchemeMatch;
                    if (this.externalUriWhiteList_.size() > 0) {
                        foundSchemeMatch = this.externalUriWhiteList_.contains(data.getScheme());
                    } else {
                        foundSchemeMatch = true;
                    }
                    if (this.skipExternalUriHosts_.size() > 0) {
                        for (String host : this.skipExternalUriHosts_) {
                            String externalHost = data.getHost();
                            if (externalHost != null && externalHost.equals(host)) {
                                skipThisHost = true;
                                break;
                            }
                        }
                    }
                    if (foundSchemeMatch && !skipThisHost) {
                        this.sessionReferredLink_ = data.toString();
                        this.prefHelper_.setExternalIntentUri(data.toString());
                        if (!(activity == null || activity.getIntent() == null || activity.getIntent().getExtras() == null)) {
                            Bundle bundle = activity.getIntent().getExtras();
                            Set<String> extraKeys = bundle.keySet();
                            if (extraKeys.size() > 0) {
                                JSONObject extrasJson = new JSONObject();
                                String[] arr$ = EXTERNAL_INTENT_EXTRA_KEY_WHITE_LIST;
                                int len$ = arr$.length;
                                for (int i$ = REFERRAL_CODE_LOCATION_REFERREE; i$ < len$; i$ += REFERRAL_CODE_AWARD_UNLIMITED) {
                                    String key = arr$[i$];
                                    if (extraKeys.contains(key)) {
                                        extrasJson.put(key, bundle.get(key));
                                    }
                                }
                                if (extrasJson.length() > 0) {
                                    this.prefHelper_.setExternalIntentExtra(extrasJson.toString());
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                }
            }
            if (activity != null) {
                try {
                    if (!(activity.getIntent() == null || activity.getIntent().getExtras() == null || activity.getIntent().getExtras().getBoolean(Jsonkey.BranchLinkUsed.getKey()))) {
                        String pushIdentifier = activity.getIntent().getExtras().getString(Jsonkey.AndroidPushNotificationKey.getKey());
                        if (pushIdentifier != null && pushIdentifier.length() > 0) {
                            this.prefHelper_.setPushIdentifier(pushIdentifier);
                            Intent thisIntent = activity.getIntent();
                            thisIntent.putExtra(Jsonkey.BranchLinkUsed.getKey(), true);
                            activity.setIntent(thisIntent);
                            return false;
                        }
                    }
                } catch (Exception e2) {
                }
            }
            if (!(data == null || !data.isHierarchical() || activity == null)) {
                try {
                    String uriString;
                    if (data.getQueryParameter(Jsonkey.LinkClickID.getKey()) != null) {
                        this.prefHelper_.setLinkClickIdentifier(data.getQueryParameter(Jsonkey.LinkClickID.getKey()));
                        String paramString = "link_click_id=" + data.getQueryParameter(Jsonkey.LinkClickID.getKey());
                        uriString = null;
                        if (activity.getIntent() != null) {
                            uriString = activity.getIntent().getDataString();
                        }
                        if (data.getQuery().length() == paramString.length()) {
                            paramString = "\\?" + paramString;
                        } else if (uriString == null || uriString.length() - paramString.length() != uriString.indexOf(paramString)) {
                            paramString = paramString + RequestParameters.AMPERSAND;
                        } else {
                            paramString = RequestParameters.AMPERSAND + paramString;
                        }
                        if (uriString != null) {
                            activity.getIntent().setData(Uri.parse(uriString.replaceFirst(paramString, BuildConfig.FLAVOR)));
                        } else {
                            Log.w(TAG, "Branch Warning. URI for the launcher activity is null. Please make sure that intent data is not set to null before calling Branch#InitSession ");
                        }
                        return true;
                    }
                    String scheme = data.getScheme();
                    if (scheme != null && activity.getIntent() != null && (activity.getIntent().getFlags() & 1048576) == 0 && ((scheme.equalsIgnoreCase(HttpHost.DEFAULT_SCHEME_NAME) || scheme.equalsIgnoreCase("https")) && data.getHost() != null && data.getHost().length() > 0 && data.getQueryParameter(Jsonkey.BranchLinkUsed.getKey()) == null)) {
                        this.prefHelper_.setAppLink(data.toString());
                        uriString = data.toString();
                        activity.getIntent().setData(Uri.parse((uriString + (uriString.contains("?") ? RequestParameters.AMPERSAND : "?")) + Jsonkey.BranchLinkUsed.getKey() + "=true"));
                        return false;
                    }
                } catch (Exception e3) {
                }
            }
        }
        return false;
    }

    public void onGAdsFetchFinished() {
        this.isGAParamsFetchInProgress_ = false;
        this.requestQueue_.unlockProcessWait(PROCESS_WAIT_LOCK.GAID_FETCH_WAIT_LOCK);
        if (this.performCookieBasedStrongMatchingOnGAIDAvailable) {
            performCookieBasedStrongMatch();
            this.performCookieBasedStrongMatchingOnGAIDAvailable = false;
            return;
        }
        processNextQueueItem();
    }

    public void onInstallReferrerEventsFinished() {
        this.requestQueue_.unlockProcessWait(PROCESS_WAIT_LOCK.INSTALL_REFERRER_FETCH_WAIT_LOCK);
        processNextQueueItem();
    }

    public Branch addWhiteListedScheme(String uriScheme) {
        if (uriScheme != null) {
            this.externalUriWhiteList_.add(uriScheme.replace("://", BuildConfig.FLAVOR));
        }
        return this;
    }

    public Branch setWhiteListedSchemes(List<String> uriSchemes) {
        this.externalUriWhiteList_ = uriSchemes;
        return this;
    }

    public Branch addUriHostsToSkip(String hostName) {
        if (!(hostName == null || hostName.equals(BuildConfig.FLAVOR))) {
            this.skipExternalUriHosts_.add(hostName);
        }
        return this;
    }

    public void setIdentity(@NonNull String userId) {
        setIdentity(userId, null);
    }

    public void setIdentity(@NonNull String userId, @Nullable BranchReferralInitListener callback) {
        ServerRequest req = new ServerRequestIdentifyUserRequest(this.context_, callback, userId);
        if (!req.constructError_ && !req.handleErrors(this.context_)) {
            handleNewRequest(req);
        } else if (((ServerRequestIdentifyUserRequest) req).isExistingID()) {
            ((ServerRequestIdentifyUserRequest) req).handleUserExist(branchReferral_);
        }
    }

    public boolean isUserIdentified() {
        return !this.prefHelper_.getIdentity().equals(SystemObserver.BLANK);
    }

    public void logout() {
        logout(null);
    }

    public void logout(LogoutStatusListener callback) {
        ServerRequest req = new ServerRequestLogout(this.context_, callback);
        if (!req.constructError_ && !req.handleErrors(this.context_)) {
            handleNewRequest(req);
        }
    }

    public void loadRewards() {
        loadRewards(null);
    }

    public void loadRewards(BranchReferralStateChangedListener callback) {
        ServerRequest req = new ServerRequestGetRewards(this.context_, callback);
        if (!req.constructError_ && !req.handleErrors(this.context_)) {
            handleNewRequest(req);
        }
    }

    public int getCredits() {
        return this.prefHelper_.getCreditCount();
    }

    public int getCreditsForBucket(String bucket) {
        return this.prefHelper_.getCreditCount(bucket);
    }

    public void redeemRewards(int count) {
        redeemRewards(Jsonkey.DefaultBucket.getKey(), count, null);
    }

    public void redeemRewards(int count, BranchReferralStateChangedListener callback) {
        redeemRewards(Jsonkey.DefaultBucket.getKey(), count, callback);
    }

    public void redeemRewards(@NonNull String bucket, int count) {
        redeemRewards(bucket, count, null);
    }

    public void redeemRewards(@NonNull String bucket, int count, BranchReferralStateChangedListener callback) {
        ServerRequestRedeemRewards req = new ServerRequestRedeemRewards(this.context_, bucket, count, callback);
        if (!req.constructError_ && !req.handleErrors(this.context_)) {
            handleNewRequest(req);
        }
    }

    public void getCreditHistory(BranchListResponseListener callback) {
        getCreditHistory(null, null, 100, CreditHistoryOrder.kMostRecentFirst, callback);
    }

    public void getCreditHistory(@NonNull String bucket, BranchListResponseListener callback) {
        getCreditHistory(bucket, null, 100, CreditHistoryOrder.kMostRecentFirst, callback);
    }

    public void getCreditHistory(@NonNull String afterId, int length, @NonNull CreditHistoryOrder order, BranchListResponseListener callback) {
        getCreditHistory(null, afterId, length, order, callback);
    }

    public void getCreditHistory(String bucket, String afterId, int length, @NonNull CreditHistoryOrder order, BranchListResponseListener callback) {
        ServerRequest req = new ServerRequestGetRewardHistory(this.context_, bucket, afterId, length, order, callback);
        if (!req.constructError_ && !req.handleErrors(this.context_)) {
            handleNewRequest(req);
        }
    }

    public void userCompletedAction(@NonNull String action, JSONObject metadata) {
        userCompletedAction(action, metadata, null);
    }

    public void userCompletedAction(String action) {
        userCompletedAction(action, null, null);
    }

    public void userCompletedAction(String action, IBranchViewEvents callback) {
        userCompletedAction(action, null, callback);
    }

    public void userCompletedAction(@NonNull String action, JSONObject metadata, IBranchViewEvents callback) {
        if (metadata != null) {
            metadata = BranchUtil.filterOutBadCharacters(metadata);
        }
        ServerRequest req = new ServerRequestActionCompleted(this.context_, action, metadata, callback);
        if (!req.constructError_ && !req.handleErrors(this.context_)) {
            handleNewRequest(req);
        }
    }

    public void sendCommerceEvent(@NonNull CommerceEvent commerceEvent, JSONObject metadata, IBranchViewEvents callback) {
        if (metadata != null) {
            metadata = BranchUtil.filterOutBadCharacters(metadata);
        }
        ServerRequest req = new ServerRequestRActionCompleted(this.context_, commerceEvent, metadata, callback);
        if (!req.constructError_ && !req.handleErrors(this.context_)) {
            handleNewRequest(req);
        }
    }

    public void sendCommerceEvent(@NonNull CommerceEvent commerceEvent) {
        sendCommerceEvent(commerceEvent, null, null);
    }

    public JSONObject getFirstReferringParams() {
        return appendDebugParams(convertParamsStringToDictionary(this.prefHelper_.getInstallParams()));
    }

    public JSONObject getFirstReferringParamsSync() {
        this.getFirstReferringParamsLatch = new CountDownLatch(REFERRAL_CODE_AWARD_UNLIMITED);
        if (this.prefHelper_.getInstallParams().equals(SystemObserver.BLANK)) {
            try {
                this.getFirstReferringParamsLatch.await((long) LATCH_WAIT_UNTIL, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
            }
        }
        JSONObject firstReferringParams = appendDebugParams(convertParamsStringToDictionary(this.prefHelper_.getInstallParams()));
        this.getFirstReferringParamsLatch = null;
        return firstReferringParams;
    }

    public JSONObject getLatestReferringParams() {
        return appendDebugParams(convertParamsStringToDictionary(this.prefHelper_.getSessionParams()));
    }

    public JSONObject getLatestReferringParamsSync() {
        this.getLatestReferringParamsLatch = new CountDownLatch(REFERRAL_CODE_AWARD_UNLIMITED);
        try {
            if (this.initState_ != SESSION_STATE.INITIALISED) {
                this.getLatestReferringParamsLatch.await((long) LATCH_WAIT_UNTIL, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
        }
        JSONObject latestParams = appendDebugParams(convertParamsStringToDictionary(this.prefHelper_.getSessionParams()));
        this.getLatestReferringParamsLatch = null;
        return latestParams;
    }

    private JSONObject appendDebugParams(JSONObject originalParams) {
        if (originalParams != null) {
            try {
                if (this.deeplinkDebugParams_ != null) {
                    if (this.deeplinkDebugParams_.length() > 0) {
                        Log.w(TAG, "You're currently in deep link debug mode. Please comment out 'setDeepLinkDebugMode' to receive the deep link parameters from a real Branch link");
                    }
                    Iterator<String> keys = this.deeplinkDebugParams_.keys();
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        originalParams.put(key, this.deeplinkDebugParams_.get(key));
                    }
                }
            } catch (Exception e) {
            }
        }
        return originalParams;
    }

    public JSONObject getDeeplinkDebugParams() {
        if (this.deeplinkDebugParams_ != null && this.deeplinkDebugParams_.length() > 0) {
            Log.w(TAG, "You're currently in deep link debug mode. Please comment out 'setDeepLinkDebugMode' to receive the deep link parameters from a real Branch link");
        }
        return this.deeplinkDebugParams_;
    }

    String generateShortLinkInternal(ServerRequestCreateUrl req) {
        if (!(req.constructError_ || req.handleErrors(this.context_))) {
            if (this.linkCache_.containsKey(req.getLinkPost())) {
                String url = (String) this.linkCache_.get(req.getLinkPost());
                req.onUrlAvailable(url);
                return url;
            } else if (!req.isAsync()) {
                return generateShortLinkSync(req);
            } else {
                generateShortLinkAsync(req);
            }
        }
        return null;
    }

    private void shareLink(ShareLinkBuilder builder) {
        if (this.shareLinkManager_ != null) {
            this.shareLinkManager_.cancelShareLinkDialog(true);
        }
        this.shareLinkManager_ = new ShareLinkManager();
        this.shareLinkManager_.shareLink(builder);
    }

    public void cancelShareLinkDialog(boolean animateClose) {
        if (this.shareLinkManager_ != null) {
            this.shareLinkManager_.cancelShareLinkDialog(animateClose);
        }
    }

    private String convertDate(Date date) {
        return DateFormat.format("yyyy-MM-dd", date).toString();
    }

    private String generateShortLinkSync(ServerRequestCreateUrl req) {
        String url = null;
        if (this.initState_ == SESSION_STATE.INITIALISED) {
            ServerResponse response = null;
            try {
                int timeOut = this.prefHelper_.getTimeout() + SESSION_KEEPALIVE;
                getShortLinkTask io_branch_referral_Branch_getShortLinkTask = new getShortLinkTask();
                ServerRequest[] serverRequestArr = new ServerRequest[REFERRAL_CODE_AWARD_UNLIMITED];
                serverRequestArr[REFERRAL_CODE_LOCATION_REFERREE] = req;
                response = (ServerResponse) io_branch_referral_Branch_getShortLinkTask.execute(serverRequestArr).get((long) timeOut, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
            } catch (ExecutionException e2) {
            } catch (TimeoutException e3) {
            }
            url = null;
            if (req.isDefaultToLongUrl()) {
                url = req.getLongUrl();
            }
            if (response != null && response.getStatusCode() == HttpStatus.SC_OK) {
                try {
                    url = response.getObject().getString(ParametersKeys.URL);
                    if (req.getLinkPost() != null) {
                        this.linkCache_.put(req.getLinkPost(), url);
                    }
                } catch (JSONException e4) {
                    e4.printStackTrace();
                }
            }
        } else {
            Log.i(TAG, "Branch Warning: User session has not been initialized");
        }
        return url;
    }

    private void generateShortLinkAsync(ServerRequest req) {
        handleNewRequest(req);
    }

    private JSONObject convertParamsStringToDictionary(String paramString) {
        if (paramString.equals(SystemObserver.BLANK)) {
            return new JSONObject();
        }
        try {
            return new JSONObject(paramString);
        } catch (JSONException e) {
            try {
                return new JSONObject(new String(Base64.decode(paramString.getBytes(), (int) REFERRAL_CREATION_SOURCE_SDK)));
            } catch (JSONException ex) {
                ex.printStackTrace();
                return new JSONObject();
            }
        }
    }

    private void scheduleListOfApps() {
        ScheduledThreadPoolExecutor scheduler = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(REFERRAL_CODE_AWARD_UNLIMITED);
        Runnable periodicTask = new Runnable() {
            public void run() {
                ServerRequest req = new ServerRequestSendAppList(Branch.this.context_);
                if (!req.constructError_ && !req.handleErrors(Branch.this.context_)) {
                    Branch.this.handleNewRequest(req);
                }
            }
        };
        Date date = new Date();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        int days = 7 - calendar.get(7);
        int hours = 2 - calendar.get(11);
        if (days == 0 && hours < 0) {
            days = 7;
        }
        this.appListingSchedule_ = scheduler.scheduleAtFixedRate(periodicTask, (long) ((((days * 24) + hours) * 60) * 60), (long) 604800, TimeUnit.SECONDS);
    }

    private void processNextQueueItem() {
        try {
            this.serverSema_.acquire();
            if (this.networkCount_ != 0 || this.requestQueue_.getSize() <= 0) {
                this.serverSema_.release();
                return;
            }
            this.networkCount_ = REFERRAL_CODE_AWARD_UNLIMITED;
            ServerRequest req = this.requestQueue_.peek();
            this.serverSema_.release();
            if (req == null) {
                this.requestQueue_.remove(null);
            } else if (req.isWaitingOnProcessToFinish()) {
                this.networkCount_ = REFERRAL_CODE_LOCATION_REFERREE;
            } else if (!(req instanceof ServerRequestRegisterInstall) && !hasUser()) {
                Log.i(TAG, "Branch Error: User session has not been initialized!");
                this.networkCount_ = REFERRAL_CODE_LOCATION_REFERREE;
                handleFailure(this.requestQueue_.getSize() - 1, (int) BranchError.ERR_NO_SESSION);
            } else if ((req instanceof ServerRequestInitSession) || (hasSession() && hasDeviceFingerPrint())) {
                new BranchPostTask(req).executeTask(new Void[REFERRAL_CODE_LOCATION_REFERREE]);
            } else {
                this.networkCount_ = REFERRAL_CODE_LOCATION_REFERREE;
                handleFailure(this.requestQueue_.getSize() - 1, (int) BranchError.ERR_NO_SESSION);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleFailure(int index, int statusCode) {
        ServerRequest req;
        if (index >= this.requestQueue_.getSize()) {
            req = this.requestQueue_.peekAt(this.requestQueue_.getSize() - 1);
        } else {
            req = this.requestQueue_.peekAt(index);
        }
        handleFailure(req, statusCode);
    }

    private void handleFailure(ServerRequest req, int statusCode) {
        if (req != null) {
            req.handleFailure(statusCode, BuildConfig.FLAVOR);
        }
    }

    private void updateAllRequestsInQueue() {
        int i = REFERRAL_CODE_LOCATION_REFERREE;
        while (i < this.requestQueue_.getSize()) {
            try {
                ServerRequest req = this.requestQueue_.peekAt(i);
                if (req != null) {
                    JSONObject reqJson = req.getPost();
                    if (reqJson != null) {
                        if (reqJson.has(Jsonkey.SessionID.getKey())) {
                            req.getPost().put(Jsonkey.SessionID.getKey(), this.prefHelper_.getSessionID());
                        }
                        if (reqJson.has(Jsonkey.IdentityID.getKey())) {
                            req.getPost().put(Jsonkey.IdentityID.getKey(), this.prefHelper_.getIdentityID());
                        }
                        if (reqJson.has(Jsonkey.DeviceFingerprintID.getKey())) {
                            req.getPost().put(Jsonkey.DeviceFingerprintID.getKey(), this.prefHelper_.getDeviceFingerPrintID());
                        }
                    }
                }
                i += REFERRAL_CODE_AWARD_UNLIMITED;
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private boolean hasSession() {
        return !this.prefHelper_.getSessionID().equals(SystemObserver.BLANK);
    }

    private boolean hasDeviceFingerPrint() {
        return !this.prefHelper_.getDeviceFingerPrintID().equals(SystemObserver.BLANK);
    }

    private boolean hasUser() {
        return !this.prefHelper_.getIdentityID().equals(SystemObserver.BLANK);
    }

    private void insertRequestAtFront(ServerRequest req) {
        if (this.networkCount_ == 0) {
            this.requestQueue_.insert(req, REFERRAL_CODE_LOCATION_REFERREE);
        } else {
            this.requestQueue_.insert(req, REFERRAL_CODE_AWARD_UNLIMITED);
        }
    }

    private void registerInstallOrOpen(ServerRequest req, BranchReferralInitListener callback) {
        if (this.requestQueue_.containsInstallOrOpen()) {
            if (callback != null) {
                this.requestQueue_.setInstallOrOpenCallback(callback);
            }
            this.requestQueue_.moveInstallOrOpenToFront(req, this.networkCount_, callback);
        } else {
            insertRequestAtFront(req);
        }
        processNextQueueItem();
    }

    private void initializeSession(BranchReferralInitListener callback) {
        if (this.prefHelper_.getBranchKey() == null || this.prefHelper_.getBranchKey().equalsIgnoreCase(SystemObserver.BLANK)) {
            this.initState_ = SESSION_STATE.UNINITIALISED;
            if (callback != null) {
                callback.onInitFinished(null, new BranchError("Trouble initializing Branch.", RemoteInterface.NO_BRANCH_KEY_STATUS));
            }
            Log.i(TAG, "Branch Warning: Please enter your branch_key in your project's res/values/strings.xml!");
            return;
        }
        if (this.prefHelper_.getBranchKey() != null && this.prefHelper_.getBranchKey().startsWith("key_test_")) {
            Log.i(TAG, "Branch Warning: You are using your test app's Branch Key. Remember to change it to live Branch Key during deployment.");
        }
        if (!this.prefHelper_.getExternalIntentUri().equals(SystemObserver.BLANK) || !this.enableFacebookAppLinkCheck_) {
            registerAppInit(callback, null);
        } else if (DeferredAppLinkDataHandler.fetchDeferredAppLinkData(this.context_, new AppLinkFetchEvents() {
            public void onAppLinkFetchFinished(String nativeAppLinkUrl) {
                Branch.this.prefHelper_.setIsAppLinkTriggeredInit(Boolean.valueOf(true));
                if (nativeAppLinkUrl != null) {
                    String bncLinkClickId = Uri.parse(nativeAppLinkUrl).getQueryParameter(Jsonkey.LinkClickID.getKey());
                    if (!TextUtils.isEmpty(bncLinkClickId)) {
                        Branch.this.prefHelper_.setLinkClickIdentifier(bncLinkClickId);
                    }
                }
                Branch.this.requestQueue_.unlockProcessWait(PROCESS_WAIT_LOCK.FB_APP_LINK_WAIT_LOCK);
                Branch.this.processNextQueueItem();
            }
        }).booleanValue()) {
            registerAppInit(callback, PROCESS_WAIT_LOCK.FB_APP_LINK_WAIT_LOCK);
        } else {
            registerAppInit(callback, null);
        }
    }

    private void registerAppInit(BranchReferralInitListener callback, PROCESS_WAIT_LOCK lock) {
        ServerRequest request;
        if (hasUser()) {
            request = new ServerRequestRegisterOpen(this.context_, callback, this.kRemoteInterface_.getSystemObserver());
        } else {
            request = new ServerRequestRegisterInstall(this.context_, callback, this.kRemoteInterface_.getSystemObserver(), InstallListener.getInstallationID(), InstallListener.getGoogleSearchInstallReferrerID());
        }
        request.addProcessWaitLock(lock);
        if (this.isGAParamsFetchInProgress_) {
            request.addProcessWaitLock(PROCESS_WAIT_LOCK.GAID_FETCH_WAIT_LOCK);
        }
        if (this.intentState_ != INTENT_STATE.READY) {
            request.addProcessWaitLock(PROCESS_WAIT_LOCK.INTENT_PENDING_WAIT_LOCK);
        }
        if (checkPlayStoreReferrer() && (request instanceof ServerRequestRegisterInstall)) {
            request.addProcessWaitLock(PROCESS_WAIT_LOCK.INSTALL_REFERRER_FETCH_WAIT_LOCK);
            InstallListener.startInstallReferrerTime(PLAYSTORE_REFERRAL_FETCH_WAIT_FOR);
        }
        registerInstallOrOpen(request, callback);
    }

    private void onIntentReady(Activity activity) {
        this.requestQueue_.unlockProcessWait(PROCESS_WAIT_LOCK.INTENT_PENDING_WAIT_LOCK);
        if (activity.getIntent() != null) {
            readAndStripParam(activity.getIntent().getData(), activity);
            if (cookieBasedMatchDomain_ == null || this.prefHelper_.getBranchKey() == null || this.prefHelper_.getBranchKey().equalsIgnoreCase(SystemObserver.BLANK)) {
                processNextQueueItem();
                return;
            } else if (this.isGAParamsFetchInProgress_) {
                this.performCookieBasedStrongMatchingOnGAIDAvailable = true;
                return;
            } else {
                performCookieBasedStrongMatch();
                return;
            }
        }
        processNextQueueItem();
    }

    private void performCookieBasedStrongMatch() {
        boolean simulateInstall = this.prefHelper_.getExternDebug() || isSimulatingInstalls();
        DeviceInfo deviceInfo = DeviceInfo.getInstance(simulateInstall, this.systemObserver_, disableDeviceIDFetch_);
        Context context = (this.currentActivityReference_ == null || this.currentActivityReference_.get() == null) ? null : ((Activity) this.currentActivityReference_.get()).getApplicationContext();
        if (context != null) {
            this.requestQueue_.setStrongMatchWaitLock();
            BranchStrongMatchHelper.getInstance().checkForStrongMatch(context, cookieBasedMatchDomain_, deviceInfo, this.prefHelper_, this.systemObserver_, new StrongMatchCheckEvents() {
                public void onStrongMatchCheckFinished() {
                    Branch.this.requestQueue_.unlockProcessWait(PROCESS_WAIT_LOCK.STRONG_MATCH_PENDING_WAIT_LOCK);
                    Branch.this.processNextQueueItem();
                }
            });
        }
    }

    public void handleNewRequest(ServerRequest req) {
        boolean isReferrable = true;
        if (!(this.initState_ == SESSION_STATE.INITIALISED || (req instanceof ServerRequestInitSession))) {
            if (req instanceof ServerRequestLogout) {
                req.handleFailure(BranchError.ERR_NO_SESSION, BuildConfig.FLAVOR);
                Log.i(TAG, "Branch is not initialized, cannot logout");
                return;
            } else if (req instanceof ServerRequestRegisterClose) {
                Log.i(TAG, "Branch is not initialized, cannot close session");
                return;
            } else {
                Activity currentActivity = null;
                if (this.currentActivityReference_ != null) {
                    currentActivity = (Activity) this.currentActivityReference_.get();
                }
                if (customReferrableSettings_ == CUSTOM_REFERRABLE_SETTINGS.USE_DEFAULT) {
                    initUserSessionInternal((BranchReferralInitListener) REFERRAL_CODE_LOCATION_REFERREE, currentActivity, true);
                } else {
                    if (customReferrableSettings_ != CUSTOM_REFERRABLE_SETTINGS.REFERRABLE) {
                        isReferrable = false;
                    }
                    initUserSessionInternal((BranchReferralInitListener) REFERRAL_CODE_LOCATION_REFERREE, currentActivity, isReferrable);
                }
            }
        }
        this.requestQueue_.enqueue(req);
        req.onRequestQueued();
        processNextQueueItem();
    }

    @TargetApi(14)
    private void setActivityLifeCycleObserver(Application application) {
        try {
            BranchActivityLifeCycleObserver activityLifeCycleObserver = new BranchActivityLifeCycleObserver();
            application.unregisterActivityLifecycleCallbacks(activityLifeCycleObserver);
            application.registerActivityLifecycleCallbacks(activityLifeCycleObserver);
            isActivityLifeCycleCallbackRegistered_ = true;
            return;
        } catch (NoSuchMethodError e) {
        } catch (NoClassDefFoundError e2) {
        }
        isActivityLifeCycleCallbackRegistered_ = false;
        isAutoSessionMode_ = false;
        Log.w(TAG, new BranchError(BuildConfig.FLAVOR, BranchError.ERR_API_LVL_14_NEEDED).getMessage());
    }

    private void startSession(Activity activity) {
        Uri intentData = null;
        if (activity.getIntent() != null) {
            intentData = activity.getIntent().getData();
        }
        initSessionWithData(intentData, activity);
    }

    private boolean checkIntentForSessionRestart(Intent intent) {
        boolean isRestartSessionRequested = false;
        if (intent != null) {
            isRestartSessionRequested = intent.getBooleanExtra(Jsonkey.ForceNewBranchSession.getKey(), false);
            if (isRestartSessionRequested) {
                intent.putExtra(Jsonkey.ForceNewBranchSession.getKey(), false);
            }
        }
        return isRestartSessionRequested;
    }

    public static boolean isAutoDeepLinkLaunch(Activity activity) {
        return activity.getIntent().getStringExtra(AUTO_DEEP_LINKED) != null;
    }

    private void checkForAutoDeepLinkConfiguration() {
        JSONObject latestParams = getLatestReferringParams();
        String deepLinkActivity = null;
        try {
            if (latestParams.has(Jsonkey.Clicked_Branch_Link.getKey()) && latestParams.getBoolean(Jsonkey.Clicked_Branch_Link.getKey()) && latestParams.length() > 0) {
                ApplicationInfo appInfo = this.context_.getPackageManager().getApplicationInfo(this.context_.getPackageName(), 128);
                if (appInfo.metaData == null || !appInfo.metaData.getBoolean(AUTO_DEEP_LINK_DISABLE, false)) {
                    ActivityInfo[] activityInfos = this.context_.getPackageManager().getPackageInfo(this.context_.getPackageName(), 129).activities;
                    int deepLinkActivityReqCode = DEF_AUTO_DEEP_LINK_REQ_CODE;
                    if (activityInfos != null) {
                        ActivityInfo[] arr$ = activityInfos;
                        int len$ = arr$.length;
                        for (int i$ = REFERRAL_CODE_LOCATION_REFERREE; i$ < len$; i$ += REFERRAL_CODE_AWARD_UNLIMITED) {
                            ActivityInfo activityInfo = arr$[i$];
                            if (activityInfo != null && activityInfo.metaData != null && ((activityInfo.metaData.getString(AUTO_DEEP_LINK_KEY) != null || activityInfo.metaData.getString(AUTO_DEEP_LINK_PATH) != null) && (checkForAutoDeepLinkKeys(latestParams, activityInfo) || checkForAutoDeepLinkPath(latestParams, activityInfo)))) {
                                deepLinkActivity = activityInfo.name;
                                deepLinkActivityReqCode = activityInfo.metaData.getInt(AUTO_DEEP_LINK_REQ_CODE, DEF_AUTO_DEEP_LINK_REQ_CODE);
                                break;
                            }
                        }
                    }
                    if (deepLinkActivity != null && this.currentActivityReference_ != null) {
                        Activity currentActivity = (Activity) this.currentActivityReference_.get();
                        if (currentActivity != null) {
                            Intent intent = new Intent(currentActivity, Class.forName(deepLinkActivity));
                            intent.putExtra(AUTO_DEEP_LINKED, ServerProtocol.DIALOG_RETURN_SCOPES_TRUE);
                            intent.putExtra(Jsonkey.ReferringData.getKey(), latestParams.toString());
                            Iterator<?> keys = latestParams.keys();
                            while (keys.hasNext()) {
                                String key = (String) keys.next();
                                intent.putExtra(key, latestParams.getString(key));
                            }
                            currentActivity.startActivityForResult(intent, deepLinkActivityReqCode);
                            return;
                        }
                        Log.w(TAG, "No activity reference to launch deep linked activity");
                    }
                }
            }
        } catch (NameNotFoundException e) {
            Log.i(TAG, "Branch Warning: Please make sure Activity names set for auto deep link are correct!");
        } catch (ClassNotFoundException e2) {
            Log.i(TAG, "Branch Warning: Please make sure Activity names set for auto deep link are correct! Error while looking for activity " + deepLinkActivity);
        } catch (Exception e3) {
        }
    }

    private boolean checkForAutoDeepLinkKeys(JSONObject params, ActivityInfo activityInfo) {
        if (activityInfo.metaData.getString(AUTO_DEEP_LINK_KEY) != null) {
            String[] arr$ = activityInfo.metaData.getString(AUTO_DEEP_LINK_KEY).split(",");
            int len$ = arr$.length;
            for (int i$ = REFERRAL_CODE_LOCATION_REFERREE; i$ < len$; i$ += REFERRAL_CODE_AWARD_UNLIMITED) {
                if (params.has(arr$[i$])) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkForAutoDeepLinkPath(JSONObject params, ActivityInfo activityInfo) {
        String deepLinkPath = null;
        try {
            if (params.has(Jsonkey.AndroidDeepLinkPath.getKey())) {
                deepLinkPath = params.getString(Jsonkey.AndroidDeepLinkPath.getKey());
            } else if (params.has(Jsonkey.DeepLinkPath.getKey())) {
                deepLinkPath = params.getString(Jsonkey.DeepLinkPath.getKey());
            }
        } catch (JSONException e) {
        }
        if (!(activityInfo.metaData.getString(AUTO_DEEP_LINK_PATH) == null || deepLinkPath == null)) {
            String[] arr$ = activityInfo.metaData.getString(AUTO_DEEP_LINK_PATH).split(",");
            int len$ = arr$.length;
            for (int i$ = REFERRAL_CODE_LOCATION_REFERREE; i$ < len$; i$ += REFERRAL_CODE_AWARD_UNLIMITED) {
                if (pathMatch(arr$[i$].trim(), deepLinkPath)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean pathMatch(String templatePath, String path) {
        boolean matched = true;
        String[] pathSegmentsTemplate = templatePath.split("\\?")[REFERRAL_CODE_LOCATION_REFERREE].split("/");
        String[] pathSegmentsTarget = path.split("\\?")[REFERRAL_CODE_LOCATION_REFERREE].split("/");
        if (pathSegmentsTemplate.length != pathSegmentsTarget.length) {
            return false;
        }
        int i = REFERRAL_CODE_LOCATION_REFERREE;
        while (i < pathSegmentsTemplate.length && i < pathSegmentsTarget.length) {
            String pathSegmentTemplate = pathSegmentsTemplate[i];
            if (!pathSegmentTemplate.equals(pathSegmentsTarget[i]) && !pathSegmentTemplate.contains("*")) {
                matched = false;
                break;
            }
            i += REFERRAL_CODE_AWARD_UNLIMITED;
        }
        return matched;
    }

    public static void enableSimulateInstalls() {
        isSimulatingInstalls_ = true;
    }

    public static void disableSimulateInstalls() {
        isSimulatingInstalls_ = false;
    }

    public static boolean isSimulatingInstalls() {
        return isSimulatingInstalls_;
    }

    public static void enableLogging() {
        isLogging_ = true;
    }

    public static void disableLogging() {
        isLogging_ = false;
    }

    public static boolean getIsLogging() {
        return isLogging_;
    }

    public void registerView(BranchUniversalObject branchUniversalObject, RegisterViewStatusListener callback) {
        if (this.context_ != null) {
            ServerRequest req = new ServerRequestRegisterView(this.context_, branchUniversalObject, this.systemObserver_, callback);
            if (!req.constructError_ && !req.handleErrors(this.context_)) {
                handleNewRequest(req);
            }
        }
    }

    public void addExtraInstrumentationData(HashMap<String, String> instrumentationData) {
        this.instrumentationExtraData_.putAll(instrumentationData);
    }

    public void addExtraInstrumentationData(String key, String value) {
        this.instrumentationExtraData_.put(key, value);
    }

    public void onBranchViewVisible(String action, String branchViewID) {
    }

    public void onBranchViewAccepted(String action, String branchViewID) {
        if (ServerRequestInitSession.isInitSessionAction(action)) {
            checkForAutoDeepLinkConfiguration();
        }
    }

    public void onBranchViewCancelled(String action, String branchViewID) {
        if (ServerRequestInitSession.isInitSessionAction(action)) {
            checkForAutoDeepLinkConfiguration();
        }
    }

    public void onBranchViewError(int errorCode, String errorMsg, String action) {
        if (ServerRequestInitSession.isInitSessionAction(action)) {
            checkForAutoDeepLinkConfiguration();
        }
    }
}
