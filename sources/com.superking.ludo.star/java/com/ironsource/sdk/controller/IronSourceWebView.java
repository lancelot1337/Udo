package com.ironsource.sdk.controller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.ConsoleMessage;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.Toast;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.internal.NativeProtocol;
import com.facebook.internal.ServerProtocol;
import com.facebook.share.internal.ShareConstants;
import com.ironsource.environment.ApplicationContext;
import com.ironsource.environment.ConnectivityService;
import com.ironsource.environment.DeviceStatus;
import com.ironsource.environment.LocationService;
import com.ironsource.environment.UrlHandler;
import com.ironsource.sdk.agent.IronSourceAdsPublisherAgent;
import com.ironsource.sdk.data.AdUnitsReady;
import com.ironsource.sdk.data.AdUnitsState;
import com.ironsource.sdk.data.DemandSource;
import com.ironsource.sdk.data.SSABCParameters;
import com.ironsource.sdk.data.SSAEnums.ControllerState;
import com.ironsource.sdk.data.SSAEnums.DebugMode;
import com.ironsource.sdk.data.SSAEnums.ProductType;
import com.ironsource.sdk.data.SSAFile;
import com.ironsource.sdk.data.SSAObj;
import com.ironsource.sdk.listeners.DSRewardedVideoListener;
import com.ironsource.sdk.listeners.OnGenericFunctionListener;
import com.ironsource.sdk.listeners.OnInterstitialListener;
import com.ironsource.sdk.listeners.OnOfferWallListener;
import com.ironsource.sdk.listeners.OnWebViewChangeListener;
import com.ironsource.sdk.precache.DownloadManager;
import com.ironsource.sdk.precache.DownloadManager.OnPreCacheCompletion;
import com.ironsource.sdk.utils.Constants;
import com.ironsource.sdk.utils.Constants.ErrorCodes;
import com.ironsource.sdk.utils.Constants.ForceClosePosition;
import com.ironsource.sdk.utils.Constants.JSMethods;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import com.ironsource.sdk.utils.Constants.RequestParameters;
import com.ironsource.sdk.utils.DeviceProperties;
import com.ironsource.sdk.utils.IronSourceAsyncHttpRequestTask;
import com.ironsource.sdk.utils.IronSourceSharedPrefHelper;
import com.ironsource.sdk.utils.IronSourceStorageUtils;
import com.ironsource.sdk.utils.Logger;
import com.ironsource.sdk.utils.SDKUtils;
import com.unity3d.ads.adunit.AdUnitActivity;
import cz.msebera.android.httpclient.cookie.ClientCookie;
import cz.msebera.android.httpclient.protocol.HTTP;
import io.branch.referral.R;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.cocos2dx.lib.BuildConfig;
import org.cocos2dx.lib.Cocos2dxEditBox;
import org.cocos2dx.lib.Cocos2dxHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class IronSourceWebView extends WebView implements DownloadListener, OnPreCacheCompletion {
    public static String APP_IDS = "appIds";
    public static int DISPLAY_WEB_VIEW_INTENT = 0;
    public static String EXTERNAL_URL = "external_url";
    public static String IS_INSTALLED = "isInstalled";
    public static String IS_STORE = "is_store";
    public static String IS_STORE_CLOSE = "is_store_close";
    private static String JSON_KEY_FAIL = "fail";
    private static String JSON_KEY_SUCCESS = GraphResponse.SUCCESS_KEY;
    public static int OPEN_URL_INTENT = 1;
    public static String REQUEST_ID = "requestId";
    public static String RESULT = "result";
    public static String SECONDARY_WEB_VIEW = "secondary_web_view";
    public static String WEBVIEW_TYPE = "webview_type";
    public static int mDebugMode = 0;
    private final String GENERIC_MESSAGE = "We're sorry, some error occurred. we will investigate it";
    private String PUB_TAG = "IronSource";
    private String TAG = IronSourceWebView.class.getSimpleName();
    private DownloadManager downloadManager;
    private Boolean isKitkatAndAbove = null;
    private boolean isRemoveCloseEventHandler;
    private String mCacheDirectory;
    private OnWebViewChangeListener mChangeListener;
    private CountDownTimer mCloseEventTimer;
    private BroadcastReceiver mConnectionReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (IronSourceWebView.this.mControllerState == ControllerState.Ready) {
                String networkType = ParametersKeys.ORIENTATION_NONE;
                if (ConnectivityService.isConnectedWifi(context)) {
                    networkType = ConnectivityService.NETWORK_TYPE_WIFI;
                } else if (ConnectivityService.isConnectedMobile(context)) {
                    networkType = ConnectivityService.NETWORK_TYPE_3G;
                }
                IronSourceWebView.this.deviceStatusChanged(networkType);
            }
        }
    };
    private String mControllerKeyPressed = "interrupt";
    private FrameLayout mControllerLayout;
    private ControllerState mControllerState = ControllerState.None;
    Context mCurrentActivityContext;
    private View mCustomView;
    private CustomViewCallback mCustomViewCallback;
    private FrameLayout mCustomViewContainer;
    private boolean mGlobalControllerTimeFinish;
    private CountDownTimer mGlobalControllerTimer;
    private int mHiddenForceCloseHeight = 50;
    private String mHiddenForceCloseLocation = ForceClosePosition.TOP_RIGHT;
    private int mHiddenForceCloseWidth = 50;
    private String mISAppKey;
    private Map<String, String> mISExtraParameters;
    private String mISUserId;
    private boolean mISmiss;
    private boolean mIsActivityThemeTranslucent = false;
    private boolean mIsImmersive = false;
    private Boolean mIsInterstitialAvailable = null;
    private CountDownTimer mLoadControllerTimer;
    private String mOWAppKey;
    private String mOWCreditsAppKey;
    private boolean mOWCreditsMiss;
    private String mOWCreditsUserId;
    private Map<String, String> mOWExtraParameters;
    private String mOWUserId;
    private boolean mOWmiss;
    private OnGenericFunctionListener mOnGenericFunctionListener;
    private OnInterstitialListener mOnInitInterstitialListener;
    private OnOfferWallListener mOnOfferWallListener;
    private DSRewardedVideoListener mOnRewardedVideoListener;
    private String mOrientationState;
    private String mRVAppKey;
    private String mRVUserId;
    private String mRequestParameters;
    private AdUnitsState mSavedState;
    private Object mSavedStateLocker = new Object();
    private State mState;
    Handler mUiHandler;
    private Uri mUri;
    private VideoEventsListener mVideoEventsListener;
    private ChromeClient mWebChromeClient;

    static /* synthetic */ class AnonymousClass8 {
        static final /* synthetic */ int[] $SwitchMap$com$ironsource$sdk$data$SSAEnums$ProductType = new int[ProductType.values().length];

        static {
            try {
                $SwitchMap$com$ironsource$sdk$data$SSAEnums$ProductType[ProductType.RewardedVideo.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$ironsource$sdk$data$SSAEnums$ProductType[ProductType.Interstitial.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$ironsource$sdk$data$SSAEnums$ProductType[ProductType.OfferWall.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$ironsource$sdk$data$SSAEnums$ProductType[ProductType.OfferWallCredits.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    private class ChromeClient extends WebChromeClient {
        private ChromeClient() {
        }

        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            WebView childView = new WebView(view.getContext());
            childView.setWebChromeClient(this);
            childView.setWebViewClient(new FrameBustWebViewClient());
            resultMsg.obj.setWebView(childView);
            resultMsg.sendToTarget();
            Logger.i("onCreateWindow", "onCreateWindow");
            return true;
        }

        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Logger.i("MyApplication", consoleMessage.message() + " -- From line " + consoleMessage.lineNumber() + " of " + consoleMessage.sourceId());
            return true;
        }

        public void onShowCustomView(View view, CustomViewCallback callback) {
            Logger.i("Test", "onShowCustomView");
            IronSourceWebView.this.setVisibility(8);
            if (IronSourceWebView.this.mCustomView != null) {
                Logger.i("Test", "mCustomView != null");
                callback.onCustomViewHidden();
                return;
            }
            Logger.i("Test", "mCustomView == null");
            IronSourceWebView.this.mCustomViewContainer.addView(view);
            IronSourceWebView.this.mCustomView = view;
            IronSourceWebView.this.mCustomViewCallback = callback;
            IronSourceWebView.this.mCustomViewContainer.setVisibility(0);
        }

        public View getVideoLoadingProgressView() {
            FrameLayout frameLayout = new FrameLayout(IronSourceWebView.this.getCurrentActivityContext());
            frameLayout.setLayoutParams(new LayoutParams(-1, -1));
            return frameLayout;
        }

        public void onHideCustomView() {
            Logger.i("Test", "onHideCustomView");
            if (IronSourceWebView.this.mCustomView != null) {
                IronSourceWebView.this.mCustomView.setVisibility(8);
                IronSourceWebView.this.mCustomViewContainer.removeView(IronSourceWebView.this.mCustomView);
                IronSourceWebView.this.mCustomView = null;
                IronSourceWebView.this.mCustomViewContainer.setVisibility(8);
                IronSourceWebView.this.mCustomViewCallback.onCustomViewHidden();
                IronSourceWebView.this.setVisibility(0);
            }
        }
    }

    private class FrameBustWebViewClient extends WebViewClient {
        private FrameBustWebViewClient() {
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Context ctx = IronSourceWebView.this.getCurrentActivityContext();
            Intent intent = new Intent(ctx, OpenUrlActivity.class);
            intent.putExtra(IronSourceWebView.EXTERNAL_URL, url);
            intent.putExtra(IronSourceWebView.SECONDARY_WEB_VIEW, false);
            ctx.startActivity(intent);
            return true;
        }
    }

    public class JSInterface {
        volatile int udiaResults = 0;

        public JSInterface(Context context) {
        }

        @JavascriptInterface
        public void initController(String value) {
            Logger.i(IronSourceWebView.this.TAG, "initController(" + value + ")");
            SSAObj ssaObj = new SSAObj(value);
            if (ssaObj.containsKey(ParametersKeys.STAGE)) {
                String stage = ssaObj.getString(ParametersKeys.STAGE);
                if (ParametersKeys.READY.equalsIgnoreCase(stage)) {
                    IronSourceWebView.this.mControllerState = ControllerState.Ready;
                    IronSourceWebView.this.mGlobalControllerTimer.cancel();
                    IronSourceWebView.this.mLoadControllerTimer.cancel();
                    for (DemandSource demandSource : IronSourceAdsPublisherAgent.getInstance((Activity) IronSourceWebView.this.getCurrentActivityContext()).getDemandSources()) {
                        if (demandSource.getDemandSourceInitState() == 1) {
                            IronSourceWebView.this.initRewardedVideo(IronSourceWebView.this.mRVAppKey, IronSourceWebView.this.mRVUserId, demandSource.getDemandSourceName(), IronSourceWebView.this.mOnRewardedVideoListener);
                        }
                    }
                    if (IronSourceWebView.this.mISmiss) {
                        IronSourceWebView.this.initInterstitial(IronSourceWebView.this.mISAppKey, IronSourceWebView.this.mISUserId, IronSourceWebView.this.mISExtraParameters, IronSourceWebView.this.mOnInitInterstitialListener);
                    }
                    if (IronSourceWebView.this.mOWmiss) {
                        IronSourceWebView.this.initOfferWall(IronSourceWebView.this.mOWAppKey, IronSourceWebView.this.mOWUserId, IronSourceWebView.this.mOWExtraParameters, IronSourceWebView.this.mOnOfferWallListener);
                    }
                    if (IronSourceWebView.this.mOWCreditsMiss) {
                        IronSourceWebView.this.getOfferWallCredits(IronSourceWebView.this.mOWCreditsAppKey, IronSourceWebView.this.mOWCreditsUserId, IronSourceWebView.this.mOnOfferWallListener);
                    }
                    IronSourceWebView.this.restoreState(IronSourceWebView.this.mSavedState);
                } else if (ParametersKeys.LOADED.equalsIgnoreCase(stage)) {
                    IronSourceWebView.this.mControllerState = ControllerState.Loaded;
                } else if (ParametersKeys.FAILED.equalsIgnoreCase(stage)) {
                    IronSourceWebView.this.mControllerState = ControllerState.Failed;
                    for (DemandSource demandSource2 : IronSourceAdsPublisherAgent.getInstance((Activity) IronSourceWebView.this.getCurrentActivityContext()).getDemandSources()) {
                        if (demandSource2.getDemandSourceInitState() == 1) {
                            IronSourceWebView.this.sendProductErrorMessage(ProductType.RewardedVideo, demandSource2.getDemandSourceName());
                        }
                    }
                    if (IronSourceWebView.this.mISmiss) {
                        IronSourceWebView.this.sendProductErrorMessage(ProductType.Interstitial, null);
                    }
                    if (IronSourceWebView.this.mOWmiss) {
                        IronSourceWebView.this.sendProductErrorMessage(ProductType.OfferWall, null);
                    }
                    if (IronSourceWebView.this.mOWCreditsMiss) {
                        IronSourceWebView.this.sendProductErrorMessage(ProductType.OfferWallCredits, null);
                    }
                } else {
                    Logger.i(IronSourceWebView.this.TAG, "No STAGE mentioned! Should not get here!");
                }
            }
        }

        @JavascriptInterface
        public void alert(String message) {
        }

        @JavascriptInterface
        public void getDeviceStatus(String value) {
            Logger.i(IronSourceWebView.this.TAG, "getDeviceStatus(" + value + ")");
            String successFunToCall = IronSourceWebView.this.extractSuccessFunctionToCall(value);
            String failFunToCall = IronSourceWebView.this.extractFailFunctionToCall(value);
            Object[] resultArr = new Object[2];
            resultArr = IronSourceWebView.this.getDeviceParams(IronSourceWebView.this.getContext());
            String params = resultArr[0];
            String funToCall = null;
            if (((Boolean) resultArr[1]).booleanValue()) {
                if (!TextUtils.isEmpty(failFunToCall)) {
                    funToCall = failFunToCall;
                }
            } else if (!TextUtils.isEmpty(successFunToCall)) {
                funToCall = successFunToCall;
            }
            if (!TextUtils.isEmpty(funToCall)) {
                IronSourceWebView.this.injectJavascript(IronSourceWebView.this.generateJSToInject(funToCall, params, JSMethods.ON_GET_DEVICE_STATUS_SUCCESS, JSMethods.ON_GET_DEVICE_STATUS_FAIL));
            }
        }

        @JavascriptInterface
        public void getApplicationInfo(String value) {
            Logger.i(IronSourceWebView.this.TAG, "getApplicationInfo(" + value + ")");
            String successFunToCall = IronSourceWebView.this.extractSuccessFunctionToCall(value);
            String failFunToCall = IronSourceWebView.this.extractFailFunctionToCall(value);
            SSAObj ssaObj = new SSAObj(value);
            String funToCall = null;
            Object[] resultArr = new Object[2];
            resultArr = IronSourceWebView.this.getApplicationParams(ssaObj.getString(ParametersKeys.PRODUCT_TYPE), ssaObj.getString(RequestParameters.DEMAND_SOURCE_NAME));
            String params = resultArr[0];
            if (((Boolean) resultArr[1]).booleanValue()) {
                if (!TextUtils.isEmpty(failFunToCall)) {
                    funToCall = failFunToCall;
                }
            } else if (!TextUtils.isEmpty(successFunToCall)) {
                funToCall = successFunToCall;
            }
            if (!TextUtils.isEmpty(funToCall)) {
                IronSourceWebView.this.injectJavascript(IronSourceWebView.this.generateJSToInject(funToCall, params, JSMethods.ON_GET_APPLICATION_INFO_SUCCESS, JSMethods.ON_GET_APPLICATION_INFO_FAIL));
            }
        }

        @JavascriptInterface
        public void checkInstalledApps(String value) {
            Logger.i(IronSourceWebView.this.TAG, "checkInstalledApps(" + value + ")");
            String successFunToCall = IronSourceWebView.this.extractSuccessFunctionToCall(value);
            String failFunToCall = IronSourceWebView.this.extractFailFunctionToCall(value);
            String funToCall = null;
            SSAObj ssaObj = new SSAObj(value);
            Object[] resultArr = IronSourceWebView.this.getAppsStatus(ssaObj.getString(IronSourceWebView.APP_IDS), ssaObj.getString(IronSourceWebView.REQUEST_ID));
            String params = resultArr[0];
            if (((Boolean) resultArr[1]).booleanValue()) {
                if (!TextUtils.isEmpty(failFunToCall)) {
                    funToCall = failFunToCall;
                }
            } else if (!TextUtils.isEmpty(successFunToCall)) {
                funToCall = successFunToCall;
            }
            if (!TextUtils.isEmpty(funToCall)) {
                IronSourceWebView.this.injectJavascript(IronSourceWebView.this.generateJSToInject(funToCall, params, JSMethods.ON_CHECK_INSTALLED_APPS_SUCCESS, JSMethods.ON_CHECK_INSTALLED_APPS_FAIL));
            }
        }

        @JavascriptInterface
        public void saveFile(String value) {
            Logger.i(IronSourceWebView.this.TAG, "saveFile(" + value + ")");
            SSAFile ssaFile = new SSAFile(value);
            if (DeviceStatus.getAvailableMemorySizeInMegaBytes(IronSourceWebView.this.mCacheDirectory) <= 0) {
                IronSourceWebView.this.responseBack(value, false, DownloadManager.NO_DISK_SPACE, null);
            } else if (!SDKUtils.isExternalStorageAvailable()) {
                IronSourceWebView.this.responseBack(value, false, DownloadManager.STORAGE_UNAVAILABLE, null);
            } else if (IronSourceStorageUtils.isFileCached(IronSourceWebView.this.mCacheDirectory, ssaFile)) {
                IronSourceWebView.this.responseBack(value, false, DownloadManager.FILE_ALREADY_EXIST, null);
            } else if (ConnectivityService.isConnected(IronSourceWebView.this.getContext())) {
                IronSourceWebView.this.responseBack(value, true, null, null);
                String lastUpdateTimeObj = ssaFile.getLastUpdateTime();
                if (lastUpdateTimeObj != null) {
                    String lastUpdateTimeStr = String.valueOf(lastUpdateTimeObj);
                    if (!TextUtils.isEmpty(lastUpdateTimeStr)) {
                        String folder;
                        String path = ssaFile.getPath();
                        if (path.contains("/")) {
                            String[] splitArr = ssaFile.getPath().split("/");
                            folder = splitArr[splitArr.length - 1];
                        } else {
                            folder = path;
                        }
                        IronSourceSharedPrefHelper.getSupersonicPrefHelper().setCampaignLastUpdate(folder, lastUpdateTimeStr);
                    }
                }
                IronSourceWebView.this.downloadManager.downloadFile(ssaFile);
            } else {
                IronSourceWebView.this.responseBack(value, false, DownloadManager.NO_NETWORK_CONNECTION, null);
            }
        }

        @JavascriptInterface
        public void adUnitsReady(String value) {
            Logger.i(IronSourceWebView.this.TAG, "adUnitsReady(" + value + ")");
            final String demandSourceName = new SSAObj(value).getString(RequestParameters.DEMAND_SOURCE_NAME);
            final AdUnitsReady adUnitsReady = new AdUnitsReady(value);
            if (adUnitsReady.isNumOfAdUnitsExist()) {
                IronSourceWebView.this.responseBack(value, true, null, null);
                final String product = adUnitsReady.getProductType();
                if (IronSourceWebView.this.shouldNotifyDeveloper(product)) {
                    IronSourceWebView.this.runOnUiThread(new Runnable() {
                        public void run() {
                            boolean fireSuccess;
                            if (Integer.parseInt(adUnitsReady.getNumOfAdUnits()) > 0) {
                                fireSuccess = true;
                            } else {
                                fireSuccess = false;
                            }
                            if (!product.equalsIgnoreCase(ProductType.RewardedVideo.toString())) {
                                return;
                            }
                            if (fireSuccess) {
                                Log.d(IronSourceWebView.this.TAG, "onRVInitSuccess()");
                                IronSourceWebView.this.mOnRewardedVideoListener.onRVInitSuccess(adUnitsReady, demandSourceName);
                                return;
                            }
                            IronSourceWebView.this.mOnRewardedVideoListener.onRVNoMoreOffers(demandSourceName);
                        }
                    });
                    return;
                }
                return;
            }
            IronSourceWebView.this.responseBack(value, false, ErrorCodes.NUM_OF_AD_UNITS_DO_NOT_EXIST, null);
        }

        @JavascriptInterface
        public void deleteFolder(String value) {
            Logger.i(IronSourceWebView.this.TAG, "deleteFolder(" + value + ")");
            SSAFile file = new SSAFile(value);
            if (IronSourceStorageUtils.isPathExist(IronSourceWebView.this.mCacheDirectory, file.getPath())) {
                IronSourceWebView.this.responseBack(value, IronSourceStorageUtils.deleteFolder(IronSourceWebView.this.mCacheDirectory, file.getPath()), null, null);
                return;
            }
            IronSourceWebView.this.responseBack(value, false, ErrorCodes.FOLDER_NOT_EXIST_MSG, ErrorCodes.FOLDER_NOT_EXIST_CODE);
        }

        @JavascriptInterface
        public void deleteFile(String value) {
            Logger.i(IronSourceWebView.this.TAG, "deleteFile(" + value + ")");
            SSAFile file = new SSAFile(value);
            if (IronSourceStorageUtils.isPathExist(IronSourceWebView.this.mCacheDirectory, file.getPath())) {
                IronSourceWebView.this.responseBack(value, IronSourceStorageUtils.deleteFile(IronSourceWebView.this.mCacheDirectory, file.getPath(), file.getFile()), null, null);
                return;
            }
            IronSourceWebView.this.responseBack(value, false, ErrorCodes.FILE_NOT_EXIST_MSG, ErrorCodes.FOLDER_NOT_EXIST_CODE);
        }

        @JavascriptInterface
        public void displayWebView(String value) {
            Logger.i(IronSourceWebView.this.TAG, "displayWebView(" + value + ")");
            IronSourceWebView.this.responseBack(value, true, null, null);
            SSAObj ssaObj = new SSAObj(value);
            boolean display = ((Boolean) ssaObj.get(ParametersKeys.DISPLAY)).booleanValue();
            String productType = ssaObj.getString(ParametersKeys.PRODUCT_TYPE);
            boolean isStandaloneView = ssaObj.getBoolean(ParametersKeys.IS_STANDALONE_VIEW);
            String demandSourceName = ssaObj.getString(RequestParameters.DEMAND_SOURCE_NAME);
            boolean isRewardedVideo = false;
            if (display) {
                IronSourceWebView.this.mIsImmersive = ssaObj.getBoolean(ParametersKeys.IMMERSIVE);
                IronSourceWebView.this.mIsActivityThemeTranslucent = ssaObj.getBoolean(ParametersKeys.ACTIVITY_THEME_TRANSLUCENT);
                if (IronSourceWebView.this.getState() != State.Display) {
                    IronSourceWebView.this.setState(State.Display);
                    Logger.i(IronSourceWebView.this.TAG, "State: " + IronSourceWebView.this.mState);
                    Context context = IronSourceWebView.this.getCurrentActivityContext();
                    String orientation = IronSourceWebView.this.getOrientationState();
                    int rotation = DeviceStatus.getApplicationRotation(context);
                    if (isStandaloneView) {
                        ControllerView controllerView = new ControllerView(context);
                        controllerView.addView(IronSourceWebView.this.mControllerLayout);
                        controllerView.showInterstitial(IronSourceWebView.this);
                        return;
                    }
                    Intent intent;
                    if (IronSourceWebView.this.mIsActivityThemeTranslucent) {
                        intent = new Intent(context, InterstitialActivity.class);
                    } else {
                        intent = new Intent(context, ControllerActivity.class);
                    }
                    if (ProductType.RewardedVideo.toString().equalsIgnoreCase(productType)) {
                        if (ParametersKeys.ORIENTATION_APPLICATION.equals(orientation)) {
                            orientation = SDKUtils.translateRequestedOrientation(DeviceStatus.getActivityRequestedOrientation(IronSourceWebView.this.getCurrentActivityContext()));
                        }
                        isRewardedVideo = true;
                        intent.putExtra(ParametersKeys.PRODUCT_TYPE, ProductType.RewardedVideo.toString());
                        IronSourceWebView.this.mSavedState.adOpened(ProductType.RewardedVideo.ordinal());
                        IronSourceWebView.this.mSavedState.setDisplayedDemandSourceName(demandSourceName);
                    } else if (ProductType.OfferWall.toString().equalsIgnoreCase(productType)) {
                        intent.putExtra(ParametersKeys.PRODUCT_TYPE, ProductType.OfferWall.toString());
                        IronSourceWebView.this.mSavedState.adOpened(ProductType.OfferWall.ordinal());
                    }
                    if (isRewardedVideo && IronSourceWebView.this.shouldNotifyDeveloper(ProductType.RewardedVideo.toString())) {
                        IronSourceWebView.this.mOnRewardedVideoListener.onRVAdOpened(demandSourceName);
                    }
                    intent.setFlags(536870912);
                    intent.putExtra(ParametersKeys.IMMERSIVE, IronSourceWebView.this.mIsImmersive);
                    intent.putExtra(ParametersKeys.ORIENTATION_SET_FLAG, orientation);
                    intent.putExtra(ParametersKeys.ROTATION_SET_FLAG, rotation);
                    context.startActivity(intent);
                    return;
                }
                Logger.i(IronSourceWebView.this.TAG, "State: " + IronSourceWebView.this.mState);
                return;
            }
            IronSourceWebView.this.setState(State.Gone);
            IronSourceWebView.this.closeWebView();
        }

        @JavascriptInterface
        public void getOrientation(String value) {
            String funToCall = IronSourceWebView.this.extractSuccessFunctionToCall(value);
            String params = SDKUtils.getOrientation(IronSourceWebView.this.getCurrentActivityContext()).toString();
            if (!TextUtils.isEmpty(funToCall)) {
                IronSourceWebView.this.injectJavascript(IronSourceWebView.this.generateJSToInject(funToCall, params, JSMethods.ON_GET_ORIENTATION_SUCCESS, JSMethods.ON_GET_ORIENTATION_FAIL));
            }
        }

        @JavascriptInterface
        public void setOrientation(String value) {
            Logger.i(IronSourceWebView.this.TAG, "setOrientation(" + value + ")");
            String orientation = new SSAObj(value).getString(AdUnitActivity.EXTRA_ORIENTATION);
            IronSourceWebView.this.setOrientationState(orientation);
            int rotation = DeviceStatus.getApplicationRotation(IronSourceWebView.this.getCurrentActivityContext());
            if (IronSourceWebView.this.mChangeListener != null) {
                IronSourceWebView.this.mChangeListener.onOrientationChanged(orientation, rotation);
            }
        }

        @JavascriptInterface
        public void getCachedFilesMap(String value) {
            Logger.i(IronSourceWebView.this.TAG, "getCachedFilesMap(" + value + ")");
            String funToCall = IronSourceWebView.this.extractSuccessFunctionToCall(value);
            if (!TextUtils.isEmpty(funToCall)) {
                SSAObj ssaObj = new SSAObj(value);
                if (ssaObj.containsKey(ClientCookie.PATH_ATTR)) {
                    String mapPath = (String) ssaObj.get(ClientCookie.PATH_ATTR);
                    if (IronSourceStorageUtils.isPathExist(IronSourceWebView.this.mCacheDirectory, mapPath)) {
                        IronSourceWebView.this.injectJavascript(IronSourceWebView.this.generateJSToInject(funToCall, IronSourceStorageUtils.getCachedFilesMap(IronSourceWebView.this.mCacheDirectory, mapPath), JSMethods.ON_GET_CACHED_FILES_MAP_SUCCESS, JSMethods.ON_GET_CACHED_FILES_MAP_FAIL));
                        return;
                    }
                    IronSourceWebView.this.responseBack(value, false, ErrorCodes.PATH_FILE_DOES_NOT_EXIST_ON_DISK, null);
                    return;
                }
                IronSourceWebView.this.responseBack(value, false, ErrorCodes.PATH_KEY_DOES_NOT_EXIST, null);
            }
        }

        @JavascriptInterface
        public void adCredited(String value) {
            String appKey;
            String userId;
            Log.d(IronSourceWebView.this.PUB_TAG, "adCredited(" + value + ")");
            SSAObj sSAObj = new SSAObj(value);
            String creditsStr = sSAObj.getString(ParametersKeys.CREDITS);
            final int credits = creditsStr != null ? Integer.parseInt(creditsStr) : 0;
            String totalCreditsStr = sSAObj.getString(ParametersKeys.TOTAL);
            final int totalCredits = totalCreditsStr != null ? Integer.parseInt(totalCreditsStr) : 0;
            final String demandSourceName = sSAObj.getString(RequestParameters.DEMAND_SOURCE_NAME);
            final String product = sSAObj.getString(ParametersKeys.PRODUCT_TYPE);
            boolean totalCreditsFlag = false;
            String latestCompeltionsTime = null;
            boolean md5Signature = false;
            if (sSAObj.getBoolean("externalPoll")) {
                appKey = IronSourceWebView.this.mOWCreditsAppKey;
                userId = IronSourceWebView.this.mOWCreditsUserId;
            } else {
                appKey = IronSourceWebView.this.mOWAppKey;
                userId = IronSourceWebView.this.mOWUserId;
            }
            if (product.equalsIgnoreCase(ProductType.OfferWall.toString())) {
                if (!sSAObj.isNull("signature")) {
                    if (!sSAObj.isNull(EventEntry.COLUMN_NAME_TIMESTAMP)) {
                        if (!sSAObj.isNull("totalCreditsFlag")) {
                            if (sSAObj.getString("signature").equalsIgnoreCase(SDKUtils.getMD5(totalCreditsStr + appKey + userId))) {
                                md5Signature = true;
                            } else {
                                IronSourceWebView.this.responseBack(value, false, "Controller signature is not equal to SDK signature", null);
                            }
                            totalCreditsFlag = sSAObj.getBoolean("totalCreditsFlag");
                            latestCompeltionsTime = sSAObj.getString(EventEntry.COLUMN_NAME_TIMESTAMP);
                        }
                    }
                }
                IronSourceWebView.this.responseBack(value, false, "One of the keys are missing: signature/timestamp/totalCreditsFlag", null);
                return;
            }
            if (IronSourceWebView.this.shouldNotifyDeveloper(product)) {
                final boolean mTotalCreditsFlag = totalCreditsFlag;
                final String mlatestCompeltionsTime = latestCompeltionsTime;
                final boolean mMd5Signature = md5Signature;
                final String str = value;
                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        if (product.equalsIgnoreCase(ProductType.RewardedVideo.toString())) {
                            IronSourceWebView.this.mOnRewardedVideoListener.onRVAdCredited(credits, demandSourceName);
                        } else if (!product.equalsIgnoreCase(ProductType.OfferWall.toString()) || !mMd5Signature || !IronSourceWebView.this.mOnOfferWallListener.onOWAdCredited(credits, totalCredits, mTotalCreditsFlag) || TextUtils.isEmpty(mlatestCompeltionsTime)) {
                        } else {
                            if (IronSourceSharedPrefHelper.getSupersonicPrefHelper().setLatestCompeltionsTime(mlatestCompeltionsTime, appKey, userId)) {
                                IronSourceWebView.this.responseBack(str, true, null, null);
                            } else {
                                IronSourceWebView.this.responseBack(str, false, "Time Stamp could not be stored", null);
                            }
                        }
                    }
                });
            }
        }

        @JavascriptInterface
        public void removeCloseEventHandler(String value) {
            Logger.i(IronSourceWebView.this.TAG, "removeCloseEventHandler(" + value + ")");
            if (IronSourceWebView.this.mCloseEventTimer != null) {
                IronSourceWebView.this.mCloseEventTimer.cancel();
            }
            IronSourceWebView.this.isRemoveCloseEventHandler = true;
        }

        @JavascriptInterface
        public void onGetDeviceStatusSuccess(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onGetDeviceStatusSuccess(" + value + ")");
            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg(JSMethods.ON_GET_DEVICE_STATUS_SUCCESS, value);
        }

        @JavascriptInterface
        public void onGetDeviceStatusFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onGetDeviceStatusFail(" + value + ")");
            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg(JSMethods.ON_GET_DEVICE_STATUS_FAIL, value);
        }

        @JavascriptInterface
        public void onInitRewardedVideoSuccess(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onInitRewardedVideoSuccess(" + value + ")");
            IronSourceSharedPrefHelper.getSupersonicPrefHelper().setSSABCParameters(new SSABCParameters(value));
            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg(JSMethods.ON_INIT_REWARDED_VIDEO_SUCCESS, value);
        }

        @JavascriptInterface
        public void onInitRewardedVideoFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onInitRewardedVideoFail(" + value + ")");
            SSAObj ssaObj = new SSAObj(value);
            final String message = ssaObj.getString(ParametersKeys.ERR_MSG);
            final String demandSourceName = ssaObj.getString(RequestParameters.DEMAND_SOURCE_NAME);
            DemandSource demandSource = IronSourceAdsPublisherAgent.getInstance((Activity) IronSourceWebView.this.getCurrentActivityContext()).getDemandSourceByName(demandSourceName);
            if (demandSource != null) {
                demandSource.setDemandSourceInitState(3);
            }
            if (IronSourceWebView.this.shouldNotifyDeveloper(ProductType.RewardedVideo.toString())) {
                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        String toSend = message;
                        if (toSend == null) {
                            toSend = "We're sorry, some error occurred. we will investigate it";
                        }
                        Log.d(IronSourceWebView.this.TAG, "onRVInitFail(message:" + message + ")");
                        IronSourceWebView.this.mOnRewardedVideoListener.onRVInitFail(toSend, demandSourceName);
                    }
                });
            }
            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg(JSMethods.ON_INIT_REWARDED_VIDEO_FAIL, value);
        }

        @JavascriptInterface
        public void onGetApplicationInfoSuccess(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onGetApplicationInfoSuccess(" + value + ")");
            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg(JSMethods.ON_GET_APPLICATION_INFO_SUCCESS, value);
        }

        @JavascriptInterface
        public void onGetApplicationInfoFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onGetApplicationInfoFail(" + value + ")");
            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg(JSMethods.ON_GET_APPLICATION_INFO_FAIL, value);
        }

        @JavascriptInterface
        public void onShowRewardedVideoSuccess(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onShowRewardedVideoSuccess(" + value + ")");
            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg(JSMethods.ON_SHOW_REWARDED_VIDEO_SUCCESS, value);
        }

        @JavascriptInterface
        public void onShowRewardedVideoFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onShowRewardedVideoFail(" + value + ")");
            SSAObj ssaObj = new SSAObj(value);
            final String message = ssaObj.getString(ParametersKeys.ERR_MSG);
            final String demandSourceName = ssaObj.getString(RequestParameters.DEMAND_SOURCE_NAME);
            if (IronSourceWebView.this.shouldNotifyDeveloper(ProductType.RewardedVideo.toString())) {
                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        String toSend = message;
                        if (toSend == null) {
                            toSend = "We're sorry, some error occurred. we will investigate it";
                        }
                        Log.d(IronSourceWebView.this.TAG, "onRVShowFail(message:" + message + ")");
                        IronSourceWebView.this.mOnRewardedVideoListener.onRVShowFail(toSend, demandSourceName);
                    }
                });
            }
            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg(JSMethods.ON_SHOW_REWARDED_VIDEO_FAIL, value);
        }

        @JavascriptInterface
        public void onGetCachedFilesMapSuccess(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onGetCachedFilesMapSuccess(" + value + ")");
            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg(JSMethods.ON_GET_CACHED_FILES_MAP_SUCCESS, value);
        }

        @JavascriptInterface
        public void onGetCachedFilesMapFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onGetCachedFilesMapFail(" + value + ")");
            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg(JSMethods.ON_GET_CACHED_FILES_MAP_FAIL, value);
        }

        @JavascriptInterface
        public void onShowOfferWallSuccess(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onShowOfferWallSuccess(" + value + ")");
            IronSourceWebView.this.mSavedState.adOpened(ProductType.OfferWall.ordinal());
            final String placementId = SDKUtils.getValueFromJsonObject(value, Constants.PLACEMENT_ID);
            if (IronSourceWebView.this.shouldNotifyDeveloper(ProductType.OfferWall.toString())) {
                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        IronSourceWebView.this.mOnOfferWallListener.onOWShowSuccess(placementId);
                    }
                });
            }
            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg(JSMethods.ON_SHOW_OFFER_WALL_SUCCESS, value);
        }

        @JavascriptInterface
        public void onShowOfferWallFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onShowOfferWallFail(" + value + ")");
            final String message = new SSAObj(value).getString(ParametersKeys.ERR_MSG);
            if (IronSourceWebView.this.shouldNotifyDeveloper(ProductType.OfferWall.toString())) {
                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        String toSend = message;
                        if (toSend == null) {
                            toSend = "We're sorry, some error occurred. we will investigate it";
                        }
                        IronSourceWebView.this.mOnOfferWallListener.onOWShowFail(toSend);
                    }
                });
            }
            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg(JSMethods.ON_SHOW_OFFER_WALL_FAIL, value);
        }

        @JavascriptInterface
        public void onInitInterstitialSuccess(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onInitInterstitialSuccess()");
            IronSourceWebView.this.toastingErrMsg(JSMethods.ON_INIT_INTERSTITIAL_SUCCESS, ServerProtocol.DIALOG_RETURN_SCOPES_TRUE);
            IronSourceWebView.this.mSavedState.setInterstitialInitSuccess(true);
            if (IronSourceWebView.this.mSavedState.reportInitInterstitial()) {
                IronSourceWebView.this.mSavedState.setReportInitInterstitial(false);
                if (IronSourceWebView.this.shouldNotifyDeveloper(ProductType.Interstitial.toString())) {
                    IronSourceWebView.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Log.d(IronSourceWebView.this.TAG, "onInterstitialInitSuccess()");
                            IronSourceWebView.this.mOnInitInterstitialListener.onInterstitialInitSuccess();
                        }
                    });
                }
            }
        }

        @JavascriptInterface
        public void onInitInterstitialFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onInitInterstitialFail(" + value + ")");
            IronSourceWebView.this.mSavedState.setInterstitialInitSuccess(false);
            final String message = new SSAObj(value).getString(ParametersKeys.ERR_MSG);
            if (IronSourceWebView.this.mSavedState.reportInitInterstitial()) {
                IronSourceWebView.this.mSavedState.setReportInitInterstitial(false);
                if (IronSourceWebView.this.shouldNotifyDeveloper(ProductType.Interstitial.toString())) {
                    IronSourceWebView.this.runOnUiThread(new Runnable() {
                        public void run() {
                            String toSend = message;
                            if (toSend == null) {
                                toSend = "We're sorry, some error occurred. we will investigate it";
                            }
                            Log.d(IronSourceWebView.this.TAG, "onInterstitialInitFail(message:" + toSend + ")");
                            IronSourceWebView.this.mOnInitInterstitialListener.onInterstitialInitFailed(toSend);
                        }
                    });
                }
            }
            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg(JSMethods.ON_INIT_INTERSTITIAL_FAIL, value);
        }

        private void setInterstitialAvailability(boolean isAvailable) {
            IronSourceWebView.this.mIsInterstitialAvailable = Boolean.valueOf(isAvailable);
            if (IronSourceWebView.this.shouldNotifyDeveloper(ProductType.Interstitial.toString())) {
                IronSourceWebView.this.toastingErrMsg(JSMethods.ON_INTERSTITIAL_AVAILABILITY, String.valueOf(IronSourceWebView.this.mIsInterstitialAvailable));
            }
        }

        @JavascriptInterface
        public void adClicked(String value) {
            Logger.i(IronSourceWebView.this.TAG, "adClicked(" + value + ")");
            SSAObj ssaObj = new SSAObj(value);
            String productType = ssaObj.getString(ParametersKeys.PRODUCT_TYPE);
            if (productType.equalsIgnoreCase(ProductType.Interstitial.toString()) && IronSourceWebView.this.shouldNotifyDeveloper(ProductType.Interstitial.toString())) {
                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        IronSourceWebView.this.mOnInitInterstitialListener.onInterstitialClick();
                    }
                });
            } else if (productType.equalsIgnoreCase(ProductType.RewardedVideo.toString()) && IronSourceWebView.this.shouldNotifyDeveloper(ProductType.RewardedVideo.toString())) {
                final String demandSourceName = ssaObj.getString(RequestParameters.DEMAND_SOURCE_NAME);
                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        IronSourceWebView.this.mOnRewardedVideoListener.onRVAdClicked(demandSourceName);
                    }
                });
            }
        }

        @JavascriptInterface
        public void onShowInterstitialSuccess(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onShowInterstitialSuccess(" + value + ")");
            IronSourceWebView.this.mSavedState.adOpened(ProductType.Interstitial.ordinal());
            IronSourceWebView.this.responseBack(value, true, null, null);
            if (IronSourceWebView.this.shouldNotifyDeveloper(ProductType.Interstitial.toString())) {
                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        IronSourceWebView.this.mOnInitInterstitialListener.onInterstitialOpen();
                        IronSourceWebView.this.mOnInitInterstitialListener.onInterstitialShowSuccess();
                    }
                });
                IronSourceWebView.this.toastingErrMsg(JSMethods.ON_SHOW_INTERSTITIAL_SUCCESS, value);
            }
            setInterstitialAvailability(false);
        }

        @JavascriptInterface
        public void onInitOfferWallSuccess(String value) {
            IronSourceWebView.this.toastingErrMsg(JSMethods.ON_INIT_OFFERWALL_SUCCESS, ServerProtocol.DIALOG_RETURN_SCOPES_TRUE);
            IronSourceWebView.this.mSavedState.setOfferwallInitSuccess(true);
            if (IronSourceWebView.this.mSavedState.reportInitOfferwall()) {
                IronSourceWebView.this.mSavedState.setOfferwallReportInit(false);
                if (IronSourceWebView.this.shouldNotifyDeveloper(ProductType.OfferWall.toString())) {
                    IronSourceWebView.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Log.d(IronSourceWebView.this.TAG, "onOfferWallInitSuccess()");
                            IronSourceWebView.this.mOnOfferWallListener.onOfferwallInitSuccess();
                        }
                    });
                }
            }
        }

        @JavascriptInterface
        public void onInitOfferWallFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onInitOfferWallFail(" + value + ")");
            IronSourceWebView.this.mSavedState.setOfferwallInitSuccess(false);
            final String message = new SSAObj(value).getString(ParametersKeys.ERR_MSG);
            if (IronSourceWebView.this.mSavedState.reportInitOfferwall()) {
                IronSourceWebView.this.mSavedState.setOfferwallReportInit(false);
                if (IronSourceWebView.this.shouldNotifyDeveloper(ProductType.OfferWall.toString())) {
                    IronSourceWebView.this.runOnUiThread(new Runnable() {
                        public void run() {
                            String toSend = message;
                            if (toSend == null) {
                                toSend = "We're sorry, some error occurred. we will investigate it";
                            }
                            Log.d(IronSourceWebView.this.TAG, "onOfferWallInitFail(message:" + toSend + ")");
                            IronSourceWebView.this.mOnOfferWallListener.onOfferwallInitFail(toSend);
                        }
                    });
                }
            }
            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg(JSMethods.ON_INIT_OFFERWALL_FAIL, value);
        }

        @JavascriptInterface
        public void onLoadInterstitialSuccess(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onLoadInterstitialSuccess(" + value + ")");
            setInterstitialAvailability(true);
            IronSourceWebView.this.responseBack(value, true, null, null);
            if (IronSourceWebView.this.shouldNotifyDeveloper(ProductType.Interstitial.toString())) {
                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        IronSourceWebView.this.mOnInitInterstitialListener.onInterstitialLoadSuccess();
                    }
                });
            }
            IronSourceWebView.this.toastingErrMsg(JSMethods.ON_LOAD_INTERSTITIAL_SUCCESS, ServerProtocol.DIALOG_RETURN_SCOPES_TRUE);
        }

        @JavascriptInterface
        public void onLoadInterstitialFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onLoadInterstitialFail(" + value + ")");
            final String message = new SSAObj(value).getString(ParametersKeys.ERR_MSG);
            IronSourceWebView.this.responseBack(value, true, null, null);
            if (IronSourceWebView.this.shouldNotifyDeveloper(ProductType.Interstitial.toString())) {
                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        String toSend = message;
                        if (toSend == null) {
                            toSend = "We're sorry, some error occurred. we will investigate it";
                        }
                        IronSourceWebView.this.mOnInitInterstitialListener.onInterstitialLoadFailed(toSend);
                    }
                });
            }
            IronSourceWebView.this.toastingErrMsg(JSMethods.ON_LOAD_INTERSTITIAL_FAIL, ServerProtocol.DIALOG_RETURN_SCOPES_TRUE);
        }

        @JavascriptInterface
        public void onShowInterstitialFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onShowInterstitialFail(" + value + ")");
            setInterstitialAvailability(false);
            final String message = new SSAObj(value).getString(ParametersKeys.ERR_MSG);
            IronSourceWebView.this.responseBack(value, true, null, null);
            if (IronSourceWebView.this.shouldNotifyDeveloper(ProductType.Interstitial.toString())) {
                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        String toSend = message;
                        if (toSend == null) {
                            toSend = "We're sorry, some error occurred. we will investigate it";
                        }
                        IronSourceWebView.this.mOnInitInterstitialListener.onInterstitialShowFailed(toSend);
                    }
                });
            }
            IronSourceWebView.this.toastingErrMsg(JSMethods.ON_SHOW_INTERSTITIAL_FAIL, value);
        }

        @JavascriptInterface
        public void onGenericFunctionSuccess(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onGenericFunctionSuccess(" + value + ")");
            if (IronSourceWebView.this.mOnGenericFunctionListener == null) {
                Logger.d(IronSourceWebView.this.TAG, "genericFunctionListener was not found");
                return;
            }
            IronSourceWebView.this.runOnUiThread(new Runnable() {
                public void run() {
                    IronSourceWebView.this.mOnGenericFunctionListener.onGFSuccess();
                }
            });
            IronSourceWebView.this.responseBack(value, true, null, null);
        }

        @JavascriptInterface
        public void onGenericFunctionFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onGenericFunctionFail(" + value + ")");
            if (IronSourceWebView.this.mOnGenericFunctionListener == null) {
                Logger.d(IronSourceWebView.this.TAG, "genericFunctionListener was not found");
                return;
            }
            final String message = new SSAObj(value).getString(ParametersKeys.ERR_MSG);
            IronSourceWebView.this.runOnUiThread(new Runnable() {
                public void run() {
                    IronSourceWebView.this.mOnGenericFunctionListener.onGFFail(message);
                }
            });
            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg(JSMethods.ON_GENERIC_FUNCTION_FAIL, value);
        }

        @JavascriptInterface
        public void createCalendarEvent(String value) {
            Logger.i(IronSourceWebView.this.TAG, "createCalendarEvent(" + value + ")");
            try {
                JSONObject jsObj = new JSONObject();
                JSONObject jsRecurrence = new JSONObject();
                jsRecurrence.put("frequency", "weekly");
                jsObj.put(ShareConstants.WEB_DIALOG_PARAM_ID, "testevent723GDf84");
                jsObj.put(ShareConstants.WEB_DIALOG_PARAM_DESCRIPTION, "Watch this crazy showInterstitial on cannel 5!");
                jsObj.put("start", "2014-02-01T20:00:00-8:00");
                jsObj.put("end", "2014-06-30T20:00:00-8:00");
                jsObj.put(ParametersKeys.VIDEO_STATUS, "pending");
                jsObj.put("recurrence", jsRecurrence.toString());
                jsObj.put("reminder", "2014-02-01T19:50:00-8:00");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void openUrl(String value) {
            Logger.i(IronSourceWebView.this.TAG, "openUrl(" + value + ")");
            SSAObj ssaObj = new SSAObj(value);
            String url = ssaObj.getString(ParametersKeys.URL);
            String method = ssaObj.getString(ParametersKeys.METHOD);
            Context context = IronSourceWebView.this.getCurrentActivityContext();
            try {
                if (method.equalsIgnoreCase(ParametersKeys.EXTERNAL_BROWSER)) {
                    UrlHandler.openUrl(context, url);
                } else if (method.equalsIgnoreCase(ParametersKeys.WEB_VIEW)) {
                    intent = new Intent(context, OpenUrlActivity.class);
                    intent.putExtra(IronSourceWebView.EXTERNAL_URL, url);
                    intent.putExtra(IronSourceWebView.SECONDARY_WEB_VIEW, true);
                    intent.putExtra(ParametersKeys.IMMERSIVE, IronSourceWebView.this.mIsImmersive);
                    context.startActivity(intent);
                } else if (method.equalsIgnoreCase(ParametersKeys.STORE)) {
                    intent = new Intent(context, OpenUrlActivity.class);
                    intent.putExtra(IronSourceWebView.EXTERNAL_URL, url);
                    intent.putExtra(IronSourceWebView.IS_STORE, true);
                    intent.putExtra(IronSourceWebView.SECONDARY_WEB_VIEW, true);
                    context.startActivity(intent);
                }
            } catch (Exception ex) {
                IronSourceWebView.this.responseBack(value, false, ex.getMessage(), null);
                ex.printStackTrace();
            }
        }

        @JavascriptInterface
        public void setForceClose(String value) {
            Logger.i(IronSourceWebView.this.TAG, "setForceClose(" + value + ")");
            SSAObj ssaObj = new SSAObj(value);
            String width = ssaObj.getString(RequestParameters.WIDTH);
            String hight = ssaObj.getString(RequestParameters.HEIGHT);
            IronSourceWebView.this.mHiddenForceCloseWidth = Integer.parseInt(width);
            IronSourceWebView.this.mHiddenForceCloseHeight = Integer.parseInt(hight);
            IronSourceWebView.this.mHiddenForceCloseLocation = ssaObj.getString(ParametersKeys.POSITION);
        }

        @JavascriptInterface
        public void setBackButtonState(String value) {
            Logger.i(IronSourceWebView.this.TAG, "setBackButtonState(" + value + ")");
            IronSourceSharedPrefHelper.getSupersonicPrefHelper().setBackButtonState(new SSAObj(value).getString(Constants.RESTORED_STATE));
        }

        @JavascriptInterface
        public void setStoreSearchKeys(String value) {
            Logger.i(IronSourceWebView.this.TAG, "setStoreSearchKeys(" + value + ")");
            IronSourceSharedPrefHelper.getSupersonicPrefHelper().setSearchKeys(value);
        }

        @JavascriptInterface
        public void setWebviewBackgroundColor(String value) {
            Logger.i(IronSourceWebView.this.TAG, "setWebviewBackgroundColor(" + value + ")");
            IronSourceWebView.this.setWebviewBackground(value);
        }

        @JavascriptInterface
        public void toggleUDIA(String value) {
            Logger.i(IronSourceWebView.this.TAG, "toggleUDIA(" + value + ")");
            SSAObj ssaObj = new SSAObj(value);
            if (ssaObj.containsKey(ParametersKeys.TOGGLE)) {
                int toggle = Integer.parseInt(ssaObj.getString(ParametersKeys.TOGGLE));
                if (toggle != 0) {
                    String binaryToggle = Integer.toBinaryString(toggle);
                    if (TextUtils.isEmpty(binaryToggle)) {
                        IronSourceWebView.this.responseBack(value, false, ErrorCodes.FIALED_TO_CONVERT_TOGGLE, null);
                        return;
                    } else if (binaryToggle.toCharArray()[3] == '0') {
                        IronSourceSharedPrefHelper.getSupersonicPrefHelper().setShouldRegisterSessions(true);
                        return;
                    } else {
                        IronSourceSharedPrefHelper.getSupersonicPrefHelper().setShouldRegisterSessions(false);
                        return;
                    }
                }
                return;
            }
            IronSourceWebView.this.responseBack(value, false, ErrorCodes.TOGGLE_KEY_DOES_NOT_EXIST, null);
        }

        @JavascriptInterface
        public void getUDIA(String value) {
            this.udiaResults = 0;
            Logger.i(IronSourceWebView.this.TAG, "getUDIA(" + value + ")");
            String funToCall = IronSourceWebView.this.extractSuccessFunctionToCall(value);
            SSAObj ssaObj = new SSAObj(value);
            if (ssaObj.containsKey(ParametersKeys.GET_BY_FLAG)) {
                int getByFlag = Integer.parseInt(ssaObj.getString(ParametersKeys.GET_BY_FLAG));
                if (getByFlag != 0) {
                    String binaryToggle = Integer.toBinaryString(getByFlag);
                    if (TextUtils.isEmpty(binaryToggle)) {
                        IronSourceWebView.this.responseBack(value, false, ErrorCodes.FIALED_TO_CONVERT_GET_BY_FLAG, null);
                        return;
                    }
                    JSONObject jsObj;
                    char[] binaryToggleArr = new StringBuilder(binaryToggle).reverse().toString().toCharArray();
                    JSONArray jsArr = new JSONArray();
                    if (binaryToggleArr[3] == '0') {
                        jsObj = new JSONObject();
                        try {
                            jsObj.put("sessions", IronSourceSharedPrefHelper.getSupersonicPrefHelper().getSessions());
                            IronSourceSharedPrefHelper.getSupersonicPrefHelper().deleteSessions();
                            jsArr.put(jsObj);
                        } catch (JSONException e) {
                        }
                    }
                    if (binaryToggleArr[2] == '1') {
                        this.udiaResults++;
                        Location location = LocationService.getLastLocation(IronSourceWebView.this.getContext());
                        if (location != null) {
                            jsObj = new JSONObject();
                            try {
                                jsObj.put("latitude", location.getLatitude());
                                jsObj.put("longitude", location.getLongitude());
                                jsArr.put(jsObj);
                                this.udiaResults--;
                                sendResults(funToCall, jsArr);
                                Logger.i(IronSourceWebView.this.TAG, "done location");
                                return;
                            } catch (JSONException e2) {
                                return;
                            }
                        }
                        this.udiaResults--;
                        return;
                    }
                    return;
                }
                return;
            }
            IronSourceWebView.this.responseBack(value, false, ErrorCodes.GET_BY_FLAG_KEY_DOES_NOT_EXIST, null);
        }

        private void sendResults(String funToCall, JSONArray jsArr) {
            Logger.i(IronSourceWebView.this.TAG, "sendResults: " + this.udiaResults);
            if (this.udiaResults <= 0) {
                injectGetUDIA(funToCall, jsArr);
            }
        }

        @JavascriptInterface
        public void onUDIASuccess(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onUDIASuccess(" + value + ")");
        }

        @JavascriptInterface
        public void onUDIAFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onUDIAFail(" + value + ")");
        }

        @JavascriptInterface
        public void onGetUDIASuccess(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onGetUDIASuccess(" + value + ")");
        }

        @JavascriptInterface
        public void onGetUDIAFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onGetUDIAFail(" + value + ")");
        }

        @JavascriptInterface
        public void setUserUniqueId(String value) {
            Logger.i(IronSourceWebView.this.TAG, "setUserUniqueId(" + value + ")");
            SSAObj ssaObj = new SSAObj(value);
            if (ssaObj.containsKey(ParametersKeys.USER_UNIQUE_ID) && ssaObj.containsKey(ParametersKeys.PRODUCT_TYPE)) {
                if (IronSourceSharedPrefHelper.getSupersonicPrefHelper().setUniqueId(ssaObj.getString(ParametersKeys.USER_UNIQUE_ID), ssaObj.getString(ParametersKeys.PRODUCT_TYPE))) {
                    IronSourceWebView.this.responseBack(value, true, null, null);
                    return;
                } else {
                    IronSourceWebView.this.responseBack(value, false, ErrorCodes.SET_USER_UNIQUE_ID_FAILED, null);
                    return;
                }
            }
            IronSourceWebView.this.responseBack(value, false, ErrorCodes.UNIQUE_ID_OR_PRODUCT_TYPE_DOES_NOT_EXIST, null);
        }

        @JavascriptInterface
        public void getUserUniqueId(String value) {
            Logger.i(IronSourceWebView.this.TAG, "getUserUniqueId(" + value + ")");
            SSAObj ssaObj = new SSAObj(value);
            if (ssaObj.containsKey(ParametersKeys.PRODUCT_TYPE)) {
                String funToCall = IronSourceWebView.this.extractSuccessFunctionToCall(value);
                if (!TextUtils.isEmpty(funToCall)) {
                    String productType = ssaObj.getString(ParametersKeys.PRODUCT_TYPE);
                    IronSourceWebView.this.injectJavascript(IronSourceWebView.this.generateJSToInject(funToCall, IronSourceWebView.this.parseToJson(ParametersKeys.USER_UNIQUE_ID, IronSourceSharedPrefHelper.getSupersonicPrefHelper().getUniqueId(productType), ParametersKeys.PRODUCT_TYPE, productType, null, null, null, null, null, false), JSMethods.ON_GET_USER_UNIQUE_ID_SUCCESS, JSMethods.ON_GET_USER_UNIQUE_ID_FAIL));
                    return;
                }
                return;
            }
            IronSourceWebView.this.responseBack(value, false, ErrorCodes.PRODUCT_TYPE_DOES_NOT_EXIST, null);
        }

        @JavascriptInterface
        public void onGetUserUniqueIdSuccess(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onGetUserUniqueIdSuccess(" + value + ")");
        }

        @JavascriptInterface
        public void onGetUserUniqueIdFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onGetUserUniqueIdFail(" + value + ")");
        }

        private void injectGetUDIA(String funToCall, JSONArray jsonArr) {
            if (!TextUtils.isEmpty(funToCall)) {
                IronSourceWebView.this.injectJavascript(IronSourceWebView.this.generateJSToInject(funToCall, jsonArr.toString(), JSMethods.ON_GET_UDIA_SUCCESS, JSMethods.ON_GET_UDIA_FAIL));
            }
        }

        @JavascriptInterface
        public void onOfferWallGeneric(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onOfferWallGeneric(" + value + ")");
            if (IronSourceWebView.this.shouldNotifyDeveloper(ProductType.OfferWall.toString())) {
                IronSourceWebView.this.mOnOfferWallListener.onOWGeneric(BuildConfig.FLAVOR, BuildConfig.FLAVOR);
            }
        }

        @JavascriptInterface
        public void setUserData(String value) {
            Logger.i(IronSourceWebView.this.TAG, "setUserData(" + value + ")");
            SSAObj ssaObj = new SSAObj(value);
            if (!ssaObj.containsKey(ParametersKeys.KEY)) {
                IronSourceWebView.this.responseBack(value, false, ErrorCodes.KEY_DOES_NOT_EXIST, null);
            } else if (ssaObj.containsKey(ParametersKeys.VALUE)) {
                String mKey = ssaObj.getString(ParametersKeys.KEY);
                String mValue = ssaObj.getString(ParametersKeys.VALUE);
                if (IronSourceSharedPrefHelper.getSupersonicPrefHelper().setUserData(mKey, mValue)) {
                    IronSourceWebView.this.injectJavascript(IronSourceWebView.this.generateJSToInject(IronSourceWebView.this.extractSuccessFunctionToCall(value), IronSourceWebView.this.parseToJson(mKey, mValue, null, null, null, null, null, null, null, false)));
                    return;
                }
                IronSourceWebView.this.responseBack(value, false, "SetUserData failed writing to shared preferences", null);
            } else {
                IronSourceWebView.this.responseBack(value, false, ErrorCodes.VALUE_DOES_NOT_EXIST, null);
            }
        }

        @JavascriptInterface
        public void getUserData(String value) {
            Logger.i(IronSourceWebView.this.TAG, "getUserData(" + value + ")");
            SSAObj ssaObj = new SSAObj(value);
            if (ssaObj.containsKey(ParametersKeys.KEY)) {
                String failFunToCall = IronSourceWebView.this.extractSuccessFunctionToCall(value);
                String mKey = ssaObj.getString(ParametersKeys.KEY);
                IronSourceWebView.this.injectJavascript(IronSourceWebView.this.generateJSToInject(failFunToCall, IronSourceWebView.this.parseToJson(mKey, IronSourceSharedPrefHelper.getSupersonicPrefHelper().getUserData(mKey), null, null, null, null, null, null, null, false)));
                return;
            }
            IronSourceWebView.this.responseBack(value, false, ErrorCodes.KEY_DOES_NOT_EXIST, null);
        }

        @JavascriptInterface
        public void onGetUserCreditsFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onGetUserCreditsFail(" + value + ")");
            final String message = new SSAObj(value).getString(ParametersKeys.ERR_MSG);
            if (IronSourceWebView.this.shouldNotifyDeveloper(ProductType.OfferWall.toString())) {
                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        String toSend = message;
                        if (toSend == null) {
                            toSend = "We're sorry, some error occurred. we will investigate it";
                        }
                        IronSourceWebView.this.mOnOfferWallListener.onGetOWCreditsFailed(toSend);
                    }
                });
            }
            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg(JSMethods.ON_GET_USER_CREDITS_FAILED, value);
        }

        @JavascriptInterface
        public void onAdWindowsClosed(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onAdWindowsClosed(" + value + ")");
            IronSourceWebView.this.mSavedState.adClosed();
            IronSourceWebView.this.mSavedState.setDisplayedDemandSourceName(null);
            SSAObj ssaObj = new SSAObj(value);
            final String product = ssaObj.getString(ParametersKeys.PRODUCT_TYPE);
            final String demandSourceName = ssaObj.getString(RequestParameters.DEMAND_SOURCE_NAME);
            if (product.equalsIgnoreCase(ProductType.RewardedVideo.toString())) {
                Log.d(IronSourceWebView.this.PUB_TAG, "onRVAdClosed()");
            } else if (product.equalsIgnoreCase(ProductType.Interstitial.toString())) {
                Log.d(IronSourceWebView.this.PUB_TAG, "onISAdClosed()");
            } else if (product.equalsIgnoreCase(ProductType.OfferWall.toString())) {
                Log.d(IronSourceWebView.this.PUB_TAG, "onOWAdClosed()");
            }
            if (IronSourceWebView.this.shouldNotifyDeveloper(product) && product != null) {
                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        if (product.equalsIgnoreCase(ProductType.RewardedVideo.toString())) {
                            IronSourceWebView.this.mOnRewardedVideoListener.onRVAdClosed(demandSourceName);
                        } else if (product.equalsIgnoreCase(ProductType.Interstitial.toString())) {
                            IronSourceWebView.this.mOnInitInterstitialListener.onInterstitialClose();
                        } else if (product.equalsIgnoreCase(ProductType.OfferWall.toString())) {
                            IronSourceWebView.this.mOnOfferWallListener.onOWAdClosed();
                        }
                    }
                });
            }
        }

        @JavascriptInterface
        public void onVideoStatusChanged(String value) {
            Log.d(IronSourceWebView.this.TAG, "onVideoStatusChanged(" + value + ")");
            SSAObj ssaObj = new SSAObj(value);
            String product = ssaObj.getString(ParametersKeys.PRODUCT_TYPE);
            if (IronSourceWebView.this.mVideoEventsListener != null && !TextUtils.isEmpty(product) && ProductType.RewardedVideo.toString().equalsIgnoreCase(product)) {
                String status = ssaObj.getString(ParametersKeys.VIDEO_STATUS);
                if (ParametersKeys.VIDEO_STATUS_STARTED.equalsIgnoreCase(status)) {
                    IronSourceWebView.this.mVideoEventsListener.onVideoStarted();
                } else if (ParametersKeys.VIDEO_STATUS_PAUSED.equalsIgnoreCase(status)) {
                    IronSourceWebView.this.mVideoEventsListener.onVideoPaused();
                } else if (ParametersKeys.VIDEO_STATUS_PLAYING.equalsIgnoreCase(status)) {
                    IronSourceWebView.this.mVideoEventsListener.onVideoResumed();
                } else if (ParametersKeys.VIDEO_STATUS_ENDED.equalsIgnoreCase(status)) {
                    IronSourceWebView.this.mVideoEventsListener.onVideoEnded();
                } else if (ParametersKeys.VIDEO_STATUS_STOPPED.equalsIgnoreCase(status)) {
                    IronSourceWebView.this.mVideoEventsListener.onVideoStopped();
                } else {
                    Logger.i(IronSourceWebView.this.TAG, "onVideoStatusChanged: unknown status: " + status);
                }
            }
        }
    }

    public enum State {
        Display,
        Gone
    }

    private class SupersonicWebViewTouchListener implements OnTouchListener {
        private SupersonicWebViewTouchListener() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == 1) {
                float xTouch = event.getX();
                float yTouch = event.getY();
                Logger.i(IronSourceWebView.this.TAG, "X:" + ((int) xTouch) + " Y:" + ((int) yTouch));
                int width = DeviceStatus.getDeviceWidth();
                int height = DeviceStatus.getDeviceHeight();
                Logger.i(IronSourceWebView.this.TAG, "Width:" + width + " Height:" + height);
                int boundsTouchAreaX = SDKUtils.dpToPx((long) IronSourceWebView.this.mHiddenForceCloseWidth);
                int boundsTouchAreaY = SDKUtils.dpToPx((long) IronSourceWebView.this.mHiddenForceCloseHeight);
                int actualTouchX = 0;
                int actualTouchY = 0;
                if (ForceClosePosition.TOP_RIGHT.equalsIgnoreCase(IronSourceWebView.this.mHiddenForceCloseLocation)) {
                    actualTouchX = width - ((int) xTouch);
                    actualTouchY = (int) yTouch;
                } else if (ForceClosePosition.TOP_LEFT.equalsIgnoreCase(IronSourceWebView.this.mHiddenForceCloseLocation)) {
                    actualTouchX = (int) xTouch;
                    actualTouchY = (int) yTouch;
                } else if (ForceClosePosition.BOTTOM_RIGHT.equalsIgnoreCase(IronSourceWebView.this.mHiddenForceCloseLocation)) {
                    actualTouchX = width - ((int) xTouch);
                    actualTouchY = height - ((int) yTouch);
                } else if (ForceClosePosition.BOTTOM_LEFT.equalsIgnoreCase(IronSourceWebView.this.mHiddenForceCloseLocation)) {
                    actualTouchX = (int) xTouch;
                    actualTouchY = height - ((int) yTouch);
                }
                if (actualTouchX <= boundsTouchAreaX && actualTouchY <= boundsTouchAreaY) {
                    IronSourceWebView.this.isRemoveCloseEventHandler = false;
                    if (IronSourceWebView.this.mCloseEventTimer != null) {
                        IronSourceWebView.this.mCloseEventTimer.cancel();
                    }
                    IronSourceWebView.this.mCloseEventTimer = new CountDownTimer(2000, 500) {
                        public void onTick(long millisUntilFinished) {
                            Logger.i(IronSourceWebView.this.TAG, "Close Event Timer Tick " + millisUntilFinished);
                        }

                        public void onFinish() {
                            Logger.i(IronSourceWebView.this.TAG, "Close Event Timer Finish");
                            if (IronSourceWebView.this.isRemoveCloseEventHandler) {
                                IronSourceWebView.this.isRemoveCloseEventHandler = false;
                            } else {
                                IronSourceWebView.this.engageEnd(ParametersKeys.FORCE_CLOSE);
                            }
                        }
                    }.start();
                }
            }
            return false;
        }
    }

    private class ViewClient extends WebViewClient {
        private ViewClient() {
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Logger.i("onPageStarted", url);
            super.onPageStarted(view, url, favicon);
        }

        public void onPageFinished(WebView view, String url) {
            Logger.i("onPageFinished", url);
            if (url.contains("adUnit") || url.contains("index.html")) {
                IronSourceWebView.this.pageFinished();
            }
            super.onPageFinished(view, url);
        }

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Logger.i("onReceivedError", failingUrl + " " + description);
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Logger.i("shouldOverrideUrlLoading", url);
            try {
                if (IronSourceWebView.this.handleSearchKeysURLs(url)) {
                    IronSourceWebView.this.interceptedUrlToStore();
                    return true;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            Logger.i("shouldInterceptRequest", url);
            boolean mraidCall = false;
            try {
                if (new URL(url).getFile().contains("mraid.js")) {
                    mraidCall = true;
                }
            } catch (MalformedURLException e) {
            }
            if (mraidCall) {
                String filePath = "file://" + IronSourceWebView.this.mCacheDirectory + File.separator + "mraid.js";
                try {
                    FileInputStream fis = new FileInputStream(new File(filePath));
                    return new WebResourceResponse("text/javascript", HTTP.UTF_8, getClass().getResourceAsStream(filePath));
                } catch (FileNotFoundException e2) {
                }
            }
            return super.shouldInterceptRequest(view, url);
        }
    }

    public IronSourceWebView(Context context) {
        super(context.getApplicationContext());
        Logger.i(this.TAG, "C'tor");
        this.mCacheDirectory = initializeCacheDirectory(context.getApplicationContext());
        this.mCurrentActivityContext = context;
        initLayout(this.mCurrentActivityContext);
        this.mSavedState = new AdUnitsState();
        this.downloadManager = getDownloadManager();
        this.downloadManager.setOnPreCacheCompletion(this);
        this.mWebChromeClient = new ChromeClient();
        setWebViewClient(new ViewClient());
        setWebChromeClient(this.mWebChromeClient);
        setWebViewSettings();
        addJavascriptInterface(createJSInterface(context), Constants.JAVASCRIPT_INTERFACE_NAME);
        setDownloadListener(this);
        setOnTouchListener(new SupersonicWebViewTouchListener());
        this.mUiHandler = createMainThreadHandler();
    }

    JSInterface createJSInterface(Context context) {
        return new JSInterface(context);
    }

    Handler createMainThreadHandler() {
        return new Handler(Looper.getMainLooper());
    }

    DownloadManager getDownloadManager() {
        return DownloadManager.getInstance(this.mCacheDirectory);
    }

    String initializeCacheDirectory(Context context) {
        return IronSourceStorageUtils.initializeCacheDirectory(context.getApplicationContext());
    }

    private void initLayout(Context context) {
        LayoutParams coverScreenParams = new LayoutParams(-1, -1);
        this.mControllerLayout = new FrameLayout(context);
        this.mCustomViewContainer = new FrameLayout(context);
        this.mCustomViewContainer.setLayoutParams(new LayoutParams(-1, -1));
        this.mCustomViewContainer.setVisibility(8);
        FrameLayout mContentView = new FrameLayout(context);
        mContentView.setLayoutParams(new LayoutParams(-1, -1));
        mContentView.addView(this);
        this.mControllerLayout.addView(this.mCustomViewContainer, coverScreenParams);
        this.mControllerLayout.addView(mContentView);
    }

    private void setWebViewSettings() {
        WebSettings s = getSettings();
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
        s.setBuiltInZoomControls(false);
        s.setJavaScriptEnabled(true);
        s.setSupportMultipleWindows(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setGeolocationEnabled(true);
        s.setGeolocationDatabasePath("/data/data/org.itri.html5webview/databases/");
        s.setDomStorageEnabled(true);
        try {
            setDisplayZoomControls(s);
            setMediaPlaybackJellyBean(s);
        } catch (Throwable e) {
            Logger.e(this.TAG, "setWebSettings - " + e.toString());
            new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=setWebViewSettings"});
        }
    }

    private void setDisplayZoomControls(WebSettings s) {
        if (VERSION.SDK_INT > 11) {
            s.setDisplayZoomControls(false);
        }
    }

    public WebBackForwardList saveState(Bundle outState) {
        return super.saveState(outState);
    }

    @SuppressLint({"NewApi"})
    private void setMediaPlaybackJellyBean(WebSettings s) {
        if (VERSION.SDK_INT >= 17) {
            s.setMediaPlaybackRequiresUserGesture(false);
        }
    }

    @SuppressLint({"NewApi"})
    private void setWebDebuggingEnabled() {
        if (VERSION.SDK_INT >= 19) {
            setWebContentsDebuggingEnabled(true);
        }
    }

    public void downloadController() {
        IronSourceStorageUtils.deleteFile(this.mCacheDirectory, BuildConfig.FLAVOR, Constants.MOBILE_CONTROLLER_HTML);
        String controllerPath = BuildConfig.FLAVOR;
        String controllerUrl = SDKUtils.getControllerUrl();
        SSAFile indexHtml = new SSAFile(controllerUrl, controllerPath);
        this.mGlobalControllerTimer = new CountDownTimer(40000, 1000) {
            public void onTick(long millisUntilFinished) {
                Logger.i(IronSourceWebView.this.TAG, "Global Controller Timer Tick " + millisUntilFinished);
            }

            public void onFinish() {
                Logger.i(IronSourceWebView.this.TAG, "Global Controller Timer Finish");
                IronSourceWebView.this.mGlobalControllerTimeFinish = true;
            }
        }.start();
        if (this.downloadManager.isMobileControllerThreadLive()) {
            Logger.i(this.TAG, "Download Mobile Controller: already alive");
            return;
        }
        Logger.i(this.TAG, "Download Mobile Controller: " + controllerUrl);
        this.downloadManager.downloadMobileControllerFile(indexHtml);
    }

    public void setDebugMode(int debugMode) {
        mDebugMode = debugMode;
    }

    public int getDebugMode() {
        return mDebugMode;
    }

    private boolean shouldNotifyDeveloper(String product) {
        boolean shouldNotify = false;
        if (TextUtils.isEmpty(product)) {
            Logger.d(this.TAG, "Trying to trigger a listener - no product was found");
            return false;
        }
        if (product.equalsIgnoreCase(ProductType.Interstitial.toString())) {
            shouldNotify = this.mOnInitInterstitialListener != null;
        } else if (product.equalsIgnoreCase(ProductType.RewardedVideo.toString())) {
            shouldNotify = this.mOnRewardedVideoListener != null;
        } else if (product.equalsIgnoreCase(ProductType.OfferWall.toString()) || product.equalsIgnoreCase(ProductType.OfferWallCredits.toString())) {
            shouldNotify = this.mOnOfferWallListener != null;
        }
        if (!shouldNotify) {
            Logger.d(this.TAG, "Trying to trigger a listener - no listener was found for product " + product);
        }
        return shouldNotify;
    }

    public void setOrientationState(String orientation) {
        this.mOrientationState = orientation;
    }

    public String getOrientationState() {
        return this.mOrientationState;
    }

    public static void setEXTERNAL_URL(String EXTERNAL_URL) {
        EXTERNAL_URL = EXTERNAL_URL;
    }

    public void setVideoEventsListener(VideoEventsListener listener) {
        this.mVideoEventsListener = listener;
    }

    public void removeVideoEventsListener() {
        this.mVideoEventsListener = null;
    }

    private void setWebviewBackground(String value) {
        String keyColor = new SSAObj(value).getString(ParametersKeys.COLOR);
        int bgColor = 0;
        if (!ParametersKeys.TRANSPARENT.equalsIgnoreCase(keyColor)) {
            bgColor = Color.parseColor(keyColor);
        }
        setBackgroundColor(bgColor);
    }

    public void load(int loadAttemp) {
        try {
            loadUrl("about:blank");
        } catch (Throwable e) {
            Logger.e(this.TAG, "WebViewController:: load: " + e.toString());
            new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=webviewLoadBlank"});
        }
        String controllerPath = "file://" + this.mCacheDirectory + File.separator + Constants.MOBILE_CONTROLLER_HTML;
        if (new File(this.mCacheDirectory + File.separator + Constants.MOBILE_CONTROLLER_HTML).exists()) {
            this.mRequestParameters = getRequestParameters();
            String controllerPathWithParams = controllerPath + "?" + this.mRequestParameters;
            final int i = loadAttemp;
            this.mLoadControllerTimer = new CountDownTimer(10000, 1000) {
                public void onTick(long millisUntilFinished) {
                    Logger.i(IronSourceWebView.this.TAG, "Loading Controller Timer Tick " + millisUntilFinished);
                }

                public void onFinish() {
                    Logger.i(IronSourceWebView.this.TAG, "Loading Controller Timer Finish");
                    if (i == 2) {
                        IronSourceWebView.this.mGlobalControllerTimer.cancel();
                        for (DemandSource demandSource : IronSourceAdsPublisherAgent.getInstance((Activity) IronSourceWebView.this.getCurrentActivityContext()).getDemandSources()) {
                            if (demandSource.getDemandSourceInitState() == 1) {
                                IronSourceWebView.this.sendProductErrorMessage(ProductType.RewardedVideo, demandSource.getDemandSourceName());
                            }
                        }
                        if (IronSourceWebView.this.mISmiss) {
                            IronSourceWebView.this.sendProductErrorMessage(ProductType.Interstitial, null);
                        }
                        if (IronSourceWebView.this.mOWmiss) {
                            IronSourceWebView.this.sendProductErrorMessage(ProductType.OfferWall, null);
                        }
                        if (IronSourceWebView.this.mOWCreditsMiss) {
                            IronSourceWebView.this.sendProductErrorMessage(ProductType.OfferWallCredits, null);
                            return;
                        }
                        return;
                    }
                    IronSourceWebView.this.load(2);
                }
            }.start();
            try {
                loadUrl(controllerPathWithParams);
            } catch (Throwable e2) {
                Logger.e(this.TAG, "WebViewController:: load: " + e2.toString());
                new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=webviewLoadWithPath"});
            }
            Logger.i(this.TAG, "load(): " + controllerPathWithParams);
            return;
        }
        Logger.i(this.TAG, "load(): Mobile Controller HTML Does not exist");
        new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=htmlControllerDoesNotExistOnFileSystem"});
    }

    private void initProduct(String applicationKey, String userId, ProductType type, String action, String demandSourceName) {
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(applicationKey)) {
            triggerOnControllerInitProductFail("User id or Application key are missing", type, demandSourceName);
        } else if (this.mControllerState == ControllerState.Ready) {
            IronSourceSharedPrefHelper.getSupersonicPrefHelper().setApplicationKey(applicationKey, type);
            IronSourceSharedPrefHelper.getSupersonicPrefHelper().setUserID(userId, type);
            createInitProductJSMethod(type, demandSourceName);
        } else {
            setMissProduct(type, demandSourceName);
            if (this.mControllerState == ControllerState.Failed) {
                triggerOnControllerInitProductFail(SDKUtils.createErrorMessage(action, ErrorCodes.InitiatingController), type, demandSourceName);
            } else if (this.mGlobalControllerTimeFinish) {
                downloadController();
            }
        }
    }

    public void initRewardedVideo(String applicationKey, String userId, String demandSourceName, DSRewardedVideoListener listener) {
        this.mRVAppKey = applicationKey;
        this.mRVUserId = userId;
        this.mOnRewardedVideoListener = listener;
        this.mSavedState.setRVAppKey(applicationKey);
        this.mSavedState.setRVUserId(userId);
        initProduct(applicationKey, userId, ProductType.RewardedVideo, ErrorCodes.InitRV, demandSourceName);
    }

    public void initInterstitial(String applicationKey, String userId, Map<String, String> extraParameters, OnInterstitialListener listener) {
        this.mISAppKey = applicationKey;
        this.mISUserId = userId;
        this.mISExtraParameters = extraParameters;
        this.mOnInitInterstitialListener = listener;
        this.mSavedState.setInterstitialAppKey(this.mISAppKey);
        this.mSavedState.setInterstitialUserId(this.mISUserId);
        this.mSavedState.setInterstitialExtraParams(this.mISExtraParameters);
        this.mSavedState.setReportInitInterstitial(true);
        initProduct(this.mISAppKey, this.mISUserId, ProductType.Interstitial, ErrorCodes.InitIS, null);
    }

    public void loadInterstitial() {
        if (!isInterstitialAdAvailable()) {
            this.mSavedState.setReportLoadInterstitial(true);
            injectJavascript(generateJSToInject(JSMethods.LOAD_INTERSTITIAL, JSMethods.ON_LOAD_INTERSTITIAL_SUCCESS, JSMethods.ON_LOAD_INTERSTITIAL_FAIL));
        } else if (shouldNotifyDeveloper(ProductType.Interstitial.toString())) {
            runOnUiThread(new Runnable() {
                public void run() {
                    IronSourceWebView.this.mOnInitInterstitialListener.onInterstitialLoadSuccess();
                }
            });
        }
    }

    public boolean isInterstitialAdAvailable() {
        return this.mIsInterstitialAvailable == null ? false : this.mIsInterstitialAvailable.booleanValue();
    }

    public void showInterstitial() {
        injectJavascript(generateJSToInject(JSMethods.SHOW_INTERSTITIAL, JSMethods.ON_SHOW_INTERSTITIAL_SUCCESS, JSMethods.ON_SHOW_INTERSTITIAL_FAIL));
    }

    public void forceShowInterstitial() {
        injectJavascript(generateJSToInject(JSMethods.FORCE_SHOW_INTERSTITIAL, JSMethods.ON_SHOW_INTERSTITIAL_SUCCESS, JSMethods.ON_SHOW_INTERSTITIAL_FAIL));
    }

    public void initOfferWall(String applicationKey, String userId, Map<String, String> extraParameters, OnOfferWallListener listener) {
        this.mOWAppKey = applicationKey;
        this.mOWUserId = userId;
        this.mOWExtraParameters = extraParameters;
        this.mOnOfferWallListener = listener;
        this.mSavedState.setOfferWallExtraParams(this.mOWExtraParameters);
        this.mSavedState.setOfferwallReportInit(true);
        initProduct(this.mOWAppKey, this.mOWUserId, ProductType.OfferWall, ErrorCodes.InitOW, null);
    }

    public void showOfferWall(Map<String, String> extraParameters) {
        this.mOWExtraParameters = extraParameters;
        injectJavascript(generateJSToInject(JSMethods.SHOW_OFFER_WALL, JSMethods.ON_SHOW_OFFER_WALL_SUCCESS, JSMethods.ON_SHOW_OFFER_WALL_FAIL));
    }

    public void getOfferWallCredits(String applicationKey, String userId, OnOfferWallListener listener) {
        this.mOWCreditsAppKey = applicationKey;
        this.mOWCreditsUserId = userId;
        this.mOnOfferWallListener = listener;
        initProduct(this.mOWCreditsAppKey, this.mOWCreditsUserId, ProductType.OfferWallCredits, ErrorCodes.ShowOWCredits, null);
    }

    private void createInitProductJSMethod(ProductType type, String demandSourceName) {
        String script = null;
        if (type == ProductType.RewardedVideo) {
            DemandSource demandSource = IronSourceAdsPublisherAgent.getInstance((Activity) getCurrentActivityContext()).getDemandSourceByName(demandSourceName);
            Map<String, String> rvParamsMap = new HashMap();
            rvParamsMap.put(RequestParameters.APPLICATION_KEY, this.mRVAppKey);
            rvParamsMap.put(RequestParameters.APPLICATION_USER_ID, this.mRVUserId);
            if (demandSource != null) {
                if (demandSource.getExtraParams() != null) {
                    rvParamsMap.putAll(demandSource.getExtraParams());
                }
                if (!TextUtils.isEmpty(demandSourceName)) {
                    rvParamsMap.put(RequestParameters.DEMAND_SOURCE_NAME, demandSourceName);
                }
            }
            script = generateJSToInject(JSMethods.INIT_REWARDED_VIDEO, flatMapToJsonAsString(rvParamsMap), JSMethods.ON_INIT_REWARDED_VIDEO_SUCCESS, JSMethods.ON_INIT_REWARDED_VIDEO_FAIL);
        } else if (type == ProductType.Interstitial) {
            Map<String, String> interstitialParamsMap = new HashMap();
            interstitialParamsMap.put(RequestParameters.APPLICATION_KEY, this.mISAppKey);
            interstitialParamsMap.put(RequestParameters.APPLICATION_USER_ID, this.mISUserId);
            if (this.mISExtraParameters != null) {
                interstitialParamsMap.putAll(this.mISExtraParameters);
            }
            script = generateJSToInject(JSMethods.INIT_INTERSTITIAL, flatMapToJsonAsString(interstitialParamsMap), JSMethods.ON_INIT_INTERSTITIAL_SUCCESS, JSMethods.ON_INIT_INTERSTITIAL_FAIL);
        } else if (type == ProductType.OfferWall) {
            Map<String, String> offerwallParamsMap = new HashMap();
            offerwallParamsMap.put(RequestParameters.APPLICATION_KEY, this.mOWAppKey);
            offerwallParamsMap.put(RequestParameters.APPLICATION_USER_ID, this.mOWUserId);
            if (this.mOWExtraParameters != null) {
                offerwallParamsMap.putAll(this.mOWExtraParameters);
            }
            script = generateJSToInject(JSMethods.INIT_OFFERWALL, flatMapToJsonAsString(offerwallParamsMap), JSMethods.ON_INIT_OFFERWALL_SUCCESS, JSMethods.ON_INIT_OFFERWALL_FAIL);
        } else if (type == ProductType.OfferWallCredits) {
            script = generateJSToInject(JSMethods.GET_USER_CREDITS, parseToJson(ParametersKeys.PRODUCT_TYPE, ParametersKeys.OFFER_WALL, RequestParameters.APPLICATION_KEY, this.mOWCreditsAppKey, RequestParameters.APPLICATION_USER_ID, this.mOWCreditsUserId, null, null, null, false), "null", JSMethods.ON_GET_USER_CREDITS_FAILED);
        }
        if (script != null) {
            injectJavascript(script);
        }
    }

    private String flatMapToJsonAsString(Map<String, String> params) {
        JSONObject jsObj = new JSONObject();
        if (params != null) {
            Iterator<Entry<String, String>> it = params.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, String> pairs = (Entry) it.next();
                try {
                    jsObj.putOpt((String) pairs.getKey(), SDKUtils.encodeString((String) pairs.getValue()));
                } catch (JSONException e) {
                    Logger.i(this.TAG, "flatMapToJsonAsStringfailed " + e.toString());
                }
                it.remove();
            }
        }
        return jsObj.toString();
    }

    void setMissProduct(ProductType type, String demandSourceName) {
        if (type == ProductType.RewardedVideo) {
            DemandSource demandSource = IronSourceAdsPublisherAgent.getInstance((Activity) getCurrentActivityContext()).getDemandSourceByName(demandSourceName);
            if (demandSource != null) {
                demandSource.setDemandSourceInitState(1);
            }
        } else if (type == ProductType.Interstitial) {
            this.mISmiss = true;
        } else if (type == ProductType.OfferWall) {
            this.mOWmiss = true;
        } else if (type == ProductType.OfferWallCredits) {
            this.mOWCreditsMiss = true;
        }
        Logger.i(this.TAG, "setMissProduct(" + type + ")");
    }

    private void triggerOnControllerInitProductFail(final String message, final ProductType type, final String demandSourceName) {
        if (shouldNotifyDeveloper(type.toString())) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (ProductType.RewardedVideo == type) {
                        Log.d(IronSourceWebView.this.TAG, "onRVInitFail(message:" + message + ")");
                        IronSourceWebView.this.mOnRewardedVideoListener.onRVInitFail(message, demandSourceName);
                    } else if (ProductType.Interstitial == type) {
                        IronSourceWebView.this.mSavedState.setInterstitialInitSuccess(false);
                        if (IronSourceWebView.this.mSavedState.reportInitInterstitial()) {
                            Log.d(IronSourceWebView.this.TAG, "onInterstitialInitFail(message:" + message + ")");
                            IronSourceWebView.this.mOnInitInterstitialListener.onInterstitialInitFailed(message);
                            IronSourceWebView.this.mSavedState.setReportInitInterstitial(false);
                        }
                    } else if (ProductType.OfferWall == type) {
                        IronSourceWebView.this.mOnOfferWallListener.onOfferwallInitFail(message);
                    } else if (ProductType.OfferWallCredits == type) {
                        IronSourceWebView.this.mOnOfferWallListener.onGetOWCreditsFailed(message);
                    }
                }
            });
        }
    }

    public void showRewardedVideo(String demandSourceName) {
        Map<String, String> rvParamsMap = new HashMap();
        if (!TextUtils.isEmpty(demandSourceName)) {
            rvParamsMap.put(RequestParameters.DEMAND_SOURCE_NAME, demandSourceName);
        }
        injectJavascript(generateJSToInject(JSMethods.SHOW_REWARDED_VIDEO, flatMapToJsonAsString(rvParamsMap), JSMethods.ON_SHOW_REWARDED_VIDEO_SUCCESS, JSMethods.ON_SHOW_REWARDED_VIDEO_FAIL));
    }

    public void assetCached(String file, String path) {
        injectJavascript(generateJSToInject(JSMethods.ASSET_CACHED, parseToJson(ParametersKeys.FILE, file, ClientCookie.PATH_ATTR, path, null, null, null, null, null, false)));
    }

    public void assetCachedFailed(String file, String path, String errorMsg) {
        injectJavascript(generateJSToInject(JSMethods.ASSET_CACHED_FAILED, parseToJson(ParametersKeys.FILE, file, ClientCookie.PATH_ATTR, path, ParametersKeys.ERR_MSG, errorMsg, null, null, null, false)));
    }

    public void enterBackground() {
        if (this.mControllerState == ControllerState.Ready) {
            injectJavascript(generateJSToInject(JSMethods.ENTER_BACKGROUND));
        }
    }

    public void enterForeground() {
        if (this.mControllerState == ControllerState.Ready) {
            injectJavascript(generateJSToInject(JSMethods.ENTER_FOREGROUND));
        }
    }

    public void viewableChange(boolean visibility, String webview) {
        injectJavascript(generateJSToInject(JSMethods.VIEWABLE_CHANGE, parseToJson(ParametersKeys.WEB_VIEW, webview, null, null, null, null, null, null, ParametersKeys.IS_VIEWABLE, visibility)));
    }

    public void nativeNavigationPressed(String action) {
        injectJavascript(generateJSToInject(JSMethods.NATIVE_NAVIGATION_PRESSED, parseToJson(ParametersKeys.ACTION, action, null, null, null, null, null, null, null, false)));
    }

    public void pageFinished() {
        injectJavascript(generateJSToInject(JSMethods.PAGE_FINISHED));
    }

    public void interceptedUrlToStore() {
        injectJavascript(generateJSToInject(JSMethods.INTERCEPTED_URL_TO_STORE));
    }

    private void injectJavascript(String script) {
        String catchClosure = "empty";
        if (getDebugMode() == DebugMode.MODE_0.getValue()) {
            catchClosure = "console.log(\"JS exeption: \" + JSON.stringify(e));";
        } else if (getDebugMode() >= DebugMode.MODE_1.getValue() && getDebugMode() <= DebugMode.MODE_3.getValue()) {
            catchClosure = "console.log(\"JS exeption: \" + JSON.stringify(e));";
        }
        final StringBuilder scriptBuilder = new StringBuilder();
        scriptBuilder.append("try{").append(script).append("}catch(e){").append(catchClosure).append("}");
        final String url = "javascript:" + scriptBuilder.toString();
        runOnUiThread(new Runnable() {
            public void run() {
                Logger.i(IronSourceWebView.this.TAG, url);
                try {
                    if (IronSourceWebView.this.isKitkatAndAbove != null) {
                        if (IronSourceWebView.this.isKitkatAndAbove.booleanValue()) {
                            IronSourceWebView.this.evaluateJavascriptKitKat(scriptBuilder.toString());
                        } else {
                            IronSourceWebView.this.loadUrl(url);
                        }
                    } else if (VERSION.SDK_INT >= 19) {
                        IronSourceWebView.this.evaluateJavascriptKitKat(scriptBuilder.toString());
                        IronSourceWebView.this.isKitkatAndAbove = Boolean.valueOf(true);
                    } else {
                        IronSourceWebView.this.loadUrl(url);
                        IronSourceWebView.this.isKitkatAndAbove = Boolean.valueOf(false);
                    }
                } catch (NoSuchMethodError e) {
                    Logger.e(IronSourceWebView.this.TAG, "evaluateJavascrip NoSuchMethodError: SDK version=" + VERSION.SDK_INT + " " + e);
                    IronSourceWebView.this.loadUrl(url);
                    IronSourceWebView.this.isKitkatAndAbove = Boolean.valueOf(false);
                } catch (Throwable t) {
                    Logger.e(IronSourceWebView.this.TAG, "injectJavascript: " + t.toString());
                    new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=injectJavaScript"});
                }
            }
        });
    }

    @SuppressLint({"NewApi"})
    private void evaluateJavascriptKitKat(String script) {
        evaluateJavascript(script, null);
    }

    public Context getCurrentActivityContext() {
        return this.mCurrentActivityContext.getBaseContext();
    }

    private String getRequestParameters() {
        DeviceProperties properties = DeviceProperties.getInstance(getContext());
        StringBuilder builder = new StringBuilder();
        String sdkVer = DeviceProperties.getSupersonicSdkVersion();
        if (!TextUtils.isEmpty(sdkVer)) {
            builder.append(RequestParameters.SDK_VERSION).append(RequestParameters.EQUAL).append(sdkVer).append(RequestParameters.AMPERSAND);
        }
        String osType = properties.getDeviceOsType();
        if (!TextUtils.isEmpty(osType)) {
            builder.append(RequestParameters.DEVICE_OS).append(RequestParameters.EQUAL).append(osType);
        }
        Uri downloadUri = Uri.parse(SDKUtils.getControllerUrl());
        if (downloadUri != null) {
            String scheme = downloadUri.getScheme() + ":";
            String host = downloadUri.getHost();
            int port = downloadUri.getPort();
            if (port != -1) {
                host = host + ":" + port;
            }
            builder.append(RequestParameters.AMPERSAND).append(RequestParameters.PROTOCOL).append(RequestParameters.EQUAL).append(scheme);
            builder.append(RequestParameters.AMPERSAND).append(ClientCookie.DOMAIN_ATTR).append(RequestParameters.EQUAL).append(host);
            String config = SDKUtils.getControllerConfig();
            if (!TextUtils.isEmpty(config)) {
                builder.append(RequestParameters.AMPERSAND).append(RequestParameters.CONTROLLER_CONFIG).append(RequestParameters.EQUAL).append(config);
            }
            builder.append(RequestParameters.AMPERSAND).append(RequestParameters.DEBUG).append(RequestParameters.EQUAL).append(getDebugMode());
        }
        return builder.toString();
    }

    private void closeWebView() {
        if (this.mChangeListener != null) {
            this.mChangeListener.onCloseRequested();
        }
    }

    private void responseBack(String value, boolean result, String errorMessage, String errorCode) {
        SSAObj ssaObj = new SSAObj(value);
        String success = ssaObj.getString(JSON_KEY_SUCCESS);
        String fail = ssaObj.getString(JSON_KEY_FAIL);
        String funToCall = null;
        if (result) {
            if (!TextUtils.isEmpty(success)) {
                funToCall = success;
            }
        } else if (!TextUtils.isEmpty(fail)) {
            funToCall = fail;
        }
        if (!TextUtils.isEmpty(funToCall)) {
            if (!TextUtils.isEmpty(errorMessage)) {
                try {
                    value = new JSONObject(value).put(ParametersKeys.ERR_MSG, errorMessage).toString();
                } catch (JSONException e) {
                }
            }
            if (!TextUtils.isEmpty(errorCode)) {
                try {
                    value = new JSONObject(value).put(ParametersKeys.ERR_CODE, errorCode).toString();
                } catch (JSONException e2) {
                }
            }
            injectJavascript(generateJSToInject(funToCall, value));
        }
    }

    private String extractSuccessFunctionToCall(String jsonStr) {
        return new SSAObj(jsonStr).getString(JSON_KEY_SUCCESS);
    }

    private String extractFailFunctionToCall(String jsonStr) {
        return new SSAObj(jsonStr).getString(JSON_KEY_FAIL);
    }

    private String parseToJson(String key1, String value1, String key2, String value2, String key3, String value3, String key4, String value4, String key5, boolean value5) {
        JSONObject jsObj = new JSONObject();
        try {
            if (!(TextUtils.isEmpty(key1) || TextUtils.isEmpty(value1))) {
                jsObj.put(key1, SDKUtils.encodeString(value1));
            }
            if (!(TextUtils.isEmpty(key2) || TextUtils.isEmpty(value2))) {
                jsObj.put(key2, SDKUtils.encodeString(value2));
            }
            if (!(TextUtils.isEmpty(key3) || TextUtils.isEmpty(value3))) {
                jsObj.put(key3, SDKUtils.encodeString(value3));
            }
            if (!(TextUtils.isEmpty(key4) || TextUtils.isEmpty(value4))) {
                jsObj.put(key4, SDKUtils.encodeString(value4));
            }
            if (!TextUtils.isEmpty(key5)) {
                jsObj.put(key5, value5);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            new IronSourceAsyncHttpRequestTask().execute(new String[]{Constants.NATIVE_EXCEPTION_BASE_URL + e.getStackTrace()[0].getMethodName()});
        }
        return jsObj.toString();
    }

    private String mapToJson(Map<String, String> map) {
        JSONObject jsObj = new JSONObject();
        if (!(map == null || map.isEmpty())) {
            for (String key : map.keySet()) {
                try {
                    jsObj.put(key, SDKUtils.encodeString((String) map.get(key)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return jsObj.toString();
    }

    private Object[] getDeviceParams(Context context) {
        boolean fail = false;
        DeviceProperties deviceProperties = DeviceProperties.getInstance(context);
        JSONObject jsObj = new JSONObject();
        try {
            StringBuilder key;
            jsObj.put(RequestParameters.APP_ORIENTATION, SDKUtils.translateRequestedOrientation(DeviceStatus.getActivityRequestedOrientation(getCurrentActivityContext())));
            String deviceOem = deviceProperties.getDeviceOem();
            if (deviceOem != null) {
                jsObj.put(SDKUtils.encodeString(RequestParameters.DEVICE_OEM), SDKUtils.encodeString(deviceOem));
            }
            String deviceModel = deviceProperties.getDeviceModel();
            if (deviceModel != null) {
                jsObj.put(SDKUtils.encodeString(RequestParameters.DEVICE_MODEL), SDKUtils.encodeString(deviceModel));
            } else {
                fail = true;
            }
            SDKUtils.loadGoogleAdvertiserInfo(context);
            String advertiserId = SDKUtils.getAdvertiserId();
            Boolean isLAT = Boolean.valueOf(SDKUtils.isLimitAdTrackingEnabled());
            if (!TextUtils.isEmpty(advertiserId)) {
                Logger.i(this.TAG, "add AID and LAT");
                jsObj.put(RequestParameters.isLAT, isLAT);
                jsObj.put(RequestParameters.DEVICE_IDS + RequestParameters.LEFT_BRACKETS + RequestParameters.AID + RequestParameters.RIGHT_BRACKETS, SDKUtils.encodeString(advertiserId));
            }
            String deviceOSType = deviceProperties.getDeviceOsType();
            if (deviceOSType != null) {
                jsObj.put(SDKUtils.encodeString(RequestParameters.DEVICE_OS), SDKUtils.encodeString(deviceOSType));
            } else {
                fail = true;
            }
            String deviceOSVersion = Integer.toString(deviceProperties.getDeviceOsVersion());
            if (deviceOSVersion != null) {
                jsObj.put(SDKUtils.encodeString(RequestParameters.DEVICE_OS_VERSION), deviceOSVersion);
            } else {
                fail = true;
            }
            String ssaSDKVersion = DeviceProperties.getSupersonicSdkVersion();
            if (ssaSDKVersion != null) {
                jsObj.put(SDKUtils.encodeString(RequestParameters.SDK_VERSION), SDKUtils.encodeString(ssaSDKVersion));
            }
            if (deviceProperties.getDeviceCarrier() != null && deviceProperties.getDeviceCarrier().length() > 0) {
                jsObj.put(SDKUtils.encodeString(RequestParameters.MOBILE_CARRIER), SDKUtils.encodeString(deviceProperties.getDeviceCarrier()));
            }
            String connectionType = ConnectivityService.getConnectionType(context);
            if (TextUtils.isEmpty(connectionType)) {
                fail = true;
            } else {
                jsObj.put(SDKUtils.encodeString(RequestParameters.CONNECTION_TYPE), SDKUtils.encodeString(connectionType));
            }
            String deviceLanguage = context.getResources().getConfiguration().locale.getLanguage();
            if (!TextUtils.isEmpty(deviceLanguage)) {
                jsObj.put(SDKUtils.encodeString(RequestParameters.DEVICE_LANGUAGE), SDKUtils.encodeString(deviceLanguage.toUpperCase()));
            }
            if (SDKUtils.isExternalStorageAvailable()) {
                long freeDiskSize = DeviceStatus.getAvailableMemorySizeInMegaBytes(this.mCacheDirectory);
                jsObj.put(SDKUtils.encodeString(RequestParameters.DISK_FREE_SIZE), SDKUtils.encodeString(String.valueOf(freeDiskSize)));
            } else {
                fail = true;
            }
            String width = String.valueOf(DeviceStatus.getDeviceWidth());
            if (TextUtils.isEmpty(width)) {
                fail = true;
            } else {
                key = new StringBuilder();
                key.append(SDKUtils.encodeString(RequestParameters.DEVICE_SCREEN_SIZE)).append(RequestParameters.LEFT_BRACKETS).append(SDKUtils.encodeString(RequestParameters.WIDTH)).append(RequestParameters.RIGHT_BRACKETS);
                jsObj.put(key.toString(), SDKUtils.encodeString(width));
            }
            String height = String.valueOf(DeviceStatus.getDeviceHeight());
            key = new StringBuilder();
            key.append(SDKUtils.encodeString(RequestParameters.DEVICE_SCREEN_SIZE)).append(RequestParameters.LEFT_BRACKETS).append(SDKUtils.encodeString(RequestParameters.HEIGHT)).append(RequestParameters.RIGHT_BRACKETS);
            jsObj.put(key.toString(), SDKUtils.encodeString(height));
            String packageName = ApplicationContext.getPackageName(getContext());
            if (!TextUtils.isEmpty(packageName)) {
                jsObj.put(SDKUtils.encodeString(RequestParameters.PACKAGE_NAME), SDKUtils.encodeString(packageName));
            }
            String scaleStr = String.valueOf(DeviceStatus.getDeviceDensity());
            if (!TextUtils.isEmpty(scaleStr)) {
                jsObj.put(SDKUtils.encodeString(RequestParameters.DEVICE_SCREEN_SCALE), SDKUtils.encodeString(scaleStr));
            }
            String rootStr = String.valueOf(DeviceStatus.isRootedDevice());
            if (!TextUtils.isEmpty(rootStr)) {
                jsObj.put(SDKUtils.encodeString(RequestParameters.IS_ROOT_DEVICE), SDKUtils.encodeString(rootStr));
            }
            float deviceVolume = DeviceProperties.getInstance(context).getDeviceVolume(context);
            if (!TextUtils.isEmpty(rootStr)) {
                jsObj.put(SDKUtils.encodeString(RequestParameters.DEVICE_VOLUME), (double) deviceVolume);
            }
            Context ctx = getCurrentActivityContext();
            if (VERSION.SDK_INT >= 19 && (ctx instanceof Activity)) {
                jsObj.put(SDKUtils.encodeString(RequestParameters.IMMERSIVE), DeviceStatus.isImmersiveSupported((Activity) ctx));
            }
            jsObj.put(SDKUtils.encodeString(RequestParameters.BATTERY_LEVEL), DeviceStatus.getBatteryLevel(ctx));
            jsObj.put(SDKUtils.encodeString(RequestParameters.NETWORK_MCC), ConnectivityService.getNetworkMCC(ctx));
            jsObj.put(SDKUtils.encodeString(RequestParameters.NETWORK_MNC), ConnectivityService.getNetworkMNC(ctx));
            jsObj.put(SDKUtils.encodeString(RequestParameters.PHONE_TYPE), ConnectivityService.getPhoneType(ctx));
            jsObj.put(SDKUtils.encodeString(RequestParameters.SIM_OPERATOR), SDKUtils.encodeString(ConnectivityService.getSimOperator(ctx)));
            jsObj.put(SDKUtils.encodeString(RequestParameters.LAST_UPDATE_TIME), ApplicationContext.getLastUpdateTime(ctx));
            jsObj.put(SDKUtils.encodeString(RequestParameters.FIRST_INSTALL_TIME), ApplicationContext.getFirstInstallTime(ctx));
            jsObj.put(SDKUtils.encodeString(RequestParameters.APPLICATION_VERSION_NAME), SDKUtils.encodeString(ApplicationContext.getApplicationVersionName(ctx)));
        } catch (JSONException e) {
            e.printStackTrace();
            new IronSourceAsyncHttpRequestTask().execute(new String[]{Constants.NATIVE_EXCEPTION_BASE_URL + e.getStackTrace()[0].getMethodName()});
        }
        return new Object[]{jsObj.toString(), Boolean.valueOf(fail)};
    }

    private Object[] getApplicationParams(String productType, String demandSourceName) {
        boolean fail = false;
        JSONObject jsObj = new JSONObject();
        String appKey = BuildConfig.FLAVOR;
        String userId = BuildConfig.FLAVOR;
        Map<String, String> productExtraParams = null;
        if (TextUtils.isEmpty(productType)) {
            fail = true;
        } else {
            if (productType.equalsIgnoreCase(ProductType.RewardedVideo.toString())) {
                appKey = this.mRVAppKey;
                userId = this.mRVUserId;
                DemandSource demandSource = IronSourceAdsPublisherAgent.getInstance((Activity) getCurrentActivityContext()).getDemandSourceByName(demandSourceName);
                if (demandSource != null) {
                    productExtraParams = demandSource.getExtraParams();
                }
            } else {
                if (productType.equalsIgnoreCase(ProductType.Interstitial.toString())) {
                    appKey = this.mISAppKey;
                    userId = this.mISUserId;
                    productExtraParams = this.mISExtraParameters;
                } else {
                    if (productType.equalsIgnoreCase(ProductType.OfferWall.toString())) {
                        appKey = this.mOWAppKey;
                        userId = this.mOWUserId;
                        productExtraParams = this.mOWExtraParameters;
                    }
                }
            }
            try {
                jsObj.put(ParametersKeys.PRODUCT_TYPE, productType);
            } catch (JSONException e) {
                e.printStackTrace();
                new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=noProductType"});
            }
        }
        if (TextUtils.isEmpty(userId)) {
            fail = true;
        } else {
            try {
                jsObj.put(SDKUtils.encodeString(RequestParameters.APPLICATION_USER_ID), SDKUtils.encodeString(userId));
            } catch (JSONException e2) {
                e2.printStackTrace();
                new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=encodeAppUserId"});
            }
        }
        if (TextUtils.isEmpty(appKey)) {
            fail = true;
        } else {
            try {
                jsObj.put(SDKUtils.encodeString(RequestParameters.APPLICATION_KEY), SDKUtils.encodeString(appKey));
            } catch (JSONException e22) {
                e22.printStackTrace();
                new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=encodeAppKey"});
            }
        }
        if (!(productExtraParams == null || productExtraParams.isEmpty())) {
            for (Entry<String, String> entry : productExtraParams.entrySet()) {
                if (((String) entry.getKey()).equalsIgnoreCase("sdkWebViewCache")) {
                    setWebviewCache((String) entry.getValue());
                }
                try {
                    jsObj.put(SDKUtils.encodeString((String) entry.getKey()), SDKUtils.encodeString((String) entry.getValue()));
                } catch (JSONException e222) {
                    e222.printStackTrace();
                    new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=extraParametersToJson"});
                }
            }
        }
        return new Object[]{jsObj.toString(), Boolean.valueOf(fail)};
    }

    private Object[] getAppsStatus(String appIds, String requestId) {
        boolean fail = false;
        JSONObject result = new JSONObject();
        try {
            if (!TextUtils.isEmpty(appIds)) {
                if (!appIds.equalsIgnoreCase("null")) {
                    if (!TextUtils.isEmpty(requestId)) {
                        if (!requestId.equalsIgnoreCase("null")) {
                            List<ApplicationInfo> packages = DeviceStatus.getInstalledApplications(getContext());
                            JSONArray appIdsArray = new JSONArray(appIds);
                            JSONObject bundleIds = new JSONObject();
                            for (int i = 0; i < appIdsArray.length(); i++) {
                                String appId = appIdsArray.getString(i).trim();
                                if (!TextUtils.isEmpty(appId)) {
                                    JSONObject isInstalled = new JSONObject();
                                    boolean found = false;
                                    for (ApplicationInfo packageInfo : packages) {
                                        if (appId.equalsIgnoreCase(packageInfo.packageName)) {
                                            isInstalled.put(IS_INSTALLED, true);
                                            bundleIds.put(appId, isInstalled);
                                            found = true;
                                            break;
                                        }
                                    }
                                    if (!found) {
                                        isInstalled.put(IS_INSTALLED, false);
                                        bundleIds.put(appId, isInstalled);
                                    }
                                }
                            }
                            result.put(RESULT, bundleIds);
                            result.put(REQUEST_ID, requestId);
                            return new Object[]{result.toString(), Boolean.valueOf(fail)};
                        }
                    }
                    fail = true;
                    result.put(NativeProtocol.BRIDGE_ARG_ERROR_BUNDLE, "requestId is null or empty");
                    return new Object[]{result.toString(), Boolean.valueOf(fail)};
                }
            }
            fail = true;
            result.put(NativeProtocol.BRIDGE_ARG_ERROR_BUNDLE, "appIds is null or empty");
        } catch (Exception e) {
            fail = true;
        }
        return new Object[]{result.toString(), Boolean.valueOf(fail)};
    }

    public void onFileDownloadSuccess(SSAFile file) {
        if (file.getFile().contains(Constants.MOBILE_CONTROLLER_HTML)) {
            load(1);
        } else {
            assetCached(file.getFile(), file.getPath());
        }
    }

    public void onFileDownloadFail(SSAFile file) {
        if (file.getFile().contains(Constants.MOBILE_CONTROLLER_HTML)) {
            this.mGlobalControllerTimer.cancel();
            for (DemandSource demandSource : IronSourceAdsPublisherAgent.getInstance((Activity) getCurrentActivityContext()).getDemandSources()) {
                if (demandSource.getDemandSourceInitState() == 1) {
                    sendProductErrorMessage(ProductType.RewardedVideo, demandSource.getDemandSourceName());
                }
            }
            if (this.mISmiss) {
                sendProductErrorMessage(ProductType.Interstitial, null);
            }
            if (this.mOWmiss) {
                sendProductErrorMessage(ProductType.OfferWall, null);
            }
            if (this.mOWCreditsMiss) {
                sendProductErrorMessage(ProductType.OfferWallCredits, null);
                return;
            }
            return;
        }
        assetCachedFailed(file.getFile(), file.getPath(), file.getErrMsg());
    }

    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        Logger.i(this.TAG, url + " " + mimetype);
    }

    private void toastingErrMsg(final String methodName, String value) {
        final String message = new SSAObj(value).getString(ParametersKeys.ERR_MSG);
        if (!TextUtils.isEmpty(message)) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (IronSourceWebView.this.getDebugMode() == DebugMode.MODE_3.getValue()) {
                        Toast.makeText(IronSourceWebView.this.getCurrentActivityContext(), methodName + " : " + message, 1).show();
                    }
                }
            });
        }
    }

    public void setControllerKeyPressed(String value) {
        this.mControllerKeyPressed = value;
    }

    public String getControllerKeyPressed() {
        String keyPressed = this.mControllerKeyPressed;
        setControllerKeyPressed("interrupt");
        return keyPressed;
    }

    public void runGenericFunction(String method, Map<String, String> keyValPairs, OnGenericFunctionListener listener) {
        this.mOnGenericFunctionListener = listener;
        if (JSMethods.INIT_REWARDED_VIDEO.equalsIgnoreCase(method)) {
            initRewardedVideo((String) keyValPairs.get(RequestParameters.APPLICATION_USER_ID), (String) keyValPairs.get(RequestParameters.APPLICATION_KEY), (String) keyValPairs.get(RequestParameters.DEMAND_SOURCE_NAME), this.mOnRewardedVideoListener);
        } else if (JSMethods.SHOW_REWARDED_VIDEO.equalsIgnoreCase(method)) {
            showRewardedVideo((String) keyValPairs.get(RequestParameters.DEMAND_SOURCE_NAME));
        } else {
            injectJavascript(generateJSToInject(method, mapToJson(keyValPairs), JSMethods.ON_GENERIC_FUNCTION_SUCCESS, JSMethods.ON_GENERIC_FUNCTION_FAIL));
        }
    }

    public void deviceStatusChanged(String networkType) {
        injectJavascript(generateJSToInject(JSMethods.DEVICE_STATUS_CHANGED, parseToJson(RequestParameters.CONNECTION_TYPE, networkType, null, null, null, null, null, null, null, false)));
    }

    public void engageEnd(String action) {
        if (action.equals(ParametersKeys.FORCE_CLOSE)) {
            closeWebView();
        }
        injectJavascript(generateJSToInject(JSMethods.ENGAGE_END, parseToJson(ParametersKeys.ACTION, action, null, null, null, null, null, null, null, false)));
    }

    public void registerConnectionReceiver(Context context) {
        context.registerReceiver(this.mConnectionReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    public void unregisterConnectionReceiver(Context context) {
        try {
            context.unregisterReceiver(this.mConnectionReceiver);
        } catch (IllegalArgumentException e) {
        } catch (Exception e1) {
            Log.e(this.TAG, "unregisterConnectionReceiver - " + e1);
            new IronSourceAsyncHttpRequestTask().execute(new String[]{Constants.NATIVE_EXCEPTION_BASE_URL + e1.getStackTrace()[0].getMethodName()});
        }
    }

    public void pause() {
        if (VERSION.SDK_INT > 10) {
            try {
                onPause();
            } catch (Throwable e) {
                Logger.i(this.TAG, "WebViewController: pause() - " + e);
                new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=webviewPause"});
            }
        }
    }

    public void resume() {
        if (VERSION.SDK_INT > 10) {
            try {
                onResume();
            } catch (Throwable e) {
                Logger.i(this.TAG, "WebViewController: onResume() - " + e);
                new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=webviewResume"});
            }
        }
    }

    public void setOnWebViewControllerChangeListener(OnWebViewChangeListener listener) {
        this.mChangeListener = listener;
    }

    public FrameLayout getLayout() {
        return this.mControllerLayout;
    }

    public boolean inCustomView() {
        return this.mCustomView != null;
    }

    public void hideCustomView() {
        this.mWebChromeClient.onHideCustomView();
    }

    private void setWebviewCache(String value) {
        if (value.equalsIgnoreCase(AppEventsConstants.EVENT_PARAM_VALUE_NO)) {
            getSettings().setCacheMode(2);
        } else {
            getSettings().setCacheMode(-1);
        }
    }

    public boolean handleSearchKeysURLs(String url) throws Exception {
        List<String> searchKeys = IronSourceSharedPrefHelper.getSupersonicPrefHelper().getSearchKeys();
        if (searchKeys != null) {
            try {
                if (!searchKeys.isEmpty()) {
                    for (String key : searchKeys) {
                        if (url.contains(key)) {
                            UrlHandler.openUrl(getCurrentActivityContext(), url);
                            return true;
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    public void setState(State state) {
        this.mState = state;
    }

    public State getState() {
        return this.mState;
    }

    private void sendProductErrorMessage(ProductType type, String demnadSourceName) {
        String action = BuildConfig.FLAVOR;
        switch (AnonymousClass8.$SwitchMap$com$ironsource$sdk$data$SSAEnums$ProductType[type.ordinal()]) {
            case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                action = ErrorCodes.InitRV;
                break;
            case R.styleable.View_paddingStart /*2*/:
                action = ErrorCodes.InitIS;
                break;
            case Cocos2dxEditBox.kEndActionReturn /*3*/:
                action = ErrorCodes.InitOW;
                break;
            case R.styleable.View_theme /*4*/:
                action = ErrorCodes.ShowOWCredits;
                break;
        }
        triggerOnControllerInitProductFail(SDKUtils.createErrorMessage(action, ErrorCodes.InitiatingController), type, demnadSourceName);
    }

    public void destroy() {
        super.destroy();
        if (this.downloadManager != null) {
            this.downloadManager.release();
        }
        if (this.mConnectionReceiver != null) {
            this.mConnectionReceiver = null;
        }
        this.mUiHandler = null;
        this.mCurrentActivityContext = null;
    }

    private String generateJSToInject(String funToCall) {
        StringBuilder script = new StringBuilder();
        script.append("SSA_CORE.SDKController.runFunction('").append(funToCall).append("');");
        return script.toString();
    }

    private String generateJSToInject(String funToCall, String parameters) {
        StringBuilder script = new StringBuilder();
        script.append("SSA_CORE.SDKController.runFunction('").append(funToCall).append("?parameters=").append(parameters).append("');");
        return script.toString();
    }

    private String generateJSToInject(String funToCall, String successFunc, String failFunc) {
        StringBuilder script = new StringBuilder();
        script.append("SSA_CORE.SDKController.runFunction('").append(funToCall).append("','").append(successFunc).append("','").append(failFunc).append("');");
        return script.toString();
    }

    private String generateJSToInject(String funToCall, String parameters, String successFunc, String failFunc) {
        StringBuilder script = new StringBuilder();
        script.append("SSA_CORE.SDKController.runFunction('").append(funToCall).append("?parameters=").append(parameters).append("','").append(successFunc).append("','").append(failFunc).append("');");
        return script.toString();
    }

    public AdUnitsState getSavedState() {
        return this.mSavedState;
    }

    public void restoreState(AdUnitsState state) {
        synchronized (this.mSavedStateLocker) {
            if (state.shouldRestore() && this.mControllerState.equals(ControllerState.Ready)) {
                String demandSourceName;
                String appKey;
                String userId;
                Log.d(this.TAG, "restoreState(state:" + state + ")");
                int lastAd = state.getDisplayedProduct();
                if (lastAd != -1) {
                    if (lastAd == ProductType.RewardedVideo.ordinal()) {
                        Log.d(this.TAG, "onRVAdClosed()");
                        demandSourceName = state.getDisplayedDemandSourceName();
                        if (!(this.mOnRewardedVideoListener == null || TextUtils.isEmpty(demandSourceName))) {
                            this.mOnRewardedVideoListener.onRVAdClosed(demandSourceName);
                        }
                    } else if (lastAd == ProductType.Interstitial.ordinal()) {
                        Log.d(this.TAG, "onInterstitialAdClosed()");
                        if (this.mOnInitInterstitialListener != null) {
                            this.mOnInitInterstitialListener.onInterstitialClose();
                        }
                    } else if (lastAd == ProductType.OfferWall.ordinal()) {
                        Log.d(this.TAG, "onOWAdClosed()");
                        if (this.mOnOfferWallListener != null) {
                            this.mOnOfferWallListener.onOWAdClosed();
                        }
                    }
                    state.adOpened(-1);
                    state.setDisplayedDemandSourceName(null);
                } else {
                    Log.d(this.TAG, "No ad was opened");
                }
                if (state.isInterstitialInitSuccess()) {
                    Log.d(this.TAG, "onInterstitialAvailability(false)");
                    Map<String, String> extraParams;
                    if (this.mOnInitInterstitialListener != null) {
                        appKey = state.getInterstitialAppKey();
                        userId = state.getInterstitialUserId();
                        extraParams = state.getInterstitialExtraParams();
                        Log.d(this.TAG, "initInterstitial(appKey:" + appKey + ", userId:" + userId + ", extraParam:" + extraParams + ")");
                        initInterstitial(appKey, userId, extraParams, this.mOnInitInterstitialListener);
                    } else {
                        appKey = state.getInterstitialAppKey();
                        userId = state.getInterstitialUserId();
                        extraParams = state.getInterstitialExtraParams();
                        Log.d(this.TAG, "initInterstitial(appKey:" + appKey + ", userId:" + userId + ", extraParam:" + extraParams + ")");
                        initInterstitial(appKey, userId, extraParams, this.mOnInitInterstitialListener);
                    }
                }
                appKey = state.getRVAppKey();
                userId = state.getRVUserId();
                for (DemandSource demandSource : IronSourceAdsPublisherAgent.getInstance((Activity) getCurrentActivityContext()).getDemandSources()) {
                    if (demandSource.getDemandSourceInitState() == 2) {
                        demandSourceName = demandSource.getDemandSourceName();
                        Log.d(this.TAG, "onRVNoMoreOffers()");
                        this.mOnRewardedVideoListener.onRVNoMoreOffers(demandSourceName);
                        initRewardedVideo(appKey, userId, demandSourceName, this.mOnRewardedVideoListener);
                    }
                }
                state.setShouldRestore(false);
            }
            this.mSavedState = state;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4) {
            return super.onKeyDown(keyCode, event);
        }
        if (this.mChangeListener.onBackButtonPressed()) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    void runOnUiThread(Runnable task) {
        this.mUiHandler.post(task);
    }
}
