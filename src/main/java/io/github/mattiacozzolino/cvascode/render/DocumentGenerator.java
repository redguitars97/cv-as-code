package io.github.mattiacozzolino.cvascode.render;

import io.github.mattiacozzolino.cvascode.config.DocumentModelFactory;
import io.github.mattiacozzolino.cvascode.config.ConfigurationValidator;
import io.github.mattiacozzolino.cvascode.domain.DocumentKind;
import io.github.mattiacozzolino.cvascode.domain.DocumentResult;
import io.github.mattiacozzolino.cvascode.domain.GenerationMode;
import io.github.mattiacozzolino.cvascode.domain.Language;
import io.github.mattiacozzolino.cvascode.domain.PhotoMode;
import io.github.mattiacozzolino.cvascode.domain.PdfReport;
import io.github.mattiacozzolino.cvascode.util.FileNames;
import io.github.mattiacozzolino.cvascode.validation.PdfValidator;
import io.github.mattiacozzolino.cvascode.validation.ReportWriter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class DocumentGenerator {
    private final DocumentModelFactory modelFactory;
    private final TemplateRenderer templateRenderer;
    private final PdfRenderer pdfRenderer;
    private final PdfValidator validator;
    private final ConfigurationValidator configurationValidator = new ConfigurationValidator();
    private final ReportWriter reportWriter;

    public DocumentGenerator(DocumentModelFactory modelFactory) {
        this(modelFactory, new TemplateRenderer(), new PdfRenderer(), new PdfValidator(), new ReportWriter());
    }

    DocumentGenerator(
            DocumentModelFactory modelFactory,
            TemplateRenderer templateRenderer,
            PdfRenderer pdfRenderer,
            PdfValidator validator,
            ReportWriter reportWriter
    ) {
        this.modelFactory = modelFactory;
        this.templateRenderer = templateRenderer;
        this.pdfRenderer = pdfRenderer;
        this.validator = validator;
        this.reportWriter = reportWriter;
    }

    public DocumentResult generateResume(Language language, Path privateConfig, Path output, boolean keepHtml) {
        return generateResume(language, privateConfig, output, keepHtml, GenerationMode.DEMO, PhotoMode.CONFIG);
    }

    public DocumentResult generateResume(Language language, Path privateConfig, Path output, boolean keepHtml, GenerationMode mode, PhotoMode photoMode) {
        Map<String, Object> model = modelFactory.resume(language, privateConfig, mode, photoMode);
        return generate(DocumentKind.RESUME, language, model, output, keepHtml, mode);
    }

    public DocumentResult generateCoverLetter(Language language, Path privateConfig, Path applicationConfig, Path output, boolean keepHtml) {
        return generateCoverLetter(language, privateConfig, applicationConfig, output, keepHtml, GenerationMode.DEMO);
    }

    public DocumentResult generateCoverLetter(Language language, Path privateConfig, Path applicationConfig, Path output, boolean keepHtml, GenerationMode mode) {
        Map<String, Object> model = modelFactory.coverLetter(language, privateConfig, applicationConfig, mode);
        return generate(DocumentKind.COVER_LETTER, language, model, output, keepHtml, mode);
    }

    private DocumentResult generate(DocumentKind kind, Language language, Map<String, Object> model, Path output, boolean keepHtml, GenerationMode mode) {
        try {
            Files.createDirectories(output);
            configurationValidator.validateModel(model, kind, language, mode);
            String html = templateRenderer.render(kind, model);
            Path pdf = output.resolve(FileNames.pdf(kind, language, model));
            Path htmlPath = writeIntermediateHtml(kind, language, output, keepHtml, html);
            pdfRenderer.render(html, pdf);
            PdfReport report = validator.validate(kind, language, pdf, model);
            Path reportPath = output.resolve("reports").resolve(FileNames.report(kind, language));
            reportWriter.write(report, reportPath);
            if (!report.isValid()) {
                throw new IllegalStateException("Generated PDF failed validation. Report: " + reportPath);
            }
            return new DocumentResult(kind, language, pdf, reportPath, htmlPath);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot generate document " + kind + " " + language, e);
        }
    }

    private Path writeIntermediateHtml(DocumentKind kind, Language language, Path output, boolean keepHtml, String html) throws IOException {
        if (!keepHtml) {
            Path temp = Files.createTempFile("cv-as-code-", ".html");
            Files.writeString(temp, html, StandardCharsets.UTF_8);
            Files.deleteIfExists(temp);
            return null;
        }
        Path debugDirectory = output.resolve("debug-html");
        Files.createDirectories(debugDirectory);
        Path target = debugDirectory.resolve(FileNames.html(kind, language));
        Files.writeString(target, html, StandardCharsets.UTF_8);
        return target;
    }
}
