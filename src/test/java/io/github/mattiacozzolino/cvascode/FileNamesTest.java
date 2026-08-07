package io.github.mattiacozzolino.cvascode;

import io.github.mattiacozzolino.cvascode.domain.DocumentKind;
import io.github.mattiacozzolino.cvascode.domain.Language;
import io.github.mattiacozzolino.cvascode.util.FileNames;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FileNamesTest {
    @Test
    void resolvesRequiredOutputNames() {
        Map<String, Object> model = Map.of(
                "candidate", Map.of("personal", Map.of("firstName", "Tizio", "lastName", "Caio")),
                "settings", Map.of("output", Map.of("fileNamePattern", "{firstName}_{lastName}_{documentType}_{language}"))
        );

        assertThat(FileNames.pdf(DocumentKind.RESUME, Language.IT, model)).isEqualTo("Tizio_Caio_CV_IT.pdf");
        assertThat(FileNames.pdf(DocumentKind.RESUME, Language.EN, model)).isEqualTo("Tizio_Caio_CV_EN.pdf");
        assertThat(FileNames.pdf(DocumentKind.COVER_LETTER, Language.IT, model)).isEqualTo("Tizio_Caio_Cover_Letter_IT.pdf");
        assertThat(FileNames.pdf(DocumentKind.COVER_LETTER, Language.EN, model)).isEqualTo("Tizio_Caio_Cover_Letter_EN.pdf");
    }

    @Test
    void resolvesReportNames() {
        assertThat(FileNames.report(DocumentKind.RESUME, Language.IT)).isEqualTo("cv-it-report.json");
        assertThat(FileNames.report(DocumentKind.COVER_LETTER, Language.EN)).isEqualTo("cover-letter-en-report.json");
    }

    @Test
    void sanitizesNamesForFileSystems() {
        assertThat(FileNames.sanitize("Anna Maria D'Angiò")).isEqualTo("Anna_Maria_DAngio");
    }
}
