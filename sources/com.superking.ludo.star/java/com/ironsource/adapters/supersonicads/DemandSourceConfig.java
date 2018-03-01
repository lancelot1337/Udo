package com.ironsource.adapters.supersonicads;

import android.text.TextUtils;
import com.facebook.appevents.AppEventsConstants;
import com.ironsource.mediationsdk.config.AbstractAdapterConfig;
import com.ironsource.mediationsdk.config.ConfigValidationResult;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.utils.ErrorBuilder;
import com.ironsource.mediationsdk.utils.IronSourceConstants;
import com.ironsource.mediationsdk.utils.IronSourceConstants.Gender;
import com.ironsource.mediationsdk.utils.ServerResponseWrapper;
import com.ironsource.sdk.utils.Constants.ErrorCodes;
import com.ironsource.sdk.utils.Constants.RequestParameters;
import java.util.ArrayList;
import org.cocos2dx.lib.BuildConfig;
import org.cocos2dx.lib.GameControllerDelegate;
import org.json.JSONObject;

public class DemandSourceConfig extends AbstractAdapterConfig {
    static final String APPLICATION_USER_AGE_GROUP = "applicationUserAgeGroup";
    static final String APPLICATION_USER_GENDER = "applicationUserGender";
    static final String CAMPAIGN_ID = "campaignId";
    static final String CLIENT_SIDE_CALLBACKS = "useClientSideCallbacks";
    private static final String CUSTOM_PARAM_PREFIX = "custom_";
    static final String CUSTOM_SEGMENT = "custom_Segment";
    static final String ITEM_COUNT = "itemCount";
    static final String ITEM_NAME = "itemName";
    static final String LANGUAGE = "language";
    static final String MAX_VIDEO_LENGTH = "maxVideoLength";
    private final String AGE = "age";
    private final String APPLICATION_KEY = RequestParameters.APPLICATION_KEY;
    private final String APPLICATION_PRIVATE_KEY = "privateKey";
    private final String DYNAMIC_CONTROLLER_CONFIG = RequestParameters.CONTROLLER_CONFIG;
    private final String DYNAMIC_CONTROLLER_DEBUG_MODE = "debugMode";
    private final String DYNAMIC_CONTROLLER_URL = "controllerUrl";
    private final String GENDER = "gender";
    private final String SDK_PLUGIN_TYPE = "SDKPluginType";
    private final String TAG = DemandSourceConfig.class.getSimpleName();
    private final String USER_ID = ServerResponseWrapper.USER_ID_FIELD;
    private String mProviderName;

    public DemandSourceConfig(String providerName) {
        super(providerName);
        this.mProviderName = providerName;
    }

    String getRVUserAgeGroup() {
        return this.mProviderSettings.getRewardedVideoSettings().optString(APPLICATION_USER_AGE_GROUP);
    }

    String getISUserAgeGroup() {
        return this.mProviderSettings.getInterstitialSettings().optString(APPLICATION_USER_AGE_GROUP);
    }

    public String getRVDynamicControllerUrl() {
        return this.mProviderSettings.getRewardedVideoSettings().optString("controllerUrl");
    }

    String getISDynamicControllerUrl() {
        return this.mProviderSettings.getInterstitialSettings().optString("controllerUrl");
    }

    public int getRVDebugMode() {
        if (this.mProviderSettings.getRewardedVideoSettings().has("debugMode")) {
            return this.mProviderSettings.getRewardedVideoSettings().optInt("debugMode");
        }
        return 0;
    }

    public int getISDebugMode() {
        if (this.mProviderSettings.getInterstitialSettings().has("debugMode")) {
            return this.mProviderSettings.getInterstitialSettings().optInt("debugMode");
        }
        return 0;
    }

    public String getRVControllerConfig() {
        String config = BuildConfig.FLAVOR;
        if (this.mProviderSettings == null || this.mProviderSettings.getRewardedVideoSettings() == null || !this.mProviderSettings.getRewardedVideoSettings().has(RequestParameters.CONTROLLER_CONFIG)) {
            return config;
        }
        return this.mProviderSettings.getRewardedVideoSettings().optString(RequestParameters.CONTROLLER_CONFIG);
    }

    public String getISControllerConfig() {
        String config = BuildConfig.FLAVOR;
        if (this.mProviderSettings == null || this.mProviderSettings.getInterstitialSettings() == null || !this.mProviderSettings.getInterstitialSettings().has(RequestParameters.CONTROLLER_CONFIG)) {
            return config;
        }
        return this.mProviderSettings.getInterstitialSettings().optString(RequestParameters.CONTROLLER_CONFIG);
    }

