package com.ironsource.adapters.supersonicads;

import android.app.Activity;
import android.text.TextUtils;
import com.ironsource.mediationsdk.AbstractAdapter;
import com.ironsource.mediationsdk.IronSourceObject;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.sdk.InternalOfferwallApi;
import com.ironsource.mediationsdk.sdk.InternalOfferwallListener;
import com.ironsource.mediationsdk.sdk.InterstitialManagerListener;
import com.ironsource.mediationsdk.sdk.OfferwallListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoManagerListener;
import com.ironsource.mediationsdk.utils.ErrorBuilder;
import com.ironsource.mediationsdk.utils.IronSourceConstants;
import com.ironsource.mediationsdk.utils.IronSourceUtils;
import com.ironsource.mediationsdk.utils.RewardedVideoHelper;
import com.ironsource.sdk.SSAFactory;
import com.ironsource.sdk.SSAPublisher;
import com.ironsource.sdk.data.AdUnitsReady;
import com.ironsource.sdk.listeners.OnInterstitialListener;
import com.ironsource.sdk.listeners.OnOfferWallListener;
import com.ironsource.sdk.listeners.OnRewardedVideoListener;
import com.ironsource.sdk.utils.Constants;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import com.ironsource.sdk.utils.SDKUtils;
import java.util.HashMap;
import java.util.Map;

class SupersonicAdsAdapter extends AbstractAdapter implements InternalOfferwallApi, OnInterstitialListener, OnOfferWallListener, OnRewardedVideoListener {
    private final String ITEM_SIGNATURE = "itemSignature";
    private final String OW_PLACEMENT_ID = Constants.PLACEMENT_ID;
    private final String SDK_PLUGIN_TYPE = "SDKPluginType";
    private final String TIMESTAMP = EventEntry.COLUMN_NAME_TIMESTAMP;
    private final String VERSION = "6.6.0";
    private DemandSourceConfig mAdapterConfig;
    private InterstitialManagerListener mInterstitialManager;
    private InternalOfferwallListener mOfferwallListener;
    private RewardedVideoHelper mRewardedVideoHelper = new RewardedVideoHelper();
    private RewardedVideoManagerListener mRewardedVideoManager;
    private SSAPublisher mSSAPublisher;

    public static SupersonicAdsAdapter startAdapter(String providerName, String providerUrl) {
        return new SupersonicAdsAdapter(providerName, providerUrl);
    }

    private SupersonicAdsAdapter(String providerName, String providerUrl) {
        super(providerName, providerUrl);
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
        return this.mAdapterConfig.getMaxRVAdsPerIteration();
    }

    public int getMaxISAdsPerIteration() {
        return this.mAdapterConfig.getMaxISAdsPerIteration();
    }

    public String getVersion() {
        return "6.6.0";
    }

    public String getCoreSDKVersion() {
        return SDKUtils.getSDKVersion();
    }

    private HashMap<String, String> getGenenralExtraParams() {
        HashMap<String, String> params = new HashMap();
        DemandSourceConfig config = this.mAdapterConfig;
        String ageGroup = config.getRVUserAgeGroup();
        if (!TextUtils.isEmpty(ageGroup)) {
            params.put("applicationUserAgeGroup", ageGroup);
        }
        String uGender = config.getRVUserGender();
        if (!TextUtils.isEmpty(uGender)) {
            params.put("applicationUserGender", uGender);
        }
        String pluginType = getPluginType();
        if (!TextUtils.isEmpty(pluginType)) {
            params.put("SDKPluginType", pluginType);
        }
        return params;
    }

