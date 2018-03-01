package cz.msebera.android.httpclient.client.methods;

import cz.msebera.android.httpclient.Consts;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HeaderIterator;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpEntityEnclosingRequest;
import cz.msebera.android.httpclient.HttpRequest;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.ProtocolVersion;
import cz.msebera.android.httpclient.annotation.NotThreadSafe;
import cz.msebera.android.httpclient.client.config.RequestConfig;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.utils.URIBuilder;
import cz.msebera.android.httpclient.client.utils.URLEncodedUtils;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.message.HeaderGroup;
import cz.msebera.android.httpclient.protocol.HTTP;
import cz.msebera.android.httpclient.util.Args;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@NotThreadSafe
public class RequestBuilder {
    private Charset charset;
    private RequestConfig config;
    private HttpEntity entity;
    private HeaderGroup headergroup;
    private String method;
    private List<NameValuePair> parameters;
    private URI uri;
    private ProtocolVersion version;

    static class InternalEntityEclosingRequest extends HttpEntityEnclosingRequestBase {
        private final String method;

        InternalEntityEclosingRequest(String method) {
            this.method = method;
        }

        public String getMethod() {
            return this.method;
        }
    }

    static class InternalRequest extends HttpRequestBase {
        private final String method;

        InternalRequest(String method) {
            this.method = method;
        }

        public String getMethod() {
            return this.method;
        }
    }

    RequestBuilder(String method) {
        this.charset = Consts.UTF_8;
        this.method = method;
    }

    RequestBuilder(String method, URI uri) {
        this.method = method;
        this.uri = uri;
    }

    RequestBuilder(String method, String uri) {
        this.method = method;
        this.uri = uri != null ? URI.create(uri) : null;
    }

    RequestBuilder() {
        this(null);
    }

    public static RequestBuilder create(String method) {
        Args.notBlank(method, "HTTP method");
        return new RequestBuilder(method);
    }

    public static RequestBuilder get() {
        return new RequestBuilder(HttpGet.METHOD_NAME);
    }

    public static RequestBuilder get(URI uri) {
        return new RequestBuilder(HttpGet.METHOD_NAME, uri);
    }

    public static RequestBuilder get(String uri) {
        return new RequestBuilder(HttpGet.METHOD_NAME, uri);
    }

    public static RequestBuilder head() {
        return new RequestBuilder(HttpHead.METHOD_NAME);
    }

    public static RequestBuilder head(URI uri) {
        return new RequestBuilder(HttpHead.METHOD_NAME, uri);
    }

    public static RequestBuilder head(String uri) {
        return new RequestBuilder(HttpHead.METHOD_NAME, uri);
    }

    public static RequestBuilder patch() {
        return new RequestBuilder(HttpPatch.METHOD_NAME);
    }

    public static RequestBuilder patch(URI uri) {
        return new RequestBuilder(HttpPatch.METHOD_NAME, uri);
    }

    public static RequestBuilder patch(String uri) {
        return new RequestBuilder(HttpPatch.METHOD_NAME, uri);
    }

    public static RequestBuilder post() {
        return new RequestBuilder(HttpPost.METHOD_NAME);
    }

    public static RequestBuilder post(URI uri) {
        return new RequestBuilder(HttpPost.METHOD_NAME, uri);
    }

    public static RequestBuilder post(String uri) {
        return new RequestBuilder(HttpPost.METHOD_NAME, uri);
    }

    public static RequestBuilder put() {
        return new RequestBuilder(HttpPut.METHOD_NAME);
    }

    public static RequestBuilder put(URI uri) {
        return new RequestBuilder(HttpPut.METHOD_NAME, uri);
    }

    public static RequestBuilder put(String uri) {
        return new RequestBuilder(HttpPut.METHOD_NAME, uri);
    }

    public static RequestBuilder delete() {
        return new RequestBuilder(HttpDelete.METHOD_NAME);
    }

