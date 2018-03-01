package cz.msebera.android.httpclient.impl.cookie;

import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.cookie.ClientCookie;
import cz.msebera.android.httpclient.cookie.CommonCookieAttributeHandler;
import cz.msebera.android.httpclient.cookie.MalformedCookieException;
import cz.msebera.android.httpclient.cookie.SM;
import cz.msebera.android.httpclient.cookie.SetCookie;
import cz.msebera.android.httpclient.util.Args;
import cz.msebera.android.httpclient.util.TextUtils;
import java.util.Date;
import java.util.regex.Pattern;

@Immutable
public class LaxMaxAgeHandler extends AbstractCookieAttributeHandler implements CommonCookieAttributeHandler {
    private static final Pattern MAX_AGE_PATTERN = Pattern.compile("^\\-?[0-9]+$");

    public void parse(SetCookie cookie, String value) throws MalformedCookieException {
        Args.notNull(cookie, SM.COOKIE);
        if (!TextUtils.isBlank(value) && MAX_AGE_PATTERN.matcher(value).matches()) {
            try {
                int age = Integer.parseInt(value);
                cookie.setExpiryDate(age >= 0 ? new Date(System.currentTimeMillis() + (((long) age) * 1000)) : new Date(Long.MIN_VALUE));
            } catch (NumberFormatException e) {
            }
        }
    }

    public String getAttributeName() {
        return ClientCookie.MAX_AGE_ATTR;
    }
}
