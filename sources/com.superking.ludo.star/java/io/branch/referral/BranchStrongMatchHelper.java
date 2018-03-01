package io.branch.referral;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import com.ironsource.sdk.utils.Constants.RequestParameters;
import io.branch.referral.Defines.Jsonkey;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

class BranchStrongMatchHelper {
    private static final int STRONG_MATCH_CHECK_TIME_OUT = 500;
    private static final long THIRTY_DAYS_EPOCH_MILLI_SEC = 2592000000L;
    private static BranchStrongMatchHelper branchStrongMatchHelper_;
    Class<?> CustomServiceTabConnectionClass;
    Class<?> CustomTabsCallbackClass;
    Class<?> CustomTabsClientClass;
    Class<?> CustomTabsSessionClass;
    Class<?> ICustomTabsServiceClass;
    private boolean isCustomTabsAvailable_ = true;
    Object mClient_ = null;
    private final Handler timeOutHandler_;

    interface StrongMatchCheckEvents {
        void onStrongMatchCheckFinished();
    }

    private abstract class MockCustomTabServiceConnection implements ServiceConnection {
        public abstract void onCustomTabsServiceConnected(ComponentName componentName, Object obj);

        public final void onServiceConnected(ComponentName name, IBinder service) {
            try {
                Constructor<?> customTabClientConstructor = BranchStrongMatchHelper.this.CustomTabsClientClass.getDeclaredConstructor(new Class[]{BranchStrongMatchHelper.this.ICustomTabsServiceClass, ComponentName.class});
                customTabClientConstructor.setAccessible(true);
                r5 = new Object[2];
                r5[0] = Class.forName("android.support.customtabs.ICustomTabsService$Stub").getMethod("asInterface", new Class[]{IBinder.class}).invoke(null, new Object[]{service});
                r5[1] = name;
                onCustomTabsServiceConnected(name, customTabClientConstructor.newInstance(r5));
            } catch (Throwable th) {
                onCustomTabsServiceConnected(null, null);
            }
        }
    }

    private BranchStrongMatchHelper() {
        try {
            this.CustomTabsClientClass = Class.forName("android.support.customtabs.CustomTabsClient");
            this.CustomServiceTabConnectionClass = Class.forName("android.support.customtabs.CustomTabsServiceConnection");
            this.CustomTabsCallbackClass = Class.forName("android.support.customtabs.CustomTabsCallback");
            this.CustomTabsSessionClass = Class.forName("android.support.customtabs.CustomTabsSession");
            this.ICustomTabsServiceClass = Class.forName("android.support.customtabs.ICustomTabsService");
        } catch (Throwable th) {
            this.isCustomTabsAvailable_ = false;
        }
        this.timeOutHandler_ = new Handler();
    }

    public static BranchStrongMatchHelper getInstance() {
        if (branchStrongMatchHelper_ == null) {
            branchStrongMatchHelper_ = new BranchStrongMatchHelper();
        }
        return branchStrongMatchHelper_;
    }

