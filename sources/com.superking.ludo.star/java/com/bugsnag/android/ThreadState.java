package com.bugsnag.android;

import android.support.annotation.NonNull;
import com.bugsnag.android.JsonStream.Streamable;
import com.facebook.share.internal.ShareConstants;
import com.unity3d.ads.metadata.MediationMetaData;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

class ThreadState implements Streamable {
    private static final String THREAD_TYPE = "android";
    final Configuration config;

    ThreadState(Configuration config) {
        this.config = config;
    }

    public void toStream(@NonNull JsonStream writer) throws IOException {
        long currentId = Thread.currentThread().getId();
        Map<Thread, StackTraceElement[]> liveThreads = Thread.getAllStackTraces();
        Object[] keys = liveThreads.keySet().toArray();
        Arrays.sort(keys, new Comparator<Object>() {
            public int compare(Object a, Object b) {
                return Long.valueOf(((Thread) a).getId()).compareTo(Long.valueOf(((Thread) b).getId()));
            }
        });
        writer.beginArray();
        for (Thread thread : keys) {
            if (thread.getId() != currentId) {
                StackTraceElement[] stacktrace = (StackTraceElement[]) liveThreads.get(thread);
                writer.beginObject();
                writer.name(ShareConstants.WEB_DIALOG_PARAM_ID).value(thread.getId());
                writer.name(MediationMetaData.KEY_NAME).value(thread.getName());
                writer.name(EventEntry.COLUMN_NAME_TYPE).value(THREAD_TYPE);
                writer.name("stacktrace").value(new Stacktrace(this.config, stacktrace));
                writer.endObject();
            }
        }
        writer.endArray();
    }
}
