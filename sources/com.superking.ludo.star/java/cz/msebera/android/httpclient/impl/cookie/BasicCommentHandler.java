package cz.msebera.android.httpclient.impl.cookie;

import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.cookie.ClientCookie;
import cz.msebera.android.httpclient.cookie.CommonCookieAttributeHandler;
import cz.msebera.android.httpclient.cookie.MalformedCookieException;
import cz.msebera.android.httpclient.cookie.SM;
import cz.msebera.android.httpclient.cookie.SetCookie;
import cz.msebera.android.httpclient.util.Args;

@Immutable
public class BasicCommentHandler extends AbstractCookieAttributeHandler implements CommonCookieAttributeHandler {
    public void parse(SetCookie cookie, String value) throws MalformedCookieException {
        Args.notNull(cookie, SM.COOKIE);
        cookie.setComment(value);
    }

    public String getAttributeName() {
        return ClientCookie.COMMENT_ATTR;
    }
}
