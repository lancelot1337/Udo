package cz.msebera.android.httpclient.impl.cookie;

import cz.msebera.android.httpclient.annotation.ThreadSafe;
import cz.msebera.android.httpclient.cookie.CommonCookieAttributeHandler;
import java.util.List;

@ThreadSafe
public class RFC6265LaxSpec extends RFC6265CookieSpecBase {
    public /* bridge */ /* synthetic */ List formatCookies(List list) {
        return super.formatCookies(list);
    }

    public RFC6265LaxSpec() {
        super(new BasicPathHandler(), new BasicDomainHandler(), new LaxMaxAgeHandler(), new BasicSecureHandler(), new LaxExpiresHandler());
    }

    RFC6265LaxSpec(CommonCookieAttributeHandler... handlers) {
        super(handlers);
    }

    public String toString() {
        return "rfc6265-lax";
    }
}
