package com.ironsource.adapters.admob;

import com.ironsource.mediationsdk.config.AbstractAdapterConfig;
import com.ironsource.mediationsdk.config.ConfigValidationResult;
import com.ironsource.mediationsdk.model.ProviderSettings;
import com.ironsource.mediationsdk.utils.ErrorBuilder;
import java.util.ArrayList;
import org.json.JSONObject;

public class AdMobConfig extends AbstractAdapterConfig {
    private static final String AD_UNIT_ID = "adUnitId";
    private static final String APP_ID = "appId";
    private static final String COPPA = "coppa";
    private static final String PROVIDER_NAME = "AdMob";

    AdMobConfig() {
        super(PROVIDER_NAME);
    }

    public ProviderSettings getProviderSettings() {
        return this.mProviderSettings;
    }

    int getMaxISAdsPerIteration() {
        return getMaxISAdsPerIterationToPresent();
    }

    public String getISAdUnitId() {
        return this.mProviderSettings.getInterstitialSettings().optString(AD_UNIT_ID);
    }

    public String getRVAdUnitId() {
        return this.mProviderSettings.getRewardedVideoSettings().optString(AD_UNIT_ID);
    }

    int getMaxRVAdsPerIteration() {
        return getMaxVideosToPresent();
    }

    public boolean hasCoppaKey() {
        return this.mProviderSettings.getRewardedVideoSettings().has(COPPA);
    }

    public boolean isCoppa() {
        return this.mProviderSettings.getRewardedVideoSettings().optBoolean(COPPA, false);
    }

    public String getRVAppKey() {
        return this.mProviderSettings.getRewardedVideoSettings().optString(APP_ID);
    }

    public String getISAppKey() {
        return this.mProviderSettings.getInterstitialSettings().optString(APP_ID);
    }

    protected ArrayList<String> initializeMandatoryFields() {
        ArrayList<String> result = new ArrayList();
        result.add(AD_UNIT_ID);
        return result;
    }

    protected ArrayList<String> initializeOptionalFields() {
        ArrayList<String> result = new ArrayList();
        result.add("maxAdsPerIteration");
        result.add(APP_ID);
        result.add(COPPA);
        return result;
    }

    protected void validateOptionalField(JSONObject config, String key, ConfigValidationResult result) {
    }

    protected void validateMandatoryField(JSONObject config, String key, ConfigValidationResult result) {
        try {
            String value = config.optString(key);
            if (AD_UNIT_ID.equals(key)) {
                validateAdUnitId(value, result);
            }
        } catch (Throwable th) {
            result.setInvalid(ErrorBuilder.buildInvalidKeyValueError(key, PROVIDER_NAME, null));
        }
    }

    protected void adapterPostValidation(JSONObject config, ConfigValidationResult result) {
    }

    private void validateAdUnitId(String appIdValue, ConfigValidationResult result) {
        validateNonEmptyString(AD_UNIT_ID, appIdValue, result);
    }
}
