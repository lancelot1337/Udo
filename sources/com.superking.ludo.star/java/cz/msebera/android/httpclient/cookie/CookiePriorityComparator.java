package cz.msebera.android.httpclient.cookie;

import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.impl.cookie.BasicClientCookie;
import java.util.Comparator;
import java.util.Date;

@Immutable
public class CookiePriorityComparator implements Comparator<Cookie> {
    public static final CookiePriorityComparator INSTANCE = new CookiePriorityComparator();

    private int getPathLength(Cookie cookie) {
        String path = cookie.getPath();
        return path != null ? path.length() : 1;
    }

    public int compare(Cookie c1, Cookie c2) {
        int result = getPathLength(c2) - getPathLength(c1);
        if (result != 0 || !(c1 instanceof BasicClientCookie) || !(c2 instanceof BasicClientCookie)) {
            return result;
        }
        Date d1 = ((BasicClientCookie) c1).getCreationDate();
        Date d2 = ((BasicClientCookie) c2).getCreationDate();
        if (d1 == null || d2 == null) {
            return result;
        }
        return (int) (d1.getTime() - d2.getTime());
    }
}
