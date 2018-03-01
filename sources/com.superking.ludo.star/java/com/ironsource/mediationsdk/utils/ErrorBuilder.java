package com.ironsource.mediationsdk.utils;

import android.text.TextUtils;
import com.ironsource.mediationsdk.logger.IronSourceError;
import cz.msebera.android.httpclient.HttpStatus;
import org.cocos2dx.lib.BuildConfig;

public class ErrorBuilder {
    public static IronSourceError buildNoConfigurationAvailableError(String adUnit) {
        return new IronSourceError(HttpStatus.SC_NOT_IMPLEMENTED, BuildConfig.FLAVOR + adUnit + " Init Fail - Unable to retrieve configurations from the server");
    }

    public static IronSourceError buildInvalidConfigurationError(String adUnit) {
        return new IronSourceError(HttpStatus.SC_NOT_IMPLEMENTED, BuildConfig.FLAVOR + adUnit + " Init Fail - Configurations from the server are not valid");
    }

    public static IronSourceError buildUsingCachedConfigurationError(String appKey, String userId) {
        return new IronSourceError(HttpStatus.SC_BAD_GATEWAY, "Mediation - Unable to retrieve configurations from IronSource server, using cached configurations with appKey:" + appKey + " and userId:" + userId);
    }

    public static IronSourceError buildKeyNotSetError(String key, String provider, String adUnit) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(provider)) {
            return getGenericErrorForMissingParams();
        }
        return new IronSourceError(HttpStatus.SC_HTTP_VERSION_NOT_SUPPORTED, adUnit + " Mediation - " + key + " is not set for " + provider);
    }

    public static IronSourceError buildInvalidKeyValueError(String key, String provider, String optionalReason) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(provider)) {
            return getGenericErrorForMissingParams();
        }
        return new IronSourceError(IronSourceError.ERROR_CODE_INVALID_KEY_VALUE, "Mediation - " + key + " value is not valid for " + provider + (!TextUtils.isEmpty(optionalReason) ? " - " + optionalReason : BuildConfig.FLAVOR));
    }

    public static IronSourceError buildInvalidCredentialsError(String credentialName, String credentialValue, String errorMessage) {
        return new IronSourceError(IronSourceError.ERROR_CODE_INVALID_KEY_VALUE, "Init Fail - " + credentialName + " value " + credentialValue + " is not valid" + (!TextUtils.isEmpty(errorMessage) ? " - " + errorMessage : BuildConfig.FLAVOR));
    }

    public static IronSourceError buildInitFailedError(String errorMsg, String adUnit) {
        if (TextUtils.isEmpty(errorMsg)) {
            errorMsg = adUnit + " init failed due to an unknown error";
        } else {
            errorMsg = adUnit + " - " + errorMsg;
        }
        return new IronSourceError(IronSourceError.ERROR_CODE_INIT_FAILED, errorMsg);
    }

    public static IronSourceError buildNoAdsToShowError(String adUnit) {
        return new IronSourceError(IronSourceError.ERROR_CODE_NO_ADS_TO_SHOW, adUnit + " Show Fail - No ads to show");
    }

    public static IronSourceError buildShowFailedError(String adUnit, String error) {
        return new IronSourceError(IronSourceError.ERROR_CODE_NO_ADS_TO_SHOW, adUnit + " Show Fail - " + error);
    }

    public static IronSourceError buildLoadFailedError(String adUnit, String adapterName, String errorMsg) {
        String resultingMessage = BuildConfig.FLAVOR + adUnit + " Load Fail" + (!TextUtils.isEmpty(adapterName) ? " " + adapterName : BuildConfig.FLAVOR) + " - ";
        if (TextUtils.isEmpty(errorMsg)) {
            errorMsg = "unknown error";
        }
        return new IronSourceError(IronSourceError.ERROR_CODE_GENERIC, resultingMessage + errorMsg);
    }

    public static IronSourceError buildGenericError(String errorMsg) {
        if (TextUtils.isEmpty(errorMsg)) {
            errorMsg = "An error occurred";
        }
        return new IronSourceError(IronSourceError.ERROR_CODE_GENERIC, errorMsg);
    }

    public static IronSourceError buildNoInternetConnectionInitFailError(String adUnit) {
        return new IronSourceError(IronSourceError.ERROR_NO_INTERNET_CONNECTION, BuildConfig.FLAVOR + adUnit + " Init Fail - No Internet connection");
    }

    public static IronSourceError buildNoInternetConnectionLoadFailError(String adUnit) {
        return new IronSourceError(IronSourceError.ERROR_NO_INTERNET_CONNECTION, BuildConfig.FLAVOR + adUnit + " Load Fail - No Internet connection");
    }

    public static IronSourceError buildNoInternetConnectionShowFailError(String adUnit) {
        return new IronSourceError(IronSourceError.ERROR_NO_INTERNET_CONNECTION, BuildConfig.FLAVOR + adUnit + " Show Fail - No Internet connection");
    }

    public static IronSourceError buildCappedError(String adUnit, String error) {
        return new IronSourceError(IronSourceError.ERROR_REACHED_CAP_LIMIT, adUnit + " Show Fail - " + error);
    }

    private static IronSourceError getGenericErrorForMissingParams() {
        return buildGenericError("Mediation - wrong configuration");
    }

    public static IronSourceError buildLoadFailedError(String errorMsg) {
        if (TextUtils.isEmpty(errorMsg)) {
            errorMsg = "Load failed due to an unknown error";
        } else {
            errorMsg = "Load failed - " + errorMsg;
        }
        return new IronSourceError(IronSourceError.ERROR_CODE_GENERIC, errorMsg);
    }
}
