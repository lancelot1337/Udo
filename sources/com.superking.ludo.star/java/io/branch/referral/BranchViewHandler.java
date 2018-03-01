package io.branch.referral;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.cache.CacheConfig;
import cz.msebera.android.httpclient.protocol.HTTP;
import io.branch.referral.Defines.Jsonkey;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.cocos2dx.lib.BuildConfig;
import org.json.JSONObject;

public class BranchViewHandler {
    public static final int BRANCH_VIEW_ERR_ALREADY_SHOWING = -200;
    public static final int BRANCH_VIEW_ERR_INVALID_VIEW = -201;
    public static final int BRANCH_VIEW_ERR_REACHED_LIMIT = -203;
    public static final int BRANCH_VIEW_ERR_TEMP_UNAVAILABLE = -202;
    private static final String BRANCH_VIEW_REDIRECT_ACTION_ACCEPT = "accept";
    private static final String BRANCH_VIEW_REDIRECT_ACTION_CANCEL = "cancel";
    private static final String BRANCH_VIEW_REDIRECT_SCHEME = "branch-cta";
    private static BranchViewHandler thisInstance_;
    private Dialog branchViewDialog_;
    private boolean isBranchViewAccepted_;
    private boolean isBranchViewDialogShowing_;
    private boolean loadingHtmlInBackGround_ = false;
    private BranchView openOrInstallPendingBranchView_ = null;
    private String parentActivityClassName_;
    private boolean webViewLoadError_;

    public interface IBranchViewEvents {
        void onBranchViewAccepted(String str, String str2);

        void onBranchViewCancelled(String str, String str2);

        void onBranchViewError(int i, String str, String str2);

        void onBranchViewVisible(String str, String str2);
    }

    private class BranchView {
        private static final int USAGE_UNLIMITED = -1;
        private String branchViewAction_;
        private String branchViewID_;
        private int num_of_use_;
        private String webViewHtml_;
        private String webViewUrl_;

        private BranchView(JSONObject branchViewJson, String actionName) {
            this.branchViewID_ = BuildConfig.FLAVOR;
            this.branchViewAction_ = BuildConfig.FLAVOR;
            this.num_of_use_ = 1;
            this.webViewUrl_ = BuildConfig.FLAVOR;
            this.webViewHtml_ = BuildConfig.FLAVOR;
            try {
                this.branchViewAction_ = actionName;
                if (branchViewJson.has(Jsonkey.BranchViewID.getKey())) {
                    this.branchViewID_ = branchViewJson.getString(Jsonkey.BranchViewID.getKey());
                }
                if (branchViewJson.has(Jsonkey.BranchViewNumOfUse.getKey())) {
                    this.num_of_use_ = branchViewJson.getInt(Jsonkey.BranchViewNumOfUse.getKey());
                }
                if (branchViewJson.has(Jsonkey.BranchViewUrl.getKey())) {
                    this.webViewUrl_ = branchViewJson.getString(Jsonkey.BranchViewUrl.getKey());
                }
                if (branchViewJson.has(Jsonkey.BranchViewHtml.getKey())) {
                    this.webViewHtml_ = branchViewJson.getString(Jsonkey.BranchViewHtml.getKey());
                }
            } catch (Exception e) {
            }
        }

        private boolean isAvailable(Context context) {
            return this.num_of_use_ > PrefHelper.getInstance(context).getBranchViewUsageCount(this.branchViewID_) || this.num_of_use_ == USAGE_UNLIMITED;
        }

        public void updateUsageCount(Context context, String branchViewID) {
            PrefHelper.getInstance(context).updateBranchViewUsageCount(branchViewID);
        }
    }

    private class loadBranchViewTask extends AsyncTask<Void, Void, Boolean> {
        private final BranchView branchView;
        private final IBranchViewEvents callback;
        private final Context context;
        private String htmlString;

        public loadBranchViewTask(BranchView branchView, Context context, IBranchViewEvents callback) {
            this.branchView = branchView;
            this.context = context;
            this.callback = callback;
        }

