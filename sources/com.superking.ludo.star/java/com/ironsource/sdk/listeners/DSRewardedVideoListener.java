package com.ironsource.sdk.listeners;

import com.ironsource.sdk.data.AdUnitsReady;

public interface DSRewardedVideoListener {
    void onRVAdClicked(String str);

    void onRVAdClosed(String str);

    void onRVAdCredited(int i, String str);

    void onRVAdOpened(String str);

    void onRVInitFail(String str, String str2);

    void onRVInitSuccess(AdUnitsReady adUnitsReady, String str);

    void onRVNoMoreOffers(String str);

    void onRVShowFail(String str, String str2);
}
