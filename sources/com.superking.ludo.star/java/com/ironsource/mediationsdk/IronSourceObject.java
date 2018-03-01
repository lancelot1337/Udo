package com.ironsource.mediationsdk;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import com.ironsource.environment.DeviceStatus;
import com.ironsource.eventsmodule.EventData;
import com.ironsource.mediationsdk.IronSource.AD_UNIT;
import com.ironsource.mediationsdk.MediationInitializer.OnMediationInitializationListener;
import com.ironsource.mediationsdk.config.ConfigValidationResult;
import com.ironsource.mediationsdk.events.InterstitialEventsManager;
import com.ironsource.mediationsdk.events.RewardedVideoEventsManager;
import com.ironsource.mediationsdk.events.SuperLooper;
import com.ironsource.mediationsdk.logger.ConsoleLogger;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.logger.LogListener;
import com.ironsource.mediationsdk.logger.PublisherLogger;
import com.ironsource.mediationsdk.logger.ServerLogger;
import com.ironsource.mediationsdk.model.BannerPlacement;
import com.ironsource.mediationsdk.model.InterstitialPlacement;
import com.ironsource.mediationsdk.model.OfferwallPlacement;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.InterstitialListener;
import com.ironsource.mediationsdk.sdk.IronSourceInterface;
import com.ironsource.mediationsdk.sdk.ListenersWrapper;
import com.ironsource.mediationsdk.sdk.OfferwallListener;
import com.ironsource.mediationsdk.sdk.RewardedInterstitialListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;
import com.ironsource.mediationsdk.server.HttpFunctions;
import com.ironsource.mediationsdk.server.ServerURL;
import com.ironsource.mediationsdk.utils.CappingManager;
import com.ironsource.mediationsdk.utils.CappingManager.ECappingStatus;
import com.ironsource.mediationsdk.utils.ErrorBuilder;
import com.ironsource.mediationsdk.utils.GeneralPropertiesWorker;
import com.ironsource.mediationsdk.utils.IronSourceConstants;
import com.ironsource.mediationsdk.utils.IronSourceConstants.Gender;
import com.ironsource.mediationsdk.utils.IronSourceUtils;
import com.ironsource.mediationsdk.utils.ServerResponseWrapper;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import cz.msebera.android.httpclient.HttpStatus;
import io.branch.referral.R;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.cocos2dx.lib.BuildConfig;
import org.cocos2dx.lib.Cocos2dxEditBox;
import org.cocos2dx.lib.Cocos2dxHandler;
import org.json.JSONException;
import org.json.JSONObject;

