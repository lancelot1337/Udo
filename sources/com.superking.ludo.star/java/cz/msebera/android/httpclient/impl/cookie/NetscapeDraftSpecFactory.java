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
public class NetscapeDraftSpecFactory implements CookieSpecFactory, CookieSpecProvider {
    private final CookieSpec cookieSpec;

    public NetscapeDraftSpecFactory(String[] datepatterns) {
        this.cookieSpec = new NetscapeDraftSpec(datepatterns);
    }

    public NetscapeDraftSpecFactory() {
        this(null);
    }

    public CookieSpec newInstance(HttpParams params) {
        if (params == null) {
            return new NetscapeDraftSpec();
        }
        String[] patterns = null;
        Collection<?> param = (Collection) params.getParameter(CookieSpecPNames.DATE_PATTERNS);
        if (param != null) {
            patterns = (String[]) param.toArray(new String[param.size()]);
        }
        return new NetscapeDraftSpec(patterns);
    }

    public CookieSpec create(HttpContext context) {
        return this.cookieSpec;
    }
}
