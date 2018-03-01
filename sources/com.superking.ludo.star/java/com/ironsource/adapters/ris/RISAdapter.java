package com.ironsource.adapters.ris;

import android.app.Activity;
import com.ironsource.adapters.supersonicads.DemandSourceConfig;
import com.ironsource.mediationsdk.AbstractAdapter;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.sdk.InterstitialManagerListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoManagerListener;
import com.ironsource.mediationsdk.utils.ErrorBuilder;
import com.ironsource.mediationsdk.utils.IronSourceUtils;
import com.ironsource.sdk.SSAFactory;
import com.ironsource.sdk.SSAPublisher;
import com.ironsource.sdk.data.AdUnitsReady;
import com.ironsource.sdk.listeners.OnRewardedVideoListener;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import com.ironsource.sdk.utils.SDKUtils;
import java.util.HashMap;

public class RISAdapter extends AbstractAdapter implements OnRewardedVideoListener {
    private boolean hasAdAvailable = false;
    private DemandSourceConfig mAdapterConfig;
    private boolean mDidReportInitStatus = false;
    private InterstitialManagerListener mInterstitialManager;
    private SSAPublisher mSSAPublisher;

    public static RISAdapter startAdapter(String providerName, String providerUrl) {
        return new RISAdapter(providerName);
    }

    private RISAdapter(String providerName) {
        super(providerName, null);
        this.mAdapterConfig = new DemandSourceConfig(providerName);
        SDKUtils.setControllerUrl(this.mAdapterConfig.getRVDynamicControllerUrl());
        if (isAdaptersDebugEnabled()) {
            SDKUtils.setDebugMode(0);
        } else {
            SDKUtils.setDebugMode(this.mAdapterConfig.getRVDebugMode());
        }
        SDKUtils.setControllerConfig(this.mAdapterConfig.getRVControllerConfig());
    }

    public int getMaxRVAdsPerIteration() {
        return 0;
    }

    public int getMaxISAdsPerIteration() {
        return this.mAdapterConfig.getMaxISAdsPerIteration();
    }

    public String getVersion() {
        return IronSourceUtils.getSDKVersion();
    }

    public String getCoreSDKVersion() {
        return SDKUtils.getSDKVersion();
    }

    public void initInterstitial(final Activity activity, final String appKey, final String userId) {
        log(IronSourceTag.ADAPTER_API, getProviderName() + ":initInterstitial(userId:" + userId + ")", 1);
        activity.runOnUiThread(new Runnable() {
            public void run() {
                RISAdapter.this.mSSAPublisher = SSAFactory.getPublisherInstance(activity);
                SSAFactory.getPublisherInstance(activity).initRewardedVideo(appKey, userId, RISAdapter.this.getProviderName(), new HashMap(), RISAdapter.this);
            }
        });
    }

    public void loadInterstitial() {
        if (this.mInterstitialManager == null) {
            return;
        }
        if (this.hasAdAvailable) {
            this.mInterstitialManager.onInterstitialAdReady(this);
        } else {
            this.mInterstitialManager.onInterstitialAdLoadFailed(ErrorBuilder.buildLoadFailedError("No ad available"), this);
        }
    }

    public void showInterstitial() {
    }

    public void showInterstitial(String placementName) {
        log(IronSourceTag.ADAPTER_API, getProviderName() + ":showRewardedVideo(placement:" + placementName + ")", 1);
        if (this.mSSAPublisher != null) {
            this.mSSAPublisher.showRewardedVideo(getProviderName());
        } else if (this.mInterstitialManager != null) {
            this.mInterstitialManager.onInterstitialAdShowFailed(new IronSourceError(IronSourceError.ERROR_CODE_NO_ADS_TO_SHOW, "Please call init before calling showRewardedVideo"), this);
        }
    }

    public boolean isInterstitialReady() {
        return this.hasAdAvailable;
    }

    public void setRewardedVideoListener(RewardedVideoManagerListener manager) {
    }

    public void initRewardedVideo(Activity activity, String appKey, String userId) {
    }

    public void showRewardedVideo() {
    }

    public boolean isRewardedVideoAvailable() {
        return false;
    }

    public void showRewardedVideo(String placementName) {
    }

    public void setInterstitialListener(InterstitialManagerListener manager) {
        this.mInterstitialManager = manager;
    }

    public void onResume(Activity activity) {
        if (this.mSSAPublisher != null) {
            this.mSSAPublisher.onResume(activity);
        }
    }

    public void onPause(Activity activity) {
        if (this.mSSAPublisher != null) {
            this.mSSAPublisher.onPause(activity);
        }
    }

    public void setAge(int age) {
        this.mAdapterConfig.setUserAgeGroup(age);
    }

    public void setGender(String gender) {
        this.mAdapterConfig.setUserGender(gender);
    }

    public void setMediationSegment(String segment) {
        this.mAdapterConfig.setMediationSegment(segment);
    }

    public void onRVInitSuccess(AdUnitsReady adUnitsReady) {
        int numOfAdUnits = 0;
        try {
            numOfAdUnits = Integer.parseInt(adUnitsReady.getNumOfAdUnits());
        } catch (NumberFormatException e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceTag.NATIVE, "onRVInitSuccess:parseInt()", e);
        }
        this.hasAdAvailable = numOfAdUnits > 0;
        if (this.mInterstitialManager != null && !this.mDidReportInitStatus) {
            this.mDidReportInitStatus = true;
            this.mInterstitialManager.onInterstitialInitSuccess(this);
        }
    }

    public void onRVInitFail(String description) {
        this.hasAdAvailable = false;
        if (this.mInterstitialManager != null && !this.mDidReportInitStatus) {
            this.mDidReportInitStatus = true;
            this.mInterstitialManager.onInterstitialInitFailed(ErrorBuilder.buildInitFailedError("Adapter initialization failure - " + getProviderName() + " - " + description, ParametersKeys.INTERSTITIAL), this);
        }
    }

    public void onRVNoMoreOffers() {
        if (this.mInterstitialManager != null && !this.mDidReportInitStatus) {
            this.mDidReportInitStatus = true;
            this.mInterstitialManager.onInterstitialInitSuccess(this);
        }
    }

    public void onRVAdCredited(int credits) {
        if (this.mRewardedInterstitialManager != null) {
            this.mRewardedInterstitialManager.onInterstitialAdRewarded(this);
        }
    }

    public void onRVAdClosed() {
        if (this.mInterstitialManager != null) {
            this.mInterstitialManager.onInterstitialAdClosed(this);
        }
    }

    public void onRVAdOpened() {
        if (this.mInterstitialManager != null) {
            this.mInterstitialManager.onInterstitialAdShowSucceeded(this);
            this.mInterstitialManager.onInterstitialAdOpened(this);
        }
    }

    public void onRVShowFail(String description) {
        if (this.mInterstitialManager != null) {
            this.mInterstitialManager.onInterstitialAdShowFailed(new IronSourceError(IronSourceError.ERROR_CODE_NO_ADS_TO_SHOW, "Show Failed"), this);
        }
    }

    public void onRVAdClicked() {
        if (this.mInterstitialManager != null) {
            this.mInterstitialManager.onInterstitialAdClicked(this);
        }
    }
}
