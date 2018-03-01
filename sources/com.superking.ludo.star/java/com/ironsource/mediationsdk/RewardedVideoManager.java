package com.ironsource.mediationsdk;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.text.TextUtils;
import com.facebook.internal.ServerProtocol;
import com.ironsource.environment.NetworkStateReceiver;
import com.ironsource.environment.NetworkStateReceiver.NetworkStateReceiverListener;
import com.ironsource.eventsmodule.EventData;
import com.ironsource.mediationsdk.config.ConfigFile;
import com.ironsource.mediationsdk.events.RewardedVideoEventsManager;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.model.ProviderSettings;
import com.ironsource.mediationsdk.sdk.RewardedVideoApi;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoManagerListener;
import com.ironsource.mediationsdk.server.Server;
import com.ironsource.mediationsdk.utils.CappingManager;
import com.ironsource.mediationsdk.utils.ErrorBuilder;
import com.ironsource.mediationsdk.utils.IronSourceConstants;
import com.ironsource.mediationsdk.utils.IronSourceUtils;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import java.util.ArrayList;
import java.util.Iterator;
import org.cocos2dx.lib.BuildConfig;
import org.json.JSONException;
import org.json.JSONObject;

class RewardedVideoManager extends AbstractAdUnitManager implements NetworkStateReceiverListener, RewardedVideoApi, RewardedVideoManagerListener {
    private final String KTO_ALGORITHM = "KTO";
    private final String TAG = getClass().getSimpleName();
    private ArrayList<AbstractAdapter> mAvailableAdapters;
    private boolean mDidReportInitialAvailability = false;
    private ArrayList<AbstractAdapter> mExhaustedAdapters;
    private ArrayList<AbstractAdapter> mInitiatedAdapters;
    private boolean mIsAdAvailable;
    private RewardedVideoListener mListenersWrapper;
    private NetworkStateReceiver mNetworkStateReceiver;
    private ArrayList<AbstractAdapter> mNotAvailableAdapters;
    private boolean mPauseSmartLoadDueToNetworkUnavailability = false;

    public RewardedVideoManager() {
        prepareStateForInit();
    }

    private void prepareStateForInit() {
        this.mIsAdAvailable = false;
        this.mAvailableAdapters = new ArrayList();
        this.mInitiatedAdapters = new ArrayList();
        this.mNotAvailableAdapters = new ArrayList();
        this.mExhaustedAdapters = new ArrayList();
    }

    private synchronized void reportImpression(String adapterUrl, boolean hit, int placementId) {
        String url;
        try {
            url = (BuildConfig.FLAVOR + adapterUrl) + "&sdkVersion=" + IronSourceUtils.getSDKVersion();
            Server.callAsyncRequestURL(url, hit, placementId);
        } catch (Throwable e) {
            this.mLoggerManager.logException(IronSourceTag.NETWORK, "reportImpression:(providerURL:" + url + ", " + "hit:" + hit + ")", e);
        }
    }

    private void reportFalseImpressionsOnHigherPriority(int priority, int placementId) {
        ArrayList<String> providers = this.mServerResponseWrapper.getProviderOrder().getRewardedVideoProviderOrder();
        int i = 0;
        while (i < priority) {
            if (!isExhausted((String) providers.get(i)) && (!isPremiumAdapter((String) providers.get(i)) || canShowPremium())) {
                ProviderSettings providerSettings = this.mServerResponseWrapper.getProviderSettingsHolder().getProviderSettings((String) providers.get(i));
                if (providerSettings != null) {
                    reportImpression(providerSettings.getRewardedVideoSettings().optString(IronSourceConstants.REQUEST_URL), false, placementId);
                }
            }
            i++;
        }
    }

