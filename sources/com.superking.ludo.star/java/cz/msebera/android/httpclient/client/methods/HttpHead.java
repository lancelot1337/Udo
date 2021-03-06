package cz.msebera.android.httpclient.client.methods;

import cz.msebera.android.httpclient.annotation.NotThreadSafe;
import java.net.URI;

@NotThreadSafe
public class HttpHead extends HttpRequestBase {
    public static final String METHOD_NAME = "HEAD";

    public HttpHead(URI uri) {
        setURI(uri);
    }

    public HttpHead(String uri) {
        setURI(URI.create(uri));
    }

    public String getMethod() {
        return METHOD_NAME;
    }
}
