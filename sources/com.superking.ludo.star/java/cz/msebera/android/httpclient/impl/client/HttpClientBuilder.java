package cz.msebera.android.httpclient.impl.client;

import com.facebook.internal.ServerProtocol;
import cz.msebera.android.httpclient.ConnectionReuseStrategy;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpHost;
import cz.msebera.android.httpclient.HttpRequestInterceptor;
import cz.msebera.android.httpclient.HttpResponseInterceptor;
import cz.msebera.android.httpclient.annotation.NotThreadSafe;
import cz.msebera.android.httpclient.auth.AuthSchemeProvider;
import cz.msebera.android.httpclient.client.AuthenticationStrategy;
import cz.msebera.android.httpclient.client.BackoffManager;
import cz.msebera.android.httpclient.client.ConnectionBackoffStrategy;
import cz.msebera.android.httpclient.client.CookieStore;
import cz.msebera.android.httpclient.client.CredentialsProvider;
import cz.msebera.android.httpclient.client.HttpRequestRetryHandler;
import cz.msebera.android.httpclient.client.RedirectStrategy;
import cz.msebera.android.httpclient.client.ServiceUnavailableRetryStrategy;
import cz.msebera.android.httpclient.client.UserTokenHandler;
import cz.msebera.android.httpclient.client.config.CookieSpecs;
import cz.msebera.android.httpclient.client.config.RequestConfig;
import cz.msebera.android.httpclient.client.entity.InputStreamFactory;
import cz.msebera.android.httpclient.client.params.AuthPolicy;
import cz.msebera.android.httpclient.client.params.CookiePolicy;
import cz.msebera.android.httpclient.client.protocol.RequestAcceptEncoding;
import cz.msebera.android.httpclient.client.protocol.RequestAddCookies;
import cz.msebera.android.httpclient.client.protocol.RequestAuthCache;
import cz.msebera.android.httpclient.client.protocol.RequestClientConnControl;
import cz.msebera.android.httpclient.client.protocol.RequestDefaultHeaders;
import cz.msebera.android.httpclient.client.protocol.RequestExpectContinue;
import cz.msebera.android.httpclient.client.protocol.ResponseContentEncoding;
import cz.msebera.android.httpclient.client.protocol.ResponseProcessCookies;
import cz.msebera.android.httpclient.config.ConnectionConfig;
import cz.msebera.android.httpclient.config.Lookup;
import cz.msebera.android.httpclient.config.RegistryBuilder;
import cz.msebera.android.httpclient.config.SocketConfig;
import cz.msebera.android.httpclient.conn.ConnectionKeepAliveStrategy;
import cz.msebera.android.httpclient.conn.HttpClientConnectionManager;
import cz.msebera.android.httpclient.conn.SchemePortResolver;
import cz.msebera.android.httpclient.conn.routing.HttpRoutePlanner;
import cz.msebera.android.httpclient.conn.socket.LayeredConnectionSocketFactory;
import cz.msebera.android.httpclient.conn.socket.PlainConnectionSocketFactory;
import cz.msebera.android.httpclient.conn.ssl.DefaultHostnameVerifier;
import cz.msebera.android.httpclient.conn.ssl.SSLConnectionSocketFactory;
import cz.msebera.android.httpclient.conn.ssl.X509HostnameVerifier;
import cz.msebera.android.httpclient.conn.util.PublicSuffixMatcher;
import cz.msebera.android.httpclient.conn.util.PublicSuffixMatcherLoader;
import cz.msebera.android.httpclient.cookie.CookieSpecProvider;
import cz.msebera.android.httpclient.impl.DefaultConnectionReuseStrategy;
import cz.msebera.android.httpclient.impl.NoConnectionReuseStrategy;
import cz.msebera.android.httpclient.impl.auth.BasicSchemeFactory;
import cz.msebera.android.httpclient.impl.auth.DigestSchemeFactory;
import cz.msebera.android.httpclient.impl.auth.NTLMSchemeFactory;
import cz.msebera.android.httpclient.impl.conn.DefaultProxyRoutePlanner;
import cz.msebera.android.httpclient.impl.conn.DefaultRoutePlanner;
import cz.msebera.android.httpclient.impl.conn.DefaultSchemePortResolver;
import cz.msebera.android.httpclient.impl.conn.PoolingHttpClientConnectionManager;
import cz.msebera.android.httpclient.impl.conn.SystemDefaultRoutePlanner;
import cz.msebera.android.httpclient.impl.cookie.DefaultCookieSpecProvider;
import cz.msebera.android.httpclient.impl.cookie.IgnoreSpecProvider;
import cz.msebera.android.httpclient.impl.cookie.NetscapeDraftSpecProvider;
import cz.msebera.android.httpclient.impl.cookie.RFC6265CookieSpecProvider;
import cz.msebera.android.httpclient.impl.cookie.RFC6265CookieSpecProvider.CompatibilityLevel;
import cz.msebera.android.httpclient.impl.execchain.BackoffStrategyExec;
import cz.msebera.android.httpclient.impl.execchain.ClientExecChain;
import cz.msebera.android.httpclient.impl.execchain.MainClientExec;
import cz.msebera.android.httpclient.impl.execchain.ProtocolExec;
import cz.msebera.android.httpclient.impl.execchain.RedirectExec;
import cz.msebera.android.httpclient.impl.execchain.RetryExec;
import cz.msebera.android.httpclient.impl.execchain.ServiceUnavailableRetryExec;
import cz.msebera.android.httpclient.protocol.HttpProcessor;
import cz.msebera.android.httpclient.protocol.HttpProcessorBuilder;
import cz.msebera.android.httpclient.protocol.HttpRequestExecutor;
import cz.msebera.android.httpclient.protocol.ImmutableHttpProcessor;
import cz.msebera.android.httpclient.protocol.RequestContent;
import cz.msebera.android.httpclient.protocol.RequestTargetHost;
import cz.msebera.android.httpclient.protocol.RequestUserAgent;
import cz.msebera.android.httpclient.ssl.SSLContexts;
import cz.msebera.android.httpclient.util.TextUtils;
import cz.msebera.android.httpclient.util.VersionInfo;
import io.branch.referral.Branch;
import java.io.Closeable;
import java.io.IOException;
import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

