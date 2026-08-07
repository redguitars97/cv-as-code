package io.github.mattiacozzolino.cvascode;

import io.github.mattiacozzolino.cvascode.domain.DocumentKind;
import io.github.mattiacozzolino.cvascode.domain.Language;
import io.github.mattiacozzolino.cvascode.validation.PdfValidator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PdfValidatorTest {
    @TempDir
    Path tempDir;

    @Test
    void validatesExtractableResumePdfText() throws Exception {
        Path pdf = tempDir.resolve("resume.pdf");
        writePdf(pdf, "Example Candidate Senior Software Engineer professional experience Example Company Java Spring email@example.com LinkedIn GitHub");

        var report = new PdfValidator().validate(DocumentKind.RESUME, Language.EN, pdf);

        assertThat(report.isValid()).isTrue();
        assertThat(report.getPageCount()).isEqualTo(1);
        assertThat(report.getChecks()).containsEntry("containsPrimarySkill", true);
    }

    @Test
    void rejectsMissingPdf() {
        var report = new PdfValidator().validate(DocumentKind.RESUME, Language.IT, tempDir.resolve("missing.pdf"));

        assertThat(report.isValid()).isFalse();
        assertThat(report.getError()).contains("does not exist");
    }

    private void writePdf(Path target, String text) throws Exception {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.newLineAtOffset(40, 750);
                content.showText(text);
                content.endText();
            }
            document.save(target.toFile());
        }
    }
}
