package com.ironsource.adapters.unityads;

import android.app.Activity;
import android.text.TextUtils;
import com.ironsource.mediationsdk.AbstractAdapter;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.sdk.InterstitialManagerListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoManagerListener;
import com.ironsource.mediationsdk.utils.ErrorBuilder;
import com.ironsource.mediationsdk.utils.IronSourceConstants;
import com.ironsource.mediationsdk.utils.RewardedVideoHelper;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAds.FinishState;
import com.unity3d.ads.UnityAds.UnityAdsError;
import com.unity3d.ads.metadata.MediationMetaData;
import com.unity3d.ads.metadata.PlayerMetaData;
import java.util.ArrayList;

class UnityAdsAdapter extends AbstractAdapter implements IUnityAdsListener {
    private static final String VERSION = "3.0.3";
    private final String CORE_SDK_VERSION = "2.0.5";
    private final String DEFAULT_PLACEMENT_ID = "rewardedVideoZone";
    private Activity mActivity;
    private UnityAdsConfig mAdapterConfig = new UnityAdsConfig();
    private boolean mDidCallLoad = false;
    private boolean mDidInit = false;
    private InterstitialManagerListener mInterstitialManager;
    private RewardedVideoHelper mRewardedVideoHelper = new RewardedVideoHelper();
    private RewardedVideoManagerListener mRewardedVideoManager;
    private String mServerId;

    public static UnityAdsAdapter startAdapter(String providerName, String providerUrl) {
        return new UnityAdsAdapter(providerName, providerUrl);
    }

    private UnityAdsAdapter(String providerName, String providerUrl) {
        super(providerName, providerUrl);
    }

    public int getMaxRVAdsPerIteration() {
        return this.mAdapterConfig.getMaxRVAdsPerIteration();
    }

    public int getMaxISAdsPerIteration() {
        return this.mAdapterConfig.getMaxISAdsPerIteration();
    }

    public String getVersion() {
        return VERSION;
    }

    public String getCoreSDKVersion() {
        return "2.0.5";
    }

    public synchronized void initRewardedVideo(Activity activity, String appKey, String userId) {
        log(IronSourceTag.ADAPTER_API, getProviderName() + ":initRewardedVideo()", 1);
        this.mRewardedVideoHelper.reset();
        if (validateConfigBeforeInitAndCallAvailabilityChangedForInvalid(this.mAdapterConfig, this.mRewardedVideoManager).isValid()) {
            this.mRewardedVideoHelper.reset();
            this.mRewardedVideoHelper.setMaxVideo(this.mAdapterConfig.getMaxVideos());
            startRVTimer(this.mRewardedVideoManager);
            if (this.mDidInit) {
                boolean shouldTriggerCallback = this.mRewardedVideoHelper.setVideoAvailability(UnityAds.isReady(this.mAdapterConfig.getRVPlacementId()));
                if (this.mRewardedVideoManager != null && shouldTriggerCallback) {
                    this.mRewardedVideoManager.onRewardedVideoAvailabilityChanged(this.mRewardedVideoHelper.isVideoAvailable(), this);
                }
            } else {
                initSDK(activity, this.mAdapterConfig.getRVGameId(), userId);
            }
        }
    }

    private synchronized void initSDK(Activity activity, String game_id, String userId) {
        this.mDidInit = true;
        this.mServerId = userId;
        this.mActivity = activity;
        MediationMetaData mediationMetaData = new MediationMetaData(activity);
        mediationMetaData.setName("IronSource");
        mediationMetaData.setVersion(VERSION);
        mediationMetaData.commit();
        UnityAds.setDebugMode(false);
        UnityAds.initialize(activity, game_id, this);
        boolean isDebugEnabled = false;
        try {
            isDebugEnabled = isAdaptersDebugEnabled();
        } catch (NoSuchMethodError e) {
        }
        UnityAds.setDebugMode(isDebugEnabled);
        log(IronSourceTag.ADAPTER_API, getProviderName() + ":init(userId:" + userId + " , gameId:" + game_id + ")", 1);
    }

