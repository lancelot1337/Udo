package com.ironsource.mediationsdk.model;

import org.json.JSONException;
import org.json.JSONObject;

public class ProviderSettings {
    private JSONObject mBannerSettings;
    private JSONObject mInterstitialSettings;
    private String mProviderName;
    private String mProviderTypeForReflection;
    private JSONObject mRewardedVideoSettings;

    public ProviderSettings(String providerName) {
        this.mProviderName = providerName;
        this.mProviderTypeForReflection = providerName;
        this.mRewardedVideoSettings = new JSONObject();
        this.mInterstitialSettings = new JSONObject();
    }

    public ProviderSettings(String providerName, String providerType, JSONObject rewardedVideoSettings, JSONObject interstitialSettings) {
        this.mProviderName = providerName;
        this.mProviderTypeForReflection = providerType;
        this.mRewardedVideoSettings = rewardedVideoSettings;
        this.mInterstitialSettings = interstitialSettings;
    }

    public String getProviderName() {
        return this.mProviderName;
    }

    public JSONObject getRewardedVideoSettings() {
        return this.mRewardedVideoSettings;
    }

    public String getProviderTypeForReflection() {
        return this.mProviderTypeForReflection;
    }

    public void setRewardedVideoSettings(JSONObject rewardedVideoSettings) {
        this.mRewardedVideoSettings = rewardedVideoSettings;
    }

    public void setRewardedVideoSettings(String key, Object value) {
        try {
            this.mRewardedVideoSettings.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getInterstitialSettings() {
        return this.mInterstitialSettings;
    }

    public void setInterstitialSettings(JSONObject interstitialSettings) {
        this.mInterstitialSettings = interstitialSettings;
    }

    public void setInterstitialSettings(String key, Object value) {
        try {
            this.mInterstitialSettings.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getBannerSettings() {
        return this.mBannerSettings;
    }

    public void setBannerSettings(JSONObject bannerSettings) {
        this.mBannerSettings = bannerSettings;
    }
}
