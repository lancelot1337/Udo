package com.bugsnag.android;

import com.facebook.internal.ServerProtocol;
import com.ironsource.sdk.utils.Constants.RequestParameters;
import io.branch.referral.R;
import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.cocos2dx.lib.Cocos2dxEditBox;
import org.cocos2dx.lib.Cocos2dxHandler;

public class JsonWriter implements Closeable {
    private static final String[] HTML_SAFE_REPLACEMENT_CHARS = ((String[]) REPLACEMENT_CHARS.clone());
    private static final String[] REPLACEMENT_CHARS = new String[128];
    private String deferredName;
    private boolean htmlSafe;
    private String indent;
    private boolean lenient;
    private final Writer out;
    private String separator;
    private boolean serializeNulls;
    private final List<JsonScope> stack = new ArrayList();

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$bugsnag$android$JsonWriter$JsonScope = new int[JsonScope.values().length];

        static {
            try {
                $SwitchMap$com$bugsnag$android$JsonWriter$JsonScope[JsonScope.NONEMPTY_DOCUMENT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$bugsnag$android$JsonWriter$JsonScope[JsonScope.EMPTY_DOCUMENT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$bugsnag$android$JsonWriter$JsonScope[JsonScope.EMPTY_ARRAY.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$bugsnag$android$JsonWriter$JsonScope[JsonScope.NONEMPTY_ARRAY.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$bugsnag$android$JsonWriter$JsonScope[JsonScope.DANGLING_NAME.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    enum JsonScope {
        EMPTY_ARRAY,
        NONEMPTY_ARRAY,
        EMPTY_OBJECT,
        DANGLING_NAME,
        NONEMPTY_OBJECT,
        EMPTY_DOCUMENT,
        NONEMPTY_DOCUMENT,
        CLOSED
    }

    static {
        for (int i = 0; i <= 31; i++) {
            REPLACEMENT_CHARS[i] = String.format("\\u%04x", new Object[]{Integer.valueOf(i)});
        }
        REPLACEMENT_CHARS[34] = "\\\"";
        REPLACEMENT_CHARS[92] = "\\\\";
        REPLACEMENT_CHARS[9] = "\\t";
        REPLACEMENT_CHARS[8] = "\\b";
        REPLACEMENT_CHARS[10] = "\\n";
        REPLACEMENT_CHARS[13] = "\\r";
        REPLACEMENT_CHARS[12] = "\\f";
        HTML_SAFE_REPLACEMENT_CHARS[60] = "\\u003c";
        HTML_SAFE_REPLACEMENT_CHARS[62] = "\\u003e";
        HTML_SAFE_REPLACEMENT_CHARS[38] = "\\u0026";
        HTML_SAFE_REPLACEMENT_CHARS[61] = "\\u003d";
        HTML_SAFE_REPLACEMENT_CHARS[39] = "\\u0027";
    }

    public JsonWriter(Writer out) {
        this.stack.add(JsonScope.EMPTY_DOCUMENT);
        this.separator = ":";
        this.serializeNulls = true;
        if (out == null) {
            throw new NullPointerException("out == null");
        }
        this.out = out;
    }

    public final void setIndent(String indent) {
        if (indent.length() == 0) {
            this.indent = null;
            this.separator = ":";
            return;
        }
        this.indent = indent;
        this.separator = ": ";
    }

    public final void setLenient(boolean lenient) {
        this.lenient = lenient;
    }

    public boolean isLenient() {
        return this.lenient;
    }

    public final void setHtmlSafe(boolean htmlSafe) {
        this.htmlSafe = htmlSafe;
    }

    public final boolean isHtmlSafe() {
        return this.htmlSafe;
    }

    public final void setSerializeNulls(boolean serializeNulls) {
        this.serializeNulls = serializeNulls;
    }

    public final boolean getSerializeNulls() {
        return this.serializeNulls;
    }

    public JsonWriter beginArray() throws IOException {
        writeDeferredName();
        return open(JsonScope.EMPTY_ARRAY, RequestParameters.LEFT_BRACKETS);
    }

    public JsonWriter endArray() throws IOException {
        return close(JsonScope.EMPTY_ARRAY, JsonScope.NONEMPTY_ARRAY, RequestParameters.RIGHT_BRACKETS);
    }

    public JsonWriter beginObject() throws IOException {
        writeDeferredName();
        return open(JsonScope.EMPTY_OBJECT, "{");
    }

    public JsonWriter endObject() throws IOException {
        return close(JsonScope.EMPTY_OBJECT, JsonScope.NONEMPTY_OBJECT, "}");
    }

    private JsonWriter open(JsonScope empty, String openBracket) throws IOException {
        beforeValue(true);
        this.stack.add(empty);
        this.out.write(openBracket);
        return this;
    }

    private JsonWriter close(JsonScope empty, JsonScope nonempty, String closeBracket) throws IOException {
        JsonScope context = peek();
        if (context != nonempty && context != empty) {
            throw new IllegalStateException("Nesting problem: " + this.stack);
        } else if (this.deferredName != null) {
            throw new IllegalStateException("Dangling name: " + this.deferredName);
        } else {
            this.stack.remove(this.stack.size() - 1);
            if (context == nonempty) {
                newline();
            }
            this.out.write(closeBracket);
            return this;
        }
    }

    private JsonScope peek() {
        int size = this.stack.size();
        if (size != 0) {
            return (JsonScope) this.stack.get(size - 1);
        }
        throw new IllegalStateException("JsonWriter is closed.");
    }

    private void replaceTop(JsonScope topOfStack) {
        this.stack.set(this.stack.size() - 1, topOfStack);
    }

    public JsonWriter name(String name) throws IOException {
        if (name == null) {
            throw new NullPointerException("name == null");
        } else if (this.deferredName != null) {
            throw new IllegalStateException();
        } else if (this.stack.isEmpty()) {
            throw new IllegalStateException("JsonWriter is closed.");
        } else {
            this.deferredName = name;
            return this;
        }
    }

    private void writeDeferredName() throws IOException {
        if (this.deferredName != null) {
            beforeName();
            string(this.deferredName);
            this.deferredName = null;
        }
    }

    public JsonWriter value(String value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        writeDeferredName();
        beforeValue(false);
        string(value);
        return this;
    }

    public JsonWriter nullValue() throws IOException {
        if (this.deferredName != null) {
            if (this.serializeNulls) {
                writeDeferredName();
            } else {
                this.deferredName = null;
                return this;
            }
        }
        beforeValue(false);
        this.out.write("null");
        return this;
    }

    public JsonWriter value(boolean value) throws IOException {
        writeDeferredName();
        beforeValue(false);
        this.out.write(value ? ServerProtocol.DIALOG_RETURN_SCOPES_TRUE : "false");
        return this;
    }

    public JsonWriter value(Boolean value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        writeDeferredName();
        beforeValue(false);
        this.out.write(value.booleanValue() ? ServerProtocol.DIALOG_RETURN_SCOPES_TRUE : "false");
        return this;
    }

    public JsonWriter value(double value) throws IOException {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
        }
        writeDeferredName();
        beforeValue(false);
        this.out.append(Double.toString(value));
        return this;
    }

    public JsonWriter value(long value) throws IOException {
        writeDeferredName();
        beforeValue(false);
        this.out.write(Long.toString(value));
        return this;
    }

    public JsonWriter value(Number value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        writeDeferredName();
        String string = value.toString();
        if (this.lenient || !(string.equals("-Infinity") || string.equals("Infinity") || string.equals("NaN"))) {
            beforeValue(false);
            this.out.append(string);
            return this;
        }
        throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
    }

    public void flush() throws IOException {
        if (this.stack.isEmpty()) {
            throw new IllegalStateException("JsonWriter is closed.");
        }
        this.out.flush();
    }

    public void close() throws IOException {
        this.out.close();
        int size = this.stack.size();
        if (size > 1 || (size == 1 && this.stack.get(size - 1) != JsonScope.NONEMPTY_DOCUMENT)) {
            throw new IOException("Incomplete document");
        }
        this.stack.clear();
    }

    private void string(String value) throws IOException {
        String[] replacements = this.htmlSafe ? HTML_SAFE_REPLACEMENT_CHARS : REPLACEMENT_CHARS;
        this.out.write("\"");
        int last = 0;
        int length = value.length();
        for (int i = 0; i < length; i++) {
            char c = value.charAt(i);
            String replacement;
            if (c < '\u0080') {
                replacement = replacements[c];
                if (replacement == null) {
                }
                if (last < i) {
                    this.out.write(value, last, i - last);
                }
                this.out.write(replacement);
                last = i + 1;
            } else {
                if (c == '\u2028') {
                    replacement = "\\u2028";
                } else if (c == '\u2029') {
                    replacement = "\\u2029";
                }
                if (last < i) {
                    this.out.write(value, last, i - last);
                }
                this.out.write(replacement);
                last = i + 1;
            }
        }
        if (last < length) {
            this.out.write(value, last, length - last);
        }
        this.out.write("\"");
    }

    private void newline() throws IOException {
        if (this.indent != null) {
            this.out.write("\n");
            for (int i = 1; i < this.stack.size(); i++) {
                this.out.write(this.indent);
            }
        }
    }

    private void beforeName() throws IOException {
        JsonScope context = peek();
        if (context == JsonScope.NONEMPTY_OBJECT) {
            this.out.write(44);
        } else if (context != JsonScope.EMPTY_OBJECT) {
            throw new IllegalStateException("Nesting problem: " + this.stack);
        }
        newline();
        replaceTop(JsonScope.DANGLING_NAME);
    }

    private void beforeValue(boolean root) throws IOException {
        switch (AnonymousClass1.$SwitchMap$com$bugsnag$android$JsonWriter$JsonScope[peek().ordinal()]) {
            case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                if (!this.lenient) {
                    throw new IllegalStateException("JSON must have only one top-level value.");
                }
                break;
            case R.styleable.View_paddingStart /*2*/:
                break;
            case Cocos2dxEditBox.kEndActionReturn /*3*/:
                replaceTop(JsonScope.NONEMPTY_ARRAY);
                newline();
                return;
            case R.styleable.View_theme /*4*/:
                this.out.append(',');
                newline();
                return;
            case R.styleable.Toolbar_contentInsetStart /*5*/:
                this.out.append(this.separator);
                replaceTop(JsonScope.NONEMPTY_OBJECT);
                return;
            default:
                throw new IllegalStateException("Nesting problem: " + this.stack);
        }
        if (this.lenient || root) {
            replaceTop(JsonScope.NONEMPTY_DOCUMENT);
            return;
        }
        throw new IllegalStateException("JSON must start with an array or an object.");
    }
}