    private boolean isExhausted(String providerName) {
        Iterator it = this.mExhaustedAdapters.iterator();
        while (it.hasNext()) {
            if (((AbstractAdapter) it.next()).getProviderName().equalsIgnoreCase(providerName)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void initRewardedVideo(Activity activity, String appKey, String userId) {
        this.mLoggerManager.log(IronSourceTag.NATIVE, this.TAG + ":initRewardedVideo(appKey: " + appKey + ", userId: " + userId + ")", 1);
        this.mAppKey = appKey;
        this.mUserId = userId;
        this.mActivity = activity;
        this.mServerResponseWrapper = IronSourceObject.getInstance().getCurrentServerResponse();
        if (this.mServerResponseWrapper != null) {
            int numOfAdaptersToLoad = this.mServerResponseWrapper.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoAdaptersSmartLoadAmount();
            for (int i = 0; i < numOfAdaptersToLoad && loadNextAdapter() != null; i++) {
            }
        }
    }

    private synchronized void reportShowFail(IronSourceError error) {
        this.mListenersWrapper.onRewardedVideoAdShowFailed(error);
    }

    private synchronized AbstractAdapter startAdapter(String providerName) {
        return startAdapter(providerName, true);
    }

    private synchronized AbstractAdapter startAdapter(String providerName, boolean regularOrder) {
        AbstractAdapter abstractAdapter;
        if (TextUtils.isEmpty(providerName)) {
            abstractAdapter = null;
        } else {
            ProviderSettings providerSettings = this.mServerResponseWrapper.getProviderSettingsHolder().getProviderSettings(providerName);
            if (providerSettings == null) {
                abstractAdapter = null;
            } else {
                String providerNameForReflection = providerSettings.getProviderTypeForReflection();
                String requestUrl = providerSettings.getRewardedVideoSettings().optString(IronSourceConstants.REQUEST_URL);
                this.mLoggerManager.log(IronSourceTag.NATIVE, this.TAG + ":startAdapter(" + providerName + ")", 1);
                if (providerName.isEmpty()) {
                    abstractAdapter = null;
                } else {
                    try {
                        IronSourceObject sso = IronSourceObject.getInstance();
                        abstractAdapter = sso.getExistingAdapter(providerName);
                        if (abstractAdapter == null) {
                            Class<?> mAdapterClass = Class.forName("com.ironsource.adapters." + providerNameForReflection.toLowerCase() + "." + providerNameForReflection + "Adapter");
                            abstractAdapter = (AbstractAdapter) mAdapterClass.getMethod(IronSourceConstants.START_ADAPTER, new Class[]{String.class, String.class}).invoke(mAdapterClass, new Object[]{providerName, requestUrl});
                            if (abstractAdapter != null) {
                                sso.addToAdaptersList(abstractAdapter);
                            }
                        }
                        if (abstractAdapter.getMaxRVAdsPerIteration() < 1) {
                            abstractAdapter = null;
                        } else {
                            setCustomParams(abstractAdapter);
                            abstractAdapter.setLogListener(this.mLoggerManager);
                            abstractAdapter.setRewardedVideoTimeout(this.mServerResponseWrapper.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoAdaptersSmartLoadTimeout());
                            if (regularOrder) {
                                abstractAdapter.setRewardedVideoPriority(this.mServerResponseWrapper.getRVAdaptersLoadPosition());
                            }
                            abstractAdapter.setRewardedVideoConfigurations(this.mServerResponseWrapper.getConfigurations().getRewardedVideoConfigurations());
                            if (!TextUtils.isEmpty(ConfigFile.getConfigFile().getPluginType())) {
                                abstractAdapter.setPluginData(ConfigFile.getConfigFile().getPluginType(), ConfigFile.getConfigFile().getPluginFrameworkVersion());
                            }
                            abstractAdapter.setRewardedVideoListener(this);
                            if (regularOrder) {
                                this.mLoggerManager.log(IronSourceTag.NATIVE, this.TAG + ": startAdapter(" + providerName + ") moved to 'Initiated' list", 0);
                                addInitiatedRewardedVideoAdapter(abstractAdapter);
                            }
                            abstractAdapter.initRewardedVideo(this.mActivity, sso.getIronSourceAppKey(), this.mUserId);
                        }
                    } catch (Throwable e) {
                        this.mLoggerManager.logException(IronSourceTag.API, this.TAG + ":startAdapter(" + providerName + ")", e);
                        if (regularOrder) {
                            this.mServerResponseWrapper.decreaseMaxRVAdapters();
                            if (shouldNotifyAvailabilityChanged(false)) {
                                this.mListenersWrapper.onRewardedVideoAvailabilityChanged(false);
                            }
                        }
                        this.mLoggerManager.log(IronSourceTag.API, ErrorBuilder.buildInitFailedError(providerName + " initialization failed - please verify that required dependencies are in you build path.", IronSourceConstants.REWARDED_VIDEO_AD_UNIT).toString(), 2);
                        abstractAdapter = null;
                    }
                }
            }
        }
        return abstractAdapter;
    }

    public void onResume(Activity activity) {
        if (activity != null) {
            this.mActivity = activity;
        }
    }

    public void onPause(Activity activity) {
    }

    public void setAge(int age) {
    }

    public void setGender(String gender) {
    }

    public void setMediationSegment(String segment) {
    }

    public void showRewardedVideo() {
    }

    public synchronized void showRewardedVideo(String placementName) {
        if (IronSourceUtils.isNetworkConnected(this.mActivity)) {
            sendShowCheckAvailabilityEvents(placementName);
            if (this.mAvailableAdapters.size() > 0) {
                Iterator it = new ArrayList(this.mAvailableAdapters).iterator();
                while (it.hasNext()) {
                    AbstractAdapter adapter = (AbstractAdapter) it.next();
                    if (!adapter.isRewardedVideoAvailable()) {
                        onRewardedVideoAvailabilityChanged(false, adapter);
                        this.mLoggerManager.logException(IronSourceTag.INTERNAL, adapter.getProviderName() + " Failed to show video", new Exception("FailedToShowVideoException"));
                    } else if (showRVAdapter(placementName, adapter)) {
                        if (!isPremiumAdapter(adapter.getProviderName())) {
                            disablePremiumForCurrentSession();
                        }
                        adapter.increaseNumberOfVideosPlayed();
                        this.mLoggerManager.log(IronSourceTag.INTERNAL, adapter.getProviderName() + ": " + adapter.getNumberOfVideosPlayed() + "/" + adapter.getMaxRVAdsPerIteration() + " videos played", 0);
                        if (adapter.getNumberOfVideosPlayed() == adapter.getMaxRVAdsPerIteration()) {
                            completeAdapterIteration(adapter);
                        }
                        completeIterationRound();
                    }
                }
            } else if (isBackFillAvailable()) {
                showRVAdapter(placementName, this.mBackFillAdapter);
            }
        } else {
            reportShowFail(ErrorBuilder.buildNoInternetConnectionShowFailError(IronSourceConstants.REWARDED_VIDEO_AD_UNIT));
        }
    }

    private synchronized boolean showRVAdapter(String placementName, AbstractAdapter adapter) {
        boolean z = true;
        synchronized (this) {
            if (TextUtils.isEmpty(placementName) || adapter == null) {
                z = false;
            } else {
                CappingManager.incrementShowCounter(this.mActivity, getPlacementByName(placementName));
                if (this.mServerResponseWrapper.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoEventsConfigurations().isUltraEventsEnabled()) {
                    Placement placement = this.mServerResponseWrapper.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoPlacement(placementName);
                    reportImpression(adapter.getUrl(), true, placement.getPlacementId());
                    reportFalseImpressionsOnHigherPriority(adapter.getRewardedVideoPriority(), placement.getPlacementId());
                }
                JSONObject data = IronSourceUtils.getProviderAdditionalData(adapter);
                try {
                    data.put("placement", placementName);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RewardedVideoEventsManager.getInstance().log(new EventData(2, data));
                adapter.showRewardedVideo(placementName);
            }
        }
        return z;
    }

    private void sendShowCheckAvailabilityEvents(String placementName) {
        Iterator it = this.mAvailableAdapters.iterator();
        while (it.hasNext()) {
            createAndSendShowCheckAvailabilityEvent((AbstractAdapter) it.next(), placementName, true);
        }
        it = this.mNotAvailableAdapters.iterator();
        while (it.hasNext()) {
            AbstractAdapter adapter = (AbstractAdapter) it.next();
            if (!isPremiumAdapter(adapter.getProviderName()) || canShowPremium()) {
                createAndSendShowCheckAvailabilityEvent(adapter, placementName, false);
            }
        }
        if (this.mBackFillAdapter != null) {
            createAndSendShowCheckAvailabilityEvent(this.mBackFillAdapter, placementName, isBackFillAvailable());
        }
    }

    private void createAndSendShowCheckAvailabilityEvent(AbstractAdapter adapter, String placementName, boolean status) {
        JSONObject data = IronSourceUtils.getProviderAdditionalData(adapter);
        try {
            data.put("placement", placementName);
            data.put(ParametersKeys.VIDEO_STATUS, status ? ServerProtocol.DIALOG_RETURN_SCOPES_TRUE : "false");
            data.put("providerPriority", adapter.getRewardedVideoPriority());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RewardedVideoEventsManager.getInstance().log(new EventData(19, data));
    }

    public synchronized boolean isRewardedVideoAvailable() {
        boolean z = false;
        synchronized (this) {
            if (!this.mPauseSmartLoadDueToNetworkUnavailability) {
                Iterator it = new ArrayList(this.mAvailableAdapters).iterator();
                while (it.hasNext()) {
                    AbstractAdapter adapter = (AbstractAdapter) it.next();
                    if (adapter.isRewardedVideoAvailable()) {
                        z = true;
                        break;
                    }
                    onRewardedVideoAvailabilityChanged(false, adapter);
                }
            }
        }
        return z;
    }

    public void setRewardedVideoListener(RewardedVideoListener listener) {
        this.mListenersWrapper = listener;
    }

    public void onRewardedVideoAdShowFailed(IronSourceError error, AbstractAdapter adapter) {
        this.mLoggerManager.log(IronSourceTag.ADAPTER_CALLBACK, adapter.getProviderName() + ":onRewardedVideoAdShowFailed(" + error + ")", 1);
        this.mListenersWrapper.onRewardedVideoAdShowFailed(error);
    }

    private AbstractAdapter loadNextAdapter() {
        AbstractAdapter initiatedAdapter = null;
        if (this.mAvailableAdapters.size() + this.mInitiatedAdapters.size() < this.mServerResponseWrapper.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoAdaptersSmartLoadAmount()) {
            while (this.mServerResponseWrapper.hasMoreRVProvidersToLoad() && initiatedAdapter == null) {
                initiatedAdapter = startAdapter(this.mServerResponseWrapper.getNextRVProvider());
            }
        }
        return initiatedAdapter;
    }

    public void onRewardedVideoAdOpened(AbstractAdapter adapter) {
        this.mLoggerManager.log(IronSourceTag.ADAPTER_CALLBACK, adapter.getProviderName() + ":onRewardedVideoAdOpened()", 1);
        RewardedVideoEventsManager.getInstance().log(new EventData(5, IronSourceUtils.getProviderAdditionalData(adapter)));
        this.mListenersWrapper.onRewardedVideoAdOpened();
    }

    public void onRewardedVideoAdClosed(AbstractAdapter adapter) {
        this.mLoggerManager.log(IronSourceTag.ADAPTER_CALLBACK, adapter.getProviderName() + ":onRewardedVideoAdClosed()", 1);
        RewardedVideoEventsManager.getInstance().log(new EventData(6, IronSourceUtils.getProviderAdditionalData(adapter)));
        this.mListenersWrapper.onRewardedVideoAdClosed();
        notifyIsAdAvailableForStatistics();
    }

    public synchronized void onRewardedVideoAvailabilityChanged(boolean available, AbstractAdapter adapter) {
        if (!this.mPauseSmartLoadDueToNetworkUnavailability) {
            JSONObject data;
            try {
                data = IronSourceUtils.getProviderAdditionalData(adapter);
                data.put(ParametersKeys.VIDEO_STATUS, String.valueOf(available));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Throwable e2) {
                this.mLoggerManager.logException(IronSourceTag.ADAPTER_CALLBACK, "onRewardedVideoAvailabilityChanged(available:" + available + ", " + "provider:" + adapter.getProviderName() + ")", e2);
            }
            RewardedVideoEventsManager.getInstance().log(new EventData(7, data));
            this.mLoggerManager.log(IronSourceTag.ADAPTER_CALLBACK, adapter.getProviderName() + ":onRewardedVideoAvailabilityChanged(available:" + available + ")", 1);
            if (isPremiumAdapter(adapter.getProviderName())) {
                this.mLoggerManager.log(IronSourceTag.ADAPTER_CALLBACK, adapter.getProviderName() + " is a Premium adapter, canShowPremium: " + canShowPremium(), 1);
            }
            if (isBackFillAdapter(adapter)) {
                if (shouldNotifyAvailabilityChanged(available)) {
                    this.mListenersWrapper.onRewardedVideoAvailabilityChanged(this.mIsAdAvailable);
                }
            } else if (isPremiumAdapter(adapter.getProviderName()) && !canShowPremium()) {
                addUnavailableRewardedVideoAdapter(adapter);
                if (shouldNotifyAvailabilityChanged(false)) {
                    this.mListenersWrapper.onRewardedVideoAvailabilityChanged(this.mIsAdAvailable);
                }
            } else if (!this.mExhaustedAdapters.contains(adapter)) {
                if (available) {
                    this.mLoggerManager.log(IronSourceTag.NATIVE, "Smart Loading - " + adapter.getProviderName() + " moved to 'Available' list", 0);
                    addAvailableRewardedVideoAdapter(adapter, false);
                    if (shouldNotifyAvailabilityChanged(available)) {
                        this.mListenersWrapper.onRewardedVideoAvailabilityChanged(this.mIsAdAvailable);
                    }
                } else {
                    this.mLoggerManager.log(IronSourceTag.NATIVE, "Smart Loading - " + adapter.getProviderName() + " moved to 'Not Available' list", 0);
                    addUnavailableRewardedVideoAdapter(adapter);
                    if (shouldNotifyAvailabilityChanged(available)) {
                        if (this.mBackFillAdapter == null && !this.mBackFillInitStarted) {
                            String backFillAdapterName = this.mServerResponseWrapper.getRVBackFillProvider();
                            if (!TextUtils.isEmpty(backFillAdapterName)) {
                                this.mBackFillInitStarted = true;
                                this.mBackFillAdapter = startAdapter(backFillAdapterName, false);
                            }
                            if (this.mBackFillAdapter == null) {
                                this.mListenersWrapper.onRewardedVideoAvailabilityChanged(this.mIsAdAvailable);
                            }
                        } else if (!isBackFillAvailable()) {
                            this.mListenersWrapper.onRewardedVideoAvailabilityChanged(this.mIsAdAvailable);
                        } else if (shouldNotifyAvailabilityChanged(true)) {
                            this.mListenersWrapper.onRewardedVideoAvailabilityChanged(this.mIsAdAvailable);
                        }
                    }
                    loadNextAdapter();
                    completeIterationRound();
                }
            }
        }
    }

    private synchronized void completeAdapterIteration(AbstractAdapter adapter) {
        try {
            this.mLoggerManager.log(IronSourceTag.ADAPTER_CALLBACK, adapter.getProviderName() + ":completeIteration", 1);
            this.mLoggerManager.log(IronSourceTag.NATIVE, "Smart Loading - " + adapter.getProviderName() + " moved to 'Exhausted' list", 0);
            addExhaustedRewardedVideoAdapter(adapter);
            loadNextAdapter();
            adapter.resetNumberOfVideosPlayed();
        } catch (Throwable e) {
            this.mLoggerManager.logException(IronSourceTag.ADAPTER_CALLBACK, "completeIteration(provider:" + adapter.getProviderName() + ")", e);
        }
    }

    private synchronized boolean isIterationRoundComplete() {
        boolean z;
        z = this.mInitiatedAdapters.size() == 0 && this.mAvailableAdapters.size() == 0 && this.mExhaustedAdapters.size() > 0;
        return z;
    }

    private synchronized void completeIterationRound() {
        if (isIterationRoundComplete()) {
            this.mLoggerManager.log(IronSourceTag.INTERNAL, "Reset Iteration", 0);
            boolean isAvailable = false;
            Iterator it = ((ArrayList) this.mExhaustedAdapters.clone()).iterator();
            while (it.hasNext()) {
                AbstractAdapter exhaustedAdapter = (AbstractAdapter) it.next();
                if (exhaustedAdapter.isRewardedVideoAvailable()) {
                    this.mLoggerManager.log(IronSourceTag.INTERNAL, exhaustedAdapter.getProviderName() + ": " + "moved to 'Available'", 0);
                    addAvailableRewardedVideoAdapter(exhaustedAdapter, true);
                    isAvailable = true;
                } else {
                    this.mLoggerManager.log(IronSourceTag.INTERNAL, exhaustedAdapter.getProviderName() + ": " + "moved to 'Not Available'", 0);
                    addUnavailableRewardedVideoAdapter(exhaustedAdapter);
                }
            }
            this.mLoggerManager.log(IronSourceTag.INTERNAL, "End of Reset Iteration", 0);
            if (shouldNotifyAvailabilityChanged(isAvailable)) {
                this.mListenersWrapper.onRewardedVideoAvailabilityChanged(this.mIsAdAvailable);
            }
        }
    }

    private synchronized boolean shouldNotifyAvailabilityChanged(boolean adapterAvailability) {
        boolean shouldNotify;
        shouldNotify = false;
        if (!this.mIsAdAvailable && adapterAvailability && (this.mAvailableAdapters.size() > 0 || isBackFillAvailable())) {
            this.mIsAdAvailable = true;
            shouldNotify = true;
        } else if (this.mIsAdAvailable && !adapterAvailability && this.mAvailableAdapters.size() <= 0 && !isBackFillAvailable()) {
            this.mIsAdAvailable = false;
            shouldNotify = true;
        } else if (!(adapterAvailability || this.mNotAvailableAdapters.size() < this.mServerResponseWrapper.getMaxRVAdapters() || isBackFillAvailable())) {
            this.mIsAdAvailable = false;
            shouldNotify = !this.mDidReportInitialAvailability;
        }
        return shouldNotify;
    }

    private synchronized void addToAvailable(AbstractAdapter adapter, boolean forceOrder) {
        String adapterAlgorithm = this.mServerResponseWrapper.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoAdapterAlgorithm();
        int priorityLocation = this.mAvailableAdapters.size();
        if (!this.mAvailableAdapters.contains(adapter)) {
            if ("KTO".equalsIgnoreCase(adapterAlgorithm) || forceOrder) {
                Iterator it = this.mAvailableAdapters.iterator();
                while (it.hasNext()) {
                    AbstractAdapter rwa = (AbstractAdapter) it.next();
                    if (adapter.getRewardedVideoPriority() <= rwa.getRewardedVideoPriority()) {
                        priorityLocation = this.mAvailableAdapters.indexOf(rwa);
                        break;
                    }
                }
            }
            this.mAvailableAdapters.add(priorityLocation, adapter);
        }
    }

    private synchronized void removeFromAvailable(AbstractAdapter adapter) {
        if (this.mAvailableAdapters.contains(adapter)) {
            this.mAvailableAdapters.remove(adapter);
        }
    }

    private synchronized void addToNotAvailable(AbstractAdapter adapter) {
        if (!this.mNotAvailableAdapters.contains(adapter)) {
            this.mNotAvailableAdapters.add(adapter);
        }
    }

    private synchronized void removeFromUnavailable(AbstractAdapter adapter) {
        if (this.mNotAvailableAdapters.contains(adapter)) {
            this.mNotAvailableAdapters.remove(adapter);
        }
    }

    private synchronized void addToInitiated(AbstractAdapter adapter) {
        if (!this.mInitiatedAdapters.contains(adapter)) {
            this.mInitiatedAdapters.add(adapter);
        }
    }

    private synchronized void removeFromInitiated(AbstractAdapter adapter) {
        if (this.mInitiatedAdapters.contains(adapter)) {
            this.mInitiatedAdapters.remove(adapter);
        }
    }

    private synchronized void addToExhausted(AbstractAdapter adapter) {
        if (!this.mExhaustedAdapters.contains(adapter)) {
            this.mExhaustedAdapters.add(adapter);
        }
    }

    private synchronized void removeFromExhausted(AbstractAdapter adapter) {
        if (this.mExhaustedAdapters.contains(adapter)) {
            this.mExhaustedAdapters.remove(adapter);
        }
    }

    public synchronized void addAvailableRewardedVideoAdapter(AbstractAdapter adapter, boolean forceOrder) {
        addToAvailable(adapter, forceOrder);
        removeFromInitiated(adapter);
        removeFromUnavailable(adapter);
        removeFromExhausted(adapter);
    }

    private synchronized void addInitiatedRewardedVideoAdapter(AbstractAdapter adapter) {
        addToInitiated(adapter);
        removeFromUnavailable(adapter);
        removeFromAvailable(adapter);
        removeFromExhausted(adapter);
    }

    private synchronized void addUnavailableRewardedVideoAdapter(AbstractAdapter adapter) {
        addToNotAvailable(adapter);
        removeFromAvailable(adapter);
        removeFromInitiated(adapter);
        removeFromExhausted(adapter);
    }

    private synchronized void addExhaustedRewardedVideoAdapter(AbstractAdapter adapter) {
        addToExhausted(adapter);
        removeFromAvailable(adapter);
        removeFromInitiated(adapter);
        removeFromUnavailable(adapter);
    }

    public void onRewardedVideoAdStarted(AbstractAdapter adapter) {
        this.mLoggerManager.log(IronSourceTag.ADAPTER_CALLBACK, adapter.getProviderName() + ":onRewardedVideoAdStarted()", 1);
        RewardedVideoEventsManager.getInstance().log(new EventData(8, IronSourceUtils.getProviderAdditionalData(adapter)));
        this.mListenersWrapper.onRewardedVideoAdStarted();
    }

    public void onRewardedVideoAdEnded(AbstractAdapter adapter) {
        this.mLoggerManager.log(IronSourceTag.ADAPTER_CALLBACK, adapter.getProviderName() + ":onRewardedVideoAdEnded()", 1);
        RewardedVideoEventsManager.getInstance().log(new EventData(9, IronSourceUtils.getProviderAdditionalData(adapter)));
        this.mListenersWrapper.onRewardedVideoAdEnded();
    }

    public void onRewardedVideoAdRewarded(Placement placement, AbstractAdapter adapter) {
        this.mLoggerManager.log(IronSourceTag.ADAPTER_CALLBACK, adapter.getProviderName() + ":onRewardedVideoAdRewarded(" + placement + ")", 1);
        if (placement == null) {
            placement = this.mServerResponseWrapper.getConfigurations().getRewardedVideoConfigurations().getDefaultRewardedVideoPlacement();
        }
        JSONObject data = IronSourceUtils.getProviderAdditionalData(adapter);
        try {
            data.put("placement", placement.getPlacementName());
            data.put("rewardName", placement.getRewardName());
            data.put("rewardAmount", placement.getRewardAmount());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        EventData event = new EventData(10, data);
        if (!TextUtils.isEmpty(this.mAppKey)) {
            event.addToAdditionalData("transId", IronSourceUtils.getTransId(BuildConfig.FLAVOR + Long.toString(event.getTimeStamp()) + this.mAppKey + adapter.getProviderName()));
            if (!TextUtils.isEmpty(IronSourceObject.getInstance().getDynamicUserId())) {
                event.addToAdditionalData("dynamicUserId", IronSourceObject.getInstance().getDynamicUserId());
            }
        }
        RewardedVideoEventsManager.getInstance().log(event);
        this.mListenersWrapper.onRewardedVideoAdRewarded(placement);
    }

    private synchronized void notifyIsAdAvailableForStatistics() {
        boolean mediationStatus = false;
        if (this.mAvailableAdapters != null && this.mAvailableAdapters.size() > 0) {
            mediationStatus = true;
        }
        JSONObject data = IronSourceUtils.getMediationAdditionalData();
        try {
            data.put(ParametersKeys.VIDEO_STATUS, String.valueOf(mediationStatus));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RewardedVideoEventsManager.getInstance().log(new EventData(3, data));
        Iterator it = new ArrayList(this.mAvailableAdapters).iterator();
        while (it.hasNext()) {
            JSONObject availableData = IronSourceUtils.getProviderAdditionalData((AbstractAdapter) it.next());
            try {
                availableData.put(ParametersKeys.VIDEO_STATUS, ServerProtocol.DIALOG_RETURN_SCOPES_TRUE);
            } catch (JSONException e2) {
                e2.printStackTrace();
            }
            RewardedVideoEventsManager.getInstance().log(new EventData(3, availableData));
        }
        it = this.mNotAvailableAdapters.iterator();
        while (it.hasNext()) {
            AbstractAdapter notavailableAdapter = (AbstractAdapter) it.next();
            if (!isPremiumAdapter(notavailableAdapter.getProviderName()) || canShowPremium()) {
                JSONObject notAvailableData = IronSourceUtils.getProviderAdditionalData(notavailableAdapter);
                try {
                    notAvailableData.put(ParametersKeys.VIDEO_STATUS, "false");
                } catch (JSONException e22) {
                    e22.printStackTrace();
                }
                RewardedVideoEventsManager.getInstance().log(new EventData(3, notAvailableData));
            }
        }
        it = this.mInitiatedAdapters.iterator();
        while (it.hasNext()) {
            JSONObject initiatedData = IronSourceUtils.getProviderAdditionalData((AbstractAdapter) it.next());
            try {
                initiatedData.put(ParametersKeys.VIDEO_STATUS, "false");
            } catch (JSONException e222) {
                e222.printStackTrace();
            }
            RewardedVideoEventsManager.getInstance().log(new EventData(3, initiatedData));
        }
        if (this.mBackFillAdapter != null) {
            JSONObject backFillData = IronSourceUtils.getProviderAdditionalData(this.mBackFillAdapter);
            try {
                backFillData.put(ParametersKeys.VIDEO_STATUS, isBackFillAvailable() ? ServerProtocol.DIALOG_RETURN_SCOPES_TRUE : "false");
            } catch (JSONException e2222) {
                e2222.printStackTrace();
            }
            RewardedVideoEventsManager.getInstance().log(new EventData(3, backFillData));
        }
    }

    void shouldTrackNetworkState(Context context, boolean track) {
        this.mLoggerManager.log(IronSourceTag.INTERNAL, this.TAG + " Should Track Network State: " + track, 0);
        this.mShouldTrackNetworkState = track;
        if (this.mShouldTrackNetworkState) {
            if (this.mNetworkStateReceiver == null) {
                this.mNetworkStateReceiver = new NetworkStateReceiver(context, this);
            }
            context.registerReceiver(this.mNetworkStateReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        } else if (this.mNetworkStateReceiver != null) {
            context.unregisterReceiver(this.mNetworkStateReceiver);
        }
    }

    public void onNetworkAvailabilityChanged(boolean connected) {
        boolean z = false;
        if (this.mShouldTrackNetworkState) {
            this.mLoggerManager.log(IronSourceTag.INTERNAL, "Network Availability Changed To: " + connected, 0);
            if (shouldNotifyNetworkAvailabilityChanged(connected)) {
                if (!connected) {
                    z = true;
                }
                this.mPauseSmartLoadDueToNetworkUnavailability = z;
                this.mListenersWrapper.onRewardedVideoAvailabilityChanged(connected);
            }
        }
    }

    private boolean shouldNotifyNetworkAvailabilityChanged(boolean networkState) {
        if (!this.mIsAdAvailable && networkState && this.mAvailableAdapters.size() > 0) {
            this.mIsAdAvailable = true;
            return true;
        } else if (!this.mIsAdAvailable || networkState) {
            return false;
        } else {
            this.mIsAdAvailable = false;
            return true;
        }
    }

    public boolean isRewardedVideoPlacementCapped(String placementName) {
        return false;
    }

    public Placement getPlacementByName(String placementName) {
        if (this.mServerResponseWrapper == null || this.mServerResponseWrapper.getConfigurations() == null || this.mServerResponseWrapper.getConfigurations().getRewardedVideoConfigurations() == null) {
            return null;
        }
        try {
            Placement placement = this.mServerResponseWrapper.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoPlacement(placementName);
            if (placement != null) {
                return placement;
            }
            placement = this.mServerResponseWrapper.getConfigurations().getRewardedVideoConfigurations().getDefaultRewardedVideoPlacement();
            if (placement != null) {
                return placement;
            }
            this.mLoggerManager.log(IronSourceTag.API, "Default placement was not found", 3);
            return placement;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected synchronized boolean isBackFillAvailable() {
        boolean isRewardedVideoAvailable;
        if (this.mBackFillAdapter != null) {
            isRewardedVideoAvailable = this.mBackFillAdapter.isRewardedVideoAvailable();
        } else {
            isRewardedVideoAvailable = false;
        }
        return isRewardedVideoAvailable;
    }

    boolean isPremiumAdapter(String providerName) {
        String premiumAdapterName = this.mServerResponseWrapper.getRVPremiumProvider();
        if (TextUtils.isEmpty(premiumAdapterName) || TextUtils.isEmpty(providerName)) {
            return false;
        }
        return providerName.equals(premiumAdapterName);
    }

    protected synchronized void disablePremiumForCurrentSession() {
        super.disablePremiumForCurrentSession();
        Iterator it = new ArrayList(this.mAvailableAdapters).iterator();
        while (it.hasNext()) {
            AbstractAdapter adapter = (AbstractAdapter) it.next();
            if (isPremiumAdapter(adapter.getProviderName())) {
                moveAdapterToUnavailableAndLoadNext(adapter);
                break;
            }
        }
        it = new ArrayList(this.mExhaustedAdapters).iterator();
        while (it.hasNext()) {
            adapter = (AbstractAdapter) it.next();
            if (isPremiumAdapter(adapter.getProviderName())) {
                moveAdapterToUnavailableAndLoadNext(adapter);
                break;
            }
        }
    }

    private synchronized void moveAdapterToUnavailableAndLoadNext(AbstractAdapter adapter) {
        addUnavailableRewardedVideoAdapter(adapter);
        this.mLoggerManager.log(IronSourceTag.NATIVE, "Smart Loading - " + adapter.getProviderName() + " moved to 'Unavailable' list", 0);
        loadNextAdapter();
    }
}