@NotThreadSafe
public class HttpClientBuilder {
    private boolean authCachingDisabled;
    private Lookup<AuthSchemeProvider> authSchemeRegistry;
    private boolean automaticRetriesDisabled;
    private BackoffManager backoffManager;
    private List<Closeable> closeables;
    private HttpClientConnectionManager connManager;
    private boolean connManagerShared;
    private long connTimeToLive = -1;
    private TimeUnit connTimeToLiveTimeUnit = TimeUnit.MILLISECONDS;
    private ConnectionBackoffStrategy connectionBackoffStrategy;
    private boolean connectionStateDisabled;
    private boolean contentCompressionDisabled;
    private Map<String, InputStreamFactory> contentDecoderMap;
    private boolean cookieManagementDisabled;
    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private CookieStore cookieStore;
    private CredentialsProvider credentialsProvider;
    private ConnectionConfig defaultConnectionConfig;
    private Collection<? extends Header> defaultHeaders;
    private RequestConfig defaultRequestConfig;
    private SocketConfig defaultSocketConfig;
    private boolean evictExpiredConnections;
    private boolean evictIdleConnections;
    private HostnameVerifier hostnameVerifier;
    private HttpProcessor httpprocessor;
    private ConnectionKeepAliveStrategy keepAliveStrategy;
    private int maxConnPerRoute = 0;
    private int maxConnTotal = 0;
    private long maxIdleTime;
    private TimeUnit maxIdleTimeUnit;
    private HttpHost proxy;
    private AuthenticationStrategy proxyAuthStrategy;
    private PublicSuffixMatcher publicSuffixMatcher;
    private boolean redirectHandlingDisabled;
    private RedirectStrategy redirectStrategy;
    private HttpRequestExecutor requestExec;
    private LinkedList<HttpRequestInterceptor> requestFirst;
    private LinkedList<HttpRequestInterceptor> requestLast;
    private LinkedList<HttpResponseInterceptor> responseFirst;
    private LinkedList<HttpResponseInterceptor> responseLast;
    private HttpRequestRetryHandler retryHandler;
    private ConnectionReuseStrategy reuseStrategy;
    private HttpRoutePlanner routePlanner;
    private SchemePortResolver schemePortResolver;
    private ServiceUnavailableRetryStrategy serviceUnavailStrategy;
    private LayeredConnectionSocketFactory sslSocketFactory;
    private SSLContext sslcontext;
    private boolean systemProperties;
    private AuthenticationStrategy targetAuthStrategy;
    private String userAgent;
    private UserTokenHandler userTokenHandler;