    private HashMap<String, String> getRewardedVideoExtraParams() {
        HashMap<String, String> rvExtraParams = getGenenralExtraParams();
        DemandSourceConfig config = this.mAdapterConfig;
        String language = config.getLanguage();
        if (!TextUtils.isEmpty(language)) {
            rvExtraParams.put("language", language);
        }
        String maxVideoLength = config.getMaxVideoLength();
        if (!TextUtils.isEmpty(maxVideoLength)) {
            rvExtraParams.put("maxVideoLength", maxVideoLength);
        }
        String campaignId = config.getCampaignId();
        if (!TextUtils.isEmpty(campaignId)) {
            rvExtraParams.put("campaignId", campaignId);
        }
        String segment = config.getMediationSegment();
        if (!TextUtils.isEmpty(segment)) {
            rvExtraParams.put("custom_Segment", segment);
        }
        addItemNameCountSignature(rvExtraParams);
        Map<String, String> customParams = SupersonicConfig.getConfigObj().getRewardedVideoCustomParams();
        if (!(customParams == null || customParams.isEmpty())) {
            rvExtraParams.putAll(customParams);
        }
        return rvExtraParams;
    }

    private HashMap<String, String> getInterstitialExtraParams() {
        return getGenenralExtraParams();
    }

    private HashMap<String, String> getOfferwallExtraParams() {
        HashMap<String, String> owExtraParams = getGenenralExtraParams();
        String language = this.mAdapterConfig.getLanguage();
        if (!TextUtils.isEmpty(language)) {
            owExtraParams.put("language", language);
        }
        owExtraParams.put(ParametersKeys.USE_CLIENT_SIDE_CALLBACKS, String.valueOf(SupersonicConfig.getConfigObj().getClientSideCallbacks()));
        Map<String, String> customParams = SupersonicConfig.getConfigObj().getOfferwallCustomParams();
        if (!(customParams == null || customParams.isEmpty())) {
            owExtraParams.putAll(customParams);
        }
        addItemNameCountSignature(owExtraParams);
        return owExtraParams;
    }

