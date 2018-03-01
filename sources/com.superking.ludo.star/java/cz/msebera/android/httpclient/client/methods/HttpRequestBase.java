package cz.msebera.android.httpclient.client.methods;

import cz.msebera.android.httpclient.ProtocolVersion;
import cz.msebera.android.httpclient.RequestLine;
import cz.msebera.android.httpclient.annotation.NotThreadSafe;
import cz.msebera.android.httpclient.client.config.RequestConfig;
import cz.msebera.android.httpclient.message.BasicRequestLine;
import cz.msebera.android.httpclient.params.HttpProtocolParams;
import java.net.URI;

@NotThreadSafe
public abstract class HttpRequestBase extends AbstractExecutionAwareRequest implements Configurable, HttpUriRequest {
    private RequestConfig config;
    private URI uri;
    private ProtocolVersion version;

    public abstract String getMethod();

    public void setProtocolVersion(ProtocolVersion version) {
        this.version = version;
    }

    public ProtocolVersion getProtocolVersion() {
        return this.version != null ? this.version : HttpProtocolParams.getVersion(getParams());
    }

    public URI getURI() {
        return this.uri;
    }

    public RequestLine getRequestLine() {
        String method = getMethod();
        ProtocolVersion ver = getProtocolVersion();
        URI uriCopy = getURI();
        String uritext = null;
        if (uriCopy != null) {
            uritext = uriCopy.toASCIIString();
        }
        if (uritext == null || uritext.isEmpty()) {
            uritext = "/";
        }
        return new BasicRequestLine(method, uritext, ver);
    }

    public RequestConfig getConfig() {
        return this.config;
    }

    public void setConfig(RequestConfig config) {
        this.config = config;
    }

    public void setURI(URI uri) {
        this.uri = uri;
    }

    public void started() {
    }

    public void releaseConnection() {
        reset();
    }

    public String toString() {
        return getMethod() + " " + getURI() + " " + getProtocolVersion();
    }
}
