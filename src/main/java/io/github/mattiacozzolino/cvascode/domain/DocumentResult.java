package io.github.mattiacozzolino.cvascode.domain;

import java.nio.file.Path;

public record DocumentResult(
        DocumentKind documentKind,
        Language language,
        Path pdf,
        Path report,
        Path debugHtml
) {
}
