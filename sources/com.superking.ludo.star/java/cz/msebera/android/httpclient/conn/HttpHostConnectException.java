package cz.msebera.android.httpclient.conn;

import cz.msebera.android.httpclient.HttpHost;
import cz.msebera.android.httpclient.annotation.Immutable;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.util.Arrays;
import org.cocos2dx.lib.BuildConfig;

@Immutable
public class HttpHostConnectException extends ConnectException {
    private static final long serialVersionUID = -3194482710275220224L;
    private final HttpHost host;

    @Deprecated
    public HttpHostConnectException(HttpHost host, ConnectException cause) {
        this(cause, host, (InetAddress[]) null);
    }

    public HttpHostConnectException(IOException cause, HttpHost host, InetAddress... remoteAddresses) {
        StringBuilder append = new StringBuilder().append("Connect to ").append(host != null ? host.toHostString() : "remote host");
        String str = (remoteAddresses == null || remoteAddresses.length <= 0) ? BuildConfig.FLAVOR : " " + Arrays.asList(remoteAddresses);
        append = append.append(str);
        if (cause == null || cause.getMessage() == null) {
            str = " refused";
        } else {
            str = " failed: " + cause.getMessage();
        }
        super(append.append(str).toString());
        this.host = host;
        initCause(cause);
    }

    public HttpHost getHost() {
        return this.host;
    }
}
