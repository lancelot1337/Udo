package com.ironsource.mediationsdk.sdk;

import android.app.Activity;

public interface BaseInterstitialApi extends BaseApi {
    void initInterstitial(Activity activity, String str, String str2);

    boolean isInterstitialReady();

    void loadInterstitial();

    void showInterstitial();

    void showInterstitial(String str);
}
