package com.ironsource.mediationsdk;

import android.app.Activity;
import android.content.Context;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.utils.ServerResponseWrapper;

abstract class AbstractAdUnitManager {
    Activity mActivity;
    String mAppKey;
    protected AbstractAdapter mBackFillAdapter;
    protected boolean mBackFillInitStarted;
    protected boolean mCanShowPremium = true;
    IronSourceLoggerManager mLoggerManager = IronSourceLoggerManager.getLogger();
    ServerResponseWrapper mServerResponseWrapper;
    boolean mShouldTrackNetworkState = false;
    String mUserId;

    abstract boolean isBackFillAvailable();

    abstract boolean isPremiumAdapter(String str);

    abstract void shouldTrackNetworkState(Context context, boolean z);

    AbstractAdUnitManager() {
    }

    protected void setCustomParams(AbstractAdapter providerAdapter) {
        try {
            Integer age = IronSourceObject.getInstance().getAge();
            if (age != null) {
                providerAdapter.setAge(age.intValue());
            }
            String gender = IronSourceObject.getInstance().getGender();
            if (gender != null) {
                providerAdapter.setGender(gender);
            }
            String segment = IronSourceObject.getInstance().getMediationSegment();
            if (segment != null) {
                providerAdapter.setMediationSegment(segment);
            }
        } catch (Exception e) {
            this.mLoggerManager.log(IronSourceTag.INTERNAL, providerAdapter.getProviderName() + ":setCustomParams():" + e.toString(), 3);
        }
    }

    protected boolean isBackFillAdapter(AbstractAdapter adapter) {
        if (this.mBackFillAdapter == null || adapter == null) {
            return false;
        }
        return adapter.getProviderName().equals(this.mBackFillAdapter.getProviderName());
    }

    protected synchronized boolean canShowPremium() {
        return this.mCanShowPremium;
    }

    protected synchronized void disablePremiumForCurrentSession() {
        this.mCanShowPremium = false;
    }
}
