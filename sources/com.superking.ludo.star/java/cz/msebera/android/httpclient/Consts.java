package cz.msebera.android.httpclient;

import cz.msebera.android.httpclient.protocol.HTTP;
import java.nio.charset.Charset;

public final class Consts {
    public static final Charset ASCII = Charset.forName(HTTP.US_ASCII);
    public static final int CR = 13;
    public static final int HT = 9;
    public static final Charset ISO_8859_1 = Charset.forName(HTTP.ISO_8859_1);
    public static final int LF = 10;
    public static final int SP = 32;
    public static final Charset UTF_8 = Charset.forName(HTTP.UTF_8);

    private Consts() {
    }
}
