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

@Immutable
public class RFC2109VersionHandler extends AbstractCookieAttributeHandler implements CommonCookieAttributeHandler {
    public void parse(SetCookie cookie, String value) throws MalformedCookieException {
        Args.notNull(cookie, SM.COOKIE);
        if (value == null) {
            throw new MalformedCookieException("Missing value for version attribute");
        } else if (value.trim().isEmpty()) {
            throw new MalformedCookieException("Blank value for version attribute");
        } else {
            try {
                cookie.setVersion(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                throw new MalformedCookieException("Invalid version: " + e.getMessage());
            }
        }
    }

    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        Args.notNull(cookie, SM.COOKIE);
        if (cookie.getVersion() < 0) {
            throw new CookieRestrictionViolationException("Cookie version may not be negative");
        }
    }

    public String getAttributeName() {
        return ClientCookie.VERSION_ATTR;
    }
}
