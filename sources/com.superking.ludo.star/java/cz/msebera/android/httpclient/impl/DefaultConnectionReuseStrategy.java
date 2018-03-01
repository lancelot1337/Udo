package cz.msebera.android.httpclient.impl;

import cz.msebera.android.httpclient.ConnectionReuseStrategy;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HeaderIterator;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.HttpVersion;
import cz.msebera.android.httpclient.ParseException;
import cz.msebera.android.httpclient.ProtocolVersion;
import cz.msebera.android.httpclient.TokenIterator;
import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.message.BasicHeaderIterator;
import cz.msebera.android.httpclient.message.BasicTokenIterator;
import cz.msebera.android.httpclient.protocol.HTTP;
import cz.msebera.android.httpclient.protocol.HttpContext;
import cz.msebera.android.httpclient.util.Args;

@Immutable
public class DefaultConnectionReuseStrategy implements ConnectionReuseStrategy {
    public static final DefaultConnectionReuseStrategy INSTANCE = new DefaultConnectionReuseStrategy();

    public boolean keepAlive(HttpResponse response, HttpContext context) {
        Args.notNull(response, "HTTP response");
        Args.notNull(context, "HTTP context");
        ProtocolVersion ver = response.getStatusLine().getProtocolVersion();
        Header teh = response.getFirstHeader(HTTP.TRANSFER_ENCODING);
        if (teh != null) {
            if (!HTTP.CHUNK_CODING.equalsIgnoreCase(teh.getValue())) {
                return false;
            }
        } else if (canResponseHaveBody(response)) {
            Header[] clhs = response.getHeaders(HTTP.CONTENT_LEN);
            if (clhs.length != 1) {
                return false;
            }
            try {
                if (Integer.parseInt(clhs[0].getValue()) < 0) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        Header[] connHeaders = response.getHeaders(HTTP.CONN_DIRECTIVE);
        if (connHeaders.length == 0) {
            connHeaders = response.getHeaders("Proxy-Connection");
        }
        if (connHeaders.length != 0) {
            try {
                TokenIterator ti = new BasicTokenIterator(new BasicHeaderIterator(connHeaders, null));
                boolean keepalive = false;
                while (ti.hasNext()) {
                    String token = ti.nextToken();
                    if (HTTP.CONN_CLOSE.equalsIgnoreCase(token)) {
                        return false;
                    }
                    if (HTTP.CONN_KEEP_ALIVE.equalsIgnoreCase(token)) {
                        keepalive = true;
                    }
                }
                if (keepalive) {
                    return true;
                }
            } catch (ParseException e2) {
                return false;
            }
        }
        return !ver.lessEquals(HttpVersion.HTTP_1_0);
    }

    protected TokenIterator createTokenIterator(HeaderIterator hit) {
        return new BasicTokenIterator(hit);
    }

    private boolean canResponseHaveBody(HttpResponse response) {
        int status = response.getStatusLine().getStatusCode();
        return (status < HttpStatus.SC_OK || status == HttpStatus.SC_NO_CONTENT || status == HttpStatus.SC_NOT_MODIFIED || status == HttpStatus.SC_RESET_CONTENT) ? false : true;
    }
}
