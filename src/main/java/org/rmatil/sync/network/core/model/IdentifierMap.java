package org.rmatil.sync.network.core.model;

import org.rmatil.sync.network.api.IIdentifierManager;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The backing up storage for a {@link IIdentifierManager}.
 * <p>
 * <i>Note</i>: There are most certainly better solution for doing this...
 *
 * @param <K> The type of the key
 * @param <V> The type of the value
 */
public class IdentifierMap<K, V> implements Serializable {

    /**
     * The storage mapping keys to values
     */
    protected Map<K, V> keyMap;

    /**
     * The storage mapping values to keys
     */
    protected Map<V, K> valueMap;

    public IdentifierMap() {
        this.keyMap = new HashMap<>();
        this.valueMap = new HashMap<>();
    }

    /**
     * Returns the storage mapping keys to values
     *
     * @return The key map
     */
    public Map<K, V> getKeyMap() {
        return keyMap;
    }

    /**
     * Returns the storage mapping values to keys
     *
     * @return The value map
     */
    public Map<V, K> getValueMap() {
        return valueMap;
    }
}
