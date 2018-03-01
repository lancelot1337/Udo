package com.ironsource.mediationsdk.sdk;

public interface InterstitialApi extends BaseInterstitialApi {
    boolean isInterstitialPlacementCapped(String str);

    void setInterstitialListener(InterstitialListener interstitialListener);
}
