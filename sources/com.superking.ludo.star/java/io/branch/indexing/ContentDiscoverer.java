package io.branch.indexing;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.facebook.share.internal.ShareConstants;
import io.branch.referral.PrefHelper;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import org.cocos2dx.lib.BuildConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ContentDiscoverer {
    private static final String CONTENT_DATA_KEY = "cd";
    private static final String CONTENT_KEYS_KEY = "ck";
    private static final String CONTENT_LINK_KEY = "cl";
    private static final String CONTENT_META_DATA_KEY = "cm";
    private static final String ENTITIES_KEY = "e";
    private static final String NAV_PATH_KEY = "n";
    private static final String PACKAGE_NAME_KEY = "p";
    private static final String REFERRAL_LINK_KEY = "rl";
    private static final String TIME_STAMP_CLOSE_KEY = "tc";
    private static final String TIME_STAMP_KEY = "ts";
    private static final String VIEW_KEY = "v";
    private static final int VIEW_SETTLE_TIME = 1000;
    private static ContentDiscoverer thisInstance_;
    private ContentDiscoveryManifest cdManifest_;
    private JSONObject contentEvent_;
    private int discoveredViewInThisSession_ = 0;
    private ArrayList<String> discoveredViewList_ = new ArrayList();
    private Handler handler_ = new Handler();
    private final HashHelper hashHelper_ = new HashHelper();
    private WeakReference<Activity> lastActivityReference_;
    private Runnable readContentRunnable = new Runnable() {
        public void run() {
            boolean z = true;
            try {
                if (ContentDiscoverer.this.cdManifest_.isCDEnabled() && ContentDiscoverer.this.lastActivityReference_ != null && ContentDiscoverer.this.lastActivityReference_.get() != null) {
                    Activity activity = (Activity) ContentDiscoverer.this.lastActivityReference_.get();
                    ContentDiscoverer.this.contentEvent_ = new JSONObject();
                    ContentDiscoverer.this.contentEvent_.put(ContentDiscoverer.TIME_STAMP_KEY, System.currentTimeMillis());
                    if (!TextUtils.isEmpty(ContentDiscoverer.this.referredUrl_)) {
                        ContentDiscoverer.this.contentEvent_.put(ContentDiscoverer.REFERRAL_LINK_KEY, ContentDiscoverer.this.referredUrl_);
                    }
                    String viewName = "/" + activity.getClass().getSimpleName();
                    ContentDiscoverer.this.contentEvent_.put(ContentDiscoverer.VIEW_KEY, viewName);
                    ViewGroup rootView = (ViewGroup) activity.findViewById(16908290);
                    if (rootView != null) {
                        boolean isClearText;
                        CDPathProperties cdPathProperties = ContentDiscoverer.this.cdManifest_.getCDPathProperties(activity);
                        if (cdPathProperties == null || !cdPathProperties.isClearTextRequested()) {
                            isClearText = false;
                        } else {
                            isClearText = true;
                        }
                        JSONArray filteredElements = null;
                        if (cdPathProperties != null) {
                            isClearText = cdPathProperties.isClearTextRequested();
                            JSONObject access$200 = ContentDiscoverer.this.contentEvent_;
                            String str = ContentDiscoveryManifest.HASH_MODE_KEY;
                            if (isClearText) {
                                z = false;
                            }
                            access$200.put(str, z);
                            filteredElements = cdPathProperties.getFilteredElements();
                        }
                        JSONArray contentKeysArray;
                        if (filteredElements != null && filteredElements.length() > 0) {
                            contentKeysArray = new JSONArray();
                            ContentDiscoverer.this.contentEvent_.put(ContentDiscoverer.CONTENT_KEYS_KEY, contentKeysArray);
                            JSONArray contentDataArray = new JSONArray();
                            ContentDiscoverer.this.contentEvent_.put(ContentDiscoverer.CONTENT_DATA_KEY, contentDataArray);
                            ContentDiscoverer.this.discoverFilteredViewContents(filteredElements, contentDataArray, contentKeysArray, activity, isClearText);
                        } else if (!ContentDiscoverer.this.discoveredViewList_.contains(viewName)) {
                            contentKeysArray = new JSONArray();
                            ContentDiscoverer.this.contentEvent_.put(ContentDiscoverer.CONTENT_KEYS_KEY, contentKeysArray);
                            ContentDiscoverer.this.discoverViewContents(rootView, null, contentKeysArray, activity.getResources(), isClearText);
                        }
                        ContentDiscoverer.this.discoveredViewList_.add(viewName);
                        PrefHelper.getInstance(activity).saveBranchAnalyticsData(ContentDiscoverer.this.contentEvent_);
                        ContentDiscoverer.this.lastActivityReference_ = null;
                    }
                }
            } catch (JSONException e) {
            }
        }
    };
    private String referredUrl_;

    private class HashHelper {
        MessageDigest messageDigest_;

        public HashHelper() {
            try {
                this.messageDigest_ = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
            }
        }

        public String hashContent(String content) {
            String hashedVal = BuildConfig.FLAVOR;
            if (this.messageDigest_ == null) {
                return hashedVal;
            }
            this.messageDigest_.reset();
            this.messageDigest_.update(content.getBytes());
            return new String(this.messageDigest_.digest());
        }
    }

    public static ContentDiscoverer getInstance() {
        if (thisInstance_ == null) {
            thisInstance_ = new ContentDiscoverer();
        }
        return thisInstance_;
    }

    private ContentDiscoverer() {
    }

    public void discoverContent(Activity activity, String referredUrl) {
        this.cdManifest_ = ContentDiscoveryManifest.getInstance(activity);
        this.referredUrl_ = referredUrl;
        CDPathProperties pathProperties = this.cdManifest_.getCDPathProperties(activity);
        if (pathProperties != null) {
            if (!pathProperties.isSkipContentDiscovery()) {
                discoverContent(activity);
            }
        } else if (!TextUtils.isEmpty(this.referredUrl_)) {
            discoverContent(activity);
        }
    }

    public void onActivityStopped(Activity activity) {
        if (!(this.lastActivityReference_ == null || this.lastActivityReference_.get() == null || !((Activity) this.lastActivityReference_.get()).getClass().getName().equals(activity.getClass().getName()))) {
            this.handler_.removeCallbacks(this.readContentRunnable);
            this.lastActivityReference_ = null;
        }
        updateLastViewTimeStampIfNeeded();
    }

    public void onSessionStarted(Activity activity, String referredUrl) {
        this.discoveredViewList_ = new ArrayList();
        discoverContent(activity, referredUrl);
    }

    private void discoverContent(Activity activity) {
        if (this.discoveredViewList_.size() < this.cdManifest_.getMaxViewHistorySize()) {
            this.handler_.removeCallbacks(this.readContentRunnable);
            this.lastActivityReference_ = new WeakReference(activity);
            this.handler_.postDelayed(this.readContentRunnable, 1000);
        }
    }

    private void updateLastViewTimeStampIfNeeded() {
        try {
            if (this.contentEvent_ != null) {
                this.contentEvent_.put(TIME_STAMP_CLOSE_KEY, System.currentTimeMillis());
            }
        } catch (JSONException e) {
        }
    }

    private void discoverFilteredViewContents(JSONArray viewIDArray, JSONArray contentDataArray, JSONArray contentKeysArray, Activity activity, boolean isClearText) {
        int i = 0;
        while (i < viewIDArray.length()) {
            try {
                updateElementData(viewIDArray.getString(i), activity.findViewById(activity.getResources().getIdentifier(viewIDArray.getString(i), ShareConstants.WEB_DIALOG_PARAM_ID, activity.getPackageName())), isClearText, contentDataArray, contentKeysArray);
                i++;
            } catch (JSONException e) {
                return;
            }
        }
    }

    private void discoverViewContents(ViewGroup viewGroup, JSONArray contentDataArray, JSONArray contentKeysArray, Resources res, boolean isClearText) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View childView = viewGroup.getChildAt(i);
            if (childView.getVisibility() == 0 && (childView instanceof ViewGroup)) {
                discoverViewContents((ViewGroup) childView, contentDataArray, contentKeysArray, res, isClearText);
            } else {
                String viewName = String.valueOf(childView.getId());
                try {
                    viewName = res.getResourceEntryName(childView.getId());
                } catch (Exception e) {
                }
                updateElementData(viewName, childView, isClearText, contentDataArray, contentKeysArray);
            }
        }
    }

    private void updateElementData(String viewName, View view, boolean isClearText, JSONArray contentDataArray, JSONArray contentKeysArray) {
        if (view instanceof TextView) {
            TextView txtView = (TextView) view;
            if (contentDataArray != null) {
                String viewVal = null;
                if (txtView.getText() != null) {
                    viewVal = txtView.getText().toString().substring(0, Math.min(txtView.getText().toString().length(), this.cdManifest_.getMaxTextLen()));
                    if (!isClearText) {
                        viewVal = this.hashHelper_.hashContent(viewVal);
                    }
                }
                contentDataArray.put(viewVal);
            }
            contentKeysArray.put(viewName);
        }
    }

    public JSONObject getContentDiscoverDataForCloseRequest(Context context) {
        JSONObject cdObj = null;
        JSONObject branchAnalyticalData = PrefHelper.getInstance(context).getBranchAnalyticsData();
        if (branchAnalyticalData.length() > 0 && branchAnalyticalData.toString().length() < this.cdManifest_.getMaxPacketSize()) {
            cdObj = new JSONObject();
            try {
                cdObj.put(ContentDiscoveryManifest.MANIFEST_VERSION_KEY, ContentDiscoveryManifest.getInstance(context).getManifestVersion()).put(ENTITIES_KEY, branchAnalyticalData);
                if (context != null) {
                    cdObj.put(PACKAGE_NAME_KEY, context.getPackageName());
                    cdObj.put(PACKAGE_NAME_KEY, context.getPackageName());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        PrefHelper.getInstance(context).clearBranchAnalyticsData();
        return cdObj;
    }
}
