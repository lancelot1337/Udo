package cz.msebera.android.httpclient.impl.client;

import cz.msebera.android.httpclient.HttpRequest;
import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.client.methods.HttpDelete;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpHead;
import cz.msebera.android.httpclient.client.methods.HttpOptions;
import cz.msebera.android.httpclient.client.methods.HttpPut;
import cz.msebera.android.httpclient.client.methods.HttpTrace;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Immutable
public class StandardHttpRequestRetryHandler extends DefaultHttpRequestRetryHandler {
    private final Map<String, Boolean> idempotentMethods;

    public StandardHttpRequestRetryHandler(int retryCount, boolean requestSentRetryEnabled) {
        super(retryCount, requestSentRetryEnabled);
        this.idempotentMethods = new ConcurrentHashMap();
        this.idempotentMethods.put(HttpGet.METHOD_NAME, Boolean.TRUE);
        this.idempotentMethods.put(HttpHead.METHOD_NAME, Boolean.TRUE);
        this.idempotentMethods.put(HttpPut.METHOD_NAME, Boolean.TRUE);
        this.idempotentMethods.put(HttpDelete.METHOD_NAME, Boolean.TRUE);
        this.idempotentMethods.put(HttpOptions.METHOD_NAME, Boolean.TRUE);
        this.idempotentMethods.put(HttpTrace.METHOD_NAME, Boolean.TRUE);
    }

    public StandardHttpRequestRetryHandler() {
        this(3, false);
    }

    protected boolean handleAsIdempotent(HttpRequest request) {
        Boolean b = (Boolean) this.idempotentMethods.get(request.getRequestLine().getMethod().toUpperCase(Locale.ROOT));
        return b != null && b.booleanValue();
    }
}
