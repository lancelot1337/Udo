package cz.msebera.android.httpclient.client.protocol;

import cz.msebera.android.httpclient.HttpException;
import cz.msebera.android.httpclient.HttpHost;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpResponseInterceptor;
import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.auth.AuthProtocolState;
import cz.msebera.android.httpclient.auth.AuthScheme;
import cz.msebera.android.httpclient.auth.AuthState;
import cz.msebera.android.httpclient.client.AuthCache;
import cz.msebera.android.httpclient.client.params.AuthPolicy;
import cz.msebera.android.httpclient.conn.scheme.SchemeRegistry;
import cz.msebera.android.httpclient.extras.HttpClientAndroidLog;
import cz.msebera.android.httpclient.impl.client.BasicAuthCache;
import cz.msebera.android.httpclient.protocol.ExecutionContext;
import cz.msebera.android.httpclient.protocol.HttpContext;
import cz.msebera.android.httpclient.util.Args;
import io.branch.referral.R;
import java.io.IOException;
import org.cocos2dx.lib.Cocos2dxHandler;

@Immutable
@Deprecated
public class ResponseAuthCache implements HttpResponseInterceptor {
    public HttpClientAndroidLog log = new HttpClientAndroidLog(getClass());

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$cz$msebera$android$httpclient$auth$AuthProtocolState = new int[AuthProtocolState.values().length];

        static {
            try {
                $SwitchMap$cz$msebera$android$httpclient$auth$AuthProtocolState[AuthProtocolState.CHALLENGED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$cz$msebera$android$httpclient$auth$AuthProtocolState[AuthProtocolState.FAILURE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
        Args.notNull(response, "HTTP request");
        Args.notNull(context, "HTTP context");
        AuthCache authCache = (AuthCache) context.getAttribute(ClientContext.AUTH_CACHE);
        HttpHost target = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
        AuthState targetState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
        if (!(target == null || targetState == null)) {
            if (this.log.isDebugEnabled()) {
                this.log.debug("Target auth state: " + targetState.getState());
            }
            if (isCachable(targetState)) {
                SchemeRegistry schemeRegistry = (SchemeRegistry) context.getAttribute(ClientContext.SCHEME_REGISTRY);
                if (target.getPort() < 0) {
                    target = new HttpHost(target.getHostName(), schemeRegistry.getScheme(target).resolvePort(target.getPort()), target.getSchemeName());
                }
                if (authCache == null) {
                    authCache = new BasicAuthCache();
                    context.setAttribute(ClientContext.AUTH_CACHE, authCache);
                }
                switch (AnonymousClass1.$SwitchMap$cz$msebera$android$httpclient$auth$AuthProtocolState[targetState.getState().ordinal()]) {
                    case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                        cache(authCache, target, targetState.getAuthScheme());
                        break;
                    case R.styleable.View_paddingStart /*2*/:
                        uncache(authCache, target, targetState.getAuthScheme());
                        break;
                }
            }
        }
        HttpHost proxy = (HttpHost) context.getAttribute(ExecutionContext.HTTP_PROXY_HOST);
        AuthState proxyState = (AuthState) context.getAttribute(ClientContext.PROXY_AUTH_STATE);
        if (proxy != null && proxyState != null) {
            if (this.log.isDebugEnabled()) {
                this.log.debug("Proxy auth state: " + proxyState.getState());
            }
            if (isCachable(proxyState)) {
                if (authCache == null) {
                    authCache = new BasicAuthCache();
                    context.setAttribute(ClientContext.AUTH_CACHE, authCache);
                }
                switch (AnonymousClass1.$SwitchMap$cz$msebera$android$httpclient$auth$AuthProtocolState[proxyState.getState().ordinal()]) {
                    case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                        cache(authCache, proxy, proxyState.getAuthScheme());
                        return;
                    case R.styleable.View_paddingStart /*2*/:
                        uncache(authCache, proxy, proxyState.getAuthScheme());
                        return;
                    default:
                        return;
                }
            }
        }
    }

    private boolean isCachable(AuthState authState) {
        AuthScheme authScheme = authState.getAuthScheme();
        if (authScheme == null || !authScheme.isComplete()) {
            return false;
        }
        String schemeName = authScheme.getSchemeName();
        if (schemeName.equalsIgnoreCase(AuthPolicy.BASIC) || schemeName.equalsIgnoreCase(AuthPolicy.DIGEST)) {
            return true;
        }
        return false;
    }

    private void cache(AuthCache authCache, HttpHost host, AuthScheme authScheme) {
        if (this.log.isDebugEnabled()) {
            this.log.debug("Caching '" + authScheme.getSchemeName() + "' auth scheme for " + host);
        }
        authCache.put(host, authScheme);
    }

    private void uncache(AuthCache authCache, HttpHost host, AuthScheme authScheme) {
        if (this.log.isDebugEnabled()) {
            this.log.debug("Removing from cache '" + authScheme.getSchemeName() + "' auth scheme for " + host);
        }
        authCache.remove(host);
    }
}
