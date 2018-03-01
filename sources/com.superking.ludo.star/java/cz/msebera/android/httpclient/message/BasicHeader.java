package cz.msebera.android.httpclient.message;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HeaderElement;
import cz.msebera.android.httpclient.ParseException;
import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.util.Args;
import java.io.Serializable;

@Immutable
public class BasicHeader implements Header, Serializable, Cloneable {
    private static final long serialVersionUID = -5427236326487562174L;
    private final String name;
    private final String value;

    public BasicHeader(String name, String value) {
        this.name = (String) Args.notNull(name, "Name");
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public String toString() {
        return BasicLineFormatter.INSTANCE.formatHeader(null, (Header) this).toString();
    }

    public HeaderElement[] getElements() throws ParseException {
        if (this.value != null) {
            return BasicHeaderValueParser.parseElements(this.value, null);
        }
        return new HeaderElement[0];
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
