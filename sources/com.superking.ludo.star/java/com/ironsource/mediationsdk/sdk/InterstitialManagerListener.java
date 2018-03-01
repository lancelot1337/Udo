package com.ironsource.mediationsdk.sdk;

import com.ironsource.mediationsdk.AbstractAdapter;
import com.ironsource.mediationsdk.logger.IronSourceError;

public interface InterstitialManagerListener {
    void onInterstitialAdClicked(AbstractAdapter abstractAdapter);

    void onInterstitialAdClosed(AbstractAdapter abstractAdapter);

    void onInterstitialAdLoadFailed(IronSourceError ironSourceError, AbstractAdapter abstractAdapter);

    void onInterstitialAdOpened(AbstractAdapter abstractAdapter);

    void onInterstitialAdReady(AbstractAdapter abstractAdapter);

    void onInterstitialAdShowFailed(IronSourceError ironSourceError, AbstractAdapter abstractAdapter);

    void onInterstitialAdShowSucceeded(AbstractAdapter abstractAdapter);

    void onInterstitialInitFailed(IronSourceError ironSourceError, AbstractAdapter abstractAdapter);

    void onInterstitialInitSuccess(AbstractAdapter abstractAdapter);
}
