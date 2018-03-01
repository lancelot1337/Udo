package com.ironsource.sdk.listeners;

import com.ironsource.sdk.data.AdUnitsReady;

public interface OnRewardedVideoListener {
    void onRVAdClicked();

    void onRVAdClosed();

    void onRVAdCredited(int i);

    void onRVAdOpened();

    void onRVInitFail(String str);

    void onRVInitSuccess(AdUnitsReady adUnitsReady);

    void onRVNoMoreOffers();

    void onRVShowFail(String str);
}
