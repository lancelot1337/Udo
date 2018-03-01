package cz.msebera.android.httpclient.impl.cookie;

import cz.msebera.android.httpclient.FormattedHeader;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HeaderElement;
import cz.msebera.android.httpclient.annotation.ThreadSafe;
import cz.msebera.android.httpclient.cookie.ClientCookie;
import cz.msebera.android.httpclient.cookie.CommonCookieAttributeHandler;
import cz.msebera.android.httpclient.cookie.Cookie;
import cz.msebera.android.httpclient.cookie.CookieOrigin;
import cz.msebera.android.httpclient.cookie.CookieSpec;
import cz.msebera.android.httpclient.cookie.MalformedCookieException;
import cz.msebera.android.httpclient.cookie.SM;
import cz.msebera.android.httpclient.cookie.SetCookie2;
import cz.msebera.android.httpclient.message.ParserCursor;
import cz.msebera.android.httpclient.util.Args;
import cz.msebera.android.httpclient.util.CharArrayBuffer;
import io.branch.referral.Branch;
import java.util.List;

@ThreadSafe
public class DefaultCookieSpec implements CookieSpec {
    private final NetscapeDraftSpec netscapeDraft;
    private final RFC2109Spec obsoleteStrict;
    private final RFC2965Spec strict;

    DefaultCookieSpec(RFC2965Spec strict, RFC2109Spec obsoleteStrict, NetscapeDraftSpec netscapeDraft) {
        this.strict = strict;
        this.obsoleteStrict = obsoleteStrict;
        this.netscapeDraft = netscapeDraft;
    }

    public DefaultCookieSpec(String[] datepatterns, boolean oneHeader) {
        this.strict = new RFC2965Spec(oneHeader, new RFC2965VersionAttributeHandler(), new BasicPathHandler(), new RFC2965DomainAttributeHandler(), new RFC2965PortAttributeHandler(), new BasicMaxAgeHandler(), new BasicSecureHandler(), new BasicCommentHandler(), new RFC2965CommentUrlAttributeHandler(), new RFC2965DiscardAttributeHandler());
        this.obsoleteStrict = new RFC2109Spec(oneHeader, new RFC2109VersionHandler(), new BasicPathHandler(), new RFC2109DomainHandler(), new BasicMaxAgeHandler(), new BasicSecureHandler(), new BasicCommentHandler());
        CommonCookieAttributeHandler[] commonCookieAttributeHandlerArr = new CommonCookieAttributeHandler[5];
        commonCookieAttributeHandlerArr[0] = new BasicDomainHandler();
        commonCookieAttributeHandlerArr[1] = new BasicPathHandler();
        commonCookieAttributeHandlerArr[2] = new BasicSecureHandler();
        commonCookieAttributeHandlerArr[3] = new BasicCommentHandler();
        commonCookieAttributeHandlerArr[4] = new BasicExpiresHandler(datepatterns != null ? (String[]) datepatterns.clone() : new String[]{"EEE, dd-MMM-yy HH:mm:ss z"});
        this.netscapeDraft = new NetscapeDraftSpec(commonCookieAttributeHandlerArr);
    }

    public DefaultCookieSpec() {
        this(null, false);
    }

    public List<Cookie> parse(Header header, CookieOrigin origin) throws MalformedCookieException {
        Args.notNull(header, "Header");
        Args.notNull(origin, "Cookie origin");
        HeaderElement[] helems = header.getElements();
        boolean versioned = false;
        boolean netscape = false;
        for (HeaderElement helem : helems) {
            if (helem.getParameterByName(ClientCookie.VERSION_ATTR) != null) {
                versioned = true;
            }
            if (helem.getParameterByName(ClientCookie.EXPIRES_ATTR) != null) {
                netscape = true;
            }
        }
        if (netscape || !versioned) {
            CharArrayBuffer buffer;
            ParserCursor cursor;
            NetscapeDraftHeaderParser parser = NetscapeDraftHeaderParser.DEFAULT;
            if (header instanceof FormattedHeader) {
                buffer = ((FormattedHeader) header).getBuffer();
                cursor = new ParserCursor(((FormattedHeader) header).getValuePos(), buffer.length());
            } else {
                String s = header.getValue();
                if (s == null) {
                    throw new MalformedCookieException("Header value is null");
                }
                buffer = new CharArrayBuffer(s.length());
                buffer.append(s);
                cursor = new ParserCursor(0, buffer.length());
            }
            return this.netscapeDraft.parse(new HeaderElement[]{parser.parseHeader(buffer, cursor)}, origin);
        } else if (SM.SET_COOKIE2.equals(header.getName())) {
            return this.strict.parse(helems, origin);
        } else {
            return this.obsoleteStrict.parse(helems, origin);
        }
    }

    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        Args.notNull(cookie, SM.COOKIE);
        Args.notNull(origin, "Cookie origin");
        if (cookie.getVersion() <= 0) {
            this.netscapeDraft.validate(cookie, origin);
        } else if (cookie instanceof SetCookie2) {
            this.strict.validate(cookie, origin);
        } else {
            this.obsoleteStrict.validate(cookie, origin);
        }
    }

    public boolean match(Cookie cookie, CookieOrigin origin) {
        Args.notNull(cookie, SM.COOKIE);
        Args.notNull(origin, "Cookie origin");
        if (cookie.getVersion() <= 0) {
            return this.netscapeDraft.match(cookie, origin);
        }
        if (cookie instanceof SetCookie2) {
            return this.strict.match(cookie, origin);
        }
        return this.obsoleteStrict.match(cookie, origin);
    }

    public List<Header> formatCookies(List<Cookie> cookies) {
        Args.notNull(cookies, "List of cookies");
        int version = Integer.MAX_VALUE;
        boolean isSetCookie2 = true;
        for (Cookie cookie : cookies) {
            if (!(cookie instanceof SetCookie2)) {
                isSetCookie2 = false;
            }
            if (cookie.getVersion() < version) {
                version = cookie.getVersion();
            }
        }
        if (version <= 0) {
            return this.netscapeDraft.formatCookies(cookies);
        }
        if (isSetCookie2) {
            return this.strict.formatCookies(cookies);
        }
        return this.obsoleteStrict.formatCookies(cookies);
    }

    public int getVersion() {
        return this.strict.getVersion();
    }

    public Header getVersionHeader() {
        return null;
    }

    public String toString() {
        return Branch.REFERRAL_BUCKET_DEFAULT;
    }
}
