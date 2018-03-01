package com.bugsnag.android;

import android.support.annotation.NonNull;
import com.bugsnag.android.JsonStream.Streamable;
import java.io.File;
import java.io.IOException;

public class Report implements Streamable {
    private String apiKey;
    private Error error;
    private final File errorFile;
    private Notifier notifier;

    Report(@NonNull String apiKey, File errorFile) {
        this.apiKey = apiKey;
        this.error = null;
        this.errorFile = errorFile;
        this.notifier = Notifier.getInstance();
    }

    Report(@NonNull String apiKey, Error error) {
        this.apiKey = apiKey;
        this.error = error;
        this.errorFile = null;
        this.notifier = Notifier.getInstance();
    }

    public void toStream(@NonNull JsonStream writer) throws IOException {
        writer.beginObject();
        writer.name("apiKey").value(this.apiKey);
        writer.name("notifier").value(this.notifier);
        writer.name(EventEntry.TABLE_NAME).beginArray();
        if (this.error != null) {
            writer.value(this.error);
        }
        if (this.errorFile != null) {
            writer.value(this.errorFile);
        }
        writer.endArray();
        writer.endObject();
    }

    public Error getError() {
        return this.error;
    }

    public void setApiKey(@NonNull String apiKey) {
        this.apiKey = apiKey;
    }

    public void setNotifierVersion(@NonNull String version) {
        this.notifier.setVersion(version);
    }

    public void setNotifierName(@NonNull String name) {
        this.notifier.setName(name);
    }

    public void setNotifierURL(@NonNull String url) {
        this.notifier.setURL(url);
    }
}
