package com.ironsource.mediationsdk;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import com.ironsource.eventsmodule.EventData;
import com.ironsource.mediationsdk.events.RewardedVideoEventsManager;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.model.OfferwallPlacement;
import com.ironsource.mediationsdk.model.ProviderSettings;
import com.ironsource.mediationsdk.sdk.InternalOfferwallApi;
import com.ironsource.mediationsdk.sdk.InternalOfferwallListener;
import com.ironsource.mediationsdk.sdk.OfferwallApi;
import com.ironsource.mediationsdk.sdk.OfferwallListener;
import com.ironsource.mediationsdk.utils.ErrorBuilder;
import com.ironsource.mediationsdk.utils.IronSourceConstants;
import com.ironsource.mediationsdk.utils.IronSourceUtils;
import com.ironsource.mediationsdk.utils.ServerResponseWrapper;
import cz.msebera.android.httpclient.HttpStatus;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONException;
import org.json.JSONObject;

class OfferwallManager extends AbstractAdUnitManager implements InternalOfferwallApi, InternalOfferwallListener {
    private final String GENERAL_PROPERTIES_USER_ID = ServerResponseWrapper.USER_ID_FIELD;
    private final String TAG = getClass().getName();
    private OfferwallApi mAdapter;
    private AtomicBoolean mAtomicShouldPerformInit = new AtomicBoolean(true);
    private String mCurrentPlacementName;
    private AtomicBoolean mIsOfferwallAvailable = new AtomicBoolean(false);
    private InternalOfferwallListener mListenersWrapper;
    private IronSourceLoggerManager mLoggerManager = IronSourceLoggerManager.getLogger();
    private ServerResponseWrapper mServerResponseWrapper;

    void shouldTrackNetworkState(Context context, boolean track) {
    }

    boolean isPremiumAdapter(String providerName) {
        return false;
    }

    boolean isBackFillAvailable() {
        return false;
    }

    public synchronized void initOfferwall(Activity activity, String appKey, String userId) {
        this.mLoggerManager.log(IronSourceTag.NATIVE, this.TAG + ":initOfferwall(appKey: " + appKey + ", userId: " + userId + ")", 1);
        this.mAppKey = appKey;
        this.mUserId = userId;
        this.mActivity = activity;
        this.mServerResponseWrapper = IronSourceObject.getInstance().getCurrentServerResponse();
        if (this.mServerResponseWrapper != null) {
            ArrayList<AbstractAdapter> startedAdapters = startAdapters(activity, userId, this.mServerResponseWrapper);
            if (startedAdapters == null || startedAdapters.isEmpty()) {
                reportInitFail(ErrorBuilder.buildInitFailedError("Please check configurations for Offerwall adapters", IronSourceConstants.OFFERWALL_AD_UNIT));
            }
        }
    }

    private synchronized void reportInitFail(IronSourceError error) {
        if (this.mIsOfferwallAvailable != null) {
            this.mIsOfferwallAvailable.set(false);
        }
        if (this.mAtomicShouldPerformInit != null) {
            this.mAtomicShouldPerformInit.set(true);
        }
        if (this.mListenersWrapper != null) {
            this.mListenersWrapper.onOfferwallAvailable(false, error);
        }
    }

    private ArrayList<AbstractAdapter> startAdapters(Activity activity, String userId, ServerResponseWrapper serverResponseWrapper) {
        ArrayList<AbstractAdapter> adapterList = new ArrayList();
        ProviderSettings settings = serverResponseWrapper.getProviderSettingsHolder().getProviderSettings(IronSourceConstants.IRONSOURCE_CONFIG_NAME);
        String providerName = IronSourceConstants.IRONSOURCE_CONFIG_NAME;
        String requestUrl = settings.getRewardedVideoSettings().optString(IronSourceConstants.REQUEST_URL);
        try {
            IronSourceObject sso = IronSourceObject.getInstance();
            AbstractAdapter providerAdapter = sso.getExistingAdapter(providerName);
            if (providerAdapter == null) {
                Class<?> mAdapterClass = Class.forName("com.ironsource.adapters." + providerName.toLowerCase() + "." + providerName + "Adapter");
                providerAdapter = (AbstractAdapter) mAdapterClass.getMethod(IronSourceConstants.START_ADAPTER, new Class[]{String.class, String.class}).invoke(mAdapterClass, new Object[]{providerName, requestUrl});
                if (providerAdapter != null) {
                    sso.addToAdaptersList(providerAdapter);
                }
            }
            setCustomParams(providerAdapter);
            providerAdapter.setLogListener(this.mLoggerManager);
            ((InternalOfferwallApi) providerAdapter).setInternalOfferwallListener(this);
            addOfferwallAdapter((OfferwallApi) providerAdapter);
            ((OfferwallApi) providerAdapter).initOfferwall(activity, IronSourceObject.getInstance().getIronSourceAppKey(), userId);
            adapterList.add(providerAdapter);
        } catch (Throwable e) {
            this.mLoggerManager.log(IronSourceTag.API, providerName + " initialization failed - please verify that required dependencies are in you build path.", 2);
            this.mLoggerManager.logException(IronSourceTag.API, this.TAG + ":startAdapter", e);
        }
        return adapterList;
    }

