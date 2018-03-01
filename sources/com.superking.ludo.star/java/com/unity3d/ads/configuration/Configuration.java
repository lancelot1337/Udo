package com.unity3d.ads.configuration;

import com.ironsource.sdk.utils.Constants.ParametersKeys;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.properties.SdkProperties;
import com.unity3d.ads.request.WebRequest;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.cookie.ClientCookie;
import java.io.IOException;
import java.net.MalformedURLException;
import org.json.JSONException;
import org.json.JSONObject;

public class Configuration {
    private String _url;
    private Class[] _webAppApiClassList;
    private String _webViewData;
    private String _webViewHash;
    private String _webViewUrl;
    private String _webViewVersion;

    public Configuration(String configUrl) {
        this._url = configUrl;
    }

    public void setConfigUrl(String url) {
        this._url = url;
    }

    public String getConfigUrl() {
        return this._url;
    }

    public void setWebAppApiClassList(Class[] apiClassList) {
        this._webAppApiClassList = apiClassList;
    }

    public Class[] getWebAppApiClassList() {
        return this._webAppApiClassList;
    }

    public String getWebViewUrl() {
        return this._webViewUrl;
    }

    public void setWebViewUrl(String url) {
        this._webViewUrl = url;
    }

    public String getWebViewHash() {
        return this._webViewHash;
    }

    public void setWebViewHash(String hash) {
        this._webViewHash = hash;
    }

    public String getWebViewVersion() {
        return this._webViewVersion;
    }

    public String getWebViewData() {
        return this._webViewData;
    }

    public void setWebViewData(String data) {
        this._webViewData = data;
    }

    protected String buildQueryString() {
        return "?ts=" + System.currentTimeMillis() + "&sdkVersion=" + SdkProperties.getVersionCode() + "&sdkVersionName=" + SdkProperties.getVersionName();
    }

    protected void makeRequest() throws IOException, JSONException {
        if (this._url == null) {
            throw new MalformedURLException("Base URL is null");
        }
        String url = this._url + buildQueryString();
        DeviceLog.debug("Requesting configuration with: " + url);
        JSONObject config = new JSONObject(new WebRequest(url, HttpGet.METHOD_NAME, null).makeRequest());
        this._webViewUrl = config.getString(ParametersKeys.URL);
        if (!config.isNull("hash")) {
            this._webViewHash = config.getString("hash");
        }
        if (config.has(ClientCookie.VERSION_ATTR)) {
            this._webViewVersion = config.getString(ClientCookie.VERSION_ATTR);
        }
        if (this._webViewUrl == null || this._webViewUrl.isEmpty()) {
            throw new MalformedURLException("Invalid data. Web view URL is null or empty");
        }
    }
}
