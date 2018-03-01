package com.ironsource.mediationsdk;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import com.ironsource.mediationsdk.config.AbstractAdapterConfig;
import com.ironsource.mediationsdk.config.ConfigValidationResult;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.logger.LogListener;
import com.ironsource.mediationsdk.logger.LoggingApi;
import com.ironsource.mediationsdk.model.BannerConfigurations;
import com.ironsource.mediationsdk.model.InterstitialConfigurations;
import com.ironsource.mediationsdk.model.RewardedVideoConfigurations;
import com.ironsource.mediationsdk.sdk.BannerManagerListener;
import com.ironsource.mediationsdk.sdk.BaseBannerApi;
import com.ironsource.mediationsdk.sdk.InterstitialAdapterApi;
import com.ironsource.mediationsdk.sdk.InterstitialManagerListener;
import com.ironsource.mediationsdk.sdk.RewardedInterstitialAdapterApi;
import com.ironsource.mediationsdk.sdk.RewardedInterstitialManagerListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoAdapterApi;
import com.ironsource.mediationsdk.sdk.RewardedVideoManagerListener;
import com.ironsource.mediationsdk.utils.ErrorBuilder;
import com.ironsource.mediationsdk.utils.IronSourceConstants;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import cz.msebera.android.httpclient.HttpHeaders;
import java.util.Timer;
import java.util.TimerTask;
import org.cocos2dx.lib.BuildConfig;
import org.cocos2dx.lib.GameControllerDelegate;

public abstract class AbstractAdapter implements LoggingApi, BaseBannerApi, InterstitialAdapterApi, RewardedInterstitialAdapterApi, RewardedVideoAdapterApi {
    private BannerConfigurations mBannerConfig;
    private TimerTask mBannerInitTimerTask;
    private TimerTask mBannerLoadTimerTask;
    protected BannerManagerListener mBannerManager;
    private int mBannerPriority = -1;
    private long mBannerTimeout;
    protected View mCurrentAdNetworkBanner;
    private TimerTask mISInitTimerTask;
    private TimerTask mISLoadTimerTask;
    private InterstitialConfigurations mInterstitialConfig;
    private int mInterstitialPriority = -1;
    private int mInterstitialTimeout;
    protected IronSourceBannerLayout mIronSourceBanner;
    private IronSourceLoggerManager mLoggerManager = IronSourceLoggerManager.getLogger();
    private int mNumberOfAdsPlayed;
    private int mNumberOfBannersShowed;
    private int mNumberOfVideosPlayed;
    private String mPluginFrameworkVersion;
    private String mPluginType;
    private String mProviderName;
    private String mProviderUrl;
    private TimerTask mRVTimerTask;
    protected RewardedInterstitialManagerListener mRewardedInterstitialManager;
    protected RewardedVideoConfigurations mRewardedVideoConfig;
    private int mRewardedVideoPriority = -1;
    private int mRewardedVideoTimeout;

    public abstract String getCoreSDKVersion();

    public abstract int getMaxISAdsPerIteration();

    public abstract int getMaxRVAdsPerIteration();

    public abstract String getVersion();

    public AbstractAdapter(String providerName, String providerUrl) {
        if (providerName == null) {
            providerName = BuildConfig.FLAVOR;
        }
        if (providerUrl == null) {
            providerUrl = BuildConfig.FLAVOR;
        }
        this.mProviderName = providerName;
        this.mProviderUrl = providerUrl;
        this.mNumberOfVideosPlayed = 0;
        this.mNumberOfAdsPlayed = 0;
        this.mNumberOfBannersShowed = 0;
    }

    public int getNumberOfAdsPlayed() {
        return this.mNumberOfAdsPlayed;
    }

    public void increaseNumberOfAdsPlayed() {
        this.mNumberOfAdsPlayed++;
    }

    public void resetNumberOfAdsPlayed() {
        this.mNumberOfAdsPlayed = 0;
    }

    public int getNumberOfVideosPlayed() {
        return this.mNumberOfVideosPlayed;
    }

    public void increaseNumberOfVideosPlayed() {
        this.mNumberOfVideosPlayed++;
    }

    public void resetNumberOfVideosPlayed() {
        this.mNumberOfVideosPlayed = 0;
    }

    public void setInterstitialTimeout(int timeout) {
        this.mInterstitialTimeout = timeout;
    }

