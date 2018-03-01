package com.ironsource.sdk;

import android.app.Activity;
import com.ironsource.sdk.agent.IronSourceAdsAdvertiserAgent;
import com.ironsource.sdk.agent.IronSourceAdsPublisherAgent;
import com.ironsource.sdk.data.SSAEnums.DebugMode;

public class SSAFactory {
    public static SSAPublisher getPublisherInstance(Activity activity) {
        return IronSourceAdsPublisherAgent.getInstance(activity);
    }

    public static SSAPublisher getPublisherTestInstance(Activity activity) {
        return IronSourceAdsPublisherAgent.getInstance(activity, DebugMode.MODE_2.getValue());
    }

    public static SSAPublisher getPublisherTestInstance(Activity activity, int debugMode) {
        return IronSourceAdsPublisherAgent.getInstance(activity, debugMode);
    }

    public static SSAAdvertiser getAdvertiserInstance() {
        return IronSourceAdsAdvertiserAgent.getInstance();
    }

    public static SSAAdvertiserTest getAdvertiserTestInstance() {
        return IronSourceAdsAdvertiserAgent.getInstance();
    }
}