        protected Boolean doInBackground(Void... params) {
            boolean z = false;
            int code = -1;
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(this.branchView.webViewUrl_).openConnection();
                connection.setRequestMethod(HttpGet.METHOD_NAME);
                connection.connect();
                code = connection.getResponseCode();
                if (code == HttpStatus.SC_OK) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    InputStream inputStream = connection.getInputStream();
                    byte[] buffer = new byte[1024];
                    while (true) {
                        int length = inputStream.read(buffer);
                        if (length == -1) {
                            break;
                        }
                        outputStream.write(buffer, 0, length);
                    }
                    this.branchView.webViewHtml_ = outputStream.toString(HTTP.UTF_8);
                    outputStream.close();
                    inputStream.close();
                }
            } catch (Exception e) {
            }
            if (code == HttpStatus.SC_OK) {
                z = true;
            }
            return Boolean.valueOf(z);
        }

        protected void onPostExecute(Boolean loadHtml) {
            super.onPostExecute(loadHtml);
            if (loadHtml.booleanValue()) {
                BranchViewHandler.this.createAndShowBranchView(this.branchView, this.context, this.callback);
            } else if (this.callback != null) {
                this.callback.onBranchViewError(BranchViewHandler.BRANCH_VIEW_ERR_TEMP_UNAVAILABLE, "Unable to create a Branch view due to a temporary network error", this.branchView.branchViewAction_);
            }
            BranchViewHandler.this.loadingHtmlInBackGround_ = false;
        }
    }

    private BranchViewHandler() {
    }

    public static BranchViewHandler getInstance() {
        if (thisInstance_ == null) {
            thisInstance_ = new BranchViewHandler();
        }
        return thisInstance_;
    }

    public boolean showPendingBranchView(Context appContext) {
        boolean isBranchViewShowed = showBranchView(this.openOrInstallPendingBranchView_, appContext, null);
        if (isBranchViewShowed) {
            this.openOrInstallPendingBranchView_ = null;
        }
        return isBranchViewShowed;
    }

    public boolean showBranchView(JSONObject branchViewObj, String actionName, Context appContext, IBranchViewEvents callback) {
        return showBranchView(new BranchView(branchViewObj, actionName), appContext, callback);
    }

    private boolean showBranchView(BranchView branchView, Context appContext, IBranchViewEvents callback) {
        if (this.isBranchViewDialogShowing_ || this.loadingHtmlInBackGround_) {
            if (callback != null) {
                callback.onBranchViewError(BRANCH_VIEW_ERR_ALREADY_SHOWING, "Unable to create a Branch view. A Branch view is already showing", branchView.branchViewAction_);
            }
            return false;
        }
        this.isBranchViewDialogShowing_ = false;
        this.isBranchViewAccepted_ = false;
        if (!(appContext == null || branchView == null)) {
            if (branchView.isAvailable(appContext)) {
                if (TextUtils.isEmpty(branchView.webViewHtml_)) {
                    this.loadingHtmlInBackGround_ = true;
                    new loadBranchViewTask(branchView, appContext, callback).execute(new Void[0]);
                    return true;
                }
                createAndShowBranchView(branchView, appContext, callback);
                return true;
            } else if (callback != null) {
                callback.onBranchViewError(BRANCH_VIEW_ERR_REACHED_LIMIT, "Unable to create this Branch view. Reached maximum usage limit ", branchView.branchViewAction_);
            }
        }
        return false;
    }

    private void createAndShowBranchView(final BranchView branchView, Context appContext, final IBranchViewEvents callback) {
        if (appContext != null && branchView != null) {
            final WebView webView = new WebView(appContext);
            webView.getSettings().setJavaScriptEnabled(true);
            if (VERSION.SDK_INT >= 19) {
                webView.setLayerType(2, null);
            }
            this.webViewLoadError_ = false;
            if (!TextUtils.isEmpty(branchView.webViewHtml_)) {
                webView.loadDataWithBaseURL(null, branchView.webViewHtml_, "text/html", "utf-8", null);
                webView.setWebViewClient(new WebViewClient() {
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        boolean isHandled = BranchViewHandler.this.handleUserActionRedirect(url);
                        if (!isHandled) {
                            view.loadUrl(url);
                        } else if (BranchViewHandler.this.branchViewDialog_ != null) {
                            BranchViewHandler.this.branchViewDialog_.dismiss();
                        }
                        return isHandled;
                    }

                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        super.onPageStarted(view, url, favicon);
                    }

                    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                        super.onReceivedError(view, errorCode, description, failingUrl);
                        BranchViewHandler.this.webViewLoadError_ = true;
                    }

                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        BranchViewHandler.this.openBranchViewDialog(branchView, callback, webView);
                    }
                });
            }
        }
    }

    private void openBranchViewDialog(final BranchView branchView, final IBranchViewEvents callback, WebView webView) {
        if (this.webViewLoadError_ || Branch.getInstance() == null || Branch.getInstance().currentActivityReference_ == null) {
            this.isBranchViewDialogShowing_ = false;
            if (callback != null) {
                callback.onBranchViewError(BRANCH_VIEW_ERR_TEMP_UNAVAILABLE, "Unable to create a Branch view due to a temporary network error", branchView.branchViewAction_);
                return;
            }
            return;
        }
        Activity currentActivity = (Activity) Branch.getInstance().currentActivityReference_.get();
        if (currentActivity != null) {
            branchView.updateUsageCount(currentActivity.getApplicationContext(), branchView.branchViewID_);
            this.parentActivityClassName_ = currentActivity.getClass().getName();
            RelativeLayout layout = new RelativeLayout(currentActivity);
            layout.setVisibility(8);
            layout.addView(webView, new LayoutParams(-1, -1));
            layout.setBackgroundColor(0);
            if (this.branchViewDialog_ == null || !this.branchViewDialog_.isShowing()) {
                this.branchViewDialog_ = new Dialog(currentActivity, 16973834);
                this.branchViewDialog_.setContentView(layout);
                layout.setVisibility(0);
                webView.setVisibility(0);
                this.branchViewDialog_.show();
                showViewWithAlphaAnimation(layout);
                showViewWithAlphaAnimation(webView);
                this.isBranchViewDialogShowing_ = true;
                if (callback != null) {
                    callback.onBranchViewVisible(branchView.branchViewAction_, branchView.branchViewID_);
                }
                this.branchViewDialog_.setOnDismissListener(new OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        BranchViewHandler.this.isBranchViewDialogShowing_ = false;
                        BranchViewHandler.this.branchViewDialog_ = null;
                        if (callback == null) {
                            return;
                        }
                        if (BranchViewHandler.this.isBranchViewAccepted_) {
                            callback.onBranchViewAccepted(branchView.branchViewAction_, branchView.branchViewID_);
                        } else {
                            callback.onBranchViewCancelled(branchView.branchViewAction_, branchView.branchViewID_);
                        }
                    }
                });
            } else if (callback != null) {
                callback.onBranchViewError(BRANCH_VIEW_ERR_ALREADY_SHOWING, "Unable to create a Branch view. A Branch view is already showing", branchView.branchViewAction_);
            }
        }
    }

    private boolean handleUserActionRedirect(String url) {
        try {
            URI uri = new URI(url);
            if (!uri.getScheme().equalsIgnoreCase(BRANCH_VIEW_REDIRECT_SCHEME)) {
                return false;
            }
            if (uri.getHost().equalsIgnoreCase(BRANCH_VIEW_REDIRECT_ACTION_ACCEPT)) {
                this.isBranchViewAccepted_ = true;
                return true;
            } else if (!uri.getHost().equalsIgnoreCase(BRANCH_VIEW_REDIRECT_ACTION_CANCEL)) {
                return false;
            } else {
                this.isBranchViewAccepted_ = false;
                return true;
            }
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private void showViewWithAlphaAnimation(View view) {
        AlphaAnimation animation1 = new AlphaAnimation(CacheConfig.DEFAULT_HEURISTIC_COEFFICIENT, 1.0f);
        animation1.setDuration(500);
        animation1.setStartOffset(10);
        animation1.setInterpolator(new AccelerateInterpolator());
        animation1.setFillAfter(true);
        view.setVisibility(0);
        view.startAnimation(animation1);
    }

    private void hideViewWithAlphaAnimation(View view) {
        AlphaAnimation animation1 = new AlphaAnimation(1.0f, 0.0f);
        animation1.setDuration(500);
        animation1.setStartOffset(10);
        animation1.setInterpolator(new DecelerateInterpolator());
        animation1.setFillAfter(true);
        view.setVisibility(8);
        view.startAnimation(animation1);
    }

    public boolean markInstallOrOpenBranchViewPending(JSONObject branchViewObj, String action) {
        BranchView branchView = new BranchView(branchViewObj, action);
        if (branchView == null || Branch.getInstance().currentActivityReference_ == null) {
            return false;
        }
        Activity currentActivity = (Activity) Branch.getInstance().currentActivityReference_.get();
        if (currentActivity == null || !branchView.isAvailable(currentActivity)) {
            return false;
        }
        this.openOrInstallPendingBranchView_ = new BranchView(branchViewObj, action);
        return true;
    }

    public boolean isInstallOrOpenBranchViewPending(Context context) {
        return this.openOrInstallPendingBranchView_ != null && this.openOrInstallPendingBranchView_.isAvailable(context);
    }

    public void onCurrentActivityDestroyed(Activity activity) {
        if (this.parentActivityClassName_ != null && this.parentActivityClassName_.equalsIgnoreCase(activity.getClass().getName())) {
            this.isBranchViewDialogShowing_ = false;
        }
    }
}
