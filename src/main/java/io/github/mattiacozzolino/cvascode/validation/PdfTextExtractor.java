package io.github.mattiacozzolino.cvascode.validation;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class PdfTextExtractor {
    public ExtractedPdf extract(Path pdf) {
        try (PDDocument document = Loader.loadPDF(pdf.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            List<String> pages = new ArrayList<>();
            for (int page = 1; page <= document.getNumberOfPages(); page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                pages.add(stripper.getText(document));
            }
            return new ExtractedPdf(document.getNumberOfPages(), text == null ? "" : text, pages);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot inspect PDF: " + pdf, e);
        }
    }

    public record ExtractedPdf(int pageCount, String text, List<String> pages) {
    }
}