    public void checkForStrongMatch(Context context, String cookieMatchDomain, DeviceInfo deviceInfo, PrefHelper prefHelper, SystemObserver systemObserver, StrongMatchCheckEvents callback) {
        if (System.currentTimeMillis() - prefHelper.getLastStrongMatchTime() < THIRTY_DAYS_EPOCH_MILLI_SEC) {
            updateStrongMatchCheckFinished(callback);
        } else if (this.isCustomTabsAvailable_) {
            try {
                if (deviceInfo.getHardwareID() != null) {
                    final Uri strongMatchUri = buildStrongMatchUrl(cookieMatchDomain, deviceInfo, prefHelper, systemObserver);
                    if (strongMatchUri != null) {
                        final StrongMatchCheckEvents strongMatchCheckEvents = callback;
                        this.timeOutHandler_.postDelayed(new Runnable() {
                            public void run() {
                                BranchStrongMatchHelper.this.updateStrongMatchCheckFinished(strongMatchCheckEvents);
                            }
                        }, 500);
                        Method bindCustomTabsServiceMethod = this.CustomTabsClientClass.getMethod("bindCustomTabsService", new Class[]{Context.class, String.class, this.CustomServiceTabConnectionClass});
                        final Method warmupMethod = this.CustomTabsClientClass.getMethod("warmup", new Class[]{Long.TYPE});
                        final Method newSessionMethod = this.CustomTabsClientClass.getMethod("newSession", new Class[]{this.CustomTabsCallbackClass});
                        final Method mayLaunchUrlMethod = this.CustomTabsSessionClass.getMethod("mayLaunchUrl", new Class[]{Uri.class, Bundle.class, List.class});
                        Intent intent = new Intent("android.support.customtabs.action.CustomTabsService");
                        intent.setPackage("com.android.chrome");
                        final PrefHelper prefHelper2 = prefHelper;
                        final StrongMatchCheckEvents strongMatchCheckEvents2 = callback;
                        context.bindService(intent, new MockCustomTabServiceConnection() {
                            public void onCustomTabsServiceConnected(ComponentName var1, Object clientObj) {
                                BranchStrongMatchHelper.this.mClient_ = BranchStrongMatchHelper.this.CustomTabsClientClass.cast(clientObj);
                                if (BranchStrongMatchHelper.this.mClient_ != null) {
                                    try {
                                        warmupMethod.invoke(BranchStrongMatchHelper.this.mClient_, new Object[]{Integer.valueOf(0)});
                                        Object customTabsSessionObj = newSessionMethod.invoke(BranchStrongMatchHelper.this.mClient_, new Object[]{null});
                                        if (customTabsSessionObj != null) {
                                            mayLaunchUrlMethod.invoke(customTabsSessionObj, new Object[]{strongMatchUri, null, null});
                                            prefHelper2.saveLastStrongMatchTime(System.currentTimeMillis());
                                        }
                                    } catch (Throwable th) {
                                        BranchStrongMatchHelper.this.mClient_ = null;
                                        BranchStrongMatchHelper.this.updateStrongMatchCheckFinished(strongMatchCheckEvents2);
                                    }
                                }
                            }

                            public void onServiceDisconnected(ComponentName name) {
                                BranchStrongMatchHelper.this.mClient_ = null;
                                BranchStrongMatchHelper.this.updateStrongMatchCheckFinished(strongMatchCheckEvents2);
                            }
                        }, 33);
                        return;
                    }
                    updateStrongMatchCheckFinished(callback);
                    return;
                }
                updateStrongMatchCheckFinished(callback);
                Log.d("BranchSDK", "Cannot use cookie-based matching since device id is not available");
            } catch (Throwable th) {
                updateStrongMatchCheckFinished(callback);
            }
        } else {
            updateStrongMatchCheckFinished(callback);
        }
    }

    private void updateStrongMatchCheckFinished(StrongMatchCheckEvents callback) {
        if (callback != null) {
            callback.onStrongMatchCheckFinished();
        }
    }

    private Uri buildStrongMatchUrl(String matchDomain, DeviceInfo deviceInfo, PrefHelper prefHelper, SystemObserver systemObserver) {
        if (TextUtils.isEmpty(matchDomain)) {
            return null;
        }
        String uriString = (("https://" + matchDomain + "/_strong_match?os=" + deviceInfo.getOsName()) + RequestParameters.AMPERSAND + Jsonkey.HardwareID.getKey() + RequestParameters.EQUAL + deviceInfo.getHardwareID()) + RequestParameters.AMPERSAND + Jsonkey.HardwareIDType.getKey() + RequestParameters.EQUAL + (deviceInfo.isHardwareIDReal() ? Jsonkey.HardwareIDTypeVendor.getKey() : Jsonkey.HardwareIDTypeRandom.getKey());
        if (systemObserver.GAIDString_ != null) {
            uriString = uriString + RequestParameters.AMPERSAND + Jsonkey.GoogleAdvertisingID.getKey() + RequestParameters.EQUAL + systemObserver.GAIDString_;
        }
        if (!prefHelper.getDeviceFingerPrintID().equals(SystemObserver.BLANK)) {
            uriString = uriString + RequestParameters.AMPERSAND + Jsonkey.DeviceFingerprintID.getKey() + RequestParameters.EQUAL + prefHelper.getDeviceFingerPrintID();
        }
        if (!deviceInfo.getAppVersion().equals(SystemObserver.BLANK)) {
            uriString = uriString + RequestParameters.AMPERSAND + Jsonkey.AppVersion.getKey() + RequestParameters.EQUAL + deviceInfo.getAppVersion();
        }
        if (!prefHelper.getBranchKey().equals(SystemObserver.BLANK)) {
            uriString = uriString + RequestParameters.AMPERSAND + Jsonkey.BranchKey.getKey() + RequestParameters.EQUAL + prefHelper.getBranchKey();
        }
        return Uri.parse(uriString + "&sdk=android2.5.9");
    }
}
