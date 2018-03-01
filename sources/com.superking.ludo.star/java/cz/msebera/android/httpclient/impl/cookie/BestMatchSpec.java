package cz.msebera.android.httpclient.impl.cookie;

import cz.msebera.android.httpclient.annotation.ThreadSafe;
import cz.msebera.android.httpclient.client.params.CookiePolicy;

@ThreadSafe
@Deprecated
public class BestMatchSpec extends DefaultCookieSpec {
    public BestMatchSpec(String[] datepatterns, boolean oneHeader) {
        super(datepatterns, oneHeader);
    }

    public BestMatchSpec() {
        this(null, false);
    }

    public String toString() {
        return CookiePolicy.BEST_MATCH;
    }
}