    public void setBannerTimeout(long timeout) {
        this.mBannerTimeout = timeout;
    }

    public void setInterstitialPriority(int priority) {
        this.mInterstitialPriority = priority;
    }

    public void setBannerPriority(int priority) {
        this.mBannerPriority = priority;
    }

    public int getInterstitialPriority() {
        return this.mInterstitialPriority;
    }

    public int getBannerPriority() {
        return this.mBannerPriority;
    }

    public void setRewardedVideoTimeout(int timeout) {
        this.mRewardedVideoTimeout = timeout;
    }

    public void setRewardedVideoPriority(int priority) {
        this.mRewardedVideoPriority = priority;
    }

    public int getRewardedVideoPriority() {
        return this.mRewardedVideoPriority;
    }

    public void setInterstitialConfigurations(InterstitialConfigurations interstitialConfigurations) {
        this.mInterstitialConfig = interstitialConfigurations;
    }

    public void setBannerConfigurations(BannerConfigurations bannerConfigurations) {
        this.mBannerConfig = bannerConfigurations;
    }

    public void setRewardedVideoConfigurations(RewardedVideoConfigurations rewardedVideoConfigurations) {
        this.mRewardedVideoConfig = rewardedVideoConfigurations;
    }

    void setPluginData(String pluginType, String pluginFrameworkVersion) {
        this.mPluginType = pluginType;
        this.mPluginFrameworkVersion = pluginFrameworkVersion;
    }

    public String getPluginType() {
        return this.mPluginType;
    }

    public String getPluginFrameworkVersion() {
        return this.mPluginFrameworkVersion;
    }

    public String getProviderName() {
        return this.mProviderName;
    }

    protected void log(IronSourceTag tag, String message, int logLevel) {
        this.mLoggerManager.onLog(tag, message, logLevel);
    }

    String getUrl() {
        return this.mProviderUrl;
    }

    protected ConfigValidationResult validateConfigBeforeInitAndCallInitFailForInvalid(AbstractAdapterConfig config, InterstitialManagerListener manager) {
        ConfigValidationResult validationResult = config.isISConfigValid();
        if (!validationResult.isValid()) {
            IronSourceError sse = validationResult.getIronSourceError();
            log(IronSourceTag.ADAPTER_API, getProviderName() + sse.getErrorMessage(), 2);
            if (manager != null) {
                manager.onInterstitialInitFailed(sse, this);
            }
        }
        return validationResult;
    }

    protected ConfigValidationResult validateConfigBeforeInitAndCallAvailabilityChangedForInvalid(AbstractAdapterConfig config, RewardedVideoManagerListener manager) {
        ConfigValidationResult validationResult = config.isRVConfigValid();
        if (!validationResult.isValid()) {
            log(IronSourceTag.ADAPTER_API, getProviderName() + validationResult.getIronSourceError().getErrorMessage(), 2);
            if (manager != null) {
                manager.onRewardedVideoAvailabilityChanged(false, this);
            }
        }
        return validationResult;
    }

