package com.ironsource.mediationsdk;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import com.ironsource.eventsmodule.EventData;
import com.ironsource.mediationsdk.IronSource.AD_UNIT;
import com.ironsource.mediationsdk.MediationInitializer.EInitStatus;
import com.ironsource.mediationsdk.MediationInitializer.OnMediationInitializationListener;
import com.ironsource.mediationsdk.config.ConfigFile;
import com.ironsource.mediationsdk.events.InterstitialEventsManager;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.model.BannerPlacement;
import com.ironsource.mediationsdk.model.ProviderSettings;
import com.ironsource.mediationsdk.sdk.BannerApi;
import com.ironsource.mediationsdk.sdk.BannerManagerListener;
import com.ironsource.mediationsdk.utils.ErrorBuilder;
import com.ironsource.mediationsdk.utils.IronSourceConstants;
import com.ironsource.mediationsdk.utils.IronSourceUtils;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import cz.msebera.android.httpclient.HttpStatus;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.cocos2dx.lib.BuildConfig;
import org.json.JSONObject;

public class BannerManager extends AbstractAdUnitManager implements OnMediationInitializationListener, BannerApi, BannerManagerListener {
    private static final long LOAD_FAILED_COOLDOWN_IN_MILLIS = 15000;
    private final String TAG = getClass().getName();
    private boolean isFirstLoad;
    private boolean mDidCallLoad = false;
    private boolean mDidFinishToInitBanner;
    private Handler mHandler;
    private HandlerThread mHandlerThread = new HandlerThread("IronSourceBannerHandler");
    private ArrayList<AbstractAdapter> mInitiatedAdapters;
    private long mLastLoadFailTimestamp;
    private ArrayList<AbstractAdapter> mLoadFailedAdapters;
    LoadFailedRunnable mLoadFailedRunnable;
    private boolean mLoadInProgress = false;
    private AbstractAdapter mLoadingAdapter;
    private ArrayList<AbstractAdapter> mNotInitAdapters;
    private IronSourceBannerLayout mPendingToLoadBannerLayout;
    private AbstractAdapter mReadyAdapter;

    private class LoadFailedRunnable implements Runnable {
        IronSourceError error;

        LoadFailedRunnable(IronSourceError error) {
            this.error = error;
        }

