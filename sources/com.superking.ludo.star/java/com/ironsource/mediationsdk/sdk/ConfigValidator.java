package com.ironsource.mediationsdk.sdk;

import com.ironsource.mediationsdk.config.ConfigValidationResult;

public interface ConfigValidator {
    ConfigValidationResult isBannerConfigValid();

    ConfigValidationResult isISConfigValid();

    ConfigValidationResult isRVConfigValid();
}
