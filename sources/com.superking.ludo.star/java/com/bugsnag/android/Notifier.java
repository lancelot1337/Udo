package com.bugsnag.android;

import android.support.annotation.NonNull;
import com.bugsnag.android.JsonStream.Streamable;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import com.unity3d.ads.metadata.MediationMetaData;
import cz.msebera.android.httpclient.cookie.ClientCookie;
import java.io.IOException;

class Notifier implements Streamable {
    static final String NOTIFIER_NAME = "Android Bugsnag Notifier";
    static final String NOTIFIER_URL = "https://bugsnag.com";
    static final String NOTIFIER_VERSION = "3.8.0";
    private static final Notifier instance = new Notifier();
    private String name = NOTIFIER_NAME;
    private String url = NOTIFIER_URL;
    private String version = NOTIFIER_VERSION;

    public static Notifier getInstance() {
        return instance;
    }

    Notifier() {
    }

    public void toStream(@NonNull JsonStream writer) throws IOException {
        writer.beginObject();
        writer.name(MediationMetaData.KEY_NAME).value(this.name);
        writer.name(ClientCookie.VERSION_ATTR).value(this.version);
        writer.name(ParametersKeys.URL).value(this.url);
        writer.endObject();
    }

    public void setVersion(@NonNull String version) {
        this.version = version;
    }

    public void setURL(@NonNull String url) {
        this.url = url;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }
}
