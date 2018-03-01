package io.branch.referral;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Process;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import com.ironsource.sdk.utils.Constants;
import com.ironsource.sdk.utils.Constants.RequestParameters;
import com.unity3d.ads.metadata.MediationMetaData;
import io.branch.referral.Defines.Jsonkey;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import org.cocos2dx.lib.BuildConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class SystemObserver {
    public static final String BLANK = "bnc_no_value";
    private static final int GAID_FETCH_TIME_OUT = 1500;
    private static final int STATE_FRESH_INSTALL = 0;
    private static final int STATE_NO_CHANGE = 1;
    private static final int STATE_UPDATE = 2;
    String GAIDString_ = null;
    int LATVal_ = STATE_FRESH_INSTALL;
    private Context context_;
    private boolean isRealHardwareId;

    interface GAdsParamsFetchEvents {
        void onGAdsFetchFinished();
    }

    private class GAdsPrefetchTask extends BranchAsyncTask<Void, Void, Void> {
        private final GAdsParamsFetchEvents callback_;

        public GAdsPrefetchTask(GAdsParamsFetchEvents callback) {
            this.callback_ = callback;
        }

        protected Void doInBackground(Void... params) {
            final CountDownLatch latch = new CountDownLatch(SystemObserver.STATE_NO_CHANGE);
            new Thread(new Runnable() {
                public void run() {
                    Process.setThreadPriority(-19);
                    Object adInfoObj = SystemObserver.this.getAdInfoObject();
                    SystemObserver.this.getAdvertisingId(adInfoObj);
                    SystemObserver.this.getLATValue(adInfoObj);
                    latch.countDown();
                }
            }).start();
            try {
                latch.await(1500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (this.callback_ != null) {
                this.callback_.onGAdsFetchFinished();
            }
        }
    }

    public SystemObserver(Context context) {
        this.context_ = context;
        this.isRealHardwareId = true;
    }

    public String getUniqueID(boolean debug) {
        if (this.context_ == null) {
            return BLANK;
        }
        String androidID = null;
        if (!debug) {
            androidID = Secure.getString(this.context_.getContentResolver(), "android_id");
        }
        if (androidID != null) {
            return androidID;
        }
        androidID = UUID.randomUUID().toString();
        this.isRealHardwareId = false;
        return androidID;
    }

    public boolean hasRealHardwareId() {
        return this.isRealHardwareId;
    }

    public String getPackageName() {
        String packageName = BuildConfig.FLAVOR;
        try {
            return this.context_.getPackageManager().getPackageInfo(this.context_.getPackageName(), STATE_FRESH_INSTALL).packageName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return packageName;
        }
    }

    public String getURIScheme() {
        return getURIScheme(this.context_.getPackageName());
    }

    public String getURIScheme(String packageName) {
        Throwable th;
        String scheme = BLANK;
        if (!isLowOnMemory()) {
            try {
                JarFile jf = null;
                InputStream is = null;
                try {
                    JarFile jf2 = new JarFile(this.context_.getPackageManager().getApplicationInfo(packageName, STATE_FRESH_INSTALL).publicSourceDir);
                    try {
                        is = jf2.getInputStream(jf2.getEntry("AndroidManifest.xml"));
                        byte[] xml = new byte[is.available()];
                        is.read(xml);
                        scheme = new ApkParser().decompressXML(xml);
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException e) {
                                jf = jf2;
                            }
                        }
                        if (jf2 != null) {
                            jf2.close();
                        }
                        jf = jf2;
                    } catch (Exception e2) {
                        jf = jf2;
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException e3) {
                            }
                        }
                        if (jf != null) {
                            jf.close();
                        }
                        return scheme;
                    } catch (Throwable th2) {
                        th = th2;
                        jf = jf2;
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException e4) {
                                throw th;
                            }
                        }
                        if (jf != null) {
                            jf.close();
                        }
                        throw th;
                    }
                } catch (Exception e5) {
                    if (is != null) {
                        is.close();
                    }
                    if (jf != null) {
                        jf.close();
                    }
                    return scheme;
                } catch (Throwable th3) {
                    th = th3;
                    if (is != null) {
                        is.close();
                    }
                    if (jf != null) {
                        jf.close();
                    }
                    throw th;
                }
            } catch (NameNotFoundException e6) {
            }
        }
        return scheme;
    }

    private boolean isLowOnMemory() {
        ActivityManager activityManager = (ActivityManager) this.context_.getSystemService("activity");
        MemoryInfo mi = new MemoryInfo();
        activityManager.getMemoryInfo(mi);
        return mi.lowMemory;
    }

    @SuppressLint({"NewApi"})
    public JSONArray getListOfApps() {
        JSONArray arr = new JSONArray();
        PackageManager pm = this.context_.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(128);
        if (packages != null) {
            for (ApplicationInfo appInfo : packages) {
                if ((appInfo.flags & STATE_NO_CHANGE) != STATE_NO_CHANGE) {
                    JSONObject packObj = new JSONObject();
                    try {
                        CharSequence labelCs = appInfo.loadLabel(pm);
                        String label = labelCs == null ? null : labelCs.toString();
                        if (label != null) {
                            packObj.put(MediationMetaData.KEY_NAME, label);
                        }
                        String packName = appInfo.packageName;
                        if (packName != null) {
                            packObj.put(Jsonkey.AppIdentifier.getKey(), packName);
                            String uriScheme = getURIScheme(packName);
                            if (!uriScheme.equals(BLANK)) {
                                packObj.put(Jsonkey.URIScheme.getKey(), uriScheme);
                            }
                        }
                        String pSourceDir = appInfo.publicSourceDir;
                        if (pSourceDir != null) {
                            packObj.put("public_source_dir", pSourceDir);
                        }
                        String sourceDir = appInfo.sourceDir;
                        if (sourceDir != null) {
                            packObj.put("source_dir", sourceDir);
                        }
                        PackageInfo packInfo = pm.getPackageInfo(appInfo.packageName, 4096);
                        if (packInfo != null) {
                            if (packInfo.versionCode >= 9) {
                                packObj.put("install_date", packInfo.firstInstallTime);
                                packObj.put("last_update_date", packInfo.lastUpdateTime);
                            }
                            packObj.put("version_code", packInfo.versionCode);
                            if (packInfo.versionName != null) {
                                packObj.put("version_name", packInfo.versionName);
                            }
                        }
                        packObj.put(Jsonkey.OS.getKey(), getOS());
                        arr.put(packObj);
                    } catch (JSONException e) {
                    } catch (NameNotFoundException e2) {
                    }
                }
            }
        }
        return arr;
    }

    public String getAppVersion() {
        try {
            PackageInfo packageInfo = this.context_.getPackageManager().getPackageInfo(this.context_.getPackageName(), STATE_FRESH_INSTALL);
            if (packageInfo.versionName != null) {
                return packageInfo.versionName;
            }
            return BLANK;
        } catch (NameNotFoundException e) {
            return BLANK;
        }
    }

    public String getPhoneBrand() {
        return Build.MANUFACTURER;
    }

    public String getPhoneModel() {
        return Build.MODEL;
    }

    public String getISO2CountryCode() {
        if (Locale.getDefault() != null) {
            return Locale.getDefault().getCountry();
        }
        return BuildConfig.FLAVOR;
    }

    public String getISO2LanguageCode() {
        if (Locale.getDefault() != null) {
            return Locale.getDefault().getLanguage();
        }
        return BuildConfig.FLAVOR;
    }

    public String getOS() {
        return Constants.JAVASCRIPT_INTERFACE_NAME;
    }

    public int getOSVersion() {
        return VERSION.SDK_INT;
    }

    public boolean isSimulator() {
        return Build.FINGERPRINT.contains("generic");
    }

    @SuppressLint({"NewApi"})
    public int getUpdateState() {
        PrefHelper pHelper = PrefHelper.getInstance(this.context_);
        String currAppVersion = getAppVersion();
        if (BLANK.equals(pHelper.getAppVersion())) {
            if (VERSION.SDK_INT >= 9) {
                try {
                    PackageInfo packageInfo = this.context_.getPackageManager().getPackageInfo(this.context_.getPackageName(), STATE_FRESH_INSTALL);
                    return packageInfo.lastUpdateTime != packageInfo.firstInstallTime ? STATE_UPDATE : STATE_FRESH_INSTALL;
                } catch (NameNotFoundException e) {
                }
            }
            return STATE_FRESH_INSTALL;
        } else if (pHelper.getAppVersion().equals(currAppVersion)) {
            return STATE_NO_CHANGE;
        } else {
            return STATE_UPDATE;
        }
    }

    public DisplayMetrics getScreenDisplay() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) this.context_.getSystemService("window")).getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    public boolean getWifiConnected() {
        if (this.context_.checkCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE") != 0) {
            return false;
        }
        NetworkInfo wifiInfo = ((ConnectivityManager) this.context_.getSystemService("connectivity")).getNetworkInfo(STATE_NO_CHANGE);
        if (wifiInfo == null || !wifiInfo.isConnected()) {
            return false;
        }
        return true;
    }

    public Object getAdInfoObject() {
        Object adInfoObj = null;
        try {
            Class[] clsArr = new Class[STATE_NO_CHANGE];
            clsArr[STATE_FRESH_INSTALL] = Context.class;
            Method getAdvertisingIdInfoMethod = Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient").getMethod("getAdvertisingIdInfo", clsArr);
            Object[] objArr = new Object[STATE_NO_CHANGE];
            objArr[STATE_FRESH_INSTALL] = this.context_;
            adInfoObj = getAdvertisingIdInfoMethod.invoke(null, objArr);
        } catch (Throwable th) {
        }
        return adInfoObj;
    }

    public String getAdvertisingId(Object adInfoObj) {
        try {
            this.GAIDString_ = (String) adInfoObj.getClass().getMethod("getId", new Class[STATE_FRESH_INSTALL]).invoke(adInfoObj, new Object[STATE_FRESH_INSTALL]);
        } catch (Exception e) {
        }
        return this.GAIDString_;
    }

    public int getLATValue(Object adInfoObj) {
        try {
            this.LATVal_ = ((Boolean) adInfoObj.getClass().getMethod(RequestParameters.isLAT, new Class[STATE_FRESH_INSTALL]).invoke(adInfoObj, new Object[STATE_FRESH_INSTALL])).booleanValue() ? STATE_NO_CHANGE : STATE_FRESH_INSTALL;
        } catch (Exception e) {
        }
        return this.LATVal_;
    }

    public boolean prefetchGAdsParams(GAdsParamsFetchEvents callback) {
        if (!TextUtils.isEmpty(this.GAIDString_)) {
            return false;
        }
        new GAdsPrefetchTask(callback).executeTask(new Void[STATE_FRESH_INSTALL]);
        return true;
    }

    public static String getLocalIPAddress() {
        String ipAddress = BuildConfig.FLAVOR;
        try {
            for (NetworkInterface netInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress address : Collections.list(netInterface.getInetAddresses())) {
                    if (!address.isLoopbackAddress()) {
                        String ip = address.getHostAddress();
                        if (ip.indexOf(58) < 0) {
                            ipAddress = ip;
                            break;
                        }
                    }
                }
            }
        } catch (Throwable th) {
        }
        return ipAddress;
    }
}
