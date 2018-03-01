package cz.msebera.android.httpclient.auth;

import cz.msebera.android.httpclient.HttpRequest;
import cz.msebera.android.httpclient.annotation.ThreadSafe;
import cz.msebera.android.httpclient.config.Lookup;
import cz.msebera.android.httpclient.params.HttpParams;
import cz.msebera.android.httpclient.protocol.ExecutionContext;
import cz.msebera.android.httpclient.protocol.HttpContext;
import cz.msebera.android.httpclient.util.Args;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ThreadSafe
@Deprecated
public final class AuthSchemeRegistry implements Lookup<AuthSchemeProvider> {
    private final ConcurrentHashMap<String, AuthSchemeFactory> registeredSchemes = new ConcurrentHashMap();

    public void register(String name, AuthSchemeFactory factory) {
        Args.notNull(name, "Name");
        Args.notNull(factory, "Authentication scheme factory");
        this.registeredSchemes.put(name.toLowerCase(Locale.ENGLISH), factory);
    }

    public void unregister(String name) {
        Args.notNull(name, "Name");
        this.registeredSchemes.remove(name.toLowerCase(Locale.ENGLISH));
    }

    public AuthScheme getAuthScheme(String name, HttpParams params) throws IllegalStateException {
        Args.notNull(name, "Name");
        AuthSchemeFactory factory = (AuthSchemeFactory) this.registeredSchemes.get(name.toLowerCase(Locale.ENGLISH));
        if (factory != null) {
            return factory.newInstance(params);
        }
        throw new IllegalStateException("Unsupported authentication scheme: " + name);
    }

    public List<String> getSchemeNames() {
        return new ArrayList(this.registeredSchemes.keySet());
    }

    public void setItems(Map<String, AuthSchemeFactory> map) {
        if (map != null) {
            this.registeredSchemes.clear();
            this.registeredSchemes.putAll(map);
        }
    }

    public AuthSchemeProvider lookup(final String name) {
        return new AuthSchemeProvider() {
            public AuthScheme create(HttpContext context) {
                return AuthSchemeRegistry.this.getAuthScheme(name, ((HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST)).getParams());
            }
        };
    }
}
