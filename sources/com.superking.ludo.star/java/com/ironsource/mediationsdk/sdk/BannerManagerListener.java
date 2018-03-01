package com.ironsource.mediationsdk.sdk;

import com.ironsource.mediationsdk.AbstractAdapter;
import com.ironsource.mediationsdk.BannerAdaptersListener;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.logger.IronSourceError;

public interface BannerManagerListener extends BannerAdaptersListener {
    void onBannerImpression(AbstractAdapter abstractAdapter, IronSourceBannerLayout ironSourceBannerLayout);

    void onBannerInitFailed(IronSourceError ironSourceError, AbstractAdapter abstractAdapter);

    void onBannerInitSuccess(AbstractAdapter abstractAdapter);
}
