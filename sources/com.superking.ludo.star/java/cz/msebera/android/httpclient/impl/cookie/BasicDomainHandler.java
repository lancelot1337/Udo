package cz.msebera.android.httpclient.impl.cookie;

import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.conn.util.InetAddressUtils;
import cz.msebera.android.httpclient.cookie.ClientCookie;
import cz.msebera.android.httpclient.cookie.CommonCookieAttributeHandler;
import cz.msebera.android.httpclient.cookie.Cookie;
import cz.msebera.android.httpclient.cookie.CookieOrigin;
import cz.msebera.android.httpclient.cookie.CookieRestrictionViolationException;
import cz.msebera.android.httpclient.cookie.MalformedCookieException;
import cz.msebera.android.httpclient.cookie.SM;
import cz.msebera.android.httpclient.cookie.SetCookie;
import cz.msebera.android.httpclient.util.Args;
import cz.msebera.android.httpclient.util.TextUtils;
import java.util.Locale;

@Immutable
public class BasicDomainHandler implements CommonCookieAttributeHandler {
    public void parse(SetCookie cookie, String value) throws MalformedCookieException {
        Args.notNull(cookie, SM.COOKIE);
        if (TextUtils.isBlank(value)) {
            throw new MalformedCookieException("Blank or null value for domain attribute");
        } else if (!value.endsWith(".")) {
            String domain = value;
            if (domain.startsWith(".")) {
                domain = domain.substring(1);
            }
            cookie.setDomain(domain.toLowerCase(Locale.ROOT));
        }
    }

    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        Args.notNull(cookie, SM.COOKIE);
        Args.notNull(origin, "Cookie origin");
        String host = origin.getHost();
        String domain = cookie.getDomain();
        if (domain == null) {
            throw new CookieRestrictionViolationException("Cookie 'domain' may not be null");
        } else if (!host.equals(domain) && !domainMatch(domain, host)) {
            throw new CookieRestrictionViolationException("Illegal 'domain' attribute \"" + domain + "\". Domain of origin: \"" + host + "\"");
        }
    }

    static boolean domainMatch(String domain, String host) {
        if (InetAddressUtils.isIPv4Address(host) || InetAddressUtils.isIPv6Address(host)) {
            return false;
        }
        String normalizedDomain;
        if (domain.startsWith(".")) {
            normalizedDomain = domain.substring(1);
        } else {
            normalizedDomain = domain;
        }
        if (host.endsWith(normalizedDomain)) {
            int prefix = host.length() - normalizedDomain.length();
            if (prefix == 0) {
                return true;
            }
            if (prefix > 1 && host.charAt(prefix - 1) == '.') {
                return true;
            }
        }
        return false;
    }

    public boolean match(Cookie cookie, CookieOrigin origin) {
        Args.notNull(cookie, SM.COOKIE);
        Args.notNull(origin, "Cookie origin");
        String host = origin.getHost();
        String domain = cookie.getDomain();
        if (domain == null) {
            return false;
        }
        if (domain.startsWith(".")) {
            domain = domain.substring(1);
        }
        domain = domain.toLowerCase(Locale.ROOT);
        if (host.equals(domain)) {
            return true;
        }
        if ((cookie instanceof ClientCookie) && ((ClientCookie) cookie).containsAttribute(ClientCookie.DOMAIN_ATTR)) {
            return domainMatch(domain, host);
        }
        return false;
    }

    public String getAttributeName() {
        return ClientCookie.DOMAIN_ATTR;
    }
}
