package com.ironsource.mediationsdk.config;

import android.text.TextUtils;
import com.facebook.internal.ServerProtocol;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.model.ProviderSettings;
import com.ironsource.mediationsdk.model.ProviderSettingsHolder;
import com.ironsource.mediationsdk.sdk.ConfigValidator;
import com.ironsource.mediationsdk.utils.ErrorBuilder;
import com.ironsource.mediationsdk.utils.IronSourceConstants;
import java.util.ArrayList;
import java.util.Iterator;
import org.cocos2dx.lib.BuildConfig;
import org.json.JSONObject;

public abstract class AbstractAdapterConfig implements ConfigValidator {
    protected final String MAX_ADS_KEY = "maxAdsPerSession";
    protected final String MAX_ADS_PER_ITERATION_KEY = "maxAdsPerIteration";
    protected final String REQUEST_URL_KEY = IronSourceConstants.REQUEST_URL;
    private ArrayList<String> mMandatoryKeys;
    private ArrayList<String> mOptionalKeys;
    private String mProviderName;
    protected ProviderSettings mProviderSettings;

    protected abstract void adapterPostValidation(JSONObject jSONObject, ConfigValidationResult configValidationResult);

    protected abstract ArrayList<String> initializeMandatoryFields();

    protected abstract ArrayList<String> initializeOptionalFields();

    protected abstract void validateMandatoryField(JSONObject jSONObject, String str, ConfigValidationResult configValidationResult);

    protected abstract void validateOptionalField(JSONObject jSONObject, String str, ConfigValidationResult configValidationResult);

    public AbstractAdapterConfig(String providerName) {
        this.mProviderSettings = ProviderSettingsHolder.getProviderSettingsHolder().getProviderSettings(providerName);
        this.mProviderName = providerName;
        this.mMandatoryKeys = initializeMandatoryFields();
        if (this.mMandatoryKeys == null) {
            this.mMandatoryKeys = new ArrayList();
        }
        this.mOptionalKeys = initializeOptionalFields();
        if (this.mOptionalKeys == null) {
            this.mOptionalKeys = new ArrayList();
        }
    }

    protected int getMaxRVAdsPerIterationToPresent() {
        int result = Integer.MAX_VALUE;
        try {
            if (this.mProviderSettings != null) {
                result = this.mProviderSettings.getRewardedVideoSettings().optInt("maxAdsPerIteration");
            }
        } catch (Exception e) {
        }
        return result;
    }

    protected int getMaxISAdsPerIterationToPresent() {
        int result = Integer.MAX_VALUE;
        try {
            if (this.mProviderSettings != null) {
                result = this.mProviderSettings.getInterstitialSettings().optInt("maxAdsPerIteration");
            }
        } catch (Exception e) {
        }
        return result;
    }

    protected int getMaxVideosToPresent() {
        int result = Integer.MAX_VALUE;
        try {
            if (this.mProviderSettings != null && this.mProviderSettings.getRewardedVideoSettings().has("maxAdsPerSession")) {
                result = this.mProviderSettings.getRewardedVideoSettings().optInt("maxAdsPerSession");
            }
        } catch (Exception e) {
        }
        return result;
    }

    public ConfigValidationResult isRVConfigValid() {
        ConfigValidationResult result = new ConfigValidationResult();
        checkForAllMandatoryFields(this.mProviderSettings.getRewardedVideoSettings(), this.mMandatoryKeys, result);
        if (result.isValid()) {
            validateAllFields(this.mProviderSettings.getRewardedVideoSettings(), result);
        }
        if (result.isValid()) {
            adapterPostValidation(this.mProviderSettings.getRewardedVideoSettings(), result);
            if (!result.isValid()) {
                logConfigWarningMessage(result.getIronSourceError());
                result.setValid();
            }
        }
        IronSourceLoggerManager.getLogger().log(IronSourceTag.NATIVE, this.mProviderName + ":isConfigValid:result(valid:" + result.isValid() + ")", 0);
        return result;
    }

    public ConfigValidationResult isISConfigValid() {
        ConfigValidationResult result = new ConfigValidationResult();
        checkForAllMandatoryFields(this.mProviderSettings.getInterstitialSettings(), this.mMandatoryKeys, result);
        if (result.isValid()) {
            validateAllFields(this.mProviderSettings.getInterstitialSettings(), result);
        }
        if (result.isValid()) {
            adapterPostValidation(this.mProviderSettings.getInterstitialSettings(), result);
            if (!result.isValid()) {
                logConfigWarningMessage(result.getIronSourceError());
                result.setValid();
            }
        }
        IronSourceLoggerManager.getLogger().log(IronSourceTag.NATIVE, this.mProviderName + ":isConfigValid:result(valid:" + result.isValid() + ")", 0);
        return result;
    }

