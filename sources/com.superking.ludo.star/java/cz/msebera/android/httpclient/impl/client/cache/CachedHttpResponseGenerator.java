package cz.msebera.android.httpclient.impl.client.cache;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpHeaders;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.HttpVersion;
import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.client.cache.HeaderConstants;
import cz.msebera.android.httpclient.client.cache.HttpCacheEntry;
import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpRequestWrapper;
import cz.msebera.android.httpclient.client.utils.DateUtils;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.message.BasicHttpResponse;
import cz.msebera.android.httpclient.protocol.HTTP;
import java.util.Date;
import org.cocos2dx.lib.BuildConfig;

@Immutable
class CachedHttpResponseGenerator {
    private final CacheValidityPolicy validityStrategy;

    CachedHttpResponseGenerator(CacheValidityPolicy validityStrategy) {
        this.validityStrategy = validityStrategy;
    }

    CachedHttpResponseGenerator() {
        this(new CacheValidityPolicy());
    }

    CloseableHttpResponse generateResponse(HttpRequestWrapper request, HttpCacheEntry entry) {
        Date now = new Date();
        HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, entry.getStatusCode(), entry.getReasonPhrase());
        response.setHeaders(entry.getAllHeaders());
        if (responseShouldContainEntity(request, entry)) {
            HttpEntity entity = new CacheEntity(entry);
            addMissingContentLengthHeader(response, entity);
            response.setEntity(entity);
        }
        long age = this.validityStrategy.getCurrentAgeSecs(entry, now);
        if (age > 0) {
            if (age >= 2147483647L) {
                response.setHeader(HeaderConstants.AGE, "2147483648");
            } else {
                response.setHeader(HeaderConstants.AGE, BuildConfig.FLAVOR + ((int) age));
            }
        }
        return Proxies.enhanceResponse(response);
    }

    CloseableHttpResponse generateNotModifiedResponse(HttpCacheEntry entry) {
        HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, (int) HttpStatus.SC_NOT_MODIFIED, "Not Modified");
        Header dateHeader = entry.getFirstHeader(HTTP.DATE_HEADER);
        if (dateHeader == null) {
            dateHeader = new BasicHeader(HTTP.DATE_HEADER, DateUtils.formatDate(new Date()));
        }
        response.addHeader(dateHeader);
        Header etagHeader = entry.getFirstHeader(HeaderConstants.ETAG);
        if (etagHeader != null) {
            response.addHeader(etagHeader);
        }
        Header contentLocationHeader = entry.getFirstHeader(HttpHeaders.CONTENT_LOCATION);
        if (contentLocationHeader != null) {
            response.addHeader(contentLocationHeader);
        }
        Header expiresHeader = entry.getFirstHeader(HeaderConstants.EXPIRES);
        if (expiresHeader != null) {
            response.addHeader(expiresHeader);
        }
        Header cacheControlHeader = entry.getFirstHeader(HeaderConstants.CACHE_CONTROL);
        if (cacheControlHeader != null) {
            response.addHeader(cacheControlHeader);
        }
        Header varyHeader = entry.getFirstHeader(HeaderConstants.VARY);
        if (varyHeader != null) {
            response.addHeader(varyHeader);
        }
        return Proxies.enhanceResponse(response);
    }

    private void addMissingContentLengthHeader(HttpResponse response, HttpEntity entity) {
        if (!transferEncodingIsPresent(response) && response.getFirstHeader(HTTP.CONTENT_LEN) == null) {
            response.setHeader(new BasicHeader(HTTP.CONTENT_LEN, Long.toString(entity.getContentLength())));
        }
    }

    private boolean transferEncodingIsPresent(HttpResponse response) {
        return response.getFirstHeader(HTTP.TRANSFER_ENCODING) != null;
    }

    private boolean responseShouldContainEntity(HttpRequestWrapper request, HttpCacheEntry cacheEntry) {
        return request.getRequestLine().getMethod().equals(HttpGet.METHOD_NAME) && cacheEntry.getResource() != null;
    }
}
