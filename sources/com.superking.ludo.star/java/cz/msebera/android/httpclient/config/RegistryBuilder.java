package cz.msebera.android.httpclient.config;

import cz.msebera.android.httpclient.annotation.NotThreadSafe;
import cz.msebera.android.httpclient.util.Args;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@NotThreadSafe
public final class RegistryBuilder<I> {
    private final Map<String, I> items = new HashMap();

    public static <I> RegistryBuilder<I> create() {
        return new RegistryBuilder();
    }

    RegistryBuilder() {
    }

    public RegistryBuilder<I> register(String id, I item) {
        Args.notEmpty((CharSequence) id, "ID");
        Args.notNull(item, "Item");
        this.items.put(id.toLowerCase(Locale.ROOT), item);
        return this;
    }

    public Registry<I> build() {
        return new Registry(this.items);
    }

    public String toString() {
        return this.items.toString();
    }
}
