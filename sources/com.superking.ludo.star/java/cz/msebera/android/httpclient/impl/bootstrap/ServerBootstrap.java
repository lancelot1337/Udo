package cz.msebera.android.httpclient.impl.bootstrap;

import cz.msebera.android.httpclient.ConnectionReuseStrategy;
import cz.msebera.android.httpclient.ExceptionLogger;
import cz.msebera.android.httpclient.HttpConnectionFactory;
import cz.msebera.android.httpclient.HttpRequestInterceptor;
import cz.msebera.android.httpclient.HttpResponseFactory;
import cz.msebera.android.httpclient.HttpResponseInterceptor;
import cz.msebera.android.httpclient.config.ConnectionConfig;
import cz.msebera.android.httpclient.config.SocketConfig;
import cz.msebera.android.httpclient.impl.DefaultBHttpServerConnection;
import cz.msebera.android.httpclient.impl.DefaultBHttpServerConnectionFactory;
import cz.msebera.android.httpclient.impl.DefaultConnectionReuseStrategy;
import cz.msebera.android.httpclient.impl.DefaultHttpResponseFactory;
import cz.msebera.android.httpclient.protocol.HttpExpectationVerifier;
import cz.msebera.android.httpclient.protocol.HttpProcessor;
import cz.msebera.android.httpclient.protocol.HttpProcessorBuilder;
import cz.msebera.android.httpclient.protocol.HttpRequestHandler;
import cz.msebera.android.httpclient.protocol.HttpRequestHandlerMapper;
import cz.msebera.android.httpclient.protocol.HttpService;
import cz.msebera.android.httpclient.protocol.ResponseConnControl;
import cz.msebera.android.httpclient.protocol.ResponseContent;
import cz.msebera.android.httpclient.protocol.ResponseDate;
import cz.msebera.android.httpclient.protocol.ResponseServer;
import cz.msebera.android.httpclient.protocol.UriHttpRequestHandlerMapper;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLContext;

public class ServerBootstrap {
    private ConnectionReuseStrategy connStrategy;
    private ConnectionConfig connectionConfig;
    private HttpConnectionFactory<? extends DefaultBHttpServerConnection> connectionFactory;
    private ExceptionLogger exceptionLogger;
    private HttpExpectationVerifier expectationVerifier;
    private Map<String, HttpRequestHandler> handlerMap;
    private HttpRequestHandlerMapper handlerMapper;
    private HttpProcessor httpProcessor;
    private int listenerPort;
    private InetAddress localAddress;
    private LinkedList<HttpRequestInterceptor> requestFirst;
    private LinkedList<HttpRequestInterceptor> requestLast;
    private HttpResponseFactory responseFactory;
    private LinkedList<HttpResponseInterceptor> responseFirst;
    private LinkedList<HttpResponseInterceptor> responseLast;
    private String serverInfo;
    private ServerSocketFactory serverSocketFactory;
    private SocketConfig socketConfig;
    private SSLContext sslContext;
    private SSLServerSetupHandler sslSetupHandler;

    private ServerBootstrap() {
    }

    public static ServerBootstrap bootstrap() {
        return new ServerBootstrap();
    }

    public final ServerBootstrap setListenerPort(int listenerPort) {
        this.listenerPort = listenerPort;
        return this;
    }

    public final ServerBootstrap setLocalAddress(InetAddress localAddress) {
        this.localAddress = localAddress;
        return this;
    }

    public final ServerBootstrap setSocketConfig(SocketConfig socketConfig) {
        this.socketConfig = socketConfig;
        return this;
    }

