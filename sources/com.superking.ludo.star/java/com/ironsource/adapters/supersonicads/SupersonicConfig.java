package com.ironsource.adapters.supersonicads;

import android.text.TextUtils;
import com.ironsource.mediationsdk.config.AbstractAdapterConfig;
import com.ironsource.mediationsdk.config.ConfigValidationResult;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.utils.IronSourceConstants;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;

public class SupersonicConfig extends AbstractAdapterConfig {
    private static SupersonicConfig mInstance;
    private final String APPLICATION_PRIVATE_KEY = "privateKey";
    private final String CAMPAIGN_ID = "campaignId";
    private final String CLIENT_SIDE_CALLBACKS = ParametersKeys.USE_CLIENT_SIDE_CALLBACKS;
    private final String CUSTOM_PARAM_PREFIX = "custom_";
    private final String DYNAMIC_CONTROLLER_DEBUG_MODE = "debugMode";
    private final String DYNAMIC_CONTROLLER_URL = "controllerUrl";
    private final String ITEM_COUNT = "itemCount";
    private final String ITEM_NAME = "itemName";
    private final String LANGUAGE = "language";
    private final String MAX_VIDEO_LENGTH = "maxVideoLength";
    private Map<String, String> mOfferwallCustomParams;
    private Map<String, String> mRewardedVideoCustomParams;

    public static SupersonicConfig getConfigObj() {
        if (mInstance == null) {
            mInstance = new SupersonicConfig();
        }
        return mInstance;
    }

    private SupersonicConfig() {
        super(IronSourceConstants.MEDIATION_PROVIDER_NAME);
    }

    public void setClientSideCallbacks(boolean status) {
        this.mProviderSettings.setRewardedVideoSettings(ParametersKeys.USE_CLIENT_SIDE_CALLBACKS, String.valueOf(status));
    }

    public void setCustomControllerUrl(String url) {
        this.mProviderSettings.setRewardedVideoSettings("controllerUrl", url);
        this.mProviderSettings.setInterstitialSettings("controllerUrl", url);
    }

    public void setDebugMode(int debugMode) {
        this.mProviderSettings.setRewardedVideoSettings("debugMode", Integer.valueOf(debugMode));
        this.mProviderSettings.setInterstitialSettings("debugMode", Integer.valueOf(debugMode));
    }

    public void setCampaignId(String id) {
        this.mProviderSettings.setRewardedVideoSettings("campaignId", id);
    }

    public void setLanguage(String language) {
        this.mProviderSettings.setRewardedVideoSettings("language", language);
        this.mProviderSettings.setInterstitialSettings("language", language);
    }

    public void setRewardedVideoCustomParams(Map<String, String> rvCustomParams) {
        this.mRewardedVideoCustomParams = convertCustomParams(rvCustomParams);
    }

    public void setOfferwallCustomParams(Map<String, String> owCustomParams) {
        this.mOfferwallCustomParams = convertCustomParams(owCustomParams);
    }

    private Map<String, String> convertCustomParams(Map<String, String> customParams) {
        Map<String, String> result = new HashMap();
        if (customParams != null) {
            try {
                Set<String> keys = customParams.keySet();
                if (keys != null) {
                    for (String k : keys) {
                        if (!TextUtils.isEmpty(k)) {
                            String value = (String) customParams.get(k);
                            if (!TextUtils.isEmpty(value)) {
                                result.put("custom_" + k, value);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                IronSourceLoggerManager.getLogger().logException(IronSourceTag.NATIVE, ":convertCustomParams()", e);
            }
        }
        return result;
    }

    public boolean getClientSideCallbacks() {
        if (this.mProviderSettings == null || this.mProviderSettings.getRewardedVideoSettings() == null || !this.mProviderSettings.getRewardedVideoSettings().has(ParametersKeys.USE_CLIENT_SIDE_CALLBACKS)) {
            return false;
        }
        return this.mProviderSettings.getRewardedVideoSettings().optBoolean(ParametersKeys.USE_CLIENT_SIDE_CALLBACKS, false);
    }

    public Map<String, String> getOfferwallCustomParams() {
        return this.mOfferwallCustomParams;
    }

    public Map<String, String> getRewardedVideoCustomParams() {
        return this.mRewardedVideoCustomParams;
    }

    protected ArrayList<String> initializeMandatoryFields() {
        return null;
    }

    protected ArrayList<String> initializeOptionalFields() {
        return null;
    }

    protected void validateOptionalField(JSONObject config, String key, ConfigValidationResult result) {
    }

    protected void validateMandatoryField(JSONObject config, String key, ConfigValidationResult result) {
    }

    protected void adapterPostValidation(JSONObject config, ConfigValidationResult result) {
    }
}
