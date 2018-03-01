package com.bugsnag.android;

import android.support.annotation.NonNull;
import com.bugsnag.android.JsonStream.Streamable;
import com.facebook.internal.NativeProtocol;
import java.io.IOException;

public enum Severity implements Streamable {
    ERROR(NativeProtocol.BRIDGE_ARG_ERROR_BUNDLE),
    WARNING("warning"),
    INFO("info");
    
    private final String name;

    private Severity(String name) {
        this.name = name;
    }

    public void toStream(@NonNull JsonStream writer) throws IOException {
        writer.value(this.name);
    }
}
