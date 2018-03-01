package com.bugsnag.android;

import android.support.annotation.NonNull;
import com.bugsnag.android.JsonStream.Streamable;
import com.facebook.share.internal.ShareConstants;
import java.io.IOException;

class Exceptions implements Streamable {
    private final Configuration config;
    private Throwable exception;

    Exceptions(Configuration config, Throwable exception) {
        this.config = config;
        this.exception = exception;
    }

    public void toStream(@NonNull JsonStream writer) throws IOException {
        writer.beginArray();
        for (Throwable currentEx = this.exception; currentEx != null; currentEx = currentEx.getCause()) {
            if (currentEx instanceof Streamable) {
                ((Streamable) currentEx).toStream(writer);
            } else {
                exceptionToStream(writer, getExceptionName(currentEx), currentEx.getLocalizedMessage(), currentEx.getStackTrace());
            }
        }
        writer.endArray();
    }

    private String getExceptionName(Throwable t) {
        if (t instanceof BugsnagException) {
            return ((BugsnagException) t).getName();
        }
        return t.getClass().getName();
    }

    private void exceptionToStream(JsonStream writer, String name, String message, StackTraceElement[] frames) throws IOException {
        Streamable stacktrace = new Stacktrace(this.config, frames);
        writer.beginObject();
        writer.name("errorClass").value(name);
        writer.name(ShareConstants.WEB_DIALOG_PARAM_MESSAGE).value(message);
        writer.name(EventEntry.COLUMN_NAME_TYPE).value(this.config.defaultExceptionType);
        writer.name("stacktrace").value(stacktrace);
        writer.endObject();
    }
}
