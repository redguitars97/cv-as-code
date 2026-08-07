package io.github.mattiacozzolino.cvascode.util;

import io.github.mattiacozzolino.cvascode.domain.DocumentKind;
import io.github.mattiacozzolino.cvascode.domain.Language;

import java.text.Normalizer;
import java.util.Map;

public final class FileNames {
    private FileNames() {
    }

    public static String pdf(DocumentKind kind, Language language, Map<String, Object> model) {
        Map<String, Object> candidate = MapValues.map(model, "candidate");
        Map<String, Object> personal = MapValues.map(candidate, "personal");
        Map<String, Object> settings = MapValues.map(model, "settings");
        Map<String, Object> output = MapValues.map(settings, "output");
        String pattern = MapValues.string(output, "fileNamePattern");
        if (pattern.isBlank()) {
            pattern = "{firstName}_{lastName}_{documentType}_{language}";
        }
        return pattern
                .replace("{firstName}", sanitize(MapValues.string(personal, "firstName")))
                .replace("{lastName}", sanitize(MapValues.string(personal, "lastName")))
                .replace("{documentType}", kind.fileSegment())
                .replace("{language}", language.name())
                + ".pdf";
    }

    public static String report(DocumentKind kind, Language language) {
        return kind.cliName() + "-" + language.tag() + "-report.json";
    }

    public static String html(DocumentKind kind, Language language) {
        return kind.cliName() + "-" + language.tag() + ".html";
    }

    public static String sanitize(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized
                .replace("'", "")
                .replaceAll("[^A-Za-z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }
}
