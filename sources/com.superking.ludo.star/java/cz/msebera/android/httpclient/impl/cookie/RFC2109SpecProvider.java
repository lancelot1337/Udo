package cz.msebera.android.httpclient.impl.cookie;

import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.annotation.Obsolete;
import cz.msebera.android.httpclient.conn.util.PublicSuffixMatcher;
import cz.msebera.android.httpclient.cookie.CookieSpec;
import cz.msebera.android.httpclient.cookie.CookieSpecProvider;
import cz.msebera.android.httpclient.protocol.HttpContext;

@Obsolete
@Immutable
public class RFC2109SpecProvider implements CookieSpecProvider {
    private volatile CookieSpec cookieSpec;
    private final boolean oneHeader;
    private final PublicSuffixMatcher publicSuffixMatcher;

    public RFC2109SpecProvider(PublicSuffixMatcher publicSuffixMatcher, boolean oneHeader) {
        this.oneHeader = oneHeader;
        this.publicSuffixMatcher = publicSuffixMatcher;
    }

    public RFC2109SpecProvider(PublicSuffixMatcher publicSuffixMatcher) {
        this(publicSuffixMatcher, false);
    }

    public RFC2109SpecProvider() {
        this(null, false);
    }

    public CookieSpec create(HttpContext context) {
        if (this.cookieSpec == null) {
            synchronized (this) {
                if (this.cookieSpec == null) {
                    this.cookieSpec = new RFC2109Spec(this.oneHeader, new RFC2109VersionHandler(), new BasicPathHandler(), PublicSuffixDomainFilter.decorate(new RFC2109DomainHandler(), this.publicSuffixMatcher), new BasicMaxAgeHandler(), new BasicSecureHandler(), new BasicCommentHandler());
                }
            }
        }
        return this.cookieSpec;
    }
}
