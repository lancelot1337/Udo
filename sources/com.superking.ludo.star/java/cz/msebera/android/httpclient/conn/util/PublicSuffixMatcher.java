package cz.msebera.android.httpclient.conn.util;

import cz.msebera.android.httpclient.annotation.ThreadSafe;
import cz.msebera.android.httpclient.util.Args;
import java.net.IDN;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ThreadSafe
public final class PublicSuffixMatcher {
    private final Map<String, String> exceptions;
    private final Map<String, String> rules;

    public PublicSuffixMatcher(Collection<String> rules, Collection<String> exceptions) {
        Args.notNull(rules, "Domain suffix rules");
        this.rules = new ConcurrentHashMap(rules.size());
        for (String rule : rules) {
            this.rules.put(rule, rule);
        }
        if (exceptions != null) {
            this.exceptions = new ConcurrentHashMap(exceptions.size());
            for (String exception : exceptions) {
                this.exceptions.put(exception, exception);
            }
            return;
        }
        this.exceptions = null;
    }

    public String getDomainRoot(String domain) {
        if (domain == null) {
            return null;
        }
        if (domain.startsWith(".")) {
            return null;
        }
        String domainName = null;
        String segment = domain.toLowerCase(Locale.ROOT);
        while (segment != null) {
            if (this.exceptions == null || !this.exceptions.containsKey(IDN.toUnicode(segment))) {
                if (!this.rules.containsKey(IDN.toUnicode(segment))) {
                    String nextSegment;
                    int nextdot = segment.indexOf(46);
                    if (nextdot != -1) {
                        nextSegment = segment.substring(nextdot + 1);
                    } else {
                        nextSegment = null;
                    }
                    if (nextSegment != null && this.rules.containsKey("*." + IDN.toUnicode(nextSegment))) {
                        break;
                    }
                    if (nextdot != -1) {
                        domainName = segment;
                    }
                    segment = nextSegment;
                } else {
                    break;
                }
            }
            return segment;
        }
        return domainName;
    }

    public boolean matches(String domain) {
        boolean z = true;
        if (domain == null) {
            return false;
        }
        if (domain.startsWith(".")) {
            domain = domain.substring(1);
        }
        if (getDomainRoot(domain) != null) {
            z = false;
        }
        return z;
    }
}
