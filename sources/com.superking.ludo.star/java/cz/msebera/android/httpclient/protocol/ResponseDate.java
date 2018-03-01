package cz.msebera.android.httpclient.protocol;

import cz.msebera.android.httpclient.HttpException;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpResponseInterceptor;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.annotation.ThreadSafe;
import cz.msebera.android.httpclient.util.Args;
import java.io.IOException;

@ThreadSafe
public class ResponseDate implements HttpResponseInterceptor {
    private static final HttpDateGenerator DATE_GENERATOR = new HttpDateGenerator();

    public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
        Args.notNull(response, "HTTP response");
        if (response.getStatusLine().getStatusCode() >= HttpStatus.SC_OK && !response.containsHeader(HTTP.DATE_HEADER)) {
            response.setHeader(HTTP.DATE_HEADER, DATE_GENERATOR.getCurrentDate());
        }
    }
}
