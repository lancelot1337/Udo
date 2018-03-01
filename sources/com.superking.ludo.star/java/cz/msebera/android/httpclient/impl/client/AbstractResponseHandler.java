package cz.msebera.android.httpclient.impl.client;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.StatusLine;
import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.client.HttpResponseException;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.util.EntityUtils;
import java.io.IOException;

@Immutable
public abstract class AbstractResponseHandler<T> implements ResponseHandler<T> {
    public abstract T handleEntity(HttpEntity httpEntity) throws IOException;

    public T handleResponse(HttpResponse response) throws HttpResponseException, IOException {
        StatusLine statusLine = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        if (statusLine.getStatusCode() < HttpStatus.SC_MULTIPLE_CHOICES) {
            return entity == null ? null : handleEntity(entity);
        } else {
            EntityUtils.consume(entity);
            throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
        }
    }
}
