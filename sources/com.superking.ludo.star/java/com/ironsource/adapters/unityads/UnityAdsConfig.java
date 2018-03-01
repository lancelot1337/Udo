package com.ironsource.adapters.unityads;

import com.ironsource.mediationsdk.config.AbstractAdapterConfig;
import com.ironsource.mediationsdk.config.ConfigValidationResult;
import com.ironsource.mediationsdk.utils.ErrorBuilder;
import com.ironsource.mediationsdk.utils.IronSourceConstants;
import java.util.ArrayList;
import org.json.JSONObject;

public class UnityAdsConfig extends AbstractAdapterConfig {
    static final String PLACEMENT_ID = "zoneId";
    private static final String PROVIDER_NAME = "UnityAds";
    private final String GAME_ID = "sourceId";

    UnityAdsConfig() {
        super(PROVIDER_NAME);
    }

    public String getRVGameId() {
        return this.mProviderSettings.getRewardedVideoSettings().optString("sourceId");
    }

    public String getISGameId() {
        return this.mProviderSettings.getInterstitialSettings().optString("sourceId");
    }

    public String getRVPlacementId() {
        return this.mProviderSettings.getRewardedVideoSettings().optString(PLACEMENT_ID);
    }

    public String getISPlacementId() {
        return this.mProviderSettings.getInterstitialSettings().optString(PLACEMENT_ID);
    }

    int getMaxVideos() {
        return getMaxVideosToPresent();
    }

    int getMaxRVAdsPerIteration() {
        return getMaxRVAdsPerIterationToPresent();
    }

    int getMaxISAdsPerIteration() {
        return getMaxISAdsPerIterationToPresent();
    }

    protected ArrayList<String> initializeMandatoryFields() {
        ArrayList<String> result = new ArrayList();
        result.add("sourceId");
        result.add(PLACEMENT_ID);
        return result;
    }

    protected ArrayList<String> initializeOptionalFields() {
        ArrayList<String> result = new ArrayList();
        result.add("maxAdsPerSession");
        result.add("maxAdsPerIteration");
        result.add(IronSourceConstants.REQUEST_URL);
        return result;
    }

    protected void validateOptionalField(JSONObject config, String key, ConfigValidationResult result) {
        try {
            if ("maxAdsPerSession".equals(key)) {
                validateMaxVideos(config.optInt(key), result);
            }
        } catch (Throwable th) {
            result.setInvalid(ErrorBuilder.buildInvalidKeyValueError(key, PROVIDER_NAME, null));
        }
    }

    protected void validateMandatoryField(JSONObject config, String key, ConfigValidationResult result) {
        try {
            String value = config.optString(key);
            if ("sourceId".equals(key)) {
                validateGameId(value, result);
            } else if (PLACEMENT_ID.equals(key)) {
                validatPlacementId(value, result);
            }
        } catch (Throwable th) {
            result.setInvalid(ErrorBuilder.buildInvalidKeyValueError(key, PROVIDER_NAME, null));
        }
    }

    protected void adapterPostValidation(JSONObject config, ConfigValidationResult result) {
    }

    private void validatPlacementId(String value, ConfigValidationResult result) {
        validateNonEmptyString(PLACEMENT_ID, value, result);
    }

    private void validateGameId(String value, ConfigValidationResult result) {
        validateNonEmptyString("sourceId", value, result);
    }
}
