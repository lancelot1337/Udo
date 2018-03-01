package cz.msebera.android.httpclient.client.protocol;

import cz.msebera.android.httpclient.annotation.NotThreadSafe;
import cz.msebera.android.httpclient.auth.AuthSchemeProvider;
import cz.msebera.android.httpclient.auth.AuthState;
import cz.msebera.android.httpclient.client.AuthCache;
import cz.msebera.android.httpclient.client.CookieStore;
import cz.msebera.android.httpclient.client.CredentialsProvider;
import cz.msebera.android.httpclient.client.config.RequestConfig;
import cz.msebera.android.httpclient.config.Lookup;
import cz.msebera.android.httpclient.conn.routing.HttpRoute;
import cz.msebera.android.httpclient.conn.routing.RouteInfo;
import cz.msebera.android.httpclient.cookie.CookieOrigin;
import cz.msebera.android.httpclient.cookie.CookieSpec;
import cz.msebera.android.httpclient.cookie.CookieSpecProvider;
import cz.msebera.android.httpclient.protocol.BasicHttpContext;
import cz.msebera.android.httpclient.protocol.HttpContext;
import cz.msebera.android.httpclient.protocol.HttpCoreContext;
import java.net.URI;
import java.util.List;

@NotThreadSafe
public class HttpClientContext extends HttpCoreContext {
    public static final String AUTHSCHEME_REGISTRY = "http.authscheme-registry";
    public static final String AUTH_CACHE = "http.auth.auth-cache";
    public static final String COOKIESPEC_REGISTRY = "http.cookiespec-registry";
    public static final String COOKIE_ORIGIN = "http.cookie-origin";
    public static final String COOKIE_SPEC = "http.cookie-spec";
    public static final String COOKIE_STORE = "http.cookie-store";
    public static final String CREDS_PROVIDER = "http.auth.credentials-provider";
    public static final String HTTP_ROUTE = "http.route";
    public static final String PROXY_AUTH_STATE = "http.auth.proxy-scope";
    public static final String REDIRECT_LOCATIONS = "http.protocol.redirect-locations";
    public static final String REQUEST_CONFIG = "http.request-config";
    public static final String TARGET_AUTH_STATE = "http.auth.target-scope";
    public static final String USER_TOKEN = "http.user-token";

    public static HttpClientContext adapt(HttpContext context) {
        if (context instanceof HttpClientContext) {
            return (HttpClientContext) context;
        }
        return new HttpClientContext(context);
    }

    public static HttpClientContext create() {
        return new HttpClientContext(new BasicHttpContext());
    }

    public HttpClientContext(HttpContext context) {
        super(context);
    }

    public RouteInfo getHttpRoute() {
        return (RouteInfo) getAttribute(HTTP_ROUTE, HttpRoute.class);
    }

    public List<URI> getRedirectLocations() {
        return (List) getAttribute(REDIRECT_LOCATIONS, List.class);
    }

    public CookieStore getCookieStore() {
        return (CookieStore) getAttribute(COOKIE_STORE, CookieStore.class);
    }

    public void setCookieStore(CookieStore cookieStore) {
        setAttribute(COOKIE_STORE, cookieStore);
    }

    public CookieSpec getCookieSpec() {
        return (CookieSpec) getAttribute(COOKIE_SPEC, CookieSpec.class);
    }

    public CookieOrigin getCookieOrigin() {
        return (CookieOrigin) getAttribute(COOKIE_ORIGIN, CookieOrigin.class);
    }

    private <T> Lookup<T> getLookup(String name, Class<T> cls) {
        return (Lookup) getAttribute(name, Lookup.class);
    }

    public Lookup<CookieSpecProvider> getCookieSpecRegistry() {
        return getLookup(COOKIESPEC_REGISTRY, CookieSpecProvider.class);
    }

    public void setCookieSpecRegistry(Lookup<CookieSpecProvider> lookup) {
        setAttribute(COOKIESPEC_REGISTRY, lookup);
    }

    public Lookup<AuthSchemeProvider> getAuthSchemeRegistry() {
        return getLookup(AUTHSCHEME_REGISTRY, AuthSchemeProvider.class);
    }

    public void setAuthSchemeRegistry(Lookup<AuthSchemeProvider> lookup) {
        setAttribute(AUTHSCHEME_REGISTRY, lookup);
    }

    public CredentialsProvider getCredentialsProvider() {
        return (CredentialsProvider) getAttribute(CREDS_PROVIDER, CredentialsProvider.class);
    }

    public void setCredentialsProvider(CredentialsProvider credentialsProvider) {
        setAttribute(CREDS_PROVIDER, credentialsProvider);
    }

    public AuthCache getAuthCache() {
        return (AuthCache) getAttribute(AUTH_CACHE, AuthCache.class);
    }

    public void setAuthCache(AuthCache authCache) {
        setAttribute(AUTH_CACHE, authCache);
    }

    public AuthState getTargetAuthState() {
        return (AuthState) getAttribute(TARGET_AUTH_STATE, AuthState.class);
    }

    public AuthState getProxyAuthState() {
        return (AuthState) getAttribute(PROXY_AUTH_STATE, AuthState.class);
    }

    public <T> T getUserToken(Class<T> clazz) {
        return getAttribute(USER_TOKEN, clazz);
    }

    public Object getUserToken() {
        return getAttribute(USER_TOKEN);
    }

    public void setUserToken(Object obj) {
        setAttribute(USER_TOKEN, obj);
    }

    public RequestConfig getRequestConfig() {
        RequestConfig config = (RequestConfig) getAttribute(REQUEST_CONFIG, RequestConfig.class);
        return config != null ? config : RequestConfig.DEFAULT;
    }

    public void setRequestConfig(RequestConfig config) {
        setAttribute(REQUEST_CONFIG, config);
    }
}