    public final ServerBootstrap setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
        return this;
    }

    public final ServerBootstrap setHttpProcessor(HttpProcessor httpProcessor) {
        this.httpProcessor = httpProcessor;
        return this;
    }

    public final ServerBootstrap addInterceptorFirst(HttpResponseInterceptor itcp) {
        if (itcp != null) {
            if (this.responseFirst == null) {
                this.responseFirst = new LinkedList();
            }
            this.responseFirst.addFirst(itcp);
        }
        return this;
    }

    public final ServerBootstrap addInterceptorLast(HttpResponseInterceptor itcp) {
        if (itcp != null) {
            if (this.responseLast == null) {
                this.responseLast = new LinkedList();
            }
            this.responseLast.addLast(itcp);
        }
        return this;
    }

    public final ServerBootstrap addInterceptorFirst(HttpRequestInterceptor itcp) {
        if (itcp != null) {
            if (this.requestFirst == null) {
                this.requestFirst = new LinkedList();
            }
            this.requestFirst.addFirst(itcp);
        }
        return this;
    }

    public final ServerBootstrap addInterceptorLast(HttpRequestInterceptor itcp) {
        if (itcp != null) {
            if (this.requestLast == null) {
                this.requestLast = new LinkedList();
            }
            this.requestLast.addLast(itcp);
        }
        return this;
    }

    public final ServerBootstrap setServerInfo(String serverInfo) {
        this.serverInfo = serverInfo;
        return this;
    }

    public final ServerBootstrap setConnectionReuseStrategy(ConnectionReuseStrategy connStrategy) {
        this.connStrategy = connStrategy;
        return this;
    }

    public final ServerBootstrap setResponseFactory(HttpResponseFactory responseFactory) {
        this.responseFactory = responseFactory;
        return this;
    }

    public final ServerBootstrap setHandlerMapper(HttpRequestHandlerMapper handlerMapper) {
        this.handlerMapper = handlerMapper;
        return this;
    }

    public final ServerBootstrap registerHandler(String pattern, HttpRequestHandler handler) {
        if (!(pattern == null || handler == null)) {
            if (this.handlerMap == null) {
                this.handlerMap = new HashMap();
            }
            this.handlerMap.put(pattern, handler);
        }
        return this;
    }

    public final ServerBootstrap setExpectationVerifier(HttpExpectationVerifier expectationVerifier) {
        this.expectationVerifier = expectationVerifier;
        return this;
    }

    public final ServerBootstrap setConnectionFactory(HttpConnectionFactory<? extends DefaultBHttpServerConnection> connectionFactory) {
        this.connectionFactory = connectionFactory;
        return this;
    }

    public final ServerBootstrap setSslSetupHandler(SSLServerSetupHandler sslSetupHandler) {
        this.sslSetupHandler = sslSetupHandler;
        return this;
    }

    public final ServerBootstrap setServerSocketFactory(ServerSocketFactory serverSocketFactory) {
        this.serverSocketFactory = serverSocketFactory;
        return this;
    }

    public final ServerBootstrap setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    public final ServerBootstrap setExceptionLogger(ExceptionLogger exceptionLogger) {
        this.exceptionLogger = exceptionLogger;
        return this;
    }

    public HttpServer create() {
        int i;
        SocketConfig socketConfig;
        HttpProcessor httpProcessorCopy = this.httpProcessor;
        if (httpProcessorCopy == null) {
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
            String serverInfoCopy = this.serverInfo;
            if (serverInfoCopy == null) {
                serverInfoCopy = "Apache-HttpCore/1.1";
            }
            b.addAll(new ResponseDate(), new ResponseServer(serverInfoCopy), new ResponseContent(), new ResponseConnControl());
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
            httpProcessorCopy = b.build();
        }
        HttpRequestHandlerMapper handlerMapperCopy = this.handlerMapper;
        if (handlerMapperCopy == null) {
            UriHttpRequestHandlerMapper reqistry = new UriHttpRequestHandlerMapper();
            if (this.handlerMap != null) {
                for (Entry<String, HttpRequestHandler> entry : this.handlerMap.entrySet()) {
                    reqistry.register((String) entry.getKey(), (HttpRequestHandler) entry.getValue());
                }
            }
            handlerMapperCopy = reqistry;
        }
        ConnectionReuseStrategy connStrategyCopy = this.connStrategy;
        if (connStrategyCopy == null) {
            connStrategyCopy = DefaultConnectionReuseStrategy.INSTANCE;
        }
        HttpResponseFactory responseFactoryCopy = this.responseFactory;
        if (responseFactoryCopy == null) {
            responseFactoryCopy = DefaultHttpResponseFactory.INSTANCE;
        }
        HttpService httpService = new HttpService(httpProcessorCopy, connStrategyCopy, responseFactoryCopy, handlerMapperCopy, this.expectationVerifier);
        ServerSocketFactory serverSocketFactoryCopy = this.serverSocketFactory;
        if (serverSocketFactoryCopy == null) {
            if (this.sslContext != null) {
                serverSocketFactoryCopy = this.sslContext.getServerSocketFactory();
            } else {
                serverSocketFactoryCopy = ServerSocketFactory.getDefault();
            }
        }
        HttpConnectionFactory<? extends DefaultBHttpServerConnection> connectionFactoryCopy = this.connectionFactory;
        if (connectionFactoryCopy == null) {
            if (this.connectionConfig != null) {
                connectionFactoryCopy = new DefaultBHttpServerConnectionFactory(this.connectionConfig);
            } else {
                connectionFactoryCopy = DefaultBHttpServerConnectionFactory.INSTANCE;
            }
        }
        ExceptionLogger exceptionLoggerCopy = this.exceptionLogger;
        if (exceptionLoggerCopy == null) {
            exceptionLoggerCopy = ExceptionLogger.NO_OP;
        }
        if (this.listenerPort > 0) {
            i = this.listenerPort;
        } else {
            i = 0;
        }
        InetAddress inetAddress = this.localAddress;
        if (this.socketConfig != null) {
            socketConfig = this.socketConfig;
        } else {
            socketConfig = SocketConfig.DEFAULT;
        }
        return new HttpServer(i, inetAddress, socketConfig, serverSocketFactoryCopy, httpService, connectionFactoryCopy, this.sslSetupHandler, exceptionLoggerCopy);
    }
}
