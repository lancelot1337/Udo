package com.ironsource.mediationsdk.sdk;

public interface RewardedVideoApi extends BaseRewardedVideoApi {
    boolean isRewardedVideoPlacementCapped(String str);

    void setRewardedVideoListener(RewardedVideoListener rewardedVideoListener);
}
