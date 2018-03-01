package com.bugsnag.android;

import android.support.annotation.NonNull;
import com.bugsnag.android.JsonStream.Streamable;
import com.facebook.share.internal.ShareConstants;
import com.unity3d.ads.metadata.MediationMetaData;
import java.io.IOException;

class User implements Streamable {
    private String email;
    private String id;
    private String name;

    User() {
    }

    User(String id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }

    User(User u) {
        this(u.id, u.email, u.name);
    }

    public void toStream(@NonNull JsonStream writer) throws IOException {
        writer.beginObject();
        writer.name(ShareConstants.WEB_DIALOG_PARAM_ID).value(this.id);
        writer.name("email").value(this.email);
        writer.name(MediationMetaData.KEY_NAME).value(this.name);
        writer.endObject();
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