    public ConfigValidationResult isBannerConfigValid() {
        ConfigValidationResult result = new ConfigValidationResult();
        checkForAllMandatoryFields(this.mProviderSettings.getBannerSettings(), this.mMandatoryKeys, result);
        if (result.isValid()) {
            validateAllFields(this.mProviderSettings.getBannerSettings(), result);
        }
        if (result.isValid()) {
            adapterPostValidation(this.mProviderSettings.getBannerSettings(), result);
            if (!result.isValid()) {
                logConfigWarningMessage(result.getIronSourceError());
                result.setValid();
            }
        }
        IronSourceLoggerManager.getLogger().log(IronSourceTag.NATIVE, this.mProviderName + ":isConfigValid:result(valid:" + result.isValid() + ")", 0);
        return result;
    }

    public void validateOptionalKeys(ArrayList<String> keys) {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.NATIVE, this.mProviderName + ":validateOptionalKeys", 1);
        ConfigValidationResult result = new ConfigValidationResult();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (isOptionalField(key)) {
                validateOptionalField(this.mProviderSettings.getRewardedVideoSettings(), key, result);
                if (!result.isValid()) {
                    logConfigWarningMessage(result.getIronSourceError());
                    result.setValid();
                }
            } else {
                IronSourceLoggerManager.getLogger().log(IronSourceTag.NATIVE, this.mProviderName + ":validateOptionalKeys(" + key + ")", 0);
            }
        }
    }

    private void checkForAllMandatoryFields(JSONObject config, ArrayList<String> mandatoryKeys, ConfigValidationResult result) {
        if (mandatoryKeys == null || config == null) {
            result.setInvalid(ErrorBuilder.buildGenericError(this.mProviderName + " - Wrong configuration"));
            return;
        }
        Iterator it = mandatoryKeys.iterator();
        while (it.hasNext()) {
            String mandatory = (String) it.next();
            if (config.has(mandatory)) {
                try {
                    if (TextUtils.isEmpty(config.get(mandatory).toString())) {
                        result.setInvalid(ErrorBuilder.buildInvalidKeyValueError(mandatory, this.mProviderName, null));
                        return;
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    result.setInvalid(ErrorBuilder.buildInvalidKeyValueError(mandatory, this.mProviderName, null));
                    return;
                }
            }
            result.setInvalid(ErrorBuilder.buildKeyNotSetError(mandatory, this.mProviderName, BuildConfig.FLAVOR));
            return;
        }
    }

    private void validateAllFields(JSONObject config, ConfigValidationResult result) {
        try {
            Iterator<String> keysIterator = config.keys();
            while (result.isValid() && keysIterator.hasNext()) {
                String key = (String) keysIterator.next();
                if (isMandatoryField(key)) {
                    validateMandatoryField(config, key, result);
                } else if (isOptionalField(key)) {
                    validateOptionalField(config, key, result);
                    if (!result.isValid()) {
                        logConfigWarningMessage(result.getIronSourceError());
                        keysIterator.remove();
                        result.setValid();
                    }
                } else {
                    IronSourceLoggerManager.getLogger().log(IronSourceTag.ADAPTER_API, this.mProviderName + ":Unknown key in configuration - " + key, 2);
                }
            }
        } catch (Throwable th) {
            result.setInvalid(ErrorBuilder.buildGenericError(this.mProviderName + " - Invalid configuration"));
        }
    }

    private boolean isOptionalField(String key) {
        return this.mOptionalKeys.contains(key);
    }

    private boolean isMandatoryField(String key) {
        return this.mMandatoryKeys.contains(key);
    }

    protected void validateMaxVideos(int maxVideos, ConfigValidationResult result) {
        if (maxVideos < 0) {
            result.setInvalid(ErrorBuilder.buildInvalidKeyValueError("maxVideos", this.mProviderName, "maxVideos value should be any integer >= 0, your value is:" + maxVideos));
        }
    }

    protected void validateNonEmptyString(String key, String value, ConfigValidationResult result) {
        if (TextUtils.isEmpty(value)) {
            result.setInvalid(ErrorBuilder.buildInvalidKeyValueError(key, this.mProviderName, "value is empty"));
        }
    }

    private void logConfigWarningMessage(IronSourceError error) {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.ADAPTER_API, error.toString(), 2);
    }

    protected void validateBoolean(String key, String value, ConfigValidationResult result) {
        value = value.trim();
        if (!value.equalsIgnoreCase(ServerProtocol.DIALOG_RETURN_SCOPES_TRUE) && !value.equalsIgnoreCase("false")) {
            result.setInvalid(ErrorBuilder.buildInvalidKeyValueError(key, this.mProviderName, "value should be 'true'/'false'"));
        }
    }
}
