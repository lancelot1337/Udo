package cz.msebera.android.httpclient.client.methods;

import cz.msebera.android.httpclient.HttpRequest;
import cz.msebera.android.httpclient.client.utils.CloneUtils;
import cz.msebera.android.httpclient.concurrent.Cancellable;
import cz.msebera.android.httpclient.conn.ClientConnectionRequest;
import cz.msebera.android.httpclient.conn.ConnectionReleaseTrigger;
import cz.msebera.android.httpclient.message.AbstractHttpMessage;
import cz.msebera.android.httpclient.message.HeaderGroup;
import cz.msebera.android.httpclient.params.HttpParams;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractExecutionAwareRequest extends AbstractHttpMessage implements HttpRequest, AbortableHttpRequest, HttpExecutionAware, Cloneable {
    private final AtomicBoolean aborted = new AtomicBoolean(false);
    private final AtomicReference<Cancellable> cancellableRef = new AtomicReference(null);

    protected AbstractExecutionAwareRequest() {
    }

    @Deprecated
    public void setConnectionRequest(final ClientConnectionRequest connRequest) {
        setCancellable(new Cancellable() {
            public boolean cancel() {
                connRequest.abortRequest();
                return true;
            }
        });
    }

    @Deprecated
    public void setReleaseTrigger(final ConnectionReleaseTrigger releaseTrigger) {
        setCancellable(new Cancellable() {
            public boolean cancel() {
                try {
                    releaseTrigger.abortConnection();
                    return true;
                } catch (IOException e) {
                    return false;
                }
            }
        });
    }

    public void abort() {
        if (this.aborted.compareAndSet(false, true)) {
            Cancellable cancellable = (Cancellable) this.cancellableRef.getAndSet(null);
            if (cancellable != null) {
                cancellable.cancel();
            }
        }
    }

    public boolean isAborted() {
        return this.aborted.get();
    }

    public void setCancellable(Cancellable cancellable) {
        if (!this.aborted.get()) {
            this.cancellableRef.set(cancellable);
        }
    }

    public Object clone() throws CloneNotSupportedException {
        AbstractExecutionAwareRequest clone = (AbstractExecutionAwareRequest) super.clone();
        clone.headergroup = (HeaderGroup) CloneUtils.cloneObject(this.headergroup);
        clone.params = (HttpParams) CloneUtils.cloneObject(this.params);
        return clone;
    }

    public void completed() {
        this.cancellableRef.set(null);
    }

    public void reset() {
        Cancellable cancellable = (Cancellable) this.cancellableRef.getAndSet(null);
        if (cancellable != null) {
            cancellable.cancel();
        }
        this.aborted.set(false);
    }
}