    protected ConfigValidationResult validateBannerConfigBeforeInit(AbstractAdapterConfig config, BannerManagerListener manager) {
        ConfigValidationResult validationResult = config.isBannerConfigValid();
        if (!validationResult.isValid()) {
            IronSourceError sse = validationResult.getIronSourceError();
            log(IronSourceTag.ADAPTER_API, getProviderName() + sse.getErrorMessage(), 2);
            if (manager != null) {
                manager.onBannerInitFailed(sse, this);
            }
        }
        return validationResult;
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof AbstractAdapter)) {
            return false;
        }
        return getProviderName().equals(((AbstractAdapter) other).getProviderName());
    }

    protected void startISInitTimer(final InterstitialManagerListener listener) {
        try {
            this.mISInitTimerTask = new TimerTask() {
                public void run() {
                    listener.onInterstitialInitFailed(ErrorBuilder.buildInitFailedError(HttpHeaders.TIMEOUT, ParametersKeys.INTERSTITIAL), AbstractAdapter.this);
                }
            };
            Timer timer = new Timer();
            if (this.mISInitTimerTask != null) {
                timer.schedule(this.mISInitTimerTask, (long) (this.mInterstitialTimeout * GameControllerDelegate.THUMBSTICK_LEFT_X));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void cancelISInitTimer() {
        try {
            if (this.mISInitTimerTask != null) {
                this.mISInitTimerTask.cancel();
                this.mISInitTimerTask = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void startISLoadTimer(final InterstitialManagerListener listener) {
        try {
            this.mISLoadTimerTask = new TimerTask() {
                public void run() {
                    listener.onInterstitialAdLoadFailed(ErrorBuilder.buildLoadFailedError("Interstitial Load Fail, " + AbstractAdapter.this.getProviderName() + " - " + HttpHeaders.TIMEOUT), AbstractAdapter.this);
                }
            };
            Timer timer = new Timer();
            if (this.mISLoadTimerTask != null) {
                timer.schedule(this.mISLoadTimerTask, (long) (this.mInterstitialTimeout * GameControllerDelegate.THUMBSTICK_LEFT_X));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void cancelISLoadTimer() {
        try {
            if (this.mISLoadTimerTask != null) {
                this.mISLoadTimerTask.cancel();
                this.mISLoadTimerTask = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void startBannerLoadTimer(final BannerAdaptersListener listener) {
        try {
            this.mBannerLoadTimerTask = new TimerTask() {
                public void run() {
                    IronSourceError error = ErrorBuilder.buildLoadFailedError("Banner Load Fail, " + AbstractAdapter.this.getProviderName() + " - " + HttpHeaders.TIMEOUT);
                    AbstractAdapter.this.removeBannerViews();
                    listener.onBannerAdLoadFailed(error, AbstractAdapter.this);
                }
            };
            Timer timer = new Timer();
            if (this.mBannerLoadTimerTask != null) {
                timer.schedule(this.mBannerLoadTimerTask, this.mBannerTimeout);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void cancelBannerLoadTimer() {
        try {
            if (this.mBannerLoadTimerTask != null) {
                this.mBannerLoadTimerTask.cancel();
                this.mBannerLoadTimerTask = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void startRVTimer(final RewardedVideoManagerListener listener) {
        try {
            this.mRVTimerTask = new TimerTask() {
                public void run() {
                    listener.onRewardedVideoAvailabilityChanged(false, AbstractAdapter.this);
                }
            };
            Timer rvtimer = new Timer();
            if (this.mRVTimerTask != null) {
                rvtimer.schedule(this.mRVTimerTask, (long) (this.mRewardedVideoTimeout * GameControllerDelegate.THUMBSTICK_LEFT_X));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void cancelRVTimer() {
        try {
            if (this.mRVTimerTask != null) {
                this.mRVTimerTask.cancel();
                this.mRVTimerTask = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void startBannerInitTimer(final BannerManagerListener listener) {
        try {
            this.mBannerInitTimerTask = new TimerTask() {
                public void run() {
                    if (listener != null) {
                        listener.onBannerInitFailed(ErrorBuilder.buildInitFailedError(HttpHeaders.TIMEOUT, IronSourceConstants.BANNER_AD_UNIT), AbstractAdapter.this);
                    }
                }
            };
            Timer timer = new Timer();
            if (this.mBannerInitTimerTask != null) {
                timer.schedule(this.mBannerInitTimerTask, this.mBannerTimeout);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void cancelBannerInitTimer() {
        try {
            if (this.mBannerInitTimerTask != null) {
                this.mBannerInitTimerTask.cancel();
                this.mBannerInitTimerTask = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void removeBannerViews() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                if (AbstractAdapter.this.mIronSourceBanner != null && AbstractAdapter.this.mCurrentAdNetworkBanner != null) {
                    AbstractAdapter.this.mIronSourceBanner.removeView(AbstractAdapter.this.mCurrentAdNetworkBanner);
                }
            }
        });
    }

    public void setLogListener(LogListener logListener) {
    }

    public void setRewardedInterstitialListener(RewardedInterstitialManagerListener listener) {
        this.mRewardedInterstitialManager = listener;
    }

    protected boolean isAdaptersDebugEnabled() {
        return this.mLoggerManager.isDebugEnabled();
    }

    public void initBanners(Activity activity, String appKey, String userId) {
    }

    public IronSourceBannerLayout createBanner(Activity activity, EBannerSize size) {
        return null;
    }

    public void loadBanner(IronSourceBannerLayout banner) {
    }

    public void loadBanner(IronSourceBannerLayout banner, String placementName) {
    }

    public void destroyBanner(IronSourceBannerLayout banner) {
    }

    public void setBannerListener(BannerManagerListener manager) {
    }
}
