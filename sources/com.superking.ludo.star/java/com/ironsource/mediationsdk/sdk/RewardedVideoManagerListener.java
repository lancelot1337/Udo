package com.ironsource.mediationsdk.sdk;

import com.ironsource.mediationsdk.AbstractAdapter;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;

public interface RewardedVideoManagerListener {
    void onRewardedVideoAdClosed(AbstractAdapter abstractAdapter);

    void onRewardedVideoAdEnded(AbstractAdapter abstractAdapter);

    void onRewardedVideoAdOpened(AbstractAdapter abstractAdapter);

    void onRewardedVideoAdRewarded(Placement placement, AbstractAdapter abstractAdapter);

    void onRewardedVideoAdShowFailed(IronSourceError ironSourceError, AbstractAdapter abstractAdapter);

    void onRewardedVideoAdStarted(AbstractAdapter abstractAdapter);

    void onRewardedVideoAvailabilityChanged(boolean z, AbstractAdapter abstractAdapter);
}
