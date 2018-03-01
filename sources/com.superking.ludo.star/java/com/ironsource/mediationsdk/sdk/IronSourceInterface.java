package com.ironsource.mediationsdk.sdk;

import android.content.Context;
import com.ironsource.mediationsdk.logger.LoggingApi;
import com.ironsource.mediationsdk.model.InterstitialPlacement;
import com.ironsource.mediationsdk.model.Placement;

public interface IronSourceInterface extends LoggingApi, BannerApi, InterstitialApi, OfferwallApi, RewardedInterstitialApi, RewardedVideoApi {
    String getAdvertiserId(Context context);

    InterstitialPlacement getInterstitialPlacementInfo(String str);

    Placement getRewardedVideoPlacementInfo(String str);

    void removeInterstitialListener();

    void removeOfferwallListener();

    void removeRewardedVideoListener();

    void setAdaptersDebug(boolean z);

    boolean setDynamicUserId(String str);

    void setMediationType(String str);

    void shouldTrackNetworkState(Context context, boolean z);
}
