package cz.msebera.android.httpclient.client.protocol;

import cz.msebera.android.httpclient.HttpException;
import cz.msebera.android.httpclient.HttpHeaders;
import cz.msebera.android.httpclient.HttpRequest;
import cz.msebera.android.httpclient.HttpRequestInterceptor;
import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.protocol.HttpContext;
import java.io.IOException;
import java.util.List;

@Immutable
public class RequestAcceptEncoding implements HttpRequestInterceptor {
    private final String acceptEncoding;

    public RequestAcceptEncoding(List<String> encodings) {
        if (encodings == null || encodings.isEmpty()) {
            this.acceptEncoding = "gzip,deflate";
            return;
        }
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < encodings.size(); i++) {
            if (i > 0) {
                buf.append(",");
            }
            buf.append((String) encodings.get(i));
        }
        this.acceptEncoding = buf.toString();
    }

    public RequestAcceptEncoding() {
        this(null);
    }

    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        if (!request.containsHeader(HttpHeaders.ACCEPT_ENCODING)) {
            request.addHeader(HttpHeaders.ACCEPT_ENCODING, this.acceptEncoding);
        }
    }
}