public class IronSourceObject implements OnMediationInitializationListener, IronSourceInterface {
    private static IronSourceObject sInstance;
    private final String TAG = getClass().getName();
    private ServerResponseWrapper currentServerResponse = null;
    private Activity mActivity;
    private String mAppKey = null;
    private ArrayList<AbstractAdapter> mBannerAdaptersList;
    private BannerManager mBannerManager;
    private String mDynamicUserId = null;
    private AtomicBoolean mEventManagersInit;
    private ArrayList<AbstractAdapter> mISAdaptersList;
    private InterstitialManager mInterstitialManager;
    private ListenersWrapper mListenersWrapper;
    private IronSourceLoggerManager mLoggerManager;
    private String mMediationType = null;
    private OfferwallManager mOfferwallManager;
    private PublisherLogger mPublisherLogger;
    private ArrayList<AbstractAdapter> mRVAdaptersList;
    private Set<AD_UNIT> mRequestedAdUnits;
    private RewardedVideoManager mRewardedVideoManager;
    private String mSegment = null;
    private boolean mShouldSendGetInstanceEvent = true;
    private Integer mUserAge = null;
    private String mUserGender = null;
    private String mUserId = null;
    private final Object serverResponseLocker = new Object();

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$ironsource$mediationsdk$IronSource$AD_UNIT = new int[AD_UNIT.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$ironsource$mediationsdk$utils$CappingManager$ECappingStatus = new int[ECappingStatus.values().length];

        static {
            try {
                $SwitchMap$com$ironsource$mediationsdk$utils$CappingManager$ECappingStatus[ECappingStatus.CAPPED_PER_DELIVERY.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$ironsource$mediationsdk$utils$CappingManager$ECappingStatus[ECappingStatus.CAPPED_PER_COUNT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$ironsource$mediationsdk$utils$CappingManager$ECappingStatus[ECappingStatus.CAPPED_PER_PACE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$ironsource$mediationsdk$utils$CappingManager$ECappingStatus[ECappingStatus.NOT_CAPPED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$ironsource$mediationsdk$IronSource$AD_UNIT[AD_UNIT.REWARDED_VIDEO.ordinal()] = 1;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$ironsource$mediationsdk$IronSource$AD_UNIT[AD_UNIT.INTERSTITIAL.ordinal()] = 2;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$ironsource$mediationsdk$IronSource$AD_UNIT[AD_UNIT.OFFERWALL.ordinal()] = 3;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$ironsource$mediationsdk$IronSource$AD_UNIT[AD_UNIT.BANNER.ordinal()] = 4;
            } catch (NoSuchFieldError e8) {
            }
        }
    }

    public interface IResponseListener {
        void onUnrecoverableError(String str);
    }

    public static synchronized IronSourceObject getInstance() {
        IronSourceObject ironSourceObject;
        synchronized (IronSourceObject.class) {
            if (sInstance == null) {
                sInstance = new IronSourceObject();
            }
            ironSourceObject = sInstance;
        }
        return ironSourceObject;
    }

    private IronSourceObject() {
        initializeManagers();
        this.mEventManagersInit = new AtomicBoolean();
        this.mRVAdaptersList = new ArrayList();
        this.mISAdaptersList = new ArrayList();
        this.mBannerAdaptersList = new ArrayList();
        this.mRequestedAdUnits = new HashSet();
    }

    public synchronized void init(Activity activity, String appKey, AD_UNIT... adUnits) {
        int i = 0;
        synchronized (this) {
            int length;
            ConfigValidationResult validationResultAppKey;
            JSONObject data;
            if (adUnits != null) {
                if (adUnits.length != 0) {
                    for (AD_UNIT adUnit : adUnits) {
                        this.mRequestedAdUnits.add(adUnit);
                    }
                    this.mLoggerManager.log(IronSourceTag.API, "init(appKey:" + appKey + ")", 1);
                    if (activity != null) {
                        this.mLoggerManager.log(IronSourceTag.API, "Init Fail - provided activity is null", 2);
                    } else {
                        this.mActivity = activity;
                        prepareEventManagers(activity);
                        validationResultAppKey = validateAppKey(appKey);
                        if (validationResultAppKey.isValid()) {
                            if (this.mRequestedAdUnits.contains(AD_UNIT.REWARDED_VIDEO)) {
                                this.mListenersWrapper.onRewardedVideoAvailabilityChanged(false);
                            }
                            if (this.mRequestedAdUnits.contains(AD_UNIT.OFFERWALL)) {
                                this.mListenersWrapper.onOfferwallAvailable(false, validationResultAppKey.getIronSourceError());
                            }
                            IronSourceLoggerManager.getLogger().log(IronSourceTag.API, validationResultAppKey.getIronSourceError().toString(), 1);
                        } else {
                            setIronSourceAppKey(appKey);
                            if (this.mShouldSendGetInstanceEvent) {
                                data = IronSourceUtils.getMediationAdditionalData();
                                if (adUnits != null) {
                                    try {
                                        length = adUnits.length;
                                        while (i < length) {
                                            data.put(adUnits[i].toString(), true);
                                            i++;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                RewardedVideoEventsManager.getInstance().log(new EventData(14, data));
                                this.mShouldSendGetInstanceEvent = false;
                            }
                            if (this.mRequestedAdUnits.contains(AD_UNIT.INTERSTITIAL)) {
                                MediationInitializer.getInstance().addMediationInitializationListener(this.mInterstitialManager);
                            }
                            MediationInitializer.getInstance().addMediationInitializationListener(this);
                            MediationInitializer.getInstance().init(activity, appKey, this.mUserId, adUnits);
                        }
                    }
                }
            }
            for (AD_UNIT adUnit2 : AD_UNIT.values()) {
                this.mRequestedAdUnits.add(adUnit2);
            }
            this.mLoggerManager.log(IronSourceTag.API, "init(appKey:" + appKey + ")", 1);
            if (activity != null) {
                this.mActivity = activity;
                prepareEventManagers(activity);
                validationResultAppKey = validateAppKey(appKey);
                if (validationResultAppKey.isValid()) {
                    if (this.mRequestedAdUnits.contains(AD_UNIT.REWARDED_VIDEO)) {
                        this.mListenersWrapper.onRewardedVideoAvailabilityChanged(false);
                    }
                    if (this.mRequestedAdUnits.contains(AD_UNIT.OFFERWALL)) {
                        this.mListenersWrapper.onOfferwallAvailable(false, validationResultAppKey.getIronSourceError());
                    }
                    IronSourceLoggerManager.getLogger().log(IronSourceTag.API, validationResultAppKey.getIronSourceError().toString(), 1);
                } else {
                    setIronSourceAppKey(appKey);
                    if (this.mShouldSendGetInstanceEvent) {
                        data = IronSourceUtils.getMediationAdditionalData();
                        if (adUnits != null) {
                            length = adUnits.length;
                            while (i < length) {
                                data.put(adUnits[i].toString(), true);
                                i++;
                            }
                        }
                        RewardedVideoEventsManager.getInstance().log(new EventData(14, data));
                        this.mShouldSendGetInstanceEvent = false;
                    }
                    if (this.mRequestedAdUnits.contains(AD_UNIT.INTERSTITIAL)) {
                        MediationInitializer.getInstance().addMediationInitializationListener(this.mInterstitialManager);
                    }
                    MediationInitializer.getInstance().addMediationInitializationListener(this);
                    MediationInitializer.getInstance().init(activity, appKey, this.mUserId, adUnits);
                }
            } else {
                this.mLoggerManager.log(IronSourceTag.API, "Init Fail - provided activity is null", 2);
            }
        }
    }

    public void onInitSuccess(List<AD_UNIT> adUnits, boolean revived) {
        try {
            this.mLoggerManager.log(IronSourceTag.API, "onInitSuccess()", 1);
            if (revived) {
                JSONObject data = IronSourceUtils.getMediationAdditionalData();
                try {
                    data.put("revived", revived);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RewardedVideoEventsManager.getInstance().log(new EventData(R.styleable.AppCompatTheme_listMenuViewStyle, data));
            }
            InterstitialEventsManager.getInstance().triggerEventsSend();
            RewardedVideoEventsManager.getInstance().triggerEventsSend();
            for (AD_UNIT adUnit : AD_UNIT.values()) {
                if (this.mRequestedAdUnits.contains(adUnit)) {
                    if (adUnits.contains(adUnit)) {
                        switch (AnonymousClass1.$SwitchMap$com$ironsource$mediationsdk$IronSource$AD_UNIT[adUnit.ordinal()]) {
                            case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                                this.mRewardedVideoManager.initRewardedVideo(this.mActivity, getIronSourceAppKey(), getIronSourceUserId());
                                break;
                            case R.styleable.View_paddingStart /*2*/:
                                this.mInterstitialManager.initInterstitial(this.mActivity, getIronSourceAppKey(), getIronSourceUserId());
                                break;
                            case Cocos2dxEditBox.kEndActionReturn /*3*/:
                                this.mOfferwallManager.initOfferwall(this.mActivity, getIronSourceAppKey(), getIronSourceUserId());
                                break;
                            case R.styleable.View_theme /*4*/:
                                this.mBannerManager.initBanners(this.mActivity, getIronSourceAppKey(), getIronSourceUserId());
                                break;
                            default:
                                continue;
                        }
                    } else {
                        notifyPublisherAboutInitFailed(adUnit);
                    }
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public void onInitFailed(String reason) {
        try {
            this.mLoggerManager.log(IronSourceTag.API, "onInitFailed(reason:" + reason + ")", 1);
            if (this.mListenersWrapper != null) {
                for (AD_UNIT adUnit : this.mRequestedAdUnits) {
                    notifyPublisherAboutInitFailed(adUnit);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifyPublisherAboutInitFailed(AD_UNIT adUnit) {
        switch (AnonymousClass1.$SwitchMap$com$ironsource$mediationsdk$IronSource$AD_UNIT[adUnit.ordinal()]) {
            case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                this.mListenersWrapper.onRewardedVideoAvailabilityChanged(false);
                return;
            case Cocos2dxEditBox.kEndActionReturn /*3*/:
                this.mListenersWrapper.onOfferwallAvailable(false);
                return;
            default:
                return;
        }
    }

    private void prepareEventManagers(Activity activity) {
        if (this.mEventManagersInit != null && this.mEventManagersInit.compareAndSet(false, true)) {
            SuperLooper.getLooper().post(new GeneralPropertiesWorker(activity.getApplicationContext()));
            InterstitialEventsManager.getInstance().start(activity.getApplicationContext());
            RewardedVideoEventsManager.getInstance().start(activity.getApplicationContext());
        }
    }

    public synchronized void addToAdaptersList(AbstractAdapter adapter) {
        if (!(this.mRVAdaptersList == null || adapter == null || this.mRVAdaptersList.contains(adapter))) {
            this.mRVAdaptersList.add(adapter);
        }
    }

    public synchronized void addToISAdaptersList(AbstractAdapter adapter) {
        if (!(this.mISAdaptersList == null || adapter == null || this.mISAdaptersList.contains(adapter))) {
            this.mISAdaptersList.add(adapter);
        }
    }

    public synchronized void addToBannerAdaptersList(AbstractAdapter adapter) {
        if (!(this.mBannerAdaptersList == null || adapter == null || this.mBannerAdaptersList.contains(adapter))) {
            this.mBannerAdaptersList.add(adapter);
        }
    }

    public synchronized AbstractAdapter getExistingAdapter(String providerName) {
        AbstractAdapter adapter;
        try {
            Iterator it;
            if (this.mRVAdaptersList != null) {
                it = this.mRVAdaptersList.iterator();
                while (it.hasNext()) {
                    adapter = (AbstractAdapter) it.next();
                    if (adapter.getProviderName().equals(providerName)) {
                        break;
                    }
                }
            }
            if (this.mISAdaptersList != null) {
                it = this.mISAdaptersList.iterator();
                while (it.hasNext()) {
                    adapter = (AbstractAdapter) it.next();
                    if (adapter.getProviderName().equals(providerName)) {
                        break;
                    }
                }
            }
            if (this.mBannerAdaptersList != null) {
                it = this.mBannerAdaptersList.iterator();
                while (it.hasNext()) {
                    adapter = (AbstractAdapter) it.next();
                    if (adapter.getProviderName().equals(providerName)) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            this.mLoggerManager.log(IronSourceTag.INTERNAL, "getExistingAdapter exception: " + e, 1);
        }
        adapter = null;
        return adapter;
    }

    private void initializeManagers() {
        this.mLoggerManager = IronSourceLoggerManager.getLogger(0);
        this.mPublisherLogger = new PublisherLogger(null, 1);
        this.mLoggerManager.addLogger(this.mPublisherLogger);
        this.mListenersWrapper = new ListenersWrapper();
        this.mRewardedVideoManager = new RewardedVideoManager();
        this.mRewardedVideoManager.setRewardedVideoListener(this.mListenersWrapper);
        this.mInterstitialManager = new InterstitialManager();
        this.mInterstitialManager.setInterstitialListener(this.mListenersWrapper);
        this.mInterstitialManager.setRewardedInterstitialListener(this.mListenersWrapper);
        this.mOfferwallManager = new OfferwallManager();
        this.mOfferwallManager.setInternalOfferwallListener(this.mListenersWrapper);
        this.mBannerManager = new BannerManager();
    }

    public void onResume(Activity activity) {
        String logMessage = "onResume()";
        try {
            this.mActivity = activity;
            this.mLoggerManager.log(IronSourceTag.API, logMessage, 1);
            if (this.mRewardedVideoManager != null) {
                this.mRewardedVideoManager.onResume(activity);
            }
            Iterator it = this.mRVAdaptersList.iterator();
            while (it.hasNext()) {
                ((AbstractAdapter) it.next()).onResume(activity);
            }
            if (this.mInterstitialManager != null) {
                this.mInterstitialManager.onResume(activity);
            }
            it = this.mISAdaptersList.iterator();
            while (it.hasNext()) {
                ((AbstractAdapter) it.next()).onResume(activity);
            }
            if (this.mBannerManager != null) {
                this.mBannerManager.onResume(activity);
            }
            it = this.mBannerAdaptersList.iterator();
            while (it.hasNext()) {
                ((AbstractAdapter) it.next()).onResume(activity);
            }
        } catch (Throwable e) {
            this.mLoggerManager.logException(IronSourceTag.API, logMessage, e);
        }
    }

    public void onPause(Activity activity) {
        String logMessage = "onPause()";
        try {
            this.mLoggerManager.log(IronSourceTag.API, logMessage, 1);
            Iterator it = this.mRVAdaptersList.iterator();
            while (it.hasNext()) {
                ((AbstractAdapter) it.next()).onPause(activity);
            }
            it = this.mISAdaptersList.iterator();
            while (it.hasNext()) {
                ((AbstractAdapter) it.next()).onPause(activity);
            }
            it = this.mBannerAdaptersList.iterator();
            while (it.hasNext()) {
                ((AbstractAdapter) it.next()).onPause(activity);
            }
        } catch (Throwable e) {
            this.mLoggerManager.logException(IronSourceTag.API, logMessage, e);
        }
    }

    public synchronized void setAge(int age) {
        try {
            this.mLoggerManager.log(IronSourceTag.API, this.TAG + ":setAge(age:" + age + ")", 1);
            ConfigValidationResult result = new ConfigValidationResult();
            validateAge(age, result);
            if (result.isValid()) {
                this.mUserAge = Integer.valueOf(age);
            } else {
                IronSourceLoggerManager.getLogger().log(IronSourceTag.API, result.getIronSourceError().toString(), 2);
            }
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceTag.API, this.TAG + ":setAge(age:" + age + ")", e);
        }
    }

    public synchronized void setGender(String gender) {
        try {
            this.mLoggerManager.log(IronSourceTag.API, this.TAG + ":setGender(gender:" + gender + ")", 1);
            ConfigValidationResult result = new ConfigValidationResult();
            validateGender(gender, result);
            if (result.isValid()) {
                this.mUserGender = gender;
            } else {
                IronSourceLoggerManager.getLogger().log(IronSourceTag.API, result.getIronSourceError().toString(), 2);
            }
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceTag.API, this.TAG + ":setGender(gender:" + gender + ")", e);
        }
    }

    public void setMediationSegment(String segment) {
        try {
            this.mLoggerManager.log(IronSourceTag.API, this.TAG + ":setMediationSegment(segment:" + segment + ")", 1);
            ConfigValidationResult result = new ConfigValidationResult();
            validateSegment(segment, result);
            if (result.isValid()) {
                this.mSegment = segment;
            } else {
                IronSourceLoggerManager.getLogger().log(IronSourceTag.API, result.getIronSourceError().toString(), 2);
            }
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceTag.API, this.TAG + ":setMediationSegment(segment:" + segment + ")", e);
        }
    }

    public boolean setDynamicUserId(String dynamicUserId) {
        try {
            this.mLoggerManager.log(IronSourceTag.API, this.TAG + ":setDynamicUserId(dynamicUserId:" + dynamicUserId + ")", 1);
            ConfigValidationResult result = new ConfigValidationResult();
            validateDynamicUserId(dynamicUserId, result);
            if (result.isValid()) {
                this.mDynamicUserId = dynamicUserId;
                return true;
            }
            IronSourceLoggerManager.getLogger().log(IronSourceTag.API, result.getIronSourceError().toString(), 2);
            return false;
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceTag.API, this.TAG + ":setDynamicUserId(dynamicUserId:" + dynamicUserId + ")", e);
            return false;
        }
    }

    public void setAdaptersDebug(boolean enabled) {
        IronSourceLoggerManager.getLogger().setAdaptersDebug(enabled);
    }

    public void setMediationType(String mediationType) {
        try {
            this.mLoggerManager.log(IronSourceTag.INTERNAL, this.TAG + ":setMediationType(mediationType:" + mediationType + ")", 1);
            if (validateLength(mediationType, 1, 64) && validateAlphanumeric(mediationType)) {
                this.mMediationType = mediationType;
                return;
            }
            this.mLoggerManager.log(IronSourceTag.INTERNAL, " mediationType value is invalid - should be alphanumeric and 1-64 chars in length", 1);
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceTag.API, this.TAG + ":setMediationType(mediationType:" + mediationType + ")", e);
        }
    }

    public synchronized Integer getAge() {
        return this.mUserAge;
    }

    public synchronized String getGender() {
        return this.mUserGender;
    }

    public synchronized String getMediationSegment() {
        return this.mSegment;
    }

    public synchronized String getDynamicUserId() {
        return this.mDynamicUserId;
    }

    public synchronized String getMediationType() {
        return this.mMediationType;
    }

    public void initRewardedVideo(Activity activity, String appKey, String userId) {
    }

    public void initInterstitial(Activity activity, String appKey, String userId) {
    }

    public void initOfferwall(Activity activity, String appKey, String userId) {
    }

    private boolean isRewardedVideoConfigurationsReady() {
        return (this.currentServerResponse == null || this.currentServerResponse.getConfigurations() == null || this.currentServerResponse.getConfigurations().getRewardedVideoConfigurations() == null) ? false : true;
    }

    public void showRewardedVideo() {
        String logMessage = "showRewardedVideo()";
        try {
            this.mLoggerManager.log(IronSourceTag.API, logMessage, 1);
            if (isRewardedVideoConfigurationsReady()) {
                Placement defaultPlacement = this.currentServerResponse.getConfigurations().getRewardedVideoConfigurations().getDefaultRewardedVideoPlacement();
                if (defaultPlacement != null) {
                    showRewardedVideo(defaultPlacement.getPlacementName());
                    return;
                }
                return;
            }
            this.mListenersWrapper.onRewardedVideoAdShowFailed(ErrorBuilder.buildInitFailedError("showRewardedVideo can't be called before the Rewarded Video ad unit initialization completed successfully", IronSourceConstants.REWARDED_VIDEO_AD_UNIT));
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceTag.API, logMessage, e);
            this.mListenersWrapper.onRewardedVideoAdShowFailed(ErrorBuilder.buildInitFailedError("showRewardedVideo can't be called before the Rewarded Video ad unit initialization completed successfully", IronSourceConstants.REWARDED_VIDEO_AD_UNIT));
        }
    }

    public void showRewardedVideo(String placementName) {
        String logMessage = "showRewardedVideo(" + placementName + ")";
        this.mLoggerManager.log(IronSourceTag.API, logMessage, 1);
        if (isRewardedVideoConfigurationsReady()) {
            Placement placement = this.currentServerResponse.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoPlacement(placementName);
            if (placement == null) {
                this.mLoggerManager.log(IronSourceTag.API, "Placement is not valid, please make sure you are using the right placements, using the default placement.", 3);
                placement = this.currentServerResponse.getConfigurations().getRewardedVideoConfigurations().getDefaultRewardedVideoPlacement();
                if (placement == null) {
                    this.mLoggerManager.log(IronSourceTag.API, "Default placement was not found, please make sure you are using the right placements.", 3);
                    return;
                }
            }
            try {
                String cappedMessage = getCappingMessage(placement.getPlacementName(), getRewardedVideoCappingStatus(placement.getPlacementName()));
                if (TextUtils.isEmpty(cappedMessage)) {
                    JSONObject data = IronSourceUtils.getMediationAdditionalData();
                    try {
                        data.put("placement", placement.getPlacementName());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    RewardedVideoEventsManager.getInstance().log(new EventData(2, data));
                    this.mRewardedVideoManager.showRewardedVideo(placement.getPlacementName());
                    return;
                }
                this.mLoggerManager.log(IronSourceTag.API, cappedMessage, 1);
                this.mListenersWrapper.onRewardedVideoAdShowFailed(ErrorBuilder.buildCappedError(IronSourceConstants.REWARDED_VIDEO_AD_UNIT, cappedMessage));
                return;
            } catch (Exception e2) {
                this.mLoggerManager.logException(IronSourceTag.API, logMessage, e2);
                this.mListenersWrapper.onRewardedVideoAdShowFailed(ErrorBuilder.buildInitFailedError("showRewardedVideo can't be called before the Rewarded Video ad unit initialization completed successfully", IronSourceConstants.REWARDED_VIDEO_AD_UNIT));
                return;
            }
        }
        this.mListenersWrapper.onRewardedVideoAdShowFailed(ErrorBuilder.buildInitFailedError("showRewardedVideo can't be called before the Rewarded Video ad unit initialization completed successfully", IronSourceConstants.REWARDED_VIDEO_AD_UNIT));
    }

    public boolean isRewardedVideoAvailable() {
        JSONObject data;
        boolean isAvailable = false;
        try {
            isAvailable = this.mRewardedVideoManager.isRewardedVideoAvailable();
            data = IronSourceUtils.getMediationAdditionalData();
            data.put(ParametersKeys.VIDEO_STATUS, String.valueOf(isAvailable));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Throwable e2) {
            this.mLoggerManager.log(IronSourceTag.API, "isRewardedVideoAvailable():" + isAvailable, 1);
            this.mLoggerManager.logException(IronSourceTag.API, "isRewardedVideoAvailable()", e2);
            return false;
        }
        RewardedVideoEventsManager.getInstance().log(new EventData(18, data));
        this.mLoggerManager.log(IronSourceTag.API, "isRewardedVideoAvailable():" + isAvailable, 1);
        return isAvailable;
    }

    public void setRewardedVideoListener(RewardedVideoListener listener) {
        if (listener == null) {
            this.mLoggerManager.log(IronSourceTag.API, "setRewardedVideoListener(RVListener:null)", 1);
        } else {
            this.mLoggerManager.log(IronSourceTag.API, "setRewardedVideoListener(RVListener)", 1);
        }
        this.mListenersWrapper.setRewardedVideoListener(listener);
    }

    public void loadInterstitial() {
        String logMessage = "loadInterstitial()";
        this.mLoggerManager.log(IronSourceTag.API, logMessage, 1);
        try {
            this.mInterstitialManager.loadInterstitial();
        } catch (Throwable e) {
            this.mLoggerManager.logException(IronSourceTag.API, logMessage, e);
        }
    }

    private boolean isInterstitialConfigurationsReady() {
        return (this.currentServerResponse == null || this.currentServerResponse.getConfigurations() == null || this.currentServerResponse.getConfigurations().getInterstitialConfigurations() == null) ? false : true;
    }

    public void showInterstitial() {
        String logMessage = "showInterstitial()";
        try {
            this.mLoggerManager.log(IronSourceTag.API, logMessage, 1);
            if (isInterstitialConfigurationsReady()) {
                InterstitialPlacement defaultPlacement = this.currentServerResponse.getConfigurations().getInterstitialConfigurations().getDefaultInterstitialPlacement();
                if (defaultPlacement != null) {
                    showInterstitial(defaultPlacement.getPlacementName());
                    return;
                }
                return;
            }
            this.mListenersWrapper.onInterstitialAdShowFailed(ErrorBuilder.buildInitFailedError("showInterstitial can't be called before the Interstitial ad unit initialization completed successfully", ParametersKeys.INTERSTITIAL));
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceTag.API, logMessage, e);
            this.mListenersWrapper.onInterstitialAdShowFailed(ErrorBuilder.buildInitFailedError("showInterstitial can't be called before the Interstitial ad unit initialization completed successfully", ParametersKeys.INTERSTITIAL));
        }
    }

    public void showInterstitial(String placementName) {
        String logMessage = "showInterstitial(" + placementName + ")";
        this.mLoggerManager.log(IronSourceTag.API, logMessage, 1);
        if (isInterstitialConfigurationsReady()) {
            InterstitialPlacement placement = this.currentServerResponse.getConfigurations().getInterstitialConfigurations().getInterstitialPlacement(placementName);
            if (placement == null) {
                this.mLoggerManager.log(IronSourceTag.API, "Placement is not valid, please make sure you are using the right placements, using the default placement.", 3);
                placement = this.currentServerResponse.getConfigurations().getInterstitialConfigurations().getDefaultInterstitialPlacement();
                if (placement == null) {
                    this.mLoggerManager.log(IronSourceTag.API, "Default placement was not found, please make sure you are using the right placements.", 3);
                    return;
                }
            }
            try {
                String cappedMessage = getCappingMessage(placement.getPlacementName(), getInterstitialCappingStatus(placement.getPlacementName()));
                if (TextUtils.isEmpty(cappedMessage)) {
                    JSONObject data = IronSourceUtils.getMediationAdditionalData();
                    try {
                        data.put("placement", placement.getPlacementName());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    InterstitialEventsManager.getInstance().log(new EventData(23, data));
                    this.mInterstitialManager.showInterstitial(placement.getPlacementName());
                    return;
                }
                this.mLoggerManager.log(IronSourceTag.API, cappedMessage, 1);
                this.mListenersWrapper.onInterstitialAdShowFailed(ErrorBuilder.buildCappedError(ParametersKeys.INTERSTITIAL, cappedMessage));
                return;
            } catch (Exception e2) {
                this.mLoggerManager.logException(IronSourceTag.API, logMessage, e2);
                this.mListenersWrapper.onInterstitialAdShowFailed(ErrorBuilder.buildInitFailedError("showInterstitial can't be called before the Interstitial ad unit initialization completed successfully", ParametersKeys.INTERSTITIAL));
                return;
            }
        }
        this.mListenersWrapper.onInterstitialAdShowFailed(ErrorBuilder.buildInitFailedError("showInterstitial can't be called before the Interstitial ad unit initialization completed successfully", ParametersKeys.INTERSTITIAL));
    }

    public void setInterstitialListener(InterstitialListener listener) {
        if (listener == null) {
            this.mLoggerManager.log(IronSourceTag.API, "setInterstitialListener(ISListener:null)", 1);
        } else {
            this.mLoggerManager.log(IronSourceTag.API, "setInterstitialListener(ISListener)", 1);
        }
        this.mListenersWrapper.setInterstitialListener(listener);
    }

    private boolean isOfferwallConfigurationsReady() {
        return (this.currentServerResponse == null || this.currentServerResponse.getConfigurations() == null || this.currentServerResponse.getConfigurations().getOfferwallConfigurations() == null) ? false : true;
    }

    public void showOfferwall() {
        String logMessage = "showOfferwall()";
        try {
            this.mLoggerManager.log(IronSourceTag.API, logMessage, 1);
            if (isOfferwallConfigurationsReady()) {
                OfferwallPlacement defaultPlacement = this.currentServerResponse.getConfigurations().getOfferwallConfigurations().getDefaultOfferwallPlacement();
                if (defaultPlacement != null) {
                    showOfferwall(defaultPlacement.getPlacementName());
                    return;
                }
                return;
            }
            this.mListenersWrapper.onOfferwallShowFailed(ErrorBuilder.buildInitFailedError("showOfferwall can't be called before the Offerwall ad unit initialization completed successfully", IronSourceConstants.OFFERWALL_AD_UNIT));
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceTag.API, logMessage, e);
            this.mListenersWrapper.onOfferwallShowFailed(ErrorBuilder.buildInitFailedError("showOfferwall can't be called before the Offerwall ad unit initialization completed successfully", IronSourceConstants.OFFERWALL_AD_UNIT));
        }
    }

    public void showOfferwall(String placementName) {
        String logMessage = "showOfferwall(" + placementName + ")";
        this.mLoggerManager.log(IronSourceTag.API, logMessage, 1);
        try {
            if (isOfferwallConfigurationsReady()) {
                OfferwallPlacement placement = this.currentServerResponse.getConfigurations().getOfferwallConfigurations().getOfferwallPlacement(placementName);
                if (placement == null) {
                    this.mLoggerManager.log(IronSourceTag.API, "Placement is not valid, please make sure you are using the right placements, using the default placement.", 3);
                    placement = this.currentServerResponse.getConfigurations().getOfferwallConfigurations().getDefaultOfferwallPlacement();
                    if (placement == null) {
                        this.mLoggerManager.log(IronSourceTag.API, "Default placement was not found, please make sure you are using the right placements.", 3);
                        return;
                    }
                }
                this.mOfferwallManager.showOfferwall(placement.getPlacementName());
                return;
            }
            this.mListenersWrapper.onOfferwallShowFailed(ErrorBuilder.buildInitFailedError("showOfferwall can't be called before the Offerwall ad unit initialization completed successfully", IronSourceConstants.OFFERWALL_AD_UNIT));
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceTag.API, logMessage, e);
            this.mListenersWrapper.onOfferwallShowFailed(ErrorBuilder.buildInitFailedError("showOfferwall can't be called before the Offerwall ad unit initialization completed successfully", IronSourceConstants.OFFERWALL_AD_UNIT));
        }
    }

    public boolean isOfferwallAvailable() {
        try {
            if (this.mOfferwallManager != null) {
                return this.mOfferwallManager.isOfferwallAvailable();
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public void getOfferwallCredits() {
        String logMessage = "getOfferwallCredits()";
        this.mLoggerManager.log(IronSourceTag.API, logMessage, 1);
        try {
            this.mOfferwallManager.getOfferwallCredits();
        } catch (Throwable e) {
            this.mLoggerManager.logException(IronSourceTag.API, logMessage, e);
        }
    }

    public void setOfferwallListener(OfferwallListener offerwallListener) {
        if (offerwallListener == null) {
            this.mLoggerManager.log(IronSourceTag.API, "setOfferwallListener(OWListener:null)", 1);
        } else {
            this.mLoggerManager.log(IronSourceTag.API, "setOfferwallListener(OWListener)", 1);
        }
        this.mListenersWrapper.setOfferwallListener(offerwallListener);
    }

    public void setLogListener(LogListener logListener) {
        if (logListener == null) {
            this.mLoggerManager.log(IronSourceTag.API, "setLogListener(LogListener:null)", 1);
            return;
        }
        this.mPublisherLogger.setLogListener(logListener);
        this.mLoggerManager.log(IronSourceTag.API, "setLogListener(LogListener:" + logListener.getClass().getSimpleName() + ")", 1);
    }

    public void setRewardedInterstitialListener(RewardedInterstitialListener listener) {
        this.mListenersWrapper.setRewardedInterstitialListener(listener);
    }

    private boolean isBannerConfigurationsReady() {
        return (this.currentServerResponse == null || this.currentServerResponse.getConfigurations() == null || this.currentServerResponse.getConfigurations().getBannerConfigurations() == null) ? false : true;
    }

    public IronSourceBannerLayout createBanner(Activity activity, EBannerSize size) {
        this.mLoggerManager.log(IronSourceTag.API, "createBanner()", 1);
        if (activity != null) {
            return this.mBannerManager.createBanner(activity, size);
        }
        this.mLoggerManager.log(IronSourceTag.API, "createBanner() : Activity cannot be null", 3);
        return null;
    }

    public void loadBanner(IronSourceBannerLayout banner, String placementName) {
        this.mLoggerManager.log(IronSourceTag.API, "loadBanner(" + placementName + ")", 1);
        if (banner == null) {
            this.mLoggerManager.log(IronSourceTag.API, "loadBanner can't be called with a null parameter", 1);
        } else {
            this.mBannerManager.loadBanner(banner, placementName);
        }
    }

    public void loadBanner(IronSourceBannerLayout banner) {
        this.mLoggerManager.log(IronSourceTag.API, "loadBanner()", 1);
        if (banner == null) {
            this.mLoggerManager.log(IronSourceTag.API, "loadBanner can't be called with a null parameter", 1);
        } else {
            loadBanner(banner, null);
        }
    }

    public void destroyBanner(IronSourceBannerLayout banner) {
        String logMessage = "destroyBanner()";
        this.mLoggerManager.log(IronSourceTag.API, logMessage, 1);
        try {
            this.mBannerManager.destroyBanner(banner);
        } catch (Throwable e) {
            this.mLoggerManager.logException(IronSourceTag.API, logMessage, e);
        }
    }

    public ServerResponseWrapper getServerResponse(Context context, String userId) {
        return getServerResponse(context, userId, null);
    }

    public ServerResponseWrapper getServerResponse(Context context, String userId, IResponseListener listener) {
        ServerResponseWrapper serverResponseWrapper;
        synchronized (this.serverResponseLocker) {
            if (this.currentServerResponse != null) {
                serverResponseWrapper = new ServerResponseWrapper(this.currentServerResponse);
            } else {
                serverResponseWrapper = connectAndGetServerResponse(context, userId, listener);
                if (serverResponseWrapper == null || !serverResponseWrapper.isValidResponse()) {
                    serverResponseWrapper = getCachedResponse(context, userId);
                }
                if (serverResponseWrapper != null) {
                    this.currentServerResponse = serverResponseWrapper;
                    IronSourceUtils.saveLastResponse(context, serverResponseWrapper.toString());
                    initializeSettingsFromServerResponse(this.currentServerResponse, context);
                }
                InterstitialEventsManager.getInstance().setHasServerResponse(true);
                RewardedVideoEventsManager.getInstance().setHasServerResponse(true);
            }
        }
        return serverResponseWrapper;
    }

    private ServerResponseWrapper getCachedResponse(Context context, String userId) {
        JSONObject cachedJsonObject;
        try {
            cachedJsonObject = new JSONObject(IronSourceUtils.getLastResponse(context));
        } catch (JSONException e) {
            cachedJsonObject = new JSONObject();
        }
        String cachedAppKey = cachedJsonObject.optString(ServerResponseWrapper.APP_KEY_FIELD);
        String cachedUserId = cachedJsonObject.optString(ServerResponseWrapper.USER_ID_FIELD);
        String cachedSettings = cachedJsonObject.optString(ServerResponseWrapper.RESPONSE_FIELD);
        if (TextUtils.isEmpty(cachedAppKey) || TextUtils.isEmpty(cachedUserId) || TextUtils.isEmpty(cachedSettings) || getIronSourceAppKey() == null || !cachedAppKey.equals(getIronSourceAppKey()) || !cachedUserId.equals(userId)) {
            return null;
        }
        ServerResponseWrapper response = new ServerResponseWrapper(context, cachedAppKey, cachedUserId, cachedSettings);
        IronSourceError sse = ErrorBuilder.buildUsingCachedConfigurationError(cachedAppKey, cachedUserId);
        this.mLoggerManager.log(IronSourceTag.INTERNAL, sse.toString(), 1);
        this.mLoggerManager.log(IronSourceTag.INTERNAL, sse.toString() + ": " + response.toString(), 0);
        return response;
    }

    private ServerResponseWrapper connectAndGetServerResponse(Context context, String userId, IResponseListener listener) {
        if (!IronSourceUtils.isNetworkConnected(context)) {
            return null;
        }
        try {
            String gaid = getAdvertiserId(context);
            if (TextUtils.isEmpty(gaid)) {
                gaid = DeviceStatus.getOrGenerateOnceUniqueIdentifier(context);
                IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, "using custom identifier", 1);
            }
            String serverResponseString = HttpFunctions.getStringFromURL(ServerURL.getCPVProvidersURL(getIronSourceAppKey(), userId, gaid), listener);
            if (serverResponseString == null) {
                return null;
            }
            ServerResponseWrapper response = new ServerResponseWrapper(context, getIronSourceAppKey(), userId, serverResponseString);
            try {
                if (response.isValidResponse()) {
                    return response;
                }
                return null;
            } catch (Exception e) {
                return response;
            }
        } catch (Exception e2) {
            return null;
        }
    }

    private void initializeSettingsFromServerResponse(ServerResponseWrapper response, Context context) {
        initializeLoggerManager(response);
        initializeEventsSettings(response, context);
    }

    private void initializeEventsSettings(ServerResponseWrapper response, Context context) {
        boolean isRVEventsEnabled = false;
        if (isRewardedVideoConfigurationsReady()) {
            isRVEventsEnabled = response.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoEventsConfigurations().isEventsEnabled();
        }
        boolean isISEventsEnabled = false;
        if (isInterstitialConfigurationsReady()) {
            isISEventsEnabled = response.getConfigurations().getInterstitialConfigurations().getInterstitialEventsConfigurations().isEventsEnabled();
        }
        if (isRVEventsEnabled) {
            RewardedVideoEventsManager.getInstance().setFormatterType(response.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoEventsConfigurations().getEventsType(), context);
            RewardedVideoEventsManager.getInstance().setEventsUrl(response.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoEventsConfigurations().getEventsURL(), context);
            RewardedVideoEventsManager.getInstance().setMaxNumberOfEvents(response.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoEventsConfigurations().getMaxNumberOfEvents());
            RewardedVideoEventsManager.getInstance().setMaxEventsPerBatch(response.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoEventsConfigurations().getMaxEventsPerBatch());
            RewardedVideoEventsManager.getInstance().setBackupThreshold(response.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoEventsConfigurations().getEventsBackupThreshold());
            RewardedVideoEventsManager.getInstance().setOptOutEvents(response.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoEventsConfigurations().getOptOutEvents(), context);
        } else {
            RewardedVideoEventsManager.getInstance().setIsEventsEnabled(isRVEventsEnabled);
        }
        if (isISEventsEnabled) {
            InterstitialEventsManager.getInstance().setFormatterType(response.getConfigurations().getInterstitialConfigurations().getInterstitialEventsConfigurations().getEventsType(), context);
            InterstitialEventsManager.getInstance().setEventsUrl(response.getConfigurations().getInterstitialConfigurations().getInterstitialEventsConfigurations().getEventsURL(), context);
            InterstitialEventsManager.getInstance().setMaxNumberOfEvents(response.getConfigurations().getInterstitialConfigurations().getInterstitialEventsConfigurations().getMaxNumberOfEvents());
            InterstitialEventsManager.getInstance().setMaxEventsPerBatch(response.getConfigurations().getInterstitialConfigurations().getInterstitialEventsConfigurations().getMaxEventsPerBatch());
            InterstitialEventsManager.getInstance().setBackupThreshold(response.getConfigurations().getInterstitialConfigurations().getInterstitialEventsConfigurations().getEventsBackupThreshold());
            InterstitialEventsManager.getInstance().setOptOutEvents(response.getConfigurations().getInterstitialConfigurations().getInterstitialEventsConfigurations().getOptOutEvents(), context);
            return;
        }
        InterstitialEventsManager.getInstance().setIsEventsEnabled(isISEventsEnabled);
    }

    private void initializeLoggerManager(ServerResponseWrapper response) {
        this.mPublisherLogger.setDebugLevel(response.getConfigurations().getApplicationConfigurations().getLoggerConfigurations().getPublisherLoggerLevel());
        this.mLoggerManager.setLoggerDebugLevel(ConsoleLogger.NAME, response.getConfigurations().getApplicationConfigurations().getLoggerConfigurations().getConsoleLoggerLevel());
        this.mLoggerManager.setLoggerDebugLevel(ServerLogger.NAME, response.getConfigurations().getApplicationConfigurations().getLoggerConfigurations().getServerLoggerLevel());
    }

    public void removeRewardedVideoListener() {
        this.mLoggerManager.log(IronSourceTag.API, "removeRewardedVideoListener()", 1);
        this.mListenersWrapper.setRewardedVideoListener(null);
    }

    public void removeInterstitialListener() {
        this.mLoggerManager.log(IronSourceTag.API, "removeInterstitialListener()", 1);
        this.mListenersWrapper.setInterstitialListener(null);
    }

    public void removeOfferwallListener() {
        this.mLoggerManager.log(IronSourceTag.API, "removeOfferwallListener()", 1);
        this.mListenersWrapper.setOfferwallListener(null);
    }

    public synchronized void setIronSourceAppKey(String appKey) {
        if (this.mAppKey == null) {
            this.mAppKey = appKey;
        }
    }

    public synchronized void setIronSourceUserId(String userId) {
        this.mUserId = userId;
    }

    public synchronized String getIronSourceAppKey() {
        return this.mAppKey;
    }

    public synchronized String getIronSourceUserId() {
        return this.mUserId;
    }

    private ConfigValidationResult validateAppKey(String appKey) {
        ConfigValidationResult result = new ConfigValidationResult();
        if (appKey == null) {
            result.setInvalid(ErrorBuilder.buildInvalidCredentialsError(ServerResponseWrapper.APP_KEY_FIELD, appKey, "it's missing"));
        } else if (!validateLength(appKey, 5, 10)) {
            result.setInvalid(ErrorBuilder.buildInvalidCredentialsError(ServerResponseWrapper.APP_KEY_FIELD, appKey, "length should be between 5-10 characters"));
        } else if (!validateAlphanumeric(appKey)) {
            result.setInvalid(ErrorBuilder.buildInvalidCredentialsError(ServerResponseWrapper.APP_KEY_FIELD, appKey, "should contain only english characters and numbers"));
        }
        return result;
    }

    private void validateGender(String gender, ConfigValidationResult result) {
        if (gender != null) {
            try {
                gender = gender.toLowerCase().trim();
                if (!Gender.MALE.equals(gender) && !Gender.FEMALE.equals(gender) && !Gender.UNKNOWN.equals(gender)) {
                    result.setInvalid(ErrorBuilder.buildInvalidKeyValueError("gender", IronSourceConstants.IRONSOURCE_CONFIG_NAME, "gender value should be one of male/female/unknown."));
                }
            } catch (Exception e) {
                result.setInvalid(ErrorBuilder.buildInvalidKeyValueError("gender", IronSourceConstants.IRONSOURCE_CONFIG_NAME, "gender value should be one of male/female/unknown."));
            }
        }
    }

    private void validateAge(int age, ConfigValidationResult result) {
        if (age < 5 || age > 120) {
            try {
                result.setInvalid(ErrorBuilder.buildInvalidKeyValueError("age", IronSourceConstants.IRONSOURCE_CONFIG_NAME, "age value should be between 5-120"));
            } catch (NumberFormatException e) {
                result.setInvalid(ErrorBuilder.buildInvalidKeyValueError("age", IronSourceConstants.IRONSOURCE_CONFIG_NAME, "age value should be between 5-120"));
            }
        }
    }

    private void validateSegment(String segment, ConfigValidationResult result) {
        if (segment != null) {
            try {
                if (segment.length() > 64) {
                    result.setInvalid(ErrorBuilder.buildInvalidKeyValueError("segment", IronSourceConstants.IRONSOURCE_CONFIG_NAME, "segment value should not exceed 64 characters."));
                }
            } catch (Exception e) {
                result.setInvalid(ErrorBuilder.buildInvalidKeyValueError("segment", IronSourceConstants.IRONSOURCE_CONFIG_NAME, "segment value should not exceed 64 characters."));
            }
        }
    }

    private void validateDynamicUserId(String dynamicUserId, ConfigValidationResult result) {
        if (!validateLength(dynamicUserId, 1, 64) || !validateAlphanumeric(dynamicUserId)) {
            result.setInvalid(ErrorBuilder.buildInvalidKeyValueError("dynamicUserId", IronSourceConstants.IRONSOURCE_CONFIG_NAME, "dynamicUserId is invalid, should be alphanumeric and between 1-64 chars in length."));
        }
    }

    private boolean validateLength(String key, int minLength, int maxLength) {
        if (key != null && key.length() >= minLength && key.length() <= maxLength) {
            return true;
        }
        return false;
    }

    private boolean validateAlphanumeric(String key) {
        if (key == null) {
            return false;
        }
        return key.matches("^[a-zA-Z0-9]*$");
    }

    public InterstitialPlacement getInterstitialPlacementInfo(String placementName) {
        InterstitialPlacement result = null;
        try {
            result = this.currentServerResponse.getConfigurations().getInterstitialConfigurations().getInterstitialPlacement(placementName);
            this.mLoggerManager.log(IronSourceTag.API, "getPlacementInfo(placement: " + placementName + "):" + result, 1);
            return result;
        } catch (Exception e) {
            return result;
        }
    }

    public Placement getRewardedVideoPlacementInfo(String placementName) {
        Placement result = null;
        try {
            result = this.currentServerResponse.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoPlacement(placementName);
            this.mLoggerManager.log(IronSourceTag.API, "getPlacementInfo(placement: " + placementName + "):" + result, 1);
            return result;
        } catch (Exception e) {
            return result;
        }
    }

    public String getAdvertiserId(Context context) {
        try {
            String[] deviceInfo = DeviceStatus.getAdvertisingIdInfo(context);
            if (deviceInfo.length <= 0 || deviceInfo[0] == null) {
                return BuildConfig.FLAVOR;
            }
            return deviceInfo[0];
        } catch (Exception e) {
            return BuildConfig.FLAVOR;
        }
    }

    public void shouldTrackNetworkState(Context context, boolean track) {
        if (this.mRewardedVideoManager != null) {
            this.mRewardedVideoManager.shouldTrackNetworkState(context, track);
        }
        if (this.mInterstitialManager != null) {
            this.mInterstitialManager.shouldTrackNetworkState(context, track);
        }
        if (this.mBannerManager != null) {
            this.mBannerManager.shouldTrackNetworkState(context, track);
        }
    }

    public boolean isInterstitialReady() {
        JSONObject data;
        boolean isAvailable = false;
        try {
            isAvailable = this.mInterstitialManager.isInterstitialReady();
            data = IronSourceUtils.getMediationAdditionalData();
            data.put(ParametersKeys.VIDEO_STATUS, String.valueOf(isAvailable));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Throwable e2) {
            this.mLoggerManager.log(IronSourceTag.API, "isInterstitialReady():" + isAvailable, 1);
            this.mLoggerManager.logException(IronSourceTag.API, "isInterstitialReady()", e2);
            return false;
        }
        InterstitialEventsManager.getInstance().log(new EventData(30, data));
        this.mLoggerManager.log(IronSourceTag.API, "isInterstitialReady():" + isAvailable, 1);
        return isAvailable;
    }

    public boolean isInterstitialPlacementCapped(String placementName) {
        boolean isCapped = false;
        ECappingStatus cappingStatus = getInterstitialCappingStatus(placementName);
        if (cappingStatus != null) {
            switch (AnonymousClass1.$SwitchMap$com$ironsource$mediationsdk$utils$CappingManager$ECappingStatus[cappingStatus.ordinal()]) {
                case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                case R.styleable.View_paddingStart /*2*/:
                case Cocos2dxEditBox.kEndActionReturn /*3*/:
                    isCapped = true;
                    break;
            }
        }
        sendIsCappedEvent(ParametersKeys.INTERSTITIAL, isCapped);
        return isCapped;
    }

    public boolean isRewardedVideoPlacementCapped(String placementName) {
        boolean isCapped = false;
        ECappingStatus cappingStatus = getRewardedVideoCappingStatus(placementName);
        if (cappingStatus != null) {
            switch (AnonymousClass1.$SwitchMap$com$ironsource$mediationsdk$utils$CappingManager$ECappingStatus[cappingStatus.ordinal()]) {
                case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                case R.styleable.View_paddingStart /*2*/:
                case Cocos2dxEditBox.kEndActionReturn /*3*/:
                    isCapped = true;
                    break;
            }
        }
        sendIsCappedEvent(IronSourceConstants.REWARDED_VIDEO_AD_UNIT, isCapped);
        return isCapped;
    }

    public boolean isBannerPlacementCapped(String placementName) {
        boolean isCapped = false;
        ECappingStatus cappingStatus = getBannerCappingStatus(placementName);
        if (cappingStatus != null) {
            switch (AnonymousClass1.$SwitchMap$com$ironsource$mediationsdk$utils$CappingManager$ECappingStatus[cappingStatus.ordinal()]) {
                case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                case R.styleable.View_paddingStart /*2*/:
                case Cocos2dxEditBox.kEndActionReturn /*3*/:
                    isCapped = true;
                    break;
            }
        }
        sendIsCappedEvent(IronSourceConstants.BANNER_AD_UNIT, isCapped);
        return isCapped;
    }

    private ECappingStatus getInterstitialCappingStatus(String placementName) {
        if (this.mInterstitialManager == null) {
            return ECappingStatus.NOT_CAPPED;
        }
        InterstitialPlacement placement = this.mInterstitialManager.getPlacementByName(placementName);
        if (placement == null) {
            return ECappingStatus.NOT_CAPPED;
        }
        return CappingManager.isPlacementCapped(this.mActivity, placement);
    }

    private ECappingStatus getRewardedVideoCappingStatus(String placementName) {
        if (this.mRewardedVideoManager == null) {
            return ECappingStatus.NOT_CAPPED;
        }
        Placement placement = this.mRewardedVideoManager.getPlacementByName(placementName);
        if (placement == null) {
            return ECappingStatus.NOT_CAPPED;
        }
        return CappingManager.isPlacementCapped(this.mActivity, placement);
    }

    public ECappingStatus getBannerCappingStatus(String placementName) {
        if (this.mBannerManager == null) {
            return ECappingStatus.NOT_CAPPED;
        }
        BannerPlacement placement = this.mBannerManager.getPlacementByName(placementName);
        if (placement == null) {
            return ECappingStatus.NOT_CAPPED;
        }
        return CappingManager.isPlacementCapped(this.mActivity, placement);
    }

    private void sendIsCappedEvent(String adUnit, boolean isCapped) {
        if (isCapped) {
            JSONObject data = IronSourceUtils.getMediationAdditionalData();
            try {
                data.put("reason", isCapped ? 1 : 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (ParametersKeys.INTERSTITIAL.equals(adUnit)) {
                InterstitialEventsManager.getInstance().log(new EventData(34, data));
            } else if (IronSourceConstants.REWARDED_VIDEO_AD_UNIT.equals(adUnit)) {
                RewardedVideoEventsManager.getInstance().log(new EventData(20, data));
            } else if (IronSourceConstants.BANNER_AD_UNIT.equals(adUnit)) {
                InterstitialEventsManager.getInstance().log(new EventData(HttpStatus.SC_REQUEST_URI_TOO_LONG, data));
            }
        }
    }

    public String getCappingMessage(String placementName, ECappingStatus cappingStatus) {
        if (cappingStatus == null) {
            return null;
        }
        switch (AnonymousClass1.$SwitchMap$com$ironsource$mediationsdk$utils$CappingManager$ECappingStatus[cappingStatus.ordinal()]) {
            case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                return "Placement " + placementName + " is capped by disabled delivery";
            case R.styleable.View_paddingStart /*2*/:
                return "Placement " + placementName + " has reached its capping limit";
            case Cocos2dxEditBox.kEndActionReturn /*3*/:
                return "Placement " + placementName + " has reached its limit as defined per pace";
            default:
                return null;
        }
    }

    public ServerResponseWrapper getCurrentServerResponse() {
        return this.currentServerResponse;
    }
}
