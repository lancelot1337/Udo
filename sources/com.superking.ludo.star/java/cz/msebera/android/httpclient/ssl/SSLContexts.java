package cz.msebera.android.httpclient.ssl;

import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.conn.ssl.SSLSocketFactory;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;

@Immutable
public class SSLContexts {
    public static SSLContext createDefault() throws SSLInitializationException {
        try {
            SSLContext sslcontext = SSLContext.getInstance(SSLSocketFactory.TLS);
            sslcontext.init(null, null, null);
            return sslcontext;
        } catch (NoSuchAlgorithmException ex) {
            throw new SSLInitializationException(ex.getMessage(), ex);
        } catch (KeyManagementException ex2) {
            throw new SSLInitializationException(ex2.getMessage(), ex2);
        }
    }

    public static SSLContext createSystemDefault() throws SSLInitializationException {
        try {
            return SSLContext.getDefault();
        } catch (NoSuchAlgorithmException e) {
            return createDefault();
        }
    }

    public static SSLContextBuilder custom() {
        return SSLContextBuilder.create();
    }
}
