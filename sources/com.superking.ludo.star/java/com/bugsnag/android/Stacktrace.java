package com.bugsnag.android;

import android.support.annotation.NonNull;
import com.bugsnag.android.JsonStream.Streamable;
import com.facebook.internal.AnalyticsEvents;
import com.ironsource.sdk.utils.Constants.ParametersKeys;
import java.io.IOException;

class Stacktrace implements Streamable {
    final Configuration config;
    final StackTraceElement[] stacktrace;

    Stacktrace(Configuration config, StackTraceElement[] stacktrace) {
        this.config = config;
        this.stacktrace = stacktrace;
    }

    public void toStream(@NonNull JsonStream writer) throws IOException {
        writer.beginArray();
        for (StackTraceElement el : this.stacktrace) {
            try {
                writer.beginObject();
                writer.name(ParametersKeys.METHOD).value(el.getClassName() + "." + el.getMethodName());
                writer.name(ParametersKeys.FILE).value(el.getFileName() == null ? AnalyticsEvents.PARAMETER_DIALOG_OUTCOME_VALUE_UNKNOWN : el.getFileName());
                writer.name("lineNumber").value((long) el.getLineNumber());
                if (this.config.inProject(el.getClassName())) {
                    writer.name("inProject").value(true);
                }
                writer.endObject();
            } catch (Exception lineEx) {
                lineEx.printStackTrace(System.err);
            }
        }
        writer.endArray();
    }
}
