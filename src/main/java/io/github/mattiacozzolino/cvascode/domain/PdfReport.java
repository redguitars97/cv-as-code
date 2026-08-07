package io.github.mattiacozzolino.cvascode.domain;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PdfReport {
    private final DocumentKind documentKind;
    private final Language language;
    private final Path pdf;
    private boolean exists;
    private int pageCount;
    private int textLength;
    private boolean valid;
    private String error;
    private final Map<String, Boolean> checks = new LinkedHashMap<>();

    public PdfReport(DocumentKind documentKind, Language language, Path pdf) {
        this.documentKind = documentKind;
        this.language = language;
        this.pdf = pdf;
    }

    public DocumentKind getDocumentKind() {
        return documentKind;
    }

    public Language getLanguage() {
        return language;
    }

    public String getPdf() {
        return pdf.toString();
    }

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getTextLength() {
        return textLength;
    }

    public void setTextLength(int textLength) {
        this.textLength = textLength;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Map<String, Boolean> getChecks() {
        return checks;
    }

    public void addCheck(String name, boolean passed) {
        checks.put(name, passed);
    }
}
