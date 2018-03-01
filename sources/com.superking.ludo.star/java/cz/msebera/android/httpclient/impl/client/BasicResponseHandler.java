package cz.msebera.android.httpclient.impl.client;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.client.HttpResponseException;
import cz.msebera.android.httpclient.util.EntityUtils;
import java.io.IOException;

@Immutable
public class BasicResponseHandler extends AbstractResponseHandler<String> {
    public String handleEntity(HttpEntity entity) throws IOException {
        return EntityUtils.toString(entity);
    }

    public String handleResponse(HttpResponse response) throws HttpResponseException, IOException {
        return (String) super.handleResponse(response);
    }
}
