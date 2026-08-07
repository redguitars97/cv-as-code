package io.github.mattiacozzolino.cvascode.util;

import io.github.mattiacozzolino.cvascode.domain.Language;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class LocalizedValues {
    private LocalizedValues() {
    }

    public static String text(Object value, Language language) {
        if (value == null) {
            return "";
        }
        if (value instanceof Map<?, ?> map) {
            Object localized = map.get(language.tag());
            return localized == null ? "" : String.valueOf(localized);
        }
        return String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> localizeMap(Map<String, Object> source, Language language) {
        Map<String, Object> localized = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            localized.put(entry.getKey(), localizeValue(entry.getValue(), language));
        }
        return localized;
    }

    @SuppressWarnings("unchecked")
    public static Object localizeValue(Object value, Language language) {
        if (value instanceof Map<?, ?> raw) {
            Map<String, Object> map = (Map<String, Object>) raw;
            if (map.containsKey("it") || map.containsKey("en")) {
                return text(map, language);
            }
            return localizeMap(map, language);
        }
        if (value instanceof List<?> list) {
            List<Object> localized = new ArrayList<>();
            for (Object item : list) {
                localized.add(localizeValue(item, language));
            }
            return localized;
        }
        return value;
    }
}
