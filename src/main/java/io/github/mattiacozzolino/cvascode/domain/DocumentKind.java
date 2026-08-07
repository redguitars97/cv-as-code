package io.github.mattiacozzolino.cvascode.domain;

public enum DocumentKind {
    RESUME("cv", "CV", 2),
    COVER_LETTER("cover-letter", "Cover_Letter", 1);

    private final String cliName;
    private final String fileSegment;
    private final int maxPages;

    DocumentKind(String cliName, String fileSegment, int maxPages) {
        this.cliName = cliName;
        this.fileSegment = fileSegment;
        this.maxPages = maxPages;
    }

    public String cliName() {
        return cliName;
    }

    public String fileSegment() {
        return fileSegment;
    }

    public int maxPages() {
        return maxPages;
    }

    public static DocumentKind fromCliName(String value) {
        for (DocumentKind kind : values()) {
            if (kind.cliName.equalsIgnoreCase(value) || kind.name().equalsIgnoreCase(value)) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Unsupported document: " + value);
    }
}
