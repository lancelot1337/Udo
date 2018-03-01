package cz.msebera.android.httpclient.auth;

import cz.msebera.android.httpclient.HttpHost;
import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.message.TokenParser;
import cz.msebera.android.httpclient.protocol.HTTP;
import cz.msebera.android.httpclient.util.Args;
import cz.msebera.android.httpclient.util.LangUtils;
import java.util.Locale;

@Immutable
public class AuthScope {
    public static final AuthScope ANY = new AuthScope(ANY_HOST, ANY_PORT, ANY_REALM, ANY_SCHEME);
    public static final String ANY_HOST = null;
    public static final int ANY_PORT = -1;
    public static final String ANY_REALM = null;
    public static final String ANY_SCHEME = null;
    private final String host;
    private final HttpHost origin;
    private final int port;
    private final String realm;
    private final String scheme;

    public AuthScope(String host, int port, String realm, String schemeName) {
        this.host = host == null ? ANY_HOST : host.toLowerCase(Locale.ROOT);
        if (port < 0) {
            port = ANY_PORT;
        }
        this.port = port;
        if (realm == null) {
            realm = ANY_REALM;
        }
        this.realm = realm;
        this.scheme = schemeName == null ? ANY_SCHEME : schemeName.toUpperCase(Locale.ROOT);
        this.origin = null;
    }

    public AuthScope(HttpHost origin, String realm, String schemeName) {
        Args.notNull(origin, HTTP.TARGET_HOST);
        this.host = origin.getHostName().toLowerCase(Locale.ROOT);
        this.port = origin.getPort() < 0 ? ANY_PORT : origin.getPort();
        if (realm == null) {
            realm = ANY_REALM;
        }
        this.realm = realm;
        this.scheme = schemeName == null ? ANY_SCHEME : schemeName.toUpperCase(Locale.ROOT);
        this.origin = origin;
    }

    public AuthScope(HttpHost origin) {
        this(origin, ANY_REALM, ANY_SCHEME);
    }

    public AuthScope(String host, int port, String realm) {
        this(host, port, realm, ANY_SCHEME);
    }

    public AuthScope(String host, int port) {
        this(host, port, ANY_REALM, ANY_SCHEME);
    }

    public AuthScope(AuthScope authscope) {
        Args.notNull(authscope, "Scope");
        this.host = authscope.getHost();
        this.port = authscope.getPort();
        this.realm = authscope.getRealm();
        this.scheme = authscope.getScheme();
        this.origin = authscope.getOrigin();
    }

    public HttpHost getOrigin() {
        return this.origin;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getRealm() {
        return this.realm;
    }

    public String getScheme() {
        return this.scheme;
    }

    public int match(AuthScope that) {
        int factor = 0;
        if (LangUtils.equals(this.scheme, that.scheme)) {
            factor = 0 + 1;
        } else if (!(this.scheme == ANY_SCHEME || that.scheme == ANY_SCHEME)) {
            return ANY_PORT;
        }
        if (LangUtils.equals(this.realm, that.realm)) {
            factor += 2;
        } else if (!(this.realm == ANY_REALM || that.realm == ANY_REALM)) {
            return ANY_PORT;
        }
        if (this.port == that.port) {
            factor += 4;
        } else if (!(this.port == ANY_PORT || that.port == ANY_PORT)) {
            return ANY_PORT;
        }
        if (LangUtils.equals(this.host, that.host)) {
            factor += 8;
        } else if (!(this.host == ANY_HOST || that.host == ANY_HOST)) {
            return ANY_PORT;
        }
        return factor;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof AuthScope)) {
            return super.equals(o);
        }
        AuthScope that = (AuthScope) o;
        if (LangUtils.equals(this.host, that.host) && this.port == that.port && LangUtils.equals(this.realm, that.realm) && LangUtils.equals(this.scheme, that.scheme)) {
            return true;
        }
        return false;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        if (this.scheme != null) {
            buffer.append(this.scheme.toUpperCase(Locale.ROOT));
            buffer.append(TokenParser.SP);
        }
        if (this.realm != null) {
            buffer.append('\'');
            buffer.append(this.realm);
            buffer.append('\'');
        } else {
            buffer.append("<any realm>");
        }
        if (this.host != null) {
            buffer.append('@');
            buffer.append(this.host);
            if (this.port >= 0) {
                buffer.append(':');
                buffer.append(this.port);
            }
        }
        return buffer.toString();
    }

    public int hashCode() {
        return LangUtils.hashCode(LangUtils.hashCode(LangUtils.hashCode(LangUtils.hashCode(17, this.host), this.port), this.realm), this.scheme);
    }
}
