package cz.msebera.android.httpclient.impl.io;

import com.facebook.appevents.AppEventsConstants;
import cz.msebera.android.httpclient.annotation.NotThreadSafe;
import cz.msebera.android.httpclient.io.SessionOutputBuffer;
import java.io.IOException;
import java.io.OutputStream;
import org.cocos2dx.lib.BuildConfig;

@NotThreadSafe
public class ChunkedOutputStream extends OutputStream {
    private final byte[] cache;
    private int cachePosition;
    private boolean closed;
    private final SessionOutputBuffer out;
    private boolean wroteLastChunk;

    @Deprecated
    public ChunkedOutputStream(SessionOutputBuffer out, int bufferSize) throws IOException {
        this(bufferSize, out);
    }

    @Deprecated
    public ChunkedOutputStream(SessionOutputBuffer out) throws IOException {
        this(2048, out);
    }

    public ChunkedOutputStream(int bufferSize, SessionOutputBuffer out) {
        this.cachePosition = 0;
        this.wroteLastChunk = false;
        this.closed = false;
        this.cache = new byte[bufferSize];
        this.out = out;
    }

    protected void flushCache() throws IOException {
        if (this.cachePosition > 0) {
            this.out.writeLine(Integer.toHexString(this.cachePosition));
            this.out.write(this.cache, 0, this.cachePosition);
            this.out.writeLine(BuildConfig.FLAVOR);
            this.cachePosition = 0;
        }
    }

    protected void flushCacheWithAppend(byte[] bufferToAppend, int off, int len) throws IOException {
        this.out.writeLine(Integer.toHexString(this.cachePosition + len));
        this.out.write(this.cache, 0, this.cachePosition);
        this.out.write(bufferToAppend, off, len);
        this.out.writeLine(BuildConfig.FLAVOR);
        this.cachePosition = 0;
    }

    protected void writeClosingChunk() throws IOException {
        this.out.writeLine(AppEventsConstants.EVENT_PARAM_VALUE_NO);
        this.out.writeLine(BuildConfig.FLAVOR);
    }

    public void finish() throws IOException {
        if (!this.wroteLastChunk) {
            flushCache();
            writeClosingChunk();
            this.wroteLastChunk = true;
        }
    }

    public void write(int b) throws IOException {
        if (this.closed) {
            throw new IOException("Attempted write to closed stream.");
        }
        this.cache[this.cachePosition] = (byte) b;
        this.cachePosition++;
        if (this.cachePosition == this.cache.length) {
            flushCache();
        }
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte[] src, int off, int len) throws IOException {
        if (this.closed) {
            throw new IOException("Attempted write to closed stream.");
        } else if (len >= this.cache.length - this.cachePosition) {
            flushCacheWithAppend(src, off, len);
        } else {
            System.arraycopy(src, off, this.cache, this.cachePosition, len);
            this.cachePosition += len;
        }
    }

    public void flush() throws IOException {
        flushCache();
        this.out.flush();
    }

    public void close() throws IOException {
        if (!this.closed) {
            this.closed = true;
            finish();
            this.out.flush();
        }
    }
}