    public void onResume(Activity activity) {
        log(IronSourceTag.ADAPTER_API, getProviderName() + ":onResume()", 1);
    }

    public void onPause(Activity activity) {
        log(IronSourceTag.ADAPTER_API, getProviderName() + ":onPause()", 1);
    }

    public void setAge(int age) {
    }

    public void setGender(String gender) {
    }

    public void setMediationSegment(String segment) {
    }

    public void showRewardedVideo() {
    }

    public void showRewardedVideo(String placementName) {
        boolean shouldNotify;
        log(IronSourceTag.ADAPTER_API, getProviderName() + ":showRewardedVideo(placement:" + placementName + ")", 1);
        String rvPlacementId = this.mAdapterConfig.getRVPlacementId();
        if (TextUtils.isEmpty(rvPlacementId)) {
            rvPlacementId = "rewardedVideoZone";
            IronSourceLoggerManager.getLogger().log(IronSourceTag.ADAPTER_API, getProviderName() + ":rvPlacementId doesn't exist in configuration, value was set to 'rewardedVideoZone'. Edit configurations file in order to change the value", 2);
        }
        if (UnityAds.isReady(rvPlacementId) && this.mRewardedVideoHelper.isVideoAvailable()) {
            shouldNotify = this.mRewardedVideoHelper.increaseCurrentVideo();
            ArrayList<String> validateBeforeShow = new ArrayList();
            validateBeforeShow.add("zoneId");
            this.mAdapterConfig.validateOptionalKeys(validateBeforeShow);
            PlayerMetaData playerMetaData = new PlayerMetaData(this.mActivity);
            playerMetaData.setServerId(this.mServerId);
            playerMetaData.commit();
            UnityAds.show(this.mActivity, rvPlacementId);
            this.mRewardedVideoHelper.setPlacementName(placementName);
        } else {
            shouldNotify = this.mRewardedVideoHelper.setVideoAvailability(false);
            this.mRewardedVideoManager.onRewardedVideoAdShowFailed(ErrorBuilder.buildNoAdsToShowError(IronSourceConstants.REWARDED_VIDEO_AD_UNIT), this);
        }
        if (shouldNotify && this.mRewardedVideoManager != null) {
            this.mRewardedVideoManager.onRewardedVideoAvailabilityChanged(this.mRewardedVideoHelper.isVideoAvailable(), this);
        }
    }

    public boolean isRewardedVideoAvailable() {
        this.mRewardedVideoHelper.setVideoAvailability(UnityAds.isReady(this.mAdapterConfig.getRVPlacementId()));
        boolean result = this.mRewardedVideoHelper.isVideoAvailable();
        log(IronSourceTag.ADAPTER_API, getProviderName() + ":isRewardedVideoAvailable(): " + result, 1);
        return result;
    }

    public void setRewardedVideoListener(RewardedVideoManagerListener manager) {
        this.mRewardedVideoManager = manager;
    }

    public void setInterstitialListener(InterstitialManagerListener manager) {
        this.mInterstitialManager = manager;
    }

    public synchronized void initInterstitial(Activity activity, String appKey, String userId) {
        log(IronSourceTag.ADAPTER_API, getProviderName() + ":initInterstitial()", 1);
        if (validateConfigBeforeInitAndCallInitFailForInvalid(this.mAdapterConfig, this.mInterstitialManager).isValid()) {
            if (!this.mDidInit) {
                initSDK(activity, this.mAdapterConfig.getISGameId(), userId);
            }
            if (this.mInterstitialManager != null) {
                this.mInterstitialManager.onInterstitialInitSuccess(this);
            }
        }
    }

    public void loadInterstitial() {
        if (!UnityAds.isReady(this.mAdapterConfig.getISPlacementId())) {
            this.mDidCallLoad = true;
            startISLoadTimer(this.mInterstitialManager);
        } else if (this.mInterstitialManager != null) {
            this.mInterstitialManager.onInterstitialAdReady(this);
        }
    }

    public void showInterstitial() {
    }

