package io.branch.referral;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import io.branch.referral.Defines.Jsonkey;
import io.branch.referral.Defines.RequestPath;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.cocos2dx.lib.BuildConfig;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class ServerRequest {
    private static final String POST_KEY = "REQ_POST";
    private static final String POST_PATH_KEY = "REQ_POST_PATH";
    public boolean constructError_ = false;
    private boolean disableAndroidIDFetch_;
    final Set<PROCESS_WAIT_LOCK> locks_;
    private JSONObject params_;
    protected PrefHelper prefHelper_;
    long queueWaitTime_ = 0;
    protected String requestPath_;
    boolean skipOnTimeOut = false;
    private final SystemObserver systemObserver_;
    private int waitLockCnt = 0;

    enum PROCESS_WAIT_LOCK {
        FB_APP_LINK_WAIT_LOCK,
        GAID_FETCH_WAIT_LOCK,
        INTENT_PENDING_WAIT_LOCK,
        STRONG_MATCH_PENDING_WAIT_LOCK,
        INSTALL_REFERRER_FETCH_WAIT_LOCK
    }

    public abstract void clearCallbacks();

    public abstract boolean handleErrors(Context context);

    public abstract void handleFailure(int i, String str);

    public abstract boolean isGetRequest();

    public abstract void onRequestSucceeded(ServerResponse serverResponse, Branch branch);

    public ServerRequest(Context context, String requestPath) {
        this.requestPath_ = requestPath;
        this.prefHelper_ = PrefHelper.getInstance(context);
        this.systemObserver_ = new SystemObserver(context);
        this.params_ = new JSONObject();
        this.disableAndroidIDFetch_ = Branch.isDeviceIDFetchDisabled();
        this.locks_ = new HashSet();
    }

    protected ServerRequest(String requestPath, JSONObject post, Context context) {
        this.requestPath_ = requestPath;
        this.params_ = post;
        this.prefHelper_ = PrefHelper.getInstance(context);
        this.systemObserver_ = new SystemObserver(context);
        this.disableAndroidIDFetch_ = Branch.isDeviceIDFetchDisabled();
        this.locks_ = new HashSet();
    }

    public boolean shouldRetryOnFail() {
        return false;
    }

    public final String getRequestPath() {
        return this.requestPath_;
    }

    public String getRequestUrl() {
        return this.prefHelper_.getAPIBaseUrl() + this.requestPath_;
    }

    protected void setPost(JSONObject post) {
        try {
            JSONObject metadata = new JSONObject();
            Iterator<String> i = this.prefHelper_.getRequestMetadata().keys();
            while (i.hasNext()) {
                String k = (String) i.next();
                metadata.put(k, this.prefHelper_.getRequestMetadata().get(k));
            }
            if (post.has(Jsonkey.Metadata.getKey())) {
                Iterator<String> postIter = post.getJSONObject(Jsonkey.Metadata.getKey()).keys();
                while (postIter.hasNext()) {
                    String key = (String) postIter.next();
                    metadata.put(key, post.getJSONObject(Jsonkey.Metadata.getKey()).get(key));
                }
            }
            post.put(Jsonkey.Metadata.getKey(), metadata);
        } catch (JSONException e) {
            Log.e("BranchSDK", "Could not merge metadata, ignoring user metadata.");
        }
        this.params_ = post;
        DeviceInfo.getInstance(this.prefHelper_.getExternDebug(), this.systemObserver_, this.disableAndroidIDFetch_).updateRequestWithDeviceParams(this.params_);
    }

    public JSONObject getPost() {
        return this.params_;
    }

    public boolean isGAdsParamsRequired() {
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public org.json.JSONObject getPostWithInstrumentationValues(java.util.concurrent.ConcurrentHashMap<java.lang.String, java.lang.String> r10) {
        /*
        r9 = this;
        r1 = new org.json.JSONObject;
        r1.<init>();
        r8 = r9.params_;	 Catch:{ JSONException -> 0x002c, ConcurrentModificationException -> 0x0064 }
        if (r8 == 0) goto L_0x002e;
    L_0x0009:
        r7 = new org.json.JSONObject;	 Catch:{ JSONException -> 0x002c, ConcurrentModificationException -> 0x0064 }
        r8 = r9.params_;	 Catch:{ JSONException -> 0x002c, ConcurrentModificationException -> 0x0064 }
        r8 = r8.toString();	 Catch:{ JSONException -> 0x002c, ConcurrentModificationException -> 0x0064 }
        r7.<init>(r8);	 Catch:{ JSONException -> 0x002c, ConcurrentModificationException -> 0x0064 }
        r5 = r7.keys();	 Catch:{ JSONException -> 0x002c, ConcurrentModificationException -> 0x0064 }
    L_0x0018:
        r8 = r5.hasNext();	 Catch:{ JSONException -> 0x002c, ConcurrentModificationException -> 0x0064 }
        if (r8 == 0) goto L_0x002e;
    L_0x001e:
        r4 = r5.next();	 Catch:{ JSONException -> 0x002c, ConcurrentModificationException -> 0x0064 }
        r4 = (java.lang.String) r4;	 Catch:{ JSONException -> 0x002c, ConcurrentModificationException -> 0x0064 }
        r8 = r7.get(r4);	 Catch:{ JSONException -> 0x002c, ConcurrentModificationException -> 0x0064 }
        r1.put(r4, r8);	 Catch:{ JSONException -> 0x002c, ConcurrentModificationException -> 0x0064 }
        goto L_0x0018;
    L_0x002c:
        r8 = move-exception;
    L_0x002d:
        return r1;
    L_0x002e:
        r8 = r10.size();	 Catch:{ JSONException -> 0x002c, ConcurrentModificationException -> 0x0064 }
        if (r8 <= 0) goto L_0x002d;
    L_0x0034:
        r3 = new org.json.JSONObject;	 Catch:{ JSONException -> 0x002c, ConcurrentModificationException -> 0x0064 }
        r3.<init>();	 Catch:{ JSONException -> 0x002c, ConcurrentModificationException -> 0x0064 }
        r6 = r10.keySet();	 Catch:{ JSONException -> 0x002c, ConcurrentModificationException -> 0x0064 }
        r2 = r6.iterator();	 Catch:{ JSONException -> 0x0058, ConcurrentModificationException -> 0x0064 }
    L_0x0041:
        r8 = r2.hasNext();	 Catch:{ JSONException -> 0x0058, ConcurrentModificationException -> 0x0064 }
        if (r8 == 0) goto L_0x005a;
    L_0x0047:
        r4 = r2.next();	 Catch:{ JSONException -> 0x0058, ConcurrentModificationException -> 0x0064 }
        r4 = (java.lang.String) r4;	 Catch:{ JSONException -> 0x0058, ConcurrentModificationException -> 0x0064 }
        r8 = r10.get(r4);	 Catch:{ JSONException -> 0x0058, ConcurrentModificationException -> 0x0064 }
        r3.put(r4, r8);	 Catch:{ JSONException -> 0x0058, ConcurrentModificationException -> 0x0064 }
        r10.remove(r4);	 Catch:{ JSONException -> 0x0058, ConcurrentModificationException -> 0x0064 }
        goto L_0x0041;
    L_0x0058:
        r8 = move-exception;
        goto L_0x002d;
    L_0x005a:
        r8 = io.branch.referral.Defines.Jsonkey.Branch_Instrumentation;	 Catch:{ JSONException -> 0x0058, ConcurrentModificationException -> 0x0064 }
        r8 = r8.getKey();	 Catch:{ JSONException -> 0x0058, ConcurrentModificationException -> 0x0064 }
        r1.put(r8, r3);	 Catch:{ JSONException -> 0x0058, ConcurrentModificationException -> 0x0064 }
        goto L_0x002d;
    L_0x0064:
        r0 = move-exception;
        r1 = r9.params_;
        goto L_0x002d;
        */
        throw new UnsupportedOperationException("Method not decompiled: io.branch.referral.ServerRequest.getPostWithInstrumentationValues(java.util.concurrent.ConcurrentHashMap):org.json.JSONObject");
    }

    public JSONObject getGetParams() {
        return this.params_;
    }

    protected void addGetParam(String paramKey, String paramValue) {
        try {
            this.params_.put(paramKey, paramValue);
        } catch (JSONException e) {
        }
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put(POST_KEY, this.params_);
            json.put(POST_PATH_KEY, this.requestPath_);
            return json;
        } catch (JSONException e) {
            return null;
        }
    }

    public static ServerRequest fromJSON(JSONObject json, Context context) {
        JSONObject post = null;
        String requestPath = BuildConfig.FLAVOR;
        try {
            if (json.has(POST_KEY)) {
                post = json.getJSONObject(POST_KEY);
            }
        } catch (JSONException e) {
        }
        try {
            if (json.has(POST_PATH_KEY)) {
                requestPath = json.getString(POST_PATH_KEY);
            }
        } catch (JSONException e2) {
        }
        if (requestPath == null || requestPath.length() <= 0) {
            return null;
        }
        return getExtendedServerRequest(requestPath, post, context);
    }

    private static ServerRequest getExtendedServerRequest(String requestPath, JSONObject post, Context context) {
        if (requestPath.equalsIgnoreCase(RequestPath.CompletedAction.getPath())) {
            return new ServerRequestActionCompleted(requestPath, post, context);
        }
        if (requestPath.equalsIgnoreCase(RequestPath.GetURL.getPath())) {
            return new ServerRequestCreateUrl(requestPath, post, context);
        }
        if (requestPath.equalsIgnoreCase(RequestPath.GetCreditHistory.getPath())) {
            return new ServerRequestGetRewardHistory(requestPath, post, context);
        }
        if (requestPath.equalsIgnoreCase(RequestPath.GetCredits.getPath())) {
            return new ServerRequestGetRewards(requestPath, post, context);
        }
        if (requestPath.equalsIgnoreCase(RequestPath.IdentifyUser.getPath())) {
            return new ServerRequestIdentifyUserRequest(requestPath, post, context);
        }
        if (requestPath.equalsIgnoreCase(RequestPath.Logout.getPath())) {
            return new ServerRequestLogout(requestPath, post, context);
        }
        if (requestPath.equalsIgnoreCase(RequestPath.RedeemRewards.getPath())) {
            return new ServerRequestRedeemRewards(requestPath, post, context);
        }
        if (requestPath.equalsIgnoreCase(RequestPath.RegisterClose.getPath())) {
            return new ServerRequestRegisterClose(requestPath, post, context);
        }
        if (requestPath.equalsIgnoreCase(RequestPath.RegisterInstall.getPath())) {
            return new ServerRequestRegisterInstall(requestPath, post, context);
        }
        if (requestPath.equalsIgnoreCase(RequestPath.RegisterOpen.getPath())) {
            return new ServerRequestRegisterOpen(requestPath, post, context);
        }
        if (requestPath.equalsIgnoreCase(RequestPath.SendAPPList.getPath())) {
            return new ServerRequestSendAppList(requestPath, post, context);
        }
        return null;
    }

    public void updateGAdsParams(SystemObserver sysObserver) {
        if (!TextUtils.isEmpty(sysObserver.GAIDString_)) {
            try {
                this.params_.put(Jsonkey.GoogleAdvertisingID.getKey(), sysObserver.GAIDString_);
                this.params_.put(Jsonkey.LATVal.getKey(), sysObserver.LATVal_);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    protected boolean doesAppHasInternetPermission(Context context) {
        return context.checkCallingOrSelfPermission("android.permission.INTERNET") == 0;
    }

    public void onRequestQueued() {
        this.queueWaitTime_ = System.currentTimeMillis();
    }

    public long getQueueWaitTime() {
        if (this.queueWaitTime_ > 0) {
            return System.currentTimeMillis() - this.queueWaitTime_;
        }
        return 0;
    }

    public void addProcessWaitLock(PROCESS_WAIT_LOCK lock) {
        if (lock != null) {
            this.locks_.add(lock);
        }
    }

    public void removeProcessWaitLock(PROCESS_WAIT_LOCK lock) {
        this.locks_.remove(lock);
    }

    public boolean isWaitingOnProcessToFinish() {
        return this.locks_.size() > 0;
    }

    public void onPreExecute() {
    }
}