        public void run() {
            BannerManager.this.mLoggerManager.log(IronSourceTag.API, "Load Banner failed: " + this.error.getErrorMessage(), 1);
            BannerManager.this.mLastLoadFailTimestamp = System.currentTimeMillis();
            if (!(BannerManager.this.mPendingToLoadBannerLayout == null || BannerManager.this.mPendingToLoadBannerLayout.getBannerListener() == null)) {
                IronSourceLoggerManager.getLogger().log(IronSourceTag.CALLBACK, "onBannerAdLoadFailed(), error: " + this.error.getErrorMessage(), 1);
                JSONObject data = IronSourceUtils.getMediationAdditionalData();
                try {
                    int bannerSizeData = BannerManager.this.mPendingToLoadBannerLayout.getSize().getValue();
                    data.put(ParametersKeys.VIDEO_STATUS, "false");
                    data.put(IronSourceConstants.ERROR_CODE_KEY, this.error.getErrorCode());
                    data.put("bannerAdSize", bannerSizeData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                InterstitialEventsManager.getInstance().log(new EventData(HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED, data));
                BannerManager.this.mPendingToLoadBannerLayout.getBannerListener().onBannerAdLoadFailed(this.error);
            }
            BannerManager.this.resetLoadRound(true);
        }
    }

    public BannerManager() {
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
        this.mLastLoadFailTimestamp = 0;
        this.mNotInitAdapters = new ArrayList();
        this.mInitiatedAdapters = new ArrayList();
        this.mLoadFailedAdapters = new ArrayList();
        this.isFirstLoad = true;
    }

    public void initBanners(Activity activity, String appKey, String userId) {
        this.mLoggerManager.log(IronSourceTag.NATIVE, this.TAG + ":initBanners(appKey: " + appKey + ", userId: " + userId + ")", 1);
        this.mAppKey = appKey;
        this.mUserId = userId;
        this.mActivity = activity;
        this.mServerResponseWrapper = IronSourceObject.getInstance().getCurrentServerResponse();
        if (this.mServerResponseWrapper != null) {
            startNextAdapter();
        }
    }

    private AbstractAdapter startNextAdapter() {
        AbstractAdapter initiatedAdapter = null;
        while (this.mServerResponseWrapper.hasMoreBannerProvidersToLoad() && initiatedAdapter == null) {
            initiatedAdapter = startAdapter(this.mServerResponseWrapper.getNextBannerProvider());
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
                    sso.addToBannerAdaptersList(providerAdapter);
                }
            }
            setCustomParams(providerAdapter);
            providerAdapter.setLogListener(this.mLoggerManager);
            providerAdapter.setBannerTimeout(this.mServerResponseWrapper.getConfigurations().getBannerConfigurations().getBannerAdaptersSmartLoadTimeout());
            providerAdapter.setBannerPriority(this.mServerResponseWrapper.getBannerAdaptersLoadPosition());
            providerAdapter.setBannerConfigurations(this.mServerResponseWrapper.getConfigurations().getBannerConfigurations());
            providerAdapter.setBannerListener(this);
            if (!TextUtils.isEmpty(ConfigFile.getConfigFile().getPluginType())) {
                providerAdapter.setPluginData(ConfigFile.getConfigFile().getPluginType(), ConfigFile.getConfigFile().getPluginFrameworkVersion());
            }
            this.mLoggerManager.log(IronSourceTag.NATIVE, this.TAG + ":startAdapter(providerAdapter: " + providerAdapter.getProviderName(), 0);
            providerAdapter.initBanners(this.mActivity, this.mAppKey, this.mUserId);
            return providerAdapter;
        } catch (Throwable e) {
            IronSourceError error = ErrorBuilder.buildInitFailedError(providerName + " initialization failed - please verify that required dependencies are in you build path.", IronSourceConstants.BANNER_AD_UNIT);
            this.mServerResponseWrapper.decreaseMaxBannerAdapters();
            this.mLoggerManager.logException(IronSourceTag.API, this.TAG + ":startAdapter", e);
            this.mLoggerManager.log(IronSourceTag.API, error.toString(), 2);
            return null;
        }
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

    public boolean isBannerPlacementCapped(String placementName) {
        return false;
    }

    public IronSourceBannerLayout createBanner(Activity activity, EBannerSize size) {
        return new IronSourceBannerLayout(activity, size, this);
    }

    public void loadBanner(IronSourceBannerLayout banner) {
    }

