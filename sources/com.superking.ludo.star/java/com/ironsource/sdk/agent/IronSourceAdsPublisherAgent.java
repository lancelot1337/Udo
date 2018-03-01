package com.ironsource.sdk.agent;

import android.app.Activity;
import android.content.Context;
import android.content.MutableContextWrapper;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import com.ironsource.sdk.SSAPublisher;
import com.ironsource.sdk.controller.IronSourceWebView;
import com.ironsource.sdk.data.AdUnitsReady;
import com.ironsource.sdk.data.DemandSource;
import com.ironsource.sdk.data.SSASession;
import com.ironsource.sdk.data.SSASession.SessionType;
import com.ironsource.sdk.listeners.DSRewardedVideoListener;
import com.ironsource.sdk.listeners.OnGenericFunctionListener;
import com.ironsource.sdk.listeners.OnInterstitialListener;
import com.ironsource.sdk.listeners.OnOfferWallListener;
import com.ironsource.sdk.listeners.OnRewardedVideoListener;
import com.ironsource.sdk.utils.Constants;
import com.ironsource.sdk.utils.DeviceProperties;
import com.ironsource.sdk.utils.IronSourceAsyncHttpRequestTask;
import com.ironsource.sdk.utils.IronSourceSharedPrefHelper;
import com.ironsource.sdk.utils.Logger;
import com.ironsource.sdk.utils.SDKUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class IronSourceAdsPublisherAgent implements SSAPublisher, DSRewardedVideoListener {
    private static final String TAG = "IronSourceAdsPublisherAgent";
    private static MutableContextWrapper mutableContextWrapper;
    private static IronSourceAdsPublisherAgent sInstance;
    private Map<String, DemandSource> mDemandSourceMap = new HashMap();
    private SSASession session;
    private IronSourceWebView wvc;

    private IronSourceAdsPublisherAgent(final Activity activity, int debugMode) {
        IronSourceSharedPrefHelper.getSupersonicPrefHelper(activity);
        Logger.enableLogging(SDKUtils.getDebugMode());
        Logger.i(TAG, "C'tor");
        mutableContextWrapper = new MutableContextWrapper(activity);
        activity.runOnUiThread(new Runnable() {
            public void run() {
                IronSourceAdsPublisherAgent.this.wvc = new IronSourceWebView(IronSourceAdsPublisherAgent.mutableContextWrapper);
                IronSourceAdsPublisherAgent.this.wvc.registerConnectionReceiver(activity);
                IronSourceAdsPublisherAgent.this.wvc.setDebugMode(SDKUtils.getDebugMode());
                IronSourceAdsPublisherAgent.this.wvc.downloadController();
            }
        });
        startSession(activity);
    }

    public static synchronized IronSourceAdsPublisherAgent getInstance(Activity activity) {
        IronSourceAdsPublisherAgent instance;
        synchronized (IronSourceAdsPublisherAgent.class) {
            instance = getInstance(activity, 0);
        }
        return instance;
    }

    public static synchronized IronSourceAdsPublisherAgent getInstance(Activity activity, int debugMode) {
        IronSourceAdsPublisherAgent ironSourceAdsPublisherAgent;
        synchronized (IronSourceAdsPublisherAgent.class) {
            Logger.i(TAG, "getInstance()");
            if (sInstance == null) {
                sInstance = new IronSourceAdsPublisherAgent(activity, debugMode);
            } else {
                mutableContextWrapper.setBaseContext(activity);
            }
            ironSourceAdsPublisherAgent = sInstance;
        }
        return ironSourceAdsPublisherAgent;
    }

    public IronSourceWebView getWebViewController() {
        return this.wvc;
    }

    private void startSession(Context context) {
        this.session = new SSASession(context, SessionType.launched);
    }

    public void resumeSession(Context context) {
        this.session = new SSASession(context, SessionType.backFromBG);
    }

    private void endSession() {
        if (this.session != null) {
            this.session.endSession();
            IronSourceSharedPrefHelper.getSupersonicPrefHelper().addSession(this.session);
            this.session = null;
        }
    }

    public void initRewardedVideo(String applicationKey, String userId, String demandSourceName, Map<String, String> extraParameters, OnRewardedVideoListener listener) {
        this.mDemandSourceMap.put(demandSourceName, new DemandSource(demandSourceName, extraParameters, listener));
        this.wvc.initRewardedVideo(applicationKey, userId, demandSourceName, this);
    }

    public void showRewardedVideo(String demandSourceName) {
        this.wvc.showRewardedVideo(demandSourceName);
    }

    public void initOfferWall(String applicationKey, String userId, Map<String, String> extraParameters, OnOfferWallListener listener) {
        this.wvc.initOfferWall(applicationKey, userId, extraParameters, listener);
    }

    public void showOfferWall(Map<String, String> extraParameters) {
        this.wvc.showOfferWall(extraParameters);
    }

    public void getOfferWallCredits(String applicationKey, String userId, OnOfferWallListener listener) {
        this.wvc.getOfferWallCredits(applicationKey, userId, listener);
    }

    public void initInterstitial(String applicationKey, String userId, Map<String, String> extraParameters, OnInterstitialListener listener) {
        this.wvc.initInterstitial(applicationKey, userId, extraParameters, listener);
    }

    public void loadInterstitial() {
        this.wvc.loadInterstitial();
    }

    public boolean isInterstitialAdAvailable() {
        return this.wvc.isInterstitialAdAvailable();
    }

    public void showInterstitial() {
        this.wvc.showInterstitial();
    }

    public void forceShowInterstitial() {
        this.wvc.forceShowInterstitial();
    }

    public void onResume(Activity activity) {
        mutableContextWrapper.setBaseContext(activity);
        this.wvc.enterForeground();
        this.wvc.registerConnectionReceiver(activity);
        if (this.session == null) {
            resumeSession(activity);
        }
    }

    public void onPause(Activity activity) {
        try {
            this.wvc.enterBackground();
            this.wvc.unregisterConnectionReceiver(activity);
            endSession();
        } catch (Exception e) {
            e.printStackTrace();
            new IronSourceAsyncHttpRequestTask().execute(new String[]{Constants.NATIVE_EXCEPTION_BASE_URL + e.getStackTrace()[0].getMethodName()});
        }
    }

    public void release(Activity activity) {
        try {
            Logger.i(TAG, "release()");
            DeviceProperties.release();
            this.wvc.unregisterConnectionReceiver(activity);
            if (Looper.getMainLooper().equals(Looper.myLooper())) {
                this.wvc.destroy();
                this.wvc = null;
            } else {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        IronSourceAdsPublisherAgent.this.wvc.destroy();
                        IronSourceAdsPublisherAgent.this.wvc = null;
                    }
                });
            }
        } catch (Exception e) {
        }
        sInstance = null;
        endSession();
    }

    public void runGenericFunction(String method, Map<String, String> keyValPairs, OnGenericFunctionListener listener) {
        this.wvc.runGenericFunction(method, keyValPairs, listener);
    }

    public void onRVInitSuccess(AdUnitsReady adUnitsReady, String demandSourceName) {
        DemandSource demandSource = getDemandSourceByName(demandSourceName);
        if (demandSource != null) {
            demandSource.setDemandSourceInitState(2);
            OnRewardedVideoListener listener = demandSource.getListener();
            if (listener != null) {
                listener.onRVInitSuccess(adUnitsReady);
            }
        }
    }

    public void onRVInitFail(String description, String demandSourceName) {
        DemandSource demandSource = getDemandSourceByName(demandSourceName);
        if (demandSource != null) {
            demandSource.setDemandSourceInitState(3);
            OnRewardedVideoListener listener = demandSource.getListener();
            if (listener != null) {
                listener.onRVInitFail(description);
            }
        }
    }

    public void onRVNoMoreOffers(String demandSourceName) {
        DemandSource demandSource = getDemandSourceByName(demandSourceName);
        if (demandSource != null) {
            OnRewardedVideoListener listener = demandSource.getListener();
            if (listener != null) {
                listener.onRVNoMoreOffers();
            }
        }
    }

    public void onRVAdCredited(int credits, String demandSourceName) {
        DemandSource demandSource = getDemandSourceByName(demandSourceName);
        if (demandSource != null) {
            OnRewardedVideoListener listener = demandSource.getListener();
            if (listener != null) {
                listener.onRVAdCredited(credits);
            }
        }
    }

    public void onRVAdClosed(String demandSourceName) {
        DemandSource demandSource = getDemandSourceByName(demandSourceName);
        if (demandSource != null) {
            OnRewardedVideoListener listener = demandSource.getListener();
            if (listener != null) {
                listener.onRVAdClosed();
            }
        }
    }

    public void onRVAdOpened(String demandSourceName) {
        DemandSource demandSource = getDemandSourceByName(demandSourceName);
        if (demandSource != null) {
            OnRewardedVideoListener listener = demandSource.getListener();
            if (listener != null) {
                listener.onRVAdOpened();
            }
        }
    }

    public void onRVShowFail(String description, String demandSourceName) {
        DemandSource demandSource = getDemandSourceByName(demandSourceName);
        if (demandSource != null) {
            OnRewardedVideoListener listener = demandSource.getListener();
            if (listener != null) {
                listener.onRVShowFail(description);
            }
        }
    }

    public void onRVAdClicked(String demandSourceName) {
        DemandSource demandSource = getDemandSourceByName(demandSourceName);
        if (demandSource != null) {
            OnRewardedVideoListener listener = demandSource.getListener();
            if (listener != null) {
                listener.onRVAdClicked();
            }
        }
    }

    public Collection<DemandSource> getDemandSources() {
        return this.mDemandSourceMap.values();
    }

    public DemandSource getDemandSourceByName(String demandSourceName) {
        if (TextUtils.isEmpty(demandSourceName)) {
            return null;
        }
        return (DemandSource) this.mDemandSourceMap.get(demandSourceName);
    }
}
