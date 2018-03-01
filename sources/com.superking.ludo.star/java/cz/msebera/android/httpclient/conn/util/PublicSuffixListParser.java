package cz.msebera.android.httpclient.conn.util;

import cz.msebera.android.httpclient.annotation.Immutable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Immutable
public final class PublicSuffixListParser {
    private static final int MAX_LINE_LEN = 256;

    public PublicSuffixList parse(Reader reader) throws IOException {
        List<String> rules = new ArrayList();
        List<String> exceptions = new ArrayList();
        BufferedReader r = new BufferedReader(reader);
        StringBuilder sb = new StringBuilder(MAX_LINE_LEN);
        boolean more = true;
        while (more) {
            more = readLine(r, sb);
            String line = sb.toString();
            if (!(line.isEmpty() || line.startsWith("//"))) {
                if (line.startsWith(".")) {
                    line = line.substring(1);
                }
                boolean isException = line.startsWith("!");
                if (isException) {
                    line = line.substring(1);
                }
                if (isException) {
                    exceptions.add(line);
                } else {
                    rules.add(line);
                }
            }
        }
        return new PublicSuffixList(rules, exceptions);
    }

    private boolean readLine(Reader r, StringBuilder sb) throws IOException {
        sb.setLength(0);
        boolean hitWhitespace = false;
        do {
            int b = r.read();
            if (b != -1) {
                char c = (char) b;
                if (c != '\n') {
                    if (Character.isWhitespace(c)) {
                        hitWhitespace = true;
                    }
                    if (!hitWhitespace) {
                        sb.append(c);
                    }
                }
            }
            if (b != -1) {
                return true;
            }
            return false;
        } while (sb.length() <= MAX_LINE_LEN);
        return false;
    }
}
