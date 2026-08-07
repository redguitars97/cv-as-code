package io.github.mattiacozzolino.cvascode.cli;

import io.github.mattiacozzolino.cvascode.validation.PdfTextExtractor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "inspect", description = "Inspect page count and extractable text from a PDF.")
public final class InspectCommand implements Callable<Integer> {
    @Option(names = "--pdf", required = true, description = "PDF to inspect.")
    private Path pdf;

    @Override
    public Integer call() {
        PdfTextExtractor.ExtractedPdf extracted = new PdfTextExtractor().extract(pdf);
        System.out.println("PDF: " + pdf);
        System.out.println("Pages: " + extracted.pageCount());
        System.out.println("Text length: " + extracted.text().trim().length());
        System.out.println(extracted.text());
        return 0;
    }
}
