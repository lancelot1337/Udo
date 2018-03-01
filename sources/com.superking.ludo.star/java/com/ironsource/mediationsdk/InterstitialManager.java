package com.ironsource.mediationsdk;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import com.facebook.internal.ServerProtocol;
import com.ironsource.eventsmodule.EventData;
import com.ironsource.mediationsdk.IronSource.AD_UNIT;
import com.ironsource.mediationsdk.MediationInitializer.EInitStatus;
import com.ironsource.mediationsdk.MediationInitializer.OnMediationInitializationListener;
import com.ironsource.mediationsdk.config.ConfigFile;
import com.ironsource.mediationsdk.events.InterstitialEventsManager;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.model.InterstitialPlacement;
import com.ironsource.mediationsdk.model.ProviderSettings;
import com.ironsource.mediationsdk.sdk.InterstitialApi;
import com.ironsource.mediationsdk.sdk.InterstitialListener;
import com.ironsource.mediationsdk.sdk.InterstitialManagerListener;
import com.ironsource.mediationsdk.sdk.RewardedInterstitialApi;
import com.ironsource.mediationsdk.sdk.RewardedInterstitialListener;
import com.ironsource.mediationsdk.sdk.RewardedInterstitialManagerListener;
import com.ironsource.mediationsdk.utils.CappingManager;
import com.ironsource.mediationsdk.utils.ErrorBuilder;
import com.ironsource.mediationsdk.utils.IronSourceConstants;
import com.ironsource.mediationsdk.utils.IronSourceUtils;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.cocos2dx.lib.BuildConfig;
import org.json.JSONException;
import org.json.JSONObject;

class InterstitialManager extends AbstractAdUnitManager implements OnMediationInitializationListener, InterstitialApi, InterstitialManagerListener, RewardedInterstitialApi, RewardedInterstitialManagerListener {
    private static final long LOAD_FAILED_COOLDOWN_IN_MILLIS = 15000;
    private final String TAG = getClass().getName();
    private boolean mDidCallLoad = false;
    private boolean mDidFinishToInitInterstitial;
    private ArrayList<AbstractAdapter> mExhaustedAdapters;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private ArrayList<AbstractAdapter> mInitiatedAdapters;
    private InterstitialListener mInterstitialListenersWrapper;
    private long mLastLoadFailTimestamp;
    private String mLastPlacementForShowFail = BuildConfig.FLAVOR;
    private ArrayList<AbstractAdapter> mLoadFailedAdapters;
    LoadFailedRunnable mLoadFailedRunnable;
    private boolean mLoadInProgress = false;
    private ArrayList<AbstractAdapter> mLoadingAdapters;
    private ArrayList<AbstractAdapter> mNotInitAdapters;
    private int mNumberOfAdaptersToLoad = 1;
    private ArrayList<AbstractAdapter> mReadyAdapters;
    private RewardedInterstitialListener mRewardedInterstitialListenerWrapper;

    private class LoadFailedRunnable implements Runnable {
        IronSourceError error;

        LoadFailedRunnable(IronSourceError error) {
            this.error = error;
        }

        public void run() {
            InterstitialManager.this.mLoggerManager.log(IronSourceTag.API, "Load Interstitial failed: " + this.error.getErrorMessage(), 1);
            InterstitialManager.this.mLastLoadFailTimestamp = System.currentTimeMillis();
            InterstitialManager.this.mInterstitialListenersWrapper.onInterstitialAdLoadFailed(this.error);
            InterstitialManager.this.resetLoadRound(true);
        }
    }

    public InterstitialManager() {
        prepareStateForInit();
    }

    private void prepareStateForInit() {
        this.mReadyAdapters = new ArrayList();
        this.mInitiatedAdapters = new ArrayList();
        this.mNotInitAdapters = new ArrayList();
        this.mExhaustedAdapters = new ArrayList();
        this.mLoadFailedAdapters = new ArrayList();
        this.mLoadingAdapters = new ArrayList();
        this.mHandlerThread = new HandlerThread("IronSourceInterstitialHandler");
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
        this.mLastLoadFailTimestamp = 0;
    }

    void shouldTrackNetworkState(Context context, boolean track) {
        this.mLoggerManager.log(IronSourceTag.INTERNAL, this.TAG + " Should Track Network State: " + track, 0);
        this.mShouldTrackNetworkState = track;
    }

