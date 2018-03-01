package cz.msebera.android.httpclient.conn.ssl;

import cz.msebera.android.httpclient.annotation.Immutable;
import javax.net.ssl.SSLException;

@Immutable
@Deprecated
public class StrictHostnameVerifier extends AbstractVerifier {
    public static final StrictHostnameVerifier INSTANCE = new StrictHostnameVerifier();

    public final void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
        verify(host, cns, subjectAlts, true);
    }

    public final String toString() {
        return "STRICT";
    }
}