    private void addOfferwallAdapter(OfferwallApi adapter) {
        this.mAdapter = adapter;
    }

    public void onResume(Activity activity) {
    }

    public void onPause(Activity activity) {
    }

    public void setAge(int age) {
    }

    public void setGender(String gender) {
    }

    public void setMediationSegment(String segment) {
    }

    public void showOfferwall() {
    }

    public void showOfferwall(String placementName) {
        String logMessage = "OWManager:showOfferwall(" + placementName + ")";
        try {
            this.mCurrentPlacementName = placementName;
            OfferwallPlacement placement = this.mServerResponseWrapper.getConfigurations().getOfferwallConfigurations().getOfferwallPlacement(placementName);
            if (placement == null) {
                this.mLoggerManager.log(IronSourceTag.INTERNAL, "Placement is not valid, please make sure you are using the right placements, using the default placement.", 3);
                placement = this.mServerResponseWrapper.getConfigurations().getOfferwallConfigurations().getDefaultOfferwallPlacement();
                if (placement == null) {
                    this.mLoggerManager.log(IronSourceTag.INTERNAL, "Default placement was not found, please make sure you are using the right placements.", 3);
                    return;
                }
            }
            this.mLoggerManager.log(IronSourceTag.INTERNAL, logMessage, 1);
            if (this.mIsOfferwallAvailable != null && this.mIsOfferwallAvailable.get() && this.mAdapter != null) {
                this.mAdapter.showOfferwall(String.valueOf(placement.getPlacementId()));
            }
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceTag.INTERNAL, logMessage, e);
        }
    }

    public synchronized boolean isOfferwallAvailable() {
        boolean result;
        result = false;
        if (this.mIsOfferwallAvailable != null) {
            result = this.mIsOfferwallAvailable.get();
        }
        return result;
    }

    public void getOfferwallCredits() {
        if (this.mAdapter != null) {
            this.mAdapter.getOfferwallCredits();
        }
    }

    public void setOfferwallListener(OfferwallListener offerwallListener) {
    }

    public void setInternalOfferwallListener(InternalOfferwallListener listener) {
        this.mListenersWrapper = listener;
    }

    public void onOfferwallAvailable(boolean isAvailable) {
        onOfferwallAvailable(isAvailable, null);
    }

    public void onOfferwallAvailable(boolean isAvailable, IronSourceError error) {
        String logString = "onOfferwallAvailable(isAvailable: " + isAvailable + ")";
        if (error != null) {
            logString = logString + ", error: " + error.getErrorMessage();
        }
        this.mLoggerManager.log(IronSourceTag.ADAPTER_CALLBACK, logString, 1);
        if (isAvailable) {
            this.mIsOfferwallAvailable.set(true);
            this.mListenersWrapper.onOfferwallAvailable(isAvailable);
            return;
        }
        reportInitFail(error);
    }

    public void onOfferwallOpened() {
        this.mLoggerManager.log(IronSourceTag.ADAPTER_CALLBACK, "onOfferwallOpened()", 1);
        JSONObject data = IronSourceUtils.getMediationAdditionalData();
        try {
            if (!TextUtils.isEmpty(this.mCurrentPlacementName)) {
                data.put("placement", this.mCurrentPlacementName);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RewardedVideoEventsManager.getInstance().log(new EventData(HttpStatus.SC_USE_PROXY, data));
        this.mListenersWrapper.onOfferwallOpened();
    }

    public void onOfferwallShowFailed(IronSourceError error) {
        this.mLoggerManager.log(IronSourceTag.ADAPTER_CALLBACK, "onOfferwallShowFailed(" + error + ")", 1);
        this.mListenersWrapper.onOfferwallShowFailed(error);
    }

    public boolean onOfferwallAdCredited(int credits, int totalCredits, boolean totalCreditsFlag) {
        return this.mListenersWrapper.onOfferwallAdCredited(credits, totalCredits, totalCreditsFlag);
    }

    public void onGetOfferwallCreditsFailed(IronSourceError error) {
        this.mLoggerManager.log(IronSourceTag.ADAPTER_CALLBACK, "onGetOfferwallCreditsFailed(" + error + ")", 1);
        this.mListenersWrapper.onGetOfferwallCreditsFailed(error);
    }

    public void onOfferwallClosed() {
        this.mLoggerManager.log(IronSourceTag.ADAPTER_CALLBACK, "onOfferwallClosed()", 1);
        this.mListenersWrapper.onOfferwallClosed();
    }
}
