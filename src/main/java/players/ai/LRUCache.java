package players.ai;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Extension of LinkedHashMap, which keeps elements in access order. Elements
 * are removed after the capacity is exceeded
 * @param <K> Key type
 * @param <V> Value type
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int maxEntries;

    public LRUCache(final int maxEntries) {
        super(maxEntries + 1, 1.0f, true);
        this.maxEntries = maxEntries;
    }

    /**
     * Removes the least used entry after the capacity is exceeded
     * @param eldest Eldest entry
     * @return
     */
    @Override
    protected boolean removeEldestEntry(final Map.Entry<K, V> eldest) {
        return super.size() > maxEntries;
    }
}