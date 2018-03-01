package cz.msebera.android.httpclient.impl.cookie;

import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.cookie.ClientCookie;
import cz.msebera.android.httpclient.cookie.CommonCookieAttributeHandler;
import cz.msebera.android.httpclient.cookie.Cookie;
import cz.msebera.android.httpclient.cookie.CookieOrigin;
import cz.msebera.android.httpclient.cookie.CookieRestrictionViolationException;
import cz.msebera.android.httpclient.cookie.MalformedCookieException;
import cz.msebera.android.httpclient.cookie.SM;
import cz.msebera.android.httpclient.cookie.SetCookie;
import cz.msebera.android.httpclient.util.Args;
import java.util.Locale;

@Immutable
public class RFC2965DomainAttributeHandler implements CommonCookieAttributeHandler {
    public void parse(SetCookie cookie, String domain) throws MalformedCookieException {
        Args.notNull(cookie, SM.COOKIE);
        if (domain == null) {
            throw new MalformedCookieException("Missing value for domain attribute");
        } else if (domain.trim().isEmpty()) {
            throw new MalformedCookieException("Blank value for domain attribute");
        } else {
            String s = domain.toLowerCase(Locale.ROOT);
            if (!domain.startsWith(".")) {
                s = '.' + s;
            }
            cookie.setDomain(s);
        }
    }

    public boolean domainMatch(String host, String domain) {
        return host.equals(domain) || (domain.startsWith(".") && host.endsWith(domain));
    }

    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        Args.notNull(cookie, SM.COOKIE);
        Args.notNull(origin, "Cookie origin");
        String host = origin.getHost().toLowerCase(Locale.ROOT);
        if (cookie.getDomain() == null) {
            throw new CookieRestrictionViolationException("Invalid cookie state: domain not specified");
        }
        String cookieDomain = cookie.getDomain().toLowerCase(Locale.ROOT);
        if ((cookie instanceof ClientCookie) && ((ClientCookie) cookie).containsAttribute(ClientCookie.DOMAIN_ATTR)) {
            if (cookieDomain.startsWith(".")) {
                int dotIndex = cookieDomain.indexOf(46, 1);
                if ((dotIndex < 0 || dotIndex == cookieDomain.length() - 1) && !cookieDomain.equals(".local")) {
                    throw new CookieRestrictionViolationException("Domain attribute \"" + cookie.getDomain() + "\" violates RFC 2965: the value contains no embedded dots " + "and the value is not .local");
                } else if (!domainMatch(host, cookieDomain)) {
                    throw new CookieRestrictionViolationException("Domain attribute \"" + cookie.getDomain() + "\" violates RFC 2965: effective host name does not " + "domain-match domain attribute.");
                } else if (host.substring(0, host.length() - cookieDomain.length()).indexOf(46) != -1) {
                    throw new CookieRestrictionViolationException("Domain attribute \"" + cookie.getDomain() + "\" violates RFC 2965: " + "effective host minus domain may not contain any dots");
                } else {
                    return;
                }
            }
            throw new CookieRestrictionViolationException("Domain attribute \"" + cookie.getDomain() + "\" violates RFC 2109: domain must start with a dot");
        } else if (!cookie.getDomain().equals(host)) {
            throw new CookieRestrictionViolationException("Illegal domain attribute: \"" + cookie.getDomain() + "\"." + "Domain of origin: \"" + host + "\"");
        }
    }

    public boolean match(Cookie cookie, CookieOrigin origin) {
        Args.notNull(cookie, SM.COOKIE);
        Args.notNull(origin, "Cookie origin");
        String host = origin.getHost().toLowerCase(Locale.ROOT);
        String cookieDomain = cookie.getDomain();
        if (domainMatch(host, cookieDomain) && host.substring(0, host.length() - cookieDomain.length()).indexOf(46) == -1) {
            return true;
        }
        return false;
    }

    public String getAttributeName() {
        return ClientCookie.DOMAIN_ATTR;
    }
}
