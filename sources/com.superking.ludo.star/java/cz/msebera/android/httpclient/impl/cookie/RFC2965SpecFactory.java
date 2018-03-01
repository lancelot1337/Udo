package cz.msebera.android.httpclient.impl.cookie;

import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.cookie.CookieSpec;
import cz.msebera.android.httpclient.cookie.CookieSpecFactory;
import cz.msebera.android.httpclient.cookie.CookieSpecProvider;
import cz.msebera.android.httpclient.cookie.params.CookieSpecPNames;
import cz.msebera.android.httpclient.params.HttpParams;
import cz.msebera.android.httpclient.protocol.HttpContext;
import java.util.Collection;

@Immutable
@Deprecated
public class RFC2965SpecFactory implements CookieSpecFactory, CookieSpecProvider {
    private final CookieSpec cookieSpec;

    public RFC2965SpecFactory(String[] datepatterns, boolean oneHeader) {
        this.cookieSpec = new RFC2965Spec(datepatterns, oneHeader);
    }

    public RFC2965SpecFactory() {
        this(null, false);
    }

    public CookieSpec newInstance(HttpParams params) {
        if (params == null) {
            return new RFC2965Spec();
        }
        String[] patterns = null;
        Collection<?> param = (Collection) params.getParameter(CookieSpecPNames.DATE_PATTERNS);
        if (param != null) {
            patterns = (String[]) param.toArray(new String[param.size()]);
        }
        return new RFC2965Spec(patterns, params.getBooleanParameter(CookieSpecPNames.SINGLE_COOKIE_HEADER, false));
    }

    public CookieSpec create(HttpContext context) {
        return this.cookieSpec;
    }
}
