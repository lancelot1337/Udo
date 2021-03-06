package cz.msebera.android.httpclient.impl.client;

import cz.msebera.android.httpclient.HttpHost;
import cz.msebera.android.httpclient.annotation.ThreadSafe;
import cz.msebera.android.httpclient.auth.AuthScheme;
import cz.msebera.android.httpclient.client.AuthCache;
import cz.msebera.android.httpclient.conn.SchemePortResolver;
import cz.msebera.android.httpclient.conn.UnsupportedSchemeException;
import cz.msebera.android.httpclient.extras.HttpClientAndroidLog;
import cz.msebera.android.httpclient.impl.conn.DefaultSchemePortResolver;
import cz.msebera.android.httpclient.util.Args;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ThreadSafe
public class BasicAuthCache implements AuthCache {
    public HttpClientAndroidLog log;
    private final Map<HttpHost, byte[]> map;
    private final SchemePortResolver schemePortResolver;

    public BasicAuthCache(SchemePortResolver schemePortResolver) {
        this.log = new HttpClientAndroidLog(getClass());
        this.map = new ConcurrentHashMap();
        if (schemePortResolver == null) {
            schemePortResolver = DefaultSchemePortResolver.INSTANCE;
        }
        this.schemePortResolver = schemePortResolver;
    }

    public BasicAuthCache() {
        this(null);
    }

    protected HttpHost getKey(HttpHost host) {
        if (host.getPort() > 0) {
            return host;
        }
        try {
            return new HttpHost(host.getHostName(), this.schemePortResolver.resolve(host), host.getSchemeName());
        } catch (UnsupportedSchemeException e) {
            return host;
        }
    }

    public void put(HttpHost host, AuthScheme authScheme) {
        Args.notNull(host, "HTTP host");
        if (authScheme != null) {
            if (authScheme instanceof Serializable) {
                try {
                    ByteArrayOutputStream buf = new ByteArrayOutputStream();
                    ObjectOutputStream out = new ObjectOutputStream(buf);
                    out.writeObject(authScheme);
                    out.close();
                    this.map.put(getKey(host), buf.toByteArray());
                } catch (IOException ex) {
                    if (this.log.isWarnEnabled()) {
                        this.log.warn("Unexpected I/O error while serializing auth scheme", ex);
                    }
                }
            } else if (this.log.isDebugEnabled()) {
                this.log.debug("Auth scheme " + authScheme.getClass() + " is not serializable");
            }
        }
    }

    public AuthScheme get(HttpHost host) {
        Args.notNull(host, "HTTP host");
        byte[] bytes = (byte[]) this.map.get(getKey(host));
        if (bytes == null) {
            return null;
        }
        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
            AuthScheme authScheme = (AuthScheme) in.readObject();
            in.close();
            return authScheme;
        } catch (IOException ex) {
            if (this.log.isWarnEnabled()) {
                this.log.warn("Unexpected I/O error while de-serializing auth scheme", ex);
            }
            return null;
        } catch (ClassNotFoundException ex2) {
            if (this.log.isWarnEnabled()) {
                this.log.warn("Unexpected error while de-serializing auth scheme", ex2);
            }
            return null;
        }
    }

    public void remove(HttpHost host) {
        Args.notNull(host, "HTTP host");
        this.map.remove(getKey(host));
    }

    public void clear() {
        this.map.clear();
    }

    public String toString() {
        return this.map.toString();
    }
}