    public static RequestBuilder delete(URI uri) {
        return new RequestBuilder(HttpDelete.METHOD_NAME, uri);
    }

    public static RequestBuilder delete(String uri) {
        return new RequestBuilder(HttpDelete.METHOD_NAME, uri);
    }

    public static RequestBuilder trace() {
        return new RequestBuilder(HttpTrace.METHOD_NAME);
    }

    public static RequestBuilder trace(URI uri) {
        return new RequestBuilder(HttpTrace.METHOD_NAME, uri);
    }

    public static RequestBuilder trace(String uri) {
        return new RequestBuilder(HttpTrace.METHOD_NAME, uri);
    }

    public static RequestBuilder options() {
        return new RequestBuilder(HttpOptions.METHOD_NAME);
    }

    public static RequestBuilder options(URI uri) {
        return new RequestBuilder(HttpOptions.METHOD_NAME, uri);
    }

    public static RequestBuilder options(String uri) {
        return new RequestBuilder(HttpOptions.METHOD_NAME, uri);
    }

    public static RequestBuilder copy(HttpRequest request) {
        Args.notNull(request, "HTTP request");
        return new RequestBuilder().doCopy(request);
    }

    private RequestBuilder doCopy(HttpRequest request) {
        URI originalUri;
        if (request != null) {
            this.method = request.getRequestLine().getMethod();
            this.version = request.getRequestLine().getProtocolVersion();
            if (this.headergroup == null) {
                this.headergroup = new HeaderGroup();
            }
            this.headergroup.clear();
            this.headergroup.setHeaders(request.getAllHeaders());
            this.parameters = null;
            this.entity = null;
            if (request instanceof HttpEntityEnclosingRequest) {
                HttpEntity originalEntity = ((HttpEntityEnclosingRequest) request).getEntity();
                ContentType contentType = ContentType.get(originalEntity);
                if (contentType == null || !contentType.getMimeType().equals(ContentType.APPLICATION_FORM_URLENCODED.getMimeType())) {
                    this.entity = originalEntity;
                } else {
                    try {
                        List<NameValuePair> formParams = URLEncodedUtils.parse(originalEntity);
                        if (!formParams.isEmpty()) {
                            this.parameters = formParams;
                        }
                    } catch (IOException e) {
                    }
                }
            }
            if (request instanceof HttpUriRequest) {
                originalUri = ((HttpUriRequest) request).getURI();
            } else {
                originalUri = URI.create(request.getRequestLine().getUri());
            }
            URIBuilder uriBuilder = new URIBuilder(originalUri);
            if (this.parameters == null) {
                List<NameValuePair> queryParams = uriBuilder.getQueryParams();
                if (queryParams.isEmpty()) {
                    this.parameters = null;
                } else {
                    this.parameters = queryParams;
                    uriBuilder.clearParameters();
                }
            }
            try {
                this.uri = uriBuilder.build();
            } catch (URISyntaxException e2) {
                this.uri = originalUri;
            }
            if (request instanceof Configurable) {
                this.config = ((Configurable) request).getConfig();
            } else {
                this.config = null;
            }
        }
        return this;
    }

    public RequestBuilder setCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public Charset getCharset() {
        return this.charset;
    }

    public String getMethod() {
        return this.method;
    }

    public ProtocolVersion getVersion() {
        return this.version;
    }

    public RequestBuilder setVersion(ProtocolVersion version) {
        this.version = version;
        return this;
    }

    public URI getUri() {
        return this.uri;
    }

    public RequestBuilder setUri(URI uri) {
        this.uri = uri;
        return this;
    }

    public RequestBuilder setUri(String uri) {
        this.uri = uri != null ? URI.create(uri) : null;
        return this;
    }

    public Header getFirstHeader(String name) {
        return this.headergroup != null ? this.headergroup.getFirstHeader(name) : null;
    }

