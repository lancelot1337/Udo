package io.branch.referral;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import com.ironsource.sdk.utils.Constants.RequestParameters;
import cz.msebera.android.httpclient.protocol.HTTP;
import io.branch.referral.Defines.Jsonkey;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

public class InstallListener extends BroadcastReceiver {
    private static IInstallReferrerEvents callback_ = null;
    private static String googleSearchInstallReferrerID_ = SystemObserver.BLANK;
    private static String installID_ = SystemObserver.BLANK;
    private static boolean isWaitingForReferrer;

    interface IInstallReferrerEvents {
        void onInstallReferrerEventsFinished();
    }

    public static void startInstallReferrerTime(long delay) {
        isWaitingForReferrer = true;
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (InstallListener.callback_ != null) {
                    InstallListener.callback_.onInstallReferrerEventsFinished();
                    InstallListener.callback_ = null;
                    InstallListener.isWaitingForReferrer = false;
                }
            }
        }, delay);
    }

    public void onReceive(Context context, Intent intent) {
        String rawReferrerString = intent.getStringExtra("referrer");
        if (rawReferrerString != null) {
            try {
                rawReferrerString = URLDecoder.decode(rawReferrerString, HTTP.UTF_8);
                HashMap<String, String> referrerMap = new HashMap();
                for (String referrerParam : rawReferrerString.split(RequestParameters.AMPERSAND)) {
                    String[] keyValue = referrerParam.split(RequestParameters.EQUAL);
                    if (keyValue.length > 1) {
                        referrerMap.put(URLDecoder.decode(keyValue[0], HTTP.UTF_8), URLDecoder.decode(keyValue[1], HTTP.UTF_8));
                    }
                }
                if (referrerMap.containsKey(Jsonkey.LinkClickID.getKey())) {
                    installID_ = (String) referrerMap.get(Jsonkey.LinkClickID.getKey());
                    if (isWaitingForReferrer) {
                        PrefHelper.getInstance(context).setLinkClickIdentifier(installID_);
                    }
                }
                if (referrerMap.containsKey(Jsonkey.GoogleSearchInstallReferrer.getKey())) {
                    googleSearchInstallReferrerID_ = (String) referrerMap.get(Jsonkey.GoogleSearchInstallReferrer.getKey());
                }
                if (callback_ != null) {
                    callback_.onInstallReferrerEventsFinished();
                    callback_ = null;
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                Log.w("BranchSDK", "Illegal characters in url encoded string");
            }
        }
    }

    public static String getInstallationID() {
        return installID_;
    }

    public static void setListener(IInstallReferrerEvents installReferrerFetch) {
        callback_ = installReferrerFetch;
    }

    public static String getGoogleSearchInstallReferrerID() {
        return googleSearchInstallReferrerID_;
    }
}