    boolean isBackFillAvailable() {
        if (this.mBackFillAdapter != null) {
            return this.mBackFillAdapter.isInterstitialReady();
        }
        return false;
    }

    boolean isPremiumAdapter(String providerName) {
        String premiumAdapterName = this.mServerResponseWrapper.getISPremiumProvider();
        if (TextUtils.isEmpty(premiumAdapterName) || TextUtils.isEmpty(providerName)) {
            return false;
        }
        return providerName.equals(premiumAdapterName);
    }

    public synchronized void initInterstitial(Activity activity, String appKey, String userId) {
        removeScheduledLoadFailedCallback();
        this.mLoggerManager.log(IronSourceTag.NATIVE, this.TAG + ":initInterstitial(appKey: " + appKey + ", userId: " + userId + ")", 1);
        this.mAppKey = appKey;
        this.mUserId = userId;
        this.mActivity = activity;
        this.mServerResponseWrapper = IronSourceObject.getInstance().getCurrentServerResponse();
        if (this.mServerResponseWrapper != null) {
            int numOfAdaptersToLoad = this.mServerResponseWrapper.getConfigurations().getInterstitialConfigurations().getInterstitialAdaptersSmartLoadAmount();
            this.mNumberOfAdaptersToLoad = numOfAdaptersToLoad;
            for (int i = 0; i < numOfAdaptersToLoad && startNextAdapter() != null; i++) {
            }
        }
    }

    public synchronized void loadInterstitial() {
        try {
            if (this.mLoadInProgress) {
                this.mLoggerManager.log(IronSourceTag.API, "Load Interstitial is already in progress", 1);
            } else {
                resetLoadRound(true);
                this.mDidCallLoad = true;
                this.mLoadInProgress = true;
                InterstitialEventsManager.getInstance().log(new EventData(22, IronSourceUtils.getMediationAdditionalData()));
                EInitStatus currentInitStatus = MediationInitializer.getInstance().getCurrentInitStatus();
                String loadFailMsg = "Load Interstitial can't be called before the Interstitial ad unit initialization completed successfully";
                if (currentInitStatus == EInitStatus.INIT_FAILED || currentInitStatus == EInitStatus.NOT_INIT) {
                    sendOrScheduleLoadFailedCallback(ErrorBuilder.buildLoadFailedError(loadFailMsg), false);
                } else if (currentInitStatus == EInitStatus.INIT_IN_PROGRESS) {
                    sendOrScheduleLoadFailedCallback(ErrorBuilder.buildLoadFailedError(loadFailMsg), true);
                } else if (!IronSourceUtils.isNetworkConnected(this.mActivity)) {
                    sendOrScheduleLoadFailedCallback(ErrorBuilder.buildNoInternetConnectionLoadFailError(ParametersKeys.INTERSTITIAL), false);
                } else if (this.mServerResponseWrapper != null && this.mInitiatedAdapters.size() != 0) {
                    ArrayList<AbstractAdapter> tempInitiatedAdaptersList = (ArrayList) this.mInitiatedAdapters.clone();
                    int i = 0;
                    while (i < this.mNumberOfAdaptersToLoad && i < tempInitiatedAdaptersList.size()) {
                        addLoadingInterstitialAdapter((AbstractAdapter) tempInitiatedAdaptersList.get(i));
                        i++;
                    }
                    i = 0;
                    while (i < this.mNumberOfAdaptersToLoad && i < tempInitiatedAdaptersList.size()) {
                        loadAdapterAndSendEvent((AbstractAdapter) tempInitiatedAdaptersList.get(i));
                        i++;
                    }
                } else if (this.mServerResponseWrapper == null || this.mDidFinishToInitInterstitial) {
                    sendOrScheduleLoadFailedCallback(ErrorBuilder.buildGenericError("no ads to show"), false);
                }
            }
        } catch (Exception e) {
            sendOrScheduleLoadFailedCallback(ErrorBuilder.buildLoadFailedError("loadInterstitial exception"), false);
        }
    }

