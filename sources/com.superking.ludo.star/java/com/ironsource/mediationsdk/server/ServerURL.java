package com.ironsource.mediationsdk.server;

import android.text.TextUtils;
import android.util.Pair;
import com.ironsource.mediationsdk.config.ConfigFile;
import com.ironsource.mediationsdk.utils.IronSourceUtils;
import cz.msebera.android.httpclient.protocol.HTTP;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Vector;
import org.cocos2dx.lib.BuildConfig;

public class ServerURL {
    private static final String AMPERSAND = "&";
    private static final String APPLICATION_KEY = "applicationKey";
    private static final String APPLICATION_USER_ID = "applicationUserId";
    private static String BASE_URL_PREFIX = "https://init.supersonicads.com/sdk/v";
    private static String BASE_URL_SUFFIX = "?platform=android&";
    private static final String EQUAL = "=";
    private static final String GAID = "advId";
    private static final String IMPRESSION = "impression";
    private static final String PLACEMENT = "placementId";
    private static final String PLUGIN_FW_VERSION = "plugin_fw_v";
    private static final String PLUGIN_TYPE = "pluginType";
    private static final String PLUGIN_VERSION = "pluginVersion";
    private static final String SDK_VERSION = "sdkVersion";

    public static String getCPVProvidersURL(String applicationKey, String applicationUserId, String gaid) throws UnsupportedEncodingException {
        Vector<Pair<String, String>> array = new Vector();
        array.add(new Pair(APPLICATION_KEY, applicationKey));
        array.add(new Pair(APPLICATION_USER_ID, applicationUserId));
        array.add(new Pair(SDK_VERSION, IronSourceUtils.getSDKVersion()));
        if (!TextUtils.isEmpty(ConfigFile.getConfigFile().getPluginType())) {
            array.add(new Pair(PLUGIN_TYPE, ConfigFile.getConfigFile().getPluginType()));
        }
        if (!TextUtils.isEmpty(ConfigFile.getConfigFile().getPluginVersion())) {
            array.add(new Pair(PLUGIN_VERSION, ConfigFile.getConfigFile().getPluginVersion()));
        }
        if (!TextUtils.isEmpty(ConfigFile.getConfigFile().getPluginFrameworkVersion())) {
            array.add(new Pair(PLUGIN_FW_VERSION, ConfigFile.getConfigFile().getPluginFrameworkVersion()));
        }
        if (!TextUtils.isEmpty(gaid)) {
            array.add(new Pair(GAID, gaid));
        }
        return getBaseUrl(IronSourceUtils.getSDKVersion()) + createURLParams(array);
    }

    public static String getRequestURL(String requestUrl, boolean hit, int placementId) throws UnsupportedEncodingException {
        Vector<Pair<String, String>> array = new Vector();
        array.add(new Pair(IMPRESSION, Boolean.toString(hit)));
        array.add(new Pair(PLACEMENT, Integer.toString(placementId)));
        return requestUrl + AMPERSAND + createURLParams(array);
    }

    private static String createURLParams(Vector<Pair<String, String>> array) throws UnsupportedEncodingException {
        String str = BuildConfig.FLAVOR;
        Iterator it = array.iterator();
        while (it.hasNext()) {
            Pair<String, String> pair = (Pair) it.next();
            if (str.length() > 0) {
                str = str + AMPERSAND;
            }
            str = str + ((String) pair.first) + EQUAL + URLEncoder.encode((String) pair.second, HTTP.UTF_8);
        }
        return str;
    }

    private static String getBaseUrl(String sdkVersion) {
        return BASE_URL_PREFIX + sdkVersion + BASE_URL_SUFFIX;
    }
}
