package com.ironsource.mediationsdk.utils;

import android.content.Context;
import android.text.TextUtils;
import com.facebook.internal.NativeProtocol;
import com.ironsource.environment.DeviceStatus;
import com.ironsource.mediationsdk.IronSource.AD_UNIT;
import com.ironsource.mediationsdk.logger.ConsoleLogger;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.logger.ServerLogger;
import com.ironsource.mediationsdk.model.ApplicationConfigurations;
import com.ironsource.mediationsdk.model.ApplicationEvents;
import com.ironsource.mediationsdk.model.ApplicationLogger;
import com.ironsource.mediationsdk.model.BannerConfigurations;
import com.ironsource.mediationsdk.model.BannerPlacement;
import com.ironsource.mediationsdk.model.Configurations;
import com.ironsource.mediationsdk.model.InterstitialConfigurations;
import com.ironsource.mediationsdk.model.InterstitialPlacement;
import com.ironsource.mediationsdk.model.OfferwallConfigurations;
import com.ironsource.mediationsdk.model.OfferwallPlacement;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.model.PlacementAvailabilitySettings;
import com.ironsource.mediationsdk.model.PlacementAvailabilitySettings.PlacementAvailabilitySettingsBuilder;
import com.ironsource.mediationsdk.model.PlacementCappingType;
import com.ironsource.mediationsdk.model.ProviderOrder;
import com.ironsource.mediationsdk.model.ProviderSettings;
import com.ironsource.mediationsdk.model.ProviderSettingsHolder;
import com.ironsource.mediationsdk.model.RewardedVideoConfigurations;
import com.ironsource.mediationsdk.sdk.GeneralProperties;
import com.ironsource.sdk.precache.DownloadManager;
import com.ironsource.sdk.utils.Constants;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import cz.msebera.android.httpclient.HttpStatus;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.cocos2dx.lib.BuildConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerResponseWrapper {
    public static final String APP_KEY_FIELD = "appKey";
    public static final String RESPONSE_FIELD = "response";
    public static final String USER_ID_FIELD = "userId";
    private final String ADAPTER_TIMEOUT_IN_MILLIS_FIELD = "atim";
    private final String ADAPTER_TIMEOUT_IN_SECS_FIELD = "adapterTimeOutInSeconds";
    private final String AD_UNITS_FIELD = "adUnits";
    private final String ALLOW_LOCATION = "allowLocation";
    private final String APPLICATION_FIELD = ParametersKeys.ORIENTATION_APPLICATION;
    private final String BACKFILL_FIELD = "backFill";
    private final String BACKUP_THRESHOLD_FIELD = "backupThreshold";
    private final String BN_FIELD = "banner";
    private final String CONFIGURATIONS_FIELD = "configurations";
    private final String CONSOLE_FIELD = ConsoleLogger.NAME;
    private final int DEFAULT_ADAPTERS_SMARTLOAD_AMOUNT = 2;
    private final int DEFAULT_ADAPTERS_SMARTLOAD_TIMEOUT = 60;
    private final String DEFAULT_ADAPTER_ALGORITHM = "KTO";
    private final int DEFAULT_BANNER_SMARTLOAD_TIMEOUT = HttpStatus.SC_OK;
    private final int DEFAULT_LOG_LEVEL = 3;
    private final int DEFAULT_MAX_EVENTS_PER_BATCH = DownloadManager.OPERATION_TIMEOUT;
    private final String ERROR_KEY = NativeProtocol.BRIDGE_ARG_ERROR_BUNDLE;
    private final String EVENTS_FIELD = EventEntry.TABLE_NAME;
    private final String IS_FIELD = "interstitial";
    private final String LOGGERS_FIELD = "loggers";
    private final String MAX_EVENTS_PER_BATCH = "maxEventsPerBatch";
    private final String MAX_NUM_OF_ADAPTERS_TO_LOAD_ON_START_FIELD = "maxNumOfAdaptersToLoadOnStart";
    private final String MAX_NUM_OF_EVENTS_FIELD = "maxNumberOfEvents";
    private final String OPT_OUT_EVENTS_FIELD = "optOut";
    private final String OW_FIELD = "offerwall";
    private final String PLACEMENTS_FIELD = "placements";
    private final String PLACEMENT_ID_FIELD = Constants.PLACEMENT_ID;
    private final String PLACEMENT_NAME_FIELD = "placementName";
    private final String PLACEMENT_SETTINGS_CAPPING_FIELD = "capping";
    private final String PLACEMENT_SETTINGS_CAPPING_UNIT_FIELD = "unit";
    private final String PLACEMENT_SETTINGS_CAPPING_VALUE_FIELD = "maxImpressions";
    private final String PLACEMENT_SETTINGS_DELIVERY_FIELD = "delivery";
    private final String PLACEMENT_SETTINGS_ENABLED_FIELD = "enabled";
    private final String PLACEMENT_SETTINGS_PACING_FIELD = "pacing";
    private final String PLACEMENT_SETTINGS_PACING_VALUE_FIELD = "numOfSeconds";
    private final String PREMIUM_FIELD = "premium";
    private final String PROVIDER_LOAD_NAME_FIELD = "providerLoadName";
    private final String PROVIDER_ORDER_FIELD = "providerOrder";
    private final String PROVIDER_SETTINGS_FIELD = "providerSettings";
    private final String PUBLISHER_FIELD = "publisher";
    private final String RV_FIELD = "rewardedVideo";
    private final String SEND_EVENTS_TOGGLE_FIELD = "sendEventsToggle";
    private final String SEND_ULTRA_EVENTS_FIELD = "sendUltraEvents";
    private final String SERVER_EVENTS_TYPE = "serverEventsType";
    private final String SERVER_EVENTS_URL_FIELD = "serverEventsURL";
    private final String SERVER_FIELD = ServerLogger.NAME;
    private final String UUID_ENABLED_FIELD = DeviceStatus.UUID_ENABLED;
    private final String VIRTUAL_ITEM_COUNT_FIELD = "virtualItemCount";
    private final String VIRTUAL_ITEM_NAME_FIELD = "virtualItemName";
    private String mAppKey;
    private int mBannerLoadPosition = -1;
    private Configurations mConfigurations;
    private Context mContext;
    private int mISLoadPosition = -1;
    private int mMaxBannerAdapters;
    private int mMaxISAdapters;
    private int mMaxRVAdapters;
    private ProviderOrder mProviderOrder;
    private ProviderSettingsHolder mProviderSettingsHolder;
    private int mRVLoadPosition = -1;
    private JSONObject mResponse;
    private String mUserId;

    public ServerResponseWrapper(Context context, String appKey, String userId, String jsonData) {
        this.mContext = context;
        try {
            if (TextUtils.isEmpty(jsonData)) {
                this.mResponse = new JSONObject();
            } else {
                this.mResponse = new JSONObject(jsonData);
            }
            parseProviderSettings();
            parseConfigurations();
            parseProviderOrder();
            this.mMaxRVAdapters = this.mProviderOrder.getRewardedVideoProviderOrder().size();
            this.mMaxISAdapters = this.mProviderOrder.getInterstitialProviderOrder().size();
            this.mMaxBannerAdapters = this.mProviderOrder.getBannerProviderOrder().size();
            if (TextUtils.isEmpty(appKey)) {
                appKey = BuildConfig.FLAVOR;
            }
            this.mAppKey = appKey;
            if (TextUtils.isEmpty(userId)) {
                userId = BuildConfig.FLAVOR;
            }
            this.mUserId = userId;
        } catch (JSONException e) {
            defaultInit();
        }
    }

    public ServerResponseWrapper(ServerResponseWrapper srw) {
        try {
            this.mContext = srw.getContext();
            this.mResponse = new JSONObject(srw.mResponse.toString());
            this.mMaxRVAdapters = srw.getMaxRVAdapters();
            this.mMaxISAdapters = srw.getMaxISAdapters();
            this.mMaxBannerAdapters = srw.getMaxBannerAdapters();
            this.mAppKey = srw.mAppKey;
            this.mUserId = srw.mUserId;
            this.mProviderOrder = srw.getProviderOrder();
            this.mProviderSettingsHolder = srw.getProviderSettingsHolder();
            this.mConfigurations = srw.getConfigurations();
        } catch (Exception e) {
            defaultInit();
        }
    }

    private void defaultInit() {
        this.mResponse = new JSONObject();
        this.mMaxRVAdapters = 0;
        this.mMaxISAdapters = 0;
        this.mMaxBannerAdapters = 0;
        this.mAppKey = BuildConfig.FLAVOR;
        this.mUserId = BuildConfig.FLAVOR;
        this.mProviderOrder = new ProviderOrder();
        this.mProviderSettingsHolder = ProviderSettingsHolder.getProviderSettingsHolder();
        this.mConfigurations = new Configurations();
    }

    public String toString() {
        JSONObject resultObject = new JSONObject();
        try {
            resultObject.put(APP_KEY_FIELD, this.mAppKey);
            resultObject.put(USER_ID_FIELD, this.mUserId);
            resultObject.put(RESPONSE_FIELD, this.mResponse);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultObject.toString();
    }

    public boolean isValidResponse() {
        boolean valid;
        if (this.mResponse != null) {
            valid = true;
        } else {
            valid = false;
        }
        if (!valid || this.mResponse.has(NativeProtocol.BRIDGE_ARG_ERROR_BUNDLE)) {
            valid = false;
        } else {
            valid = true;
        }
        if (!valid || this.mProviderOrder == null) {
            valid = false;
        } else {
            valid = true;
        }
        if (!valid || this.mProviderSettingsHolder == null) {
            valid = false;
        } else {
            valid = true;
        }
        if (!valid || this.mConfigurations == null) {
            return false;
        }
        return true;
    }

    public List<AD_UNIT> getInitiatedAdUnits() {
        if (this.mResponse == null || this.mConfigurations == null) {
            return null;
        }
        List<AD_UNIT> adUnits = new ArrayList();
        if (!(this.mConfigurations.getRewardedVideoConfigurations() == null || this.mProviderOrder == null || this.mProviderOrder.getRewardedVideoProviderOrder().size() <= 0)) {
            adUnits.add(AD_UNIT.REWARDED_VIDEO);
        }
        if (!(this.mConfigurations.getInterstitialConfigurations() == null || this.mProviderOrder == null || this.mProviderOrder.getInterstitialProviderOrder().size() <= 0)) {
            adUnits.add(AD_UNIT.INTERSTITIAL);
        }
        if (this.mConfigurations.getOfferwallConfigurations() != null) {
            adUnits.add(AD_UNIT.OFFERWALL);
        }
        if (this.mConfigurations.getBannerConfigurations() == null) {
            return adUnits;
        }
        adUnits.add(AD_UNIT.BANNER);
        return adUnits;
    }

    public IronSourceError getReponseError() {
        if (isValidResponse()) {
            return null;
        }
        String errorMsg = BuildConfig.FLAVOR;
        if (this.mResponse != null && this.mResponse.has(NativeProtocol.BRIDGE_ARG_ERROR_BUNDLE)) {
            errorMsg = this.mResponse.optString(NativeProtocol.BRIDGE_ARG_ERROR_BUNDLE);
        }
        if (errorMsg == null) {
            errorMsg = BuildConfig.FLAVOR;
        }
        return ErrorBuilder.buildInitFailedError(errorMsg, IronSourceConstants.MEDIATION_PROVIDER_NAME);
    }

    private void parseProviderOrder() {
        try {
            String backFillProviderName;
            String premiumProviderName;
            int i;
            String providerName;
            JSONObject providerOrderSection = getSection(this.mResponse, "providerOrder");
            JSONArray rvOrderSection = providerOrderSection.optJSONArray("rewardedVideo");
            JSONArray isOrderSection = providerOrderSection.optJSONArray("interstitial");
            JSONArray bnOrderSection = providerOrderSection.optJSONArray("banner");
            this.mProviderOrder = new ProviderOrder();
            if (!(rvOrderSection == null || getConfigurations() == null || getConfigurations().getRewardedVideoConfigurations() == null)) {
                backFillProviderName = getConfigurations().getRewardedVideoConfigurations().getBackFillProviderName();
                premiumProviderName = getConfigurations().getRewardedVideoConfigurations().getPremiumProviderName();
                for (i = 0; i < rvOrderSection.length(); i++) {
                    providerName = rvOrderSection.optString(i);
                    if (providerName.equals(backFillProviderName)) {
                        this.mProviderOrder.setRVBackFillProvider(backFillProviderName);
                    } else {
                        if (providerName.equals(premiumProviderName)) {
                            this.mProviderOrder.setRVPremiumProvider(premiumProviderName);
                        }
                        this.mProviderOrder.addRewardedVideoProvider(providerName);
                    }
                }
            }
            if (isOrderSection != null) {
                if (!(getConfigurations() == null || getConfigurations().getInterstitialConfigurations() == null)) {
                    backFillProviderName = getConfigurations().getInterstitialConfigurations().getBackFillProviderName();
                    premiumProviderName = getConfigurations().getInterstitialConfigurations().getPremiumProviderName();
                    for (i = 0; i < isOrderSection.length(); i++) {
                        providerName = isOrderSection.optString(i);
                        if (providerName.equals(backFillProviderName)) {
                            this.mProviderOrder.setISBackFillProvider(backFillProviderName);
                        } else {
                            if (providerName.equals(premiumProviderName)) {
                                this.mProviderOrder.setISPremiumProvider(premiumProviderName);
                            }
                            this.mProviderOrder.addInterstitialProvider(providerName);
                        }
                    }
                }
            }
            if (bnOrderSection != null) {
                for (i = 0; i < bnOrderSection.length(); i++) {
                    this.mProviderOrder.addBannerProvider(bnOrderSection.optString(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseProviderSettings() {
        try {
            this.mProviderSettingsHolder = ProviderSettingsHolder.getProviderSettingsHolder();
            JSONObject providerSettingsSection = getSection(this.mResponse, "providerSettings");
            Iterator<?> keys = providerSettingsSection.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                JSONObject concreteProviderSettingsSection = providerSettingsSection.optJSONObject(key);
                if (concreteProviderSettingsSection != null) {
                    String nameForReflection = concreteProviderSettingsSection.optString("providerLoadName", key);
                    JSONObject adUnitSection = getSection(concreteProviderSettingsSection, "adUnits");
                    JSONObject appSection = getSection(concreteProviderSettingsSection, ParametersKeys.ORIENTATION_APPLICATION);
                    JSONObject rvSection = getSection(adUnitSection, "rewardedVideo");
                    JSONObject isSection = getSection(adUnitSection, "interstitial");
                    JSONObject bnSection = getSection(adUnitSection, "banner");
                    JSONObject rewardedVideoSettings = mergeJsons(rvSection, appSection);
                    JSONObject interstitialSettings = mergeJsons(isSection, appSection);
                    JSONObject bannerSettings = mergeJsons(bnSection, appSection);
                    if (this.mProviderSettingsHolder.containsProviderSettings(key)) {
                        ProviderSettings providerLocalSettings = this.mProviderSettingsHolder.getProviderSettings(key);
                        JSONObject providerLocalRVSettings = providerLocalSettings.getRewardedVideoSettings();
                        JSONObject providerLocalISSettings = providerLocalSettings.getInterstitialSettings();
                        JSONObject providerLocalBNSettings = providerLocalSettings.getBannerSettings();
                        providerLocalSettings.setRewardedVideoSettings(mergeJsons(providerLocalRVSettings, rewardedVideoSettings));
                        providerLocalSettings.setInterstitialSettings(mergeJsons(providerLocalISSettings, interstitialSettings));
                        providerLocalSettings.setBannerSettings(mergeJsons(providerLocalBNSettings, bannerSettings));
                    } else if (this.mProviderSettingsHolder.containsProviderSettings(IronSourceConstants.MEDIATION_PROVIDER_NAME) && (IronSourceConstants.IRONSOURCE_CONFIG_NAME.toLowerCase().equals(nameForReflection.toLowerCase()) || IronSourceConstants.RIS_CONFIG_NAME.toLowerCase().equals(nameForReflection.toLowerCase()))) {
                        ProviderSettings mediationLocalSettings = this.mProviderSettingsHolder.getProviderSettings(IronSourceConstants.MEDIATION_PROVIDER_NAME);
                        JSONObject mediationLocalRVSettings = mediationLocalSettings.getRewardedVideoSettings();
                        JSONObject mediationLocalISSettings = mediationLocalSettings.getInterstitialSettings();
                        JSONObject jSONObject = new JSONObject(mediationLocalRVSettings.toString());
                        jSONObject = new JSONObject(mediationLocalISSettings.toString());
                        this.mProviderSettingsHolder.addProviderSettings(new ProviderSettings(key, nameForReflection, mergeJsons(jSONObject, rewardedVideoSettings), mergeJsons(jSONObject, interstitialSettings)));
                    } else {
                        ProviderSettings providerSettings = new ProviderSettings(key, nameForReflection, rewardedVideoSettings, interstitialSettings);
                        if (bannerSettings.length() > 0) {
                            providerSettings.setBannerSettings(bannerSettings);
                        }
                        this.mProviderSettingsHolder.addProviderSettings(providerSettings);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseConfigurations() {
        try {
            int[] optOutEvents;
            JSONArray optOutJsonArray;
            int i;
            String backFillProviderName;
            String premiumProviderName;
            JSONObject configurationsSection = getSection(this.mResponse, "configurations");
            JSONObject adUnitSection = getSection(configurationsSection, "adUnits");
            JSONObject appSection = getSection(configurationsSection, ParametersKeys.ORIENTATION_APPLICATION);
            JSONObject rvSection = getSection(adUnitSection, "rewardedVideo");
            JSONObject isSection = getSection(adUnitSection, "interstitial");
            JSONObject owSection = getSection(adUnitSection, "offerwall");
            JSONObject bnSection = getSection(adUnitSection, "banner");
            JSONObject appEventsSection = getSection(appSection, EventEntry.TABLE_NAME);
            JSONObject loggerSection = getSection(appSection, "loggers");
            RewardedVideoConfigurations rvConfig = null;
            InterstitialConfigurations isConfig = null;
            OfferwallConfigurations owConfig = null;
            BannerConfigurations bannerConfig = null;
            if (appSection != null) {
                boolean isUuidEnabled = appSection.optBoolean(DeviceStatus.UUID_ENABLED, true);
                IronSourceUtils.saveBooleanToSharedPrefs(this.mContext, DeviceStatus.UUID_ENABLED, isUuidEnabled);
            }
            if (rvSection != null) {
                JSONArray rvPlacementsSection = rvSection.optJSONArray("placements");
                JSONObject rvEventsSection = getSection(rvSection, EventEntry.TABLE_NAME);
                int rvSmartLoadAmount = getIntConfigValue(rvSection, appSection, "maxNumOfAdaptersToLoadOnStart", 2);
                int rvSmartLoadTimeout = getIntConfigValue(rvSection, appSection, "adapterTimeOutInSeconds", 60);
                String rvAlgorithm = "KTO";
                JSONObject rewardedVideoCombinedEvents = mergeJsons(rvEventsSection, appEventsSection);
                boolean rvUltraEvents = rewardedVideoCombinedEvents.optBoolean("sendUltraEvents", false);
                boolean rvEventsToggle = rewardedVideoCombinedEvents.optBoolean("sendEventsToggle", false);
                String rvEventsUrl = rewardedVideoCombinedEvents.optString("serverEventsURL", BuildConfig.FLAVOR);
                String rvEventsType = rewardedVideoCombinedEvents.optString("serverEventsType", BuildConfig.FLAVOR);
                int rvBackupThreshold = rewardedVideoCombinedEvents.optInt("backupThreshold", -1);
                int rvMaxNumOfEvents = rewardedVideoCombinedEvents.optInt("maxNumberOfEvents", -1);
                int rvMaxEventsPerBatch = rewardedVideoCombinedEvents.optInt("maxEventsPerBatch", DownloadManager.OPERATION_TIMEOUT);
                optOutEvents = null;
                optOutJsonArray = rewardedVideoCombinedEvents.optJSONArray("optOut");
                if (optOutJsonArray != null) {
                    optOutEvents = new int[optOutJsonArray.length()];
                    for (i = 0; i < optOutJsonArray.length(); i++) {
                        optOutEvents[i] = optOutJsonArray.optInt(i);
                    }
                }
                RewardedVideoConfigurations rewardedVideoConfigurations = new RewardedVideoConfigurations(rvSmartLoadAmount, rvSmartLoadTimeout, rvAlgorithm, new ApplicationEvents(rvUltraEvents, rvEventsToggle, rvEventsUrl, rvEventsType, rvBackupThreshold, rvMaxNumOfEvents, rvMaxEventsPerBatch, optOutEvents));
                if (rvPlacementsSection != null) {
                    for (i = 0; i < rvPlacementsSection.length(); i++) {
                        Placement placement = parseSingleRVPlacement(rvPlacementsSection.optJSONObject(i));
                        if (placement != null) {
                            rewardedVideoConfigurations.addRewardedVideoPlacement(placement);
                        }
                    }
                }
                backFillProviderName = rvSection.optString("backFill");
                if (!TextUtils.isEmpty(backFillProviderName)) {
                    rewardedVideoConfigurations.setBackFillProviderName(backFillProviderName);
                }
                premiumProviderName = rvSection.optString("premium");
                if (!TextUtils.isEmpty(premiumProviderName)) {
                    rewardedVideoConfigurations.setPremiumProviderName(premiumProviderName);
                }
            }
            if (isSection != null) {
                JSONArray isPlacementsSection = isSection.optJSONArray("placements");
                JSONObject isEventsSection = getSection(isSection, EventEntry.TABLE_NAME);
                int isSmartLoadAmount = getIntConfigValue(isSection, appSection, "maxNumOfAdaptersToLoadOnStart", 2);
                int isSmartLoadTimeout = getIntConfigValue(isSection, appSection, "adapterTimeOutInSeconds", 60);
                JSONObject interstitialCombinedEvents = mergeJsons(isEventsSection, appEventsSection);
                boolean isEventsToggle = interstitialCombinedEvents.optBoolean("sendEventsToggle", false);
                String isEventsUrl = interstitialCombinedEvents.optString("serverEventsURL", BuildConfig.FLAVOR);
                String isEventsType = interstitialCombinedEvents.optString("serverEventsType", BuildConfig.FLAVOR);
                int isBackupThreshold = interstitialCombinedEvents.optInt("backupThreshold", -1);
                int isMaxNumOfEvents = interstitialCombinedEvents.optInt("maxNumberOfEvents", -1);
                int isMaxEventsPerBatch = interstitialCombinedEvents.optInt("maxEventsPerBatch", DownloadManager.OPERATION_TIMEOUT);
                optOutEvents = null;
                optOutJsonArray = interstitialCombinedEvents.optJSONArray("optOut");
                if (optOutJsonArray != null) {
                    optOutEvents = new int[optOutJsonArray.length()];
                    for (i = 0; i < optOutJsonArray.length(); i++) {
                        optOutEvents[i] = optOutJsonArray.optInt(i);
                    }
                }
                InterstitialConfigurations interstitialConfigurations = new InterstitialConfigurations(isSmartLoadAmount, isSmartLoadTimeout, new ApplicationEvents(false, isEventsToggle, isEventsUrl, isEventsType, isBackupThreshold, isMaxNumOfEvents, isMaxEventsPerBatch, optOutEvents));
                if (isPlacementsSection != null) {
                    for (i = 0; i < isPlacementsSection.length(); i++) {
                        InterstitialPlacement placement2 = parseSingleISPlacement(isPlacementsSection.optJSONObject(i));
                        if (placement2 != null) {
                            interstitialConfigurations.addInterstitialPlacement(placement2);
                        }
                    }
                }
                backFillProviderName = isSection.optString("backFill");
                if (!TextUtils.isEmpty(backFillProviderName)) {
                    interstitialConfigurations.setBackFillProviderName(backFillProviderName);
                }
                premiumProviderName = isSection.optString("premium");
                if (!TextUtils.isEmpty(premiumProviderName)) {
                    interstitialConfigurations.setPremiumProviderName(premiumProviderName);
                }
            }
            if (bnSection != null) {
                JSONArray bnPlacementsSection = bnSection.optJSONArray("placements");
                JSONObject bnEventsSection = getSection(bnSection, EventEntry.TABLE_NAME);
                int bnSmartLoadAmount = getIntConfigValue(bnSection, appSection, "maxNumOfAdaptersToLoadOnStart", 1);
                long bnSmartLoadTimeout = getLongConfigValue(bnSection, appSection, "atim", 200);
                JSONObject bannerCombinedEvents = mergeJsons(bnEventsSection, appEventsSection);
                boolean bnEventsToggle = bannerCombinedEvents.optBoolean("sendEventsToggle", false);
                String bnEventsUrl = bannerCombinedEvents.optString("serverEventsURL", BuildConfig.FLAVOR);
                String bnEventsType = bannerCombinedEvents.optString("serverEventsType", BuildConfig.FLAVOR);
                int bnBackupThreshold = bannerCombinedEvents.optInt("backupThreshold", -1);
                int bnMaxNumOfEvents = bannerCombinedEvents.optInt("maxNumberOfEvents", -1);
                int bnMaxEventsPerBatch = bannerCombinedEvents.optInt("maxEventsPerBatch", DownloadManager.OPERATION_TIMEOUT);
                optOutEvents = null;
                optOutJsonArray = bannerCombinedEvents.optJSONArray("optOut");
                if (optOutJsonArray != null) {
                    optOutEvents = new int[optOutJsonArray.length()];
                    for (i = 0; i < optOutJsonArray.length(); i++) {
                        optOutEvents[i] = optOutJsonArray.optInt(i);
                    }
                }
                BannerConfigurations bannerConfigurations = new BannerConfigurations(bnSmartLoadAmount, bnSmartLoadTimeout, new ApplicationEvents(false, bnEventsToggle, bnEventsUrl, bnEventsType, bnBackupThreshold, bnMaxNumOfEvents, bnMaxEventsPerBatch, optOutEvents));
                if (bnPlacementsSection != null) {
                    for (i = 0; i < bnPlacementsSection.length(); i++) {
                        BannerPlacement placement3 = parseSingleBNPlacement(bnPlacementsSection.optJSONObject(i));
                        if (placement3 != null) {
                            bannerConfigurations.addBannerPlacement(placement3);
                        }
                    }
                }
            }
            if (owSection != null) {
                JSONArray owPlacementsSection = owSection.optJSONArray("placements");
                owConfig = new OfferwallConfigurations();
                if (owPlacementsSection != null) {
                    for (i = 0; i < owPlacementsSection.length(); i++) {
                        OfferwallPlacement placement4 = parseSingleOWPlacement(owPlacementsSection.optJSONObject(i));
                        if (placement4 != null) {
                            owConfig.addOfferwallPlacement(placement4);
                        }
                    }
                }
            }
            ApplicationConfigurations applicationConfigurations = new ApplicationConfigurations(new ApplicationLogger(loggerSection.optInt(ServerLogger.NAME, 3), loggerSection.optInt("publisher", 3), loggerSection.optInt(ConsoleLogger.NAME, 3)));
            boolean allowLocation = appSection.optBoolean("allowLocation", false);
            IronSourceUtils.saveBooleanToSharedPrefs(this.mContext, GeneralProperties.ALLOW_LOCATION_SHARED_PREFS_KEY, allowLocation);
            this.mConfigurations = new Configurations(rvConfig, isConfig, owConfig, bannerConfig, applicationConfigurations);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getIntConfigValue(JSONObject mainJson, JSONObject secondaryJson, String key, int defaultValue) {
        int result = 0;
        if (mainJson.has(key)) {
            result = mainJson.optInt(key, 0);
        } else if (secondaryJson.has(key)) {
            result = secondaryJson.optInt(key, 0);
        }
        if (result == 0) {
            return defaultValue;
        }
        return result;
    }

    private long getLongConfigValue(JSONObject mainJson, JSONObject secondaryJson, String key, long defaultValue) {
        long result = 0;
        if (mainJson.has(key)) {
            result = mainJson.optLong(key, 0);
        } else if (secondaryJson.has(key)) {
            result = secondaryJson.optLong(key, 0);
        }
        if (result == 0) {
            return defaultValue;
        }
        return result;
    }

    private Placement parseSingleRVPlacement(JSONObject placementJson) {
        Placement result = null;
        if (placementJson != null) {
            int placementId = placementJson.optInt(Constants.PLACEMENT_ID, -1);
            String placementName = placementJson.optString("placementName", BuildConfig.FLAVOR);
            String virtualItemName = placementJson.optString("virtualItemName", BuildConfig.FLAVOR);
            int virtualItemCount = placementJson.optInt("virtualItemCount", -1);
            PlacementAvailabilitySettings settings = getPlacementAvailabilitySettings(placementJson);
            if (placementId >= 0 && !TextUtils.isEmpty(placementName) && !TextUtils.isEmpty(virtualItemName) && virtualItemCount > 0) {
                result = new Placement(placementId, placementName, virtualItemName, virtualItemCount, settings);
                if (settings != null) {
                    CappingManager.addCappingInfo(this.mContext, result);
                }
            }
        }
        return result;
    }

    private InterstitialPlacement parseSingleISPlacement(JSONObject placementJson) {
        InterstitialPlacement result = null;
        if (placementJson != null) {
            int placementId = placementJson.optInt(Constants.PLACEMENT_ID, -1);
            String placementName = placementJson.optString("placementName", BuildConfig.FLAVOR);
            PlacementAvailabilitySettings settings = getPlacementAvailabilitySettings(placementJson);
            if (placementId >= 0 && !TextUtils.isEmpty(placementName)) {
                result = new InterstitialPlacement(placementId, placementName, settings);
                if (settings != null) {
                    CappingManager.addCappingInfo(this.mContext, result);
                }
            }
        }
        return result;
    }

    private OfferwallPlacement parseSingleOWPlacement(JSONObject placementJson) {
        if (placementJson == null) {
            return null;
        }
        int placementId = placementJson.optInt(Constants.PLACEMENT_ID, -1);
        String placementName = placementJson.optString("placementName", BuildConfig.FLAVOR);
        if (placementId < 0 || TextUtils.isEmpty(placementName)) {
            return null;
        }
        return new OfferwallPlacement(placementId, placementName);
    }

    private BannerPlacement parseSingleBNPlacement(JSONObject placementJson) {
        BannerPlacement result = null;
        if (placementJson != null) {
            int placementId = placementJson.optInt(Constants.PLACEMENT_ID, -1);
            String placementName = placementJson.optString("placementName", BuildConfig.FLAVOR);
            PlacementAvailabilitySettings settings = getPlacementAvailabilitySettings(placementJson);
            if (placementId >= 0 && !TextUtils.isEmpty(placementName)) {
                result = new BannerPlacement(placementId, placementName, settings);
                if (settings != null) {
                    CappingManager.addCappingInfo(this.mContext, result);
                }
            }
        }
        return result;
    }

    private PlacementAvailabilitySettings getPlacementAvailabilitySettings(JSONObject placementJson) {
        if (placementJson == null) {
            return null;
        }
        PlacementAvailabilitySettingsBuilder settingsBuilder = new PlacementAvailabilitySettingsBuilder();
        settingsBuilder.delivery(placementJson.optBoolean("delivery", true));
        JSONObject cappingJson = placementJson.optJSONObject("capping");
        if (cappingJson != null) {
            boolean isCappingEnabled;
            PlacementCappingType cappingType = null;
            String cappingUnitString = cappingJson.optString("unit");
            if (!TextUtils.isEmpty(cappingUnitString)) {
                if (PlacementCappingType.PER_DAY.toString().equals(cappingUnitString)) {
                    cappingType = PlacementCappingType.PER_DAY;
                } else if (PlacementCappingType.PER_HOUR.toString().equals(cappingUnitString)) {
                    cappingType = PlacementCappingType.PER_HOUR;
                }
            }
            int cappingValue = cappingJson.optInt("maxImpressions", 0);
            if (!cappingJson.optBoolean("enabled", false) || cappingValue <= 0) {
                isCappingEnabled = false;
            } else {
                isCappingEnabled = true;
            }
            settingsBuilder.capping(isCappingEnabled, cappingType, cappingValue);
        }
        JSONObject pacingJson = placementJson.optJSONObject("pacing");
        if (pacingJson != null) {
            boolean isPacingEnabled;
            int pacingValue = pacingJson.optInt("numOfSeconds", 0);
            if (!pacingJson.optBoolean("enabled", false) || pacingValue <= 0) {
                isPacingEnabled = false;
            } else {
                isPacingEnabled = true;
            }
            settingsBuilder.pacing(isPacingEnabled, pacingValue);
        }
        return settingsBuilder.build();
    }

    private JSONObject getSection(JSONObject json, String sectionName) {
        if (json != null) {
            return json.optJSONObject(sectionName);
        }
        return null;
    }

    private JSONObject mergeJsons(JSONObject mainJson, JSONObject secondaryJson) {
        if (mainJson == null && secondaryJson == null) {
            try {
                return new JSONObject();
            } catch (JSONException e) {
                e.printStackTrace();
                return mainJson;
            }
        } else if (mainJson == null) {
            return secondaryJson;
        } else {
            if (secondaryJson == null) {
                return mainJson;
            }
            Iterator it = secondaryJson.keys();
            while (it.hasNext()) {
                String key = (String) it.next();
                if (!mainJson.has(key)) {
                    mainJson.put(key, secondaryJson.get(key));
                }
            }
            return mainJson;
        }
    }

    public int getMaxRVAdapters() {
        return this.mMaxRVAdapters;
    }

    public int getMaxISAdapters() {
        return this.mMaxISAdapters;
    }

    public int getMaxBannerAdapters() {
        return this.mMaxBannerAdapters;
    }

    public int decreaseMaxRVAdapters() {
        if (this.mMaxRVAdapters > 0) {
            this.mMaxRVAdapters--;
        }
        return this.mMaxRVAdapters;
    }

    public int decreaseMaxISAdapters() {
        if (this.mMaxISAdapters > 0) {
            this.mMaxISAdapters--;
        }
        return this.mMaxISAdapters;
    }

    public int decreaseMaxBannerAdapters() {
        if (this.mMaxBannerAdapters > 0) {
            this.mMaxBannerAdapters--;
        }
        return this.mMaxBannerAdapters;
    }

    public String getNextRVProvider() {
        try {
            this.mRVLoadPosition++;
            if (this.mProviderOrder.getRewardedVideoProviderOrder().size() > this.mRVLoadPosition) {
                return (String) this.mProviderOrder.getRewardedVideoProviderOrder().get(this.mRVLoadPosition);
            }
            return null;
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceTag.INTERNAL, "getNextProvider(RVLoadPosition: " + this.mRVLoadPosition + " RVProviders.size(): " + this.mProviderOrder.getRewardedVideoProviderOrder().size() + ")", e);
            return null;
        }
    }

    public String getNextISProvider() {
        try {
            this.mISLoadPosition++;
            if (this.mProviderOrder.getInterstitialProviderOrder().size() > this.mISLoadPosition) {
                return (String) this.mProviderOrder.getInterstitialProviderOrder().get(this.mISLoadPosition);
            }
            return null;
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceTag.INTERNAL, "getNextProvider(ISLoadPosition: " + this.mISLoadPosition + " ISProviders.size(): " + this.mProviderOrder.getInterstitialProviderOrder().size() + ")", e);
            return null;
        }
    }

    public String getNextBannerProvider() {
        try {
            this.mBannerLoadPosition++;
            if (this.mProviderOrder.getBannerProviderOrder().size() > this.mBannerLoadPosition) {
                return (String) this.mProviderOrder.getBannerProviderOrder().get(this.mBannerLoadPosition);
            }
            return null;
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceTag.INTERNAL, "getNextProvider(BannerLoadPosition: " + this.mBannerLoadPosition + " BannerProviders.size(): " + this.mProviderOrder.getBannerProviderOrder().size() + ")", e);
            return null;
        }
    }

    public String getRVBackFillProvider() {
        try {
            return this.mProviderOrder.getRVBackFillProvider();
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceTag.INTERNAL, "getRVBackFillProvider", e);
            return null;
        }
    }

    public String getRVPremiumProvider() {
        try {
            return this.mProviderOrder.getRVPremiumProvider();
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceTag.INTERNAL, "getRVPremiumProvider", e);
            return null;
        }
    }

    public String getISBackFillProvider() {
        try {
            return this.mProviderOrder.getISBackFillProvider();
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceTag.INTERNAL, "getISBackFillProvider", e);
            return null;
        }
    }

    public String getISPremiumProvider() {
        try {
            return this.mProviderOrder.getISPremiumProvider();
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceTag.INTERNAL, "getISPremiumProvider", e);
            return null;
        }
    }

    public boolean hasMoreRVProvidersToLoad() {
        return this.mRVLoadPosition < this.mProviderOrder.getRewardedVideoProviderOrder().size() && this.mProviderOrder.getRewardedVideoProviderOrder().size() > 0;
    }

    public boolean hasMoreISProvidersToLoad() {
        return this.mISLoadPosition < this.mProviderOrder.getInterstitialProviderOrder().size() && this.mProviderOrder.getInterstitialProviderOrder().size() > 0;
    }

    public boolean hasMoreBannerProvidersToLoad() {
        return this.mBannerLoadPosition < this.mProviderOrder.getBannerProviderOrder().size() && this.mProviderOrder.getBannerProviderOrder().size() > 0;
    }

    public int getRVAdaptersLoadPosition() {
        return this.mRVLoadPosition;
    }

    public int getISAdaptersLoadPosition() {
        return this.mISLoadPosition;
    }

    public int getBannerAdaptersLoadPosition() {
        return this.mBannerLoadPosition;
    }

    public ProviderSettingsHolder getProviderSettingsHolder() {
        return this.mProviderSettingsHolder;
    }

    public ProviderOrder getProviderOrder() {
        return this.mProviderOrder;
    }

    public Configurations getConfigurations() {
        return this.mConfigurations;
    }

    private Context getContext() {
        return this.mContext;
    }
}
