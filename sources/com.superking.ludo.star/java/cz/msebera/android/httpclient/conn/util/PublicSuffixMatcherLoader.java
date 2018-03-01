package cz.msebera.android.httpclient.conn.util;

import cz.msebera.android.httpclient.Consts;
import cz.msebera.android.httpclient.annotation.ThreadSafe;
import cz.msebera.android.httpclient.extras.HttpClientAndroidLog;
import cz.msebera.android.httpclient.util.Args;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;

@ThreadSafe
public final class PublicSuffixMatcherLoader {
    private static volatile PublicSuffixMatcher DEFAULT_INSTANCE;

    private static PublicSuffixMatcher load(InputStream in) throws IOException {
        PublicSuffixList list = new PublicSuffixListParser().parse(new InputStreamReader(in, Consts.UTF_8));
        return new PublicSuffixMatcher(list.getRules(), list.getExceptions());
    }

    public static PublicSuffixMatcher load(URL url) throws IOException {
        Args.notNull(url, "URL");
        InputStream in = url.openStream();
        try {
            PublicSuffixMatcher load = load(in);
            return load;
        } finally {
            in.close();
        }
    }

    public static PublicSuffixMatcher load(File file) throws IOException {
        Args.notNull(file, "File");
        InputStream in = new FileInputStream(file);
        try {
            PublicSuffixMatcher load = load(in);
            return load;
        } finally {
            in.close();
        }
    }

    public static PublicSuffixMatcher getDefault() {
        if (DEFAULT_INSTANCE == null) {
            synchronized (PublicSuffixMatcherLoader.class) {
                if (DEFAULT_INSTANCE == null) {
                    URL url = PublicSuffixMatcherLoader.class.getResource("/mozilla/public-suffix-list.txt");
                    if (url != null) {
                        try {
                            DEFAULT_INSTANCE = load(url);
                        } catch (IOException ex) {
                            HttpClientAndroidLog log = new HttpClientAndroidLog(PublicSuffixMatcherLoader.class);
                            if (log.isWarnEnabled()) {
                                log.warn("Failure loading public suffix list from default resource", ex);
                            }
                        }
                    } else {
                        DEFAULT_INSTANCE = new PublicSuffixMatcher(Arrays.asList(new String[]{"com"}), null);
                    }
                }
            }
        }
        return DEFAULT_INSTANCE;
    }
}
