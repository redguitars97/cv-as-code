package io.github.mattiacozzolino.cvascode.domain;

public enum Language {
    IT,
    EN;

    public static Language fromCliName(String value) {
        return Language.valueOf(value.trim().toUpperCase());
    }

    public String tag() {
        return name().toLowerCase();
    }
}
