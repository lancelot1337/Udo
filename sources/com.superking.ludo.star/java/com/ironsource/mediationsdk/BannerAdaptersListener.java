package com.ironsource.mediationsdk;

import com.ironsource.mediationsdk.logger.IronSourceError;

public interface BannerAdaptersListener {
    void onBannerAdClicked(AbstractAdapter abstractAdapter);

    void onBannerAdLeftApplication(AbstractAdapter abstractAdapter);

    void onBannerAdLoadFailed(IronSourceError ironSourceError, AbstractAdapter abstractAdapter);

    void onBannerAdLoaded(AbstractAdapter abstractAdapter);

    void onBannerAdScreenDismissed(AbstractAdapter abstractAdapter);

    void onBannerAdScreenPresented(AbstractAdapter abstractAdapter);
}