    private synchronized void sendOrScheduleLoadFailedCallback(IronSourceError error, boolean shouldWait) {
        removeScheduledLoadFailedCallback();
        this.mLoadFailedRunnable = new LoadFailedRunnable(error);
        long timeFromPreviousLoadFailed = shouldWait ? 0 : System.currentTimeMillis() - this.mLastLoadFailTimestamp;
        if (timeFromPreviousLoadFailed < LOAD_FAILED_COOLDOWN_IN_MILLIS) {
            long timeToNextLoadFailed = LOAD_FAILED_COOLDOWN_IN_MILLIS - timeFromPreviousLoadFailed;
            if (this.mHandler != null) {
                this.mHandler.postDelayed(this.mLoadFailedRunnable, timeToNextLoadFailed);
            }
        } else if (this.mHandler != null) {
            this.mHandler.post(this.mLoadFailedRunnable);
        }
    }

    private synchronized void removeScheduledLoadFailedCallback() {
        if (!(this.mHandler == null || this.mLoadFailedRunnable == null)) {
            this.mHandler.removeCallbacks(this.mLoadFailedRunnable);
        }
    }

    public void onInterstitialAdRewarded(AbstractAdapter adapter) {
        InterstitialEventsManager.getInstance().log(new EventData(IronSourceConstants.INTERSTITIAL_AD_REWARDED, IronSourceUtils.getProviderAdditionalData(adapter)));
        if (this.mRewardedInterstitialListenerWrapper != null) {
            this.mRewardedInterstitialListenerWrapper.onInterstitialAdRewarded();
        }
    }

    private synchronized void loadAdapterAndSendEvent(AbstractAdapter adapter) {
        InterstitialEventsManager.getInstance().log(new EventData(22, IronSourceUtils.getProviderAdditionalData(adapter)));
        adapter.loadInterstitial();
    }

    private synchronized void resetLoadRound(boolean moveAdaptersToInitiated) {
        if (moveAdaptersToInitiated) {
            moveAdaptersToInitiated();
        }
        this.mLoadInProgress = false;
        this.mDidCallLoad = false;
        if (this.mLoadFailedRunnable != null) {
            this.mHandler.removeCallbacks(this.mLoadFailedRunnable);
        }
    }

    private synchronized void moveAdaptersToInitiated() {
        Iterator it;
        if (this.mReadyAdapters.size() > 0) {
            it = ((ArrayList) this.mReadyAdapters.clone()).iterator();
            while (it.hasNext()) {
                AbstractAdapter adapter = (AbstractAdapter) it.next();
                this.mLoggerManager.log(IronSourceTag.NATIVE, "Smart Loading - " + adapter.getProviderName() + " moved to 'Initiated' list", 0);
                addInitiatedInterstitialAdapter(adapter);
            }
        }
        if (this.mLoadingAdapters.size() > 0) {
            it = ((ArrayList) this.mLoadingAdapters.clone()).iterator();
            while (it.hasNext()) {
                adapter = (AbstractAdapter) it.next();
                this.mLoggerManager.log(IronSourceTag.NATIVE, "Smart Loading - " + adapter.getProviderName() + " moved to 'Initiated' list", 0);
                addInitiatedInterstitialAdapter(adapter);
            }
        }
        if (this.mLoadFailedAdapters.size() > 0) {
            it = ((ArrayList) this.mLoadFailedAdapters.clone()).iterator();
            while (it.hasNext()) {
                adapter = (AbstractAdapter) it.next();
                this.mLoggerManager.log(IronSourceTag.NATIVE, "Smart Loading - " + adapter.getProviderName() + " moved to 'Initiated' list", 0);
                addInitiatedInterstitialAdapter(adapter);
            }
        }
    }

    public synchronized void showInterstitial() {
    }

