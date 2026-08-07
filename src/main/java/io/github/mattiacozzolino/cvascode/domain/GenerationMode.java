package io.github.mattiacozzolino.cvascode.domain;

public enum GenerationMode {
    DEMO,
    PRODUCTION;

    public static GenerationMode fromCliName(String value) {
        if (value == null || value.isBlank()) {
            return DEMO;
        }
        return GenerationMode.valueOf(value.trim().toUpperCase());
    }
}
