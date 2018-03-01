package com.bugsnag.android;

import android.support.annotation.NonNull;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;

public class JsonStream extends JsonWriter {
    private final Writer out;

    public interface Streamable {
        void toStream(@NonNull JsonStream jsonStream) throws IOException;
    }

    JsonStream(Writer out) {
        super(out);
        setSerializeNulls(false);
        this.out = out;
    }

    public JsonStream name(@NonNull String name) throws IOException {
        super.name(name);
        return this;
    }

    public void value(Streamable streamable) throws IOException {
        if (streamable == null) {
            nullValue();
        } else {
            streamable.toStream(this);
        }
    }

    public void value(@NonNull File file) throws IOException {
        Throwable th;
        super.flush();
        FileReader input = null;
        try {
            FileReader input2 = new FileReader(file);
            try {
                IOUtils.copy(input2, this.out);
                IOUtils.closeQuietly(input2);
                this.out.flush();
            } catch (Throwable th2) {
                th = th2;
                input = input2;
                IOUtils.closeQuietly(input);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            IOUtils.closeQuietly(input);
            throw th;
        }
    }
}
