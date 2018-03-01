package com.bugsnag.android;

import android.support.annotation.NonNull;
import com.bugsnag.android.JsonStream.Streamable;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MetaData extends Observable implements Streamable {
    private static final String FILTERED_PLACEHOLDER = "[FILTERED]";
    private static final String OBJECT_PLACEHOLDER = "[OBJECT]";
    private String[] filters;
    final Map<String, Object> store;

    public MetaData() {
        this.store = new ConcurrentHashMap();
    }

    public MetaData(Map<String, Object> m) {
        this.store = new ConcurrentHashMap(m);
    }

    public void toStream(@NonNull JsonStream writer) throws IOException {
        objectToStream(this.store, writer);
    }

    public void addToTab(String tabName, String key, Object value) {
        addToTab(tabName, key, value, true);
    }

    void addToTab(String tabName, String key, Object value, boolean notify) {
        Map<String, Object> tab = getTab(tabName);
        if (value != null) {
            tab.put(key, value);
        } else {
            tab.remove(key);
        }
        notifyBugsnagObservers(NotifyType.META);
    }

    public void clearTab(String tabName) {
        this.store.remove(tabName);
        notifyBugsnagObservers(NotifyType.META);
    }

    Map<String, Object> getTab(String tabName) {
        Map<String, Object> tab = (Map) this.store.get(tabName);
        if (tab != null) {
            return tab;
        }
        tab = new ConcurrentHashMap();
        this.store.put(tabName, tab);
        return tab;
    }

    void setFilters(String... filters) {
        this.filters = filters;
        notifyBugsnagObservers(NotifyType.FILTERS);
    }

    static MetaData merge(MetaData... metaDataList) {
        ArrayList<Map<String, Object>> stores = new ArrayList();
        List<String> filters = new ArrayList();
        for (MetaData metaData : metaDataList) {
            if (metaData != null) {
                stores.add(metaData.store);
                if (metaData.filters != null) {
                    filters.addAll(Arrays.asList(metaData.filters));
                }
            }
        }
        MetaData newMeta = new MetaData(mergeMaps((Map[]) stores.toArray(new Map[0])));
        newMeta.filters = (String[]) filters.toArray(new String[filters.size()]);
        return newMeta;
    }

    private static Map<String, Object> mergeMaps(Map<String, Object>... maps) {
        Map<String, Object> result = new ConcurrentHashMap();
        for (Map<String, Object> map : maps) {
            if (map != null) {
                Set<String> allKeys = new HashSet(result.keySet());
                allKeys.addAll(map.keySet());
                for (String key : allKeys) {
                    Object baseValue = result.get(key);
                    Object overridesValue = map.get(key);
                    if (overridesValue == null) {
                        result.put(key, baseValue);
                    } else if (baseValue != null && (baseValue instanceof Map) && (overridesValue instanceof Map)) {
                        result.put(key, mergeMaps((Map) baseValue, (Map) overridesValue));
                    } else {
                        result.put(key, overridesValue);
                    }
                }
            }
        }
        return result;
    }

    private void objectToStream(Object obj, JsonStream writer) throws IOException {
        if (obj == null) {
            writer.nullValue();
        } else if (obj instanceof String) {
            writer.value((String) obj);
        } else if (obj instanceof Number) {
            writer.value((Number) obj);
        } else if (obj instanceof Boolean) {
            writer.value((Boolean) obj);
        } else if (obj instanceof Map) {
            writer.beginObject();
            for (Entry entry : ((Map) obj).entrySet()) {
                String keyObj = entry.getKey();
                if (keyObj instanceof String) {
                    String key = keyObj;
                    writer.name(key);
                    if (shouldFilter(key)) {
                        writer.value(FILTERED_PLACEHOLDER);
                    } else {
                        objectToStream(entry.getValue(), writer);
                    }
                }
            }
            writer.endObject();
        } else if (obj instanceof Collection) {
            writer.beginArray();
            for (Object entry2 : (Collection) obj) {
                objectToStream(entry2, writer);
            }
            writer.endArray();
        } else if (obj.getClass().isArray()) {
            writer.beginArray();
            int length = Array.getLength(obj);
            for (int i = 0; i < length; i++) {
                objectToStream(Array.get(obj, i), writer);
            }
            writer.endArray();
        } else {
            writer.value(OBJECT_PLACEHOLDER);
        }
    }

    private boolean shouldFilter(String key) {
        if (this.filters == null || key == null) {
            return false;
        }
        for (String filter : this.filters) {
            if (key.contains(filter)) {
                return true;
            }
        }
        return false;
    }

    private void notifyBugsnagObservers(NotifyType type) {
        setChanged();
        super.notifyObservers(type.getValue());
    }
}
