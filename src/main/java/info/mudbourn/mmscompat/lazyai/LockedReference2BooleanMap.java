package info.mudbourn.mmscompat.lazyai;

import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;

/**
 * Reference2BooleanOpenHashMap whose accessors lock on the map itself.
 */
public class LockedReference2BooleanMap<K> extends Reference2BooleanOpenHashMap<K> {

    @Override
    public synchronized boolean put(K key, boolean value) {
        return super.put(key, value);
    }

    @Override
    public synchronized boolean getBoolean(Object key) {
        return super.getBoolean(key);
    }

    @Override
    public synchronized boolean containsKey(Object key) {
        return super.containsKey(key);
    }

    @Override
    public synchronized boolean removeBoolean(Object key) {
        return super.removeBoolean(key);
    }

    @Override
    public synchronized void clear() {
        super.clear();
    }
}
