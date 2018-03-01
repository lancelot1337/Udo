package com.ironsource.sdk.listeners;

public interface OnInterstitialListener {
    void onInterstitialClick();

    void onInterstitialClose();

    void onInterstitialInitFailed(String str);

    void onInterstitialInitSuccess();

    void onInterstitialLoadFailed(String str);

    void onInterstitialLoadSuccess();

    void onInterstitialOpen();

    void onInterstitialShowFailed(String str);

    void onInterstitialShowSuccess();
}