    private void addItemNameCountSignature(HashMap<String, String> params) {
        try {
            String itemName = this.mAdapterConfig.getItemName();
            int itemCount = this.mAdapterConfig.getItemCount();
            String privateKey = this.mAdapterConfig.getPrivateKey();
            boolean shouldAddSignature = true;
            if (TextUtils.isEmpty(itemName)) {
                shouldAddSignature = false;
            } else {
                params.put("itemName", itemName);
            }
            if (TextUtils.isEmpty(privateKey)) {
                shouldAddSignature = false;
            }
            if (itemCount == -1) {
                shouldAddSignature = false;
            } else {
                params.put("itemCount", String.valueOf(itemCount));
            }
            if (shouldAddSignature) {
                int timestamp = IronSourceUtils.getCurrentTimestamp();
                params.put(EventEntry.COLUMN_NAME_TIMESTAMP, String.valueOf(timestamp));
                params.put("itemSignature", createItemSig(timestamp, itemName, itemCount, privateKey));
            }
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceTag.ADAPTER_API, " addItemNameCountSignature", e);
        }
    }

    private String createItemSig(int timestamp, String itemName, int itemCount, String privateKey) {
        return IronSourceUtils.getMD5(timestamp + itemName + itemCount + privateKey);
    }

    private String createMinimumOfferCommissionSig(double min, String privateKey) {
        return IronSourceUtils.getMD5(min + privateKey);
    }

    private String createUserCreationDateSig(String userid, String uCreationDate, String privateKey) {
        return IronSourceUtils.getMD5(userid + uCreationDate + privateKey);
    }

    public void initRewardedVideo(final Activity activity, final String appKey, final String userId) {
        log(IronSourceTag.ADAPTER_API, getProviderName() + ":initRewardedVideo(userId:" + userId + ")", 1);
        this.mRewardedVideoHelper.reset();
        if (validateConfigBeforeInitAndCallAvailabilityChangedForInvalid(this.mAdapterConfig, this.mRewardedVideoManager).isValid()) {
            this.mRewardedVideoHelper.setMaxVideo(this.mAdapterConfig.getMaxVideos());
            startRVTimer(this.mRewardedVideoManager);
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    SupersonicAdsAdapter.this.mSSAPublisher = SSAFactory.getPublisherInstance(activity);
                    HashMap<String, String> rewardedVideoExtraParams = SupersonicAdsAdapter.this.getRewardedVideoExtraParams();
                    SupersonicAdsAdapter.this.log(IronSourceTag.ADAPTER_API, SupersonicAdsAdapter.this.getProviderName() + ":initRewardedVideo(appKey:" + appKey + ", userId:" + userId + ", demandSource: " + SupersonicAdsAdapter.this.getProviderName() + ", extraParams:" + rewardedVideoExtraParams + ")", 1);
                    SupersonicAdsAdapter.this.mSSAPublisher.initRewardedVideo(appKey, userId, SupersonicAdsAdapter.this.getProviderName(), rewardedVideoExtraParams, SupersonicAdsAdapter.this);
                }
            });
        }
    }

    public void onPause(Activity activity) {
        log(IronSourceTag.ADAPTER_API, getProviderName() + ":onPause()", 1);
        if (this.mSSAPublisher != null) {
            this.mSSAPublisher.onPause(activity);
        }
    }

    public void setAge(int age) {
        this.mAdapterConfig.setUserAgeGroup(age);
        log(IronSourceTag.ADAPTER_API, getProviderName() + ":setAge(age:" + age + ")", 1);
    }

    public void setGender(String gender) {
        this.mAdapterConfig.setUserGender(gender);
        log(IronSourceTag.ADAPTER_API, getProviderName() + ":setGender(gender:" + gender + ")", 1);
    }

    public void setMediationSegment(String segment) {
        this.mAdapterConfig.setMediationSegment(segment);
        log(IronSourceTag.ADAPTER_API, getProviderName() + ":setMediationSegment(segment:" + segment + ")", 1);
    }

    public void onResume(Activity activity) {
        log(IronSourceTag.ADAPTER_API, getProviderName() + ":onResume()", 1);
        if (this.mSSAPublisher != null) {
            this.mSSAPublisher.onResume(activity);
        }
    }

    public synchronized boolean isRewardedVideoAvailable() {
        boolean availability;
        availability = this.mRewardedVideoHelper.isVideoAvailable();
        log(IronSourceTag.ADAPTER_API, getProviderName() + ":isRewardedVideoAvailable():" + availability, 1);
        return availability;
    }

    public void setRewardedVideoListener(RewardedVideoManagerListener rewardedVideoManager) {
        this.mRewardedVideoManager = rewardedVideoManager;
    }

    public void showRewardedVideo() {
    }

    public void showRewardedVideo(String placementName) {
        log(IronSourceTag.ADAPTER_API, getProviderName() + ":showRewardedVideo(placement:" + placementName + ")", 1);
        boolean shouldNotify;
        if (this.mSSAPublisher != null) {
            this.mSSAPublisher.showRewardedVideo(getProviderName());
            this.mRewardedVideoHelper.setPlacementName(placementName);
            shouldNotify = this.mRewardedVideoHelper.increaseCurrentVideo();
        } else {
            shouldNotify = this.mRewardedVideoHelper.setVideoAvailability(false);
            log(IronSourceTag.NATIVE, "Please call init before calling showRewardedVideo", 2);
            this.mRewardedVideoManager.onRewardedVideoAdShowFailed(new IronSourceError(IronSourceError.ERROR_CODE_NO_ADS_TO_SHOW, "Please call init before calling showRewardedVideo"), this);
        }
        if (this.mRewardedVideoManager != null && shouldNotify) {
            this.mRewardedVideoManager.onRewardedVideoAvailabilityChanged(this.mRewardedVideoHelper.isVideoAvailable(), this);
        }
    }

    public void onRVNoMoreOffers() {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, getProviderName() + " :onRVNoMoreOffers ", 1);
        cancelRVTimer();
        boolean shouldNotify = this.mRewardedVideoHelper.setVideoAvailability(false);
        if (this.mRewardedVideoManager != null && shouldNotify) {
            this.mRewardedVideoManager.onRewardedVideoAvailabilityChanged(this.mRewardedVideoHelper.isVideoAvailable(), this);
        }
    }

    public void onRVInitSuccess(AdUnitsReady aur) {
        boolean availability = true;
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, getProviderName() + " :onRVInitSuccess ", 1);
        cancelRVTimer();
        int numOfAdUnits = 0;
        try {
            numOfAdUnits = Integer.parseInt(aur.getNumOfAdUnits());
        } catch (NumberFormatException e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceTag.NATIVE, "onRVInitSuccess:parseInt()", e);
        }
        if (numOfAdUnits <= 0) {
            availability = false;
        }
        boolean shouldNotify = this.mRewardedVideoHelper.setVideoAvailability(availability);
        if (this.mRewardedVideoManager != null && shouldNotify) {
            this.mRewardedVideoManager.onRewardedVideoAvailabilityChanged(this.mRewardedVideoHelper.isVideoAvailable(), this);
        }
    }

    public void onRVInitFail(String error) {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, getProviderName() + " :onRVInitFail ", 1);
        cancelRVTimer();
        if (this.mRewardedVideoHelper.setVideoAvailability(false) && this.mRewardedVideoManager != null) {
            this.mRewardedVideoManager.onRewardedVideoAvailabilityChanged(this.mRewardedVideoHelper.isVideoAvailable(), this);
        }
    }

    public void onRVAdClicked() {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, getProviderName() + " :onRVAdClicked ", 1);
    }

    public void onRVShowFail(String error) {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, getProviderName() + " :onRVShowFail ", 1);
        if (this.mRewardedVideoManager != null) {
            this.mRewardedVideoManager.onRewardedVideoAdShowFailed(new IronSourceError(IronSourceError.ERROR_CODE_NO_ADS_TO_SHOW, error), this);
        }
    }

    public void onRVAdCredited(int amount) {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, getProviderName() + " :onRVAdCredited ", 1);
        if (this.mRewardedVideoManager != null) {
            this.mRewardedVideoManager.onRewardedVideoAdRewarded(this.mRewardedVideoConfig.getRewardedVideoPlacement(this.mRewardedVideoHelper.getPlacementName()), this);
        }
    }

    public void onRVAdClosed() {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, getProviderName() + " :onRVAdClosed ", 1);
        if (this.mRewardedVideoManager != null) {
            this.mRewardedVideoManager.onRewardedVideoAdClosed(this);
        }
    }

    public void onRVAdOpened() {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, getProviderName() + " :onRVAdOpened ", 1);
        if (this.mRewardedVideoManager != null) {
            this.mRewardedVideoManager.onRewardedVideoAdOpened(this);
        }
    }

    public void getOfferwallCredits() {
        if (this.mSSAPublisher != null) {
            String appKey = IronSourceObject.getInstance().getIronSourceAppKey();
            String userId = IronSourceObject.getInstance().getIronSourceUserId();
            log(IronSourceTag.ADAPTER_API, getProviderName() + ":getOfferwallCredits(appKey:" + appKey + "userId:" + userId + ")", 1);
            this.mSSAPublisher.getOfferWallCredits(appKey, userId, this);
            return;
        }
        log(IronSourceTag.NATIVE, "Please call init before calling getOfferwallCredits", 2);
    }

    public void setOfferwallListener(OfferwallListener owListener) {
    }

    public void setInternalOfferwallListener(InternalOfferwallListener listener) {
        this.mOfferwallListener = listener;
    }

    public void initOfferwall(final Activity activity, final String appKey, final String userId) {
        try {
            log(IronSourceTag.ADAPTER_API, getProviderName() + ":initOfferwall(appKey:" + appKey + ", userId:" + userId + ")", 1);
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Map<String, String> offerwallExtraParams = SupersonicAdsAdapter.this.getOfferwallExtraParams();
                    SupersonicAdsAdapter.this.mSSAPublisher = SSAFactory.getPublisherInstance(activity);
                    SupersonicAdsAdapter.this.mSSAPublisher.initOfferWall(appKey, userId, offerwallExtraParams, SupersonicAdsAdapter.this);
                }
            });
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceTag.ADAPTER_API, getProviderName() + ":initOfferwall(userId:" + userId + ")", e);
            this.mOfferwallListener.onOfferwallAvailable(false, ErrorBuilder.buildInitFailedError("Adapter initialization failure - " + getProviderName() + " - " + e.getMessage(), IronSourceConstants.OFFERWALL_AD_UNIT));
        }
    }

    public void showOfferwall() {
    }

    public void showOfferwall(String placementId) {
        Map<String, String> offerwallExtraParams = getOfferwallExtraParams();
        if (offerwallExtraParams != null) {
            offerwallExtraParams.put(Constants.PLACEMENT_ID, placementId);
        }
        log(IronSourceTag.ADAPTER_API, getProviderName() + ":showOfferwall(" + "extraParams:" + offerwallExtraParams + ")", 1);
        if (this.mSSAPublisher != null) {
            this.mSSAPublisher.showOfferWall(offerwallExtraParams);
        } else {
            log(IronSourceTag.NATIVE, "Please call init before calling showOfferwall", 2);
        }
    }

    public boolean isOfferwallAvailable() {
        return true;
    }

    public void onOWShowSuccess(String placementId) {
        if (TextUtils.isEmpty(placementId)) {
            log(IronSourceTag.ADAPTER_API, getProviderName() + ":onOWShowSuccess()", 1);
        } else {
            log(IronSourceTag.ADAPTER_API, getProviderName() + ":onOWShowSuccess(placementId:" + placementId + ")", 1);
        }
        if (this.mOfferwallListener != null) {
            this.mOfferwallListener.onOfferwallOpened();
        }
    }

    public void onOWShowFail(String desc) {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, getProviderName() + " :onOWShowFail ", 1);
        if (this.mOfferwallListener != null) {
            this.mOfferwallListener.onOfferwallShowFailed(ErrorBuilder.buildGenericError(desc));
        }
    }

    public void onOWGeneric(String arg0, String arg1) {
    }

    public boolean onOWAdCredited(int credits, int totalCredits, boolean totalCreditsFlag) {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, getProviderName() + " :onOWAdCredited ", 1);
        if (this.mOfferwallListener == null || !this.mOfferwallListener.onOfferwallAdCredited(credits, totalCredits, totalCreditsFlag)) {
            return false;
        }
        return true;
    }

    public void onOWAdClosed() {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, getProviderName() + " :onOWAdClosed ", 1);
        if (this.mOfferwallListener != null) {
            this.mOfferwallListener.onOfferwallClosed();
        }
    }

    public void onGetOWCreditsFailed(String desc) {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, getProviderName() + " :onGetOWCreditsFailed ", 1);
        if (this.mOfferwallListener != null) {
            this.mOfferwallListener.onGetOfferwallCreditsFailed(ErrorBuilder.buildGenericError(desc));
        }
    }

    public void onOfferwallInitSuccess() {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, getProviderName() + " :onOfferwallInitSuccess ", 1);
        if (this.mOfferwallListener != null) {
            this.mOfferwallListener.onOfferwallAvailable(true);
        }
    }

    public void onOfferwallInitFail(String description) {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, getProviderName() + " :onOfferwallInitFail ", 1);
        if (this.mOfferwallListener != null) {
            this.mOfferwallListener.onOfferwallAvailable(false, ErrorBuilder.buildGenericError(description));
        }
    }

    public void setInterstitialListener(InterstitialManagerListener manager) {
        this.mInterstitialManager = manager;
    }

    public void initInterstitial(final Activity activity, final String appKey, final String userId) {
        if (validateConfigBeforeInitAndCallInitFailForInvalid(this.mAdapterConfig, this.mInterstitialManager).isValid()) {
            startISInitTimer(this.mInterstitialManager);
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    SupersonicAdsAdapter.this.mSSAPublisher = SSAFactory.getPublisherInstance(activity);
                    HashMap<String, String> interstitialExtraParams = SupersonicAdsAdapter.this.getInterstitialExtraParams();
                    SupersonicAdsAdapter.this.log(IronSourceTag.ADAPTER_API, SupersonicAdsAdapter.this.getProviderName() + ":initInterstitial(appKey:" + appKey + ", userId:" + userId + ", extraParams:" + interstitialExtraParams + ")", 1);
                    SupersonicAdsAdapter.this.mSSAPublisher.initInterstitial(appKey, userId, interstitialExtraParams, SupersonicAdsAdapter.this);
                }
            });
        }
    }

    public void loadInterstitial() {
        startISLoadTimer(this.mInterstitialManager);
        if (this.mSSAPublisher != null) {
            this.mSSAPublisher.loadInterstitial();
        } else {
            log(IronSourceTag.NATIVE, "Please call initInterstitial before calling loadInterstitial", 2);
        }
    }

    public void showInterstitial() {
    }

    public void showInterstitial(String placementName) {
        if (this.mSSAPublisher != null) {
            this.mSSAPublisher.showInterstitial();
        } else {
            log(IronSourceTag.NATIVE, "Please call loadInterstitial before calling showInterstitial", 2);
        }
    }

    public boolean isInterstitialReady() {
        return this.mSSAPublisher != null && this.mSSAPublisher.isInterstitialAdAvailable();
    }

    public void onInterstitialInitSuccess() {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, getProviderName() + " :onInterstitialInitSuccess ", 1);
        cancelISInitTimer();
        if (this.mInterstitialManager != null) {
            this.mInterstitialManager.onInterstitialInitSuccess(this);
        }
    }

    public void onInterstitialInitFailed(String description) {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, getProviderName() + " :onInterstitialInitFailed ", 1);
        cancelISInitTimer();
        if (this.mInterstitialManager != null) {
            this.mInterstitialManager.onInterstitialInitFailed(ErrorBuilder.buildInitFailedError("Adapter initialization failure - " + getProviderName() + " - " + description, ParametersKeys.INTERSTITIAL), this);
        }
    }

    public void onInterstitialLoadSuccess() {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, getProviderName() + " :onInterstitialLoadSuccess ", 1);
        cancelISLoadTimer();
        if (this.mInterstitialManager != null) {
            this.mInterstitialManager.onInterstitialAdReady(this);
        }
    }

    public void onInterstitialLoadFailed(String description) {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, getProviderName() + " :onInterstitialAdLoadFailed ", 1);
        cancelISLoadTimer();
        if (this.mInterstitialManager != null) {
            this.mInterstitialManager.onInterstitialAdLoadFailed(ErrorBuilder.buildLoadFailedError("Interstitial Load Fail, " + getProviderName() + " - " + description), this);
        }
    }

    public void onInterstitialOpen() {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, getProviderName() + " :onInterstitialAdOpened ", 1);
        if (this.mInterstitialManager != null) {
            this.mInterstitialManager.onInterstitialAdOpened(this);
        }
    }

    public void onInterstitialClose() {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, getProviderName() + " :onInterstitialAdClosed ", 1);
        if (this.mInterstitialManager != null) {
            this.mInterstitialManager.onInterstitialAdClosed(this);
        }
    }

    public void onInterstitialShowSuccess() {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, getProviderName() + " :onInterstitialAdShowSucceeded ", 1);
        if (this.mInterstitialManager != null) {
            this.mInterstitialManager.onInterstitialAdShowSucceeded(this);
        }
    }

    public void onInterstitialShowFailed(String description) {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, getProviderName() + " :onInterstitialAdShowFailed ", 1);
        if (this.mInterstitialManager != null) {
            this.mInterstitialManager.onInterstitialAdShowFailed(ErrorBuilder.buildShowFailedError(ParametersKeys.INTERSTITIAL, description), this);
        }
    }

    public void onInterstitialClick() {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, getProviderName() + " :onInterstitialAdClicked ", 1);
        if (this.mInterstitialManager != null) {
            this.mInterstitialManager.onInterstitialAdClicked(this);
        }
    }
}
