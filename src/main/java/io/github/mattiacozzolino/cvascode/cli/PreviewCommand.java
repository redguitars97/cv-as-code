package io.github.mattiacozzolino.cvascode.cli;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "preview", description = "Render PNG previews from a generated PDF.")
public final class PreviewCommand implements Callable<Integer> {
    @Option(names = "--pdf", required = true, description = "PDF to render.")
    private Path pdf;

    @Option(names = "--output", required = true, description = "Directory where preview PNG files are written.")
    private Path output;

    @Option(names = "--prefix", description = "Preview file prefix. Defaults to the PDF file name without extension.")
    private String prefix;

    @Option(names = "--dpi", description = "Preview resolution. Default: ${DEFAULT-VALUE}.")
    private float dpi = 144f;

    @Override
    public Integer call() throws IOException {
        if (!Files.exists(pdf)) {
            throw new IllegalArgumentException("PDF does not exist: " + pdf);
        }
        Files.createDirectories(output);
        String filePrefix = prefix == null || prefix.isBlank() ? baseName(pdf) : prefix;
        try (PDDocument document = Loader.loadPDF(pdf.toFile())) {
            PDFRenderer renderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); page++) {
                Path target = output.resolve(fileName(filePrefix, page, document.getNumberOfPages()));
                ImageIO.write(renderer.renderImageWithDPI(page, dpi, ImageType.RGB), "png", target.toFile());
                System.out.println("Wrote preview: " + target);
            }
        }
        return 0;
    }

    private String baseName(Path path) {
        String name = path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    private String fileName(String prefix, int page, int pages) {
        if (pages == 1) {
            return prefix + ".png";
        }
        return prefix + "-page-" + (page + 1) + ".png";
    }
}
