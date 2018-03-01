package com.ironsource.mediationsdk.utils;

public class SessionDepthManager {
    public static final int BANNER = 3;
    public static final int INTERSTITIAL = 2;
    public static final int NONE = -1;
    public static final int OFFERWALL = 0;
    public static final int REWARDEDVIDEO = 1;
    private static SessionDepthManager mInstance;
    private int mBannerDepth = OFFERWALL;
    private int mInterstitialDepth = REWARDEDVIDEO;
    private int mOfferwallDepth = REWARDEDVIDEO;
    private int mRewardedVideoDepth = REWARDEDVIDEO;

    public static synchronized SessionDepthManager getInstance() {
        SessionDepthManager sessionDepthManager;
        synchronized (SessionDepthManager.class) {
            if (mInstance == null) {
                mInstance = new SessionDepthManager();
            }
            sessionDepthManager = mInstance;
        }
        return sessionDepthManager;
    }

    public synchronized void increaseSessionDepth(int adUnit) {
        switch (adUnit) {
            case OFFERWALL /*0*/:
                this.mOfferwallDepth += REWARDEDVIDEO;
                break;
            case REWARDEDVIDEO /*1*/:
                this.mRewardedVideoDepth += REWARDEDVIDEO;
                break;
            case INTERSTITIAL /*2*/:
                this.mInterstitialDepth += REWARDEDVIDEO;
                break;
            case BANNER /*3*/:
                this.mBannerDepth += REWARDEDVIDEO;
                break;
        }
    }

    public synchronized int getSessionDepth(int adUnit) {
        int i;
        switch (adUnit) {
            case OFFERWALL /*0*/:
                i = this.mOfferwallDepth;
                break;
            case REWARDEDVIDEO /*1*/:
                i = this.mRewardedVideoDepth;
                break;
            case INTERSTITIAL /*2*/:
                i = this.mInterstitialDepth;
                break;
            case BANNER /*3*/:
                i = this.mBannerDepth;
                break;
            default:
                i = NONE;
                break;
        }
        return i;
    }
}
