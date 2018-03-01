package cz.msebera.android.httpclient.impl.client.cache;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HeaderElement;
import cz.msebera.android.httpclient.ProtocolException;
import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.client.cache.HeaderConstants;
import cz.msebera.android.httpclient.client.cache.HttpCacheEntry;
import cz.msebera.android.httpclient.client.methods.HttpRequestWrapper;
import java.util.Map;

@Immutable
class ConditionalRequestBuilder {
    ConditionalRequestBuilder() {
    }

    public HttpRequestWrapper buildConditionalRequest(HttpRequestWrapper request, HttpCacheEntry cacheEntry) throws ProtocolException {
        HttpRequestWrapper newRequest = HttpRequestWrapper.wrap(request.getOriginal());
        newRequest.setHeaders(request.getAllHeaders());
        Header eTag = cacheEntry.getFirstHeader(HeaderConstants.ETAG);
        if (eTag != null) {
            newRequest.setHeader(HeaderConstants.IF_NONE_MATCH, eTag.getValue());
        }
        Header lastModified = cacheEntry.getFirstHeader(HeaderConstants.LAST_MODIFIED);
        if (lastModified != null) {
            newRequest.setHeader(HeaderConstants.IF_MODIFIED_SINCE, lastModified.getValue());
        }
        boolean mustRevalidate = false;
        for (Header h : cacheEntry.getHeaders(HeaderConstants.CACHE_CONTROL)) {
            for (HeaderElement elt : h.getElements()) {
                if (HeaderConstants.CACHE_CONTROL_MUST_REVALIDATE.equalsIgnoreCase(elt.getName()) || HeaderConstants.CACHE_CONTROL_PROXY_REVALIDATE.equalsIgnoreCase(elt.getName())) {
                    mustRevalidate = true;
                    break;
                }
            }
        }
        if (mustRevalidate) {
            newRequest.addHeader(HeaderConstants.CACHE_CONTROL, "max-age=0");
        }
        return newRequest;
    }

    public HttpRequestWrapper buildConditionalRequestFromVariants(HttpRequestWrapper request, Map<String, Variant> variants) {
        HttpRequestWrapper newRequest = HttpRequestWrapper.wrap(request.getOriginal());
        newRequest.setHeaders(request.getAllHeaders());
        StringBuilder etags = new StringBuilder();
        boolean first = true;
        for (String etag : variants.keySet()) {
            if (!first) {
                etags.append(",");
            }
            first = false;
            etags.append(etag);
        }
        newRequest.setHeader(HeaderConstants.IF_NONE_MATCH, etags.toString());
        return newRequest;
    }

    public HttpRequestWrapper buildUnconditionalRequest(HttpRequestWrapper request, HttpCacheEntry entry) {
        HttpRequestWrapper newRequest = HttpRequestWrapper.wrap(request.getOriginal());
        newRequest.setHeaders(request.getAllHeaders());
        newRequest.addHeader(HeaderConstants.CACHE_CONTROL, HeaderConstants.CACHE_CONTROL_NO_CACHE);
        newRequest.addHeader(HeaderConstants.PRAGMA, HeaderConstants.CACHE_CONTROL_NO_CACHE);
        newRequest.removeHeaders(HeaderConstants.IF_RANGE);
        newRequest.removeHeaders(HeaderConstants.IF_MATCH);
        newRequest.removeHeaders(HeaderConstants.IF_NONE_MATCH);
        newRequest.removeHeaders(HeaderConstants.IF_UNMODIFIED_SINCE);
        newRequest.removeHeaders(HeaderConstants.IF_MODIFIED_SINCE);
        return newRequest;
    }
}