    public void showInterstitial(String placementName) {
        if (this.mShouldTrackNetworkState && this.mActivity != null && !IronSourceUtils.isNetworkConnected(this.mActivity)) {
            this.mLoggerManager.log(IronSourceTag.API, this.TAG + ":showInterstitial fail - no internet connection", 2);
            this.mInterstitialListenersWrapper.onInterstitialAdShowFailed(ErrorBuilder.buildNoInternetConnectionShowFailError(ParametersKeys.INTERSTITIAL));
        } else if (!this.mDidCallLoad) {
            this.mInterstitialListenersWrapper.onInterstitialAdShowFailed(ErrorBuilder.buildShowFailedError(ParametersKeys.INTERSTITIAL, "showInterstitial failed - You need to load interstitial before showing it"));
        } else if (this.mReadyAdapters == null || this.mReadyAdapters.size() == 0) {
            this.mLoggerManager.log(IronSourceTag.API, this.TAG + ":No adapters to show", 2);
            this.mInterstitialListenersWrapper.onInterstitialAdShowFailed(ErrorBuilder.buildShowFailedError(ParametersKeys.INTERSTITIAL, "showInterstitial failed - No adapters ready to show"));
        } else {
            this.mLastPlacementForShowFail = placementName;
            AbstractAdapter adapter = (AbstractAdapter) this.mReadyAdapters.get(0);
            if (adapter == null) {
                this.mLoggerManager.log(IronSourceTag.API, this.TAG + ":No adapters to show", 2);
                this.mInterstitialListenersWrapper.onInterstitialAdShowFailed(ErrorBuilder.buildShowFailedError(ParametersKeys.INTERSTITIAL, "showInterstitial failed - No adapters ready to show"));
                return;
            }
            adapter.increaseNumberOfAdsPlayed();
            this.mLoggerManager.log(IronSourceTag.INTERNAL, adapter.getProviderName() + ": " + adapter.getNumberOfAdsPlayed() + "/" + adapter.getMaxISAdsPerIteration() + " ads played", 0);
            JSONObject data = IronSourceUtils.getProviderAdditionalData(adapter);
            try {
                data.put("placement", placementName);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            InterstitialEventsManager.getInstance().log(new EventData(23, data));
            adapter.showInterstitial(placementName);
            CappingManager.incrementShowCounter(this.mActivity, getPlacementByName(placementName));
            resetLoadRound(false);
        }
    }

    public void setInterstitialListener(InterstitialListener listener) {
        this.mInterstitialListenersWrapper = listener;
    }

    public void setRewardedInterstitialListener(RewardedInterstitialListener listener) {
        this.mRewardedInterstitialListenerWrapper = listener;
    }

    private boolean isIterationRoundComplete() {
        return allAdaptersAreInTheLoop() && this.mReadyAdapters.size() == 0 && this.mInitiatedAdapters.size() == 0 && this.mLoadingAdapters.size() == 0;
    }

    private boolean allAdaptersAreInTheLoop() {
        return ((((this.mNotInitAdapters.size() + this.mExhaustedAdapters.size()) + this.mInitiatedAdapters.size()) + this.mLoadingAdapters.size()) + this.mLoadFailedAdapters.size()) + this.mReadyAdapters.size() == this.mServerResponseWrapper.getMaxISAdapters();
    }

    private void completeIterationRound() {
        this.mLoggerManager.log(IronSourceTag.INTERNAL, "Reset Iteration", 0);
        Iterator it = ((ArrayList) this.mExhaustedAdapters.clone()).iterator();
        while (it.hasNext()) {
            AbstractAdapter exhaustedAdapter = (AbstractAdapter) it.next();
            this.mLoggerManager.log(IronSourceTag.INTERNAL, exhaustedAdapter.getProviderName() + ": " + "moved to 'Initiated' list", 0);
            addInitiatedInterstitialAdapter(exhaustedAdapter);
            exhaustedAdapter.resetNumberOfAdsPlayed();
        }
        this.mLoggerManager.log(IronSourceTag.INTERNAL, "End of Reset Iteration", 0);
    }

    private void completeAdapterIteration(AbstractAdapter adapter) {
        try {
            this.mLoggerManager.log(IronSourceTag.ADAPTER_CALLBACK, adapter.getProviderName() + ":completeIteration", 1);
            this.mLoggerManager.log(IronSourceTag.NATIVE, "Smart Loading - " + adapter.getProviderName() + " moved to 'Exhausted' list", 0);
            addExhaustedInterstitialAdapter(adapter);
            if (this.mInitiatedAdapters.size() + this.mReadyAdapters.size() < this.mNumberOfAdaptersToLoad) {
                startNextAdapter();
            }
            adapter.resetNumberOfAdsPlayed();
        } catch (Throwable e) {
            this.mLoggerManager.logException(IronSourceTag.ADAPTER_CALLBACK, "completeIteration(provider:" + adapter.getProviderName() + ")", e);
        }
    }

    private void completeAdapterShow(AbstractAdapter adapter) {
        if (adapter.getNumberOfAdsPlayed() == adapter.getMaxISAdsPerIteration()) {
            completeAdapterIteration(adapter);
            if (isIterationRoundComplete()) {
                completeIterationRound();
                return;
            }
            return;
        }
        this.mLoggerManager.log(IronSourceTag.NATIVE, "Smart Loading - " + adapter.getProviderName() + " moved to 'Initiated' list", 0);
        addInitiatedInterstitialAdapter(adapter);
    }

    private AbstractAdapter startNextAdapter() {
        AbstractAdapter initiatedAdapter = null;
        while (this.mServerResponseWrapper.hasMoreISProvidersToLoad() && initiatedAdapter == null) {
            initiatedAdapter = startAdapter(this.mServerResponseWrapper.getNextISProvider());
        }
        return initiatedAdapter;
    }

    private AbstractAdapter startAdapter(String providerName) {
        if (TextUtils.isEmpty(providerName)) {
            return null;
        }
        ProviderSettings providerSettings = this.mServerResponseWrapper.getProviderSettingsHolder().getProviderSettings(providerName);
        if (providerSettings == null) {
            return null;
        }
        String requestUrl = BuildConfig.FLAVOR;
        if (providerSettings.getRewardedVideoSettings() != null) {
            requestUrl = providerSettings.getRewardedVideoSettings().optString(IronSourceConstants.REQUEST_URL);
        }
        this.mLoggerManager.log(IronSourceTag.NATIVE, this.TAG + ":startAdapter(" + providerName + ")", 1);
        try {
            IronSourceObject sso = IronSourceObject.getInstance();
            AbstractAdapter providerAdapter = sso.getExistingAdapter(providerName);
            if (providerAdapter == null) {
                Class<?> mAdapterClass = Class.forName("com.ironsource.adapters." + providerName.toLowerCase() + "." + providerName + "Adapter");
                providerAdapter = (AbstractAdapter) mAdapterClass.getMethod(IronSourceConstants.START_ADAPTER, new Class[]{String.class, String.class}).invoke(mAdapterClass, new Object[]{providerName, requestUrl});
                if (providerAdapter != null) {
                    sso.addToISAdaptersList(providerAdapter);
                }
            }
            if (providerAdapter.getMaxISAdsPerIteration() < 1) {
                return null;
            }
            setCustomParams(providerAdapter);
            providerAdapter.setLogListener(this.mLoggerManager);
            providerAdapter.setInterstitialTimeout(this.mServerResponseWrapper.getConfigurations().getInterstitialConfigurations().getInterstitialAdaptersSmartLoadTimeout());
            providerAdapter.setInterstitialPriority(this.mServerResponseWrapper.getISAdaptersLoadPosition());
            providerAdapter.setInterstitialConfigurations(this.mServerResponseWrapper.getConfigurations().getInterstitialConfigurations());
            if (!TextUtils.isEmpty(ConfigFile.getConfigFile().getPluginType())) {
                providerAdapter.setPluginData(ConfigFile.getConfigFile().getPluginType(), ConfigFile.getConfigFile().getPluginFrameworkVersion());
            }
            providerAdapter.setInterstitialListener(this);
            if (this.mRewardedInterstitialListenerWrapper != null) {
                providerAdapter.setRewardedInterstitialListener(this);
            }
            providerAdapter.initInterstitial(this.mActivity, this.mAppKey, this.mUserId);
            return providerAdapter;
        } catch (Throwable e) {
            IronSourceError error = ErrorBuilder.buildInitFailedError(providerName + " initialization failed - please verify that required dependencies are in you build path.", ParametersKeys.INTERSTITIAL);
            this.mServerResponseWrapper.decreaseMaxISAdapters();
            this.mLoggerManager.logException(IronSourceTag.API, this.TAG + ":startAdapter", e);
            this.mLoggerManager.log(IronSourceTag.API, error.toString(), 2);
            return null;
        }
    }

    private synchronized void addInitiatedInterstitialAdapter(AbstractAdapter adapter) {
        addToInitiated(adapter);
        removeFromNotInit(adapter);
        removeFromReady(adapter);
        removeFromExhausted(adapter);
        removeFromLoadFailed(adapter);
        removeFromLoading(adapter);
    }

    private synchronized void addReadyInterstitialAdapter(AbstractAdapter adapter) {
        addToReady(adapter);
        removeFromInitiated(adapter);
        removeFromNotInit(adapter);
        removeFromExhausted(adapter);
        removeFromLoadFailed(adapter);
        removeFromLoading(adapter);
    }

    private synchronized void addNotInitInterstitialAdapter(AbstractAdapter adapter) {
        addToNotInit(adapter);
        removeFromReady(adapter);
        removeFromInitiated(adapter);
        removeFromExhausted(adapter);
        removeFromLoadFailed(adapter);
        removeFromLoading(adapter);
    }

    private synchronized void addExhaustedInterstitialAdapter(AbstractAdapter adapter) {
        addToExhausted(adapter);
        removeFromReady(adapter);
        removeFromInitiated(adapter);
        removeFromNotInit(adapter);
        removeFromLoadFailed(adapter);
        removeFromLoading(adapter);
    }

    private synchronized void addLoadFailedInterstitialAdapter(AbstractAdapter adapter) {
        addToLoadFailed(adapter);
        removeFromReady(adapter);
        removeFromInitiated(adapter);
        removeFromNotInit(adapter);
        removeFromExhausted(adapter);
        removeFromLoading(adapter);
    }

    private synchronized void addLoadingInterstitialAdapter(AbstractAdapter adapter) {
        addToLoading(adapter);
        removeFromReady(adapter);
        removeFromInitiated(adapter);
        removeFromNotInit(adapter);
        removeFromExhausted(adapter);
        removeFromLoadFailed(adapter);
    }

    private synchronized void addToInitiated(AbstractAdapter adapter) {
        int priorityLocation = this.mInitiatedAdapters.size();
        if (!this.mInitiatedAdapters.contains(adapter)) {
            Iterator it = this.mInitiatedAdapters.iterator();
            while (it.hasNext()) {
                AbstractAdapter ia = (AbstractAdapter) it.next();
                if (adapter.getInterstitialPriority() <= ia.getInterstitialPriority()) {
                    priorityLocation = this.mInitiatedAdapters.indexOf(ia);
                    break;
                }
            }
            this.mInitiatedAdapters.add(priorityLocation, adapter);
        }
    }

    private synchronized void removeFromInitiated(AbstractAdapter adapter) {
        if (this.mInitiatedAdapters.contains(adapter)) {
            this.mInitiatedAdapters.remove(adapter);
        }
    }

    private synchronized void addToReady(AbstractAdapter adapter) {
        int priorityLocation = this.mReadyAdapters.size();
        if (!this.mReadyAdapters.contains(adapter)) {
            Iterator it = this.mReadyAdapters.iterator();
            while (it.hasNext()) {
                AbstractAdapter ia = (AbstractAdapter) it.next();
                if (adapter.getInterstitialPriority() <= ia.getInterstitialPriority()) {
                    priorityLocation = this.mReadyAdapters.indexOf(ia);
                    break;
                }
            }
            this.mReadyAdapters.add(priorityLocation, adapter);
        }
    }

    private synchronized void removeFromReady(AbstractAdapter adapter) {
        if (this.mReadyAdapters.contains(adapter)) {
            this.mReadyAdapters.remove(adapter);
        }
    }

    private synchronized void addToNotInit(AbstractAdapter adapter) {
        if (!this.mNotInitAdapters.contains(adapter)) {
            this.mNotInitAdapters.add(adapter);
        }
    }

    private synchronized void removeFromNotInit(AbstractAdapter adapter) {
        if (this.mNotInitAdapters.contains(adapter)) {
            this.mNotInitAdapters.remove(adapter);
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

    private synchronized void addToLoadFailed(AbstractAdapter adapter) {
        if (!this.mLoadFailedAdapters.contains(adapter)) {
            this.mLoadFailedAdapters.add(adapter);
        }
    }

    private synchronized void removeFromLoadFailed(AbstractAdapter adapter) {
        if (this.mLoadFailedAdapters.contains(adapter)) {
            this.mLoadFailedAdapters.remove(adapter);
        }
    }

    private synchronized void addToLoading(AbstractAdapter adapter) {
        if (!this.mLoadingAdapters.contains(adapter)) {
            this.mLoadingAdapters.add(adapter);
        }
    }

    private synchronized void removeFromLoading(AbstractAdapter adapter) {
        if (this.mLoadingAdapters.contains(adapter)) {
            this.mLoadingAdapters.remove(adapter);
        }
    }

    public synchronized void onInterstitialInitSuccess(AbstractAdapter adapter) {
        this.mLoggerManager.log(IronSourceTag.ADAPTER_CALLBACK, adapter.getProviderName() + " :onInterstitialInitSuccess()", 1);
        this.mLoggerManager.log(IronSourceTag.NATIVE, this.TAG + ": startAdapter(" + adapter.getProviderName() + ") moved to 'Initiated' list", 0);
        addInitiatedInterstitialAdapter(adapter);
        this.mDidFinishToInitInterstitial = true;
        if (this.mDidCallLoad && this.mReadyAdapters.size() + this.mLoadingAdapters.size() < this.mNumberOfAdaptersToLoad) {
            addLoadingInterstitialAdapter(adapter);
            loadAdapterAndSendEvent(adapter);
        }
    }

    public synchronized void onInterstitialInitFailed(IronSourceError error, AbstractAdapter adapter) {
        try {
            this.mLoggerManager.log(IronSourceTag.ADAPTER_CALLBACK, adapter.getProviderName() + ":onInterstitialInitFailed(" + error + ")", 1);
            this.mLoggerManager.log(IronSourceTag.NATIVE, "Smart Loading - " + adapter.getProviderName() + " moved to 'Not Ready' list", 0);
            addNotInitInterstitialAdapter(adapter);
            if (this.mNotInitAdapters.size() >= this.mServerResponseWrapper.getMaxISAdapters()) {
                this.mLoggerManager.log(IronSourceTag.NATIVE, "Smart Loading - initialization failed - no adapters are initiated and no more left to init, error: " + error.getErrorMessage(), 2);
                if (this.mDidCallLoad) {
                    sendOrScheduleLoadFailedCallback(ErrorBuilder.buildGenericError("no ads to show"), false);
                }
                this.mDidFinishToInitInterstitial = true;
            } else {
                startNextAdapter();
            }
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceTag.ADAPTER_CALLBACK, "onInterstitialInitFailed(error:" + error + ", " + "provider:" + adapter.getProviderName() + ")", e);
        }
    }

    public synchronized void onInterstitialAdReady(AbstractAdapter adapter) {
        boolean shouldReportReady = false;
        this.mLoggerManager.log(IronSourceTag.ADAPTER_CALLBACK, adapter.getProviderName() + ":onInterstitialAdReady()", 1);
        JSONObject data = IronSourceUtils.getProviderAdditionalData(adapter);
        try {
            data.put(ParametersKeys.VIDEO_STATUS, ServerProtocol.DIALOG_RETURN_SCOPES_TRUE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        InterstitialEventsManager.getInstance().log(new EventData(27, data));
        if (this.mDidCallLoad) {
            if (this.mReadyAdapters.size() == 0) {
                shouldReportReady = true;
            }
            this.mLoggerManager.log(IronSourceTag.NATIVE, "Smart Loading - " + adapter.getProviderName() + " moved to 'Ready' list", 0);
            addReadyInterstitialAdapter(adapter);
        }
        removeScheduledLoadFailedCallback();
        this.mLoadInProgress = false;
        if (shouldReportReady) {
            this.mInterstitialListenersWrapper.onInterstitialAdReady();
        }
    }

    public synchronized void onInterstitialAdLoadFailed(IronSourceError error, AbstractAdapter adapter) {
        boolean shouldReportFailed = false;
        this.mLoggerManager.log(IronSourceTag.ADAPTER_CALLBACK, adapter.getProviderName() + ":onInterstitialAdLoadFailed(" + error + ")", 1);
        JSONObject data = IronSourceUtils.getProviderAdditionalData(adapter);
        try {
            data.put(ParametersKeys.VIDEO_STATUS, "false");
            data.put(IronSourceConstants.ERROR_CODE_KEY, error.getErrorCode());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        InterstitialEventsManager.getInstance().log(new EventData(27, data));
        addLoadFailedInterstitialAdapter(adapter);
        if (this.mReadyAdapters.size() < this.mNumberOfAdaptersToLoad) {
            if (this.mInitiatedAdapters.size() > 0) {
                AbstractAdapter nextAdapter = (AbstractAdapter) this.mInitiatedAdapters.get(0);
                addLoadingInterstitialAdapter(nextAdapter);
                loadAdapterAndSendEvent(nextAdapter);
            } else if (startNextAdapter() == null && this.mDidCallLoad && this.mReadyAdapters.size() == 0 && this.mLoadingAdapters.size() == 0) {
                shouldReportFailed = true;
                if (isIterationRoundComplete()) {
                    completeIterationRound();
                }
            }
        }
        if (shouldReportFailed) {
            sendOrScheduleLoadFailedCallback(error, false);
        }
    }

    public void onInterstitialAdOpened(AbstractAdapter adapter) {
        this.mLoggerManager.log(IronSourceTag.ADAPTER_CALLBACK, adapter.getProviderName() + ":onInterstitialAdOpened()", 1);
        InterstitialEventsManager.getInstance().log(new EventData(25, IronSourceUtils.getProviderAdditionalData(adapter)));
        this.mInterstitialListenersWrapper.onInterstitialAdOpened();
    }

    public void onInterstitialAdClosed(AbstractAdapter adapter) {
        this.mLoggerManager.log(IronSourceTag.ADAPTER_CALLBACK, adapter.getProviderName() + ":onInterstitialAdClosed()", 1);
        InterstitialEventsManager.getInstance().log(new EventData(26, IronSourceUtils.getProviderAdditionalData(adapter)));
        this.mInterstitialListenersWrapper.onInterstitialAdClosed();
    }

    public void onInterstitialAdShowSucceeded(AbstractAdapter adapter) {
        this.mLoggerManager.log(IronSourceTag.ADAPTER_CALLBACK, adapter.getProviderName() + ":onInterstitialAdShowSucceeded()", 1);
        if (this.mReadyAdapters.size() > 0) {
            Iterator it = ((ArrayList) this.mReadyAdapters.clone()).iterator();
            while (it.hasNext()) {
                completeAdapterShow((AbstractAdapter) it.next());
            }
        }
        moveAdaptersToInitiated();
        this.mInterstitialListenersWrapper.onInterstitialAdShowSucceeded();
    }

    public void onInterstitialAdShowFailed(IronSourceError error, AbstractAdapter adapter) {
        this.mLoggerManager.log(IronSourceTag.ADAPTER_CALLBACK, adapter.getProviderName() + ":onInterstitialAdShowFailed(" + error + ")", 1);
        completeAdapterShow(adapter);
        if (this.mReadyAdapters.size() > 0) {
            this.mDidCallLoad = true;
            showInterstitial(this.mLastPlacementForShowFail);
            return;
        }
        this.mInterstitialListenersWrapper.onInterstitialAdShowFailed(error);
    }

    public void onInterstitialAdClicked(AbstractAdapter adapter) {
        this.mLoggerManager.log(IronSourceTag.ADAPTER_CALLBACK, adapter.getProviderName() + ":onInterstitialAdClicked()", 1);
        InterstitialEventsManager.getInstance().log(new EventData(28, IronSourceUtils.getProviderAdditionalData(adapter)));
        this.mInterstitialListenersWrapper.onInterstitialAdClicked();
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

    public boolean isInterstitialReady() {
        if (this.mShouldTrackNetworkState && this.mActivity != null && !IronSourceUtils.isNetworkConnected(this.mActivity)) {
            return false;
        }
        Iterator it = this.mReadyAdapters.iterator();
        while (it.hasNext()) {
            if (((AbstractAdapter) it.next()).isInterstitialReady()) {
                return true;
            }
        }
        return false;
    }

    public boolean isInterstitialPlacementCapped(String placementName) {
        return false;
    }

    public InterstitialPlacement getPlacementByName(String placementName) {
        if (this.mServerResponseWrapper == null || this.mServerResponseWrapper.getConfigurations() == null || this.mServerResponseWrapper.getConfigurations().getInterstitialConfigurations() == null) {
            return null;
        }
        try {
            InterstitialPlacement placement = this.mServerResponseWrapper.getConfigurations().getInterstitialConfigurations().getInterstitialPlacement(placementName);
            if (placement != null) {
                return placement;
            }
            placement = this.mServerResponseWrapper.getConfigurations().getInterstitialConfigurations().getDefaultInterstitialPlacement();
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

    public void onInitSuccess(List<AD_UNIT> list, boolean revived) {
    }

    public void onInitFailed(String reason) {
        if (this.mDidCallLoad) {
            sendOrScheduleLoadFailedCallback(ErrorBuilder.buildGenericError("no ads to show"), false);
        }
    }
}
