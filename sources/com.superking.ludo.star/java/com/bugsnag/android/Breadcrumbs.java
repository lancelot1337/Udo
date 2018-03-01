package com.bugsnag.android;

import android.support.annotation.NonNull;
import com.bugsnag.android.JsonStream.Streamable;
import com.unity3d.ads.metadata.MediationMetaData;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

class Breadcrumbs implements Streamable {
    private static final int DEFAULT_MAX_SIZE = 20;
    private static final int MAX_PAYLOAD_SIZE = 4096;
    private int maxSize = DEFAULT_MAX_SIZE;
    final Queue<Breadcrumb> store = new ConcurrentLinkedQueue();

    private static class Breadcrumb implements Streamable {
        private static final String DEFAULT_NAME = "manual";
        private static final int MAX_MESSAGE_LENGTH = 140;
        private static final String MESSAGE_METAKEY = "message";
        private final String METADATA_KEY;
        private final String NAME_KEY;
        private final String TIMESTAMP_KEY;
        private final String TYPE_KEY;
        final Map<String, String> metadata;
        final String name;
        final String timestamp;
        final BreadcrumbType type;

        Breadcrumb(@NonNull String message) {
            this.TIMESTAMP_KEY = EventEntry.COLUMN_NAME_TIMESTAMP;
            this.NAME_KEY = MediationMetaData.KEY_NAME;
            this.METADATA_KEY = "metaData";
            this.TYPE_KEY = EventEntry.COLUMN_NAME_TYPE;
            this.timestamp = DateUtils.toISO8601(new Date());
            this.type = BreadcrumbType.MANUAL;
            this.metadata = Collections.singletonMap(MESSAGE_METAKEY, message.substring(0, Math.min(message.length(), MAX_MESSAGE_LENGTH)));
            this.name = DEFAULT_NAME;
        }

        Breadcrumb(@NonNull String name, BreadcrumbType type, Map<String, String> metadata) {
            this.TIMESTAMP_KEY = EventEntry.COLUMN_NAME_TIMESTAMP;
            this.NAME_KEY = MediationMetaData.KEY_NAME;
            this.METADATA_KEY = "metaData";
            this.TYPE_KEY = EventEntry.COLUMN_NAME_TYPE;
            this.timestamp = DateUtils.toISO8601(new Date());
            this.type = type;
            this.metadata = metadata;
            this.name = name;
        }

        public void toStream(@NonNull JsonStream writer) throws IOException {
            writer.beginObject();
            writer.name(EventEntry.COLUMN_NAME_TIMESTAMP).value(this.timestamp);
            writer.name(MediationMetaData.KEY_NAME).value(this.name);
            writer.name(EventEntry.COLUMN_NAME_TYPE).value(this.type.toString());
            writer.name("metaData");
            writer.beginObject();
            for (Entry<String, String> entry : this.metadata.entrySet()) {
                writer.name((String) entry.getKey()).value((String) entry.getValue());
            }
            writer.endObject();
            writer.endObject();
        }

        public int payloadSize() throws IOException {
            StringWriter writer = new StringWriter();
            toStream(new JsonStream(writer));
            return writer.toString().length();
        }
    }

    Breadcrumbs() {
    }

    public void toStream(@NonNull JsonStream writer) throws IOException {
        writer.beginArray();
        for (Breadcrumb breadcrumb : this.store) {
            breadcrumb.toStream(writer);
        }
        writer.endArray();
    }

    void add(@NonNull String message) {
        addToStore(new Breadcrumb(message));
    }

    void add(@NonNull String name, BreadcrumbType type, Map<String, String> metadata) {
        addToStore(new Breadcrumb(name, type, metadata));
    }

    void clear() {
        this.store.clear();
    }

    void setSize(int size) {
        if (size > this.store.size()) {
            this.maxSize = size;
            return;
        }
        while (this.store.size() > size) {
            this.store.poll();
        }
    }

    private void addToStore(Breadcrumb breadcrumb) {
        try {
            if (breadcrumb.payloadSize() > MAX_PAYLOAD_SIZE) {
                Logger.warn("Dropping breadcrumb because payload exceeds 4KB limit");
                return;
            }
            if (this.store.size() >= this.maxSize) {
                this.store.poll();
            }
            this.store.add(breadcrumb);
        } catch (IOException ex) {
            Logger.warn("Dropping breadcrumb because it could not be serialized", ex);
        }
    }
}
