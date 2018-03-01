package cz.msebera.android.httpclient.cookie;

import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.protocol.HTTP;
import cz.msebera.android.httpclient.util.Args;
import cz.msebera.android.httpclient.util.TextUtils;
import java.util.Locale;

@Immutable
public final class CookieOrigin {
    private final String host;
    private final String path;
    private final int port;
    private final boolean secure;

    public CookieOrigin(String host, int port, String path, boolean secure) {
        Args.notBlank(host, HTTP.TARGET_HOST);
        Args.notNegative(port, "Port");
        Args.notNull(path, "Path");
        this.host = host.toLowerCase(Locale.ROOT);
        this.port = port;
        if (TextUtils.isBlank(path)) {
            this.path = "/";
        } else {
            this.path = path;
        }
        this.secure = secure;
    }

    public String getHost() {
        return this.host;
    }

    public String getPath() {
        return this.path;
    }

    public int getPort() {
        return this.port;
    }

    public boolean isSecure() {
        return this.secure;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        if (this.secure) {
            buffer.append("(secure)");
        }
        buffer.append(this.host);
        buffer.append(':');
        buffer.append(Integer.toString(this.port));
        buffer.append(this.path);
        buffer.append(']');
        return buffer.toString();
    }
}
