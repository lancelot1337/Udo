package cz.msebera.android.httpclient.impl.client.cache;

import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.client.cache.Resource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Immutable
public class HeapResource implements Resource {
    private static final long serialVersionUID = -2078599905620463394L;
    private final byte[] b;

    public HeapResource(byte[] b) {
        this.b = b;
    }

    byte[] getByteArray() {
        return this.b;
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(this.b);
    }

    public long length() {
        return (long) this.b.length;
    }

    public void dispose() {
    }
}
