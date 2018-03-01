package com.bugsnag.android;

import android.support.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URLConnection;

class IOUtils {
    private static final int DEFAULT_BUFFER_SIZE = 4096;
    private static final int EOF = -1;

    IOUtils() {
    }

    public static void closeQuietly(@Nullable Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
            }
        }
    }

    public static void close(@Nullable URLConnection conn) {
        if (conn instanceof HttpURLConnection) {
            ((HttpURLConnection) conn).disconnect();
        }
    }

    public static int copy(Reader input, Writer output) throws IOException {
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        long count = 0;
        while (true) {
            int n = input.read(buffer);
            if (EOF == n) {
                break;
            }
            output.write(buffer, 0, n);
            count += (long) n;
        }
        if (count > 2147483647L) {
            return EOF;
        }
        return (int) count;
    }
}