    public Header getLastHeader(String name) {
        return this.headergroup != null ? this.headergroup.getLastHeader(name) : null;
    }

    public Header[] getHeaders(String name) {
        return this.headergroup != null ? this.headergroup.getHeaders(name) : null;
    }

    public RequestBuilder addHeader(Header header) {
        if (this.headergroup == null) {
            this.headergroup = new HeaderGroup();
        }
        this.headergroup.addHeader(header);
        return this;
    }

    public RequestBuilder addHeader(String name, String value) {
        if (this.headergroup == null) {
            this.headergroup = new HeaderGroup();
        }
        this.headergroup.addHeader(new BasicHeader(name, value));
        return this;
    }

    public RequestBuilder removeHeader(Header header) {
        if (this.headergroup == null) {
            this.headergroup = new HeaderGroup();
        }
        this.headergroup.removeHeader(header);
        return this;
    }

    public RequestBuilder removeHeaders(String name) {
        if (!(name == null || this.headergroup == null)) {
            HeaderIterator i = this.headergroup.iterator();
            while (i.hasNext()) {
                if (name.equalsIgnoreCase(i.nextHeader().getName())) {
                    i.remove();
                }
            }
        }
        return this;
    }

    public RequestBuilder setHeader(Header header) {
        if (this.headergroup == null) {
            this.headergroup = new HeaderGroup();
        }
        this.headergroup.updateHeader(header);
        return this;
    }

    public RequestBuilder setHeader(String name, String value) {
        if (this.headergroup == null) {
            this.headergroup = new HeaderGroup();
        }
        this.headergroup.updateHeader(new BasicHeader(name, value));
        return this;
    }

    public HttpEntity getEntity() {
        return this.entity;
    }

    public RequestBuilder setEntity(HttpEntity entity) {
        this.entity = entity;
        return this;
    }

    public List<NameValuePair> getParameters() {
        return this.parameters != null ? new ArrayList(this.parameters) : new ArrayList();
    }

    public RequestBuilder addParameter(NameValuePair nvp) {
        Args.notNull(nvp, "Name value pair");
        if (this.parameters == null) {
            this.parameters = new LinkedList();
        }
        this.parameters.add(nvp);
        return this;
    }

    public RequestBuilder addParameter(String name, String value) {
        return addParameter(new BasicNameValuePair(name, value));
    }

    public RequestBuilder addParameters(NameValuePair... nvps) {
        for (NameValuePair nvp : nvps) {
            addParameter(nvp);
        }
        return this;
    }

    public RequestConfig getConfig() {
        return this.config;
    }

    public RequestBuilder setConfig(RequestConfig config) {
        this.config = config;
        return this;
    }

    public HttpUriRequest build() {
        HttpRequestBase result;
        URI uriNotNull = this.uri != null ? this.uri : URI.create("/");
        HttpEntity entityCopy = this.entity;
        if (!(this.parameters == null || this.parameters.isEmpty())) {
            if (entityCopy == null && (HttpPost.METHOD_NAME.equalsIgnoreCase(this.method) || HttpPut.METHOD_NAME.equalsIgnoreCase(this.method))) {
                entityCopy = new UrlEncodedFormEntity(this.parameters, HTTP.DEF_CONTENT_CHARSET);
            } else {
                try {
                    uriNotNull = new URIBuilder(uriNotNull).setCharset(this.charset).addParameters(this.parameters).build();
                } catch (URISyntaxException e) {
                }
            }
        }
        if (entityCopy == null) {
            result = new InternalRequest(this.method);
        } else {
            HttpRequestBase request = new InternalEntityEclosingRequest(this.method);
            request.setEntity(entityCopy);
            result = request;
        }
        result.setProtocolVersion(this.version);
        result.setURI(uriNotNull);
        if (this.headergroup != null) {
            result.setHeaders(this.headergroup.getAllHeaders());
        }
        result.setConfig(this.config);
        return result;
    }
}
