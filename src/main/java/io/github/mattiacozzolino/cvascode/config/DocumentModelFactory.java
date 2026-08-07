package io.github.mattiacozzolino.cvascode.config;

import io.github.mattiacozzolino.cvascode.domain.DocumentKind;
import io.github.mattiacozzolino.cvascode.domain.GenerationMode;
import io.github.mattiacozzolino.cvascode.domain.Language;
import io.github.mattiacozzolino.cvascode.domain.PhotoMode;
import io.github.mattiacozzolino.cvascode.util.LocalizedValues;
import io.github.mattiacozzolino.cvascode.util.MapValues;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DocumentModelFactory {
    private static final String DEFAULT_PHOTO_PATH = "../assets/photo-placeholder.png";

    private final YamlRepository repository;

    public DocumentModelFactory(YamlRepository repository) {
        this.repository = repository;
    }

    public Map<String, Object> resume(Language language, Path privateConfig) {
        return resume(language, privateConfig, GenerationMode.DEMO, PhotoMode.CONFIG);
    }

    public Map<String, Object> resume(Language language, Path privateConfig, GenerationMode mode, PhotoMode photoMode) {
        Map<String, Object> model = base(DocumentKind.RESUME, language, privateConfig);
        model.put("resume", localizedResume(language));
        model.put("includePhoto", includePhoto(language, photoMode));
        model.put("photoUri", photoUri(model));
        model.put("generationMode", mode.name());
        return model;
    }

    public Map<String, Object> coverLetter(Language language, Path privateConfig, Path applicationConfig) {
        return coverLetter(language, privateConfig, applicationConfig, GenerationMode.DEMO);
    }

    public Map<String, Object> coverLetter(Language language, Path privateConfig, Path applicationConfig, GenerationMode mode) {
        Map<String, Object> model = base(DocumentKind.COVER_LETTER, language, privateConfig);
        model.put("resume", localizedResume(language));
        model.put("cover", localizedCoverLetter(language, applicationConfig));
        model.put("application", localizedApplication(language, applicationConfig));
        model.put("today", naturalDate(LocalDate.now(), language));
        model.put("includePhoto", false);
        model.put("photoUri", "");
        model.put("generationMode", mode.name());
        return model;
    }

    private Map<String, Object> base(DocumentKind kind, Language language, Path privateConfig) {
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("documentKind", kind.name());
        model.put("language", language.name());
        model.put("languageTag", language.tag());
        model.put("configDirectory", repository.configDirectory());
        model.put("settings", repository.settings());
        model.put("candidate", localizedCandidate(language));
        model.put("labels", labels(language));
        Map<String, Object> privateValues = repository.privateConfig(privateConfig);
        model.put("private", privateValues);
        model.put("signature", signature(language, privateValues));
        model.put("privacy", privacy(language));
        return model;
    }

    private Map<String, Object> localizedCandidate(Language language) {
        Map<String, Object> raw = repository.candidate();
        Map<String, Object> candidate = MapValues.map(raw, "candidate").isEmpty() ? raw : MapValues.map(raw, "candidate");
        Map<String, Object> localized = new LinkedHashMap<>();
        Map<String, Object> personal = new LinkedHashMap<>();
        personal.put("firstName", MapValues.string(candidate, "firstName"));
        personal.put("lastName", MapValues.string(candidate, "lastName"));
        personal.put("professionalTitle", LocalizedValues.text(candidate.get("professionalTitle"), language));
        personal.put("positioning", LocalizedValues.text(candidate.get("positioning"), language));
        personal.put("location", LocalizedValues.text(candidate.get("location"), language));
        localized.put("personal", personal);
        return localized;
    }

    private Map<String, Object> localizedResume(Language language) {
        Map<String, Object> raw = repository.resume();
        Map<String, Object> resume = MapValues.map(raw, "resume").isEmpty() ? raw : MapValues.map(raw, "resume");
        Map<String, Object> localized = LocalizedValues.localizeMap(resume, language);
        localized.put("experience", experienceWithDisplayPeriods(MapValues.list(localized, "experiences"), language));
        localized.put("skills", localized.getOrDefault("expertise", List.of()));
        localized.put("additionalTraining", visibleAdditionalTraining(MapValues.list(localized, "additionalTraining")));
        return localized;
    }

    private List<Object> visibleAdditionalTraining(List<Object> courses) {
        List<Object> visible = new ArrayList<>();
        for (Object item : courses) {
            if (item instanceof Map<?, ?> raw) {
                Map<String, Object> course = (Map<String, Object>) raw;
                if (MapValues.bool(course, "visibleByDefault", false)) {
                    visible.add(course);
                }
            }
        }
        return visible;
    }

    private List<Object> experienceWithDisplayPeriods(List<Object> experiences, Language language) {
        List<Object> localized = new ArrayList<>();
        for (Object item : experiences) {
            if (item instanceof Map<?, ?> raw) {
                Map<String, Object> experience = new LinkedHashMap<>((Map<String, Object>) raw);
                experience.put("period", period(
                        String.valueOf(experience.getOrDefault("startDate", "")),
                        experience.get("endDate") == null ? "" : String.valueOf(experience.get("endDate")),
                        language
                ));
                localized.add(experience);
            }
        }
        return localized;
    }

    private String period(String startDate, String endDate, Language language) {
        if (startDate.isBlank()) {
            return "";
        }
        String start = monthYear(startDate, language);
        String end = endDate.isBlank() || "null".equals(endDate) ? (language == Language.IT ? "presente" : "present") : monthYear(endDate, language);
        return start + " / " + end;
    }

    private String monthYear(String value, Language language) {
        if (!value.matches("\\d{4}-\\d{2}")) {
            return value;
        }
        String[] parts = value.split("-");
        int month = Integer.parseInt(parts[1]);
        String[] it = {"gennaio", "febbraio", "marzo", "aprile", "maggio", "giugno", "luglio", "agosto", "settembre", "ottobre", "novembre", "dicembre"};
        String[] en = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        return (language == Language.IT ? it[month - 1] : en[month - 1]) + " " + parts[0];
    }

    private String naturalDate(LocalDate date, Language language) {
        String month = monthYear("%04d-%02d".formatted(date.getYear(), date.getMonthValue()), language)
                .replace(" " + date.getYear(), "");
        return language == Language.IT
                ? date.getDayOfMonth() + " " + month + " " + date.getYear()
                : month + " " + date.getDayOfMonth() + ", " + date.getYear();
    }

    private Map<String, Object> localizedCoverLetter(Language language, Path applicationConfig) {
        Map<String, Object> raw = repository.coverLetter();
        Map<String, Object> coverLetter = MapValues.map(raw, "coverLetter");
        Map<String, Object> generic = MapValues.map(coverLetter, "generic");
        if (generic.isEmpty()) {
            generic = MapValues.map(coverLetter, "default");
        }
        Map<String, Object> localized = LocalizedValues.localizeMap(generic, language);
        String applicationId = applicationId(applicationConfig);
        Map<String, Object> application = findApplication(coverLetter, applicationId);
        if (!application.isEmpty()) {
            localized.put("selectedApplication", LocalizedValues.localizeMap(application, language));
        }
        return localized;
    }

    private Map<String, Object> localizedApplication(Language language, Path applicationConfig) {
        Map<String, Object> raw = repository.application(applicationConfig);
        if (!raw.isEmpty()) {
            Map<String, Object> application = new LinkedHashMap<>(LocalizedValues.localizeMap(MapValues.map(raw, "application"), language));
            application.putAll(LocalizedValues.localizeMap(MapValues.map(raw, "motivation"), language));
            return application;
        }
        Map<String, Object> cover = repository.coverLetter();
        Map<String, Object> application = findApplication(MapValues.map(cover, "coverLetter"), applicationId(applicationConfig));
        return application.isEmpty() ? new LinkedHashMap<>() : LocalizedValues.localizeMap(application, language);
    }

    private String applicationId(Path applicationConfig) {
        if (applicationConfig == null) {
            return "";
        }
        Map<String, Object> application = repository.application(applicationConfig);
        return MapValues.string(MapValues.map(application, "application"), "id");
    }

    private Map<String, Object> findApplication(Map<String, Object> coverLetter, String applicationId) {
        if (applicationId.isBlank()) {
            return new LinkedHashMap<>();
        }
        for (Object item : MapValues.list(coverLetter, "applications")) {
            if (item instanceof Map<?, ?> raw) {
                Map<String, Object> app = (Map<String, Object>) raw;
                if (applicationId.equals(MapValues.string(app, "id"))) {
                    return app;
                }
            }
        }
        return new LinkedHashMap<>();
    }

    private Map<String, Object> labels(Language language) {
        Map<String, Object> labels = new LinkedHashMap<>();
        labels.put("document", Map.of(
                "cvTitle", language == Language.IT ? "Curriculum Vitae" : "Curriculum Vitae",
                "coverLetterTitle", language == Language.IT ? "Lettera di presentazione" : "Cover Letter"
        ));
        labels.put("labels", Map.of("demo", language == Language.IT ? "Documento demo" : "Demo document"));
        labels.put("sections", Map.of(
                "summary", language == Language.IT ? "Profilo professionale" : "Professional Profile",
                "expertise", language == Language.IT ? "Competenze chiave" : "Core Expertise",
                "experience", language == Language.IT ? "Esperienza professionale" : "Professional Experience",
                "projects", language == Language.IT ? "Progetti" : "Projects",
                "certifications", language == Language.IT ? "Certificazioni" : "Certifications",
                "education", language == Language.IT ? "Formazione" : "Education",
                "languages", language == Language.IT ? "Lingue" : "Languages",
                "additionalTraining", language == Language.IT ? "Formazione aggiuntiva" : "Additional Training"
        ));
        labels.put("cover", Map.of(
                "subject", language == Language.IT ? "Oggetto" : "Subject",
                "genericGreeting", language == Language.IT ? "Gentile Team di selezione," : "Dear Hiring Team,",
                "greeting", language == Language.IT ? "Gentile" : "Dear",
                "closing", language == Language.IT ? "Cordiali saluti," : "Sincerely,"
        ));
        return labels;
    }

    private Map<String, Object> signature(Language language, Map<String, Object> privateValues) {
        Map<String, Object> signature = new LinkedHashMap<>(MapValues.map(repository.settings(), "signature"));
        signature.putAll(MapValues.map(privateValues, "signature"));
        boolean enabled = MapValues.bool(signature, "enabled", false);
        String name = LocalizedValues.text(signature.get("name"), language);
        if (name.isBlank()) {
            Map<String, Object> candidate = localizedCandidate(language);
            Map<String, Object> personal = MapValues.map(candidate, "personal");
            name = MapValues.string(personal, "firstName") + " " + MapValues.string(personal, "lastName");
        }
        String imagePath = MapValues.string(signature, "imagePath");
        signature.put("enabled", enabled);
        signature.put("name", name.trim());
        signature.put("imageUri", assetUri(imagePath, ""));
        return signature;
    }

    private Map<String, Object> privacy(Language language) {
        Map<String, Object> privacy = new LinkedHashMap<>(MapValues.map(repository.settings(), "privacy"));
        Map<String, Object> enabled = MapValues.map(privacy, "enabled");
        boolean defaultEnabled = language == Language.IT;
        boolean isEnabled = MapValues.bool(enabled, language.tag(), defaultEnabled);
        String text = LocalizedValues.text(privacy.get("text"), language);
        privacy.put("enabled", isEnabled);
        privacy.put("text", text);
        return privacy;
    }

    private boolean includePhoto(Language language, PhotoMode photoMode) {
        if (photoMode == PhotoMode.INCLUDE) {
            return true;
        }
        if (photoMode == PhotoMode.EXCLUDE) {
            return false;
        }
        Map<String, Object> settings = repository.settings();
        Map<String, Object> document = MapValues.map(settings, "document");
        Map<String, Object> cv = MapValues.map(document, "resume");
        Map<String, Object> includePhoto = MapValues.map(cv, "includePhoto");
        return MapValues.bool(includePhoto, language.tag(), language == Language.IT);
    }

    private String photoUri(Map<String, Object> model) {
        Map<String, Object> privatePhoto = MapValues.map(MapValues.map(model, "private"), "photo");
        String privatePath = MapValues.string(privatePhoto, "path");
        return assetUri(privatePath, DEFAULT_PHOTO_PATH);
    }

    private String assetUri(String path, String fallbackPath) {
        Path assetPath = resolveAsset(path);
        if ((assetPath == null || !java.nio.file.Files.exists(assetPath)) && !fallbackPath.isBlank()) {
            assetPath = resolveAsset(fallbackPath);
        }
        if (assetPath == null || !java.nio.file.Files.exists(assetPath)) {
            return "";
        }
        try {
            String mime = mimeType(assetPath);
            String encoded = Base64.getEncoder().encodeToString(java.nio.file.Files.readAllBytes(assetPath));
            return "data:" + mime + ";base64," + encoded;
        } catch (java.io.IOException e) {
            return "";
        }
    }

    private String mimeType(Path assetPath) {
        String path = assetPath.toString().toLowerCase();
        if (path.endsWith(".png")) {
            return "image/png";
        }
        if (path.endsWith(".svg")) {
            return "image/svg+xml";
        }
        return "image/jpeg";
    }

    private Path resolveAsset(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        Path photoPath = Path.of(path);
        if (photoPath.isAbsolute()) {
            return null;
        }
        if (!photoPath.isAbsolute()) {
            photoPath = repository.configDirectory().resolve(path).normalize();
        }
        return photoPath;
    }
}
