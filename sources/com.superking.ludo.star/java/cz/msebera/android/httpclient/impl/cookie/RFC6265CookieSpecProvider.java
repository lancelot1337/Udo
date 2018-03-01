package cz.msebera.android.httpclient.impl.cookie;

import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.conn.util.PublicSuffixMatcher;
import cz.msebera.android.httpclient.cookie.Cookie;
import cz.msebera.android.httpclient.cookie.CookieOrigin;
import cz.msebera.android.httpclient.cookie.CookieSpec;
import cz.msebera.android.httpclient.cookie.CookieSpecProvider;
import cz.msebera.android.httpclient.cookie.MalformedCookieException;
import cz.msebera.android.httpclient.protocol.HttpContext;
import io.branch.referral.R;
import org.cocos2dx.lib.Cocos2dxHandler;

@Immutable
public class RFC6265CookieSpecProvider implements CookieSpecProvider {
    private final CompatibilityLevel compatibilityLevel;
    private volatile CookieSpec cookieSpec;
    private final PublicSuffixMatcher publicSuffixMatcher;

    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$cz$msebera$android$httpclient$impl$cookie$RFC6265CookieSpecProvider$CompatibilityLevel = new int[CompatibilityLevel.values().length];

        static {
            try {
                $SwitchMap$cz$msebera$android$httpclient$impl$cookie$RFC6265CookieSpecProvider$CompatibilityLevel[CompatibilityLevel.STRICT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$cz$msebera$android$httpclient$impl$cookie$RFC6265CookieSpecProvider$CompatibilityLevel[CompatibilityLevel.IE_MEDIUM_SECURITY.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public enum CompatibilityLevel {
        STRICT,
        RELAXED,
        IE_MEDIUM_SECURITY
    }

    public RFC6265CookieSpecProvider(CompatibilityLevel compatibilityLevel, PublicSuffixMatcher publicSuffixMatcher) {
        if (compatibilityLevel == null) {
            compatibilityLevel = CompatibilityLevel.RELAXED;
        }
        this.compatibilityLevel = compatibilityLevel;
        this.publicSuffixMatcher = publicSuffixMatcher;
    }

    public RFC6265CookieSpecProvider(PublicSuffixMatcher publicSuffixMatcher) {
        this(CompatibilityLevel.RELAXED, publicSuffixMatcher);
    }

    public RFC6265CookieSpecProvider() {
        this(CompatibilityLevel.RELAXED, null);
    }

    public CookieSpec create(HttpContext context) {
        if (this.cookieSpec == null) {
            synchronized (this) {
                if (this.cookieSpec == null) {
                    switch (AnonymousClass2.$SwitchMap$cz$msebera$android$httpclient$impl$cookie$RFC6265CookieSpecProvider$CompatibilityLevel[this.compatibilityLevel.ordinal()]) {
                        case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                            this.cookieSpec = new RFC6265StrictSpec(new BasicPathHandler(), PublicSuffixDomainFilter.decorate(new BasicDomainHandler(), this.publicSuffixMatcher), new BasicMaxAgeHandler(), new BasicSecureHandler(), new BasicExpiresHandler(RFC6265StrictSpec.DATE_PATTERNS));
                            break;
                        case R.styleable.View_paddingStart /*2*/:
                            this.cookieSpec = new RFC6265LaxSpec(new BasicPathHandler() {
                                public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
                                }
                            }, PublicSuffixDomainFilter.decorate(new BasicDomainHandler(), this.publicSuffixMatcher), new BasicMaxAgeHandler(), new BasicSecureHandler(), new BasicExpiresHandler(RFC6265StrictSpec.DATE_PATTERNS));
                            break;
                        default:
                            this.cookieSpec = new RFC6265LaxSpec(new BasicPathHandler(), PublicSuffixDomainFilter.decorate(new BasicDomainHandler(), this.publicSuffixMatcher), new LaxMaxAgeHandler(), new BasicSecureHandler(), new LaxExpiresHandler());
                            break;
                    }
                }
            }
        }
        return this.cookieSpec;
    }
}
