package io.github.mattiacozzolino.cvascode.validation;

import io.github.mattiacozzolino.cvascode.domain.DocumentKind;
import io.github.mattiacozzolino.cvascode.domain.Language;
import io.github.mattiacozzolino.cvascode.domain.PdfReport;
import io.github.mattiacozzolino.cvascode.util.MapValues;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class PdfValidator {
    private final PdfTextExtractor extractor = new PdfTextExtractor();

    public PdfReport validate(DocumentKind kind, Language language, Path pdf) {
        return validate(kind, language, pdf, Map.of());
    }

    public PdfReport validate(DocumentKind kind, Language language, Path pdf, Map<String, Object> model) {
        PdfReport report = new PdfReport(kind, language, pdf);
        report.setExists(Files.exists(pdf));
        report.addCheck("pdfExists", report.isExists());
        if (!report.isExists()) {
            report.setError("PDF does not exist");
            report.setValid(false);
            return report;
        }

        PdfTextExtractor.ExtractedPdf extracted = extractor.extract(pdf);
        String text = normalize(extracted.text());
        String rawText = extracted.text();
        String lower = text.toLowerCase(Locale.ROOT);
        report.setPageCount(extracted.pageCount());
        report.setTextLength(text.length());
        Map<String, Object> candidate = MapValues.map(model, "candidate");
        Map<String, Object> personal = MapValues.map(candidate, "personal");
        Map<String, Object> resume = MapValues.map(model, "resume");
        Map<String, Object> settings = MapValues.map(model, "settings");
        Map<String, Object> document = MapValues.map(settings, "document");
        Map<String, Object> kindSettings = MapValues.map(document, kind == DocumentKind.RESUME ? "resume" : "coverLetter");
        int maxPages = MapValues.integer(kindSettings, "maxPages", kind.maxPages());
        String fullName = (MapValues.string(personal, "firstName") + " " + MapValues.string(personal, "lastName")).trim();
        String title = MapValues.string(personal, "professionalTitle");

        report.addCheck("textNotEmpty", !text.isBlank());
        report.addCheck("containsCandidateName", fullName.isBlank() || lower.contains(fullName.toLowerCase(Locale.ROOT)));
        report.addCheck("containsProfileTitle", title.isBlank() || lower.contains(title.toLowerCase(Locale.ROOT)));
        report.addCheck("containsContacts", lower.contains("@") && (lower.contains("linkedin") || lower.contains("github")));
        report.addCheck("noReplacementCharacter", !text.contains("\uFFFD"));
        report.addCheck("noPrivateUseCharacters", noPrivateUseCharacters(text));
        report.addCheck("noVerticalText", singleCharacterLineRatio(rawText) < 0.08);
        report.addCheck("noAlmostEmptyPage", !hasAlmostEmptyPage(extracted));

        if (kind == DocumentKind.RESUME) {
            report.addCheck("pageCountAtMostTwo", extracted.pageCount() <= maxPages);
            report.addCheck("containsPrimarySkill", containsPrimarySkill(lower, resume));
            report.addCheck("technologyChipsReadable", technologiesReadable(lower, resume));
            report.addCheck("containsExpectedCompanies", containsExpectedCompanies(lower, resume));
            report.addCheck("reasonableTextOrder", appearsInOrder(lower, List.of("professional", "experience"))
                    || appearsInOrder(lower, List.of("profilo", "esperienza")));
        } else {
            report.addCheck("pageCountWithinLimit", extracted.pageCount() <= maxPages);
            report.addCheck("containsOpening", lower.contains("dear") || lower.contains("gentile"));
            report.addCheck("containsClosing", lower.contains("sincerely") || lower.contains("cordiali saluti"));
        }

        report.setValid(report.getChecks().values().stream().allMatch(Boolean::booleanValue));
        return report;
    }

    private boolean appearsInOrder(String text, List<String> markers) {
        int cursor = 0;
        for (String marker : markers) {
            int index = text.indexOf(marker, cursor);
            if (index < 0) {
                return false;
            }
            cursor = index + marker.length();
        }
        return true;
    }

    private String normalize(String text) {
        return text.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
    }

    private boolean noPrivateUseCharacters(String text) {
        return text.codePoints().noneMatch(codePoint ->
                (codePoint >= 0xE000 && codePoint <= 0xF8FF)
                        || (codePoint >= 0xF0000 && codePoint <= 0xFFFFD)
                        || (codePoint >= 0x100000 && codePoint <= 0x10FFFD));
    }

    private boolean containsPrimarySkill(String lowerText, Map<String, Object> resume) {
        for (Object group : MapValues.list(resume, "skills")) {
            if (group instanceof Map<?, ?> raw) {
                Object values = raw.get("items");
                if (values instanceof List<?> items && !items.isEmpty()) {
                    String first = String.valueOf(items.get(0)).toLowerCase(Locale.ROOT);
                    return lowerText.contains(first);
                }
            }
        }
        return true;
    }

    private boolean technologiesReadable(String lowerText, Map<String, Object> resume) {
        for (Object item : MapValues.list(resume, "experience")) {
            if (item instanceof Map<?, ?> raw) {
                Map<String, Object> experience = (Map<String, Object>) raw;
                List<Object> projects = MapValues.list(experience, "projects");
                if (projects.isEmpty()
                        && !technologiesReadableForItem(lowerText, MapValues.list(experience, "technologies"))) {
                    return false;
                }
                for (Object projectItem : projects) {
                    if (projectItem instanceof Map<?, ?> projectRaw
                            && !technologiesReadableForItem(lowerText, MapValues.list((Map<String, Object>) projectRaw, "technologies"))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean technologiesReadableForItem(String lowerText, List<Object> technologies) {
        if (technologies.size() < 3) {
            return true;
        }
        int cursor = 0;
        int checked = 0;
        for (Object technology : technologies) {
            String value = String.valueOf(technology).toLowerCase(Locale.ROOT);
            if (value.length() < 3) {
                continue;
            }
            int index = lowerText.indexOf(value, cursor);
            if (index < 0) {
                return false;
            }
            cursor = index + value.length();
            checked++;
            if (checked >= 5) {
                return true;
            }
        }
        return true;
    }

    private boolean containsExpectedCompanies(String lowerText, Map<String, Object> resume) {
        List<Object> experiences = MapValues.list(resume, "experience");
        if (experiences.isEmpty()) {
            return true;
        }
        for (Object item : experiences) {
            if (item instanceof Map<?, ?> raw) {
                String company = MapValues.string((Map<String, Object>) raw, "company").toLowerCase(Locale.ROOT);
                if (!company.isBlank() && !lowerText.contains(companyToken(company))) {
                    return false;
                }
            }
        }
        return true;
    }

    private String companyToken(String company) {
        return company.replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\b(s|r|l|srl|inc|ltd|llc|engineering|boots|alliance)\\b", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .split(" ")[0];
    }

    private double singleCharacterLineRatio(String rawText) {
        String[] lines = rawText.split("\\R");
        int nonBlank = 0;
        int single = 0;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isBlank()) {
                continue;
            }
            nonBlank++;
            if (trimmed.length() == 1) {
                single++;
            }
        }
        return nonBlank == 0 ? 0 : (double) single / nonBlank;
    }

    private boolean hasAlmostEmptyPage(PdfTextExtractor.ExtractedPdf extracted) {
        if (extracted.pageCount() <= 1) {
            return false;
        }
        for (String page : extracted.pages()) {
            if (normalize(page).length() < 700) {
                return true;
            }
        }
        return false;
    }
}
