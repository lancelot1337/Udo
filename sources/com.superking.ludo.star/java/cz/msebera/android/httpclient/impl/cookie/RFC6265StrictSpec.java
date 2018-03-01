package cz.msebera.android.httpclient.impl.cookie;

import cz.msebera.android.httpclient.annotation.ThreadSafe;
import cz.msebera.android.httpclient.cookie.CommonCookieAttributeHandler;
import cz.msebera.android.httpclient.protocol.HttpDateGenerator;
import java.util.List;

@ThreadSafe
public class RFC6265StrictSpec extends RFC6265CookieSpecBase {
    static final String[] DATE_PATTERNS = new String[]{HttpDateGenerator.PATTERN_RFC1123, DateUtils.PATTERN_RFC1036, DateUtils.PATTERN_ASCTIME};

    public /* bridge */ /* synthetic */ List formatCookies(List list) {
        return super.formatCookies(list);
    }

    public RFC6265StrictSpec() {
        super(new BasicPathHandler(), new BasicDomainHandler(), new BasicMaxAgeHandler(), new BasicSecureHandler(), new BasicExpiresHandler(DATE_PATTERNS));
    }

    RFC6265StrictSpec(CommonCookieAttributeHandler... handlers) {
        super(handlers);
    }

    public String toString() {
        return "rfc6265-strict";
    }
}