    public static HttpClientBuilder create() {
        return new HttpClientBuilder();
    }

    protected HttpClientBuilder() {
    }

    public final HttpClientBuilder setRequestExecutor(HttpRequestExecutor requestExec) {
        this.requestExec = requestExec;
        return this;
    }

    @Deprecated
    public final HttpClientBuilder setHostnameVerifier(X509HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
        return this;
    }

    public final HttpClientBuilder setSSLHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
        return this;
    }

    public final HttpClientBuilder setPublicSuffixMatcher(PublicSuffixMatcher publicSuffixMatcher) {
        this.publicSuffixMatcher = publicSuffixMatcher;
        return this;
    }

    public final HttpClientBuilder setSslcontext(SSLContext sslcontext) {
        this.sslcontext = sslcontext;
        return this;
    }

    public final HttpClientBuilder setSSLSocketFactory(LayeredConnectionSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
        return this;
    }

    public final HttpClientBuilder setMaxConnTotal(int maxConnTotal) {
        this.maxConnTotal = maxConnTotal;
        return this;
    }

    public final HttpClientBuilder setMaxConnPerRoute(int maxConnPerRoute) {
        this.maxConnPerRoute = maxConnPerRoute;
        return this;
    }

    public final HttpClientBuilder setDefaultSocketConfig(SocketConfig config) {
        this.defaultSocketConfig = config;
        return this;
    }

    public final HttpClientBuilder setDefaultConnectionConfig(ConnectionConfig config) {
        this.defaultConnectionConfig = config;
        return this;
    }

    public final HttpClientBuilder setConnectionTimeToLive(long connTimeToLive, TimeUnit connTimeToLiveTimeUnit) {
        this.connTimeToLive = connTimeToLive;
        this.connTimeToLiveTimeUnit = connTimeToLiveTimeUnit;
        return this;
    }

    public final HttpClientBuilder setConnectionManager(HttpClientConnectionManager connManager) {
        this.connManager = connManager;
        return this;
    }

    public final HttpClientBuilder setConnectionManagerShared(boolean shared) {
        this.connManagerShared = shared;
        return this;
    }

    public final HttpClientBuilder setConnectionReuseStrategy(ConnectionReuseStrategy reuseStrategy) {
        this.reuseStrategy = reuseStrategy;
        return this;
    }

    public final HttpClientBuilder setKeepAliveStrategy(ConnectionKeepAliveStrategy keepAliveStrategy) {
        this.keepAliveStrategy = keepAliveStrategy;
        return this;
    }

    public final HttpClientBuilder setTargetAuthenticationStrategy(AuthenticationStrategy targetAuthStrategy) {
        this.targetAuthStrategy = targetAuthStrategy;
        return this;
    }

    public final HttpClientBuilder setProxyAuthenticationStrategy(AuthenticationStrategy proxyAuthStrategy) {
        this.proxyAuthStrategy = proxyAuthStrategy;
        return this;
    }

    public final HttpClientBuilder setUserTokenHandler(UserTokenHandler userTokenHandler) {
        this.userTokenHandler = userTokenHandler;
        return this;
    }

    public final HttpClientBuilder disableConnectionState() {
        this.connectionStateDisabled = true;
        return this;
    }

    public final HttpClientBuilder setSchemePortResolver(SchemePortResolver schemePortResolver) {
        this.schemePortResolver = schemePortResolver;
        return this;
    }

    public final HttpClientBuilder setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public final HttpClientBuilder setDefaultHeaders(Collection<? extends Header> defaultHeaders) {
        this.defaultHeaders = defaultHeaders;
        return this;
    }

    public final HttpClientBuilder addInterceptorFirst(HttpResponseInterceptor itcp) {
        if (itcp != null) {
            if (this.responseFirst == null) {
                this.responseFirst = new LinkedList();
            }
            this.responseFirst.addFirst(itcp);
        }
        return this;
    }

    public final HttpClientBuilder addInterceptorLast(HttpResponseInterceptor itcp) {
        if (itcp != null) {
            if (this.responseLast == null) {
                this.responseLast = new LinkedList();
            }
            this.responseLast.addLast(itcp);
        }
        return this;
    }

    public final HttpClientBuilder addInterceptorFirst(HttpRequestInterceptor itcp) {
        if (itcp != null) {
            if (this.requestFirst == null) {
                this.requestFirst = new LinkedList();
            }
            this.requestFirst.addFirst(itcp);
        }
        return this;
    }

    public final HttpClientBuilder addInterceptorLast(HttpRequestInterceptor itcp) {
        if (itcp != null) {
            if (this.requestLast == null) {
                this.requestLast = new LinkedList();
            }
            this.requestLast.addLast(itcp);
        }
        return this;
    }

    public final HttpClientBuilder disableCookieManagement() {
        this.cookieManagementDisabled = true;
        return this;
    }

    public final HttpClientBuilder disableContentCompression() {
        this.contentCompressionDisabled = true;
        return this;
    }

    public final HttpClientBuilder disableAuthCaching() {
        this.authCachingDisabled = true;
        return this;
    }

    public final HttpClientBuilder setHttpProcessor(HttpProcessor httpprocessor) {
        this.httpprocessor = httpprocessor;
        return this;
    }

    public final HttpClientBuilder setRetryHandler(HttpRequestRetryHandler retryHandler) {
        this.retryHandler = retryHandler;
        return this;
    }

    public final HttpClientBuilder disableAutomaticRetries() {
        this.automaticRetriesDisabled = true;
        return this;
    }

    public final HttpClientBuilder setProxy(HttpHost proxy) {
        this.proxy = proxy;
        return this;
    }

    public final HttpClientBuilder setRoutePlanner(HttpRoutePlanner routePlanner) {
        this.routePlanner = routePlanner;
        return this;
    }

    public final HttpClientBuilder setRedirectStrategy(RedirectStrategy redirectStrategy) {
        this.redirectStrategy = redirectStrategy;
        return this;
    }

    public final HttpClientBuilder disableRedirectHandling() {
        this.redirectHandlingDisabled = true;
        return this;
    }

    public final HttpClientBuilder setConnectionBackoffStrategy(ConnectionBackoffStrategy connectionBackoffStrategy) {
        this.connectionBackoffStrategy = connectionBackoffStrategy;
        return this;
    }

    public final HttpClientBuilder setBackoffManager(BackoffManager backoffManager) {
        this.backoffManager = backoffManager;
        return this;
    }

    public final HttpClientBuilder setServiceUnavailableRetryStrategy(ServiceUnavailableRetryStrategy serviceUnavailStrategy) {
        this.serviceUnavailStrategy = serviceUnavailStrategy;
        return this;
    }

    public final HttpClientBuilder setDefaultCookieStore(CookieStore cookieStore) {
        this.cookieStore = cookieStore;
        return this;
    }

    public final HttpClientBuilder setDefaultCredentialsProvider(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return this;
    }

    public final HttpClientBuilder setDefaultAuthSchemeRegistry(Lookup<AuthSchemeProvider> authSchemeRegistry) {
        this.authSchemeRegistry = authSchemeRegistry;
        return this;
    }

    public final HttpClientBuilder setDefaultCookieSpecRegistry(Lookup<CookieSpecProvider> cookieSpecRegistry) {
        this.cookieSpecRegistry = cookieSpecRegistry;
        return this;
    }

    public final HttpClientBuilder setContentDecoderRegistry(Map<String, InputStreamFactory> contentDecoderMap) {
        this.contentDecoderMap = contentDecoderMap;
        return this;
    }

    public final HttpClientBuilder setDefaultRequestConfig(RequestConfig config) {
        this.defaultRequestConfig = config;
        return this;
    }

    public final HttpClientBuilder useSystemProperties() {
        this.systemProperties = true;
        return this;
    }

    public final HttpClientBuilder evictExpiredConnections() {
        this.evictExpiredConnections = true;
        return this;
    }

    public final HttpClientBuilder evictIdleConnections(Long maxIdleTime, TimeUnit maxIdleTimeUnit) {
        this.evictIdleConnections = true;
        this.maxIdleTime = maxIdleTime.longValue();
        this.maxIdleTimeUnit = maxIdleTimeUnit;
        return this;
    }

    protected ClientExecChain createMainExec(HttpRequestExecutor requestExec, HttpClientConnectionManager connManager, ConnectionReuseStrategy reuseStrategy, ConnectionKeepAliveStrategy keepAliveStrategy, HttpProcessor proxyHttpProcessor, AuthenticationStrategy targetAuthStrategy, AuthenticationStrategy proxyAuthStrategy, UserTokenHandler userTokenHandler) {
        return new MainClientExec(requestExec, connManager, reuseStrategy, keepAliveStrategy, proxyHttpProcessor, targetAuthStrategy, proxyAuthStrategy, userTokenHandler);
    }

    protected ClientExecChain decorateMainExec(ClientExecChain mainExec) {
        return mainExec;
    }

    protected ClientExecChain decorateProtocolExec(ClientExecChain protocolExec) {
        return protocolExec;
    }

    protected void addCloseable(Closeable closeable) {
        if (closeable != null) {
            if (this.closeables == null) {
                this.closeables = new ArrayList();
            }
            this.closeables.add(closeable);
        }
    }

    private static String[] split(String s) {
        if (TextUtils.isBlank(s)) {
            return null;
        }
        return s.split(" *, *");
    }

    public CloseableHttpClient build() {
        List<Closeable> closeablesCopy;
        RequestConfig requestConfig;
        PublicSuffixMatcher publicSuffixMatcherCopy = this.publicSuffixMatcher;
        if (publicSuffixMatcherCopy == null) {
            publicSuffixMatcherCopy = PublicSuffixMatcherLoader.getDefault();
        }
        HttpRequestExecutor requestExecCopy = this.requestExec;
        if (requestExecCopy == null) {
            requestExecCopy = new HttpRequestExecutor();
        }
        HttpClientConnectionManager connManagerCopy = this.connManager;
        if (connManagerCopy == null) {
            LayeredConnectionSocketFactory sslSocketFactoryCopy = this.sslSocketFactory;
            if (sslSocketFactoryCopy == null) {
                String[] supportedProtocols = this.systemProperties ? split(System.getProperty("https.protocols")) : null;
                String[] supportedCipherSuites = this.systemProperties ? split(System.getProperty("https.cipherSuites")) : null;
                HostnameVerifier hostnameVerifierCopy = this.hostnameVerifier;
                if (hostnameVerifierCopy == null) {
                    HostnameVerifier defaultHostnameVerifier = new DefaultHostnameVerifier(publicSuffixMatcherCopy);
                }
                LayeredConnectionSocketFactory sSLConnectionSocketFactory;
                if (this.sslcontext != null) {
                    sSLConnectionSocketFactory = new SSLConnectionSocketFactory(this.sslcontext, supportedProtocols, supportedCipherSuites, hostnameVerifierCopy);
                } else if (this.systemProperties) {
                    sSLConnectionSocketFactory = new SSLConnectionSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault(), supportedProtocols, supportedCipherSuites, hostnameVerifierCopy);
                } else {
                    sSLConnectionSocketFactory = new SSLConnectionSocketFactory(SSLContexts.createDefault(), hostnameVerifierCopy);
                }
            }
            PoolingHttpClientConnectionManager poolingmgr = new PoolingHttpClientConnectionManager(RegistryBuilder.create().register(HttpHost.DEFAULT_SCHEME_NAME, PlainConnectionSocketFactory.getSocketFactory()).register("https", sslSocketFactoryCopy).build(), null, null, null, this.connTimeToLive, this.connTimeToLiveTimeUnit != null ? this.connTimeToLiveTimeUnit : TimeUnit.MILLISECONDS);
            if (this.defaultSocketConfig != null) {
                poolingmgr.setDefaultSocketConfig(this.defaultSocketConfig);
            }
            if (this.defaultConnectionConfig != null) {
                poolingmgr.setDefaultConnectionConfig(this.defaultConnectionConfig);
            }
            if (this.systemProperties) {
                if (ServerProtocol.DIALOG_RETURN_SCOPES_TRUE.equalsIgnoreCase(System.getProperty("http.keepAlive", ServerProtocol.DIALOG_RETURN_SCOPES_TRUE))) {
                    int max = Integer.parseInt(System.getProperty("http.maxConnections", "5"));
                    poolingmgr.setDefaultMaxPerRoute(max);
                    poolingmgr.setMaxTotal(max * 2);
                }
            }
            if (this.maxConnTotal > 0) {
                poolingmgr.setMaxTotal(this.maxConnTotal);
            }
            if (this.maxConnPerRoute > 0) {
                poolingmgr.setDefaultMaxPerRoute(this.maxConnPerRoute);
            }
            connManagerCopy = poolingmgr;
        }
        ConnectionReuseStrategy reuseStrategyCopy = this.reuseStrategy;
        if (reuseStrategyCopy == null) {
            if (this.systemProperties) {
                if (ServerProtocol.DIALOG_RETURN_SCOPES_TRUE.equalsIgnoreCase(System.getProperty("http.keepAlive", ServerProtocol.DIALOG_RETURN_SCOPES_TRUE))) {
                    reuseStrategyCopy = DefaultConnectionReuseStrategy.INSTANCE;
                } else {
                    reuseStrategyCopy = NoConnectionReuseStrategy.INSTANCE;
                }
            } else {
                reuseStrategyCopy = DefaultConnectionReuseStrategy.INSTANCE;
            }
        }
        ConnectionKeepAliveStrategy keepAliveStrategyCopy = this.keepAliveStrategy;
        if (keepAliveStrategyCopy == null) {
            keepAliveStrategyCopy = DefaultConnectionKeepAliveStrategy.INSTANCE;
        }
        AuthenticationStrategy targetAuthStrategyCopy = this.targetAuthStrategy;
        if (targetAuthStrategyCopy == null) {
            targetAuthStrategyCopy = TargetAuthenticationStrategy.INSTANCE;
        }
        AuthenticationStrategy proxyAuthStrategyCopy = this.proxyAuthStrategy;
        if (proxyAuthStrategyCopy == null) {
            proxyAuthStrategyCopy = ProxyAuthenticationStrategy.INSTANCE;
        }
        UserTokenHandler userTokenHandlerCopy = this.userTokenHandler;
        if (userTokenHandlerCopy == null) {
            if (this.connectionStateDisabled) {
                userTokenHandlerCopy = NoopUserTokenHandler.INSTANCE;
            } else {
                userTokenHandlerCopy = DefaultUserTokenHandler.INSTANCE;
            }
        }
        String userAgentCopy = this.userAgent;
        if (userAgentCopy == null) {
            if (this.systemProperties) {
                userAgentCopy = System.getProperty("http.agent");
            }
            if (userAgentCopy == null) {
                userAgentCopy = VersionInfo.getUserAgent("Apache-HttpClient", "cz.msebera.android.httpclient.client", getClass());
            }
        }
        ClientExecChain execChain = decorateMainExec(createMainExec(requestExecCopy, connManagerCopy, reuseStrategyCopy, keepAliveStrategyCopy, new ImmutableHttpProcessor(new RequestTargetHost(), new RequestUserAgent(userAgentCopy)), targetAuthStrategyCopy, proxyAuthStrategyCopy, userTokenHandlerCopy));
        HttpProcessor httpprocessorCopy = this.httpprocessor;
        if (httpprocessorCopy == null) {
            Iterator it;
            HttpProcessorBuilder b = HttpProcessorBuilder.create();
            if (this.requestFirst != null) {
                it = this.requestFirst.iterator();
                while (it.hasNext()) {
                    b.addFirst((HttpRequestInterceptor) it.next());
                }
            }
            if (this.responseFirst != null) {
                it = this.responseFirst.iterator();
                while (it.hasNext()) {
                    b.addFirst((HttpResponseInterceptor) it.next());
                }
            }
            b.addAll(new RequestDefaultHeaders(this.defaultHeaders), new RequestContent(), new RequestTargetHost(), new RequestClientConnControl(), new RequestUserAgent(userAgentCopy), new RequestExpectContinue());
            if (!this.cookieManagementDisabled) {
                b.add(new RequestAddCookies());
            }
            if (!this.contentCompressionDisabled) {
                if (this.contentDecoderMap != null) {
                    List<String> arrayList = new ArrayList(this.contentDecoderMap.keySet());
                    Collections.sort(arrayList);
                    b.add(new RequestAcceptEncoding(arrayList));
                } else {
                    b.add(new RequestAcceptEncoding());
                }
            }
            if (!this.authCachingDisabled) {
                b.add(new RequestAuthCache());
            }
            if (!this.cookieManagementDisabled) {
                b.add(new ResponseProcessCookies());
            }
            if (!this.contentCompressionDisabled) {
                if (this.contentDecoderMap != null) {
                    RegistryBuilder<InputStreamFactory> b2 = RegistryBuilder.create();
                    for (Entry<String, InputStreamFactory> entry : this.contentDecoderMap.entrySet()) {
                        b2.register((String) entry.getKey(), entry.getValue());
                    }
                    b.add(new ResponseContentEncoding(b2.build()));
                } else {
                    b.add(new ResponseContentEncoding());
                }
            }
            if (this.requestLast != null) {
                it = this.requestLast.iterator();
                while (it.hasNext()) {
                    b.addLast((HttpRequestInterceptor) it.next());
                }
            }
            if (this.responseLast != null) {
                it = this.responseLast.iterator();
                while (it.hasNext()) {
                    b.addLast((HttpResponseInterceptor) it.next());
                }
            }
            httpprocessorCopy = b.build();
        }
        execChain = decorateProtocolExec(new ProtocolExec(execChain, httpprocessorCopy));
        if (!this.automaticRetriesDisabled) {
            HttpRequestRetryHandler retryHandlerCopy = this.retryHandler;
            if (retryHandlerCopy == null) {
                retryHandlerCopy = DefaultHttpRequestRetryHandler.INSTANCE;
            }
            execChain = new RetryExec(execChain, retryHandlerCopy);
        }
        HttpRoutePlanner routePlannerCopy = this.routePlanner;
        if (routePlannerCopy == null) {
            SchemePortResolver schemePortResolverCopy = this.schemePortResolver;
            if (schemePortResolverCopy == null) {
                schemePortResolverCopy = DefaultSchemePortResolver.INSTANCE;
            }
            HttpRoutePlanner defaultProxyRoutePlanner;
            if (this.proxy != null) {
                defaultProxyRoutePlanner = new DefaultProxyRoutePlanner(this.proxy, schemePortResolverCopy);
            } else if (this.systemProperties) {
                defaultProxyRoutePlanner = new SystemDefaultRoutePlanner(schemePortResolverCopy, ProxySelector.getDefault());
            } else {
                defaultProxyRoutePlanner = new DefaultRoutePlanner(schemePortResolverCopy);
            }
        }
        if (!this.redirectHandlingDisabled) {
            RedirectStrategy redirectStrategyCopy = this.redirectStrategy;
            if (redirectStrategyCopy == null) {
                redirectStrategyCopy = DefaultRedirectStrategy.INSTANCE;
            }
            execChain = new RedirectExec(execChain, routePlannerCopy, redirectStrategyCopy);
        }
        ServiceUnavailableRetryStrategy serviceUnavailStrategyCopy = this.serviceUnavailStrategy;
        if (serviceUnavailStrategyCopy != null) {
            execChain = new ServiceUnavailableRetryExec(execChain, serviceUnavailStrategyCopy);
        }
        if (!(this.backoffManager == null || this.connectionBackoffStrategy == null)) {
            execChain = new BackoffStrategyExec(execChain, this.connectionBackoffStrategy, this.backoffManager);
        }
        Lookup<AuthSchemeProvider> authSchemeRegistryCopy = this.authSchemeRegistry;
        if (authSchemeRegistryCopy == null) {
            authSchemeRegistryCopy = RegistryBuilder.create().register(AuthPolicy.BASIC, new BasicSchemeFactory()).register(AuthPolicy.DIGEST, new DigestSchemeFactory()).register(AuthPolicy.NTLM, new NTLMSchemeFactory()).build();
        }
        Lookup<CookieSpecProvider> cookieSpecRegistryCopy = this.cookieSpecRegistry;
        if (cookieSpecRegistryCopy == null) {
            CookieSpecProvider defaultCookieSpecProvider = new DefaultCookieSpecProvider(publicSuffixMatcherCopy);
            cookieSpecRegistryCopy = RegistryBuilder.create().register(Branch.REFERRAL_BUCKET_DEFAULT, defaultCookieSpecProvider).register(CookiePolicy.BEST_MATCH, defaultCookieSpecProvider).register(CookiePolicy.BROWSER_COMPATIBILITY, defaultCookieSpecProvider).register(CookieSpecs.STANDARD, new RFC6265CookieSpecProvider(CompatibilityLevel.RELAXED, publicSuffixMatcherCopy)).register(CookieSpecs.STANDARD_STRICT, new RFC6265CookieSpecProvider(CompatibilityLevel.STRICT, publicSuffixMatcherCopy)).register(CookiePolicy.NETSCAPE, new NetscapeDraftSpecProvider()).register(CookiePolicy.IGNORE_COOKIES, new IgnoreSpecProvider()).build();
        }
        CookieStore defaultCookieStore = this.cookieStore;
        if (defaultCookieStore == null) {
            defaultCookieStore = new BasicCookieStore();
        }
        CredentialsProvider defaultCredentialsProvider = this.credentialsProvider;
        if (defaultCredentialsProvider == null) {
            if (this.systemProperties) {
                defaultCredentialsProvider = new SystemDefaultCredentialsProvider();
            } else {
                defaultCredentialsProvider = new BasicCredentialsProvider();
            }
        }
        if (this.closeables != null) {
            List<Closeable> arrayList2 = new ArrayList(this.closeables);
        } else {
            closeablesCopy = null;
        }
        if (!this.connManagerShared) {
            if (closeablesCopy == null) {
                arrayList2 = new ArrayList(1);
            }
            HttpClientConnectionManager cm = connManagerCopy;
            if (this.evictExpiredConnections || this.evictIdleConnections) {
                TimeUnit timeUnit;
                long j = this.maxIdleTime > 0 ? this.maxIdleTime : 10;
                if (this.maxIdleTimeUnit != null) {
                    timeUnit = this.maxIdleTimeUnit;
                } else {
                    timeUnit = TimeUnit.SECONDS;
                }
                IdleConnectionEvictor idleConnectionEvictor = new IdleConnectionEvictor(cm, j, timeUnit);
                final IdleConnectionEvictor idleConnectionEvictor2 = idleConnectionEvictor;
                closeablesCopy.add(new Closeable() {
                    public void close() throws IOException {
                        idleConnectionEvictor2.shutdown();
                    }
                });
                idleConnectionEvictor.start();
            }
            final HttpClientConnectionManager httpClientConnectionManager = cm;
            closeablesCopy.add(new Closeable() {
                public void close() throws IOException {
                    httpClientConnectionManager.shutdown();
                }
            });
        }
        if (this.defaultRequestConfig != null) {
            requestConfig = this.defaultRequestConfig;
        } else {
            requestConfig = RequestConfig.DEFAULT;
        }
        return new InternalHttpClient(execChain, connManagerCopy, routePlannerCopy, cookieSpecRegistryCopy, authSchemeRegistryCopy, defaultCookieStore, defaultCredentialsProvider, requestConfig, closeablesCopy);
    }
}
