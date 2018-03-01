package cz.msebera.android.httpclient.conn;

import cz.msebera.android.httpclient.annotation.NotThreadSafe;
import cz.msebera.android.httpclient.protocol.HTTP;
import cz.msebera.android.httpclient.util.Args;
import java.io.IOException;
import java.io.InputStream;

@Deprecated
@NotThreadSafe
public class BasicEofSensorWatcher implements EofSensorWatcher {
    protected final boolean attemptReuse;
    protected final ManagedClientConnection managedConn;

    public BasicEofSensorWatcher(ManagedClientConnection conn, boolean reuse) {
        Args.notNull(conn, HTTP.CONN_DIRECTIVE);
        this.managedConn = conn;
        this.attemptReuse = reuse;
    }

    public boolean eofDetected(InputStream wrapped) throws IOException {
        try {
            if (this.attemptReuse) {
                wrapped.close();
                this.managedConn.markReusable();
            }
            this.managedConn.releaseConnection();
            return false;
        } catch (Throwable th) {
            this.managedConn.releaseConnection();
        }
    }

    public boolean streamClosed(InputStream wrapped) throws IOException {
        try {
            if (this.attemptReuse) {
                wrapped.close();
                this.managedConn.markReusable();
            }
            this.managedConn.releaseConnection();
            return false;
        } catch (Throwable th) {
            this.managedConn.releaseConnection();
        }
    }

    public boolean streamAbort(InputStream wrapped) throws IOException {
        this.managedConn.abortConnection();
        return false;
    }
}
