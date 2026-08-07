package io.github.mattiacozzolino.cvascode.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MapValues {
    private MapValues() {
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> map(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value instanceof Map<?, ?> raw) {
            Map<String, Object> result = new LinkedHashMap<>();
            raw.forEach((k, v) -> result.put(String.valueOf(k), v));
            return result;
        }
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> list(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value instanceof List<?> raw) {
            return (List<Object>) raw;
        }
        return List.of();
    }

    public static String string(Map<String, Object> source, String key) {
        Object value = source.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    public static boolean bool(Map<String, Object> source, String key, boolean fallback) {
        Object value = source.get(key);
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof String s) {
            return Boolean.parseBoolean(s);
        }
        return fallback;
    }

    public static int integer(Map<String, Object> source, String key, int fallback) {
        Object value = source.get(key);
        if (value instanceof Number n) {
            return n.intValue();
        }
        if (value instanceof String s && !s.isBlank()) {
            return Integer.parseInt(s);
        }
        return fallback;
    }
}
