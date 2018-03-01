package cz.msebera.android.httpclient.impl.cookie;

import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.conn.util.PublicSuffixList;
import cz.msebera.android.httpclient.conn.util.PublicSuffixMatcher;
import cz.msebera.android.httpclient.cookie.CommonCookieAttributeHandler;
import cz.msebera.android.httpclient.cookie.Cookie;
import cz.msebera.android.httpclient.cookie.CookieOrigin;
import cz.msebera.android.httpclient.cookie.MalformedCookieException;
import cz.msebera.android.httpclient.cookie.SetCookie;
import cz.msebera.android.httpclient.util.Args;

@Immutable
public class PublicSuffixDomainFilter implements CommonCookieAttributeHandler {
    private final CommonCookieAttributeHandler handler;
    private final PublicSuffixMatcher publicSuffixMatcher;

    public PublicSuffixDomainFilter(CommonCookieAttributeHandler handler, PublicSuffixMatcher publicSuffixMatcher) {
        this.handler = (CommonCookieAttributeHandler) Args.notNull(handler, "Cookie handler");
        this.publicSuffixMatcher = (PublicSuffixMatcher) Args.notNull(publicSuffixMatcher, "Public suffix matcher");
    }

    public PublicSuffixDomainFilter(CommonCookieAttributeHandler handler, PublicSuffixList suffixList) {
        Args.notNull(handler, "Cookie handler");
        Args.notNull(suffixList, "Public suffix list");
        this.handler = handler;
        this.publicSuffixMatcher = new PublicSuffixMatcher(suffixList.getRules(), suffixList.getExceptions());
    }

    public boolean match(Cookie cookie, CookieOrigin origin) {
        String domain = cookie.getDomain();
        if (domain.equalsIgnoreCase("localhost") || !this.publicSuffixMatcher.matches(domain)) {
            return this.handler.match(cookie, origin);
        }
        return false;
    }

    public void parse(SetCookie cookie, String value) throws MalformedCookieException {
        this.handler.parse(cookie, value);
    }

    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        this.handler.validate(cookie, origin);
    }

    public String getAttributeName() {
        return this.handler.getAttributeName();
    }

    public static CommonCookieAttributeHandler decorate(CommonCookieAttributeHandler handler, PublicSuffixMatcher publicSuffixMatcher) {
        Args.notNull(handler, "Cookie attribute handler");
        return publicSuffixMatcher != null ? new PublicSuffixDomainFilter(handler, publicSuffixMatcher) : handler;
    }
}
