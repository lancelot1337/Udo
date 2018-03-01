package com.ironsource.mediationsdk.integration;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.facebook.internal.NativeProtocol;
import com.ironsource.mediationsdk.IronSourceObject;
import com.ironsource.mediationsdk.utils.IronSourceUtils;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class IntegrationHelper {
    private static String[] SDK_COMPATIBILITY_VERSION_ARR = new String[]{"3.0", "3.1"};
    private static final String TAG = "IntegrationHelper";

    public static void validateIntegration(Activity activity) {
        String ironSource = "IronSource";
        String adcolony = "AdColony";
        String applovin = "AppLovin";
        String chartboost = "Chartboost";
        String hyprmx = "HyprMX";
        String unityads = "UnityAds";
        String vungle = "Vungle";
        String inmobi = "InMobi";
        String facebook = "Facebook";
        String mediaBrix = "MediaBrix";
        String tapjoy = "Tapjoy";
        String admob = "AdMob";
        List<String> generalPermissions = Arrays.asList(new String[]{"android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE"});
        String vungleWriteExternalStoragePermission = "android.permission.WRITE_EXTERNAL_STORAGE";
        Map<String, Integer> vunglePermissionsToMaxSdkVersionMap = new HashMap();
        vunglePermissionsToMaxSdkVersionMap.put("android.permission.WRITE_EXTERNAL_STORAGE", Integer.valueOf(18));
        List<String> vunglePermissions = Collections.singletonList("android.permission.WRITE_EXTERNAL_STORAGE");
        List<String> ironSourceActivities = Arrays.asList(new String[]{"com.ironsource.sdk.controller.ControllerActivity", "com.ironsource.sdk.controller.InterstitialActivity", "com.ironsource.sdk.controller.OpenUrlActivity"});
        List<String> adColonyActivities = Arrays.asList(new String[]{"com.adcolony.sdk.AdColonyInterstitialActivity", "com.adcolony.sdk.AdColonyAdViewActivity"});
        List<String> appLovinActivities = Arrays.asList(new String[]{"com.applovin.adview.AppLovinInterstitialActivity", "com.applovin.adview.AppLovinConfirmationActivity"});
        List<String> chartboostActivities = Collections.singletonList("com.chartboost.sdk.CBImpressionActivity");
        List<String> hyprMXActivities = Arrays.asList(new String[]{"com.hyprmx.android.sdk.activity.HyprMXOfferViewerActivity", "com.hyprmx.android.sdk.activity.HyprMXRequiredInformationActivity", "com.hyprmx.android.sdk.activity.HyprMXNoOffersActivity", "com.hyprmx.android.sdk.videoplayer.HyprMXVideoPlayerActivity"});
        List<String> vungleActivities = Arrays.asList(new String[]{"com.vungle.publisher.VideoFullScreenAdActivity", "com.vungle.publisher.MraidFullScreenAdActivity"});
        List<String> inMobiActivities = Collections.singletonList("com.inmobi.rendering.InMobiAdActivity");
        List<String> inMobiBroadcastReceivers = Collections.singletonList("com.inmobi.commons.core.utilities.uid.ImIdShareBroadCastReceiver");
        List<String> facebookActivities = Collections.singletonList("com.facebook.ads.InterstitialAdActivity");
        List<String> mediaBrixActivities = Collections.singletonList("com.mediabrix.android.service.AdViewActivity");
        List<String> tapjoyActivities = Arrays.asList(new String[]{"com.tapjoy.TJAdUnitActivity", "com.tapjoy.mraid.view.ActionHandler", "com.tapjoy.mraid.view.Browser", "com.tapjoy.TJContentActivity"});
        List<String> admobActivities = Collections.singletonList("com.google.android.gms.ads.AdActivity");
        List<String> unityAdsActivities = Arrays.asList(new String[]{"com.unity3d.ads.adunit.AdUnitActivity", "com.unity3d.ads.adunit.AdUnitSoftwareActivity"});
        ArrayList<Pair<String, String>> vungleExternalLibraries = new ArrayList<Pair<String, String>>() {
            {
                add(new Pair("javax.inject.Inject", "javax.inject-1.jar"));
                add(new Pair("dagger.Module", "dagger-2.7.jar"));
            }
        };
        String hyprMXSdk = "com.hyprmx.android.sdk.activity.HyprMXOfferViewerActivity";
        final AdapterObject ironSourceAdapter = new AdapterObject("IronSource", ironSourceActivities, false);
        ironSourceAdapter.setPermissions(generalPermissions);
        final AdapterObject adColonyAdapter = new AdapterObject("AdColony", adColonyActivities, true);
        final AdapterObject appLovinAdapter = new AdapterObject("AppLovin", appLovinActivities, true);
        final AdapterObject chartboostAdapter = new AdapterObject("Chartboost", chartboostActivities, true);
        final AdapterObject hyprMXAdapter = new AdapterObject("HyprMX", hyprMXActivities, true);
        hyprMXAdapter.setSdkName("com.hyprmx.android.sdk.activity.HyprMXOfferViewerActivity");
        final AdapterObject unityAdsAdapter = new AdapterObject("UnityAds", unityAdsActivities, true);
        final AdapterObject vungleAdapter = new AdapterObject("Vungle", vungleActivities, true);
        vungleAdapter.setExternalLibraries(vungleExternalLibraries);
        vungleAdapter.setPermissions(vunglePermissions);
        vungleAdapter.setPermissionToMaxSdkVersion(vunglePermissionsToMaxSdkVersionMap);
        final AdapterObject inMobiAdapter = new AdapterObject("InMobi", inMobiActivities, true);
        inMobiAdapter.setBroadcastReceivers(inMobiBroadcastReceivers);
        final AdapterObject facebookAdapter = new AdapterObject("Facebook", facebookActivities, true);
        final AdapterObject mediaBrixAdapter = new AdapterObject("MediaBrix", mediaBrixActivities, true);
        final AdapterObject tapjoyAdapter = new AdapterObject("Tapjoy", tapjoyActivities, true);
        final AdapterObject admobAdapter = new AdapterObject("AdMob", admobActivities, true);
        ArrayList<AdapterObject> adapters = new ArrayList<AdapterObject>() {
        };
        Log.i(TAG, "Verifying Integration:");
        Iterator it = adapters.iterator();
        while (it.hasNext()) {
            AdapterObject adapterObject = (AdapterObject) it.next();
            boolean verified = true;
            Log.w(TAG, "--------------- " + adapterObject.getName() + " --------------");
            if (adapterObject.isAdapter() && !validateAdapter(adapterObject)) {
                verified = false;
            }
            if (verified) {
                if (!(adapterObject.getSdkName() == null || validateSdk(adapterObject.getSdkName()))) {
                    verified = false;
                }
                if (!(adapterObject.getPermissions() == null || validatePermissions(activity, adapterObject))) {
                    verified = false;
                }
                if (!(adapterObject.getActivities() == null || validateActivities(activity, adapterObject.getActivities()))) {
                    verified = false;
                }
                if (!(adapterObject.getExternalLibraries() == null || validateExternalLibraries(adapterObject.getExternalLibraries()))) {
                    verified = false;
                }
                if (!(adapterObject.getBroadcastReceivers() == null || validateBroadcastReceivers(activity, adapterObject.getBroadcastReceivers()))) {
                    verified = false;
                }
            }
            if (verified) {
                Log.w(TAG, ">>>> " + adapterObject.getName() + " - VERIFIED");
            } else {
                Log.e(TAG, ">>>> " + adapterObject.getName() + " - NOT VERIFIED");
            }
        }
        validateGooglePlayServices(activity);
    }

    private static void validateGooglePlayServices(final Activity activity) {
        String mGooglePlayServicesMetaData = "com.google.android.gms.version";
        String mGooglePlayServices = "Google Play Services";
        new Thread() {
            public void run() {
                try {
                    Log.w(IntegrationHelper.TAG, "--------------- Google Play Services --------------");
                    if (activity.getPackageManager().getApplicationInfo(activity.getPackageName(), 128).metaData.containsKey("com.google.android.gms.version")) {
                        IntegrationHelper.validationMessageIsPresent("Google Play Services", true);
                        String gaid = IronSourceObject.getInstance().getAdvertiserId(activity);
                        if (!TextUtils.isEmpty(gaid)) {
                            Log.i(IntegrationHelper.TAG, "GAID is: " + gaid + " (use this for test devices)");
                            return;
                        }
                        return;
                    }
                    IntegrationHelper.validationMessageIsPresent("Google Play Services", false);
                } catch (Exception e) {
                    IntegrationHelper.validationMessageIsPresent("Google Play Services", false);
                }
            }
        }.start();
    }

    private static boolean validateAdapter(AdapterObject adapter) {
        boolean result = false;
        try {
            try {
                Field versionField = Class.forName(adapter.getAdapterName()).getDeclaredField("VERSION");
                versionField.setAccessible(true);
                String adapterVersion = (String) versionField.get(null);
                for (String sdkCompatVersion : SDK_COMPATIBILITY_VERSION_ARR) {
                    if (!TextUtils.isEmpty(adapterVersion) && adapterVersion.indexOf(sdkCompatVersion) == 0) {
                        result = true;
                        break;
                    }
                }
                if (result) {
                    validationMessageIsVerified("Adapter version", true);
                } else {
                    Log.e(TAG, adapter.getName() + " adapter " + adapterVersion + " is incompatible with SDK version " + IronSourceUtils.getSDKVersion() + ", please update your adapter to version " + SDK_COMPATIBILITY_VERSION_ARR[0] + ".*");
                }
            } catch (Exception e) {
                Log.e(TAG, adapter.getName() + " adapter version is incompatible with SDK version " + IronSourceUtils.getSDKVersion() + ", please update your adapter to version " + SDK_COMPATIBILITY_VERSION_ARR[0] + ".*");
            }
        } catch (ClassNotFoundException e2) {
            validationMessageIsPresent("Adapter", false);
        }
        if (result) {
            validationMessageIsVerified("Adapter", true);
        }
        return result;
    }

    private static boolean validateSdk(String sdkName) {
        boolean result = false;
        try {
            Class localClass = Class.forName(sdkName);
            result = true;
            validationMessageIsPresent("SDK", true);
            return true;
        } catch (ClassNotFoundException e) {
            validationMessageIsPresent("SDK", false);
            return result;
        }
    }

    private static boolean validateActivities(Activity activity, List<String> activities) {
        boolean result = true;
        PackageManager packageManager = activity.getPackageManager();
        Log.i(TAG, "*** Activities ***");
        for (String act : activities) {
            try {
                if (packageManager.queryIntentActivities(new Intent(activity, Class.forName(act)), NativeProtocol.MESSAGE_GET_ACCESS_TOKEN_REQUEST).size() > 0) {
                    validationMessageIsPresent(act, true);
                } else {
                    result = false;
                    validationMessageIsPresent(act, false);
                }
            } catch (ClassNotFoundException e) {
                result = false;
                validationMessageIsPresent(act, false);
            }
        }
        return result;
    }

    private static boolean validatePermissions(Activity activity, AdapterObject adapterObject) {
        List<String> permissions = adapterObject.getPermissions();
        Map<String, Integer> permissionsToMaxSdkVersionMap = adapterObject.getPermissionToMaxSdkVersion();
        Map<String, Integer> permissionsToMinSdkVersionMap = adapterObject.getPermissionToMinSdkVersion();
        int currentSdkVersion = VERSION.SDK_INT;
        boolean result = true;
        PackageManager packageManager = activity.getPackageManager();
        Log.i(TAG, "*** Permissions ***");
        for (String permission : permissions) {
            if ((permissionsToMaxSdkVersionMap == null || !permissionsToMaxSdkVersionMap.containsKey(permission) || ((Integer) permissionsToMaxSdkVersionMap.get(permission)).intValue() >= currentSdkVersion) && (permissionsToMinSdkVersionMap == null || !permissionsToMinSdkVersionMap.containsKey(permission) || ((Integer) permissionsToMinSdkVersionMap.get(permission)).intValue() <= currentSdkVersion)) {
                if (packageManager.checkPermission(permission, activity.getPackageName()) == 0) {
                    validationMessageIsPresent(permission, true);
                } else {
                    result = false;
                    validationMessageIsPresent(permission, false);
                }
            }
        }
        return result;
    }

    private static boolean validateExternalLibraries(ArrayList<Pair<String, String>> externalLibraries) {
        boolean result = true;
        Log.i(TAG, "*** External Libraries ***");
        Iterator it = externalLibraries.iterator();
        while (it.hasNext()) {
            Pair<String, String> externalLibrary = (Pair) it.next();
            try {
                Class localClass = Class.forName((String) externalLibrary.first);
                validationMessageIsPresent((String) externalLibrary.second, true);
            } catch (ClassNotFoundException e) {
                result = false;
                validationMessageIsPresent((String) externalLibrary.second, false);
            }
        }
        return result;
    }

    private static boolean validateBroadcastReceivers(Activity activity, List<String> broadcastReceivers) {
        boolean result = true;
        PackageManager packageManager = activity.getPackageManager();
        Log.i(TAG, "*** Broadcast Receivers ***");
        for (String broadcastReceiver : broadcastReceivers) {
            try {
                if (packageManager.queryBroadcastReceivers(new Intent(activity, Class.forName(broadcastReceiver)), NativeProtocol.MESSAGE_GET_ACCESS_TOKEN_REQUEST).size() > 0) {
                    validationMessageIsPresent(broadcastReceiver, true);
                } else {
                    result = false;
                    validationMessageIsPresent(broadcastReceiver, false);
                }
            } catch (ClassNotFoundException e) {
                result = false;
                validationMessageIsPresent(broadcastReceiver, false);
            }
        }
        return result;
    }

    private static void validationMessageIsPresent(String paramToValidate, boolean successful) {
        if (successful) {
            Log.i(TAG, paramToValidate + " - VERIFIED");
        } else {
            Log.e(TAG, paramToValidate + " - MISSING");
        }
    }

    private static void validationMessageIsVerified(String paramToValidate, boolean successful) {
        if (successful) {
            Log.i(TAG, paramToValidate + " - VERIFIED");
        } else {
            Log.e(TAG, paramToValidate + " - NOT VERIFIED");
        }
    }
}
