package cz.msebera.android.httpclient.impl.cookie;

import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.conn.util.PublicSuffixMatcher;
import cz.msebera.android.httpclient.cookie.CommonCookieAttributeHandler;
import cz.msebera.android.httpclient.cookie.Cookie;
import cz.msebera.android.httpclient.cookie.CookieOrigin;
import cz.msebera.android.httpclient.cookie.CookieSpec;
import cz.msebera.android.httpclient.cookie.CookieSpecProvider;
import cz.msebera.android.httpclient.cookie.MalformedCookieException;
import cz.msebera.android.httpclient.protocol.HttpContext;

@Immutable
public class DefaultCookieSpecProvider implements CookieSpecProvider {
    private final CompatibilityLevel compatibilityLevel;
    private volatile CookieSpec cookieSpec;
    private final String[] datepatterns;
    private final boolean oneHeader;
    private final PublicSuffixMatcher publicSuffixMatcher;

    public enum CompatibilityLevel {
        DEFAULT,
        IE_MEDIUM_SECURITY
    }

    public DefaultCookieSpecProvider(CompatibilityLevel compatibilityLevel, PublicSuffixMatcher publicSuffixMatcher, String[] datepatterns, boolean oneHeader) {
        if (compatibilityLevel == null) {
            compatibilityLevel = CompatibilityLevel.DEFAULT;
        }
        this.compatibilityLevel = compatibilityLevel;
        this.publicSuffixMatcher = publicSuffixMatcher;
        this.datepatterns = datepatterns;
        this.oneHeader = oneHeader;
    }

    public DefaultCookieSpecProvider(CompatibilityLevel compatibilityLevel, PublicSuffixMatcher publicSuffixMatcher) {
        this(compatibilityLevel, publicSuffixMatcher, null, false);
    }

    public DefaultCookieSpecProvider(PublicSuffixMatcher publicSuffixMatcher) {
        this(CompatibilityLevel.DEFAULT, publicSuffixMatcher, null, false);
    }

    public DefaultCookieSpecProvider() {
        this(CompatibilityLevel.DEFAULT, null, null, false);
    }

    public CookieSpec create(HttpContext context) {
        if (this.cookieSpec == null) {
            synchronized (this) {
                if (this.cookieSpec == null) {
                    AnonymousClass1 anonymousClass1;
                    RFC2965Spec strict = new RFC2965Spec(this.oneHeader, new RFC2965VersionAttributeHandler(), new BasicPathHandler(), PublicSuffixDomainFilter.decorate(new RFC2965DomainAttributeHandler(), this.publicSuffixMatcher), new RFC2965PortAttributeHandler(), new BasicMaxAgeHandler(), new BasicSecureHandler(), new BasicCommentHandler(), new RFC2965CommentUrlAttributeHandler(), new RFC2965DiscardAttributeHandler());
                    RFC2109Spec obsoleteStrict = new RFC2109Spec(this.oneHeader, new RFC2109VersionHandler(), new BasicPathHandler(), PublicSuffixDomainFilter.decorate(new RFC2109DomainHandler(), this.publicSuffixMatcher), new BasicMaxAgeHandler(), new BasicSecureHandler(), new BasicCommentHandler());
                    CommonCookieAttributeHandler[] commonCookieAttributeHandlerArr = new CommonCookieAttributeHandler[5];
                    commonCookieAttributeHandlerArr[0] = PublicSuffixDomainFilter.decorate(new BasicDomainHandler(), this.publicSuffixMatcher);
                    if (this.compatibilityLevel == CompatibilityLevel.IE_MEDIUM_SECURITY) {
                        anonymousClass1 = new BasicPathHandler() {
                            public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
                            }
                        };
                    } else {
                        anonymousClass1 = new BasicPathHandler();
                    }
                    commonCookieAttributeHandlerArr[1] = anonymousClass1;
                    commonCookieAttributeHandlerArr[2] = new BasicSecureHandler();
                    commonCookieAttributeHandlerArr[3] = new BasicCommentHandler();
                    commonCookieAttributeHandlerArr[4] = new BasicExpiresHandler(this.datepatterns != null ? (String[]) this.datepatterns.clone() : new String[]{"EEE, dd-MMM-yy HH:mm:ss z"});
                    this.cookieSpec = new DefaultCookieSpec(strict, obsoleteStrict, new NetscapeDraftSpec(commonCookieAttributeHandlerArr));
                }
            }
        }
        return this.cookieSpec;
    }
}
