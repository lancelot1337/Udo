package cz.msebera.android.httpclient.impl.cookie;

import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.cookie.ClientCookie;
import cz.msebera.android.httpclient.cookie.Cookie;
import cz.msebera.android.httpclient.cookie.CookieOrigin;
import cz.msebera.android.httpclient.cookie.CookieRestrictionViolationException;
import cz.msebera.android.httpclient.cookie.MalformedCookieException;
import cz.msebera.android.httpclient.cookie.SM;
import cz.msebera.android.httpclient.cookie.SetCookie;
import cz.msebera.android.httpclient.util.Args;
import cz.msebera.android.httpclient.util.TextUtils;
import java.util.Locale;
import java.util.StringTokenizer;

@Immutable
public class NetscapeDomainHandler extends BasicDomainHandler {
    public void parse(SetCookie cookie, String value) throws MalformedCookieException {
        Args.notNull(cookie, SM.COOKIE);
        if (TextUtils.isBlank(value)) {
            throw new MalformedCookieException("Blank or null value for domain attribute");
        }
        cookie.setDomain(value);
    }

    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        String host = origin.getHost();
        String domain = cookie.getDomain();
        if (!host.equals(domain) && !BasicDomainHandler.domainMatch(domain, host)) {
            throw new CookieRestrictionViolationException("Illegal domain attribute \"" + domain + "\". Domain of origin: \"" + host + "\"");
        } else if (host.contains(".")) {
            int domainParts = new StringTokenizer(domain, ".").countTokens();
            if (isSpecialDomain(domain)) {
                if (domainParts < 2) {
                    throw new CookieRestrictionViolationException("Domain attribute \"" + domain + "\" violates the Netscape cookie specification for " + "special domains");
                }
            } else if (domainParts < 3) {
                throw new CookieRestrictionViolationException("Domain attribute \"" + domain + "\" violates the Netscape cookie specification");
            }
        }
    }

    private static boolean isSpecialDomain(String domain) {
        String ucDomain = domain.toUpperCase(Locale.ROOT);
        return ucDomain.endsWith(".COM") || ucDomain.endsWith(".EDU") || ucDomain.endsWith(".NET") || ucDomain.endsWith(".GOV") || ucDomain.endsWith(".MIL") || ucDomain.endsWith(".ORG") || ucDomain.endsWith(".INT");
    }

    public boolean match(Cookie cookie, CookieOrigin origin) {
        Args.notNull(cookie, SM.COOKIE);
        Args.notNull(origin, "Cookie origin");
        String host = origin.getHost();
        String domain = cookie.getDomain();
        if (domain == null) {
            return false;
        }
        return host.endsWith(domain);
    }

    public String getAttributeName() {
        return ClientCookie.DOMAIN_ATTR;
    }
}
