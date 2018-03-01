package com.ironsource.adapters.admob;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdRequest.Builder;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.ironsource.mediationsdk.AbstractAdapter;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.sdk.InterstitialManagerListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoManagerListener;
import com.ironsource.mediationsdk.utils.ErrorBuilder;
import com.ironsource.mediationsdk.utils.IronSourceConstants;
import com.ironsource.mediationsdk.utils.IronSourceConstants.Gender;
import com.ironsource.mediationsdk.utils.RewardedVideoHelper;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import io.branch.referral.R;
import org.cocos2dx.lib.Cocos2dxEditBox;
import org.cocos2dx.lib.Cocos2dxHandler;

public class AdMobAdapter extends AbstractAdapter {
    private static final String CORE_SDK_VERSION = "9.0.2";
    private static final String VERSION = "3.0.2";
    private final String IRONSOURCE_REQUEST_AGENT = "ironSource";
    private AdMobConfig mAdapterConfig = new AdMobConfig();
    private int mAge = -1;
    private boolean mDidInitSdk = false;
    private int mGender;
    private InterstitialAd mInterstitialAd;
    private AdListener mInterstitialAdListener = new AdListener() {
        public void onAdClosed() {
            IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, "onAdClosed", 1);
            if (AdMobAdapter.this.mInterstitialManager != null) {
                AdMobAdapter.this.mInterstitialManager.onInterstitialAdClosed(AdMobAdapter.this);
            }
        }

