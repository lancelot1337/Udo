package cz.msebera.android.httpclient.client.protocol;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HeaderElement;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpException;
import cz.msebera.android.httpclient.HttpHeaders;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpResponseInterceptor;
import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.client.entity.DecompressingEntity;
import cz.msebera.android.httpclient.client.entity.DeflateInputStream;
import cz.msebera.android.httpclient.client.entity.InputStreamFactory;
import cz.msebera.android.httpclient.config.Lookup;
import cz.msebera.android.httpclient.config.RegistryBuilder;
import cz.msebera.android.httpclient.protocol.HTTP;
import cz.msebera.android.httpclient.protocol.HttpContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

@Immutable
public class ResponseContentEncoding implements HttpResponseInterceptor {
    private static final InputStreamFactory DEFLATE = new InputStreamFactory() {
        public InputStream create(InputStream instream) throws IOException {
            return new DeflateInputStream(instream);
        }
    };
    private static final InputStreamFactory GZIP = new InputStreamFactory() {
        public InputStream create(InputStream instream) throws IOException {
            return new GZIPInputStream(instream);
        }
    };
    public static final String UNCOMPRESSED = "http.client.response.uncompressed";
    private final Lookup<InputStreamFactory> decoderRegistry;

    public ResponseContentEncoding(Lookup<InputStreamFactory> decoderRegistry) {
        if (decoderRegistry == null) {
            decoderRegistry = RegistryBuilder.create().register("gzip", GZIP).register("x-gzip", GZIP).register("deflate", DEFLATE).build();
        }
        this.decoderRegistry = decoderRegistry;
    }

    public ResponseContentEncoding() {
        this(null);
    }

    public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
        HttpEntity entity = response.getEntity();
        if (HttpClientContext.adapt(context).getRequestConfig().isDecompressionEnabled() && entity != null && entity.getContentLength() != 0) {
            Header ceheader = entity.getContentEncoding();
            if (ceheader != null) {
                for (HeaderElement codec : ceheader.getElements()) {
                    String codecname = codec.getName().toLowerCase(Locale.ROOT);
                    InputStreamFactory decoderFactory = (InputStreamFactory) this.decoderRegistry.lookup(codecname);
                    if (decoderFactory != null) {
                        response.setEntity(new DecompressingEntity(response.getEntity(), decoderFactory));
                        response.removeHeaders(HTTP.CONTENT_LEN);
                        response.removeHeaders(HTTP.CONTENT_ENCODING);
                        response.removeHeaders(HttpHeaders.CONTENT_MD5);
                    } else if (!HTTP.IDENTITY_CODING.equals(codecname)) {
                        throw new HttpException("Unsupported Content-Coding: " + codec.getName());
                    }
                }
            }
        }
    }
}
