package com.devtype.training.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BEFORE_COMMIT bosqichida boshqa modullar (gamification, coach)
 * hisoblagan mukofotlar va maslahatlarni jamlash uchun kollektor.
 */
public class SessionOutcomeCollector {

    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    public void set(String key, Object value) {
        if (value != null) {
            attributes.put(key, value);
        }
    }

    public Object get(String key) {
        return attributes.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object val = attributes.get(key);
        return clazz.isInstance(val) ? (T) val : null;
    }

    public Map<String, Object> getAll() {
        return Map.copyOf(attributes);
    }
}