        public void onAdFailedToLoad(int errorCode) {
            IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, "onAdFailedToLoad", 1);
            AdMobAdapter.this.cancelISLoadTimer();
            if (AdMobAdapter.this.mInterstitialManager != null) {
                AdMobAdapter.this.mInterstitialManager.onInterstitialAdLoadFailed(ErrorBuilder.buildLoadFailedError("Interstitial Load Fail, " + AdMobAdapter.this.getProviderName() + " - " + (AdMobAdapter.this.getErrorString(errorCode) + "( " + errorCode + " )")), AdMobAdapter.this);
            }
        }

        public void onAdLeftApplication() {
            IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, "onAdLeftApplication", 1);
            if (AdMobAdapter.this.mInterstitialManager != null) {
                AdMobAdapter.this.mInterstitialManager.onInterstitialAdClicked(AdMobAdapter.this);
            }
        }

        public void onAdOpened() {
            IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, "onAdOpened", 1);
            if (AdMobAdapter.this.mInterstitialManager != null) {
                AdMobAdapter.this.mInterstitialManager.onInterstitialAdOpened(AdMobAdapter.this);
                AdMobAdapter.this.mInterstitialManager.onInterstitialAdShowSucceeded(AdMobAdapter.this);
            }
        }

        public void onAdLoaded() {
            IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, "onAdLoaded", 1);
            AdMobAdapter.this.cancelISLoadTimer();
            AdMobAdapter.this.mIsInterstitialReady = true;
            if (AdMobAdapter.this.mInterstitialManager != null) {
                AdMobAdapter.this.mInterstitialManager.onInterstitialAdReady(AdMobAdapter.this);
            }
        }
    };
    private InterstitialManagerListener mInterstitialManager;
    private boolean mIsInterstitialReady = false;
    private boolean mIsRewardedVideoReady = false;
    private RewardedVideoAd mRewardedVideoAd;
    private RewardedVideoHelper mRewardedVideoHelper = new RewardedVideoHelper();
    private RewardedVideoManagerListener mRewardedVideoManager;
    private RewardedVideoAdListener rewardedVideoAdListener = new RewardedVideoAdListener() {
        public void onRewardedVideoAdLoaded() {
            IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, "onRewardedVideoAdLoaded", 1);
            AdMobAdapter.this.cancelRVTimer();
            AdMobAdapter.this.mIsRewardedVideoReady = true;
            if (AdMobAdapter.this.mRewardedVideoManager != null) {
                AdMobAdapter.this.mRewardedVideoManager.onRewardedVideoAvailabilityChanged(true, AdMobAdapter.this);
            }
        }

        public void onRewardedVideoAdOpened() {
            IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, "onRewardedVideoAdOpened", 1);
            AdMobAdapter.this.mIsRewardedVideoReady = false;
            if (AdMobAdapter.this.mRewardedVideoManager != null) {
                AdMobAdapter.this.mRewardedVideoManager.onRewardedVideoAdOpened(AdMobAdapter.this);
                AdMobAdapter.this.mRewardedVideoManager.onRewardedVideoAvailabilityChanged(false, AdMobAdapter.this);
            }
        }

        public void onRewardedVideoStarted() {
            IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, "onRewardedVideoStarted", 1);
            if (AdMobAdapter.this.mRewardedVideoManager != null) {
                AdMobAdapter.this.mRewardedVideoManager.onRewardedVideoAdStarted(AdMobAdapter.this);
            }
        }

        public void onRewardedVideoAdClosed() {
            IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, "onRewardedVideoAdClosed", 1);
            if (AdMobAdapter.this.mRewardedVideoManager != null) {
                AdMobAdapter.this.mRewardedVideoManager.onRewardedVideoAdClosed(AdMobAdapter.this);
            }
            AdMobAdapter.this.loadRewardedVideoAd();
        }

        public void onRewarded(RewardItem rewardItem) {
            IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, "onRewarded", 1);
            if (AdMobAdapter.this.mRewardedVideoManager != null) {
                AdMobAdapter.this.mRewardedVideoManager.onRewardedVideoAdRewarded(AdMobAdapter.this.mRewardedVideoConfig.getRewardedVideoPlacement(AdMobAdapter.this.mRewardedVideoHelper.getPlacementName()), AdMobAdapter.this);
            }
        }

        public void onRewardedVideoAdLeftApplication() {
            IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, "onRewardedVideoAdLeftApplication", 1);
        }

        public void onRewardedVideoAdFailedToLoad(int errorCode) {
            IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, "onRewardedVideoAdFailedToLoad", 1);
            AdMobAdapter.this.cancelRVTimer();
            AdMobAdapter.this.mIsRewardedVideoReady = false;
            if (AdMobAdapter.this.mRewardedVideoManager != null) {
                AdMobAdapter.this.mRewardedVideoManager.onRewardedVideoAvailabilityChanged(false, AdMobAdapter.this);
            }
        }
    };

    public static AdMobAdapter startAdapter(String providerName, String providerUrl) {
        return new AdMobAdapter(providerName, providerUrl);
    }

    private AdMobAdapter(String providerName, String providerUrl) {
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
        return CORE_SDK_VERSION;
    }

    public void setRewardedVideoListener(RewardedVideoManagerListener manager) {
        this.mRewardedVideoManager = manager;
    }

    private synchronized void initSDK(Activity activity, String appKey) {
        if (!this.mDidInitSdk) {
            if (TextUtils.isEmpty(appKey)) {
                MobileAds.initialize(activity.getApplicationContext());
            } else {
                MobileAds.initialize(activity.getApplicationContext(), appKey);
            }
            this.mDidInitSdk = true;
        }
    }

    public void initRewardedVideo(final Activity activity, String appKey, String userId) {
        this.mRewardedVideoHelper.reset();
        this.mRewardedVideoHelper.setMaxVideo(this.mAdapterConfig.getMaxRVAdsPerIteration());
        startRVTimer(this.mRewardedVideoManager);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                AdMobAdapter.this.initSDK(activity, AdMobAdapter.this.mAdapterConfig.getRVAppKey());
                AdMobAdapter.this.mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(activity);
                AdMobAdapter.this.mRewardedVideoAd.setRewardedVideoAdListener(AdMobAdapter.this.rewardedVideoAdListener);
                AdMobAdapter.this.loadRewardedVideoAd();
            }
        });
    }

    private void loadRewardedVideoAd() {
        this.mRewardedVideoAd.loadAd(this.mAdapterConfig.getRVAdUnitId(), createAdRequest());
    }

    private AdRequest createAdRequest() {
        Builder builder = new Builder();
        builder.setGender(this.mGender);
        builder.setRequestAgent("ironSource");
        if (this.mAge > -1) {
            builder.tagForChildDirectedTreatment(this.mAge < 13);
        }
        return builder.build();
    }

    public void showRewardedVideo() {
    }

    public boolean isRewardedVideoAvailable() {
        boolean adMobVideoStatus = this.mRewardedVideoAd != null && this.mIsRewardedVideoReady;
        this.mRewardedVideoHelper.setVideoAvailability(adMobVideoStatus);
        boolean result = this.mRewardedVideoHelper.isVideoAvailable();
        log(IronSourceTag.ADAPTER_API, getProviderName() + ":isRewardedVideoAvailable(): " + result, 1);
        return result;
    }

    public void showRewardedVideo(final String placementName) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                boolean shouldNotify;
                AdMobAdapter.this.log(IronSourceTag.ADAPTER_API, AdMobAdapter.this.getProviderName() + ":showRewardedVideo(placement:" + placementName + ")", 1);
                if (AdMobAdapter.this.mRewardedVideoAd.isLoaded()) {
                    AdMobAdapter.this.mRewardedVideoHelper.setPlacementName(placementName);
                    AdMobAdapter.this.mRewardedVideoAd.show();
                    shouldNotify = AdMobAdapter.this.mRewardedVideoHelper.increaseCurrentVideo();
                } else {
                    shouldNotify = AdMobAdapter.this.mRewardedVideoHelper.setVideoAvailability(false);
                    if (AdMobAdapter.this.mRewardedVideoManager != null) {
                        AdMobAdapter.this.mRewardedVideoManager.onRewardedVideoAdShowFailed(ErrorBuilder.buildNoAdsToShowError(IronSourceConstants.REWARDED_VIDEO_AD_UNIT), AdMobAdapter.this);
                    }
                }
                if (shouldNotify && AdMobAdapter.this.mRewardedVideoManager != null) {
                    AdMobAdapter.this.mRewardedVideoManager.onRewardedVideoAvailabilityChanged(AdMobAdapter.this.mRewardedVideoHelper.isVideoAvailable(), AdMobAdapter.this);
                }
            }
        });
    }

    public void setInterstitialListener(InterstitialManagerListener manager) {
        this.mInterstitialManager = manager;
    }

    public void initInterstitial(Activity activity, String appKey, String userId) {
        if (validateConfigBeforeInitAndCallInitFailForInvalid(this.mAdapterConfig, this.mInterstitialManager).isValid()) {
            initSDK(activity, this.mAdapterConfig.getISAppKey());
            startISInitTimer(this.mInterstitialManager);
            String ISadUnitId = this.mAdapterConfig.getISAdUnitId();
            log(IronSourceTag.ADAPTER_API, getProviderName() + ":init(adUnitId:" + ISadUnitId + ")", 1);
            this.mInterstitialAd = new InterstitialAd(activity);
            this.mInterstitialAd.setAdUnitId(ISadUnitId);
            this.mInterstitialAd.setAdListener(this.mInterstitialAdListener);
            if (!(this.mInterstitialManager == null || this.mInterstitialAd == null)) {
                this.mInterstitialManager.onInterstitialInitSuccess(this);
            }
            cancelISInitTimer();
        }
    }

    public void loadInterstitial() {
        startISLoadTimer(this.mInterstitialManager);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                if (AdMobAdapter.this.mInterstitialAd.isLoaded()) {
                    AdMobAdapter.this.mIsInterstitialReady = true;
                    AdMobAdapter.this.mInterstitialManager.onInterstitialAdReady(AdMobAdapter.this);
                } else if (!AdMobAdapter.this.mInterstitialAd.isLoading() && !AdMobAdapter.this.mInterstitialAd.isLoaded()) {
                    AdMobAdapter.this.mInterstitialAd.loadAd(AdMobAdapter.this.createAdRequest());
                }
            }
        });
    }

    public void showInterstitial() {
    }

    public void showInterstitial(final String placementName) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                if (AdMobAdapter.this.mInterstitialAd == null || !AdMobAdapter.this.mInterstitialAd.isLoaded()) {
                    AdMobAdapter.this.log(IronSourceTag.ADAPTER_API, AdMobAdapter.this.getProviderName() + ":showInterstitial(placement:" + placementName + ") : failed", 0);
                    AdMobAdapter.this.mInterstitialManager.onInterstitialAdShowFailed(ErrorBuilder.buildNoAdsToShowError(ParametersKeys.INTERSTITIAL), AdMobAdapter.this);
                    return;
                }
                AdMobAdapter.this.mInterstitialAd.show();
                AdMobAdapter.this.mIsInterstitialReady = false;
                AdMobAdapter.this.log(IronSourceTag.ADAPTER_API, AdMobAdapter.this.getProviderName() + ":showInterstitial(placement:" + placementName + ")", 1);
            }
        });
    }

    public boolean isInterstitialReady() {
        return this.mIsInterstitialReady;
    }

    public void onResume(final Activity activity) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                if (AdMobAdapter.this.mRewardedVideoAd != null) {
                    AdMobAdapter.this.mRewardedVideoAd.resume(activity);
                }
            }
        });
    }

    public void onPause(final Activity activity) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                if (AdMobAdapter.this.mRewardedVideoAd != null) {
                    AdMobAdapter.this.mRewardedVideoAd.pause(activity);
                }
            }
        });
    }

    public void setAge(int age) {
        this.mAge = age;
    }

    public void setGender(String gender) {
        int i = -1;
        switch (gender.hashCode()) {
            case -1278174388:
                if (gender.equals(Gender.FEMALE)) {
                    i = 0;
                    break;
                }
                break;
            case 3343885:
                if (gender.equals(Gender.MALE)) {
                    i = 1;
                    break;
                }
                break;
        }
        switch (i) {
            case Cocos2dxEditBox.kEndActionUnknown /*0*/:
                this.mGender = 2;
                return;
            case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                this.mGender = 1;
                return;
            default:
                this.mGender = 0;
                return;
        }
    }

    public void setMediationSegment(String segment) {
    }

    private String getErrorString(int errorCode) {
        switch (errorCode) {
            case Cocos2dxEditBox.kEndActionUnknown /*0*/:
                return "Internal error";
            case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                return "The ad request was invalid";
            case R.styleable.View_paddingStart /*2*/:
                return "The ad request was unsuccessful due to network connectivity";
            case Cocos2dxEditBox.kEndActionReturn /*3*/:
                return "The ad request was successful, but no ad was returned due to lack of ad inventory";
            default:
                return "Unknown error";
        }
    }
}