    public void loadBanner(IronSourceBannerLayout banner, String placementName) {
        if (banner == null) {
            try {
                this.mLoggerManager.log(IronSourceTag.API, "Load Banner can't be called on null object", 1);
            } catch (Exception e) {
                sendOrScheduleLoadFailedCallback(ErrorBuilder.buildLoadFailedError("loadBanner exception"), false);
            }
        } else if (banner.isDestoyed()) {
            this.mLoggerManager.log(IronSourceTag.API, "Banner is already destroyed and can't be used anymore. Please create a new one using IronSource.createBanner API", 1);
        } else if (this.mLoadInProgress) {
            this.mLoggerManager.log(IronSourceTag.API, "Load Banner is already in progress", 1);
        } else {
            resetLoadRound(true);
            this.mDidCallLoad = true;
            this.mPendingToLoadBannerLayout = banner;
            this.mLoadInProgress = true;
            banner.setPlacementName(placementName);
            EInitStatus currentInitStatus = MediationInitializer.getInstance().getCurrentInitStatus();
            String loadFailMsg = "Load Banner can't be called before the Banner ad unit initialization completed successfully";
            if (currentInitStatus == EInitStatus.INIT_FAILED || currentInitStatus == EInitStatus.NOT_INIT) {
                sendOrScheduleLoadFailedCallback(ErrorBuilder.buildLoadFailedError(loadFailMsg), false);
            } else if (currentInitStatus == EInitStatus.INIT_IN_PROGRESS) {
                sendOrScheduleLoadFailedCallback(ErrorBuilder.buildLoadFailedError(loadFailMsg), true);
            } else if (!IronSourceUtils.isNetworkConnected(this.mActivity)) {
                sendOrScheduleLoadFailedCallback(ErrorBuilder.buildNoInternetConnectionLoadFailError(IronSourceConstants.BANNER_AD_UNIT), false);
            } else if (this.mServerResponseWrapper != null && this.mInitiatedAdapters.size() != 0) {
                if (this.mServerResponseWrapper != null) {
                    this.isFirstLoad = false;
                    BannerPlacement placement = validatePlacement(placementName);
                    sendMediationLevelLoadEvent(banner, placement.getPlacementName());
                    String cappedMessage = IronSourceObject.getInstance().getCappingMessage(placement.getPlacementName(), IronSourceObject.getInstance().getBannerCappingStatus(placement.getPlacementName()));
                    if (TextUtils.isEmpty(cappedMessage)) {
                        banner.setPlacementName(placement.getPlacementName());
                    } else {
                        this.mLoggerManager.log(IronSourceTag.API, cappedMessage, 1);
                        sendOrScheduleLoadFailedCallback(ErrorBuilder.buildCappedError(IronSourceConstants.BANNER_AD_UNIT, cappedMessage), false);
                        return;
                    }
                }
                AbstractAdapter adapter = (AbstractAdapter) this.mInitiatedAdapters.get(0);
                addLoadingBannerAdapter(adapter);
                loadAdapterAndSendEvent(adapter, banner);
            } else if (this.mServerResponseWrapper == null || this.mDidFinishToInitBanner) {
                sendOrScheduleLoadFailedCallback(ErrorBuilder.buildGenericError("no ads to show"), false);
            }
        }
    }

