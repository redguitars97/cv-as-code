package io.github.mattiacozzolino.cvascode;

import io.github.mattiacozzolino.cvascode.config.DocumentModelFactory;
import io.github.mattiacozzolino.cvascode.config.YamlRepository;
import io.github.mattiacozzolino.cvascode.domain.DocumentKind;
import io.github.mattiacozzolino.cvascode.domain.GenerationMode;
import io.github.mattiacozzolino.cvascode.domain.Language;
import io.github.mattiacozzolino.cvascode.render.DocumentGenerator;
import io.github.mattiacozzolino.cvascode.util.FileNames;
import io.github.mattiacozzolino.cvascode.validation.PdfTextExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfSystemProperty(named = "cv.e2e", matches = "true")
class GenerateAllE2ETest {
    @TempDir
    Path output;

    @Test
    void generatesAllFourPdfOutputsAndKeepsNoHtmlByDefault() {
        DocumentModelFactory factory = new DocumentModelFactory(new YamlRepository(Path.of("config")));
        DocumentGenerator generator = new DocumentGenerator(factory);

        for (Language language : Language.values()) {
            generator.generateResume(language, Path.of("config/private.example.yaml"), output, false);
            generator.generateCoverLetter(language, Path.of("config/private.example.yaml"), null, output, false);
        }

        assertThat(output.resolve(FileNames.pdf(DocumentKind.RESUME, Language.IT,
                factory.resume(Language.IT, Path.of("config/private.example.yaml"))))).exists();
        assertThat(output.resolve(FileNames.pdf(DocumentKind.RESUME, Language.EN,
                factory.resume(Language.EN, Path.of("config/private.example.yaml"))))).exists();
        assertThat(output.resolve(FileNames.pdf(DocumentKind.COVER_LETTER, Language.IT,
                factory.coverLetter(Language.IT, Path.of("config/private.example.yaml"), null)))).exists();
        assertThat(output.resolve(FileNames.pdf(DocumentKind.COVER_LETTER, Language.EN,
                factory.coverLetter(Language.EN, Path.of("config/private.example.yaml"), null)))).exists();
        assertThat(output.resolve("reports").resolve("cv-it-report.json")).exists();
        assertThat(output.resolve("reports").resolve("cover-letter-en-report.json")).exists();
        assertThat(Files.exists(output.resolve("debug-html"))).isFalse();
    }

    @Test
    void keepHtmlStoresDebugHtmlOutsideFinalPdfSet() {
        DocumentGenerator generator = new DocumentGenerator(new DocumentModelFactory(new YamlRepository(Path.of("config"))));

        generator.generateResume(Language.IT, Path.of("config/private.example.yaml"), output, true);

        assertThat(output.resolve("debug-html").resolve("cv-it.html")).exists();
    }

    @Test
    void generatesAlternativeCandidateWithoutPreviousCandidateData() {
        Path config = Path.of("test-fixtures/tizio-caio/config");
        DocumentGenerator generator = new DocumentGenerator(new DocumentModelFactory(new YamlRepository(config)));

        generator.generateResume(Language.IT, null, output, false, GenerationMode.PRODUCTION, io.github.mattiacozzolino.cvascode.domain.PhotoMode.EXCLUDE);
        generator.generateResume(Language.EN, null, output, false, GenerationMode.PRODUCTION, io.github.mattiacozzolino.cvascode.domain.PhotoMode.EXCLUDE);

        Path itPdf = output.resolve("Tizio_Caio_CV_IT.pdf");
        Path enPdf = output.resolve("Tizio_Caio_CV_EN.pdf");
        assertThat(itPdf).exists();
        assertThat(enPdf).exists();

        String text = new PdfTextExtractor().extract(itPdf).text().toLowerCase(Locale.ROOT);
        assertThat(text)
                .contains("tizio caio", "senior data engineer", "northwind analytics")
                .doesNotContain("mattia", "cozzolino", "itsvil", "capgemini", "walgreens");
    }
}
