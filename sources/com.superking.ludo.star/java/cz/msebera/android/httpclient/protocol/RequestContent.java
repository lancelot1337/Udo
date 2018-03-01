package cz.msebera.android.httpclient.protocol;

import com.facebook.appevents.AppEventsConstants;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpEntityEnclosingRequest;
import cz.msebera.android.httpclient.HttpException;
import cz.msebera.android.httpclient.HttpRequest;
import cz.msebera.android.httpclient.HttpRequestInterceptor;
import cz.msebera.android.httpclient.HttpVersion;
import cz.msebera.android.httpclient.ProtocolException;
import cz.msebera.android.httpclient.ProtocolVersion;
import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.util.Args;
import java.io.IOException;

@Immutable
public class RequestContent implements HttpRequestInterceptor {
    private final boolean overwrite;

    public RequestContent() {
        this(false);
    }

    public RequestContent(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        if (request instanceof HttpEntityEnclosingRequest) {
            if (this.overwrite) {
                request.removeHeaders(HTTP.TRANSFER_ENCODING);
                request.removeHeaders(HTTP.CONTENT_LEN);
            } else if (request.containsHeader(HTTP.TRANSFER_ENCODING)) {
                throw new ProtocolException("Transfer-encoding header already present");
            } else if (request.containsHeader(HTTP.CONTENT_LEN)) {
                throw new ProtocolException("Content-Length header already present");
            }
            ProtocolVersion ver = request.getRequestLine().getProtocolVersion();
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            if (entity == null) {
                request.addHeader(HTTP.CONTENT_LEN, AppEventsConstants.EVENT_PARAM_VALUE_NO);
                return;
            }
            if (!entity.isChunked() && entity.getContentLength() >= 0) {
                request.addHeader(HTTP.CONTENT_LEN, Long.toString(entity.getContentLength()));
            } else if (ver.lessEquals(HttpVersion.HTTP_1_0)) {
                throw new ProtocolException("Chunked transfer encoding not allowed for " + ver);
            } else {
                request.addHeader(HTTP.TRANSFER_ENCODING, HTTP.CHUNK_CODING);
            }
            if (!(entity.getContentType() == null || request.containsHeader(HTTP.CONTENT_TYPE))) {
                request.addHeader(entity.getContentType());
            }
            if (entity.getContentEncoding() != null && !request.containsHeader(HTTP.CONTENT_ENCODING)) {
                request.addHeader(entity.getContentEncoding());
            }
        }
    }
}
