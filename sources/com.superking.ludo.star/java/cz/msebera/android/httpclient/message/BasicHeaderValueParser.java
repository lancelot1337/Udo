package cz.msebera.android.httpclient.message;

import cz.msebera.android.httpclient.HeaderElement;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.ParseException;
import cz.msebera.android.httpclient.annotation.Immutable;
import cz.msebera.android.httpclient.util.Args;
import cz.msebera.android.httpclient.util.CharArrayBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

@Immutable
public class BasicHeaderValueParser implements HeaderValueParser {
    @Deprecated
    public static final BasicHeaderValueParser DEFAULT = new BasicHeaderValueParser();
    private static final char ELEM_DELIMITER = ',';
    public static final BasicHeaderValueParser INSTANCE = new BasicHeaderValueParser();
    private static final char PARAM_DELIMITER = ';';
    private static final BitSet TOKEN_DELIMS = TokenParser.INIT_BITSET(61, 59, 44);
    private static final BitSet VALUE_DELIMS = TokenParser.INIT_BITSET(59, 44);
    private final TokenParser tokenParser = TokenParser.INSTANCE;

    public static HeaderElement[] parseElements(String value, HeaderValueParser parser) throws ParseException {
        Args.notNull(value, "Value");
        CharArrayBuffer buffer = new CharArrayBuffer(value.length());
        buffer.append(value);
        ParserCursor cursor = new ParserCursor(0, value.length());
        if (parser == null) {
            parser = INSTANCE;
        }
        return parser.parseElements(buffer, cursor);
    }

    public HeaderElement[] parseElements(CharArrayBuffer buffer, ParserCursor cursor) {
        Args.notNull(buffer, "Char array buffer");
        Args.notNull(cursor, "Parser cursor");
        List<HeaderElement> elements = new ArrayList();
        while (!cursor.atEnd()) {
            HeaderElement element = parseHeaderElement(buffer, cursor);
            if (element.getName().length() != 0 || element.getValue() != null) {
                elements.add(element);
            }
        }
        return (HeaderElement[]) elements.toArray(new HeaderElement[elements.size()]);
    }

    public static HeaderElement parseHeaderElement(String value, HeaderValueParser parser) throws ParseException {
        Args.notNull(value, "Value");
        CharArrayBuffer buffer = new CharArrayBuffer(value.length());
        buffer.append(value);
        ParserCursor cursor = new ParserCursor(0, value.length());
        if (parser == null) {
            parser = INSTANCE;
        }
        return parser.parseHeaderElement(buffer, cursor);
    }

    public HeaderElement parseHeaderElement(CharArrayBuffer buffer, ParserCursor cursor) {
        Args.notNull(buffer, "Char array buffer");
        Args.notNull(cursor, "Parser cursor");
        NameValuePair nvp = parseNameValuePair(buffer, cursor);
        NameValuePair[] params = null;
        if (!(cursor.atEnd() || buffer.charAt(cursor.getPos() - 1) == ELEM_DELIMITER)) {
            params = parseParameters(buffer, cursor);
        }
        return createHeaderElement(nvp.getName(), nvp.getValue(), params);
    }

    protected HeaderElement createHeaderElement(String name, String value, NameValuePair[] params) {
        return new BasicHeaderElement(name, value, params);
    }

    public static NameValuePair[] parseParameters(String value, HeaderValueParser parser) throws ParseException {
        Args.notNull(value, "Value");
        CharArrayBuffer buffer = new CharArrayBuffer(value.length());
        buffer.append(value);
        ParserCursor cursor = new ParserCursor(0, value.length());
        if (parser == null) {
            parser = INSTANCE;
        }
        return parser.parseParameters(buffer, cursor);
    }

    public NameValuePair[] parseParameters(CharArrayBuffer buffer, ParserCursor cursor) {
        Args.notNull(buffer, "Char array buffer");
        Args.notNull(cursor, "Parser cursor");
        this.tokenParser.skipWhiteSpace(buffer, cursor);
        List<NameValuePair> params = new ArrayList();
        while (!cursor.atEnd()) {
            params.add(parseNameValuePair(buffer, cursor));
            if (buffer.charAt(cursor.getPos() - 1) == ELEM_DELIMITER) {
                break;
            }
        }
        return (NameValuePair[]) params.toArray(new NameValuePair[params.size()]);
    }

    public static NameValuePair parseNameValuePair(String value, HeaderValueParser parser) throws ParseException {
        Args.notNull(value, "Value");
        CharArrayBuffer buffer = new CharArrayBuffer(value.length());
        buffer.append(value);
        ParserCursor cursor = new ParserCursor(0, value.length());
        if (parser == null) {
            parser = INSTANCE;
        }
        return parser.parseNameValuePair(buffer, cursor);
    }

    public NameValuePair parseNameValuePair(CharArrayBuffer buffer, ParserCursor cursor) {
        Args.notNull(buffer, "Char array buffer");
        Args.notNull(cursor, "Parser cursor");
        String name = this.tokenParser.parseToken(buffer, cursor, TOKEN_DELIMS);
        if (cursor.atEnd()) {
            return new BasicNameValuePair(name, null);
        }
        int delim = buffer.charAt(cursor.getPos());
        cursor.updatePos(cursor.getPos() + 1);
        if (delim != 61) {
            return createNameValuePair(name, null);
        }
        String value = this.tokenParser.parseValue(buffer, cursor, VALUE_DELIMS);
        if (!cursor.atEnd()) {
            cursor.updatePos(cursor.getPos() + 1);
        }
        return createNameValuePair(name, value);
    }

    @Deprecated
    public NameValuePair parseNameValuePair(CharArrayBuffer buffer, ParserCursor cursor, char[] delimiters) {
        Args.notNull(buffer, "Char array buffer");
        Args.notNull(cursor, "Parser cursor");
        BitSet delimSet = new BitSet();
        if (delimiters != null) {
            for (char delimiter : delimiters) {
                delimSet.set(delimiter);
            }
        }
        delimSet.set(61);
        String name = this.tokenParser.parseToken(buffer, cursor, delimSet);
        if (cursor.atEnd()) {
            return new BasicNameValuePair(name, null);
        }
        int delim = buffer.charAt(cursor.getPos());
        cursor.updatePos(cursor.getPos() + 1);
        if (delim != 61) {
            return createNameValuePair(name, null);
        }
        delimSet.clear(61);
        String value = this.tokenParser.parseValue(buffer, cursor, delimSet);
        if (!cursor.atEnd()) {
            cursor.updatePos(cursor.getPos() + 1);
        }
        return createNameValuePair(name, value);
    }

    protected NameValuePair createNameValuePair(String name, String value) {
        return new BasicNameValuePair(name, value);
    }
}