    public void showInterstitial(String placementName) {
        String isPlacementId = this.mAdapterConfig.getISPlacementId();
        if (UnityAds.isReady(isPlacementId)) {
            PlayerMetaData playerMetaData = new PlayerMetaData(this.mActivity);
            playerMetaData.setServerId(this.mServerId);
            playerMetaData.commit();
            UnityAds.show(this.mActivity, isPlacementId);
        } else if (this.mInterstitialManager != null) {
            this.mInterstitialManager.onInterstitialAdShowFailed(ErrorBuilder.buildNoAdsToShowError(ParametersKeys.INTERSTITIAL), this);
        }
    }

    public boolean isInterstitialReady() {
        return UnityAds.isReady(this.mAdapterConfig.getISPlacementId());
    }

    public void onUnityAdsReady(String placementId) {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, "onUnityAdsReady(placementId: " + placementId + ")", 1);
        if (!TextUtils.isEmpty(placementId)) {
            if (placementId.equals(this.mAdapterConfig.getRVPlacementId())) {
                cancelRVTimer();
                boolean shouldTriggerCallback = this.mRewardedVideoHelper.setVideoAvailability(true);
                if (this.mRewardedVideoManager != null && shouldTriggerCallback) {
                    this.mRewardedVideoManager.onRewardedVideoAvailabilityChanged(this.mRewardedVideoHelper.isVideoAvailable(), this);
                }
            } else if (placementId.equals(this.mAdapterConfig.getISPlacementId()) && this.mDidCallLoad) {
                this.mDidCallLoad = false;
                cancelISLoadTimer();
                if (this.mInterstitialManager != null) {
                    this.mInterstitialManager.onInterstitialAdReady(this);
                }
            }
        }
    }

    public void onUnityAdsStart(String placementId) {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, "onUnityAdsStart(placementId: " + placementId + ")", 1);
        if (!TextUtils.isEmpty(placementId)) {
            if (placementId.equals(this.mAdapterConfig.getRVPlacementId())) {
                if (this.mRewardedVideoManager != null) {
                    this.mRewardedVideoManager.onRewardedVideoAdOpened(this);
                    this.mRewardedVideoManager.onRewardedVideoAdStarted(this);
                }
            } else if (placementId.equals(this.mAdapterConfig.getISPlacementId()) && this.mInterstitialManager != null) {
                this.mInterstitialManager.onInterstitialAdOpened(this);
                this.mInterstitialManager.onInterstitialAdShowSucceeded(this);
            }
        }
    }

    public void onUnityAdsFinish(String placementId, FinishState finishState) {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, "onUnityAdsFinish(placementId: " + placementId + ", finishState: " + finishState + ")", 1);
        if (!TextUtils.isEmpty(placementId)) {
            if (placementId.equals(this.mAdapterConfig.getRVPlacementId())) {
                if (this.mRewardedVideoHelper.setVideoAvailability(UnityAds.isReady(this.mAdapterConfig.getRVPlacementId()))) {
                    this.mRewardedVideoManager.onRewardedVideoAvailabilityChanged(this.mRewardedVideoHelper.isVideoAvailable(), this);
                }
                if (this.mRewardedVideoManager != null) {
                    if (finishState.equals(FinishState.COMPLETED)) {
                        this.mRewardedVideoManager.onRewardedVideoAdEnded(this);
                        this.mRewardedVideoManager.onRewardedVideoAdRewarded(this.mRewardedVideoConfig.getRewardedVideoPlacement(this.mRewardedVideoHelper.getPlacementName()), this);
                    }
                    this.mRewardedVideoManager.onRewardedVideoAdClosed(this);
                }
            } else if (placementId.equals(this.mAdapterConfig.getISPlacementId()) && this.mInterstitialManager != null) {
                this.mInterstitialManager.onInterstitialAdClosed(this);
            }
        }
    }

    public void onUnityAdsError(UnityAdsError unityAdsError, String errorMessage) {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, "onUnityAdsError(errorType: " + unityAdsError + ", errorMessage: " + errorMessage + ")", 1);
    }
}