    public String getMaxVideoLength() {
        return this.mProviderSettings.getRewardedVideoSettings().optString(MAX_VIDEO_LENGTH);
    }

    public String getLanguage() {
        return this.mProviderSettings.getRewardedVideoSettings().optString(LANGUAGE);
    }

    public String getPrivateKey() {
        return this.mProviderSettings.getRewardedVideoSettings().optString("privateKey");
    }

    public String getItemName() {
        return this.mProviderSettings.getRewardedVideoSettings().optString(ITEM_NAME);
    }

    public int getItemCount() {
        int itemCount = -1;
        try {
            String itemCountString = this.mProviderSettings.getRewardedVideoSettings().optString(ITEM_COUNT);
            if (!TextUtils.isEmpty(itemCountString)) {
                itemCount = Integer.valueOf(itemCountString).intValue();
            }
        } catch (NumberFormatException e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceTag.NATIVE, this.TAG + ":getItemCount()", e);
        }
        return itemCount;
    }

    String getCampaignId() {
        return this.mProviderSettings.getRewardedVideoSettings().optString(CAMPAIGN_ID);
    }

    String getMediationSegment() {
        return this.mProviderSettings.getRewardedVideoSettings().optString(CUSTOM_SEGMENT);
    }

    int getMaxVideos() {
        return getMaxVideosToPresent();
    }

    int getMaxRVAdsPerIteration() {
        return getMaxRVAdsPerIterationToPresent();
    }

    public int getMaxISAdsPerIteration() {
        return getMaxISAdsPerIterationToPresent();
    }

    String getRVUserGender() {
        return this.mProviderSettings.getRewardedVideoSettings().optString(APPLICATION_USER_GENDER);
    }

    String getISUserGender() {
        return this.mProviderSettings.getInterstitialSettings().optString(APPLICATION_USER_GENDER);
    }

    public void setMediationSegment(String segment) {
        this.mProviderSettings.setRewardedVideoSettings(CUSTOM_SEGMENT, segment);
    }

    public void setUserAgeGroup(int age) {
        String ageGroup = AppEventsConstants.EVENT_PARAM_VALUE_NO;
        if (age >= 13 && age <= 17) {
            ageGroup = ErrorCodes.FOLDER_NOT_EXIST_CODE;
        } else if (age >= 18 && age <= 20) {
            ageGroup = "2";
        } else if (age >= 21 && age <= 24) {
            ageGroup = "3";
        } else if (age >= 25 && age <= 34) {
            ageGroup = "4";
        } else if (age >= 35 && age <= 44) {
            ageGroup = "5";
        } else if (age >= 45 && age <= 54) {
            ageGroup = "6";
        } else if (age >= 55 && age <= 64) {
            ageGroup = "7";
        } else if (age > 65 && age <= 120) {
            ageGroup = "8";
        }
        this.mProviderSettings.setRewardedVideoSettings(APPLICATION_USER_AGE_GROUP, ageGroup);
        this.mProviderSettings.setInterstitialSettings(APPLICATION_USER_AGE_GROUP, ageGroup);
    }

    public void setUserGender(String gender) {
        this.mProviderSettings.setRewardedVideoSettings(APPLICATION_USER_GENDER, gender);
        this.mProviderSettings.setInterstitialSettings(APPLICATION_USER_GENDER, gender);
    }

    protected ArrayList<String> initializeMandatoryFields() {
        ArrayList<String> result = new ArrayList();
        result.add("controllerUrl");
        return result;
    }

    protected ArrayList<String> initializeOptionalFields() {
        ArrayList<String> result = new ArrayList();
        result.add(CLIENT_SIDE_CALLBACKS);
        result.add(APPLICATION_USER_GENDER);
        result.add(APPLICATION_USER_AGE_GROUP);
        result.add(LANGUAGE);
        result.add("maxAdsPerSession");
        result.add("maxAdsPerIteration");
        result.add("privateKey");
        result.add(MAX_VIDEO_LENGTH);
        result.add(ITEM_NAME);
        result.add(ITEM_COUNT);
        result.add("SDKPluginType");
        result.add(RequestParameters.CONTROLLER_CONFIG);
        result.add("debugMode");
        result.add(IronSourceConstants.REQUEST_URL);
        result.add(CUSTOM_SEGMENT);
        return result;
    }

    protected void validateOptionalField(JSONObject config, String key, ConfigValidationResult result) {
        try {
            if ("maxAdsPerSession".equals(key)) {
                validateMaxVideos(config.optInt(key), result);
            } else if (!"maxAdsPerIteration".equals(key) && !"debugMode".equals(key) && !RequestParameters.CONTROLLER_CONFIG.equals(key)) {
                String value = (String) config.get(key);
                if (CLIENT_SIDE_CALLBACKS.equals(key)) {
                    validateClientSideCallbacks(value, result);
                } else if (APPLICATION_USER_GENDER.equals(key)) {
                    validateGender(value, result);
                } else if (APPLICATION_USER_AGE_GROUP.equals(key)) {
                    validateAgeGroup(value, result);
                } else if (LANGUAGE.equals(key)) {
                    validateLanguage(value, result);
                } else if (MAX_VIDEO_LENGTH.equals(key)) {
                    validateMaxVideoLength(value, result);
                } else if ("privateKey".equals(key)) {
                    validatePrivateKey(value, result);
                } else if (ITEM_NAME.equals(key)) {
                    validateItemName(value, result);
                } else if (ITEM_COUNT.equals(key)) {
                    validateItemCount(value, result);
                }
            }
        } catch (Throwable th) {
            result.setInvalid(ErrorBuilder.buildInvalidKeyValueError(key, this.mProviderName, null));
        }
    }

    private void validateItemCount(String value, ConfigValidationResult result) {
        try {
            int itemCount = Integer.parseInt(value.trim());
            if (itemCount < 1 || itemCount > 100000) {
                result.setInvalid(ErrorBuilder.buildInvalidKeyValueError(ITEM_COUNT, this.mProviderName, "itemCount value should be between 1-100000"));
            }
        } catch (NumberFormatException e) {
            result.setInvalid(ErrorBuilder.buildInvalidKeyValueError(ITEM_COUNT, this.mProviderName, "itemCount value should be between 1-100000"));
        }
    }

    private void validateItemName(String value, ConfigValidationResult result) {
        if (value == null) {
            result.setInvalid(ErrorBuilder.buildInvalidKeyValueError(ITEM_NAME, this.mProviderName, "itemNamelength should be between 1-50 characters"));
        } else if (value.length() < 1 || value.length() > 50) {
            result.setInvalid(ErrorBuilder.buildInvalidKeyValueError(ITEM_NAME, this.mProviderName, "itemNamelength should be between 1-50 characters"));
        }
    }

    private void validatePrivateKey(String value, ConfigValidationResult result) {
        if (value == null) {
            result.setInvalid(ErrorBuilder.buildInvalidKeyValueError("privateKey", this.mProviderName, "privateKey length should be between 5-30 characters"));
        } else if (value.length() < 5 || value.length() > 30) {
            result.setInvalid(ErrorBuilder.buildInvalidKeyValueError("privateKey", this.mProviderName, "privateKey length should be between 5-30 characters"));
        } else if (!value.matches("^[a-zA-Z0-9]*$")) {
            result.setInvalid(ErrorBuilder.buildInvalidKeyValueError("privateKey", this.mProviderName, "privateKey should contains only characters and numbers"));
        }
    }

    private void validateMaxVideoLength(String value, ConfigValidationResult result) {
        try {
            int age = Integer.parseInt(value.trim());
            if (age < 1 || age > GameControllerDelegate.THUMBSTICK_LEFT_X) {
                result.setInvalid(ErrorBuilder.buildInvalidKeyValueError(MAX_VIDEO_LENGTH, this.mProviderName, "maxVideoLength value should be between 1-1000"));
            }
        } catch (NumberFormatException e) {
            result.setInvalid(ErrorBuilder.buildInvalidKeyValueError(MAX_VIDEO_LENGTH, this.mProviderName, "maxVideoLength value should be between 1-1000"));
        }
    }

    private void validateLanguage(String value, ConfigValidationResult result) {
        if (value != null) {
            value = value.trim();
            if (!value.matches("^[a-zA-Z]*$") || value.length() != 2) {
                result.setInvalid(ErrorBuilder.buildInvalidKeyValueError(LANGUAGE, this.mProviderName, "language value should be two letters format."));
                return;
            }
            return;
        }
        result.setInvalid(ErrorBuilder.buildInvalidKeyValueError(LANGUAGE, this.mProviderName, "language value should be two letters format."));
    }

    private void validateAgeGroup(String value, ConfigValidationResult result) {
        try {
            int age = Integer.parseInt(value.trim());
            if (age < 0 || age > 8) {
                result.setInvalid(ErrorBuilder.buildInvalidKeyValueError(APPLICATION_USER_AGE_GROUP, this.mProviderName, "applicationUserAgeGroup value should be between 0-8"));
            }
        } catch (NumberFormatException e) {
            result.setInvalid(ErrorBuilder.buildInvalidKeyValueError(APPLICATION_USER_AGE_GROUP, this.mProviderName, "applicationUserAgeGroup value should be between 0-8"));
        }
    }

    private void validateGender(String gender, ConfigValidationResult result) {
        if (gender != null) {
            try {
                gender = gender.toLowerCase().trim();
                if (!Gender.MALE.equals(gender) && !Gender.FEMALE.equals(gender) && !Gender.UNKNOWN.equals(gender)) {
                    result.setInvalid(ErrorBuilder.buildInvalidKeyValueError("gender", this.mProviderName, "gender value should be one of male/female/unknown."));
                }
            } catch (Exception e) {
                result.setInvalid(ErrorBuilder.buildInvalidKeyValueError("gender", this.mProviderName, "gender value should be one of male/female/unknown."));
            }
        }
    }

    private void validateClientSideCallbacks(String value, ConfigValidationResult result) {
        validateBoolean(CLIENT_SIDE_CALLBACKS, value, result);
    }

    protected void validateMandatoryField(JSONObject config, String key, ConfigValidationResult result) {
        try {
            String value = config.optString(key);
            if (RequestParameters.APPLICATION_KEY.equals(key)) {
                validateApplicationKey(value, result);
            } else if (ServerResponseWrapper.USER_ID_FIELD.equals(key)) {
                validateUserId(value, result);
            } else if ("controllerUrl".equals(key)) {
                validateDynamicUrl(value, result);
            }
        } catch (Throwable th) {
            result.setInvalid(ErrorBuilder.buildInvalidKeyValueError(key, this.mProviderName, null));
        }
    }

    protected void adapterPostValidation(JSONObject config, ConfigValidationResult result) {
        try {
            validatePrivateKeyItemNameCountCombination(config, result);
        } catch (Exception e) {
            result.setInvalid(ErrorBuilder.buildGenericError(BuildConfig.FLAVOR));
        }
    }

    private void validatePrivateKeyItemNameCountCombination(JSONObject config, ConfigValidationResult result) {
        if (!config.has("privateKey") && !config.has(ITEM_NAME) && !config.has(ITEM_COUNT)) {
            return;
        }
        if (!config.has("privateKey") || !config.has(ITEM_NAME) || !config.has(ITEM_COUNT)) {
            result.setInvalid(ErrorBuilder.buildInvalidKeyValueError("itemName, itemCount or privateKey", this.mProviderName, "configure itemName/itemCount requires the following configurations: itemName, itemCount and privateKey"));
        }
    }

    private void validateUserId(String value, ConfigValidationResult result) {
        if (value == null) {
            result.setInvalid(ErrorBuilder.buildInvalidKeyValueError(ServerResponseWrapper.USER_ID_FIELD, this.mProviderName, "userId is missing"));
        } else if (value.length() < 1 || value.length() > 64) {
            result.setInvalid(ErrorBuilder.buildInvalidKeyValueError(ServerResponseWrapper.USER_ID_FIELD, this.mProviderName, "userId value should be between 1-64 characters"));
        }
    }

    private void validateDynamicUrl(String value, ConfigValidationResult result) {
        if (TextUtils.isEmpty(value)) {
            result.setInvalid(ErrorBuilder.buildInvalidKeyValueError("controllerUrl", this.mProviderName, "controllerUrl is missing"));
        }
    }

    private void validateApplicationKey(String value, ConfigValidationResult result) {
        if (value != null) {
            value = value.trim();
            if (value.length() < 5 || value.length() > 10) {
                result.setInvalid(ErrorBuilder.buildInvalidKeyValueError(RequestParameters.APPLICATION_KEY, this.mProviderName, "applicationKey length should be between 5-10 characters"));
                return;
            } else if (!value.matches("^[a-zA-Z0-9]*$")) {
                result.setInvalid(ErrorBuilder.buildInvalidKeyValueError(RequestParameters.APPLICATION_KEY, this.mProviderName, "applicationKey value should contains only english characters and numbers"));
                return;
            } else {
                return;
            }
        }
        result.setInvalid(ErrorBuilder.buildInvalidKeyValueError(RequestParameters.APPLICATION_KEY, this.mProviderName, "applicationKey value is missing"));
    }
}
