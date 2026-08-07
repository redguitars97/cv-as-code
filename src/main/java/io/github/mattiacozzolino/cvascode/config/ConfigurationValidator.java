package io.github.mattiacozzolino.cvascode.config;

import io.github.mattiacozzolino.cvascode.domain.DocumentKind;
import io.github.mattiacozzolino.cvascode.domain.GenerationMode;
import io.github.mattiacozzolino.cvascode.domain.Language;
import io.github.mattiacozzolino.cvascode.util.MapValues;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class ConfigurationValidator {
    private static final List<String> PLACEHOLDERS = List.of(
            "example.com",
            "linkedin.com/in/example",
            "github.com/example",
            "+39 000 000 0000",
            "TODO",
            "CHANGE_ME"
    );
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern YEAR_MONTH = Pattern.compile("^\\d{4}-\\d{2}$");

    public void validateModel(Map<String, Object> model, DocumentKind kind, Language language, GenerationMode mode) {
        List<String> errors = new ArrayList<>();
        Map<String, Object> candidate = MapValues.map(model, "candidate");
        Map<String, Object> personal = MapValues.map(candidate, "personal");
        Map<String, Object> resume = MapValues.map(model, "resume");
        Map<String, Object> contacts = MapValues.map(MapValues.map(model, "private"), "contact");

        required(errors, "config/candidate.yaml", "personal.firstName", personal.get("firstName"));
        required(errors, "config/candidate.yaml", "personal.lastName", personal.get("lastName"));
        required(errors, "config/candidate.yaml", "personal.professionalTitle", personal.get("professionalTitle"));
        required(errors, "config/candidate.yaml", "personal.location", personal.get("location"));
        required(errors, "config/private.yaml", "contact.email", contacts.get("email"));
        required(errors, "config/private.yaml", "contact.linkedin", contacts.get("linkedin"));
        required(errors, "config/private.yaml", "contact.github", contacts.get("github"));
        validateEmail(errors, contacts.get("email"));
        validateUrl(errors, "contact.linkedin", contacts.get("linkedin"), "linkedin.com");
        validateUrl(errors, "contact.github", contacts.get("github"), "github.com");

        if (kind == DocumentKind.RESUME) {
            required(errors, "config/resume.yaml", "summary." + language.tag(), resume.get("summary"));
            validateResume(errors, resume);
        } else {
            validateCoverLetter(errors, MapValues.map(model, "cover"), MapValues.map(model, "application"));
        }

        if (mode == GenerationMode.PRODUCTION) {
            rejectPlaceholder(errors, "config/candidate.yaml", "candidate.firstName", personal.get("firstName"));
            rejectPlaceholder(errors, "config/candidate.yaml", "candidate.lastName", personal.get("lastName"));
            rejectPlaceholder(errors, "config/private.yaml", "contact.email", contacts.get("email"));
            rejectPlaceholder(errors, "config/private.yaml", "contact.phone", contacts.get("phone"));
            rejectPlaceholder(errors, "config/private.yaml", "contact.linkedin", contacts.get("linkedin"));
            rejectPlaceholder(errors, "config/private.yaml", "contact.github", contacts.get("github"));
        }

        validatePhoto(errors, model);
        validatePrivacy(errors, model, language);

        if (!errors.isEmpty()) {
            throw new ConfigurationException("Configuration validation failed:\n- " + String.join("\n- ", errors));
        }
    }

    private void validateResume(List<String> errors, Map<String, Object> resume) {
        List<Object> experiences = MapValues.list(resume, "experience");
        if (experiences.isEmpty()) {
            errors.add("config/resume.yaml -> experiences: add at least one experience item.");
        }
        if (experiences.size() < 2) {
            errors.add("config/resume.yaml -> experiences: add at least two experiences for a production-ready CV.");
        }
        for (int i = 0; i < experiences.size(); i++) {
            if (experiences.get(i) instanceof Map<?, ?> raw) {
                Map<String, Object> experience = (Map<String, Object>) raw;
                required(errors, "config/resume.yaml", "experiences[" + i + "].company", experience.get("company"));
                required(errors, "config/resume.yaml", "experiences[" + i + "].role", experience.get("role"));
                validateDate(errors, "experiences[" + i + "].startDate", experience.get("startDate"));
                Object endDate = experience.get("endDate");
                if (endDate != null && !String.valueOf(endDate).isBlank()) {
                    validateDate(errors, "experiences[" + i + "].endDate", endDate);
                }
                if (i < 3 && MapValues.list(experience, "technologies").isEmpty()) {
                    errors.add("config/resume.yaml -> experiences[" + i + "].technologies: add at least one technology.");
                }
            }
        }
    }

    private void validateCoverLetter(List<String> errors, Map<String, Object> cover, Map<String, Object> application) {
        required(errors, "config/cover-letter.yaml", "coverLetter.generic.opening", cover.get("opening"));
        required(errors, "config/cover-letter.yaml", "coverLetter.generic.professionalValue", cover.get("professionalValue"));
        required(errors, "config/cover-letter.yaml", "coverLetter.generic.closing", cover.get("closing"));
        if (!application.isEmpty()) {
            required(errors, "application.yaml", "application.company", application.get("company"));
            required(errors, "application.yaml", "application.role", application.get("role"));
            if (MapValues.list(application, "matchingPoints").size() < 2) {
                errors.add("application.yaml -> motivation.matchingPoints: application-ready mode requires at least two matching points.");
            }
            if (MapValues.string(application, "companyReason").isBlank() && MapValues.string(application, "roleReason").isBlank()) {
                errors.add("application.yaml -> motivation: add companyReason or roleReason for application-ready mode.");
            }
        }
    }

    private void validatePrivacy(List<String> errors, Map<String, Object> model, Language language) {
        Map<String, Object> privacy = MapValues.map(model, "privacy");
        if (MapValues.bool(privacy, "enabled", false) && MapValues.string(privacy, "text").isBlank()) {
            errors.add("config/settings.yaml -> privacy.text." + language.tag()
                    + ": required string is missing when privacy is enabled.");
        }
    }

    private void validatePhoto(List<String> errors, Map<String, Object> model) {
        validateRelativeAssetPath(errors, "config/private.yaml -> photo.path",
                MapValues.string(MapValues.map(MapValues.map(model, "private"), "photo"), "path"),
                "../assets/photo.png");

        validateRelativeAssetPath(errors, "config/private.yaml -> signature.imagePath",
                MapValues.string(MapValues.map(MapValues.map(model, "private"), "signature"), "imagePath"),
                "../assets/signature.png");
    }

    private void validateRelativeAssetPath(List<String> errors, String yamlPath, String value, String example) {
        if (!value.isBlank() && Path.of(value).isAbsolute()) {
            errors.add(yamlPath + ": use a relative path such as " + example + ".");
        }
    }

    private void required(List<String> errors, String file, String yamlPath, Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            errors.add(file + " -> " + yamlPath + ": required string is missing.");
        }
    }

    private void validateEmail(List<String> errors, Object value) {
        if (value != null && !String.valueOf(value).isBlank() && !EMAIL.matcher(String.valueOf(value)).matches()) {
            errors.add("config/private.yaml -> contact.email: provide a valid email address.");
        }
    }

    private void validateUrl(List<String> errors, String yamlPath, Object value, String expectedHost) {
        String text = value == null ? "" : String.valueOf(value);
        if (!text.isBlank() && (!text.startsWith("https://") || !text.toLowerCase().contains(expectedHost))) {
            errors.add("config/private.yaml -> " + yamlPath + ": provide a valid https://" + expectedHost + " URL.");
        }
    }

    private void validateDate(List<String> errors, String yamlPath, Object value) {
        String text = value == null ? "" : String.valueOf(value);
        if (!YEAR_MONTH.matcher(text).matches()) {
            errors.add("config/resume.yaml -> " + yamlPath + ": expected YYYY-MM.");
        }
    }

    private void rejectPlaceholder(List<String> errors, String file, String yamlPath, Object value) {
        if (value == null) {
            return;
        }
        String text = String.valueOf(value);
        for (String placeholder : PLACEHOLDERS) {
            if (text.toLowerCase().contains(placeholder.toLowerCase())) {
                errors.add(file + " -> " + yamlPath + ": replace placeholder value `" + text + "` for production mode.");
                return;
            }
        }
    }
}