    private void sendMediationLevelLoadEvent(IronSourceBannerLayout banner, String placementName) {
        JSONObject data = IronSourceUtils.getMediationAdditionalData();
        int bannerSizeData = 0;
        if (banner != null) {
            try {
                bannerSizeData = banner.getSize().getValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        data.put("bannerAdSize", bannerSizeData);
        data.put("placement", placementName);
        InterstitialEventsManager.getInstance().log(new EventData(HttpStatus.SC_PAYMENT_REQUIRED, data));
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

    public void onInitSuccess(List<AD_UNIT> list, boolean revived) {
    }

    public void onInitFailed(String reason) {
        if (this.mDidCallLoad) {
            sendOrScheduleLoadFailedCallback(ErrorBuilder.buildGenericError("no ads to show"), false);
        }
    }

    private synchronized void removeScheduledLoadFailedCallback() {
        if (!(this.mHandler == null || this.mLoadFailedRunnable == null)) {
            this.mHandler.removeCallbacks(this.mLoadFailedRunnable);
        }
    }

    private synchronized void resetLoadRound(boolean moveAdaptersToInitiated) {
        if (moveAdaptersToInitiated) {
            moveAdaptersToInitiated();
        }
        this.mLoadInProgress = false;
        this.mDidCallLoad = false;
        this.mPendingToLoadBannerLayout = null;
        if (this.mLoadFailedRunnable != null) {
            this.mHandler.removeCallbacks(this.mLoadFailedRunnable);
        }
    }

    private synchronized void moveAdaptersToInitiated() {
        if (this.mReadyAdapter != null) {
            AbstractAdapter adapter = this.mReadyAdapter;
            this.mLoggerManager.log(IronSourceTag.NATIVE, "Smart Loading - " + adapter.getProviderName() + " moved to 'Initiated' list", 0);
            addInitiatedBannerAdapter(adapter);
        }
        if (this.mLoadingAdapter != null) {
            adapter = this.mLoadingAdapter;
            this.mLoggerManager.log(IronSourceTag.NATIVE, "Smart Loading - " + adapter.getProviderName() + " moved to 'Initiated' list", 0);
            addInitiatedBannerAdapter(adapter);
        }
        if (this.mLoadFailedAdapters.size() > 0) {
            Iterator it = ((ArrayList) this.mLoadFailedAdapters.clone()).iterator();
            while (it.hasNext()) {
                adapter = (AbstractAdapter) it.next();
                this.mLoggerManager.log(IronSourceTag.NATIVE, "Smart Loading - " + adapter.getProviderName() + " moved to 'Initiated' list", 0);
                addInitiatedBannerAdapter(adapter);
            }
        }
    }

    public void destroyBanner(IronSourceBannerLayout banner) {
        if (banner == null) {
            return;
        }
        if (banner.isDestoyed()) {
            this.mLoggerManager.log(IronSourceTag.API, "Banner is already destroyed and can't be used anymore. Please create a new one using IronSource.createBanner API", 1);
            return;
        }
        InterstitialEventsManager.getInstance().log(new EventData(HttpStatus.SC_NOT_ACCEPTABLE, IronSourceUtils.getMediationAdditionalData()));
        this.mLoadInProgress = false;
        this.mDidCallLoad = false;
        banner.destroyBanner();
    }

    void shouldTrackNetworkState(Context context, boolean track) {
    }

    boolean isPremiumAdapter(String providerName) {
        return false;
    }

    boolean isBackFillAvailable() {
        return false;
    }

    private synchronized void loadAdapterAndSendEvent(AbstractAdapter adapter, IronSourceBannerLayout bannerLayout) {
        JSONObject data = IronSourceUtils.getProviderAdditionalData(adapter);
        if (bannerLayout != null) {
            try {
                if (!TextUtils.isEmpty(bannerLayout.getPlacementName())) {
                    data.put("placement", bannerLayout.getPlacementName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        InterstitialEventsManager.getInstance().log(new EventData(HttpStatus.SC_PAYMENT_REQUIRED, data));
        adapter.loadBanner(bannerLayout);
    }

    public synchronized void onBannerInitSuccess(AbstractAdapter adapter) {
        this.mLoggerManager.log(IronSourceTag.ADAPTER_CALLBACK, adapter.getProviderName() + " :onBannerInitSuccess()", 1);
        this.mLoggerManager.log(IronSourceTag.NATIVE, this.TAG + ": startAdapter(" + adapter.getProviderName() + ") moved to 'Initiated' list", 0);
        this.mDidFinishToInitBanner = true;
        if (this.mDidCallLoad && this.mReadyAdapter == null && this.mLoadingAdapter == null) {
            if (!(this.mServerResponseWrapper == null || this.mPendingToLoadBannerLayout == null || !this.isFirstLoad)) {
                this.isFirstLoad = false;
                this.mPendingToLoadBannerLayout.setPlacementName(validatePlacement(this.mPendingToLoadBannerLayout.getPlacementName()).getPlacementName());
                sendMediationLevelLoadEvent(this.mPendingToLoadBannerLayout, this.mPendingToLoadBannerLayout.getPlacementName());
                String cappedMessage = IronSourceObject.getInstance().getCappingMessage(this.mPendingToLoadBannerLayout.getPlacementName(), IronSourceObject.getInstance().getBannerCappingStatus(this.mPendingToLoadBannerLayout.getPlacementName()));
                if (!TextUtils.isEmpty(cappedMessage)) {
                    this.mLoggerManager.log(IronSourceTag.API, cappedMessage, 1);
                    sendOrScheduleLoadFailedCallback(ErrorBuilder.buildCappedError(IronSourceConstants.BANNER_AD_UNIT, cappedMessage), false);
                }
            }
            addLoadingBannerAdapter(adapter);
            if (this.mPendingToLoadBannerLayout != null) {
                loadAdapterAndSendEvent(adapter, this.mPendingToLoadBannerLayout);
            }
        } else {
            addInitiatedBannerAdapter(adapter);
        }
    }

    public synchronized void onBannerInitFailed(IronSourceError error, AbstractAdapter adapter) {
        try {
            this.mLoggerManager.log(IronSourceTag.ADAPTER_CALLBACK, adapter.getProviderName() + ":onBannerInitFailed(" + error + ")", 1);
            this.mLoggerManager.log(IronSourceTag.NATIVE, "Smart Loading - " + adapter.getProviderName() + " moved to 'Not Ready' list", 0);
            addNotInitBannerAdapter(adapter);
            if (this.mNotInitAdapters.size() >= this.mServerResponseWrapper.getMaxBannerAdapters()) {
                this.mLoggerManager.log(IronSourceTag.NATIVE, "Smart Loading - initialization failed - no adapters are initiated and no more left to init, error: " + error.getErrorMessage(), 2);
                if (this.mDidCallLoad) {
                    sendOrScheduleLoadFailedCallback(ErrorBuilder.buildGenericError("no ads to show"), false);
                }
                this.mDidFinishToInitBanner = true;
            } else {
                startNextAdapter();
            }
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceTag.ADAPTER_CALLBACK, "onBannerInitFailed(error:" + error + ", " + "provider:" + adapter.getProviderName() + ")", e);
        }
    }

    public void onBannerImpression(AbstractAdapter adapter, IronSourceBannerLayout banner) {
        JSONObject data = IronSourceUtils.getProviderAdditionalData(adapter);
        int bannerSizeData = 0;
        if (banner != null) {
            try {
                bannerSizeData = banner.getSize().getValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        data.put("bannerAdSize", bannerSizeData);
        InterstitialEventsManager.getInstance().log(new EventData(HttpStatus.SC_METHOD_NOT_ALLOWED, data));
        moveAdaptersToInitiated();
    }

    public void onBannerAdLoaded(AbstractAdapter adapter) {
        if (this.mDidCallLoad) {
            this.mLoggerManager.log(IronSourceTag.NATIVE, "Smart Loading - " + adapter.getProviderName() + " moved to 'Ready' list", 0);
            addReadyBannerAdapter(adapter);
        }
        removeScheduledLoadFailedCallback();
        this.mLoadInProgress = false;
    }

    public void onBannerAdLoadFailed(IronSourceError error, AbstractAdapter adapter) {
        boolean shouldReportFailed = false;
        addLoadFailedBannerAdapter(adapter);
        if (this.mReadyAdapter == null) {
            if (this.mInitiatedAdapters.size() > 0) {
                AbstractAdapter nextAdapter = (AbstractAdapter) this.mInitiatedAdapters.get(0);
                addLoadingBannerAdapter(nextAdapter);
                if (this.mPendingToLoadBannerLayout != null) {
                    loadAdapterAndSendEvent(nextAdapter, this.mPendingToLoadBannerLayout);
                }
            } else if (startNextAdapter() == null && this.mDidCallLoad && this.mReadyAdapter == null && this.mLoadingAdapter == null) {
                shouldReportFailed = true;
            }
        }
        if (shouldReportFailed) {
            JSONObject data = IronSourceUtils.getMediationAdditionalData();
            try {
                data.put(ParametersKeys.VIDEO_STATUS, "false");
                data.put(IronSourceConstants.ERROR_CODE_KEY, error.getErrorCode());
                if (!(this.mPendingToLoadBannerLayout == null || this.mPendingToLoadBannerLayout.getSize() == null)) {
                    data.put("bannerAdSize", this.mPendingToLoadBannerLayout.getSize().getValue());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            InterstitialEventsManager.getInstance().log(new EventData(HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED, data));
            sendOrScheduleLoadFailedCallback(error, false);
        }
    }

    public void onBannerAdClicked(AbstractAdapter adapter) {
    }

    public void onBannerAdScreenPresented(AbstractAdapter adapter) {
    }

    public void onBannerAdScreenDismissed(AbstractAdapter adapter) {
    }

    public void onBannerAdLeftApplication(AbstractAdapter adapter) {
    }

    public BannerPlacement getPlacementByName(String placementName) {
        if (this.mServerResponseWrapper == null || this.mServerResponseWrapper.getConfigurations() == null || this.mServerResponseWrapper.getConfigurations().getBannerConfigurations() == null) {
            return null;
        }
        try {
            BannerPlacement placement = this.mServerResponseWrapper.getConfigurations().getBannerConfigurations().getBannerPlacement(placementName);
            if (placement != null) {
                return placement;
            }
            placement = this.mServerResponseWrapper.getConfigurations().getBannerConfigurations().getDefaultBannerPlacement();
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

    private synchronized void addInitiatedBannerAdapter(AbstractAdapter adapter) {
        addToInitiated(adapter);
        removeFromNotInit(adapter);
        removeFromReady(adapter);
        removeFromLoadFailed(adapter);
        removeFromLoading(adapter);
    }

    private synchronized void addReadyBannerAdapter(AbstractAdapter adapter) {
        addToReady(adapter);
        removeFromInitiated(adapter);
        removeFromNotInit(adapter);
        removeFromLoadFailed(adapter);
        removeFromLoading(adapter);
    }

    private synchronized void addNotInitBannerAdapter(AbstractAdapter adapter) {
        addToNotInit(adapter);
        removeFromReady(adapter);
        removeFromInitiated(adapter);
        removeFromLoadFailed(adapter);
        removeFromLoading(adapter);
    }

    private synchronized void addLoadFailedBannerAdapter(AbstractAdapter adapter) {
        addToLoadFailed(adapter);
        removeFromReady(adapter);
        removeFromInitiated(adapter);
        removeFromNotInit(adapter);
        removeFromLoading(adapter);
    }

    private synchronized void addLoadingBannerAdapter(AbstractAdapter adapter) {
        addToLoading(adapter);
        removeFromReady(adapter);
        removeFromInitiated(adapter);
        removeFromNotInit(adapter);
        removeFromLoadFailed(adapter);
    }

    private synchronized void addToInitiated(AbstractAdapter adapter) {
        int priorityLocation = this.mInitiatedAdapters.size();
        if (!this.mInitiatedAdapters.contains(adapter)) {
            Iterator it = this.mInitiatedAdapters.iterator();
            while (it.hasNext()) {
                AbstractAdapter ia = (AbstractAdapter) it.next();
                if (adapter.getBannerPriority() <= ia.getBannerPriority()) {
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
        this.mReadyAdapter = adapter;
    }

    private synchronized void removeFromReady(AbstractAdapter adapter) {
        if (this.mReadyAdapter != null && this.mReadyAdapter.equals(adapter)) {
            this.mReadyAdapter = null;
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
        this.mLoadingAdapter = adapter;
    }

    private synchronized void removeFromLoading(AbstractAdapter adapter) {
        if (this.mLoadingAdapter != null && this.mLoadingAdapter.equals(adapter)) {
            this.mLoadingAdapter = null;
        }
    }

    private BannerPlacement validatePlacement(String placementName) {
        BannerPlacement placement = this.mServerResponseWrapper.getConfigurations().getBannerConfigurations().getBannerPlacement(placementName);
        if (placement == null) {
            String noPlacementMessage = "Placement is not valid, please make sure you are using the right placements, using the default placement.";
            if (placementName != null) {
                this.mLoggerManager.log(IronSourceTag.API, noPlacementMessage, 3);
            }
            placement = this.mServerResponseWrapper.getConfigurations().getBannerConfigurations().getDefaultBannerPlacement();
            if (placement == null) {
                this.mLoggerManager.log(IronSourceTag.API, "Default placement was not found, please make sure you are using the right placements.", 3);
            }
        }
        return placement;
    }
}
