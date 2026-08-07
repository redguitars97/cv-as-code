package io.github.mattiacozzolino.cvascode.cli;

import io.github.mattiacozzolino.cvascode.domain.DocumentKind;
import io.github.mattiacozzolino.cvascode.domain.GenerationMode;
import io.github.mattiacozzolino.cvascode.domain.Language;
import io.github.mattiacozzolino.cvascode.config.ConfigurationValidator;
import io.github.mattiacozzolino.cvascode.domain.PdfReport;
import io.github.mattiacozzolino.cvascode.config.DocumentModelFactory;
import io.github.mattiacozzolino.cvascode.config.YamlRepository;
import io.github.mattiacozzolino.cvascode.util.FileNames;
import io.github.mattiacozzolino.cvascode.validation.PdfValidator;
import io.github.mattiacozzolino.cvascode.validation.ReportWriter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "validate", description = "Validate an expected generated PDF.")
public final class ValidateCommand implements Callable<Integer> {
    @Option(names = "--document", description = "Document: cv or cover-letter. Omit to validate YAML configuration only.")
    private String document;

    @Option(names = "--language", description = "Language: it or en. Omit to validate YAML configuration only.")
    private String language;

    @Option(names = "--output", defaultValue = "./output", description = "Output directory.")
    private Path output;

    @Option(names = "--config", defaultValue = "./config", description = "Config directory.")
    private Path configDirectory;

    @Option(names = "--private-config", description = "Path to private YAML config. Defaults to <config>/private.yaml.")
    private Path privateConfig;

    @Option(names = "--mode", defaultValue = "demo", description = "Validation mode: demo or production.")
    private String mode;

    @Override
    public Integer call() {
        if (document == null && language == null) {
            validateConfiguration();
            System.out.println("Configuration validation passed: " + configDirectory);
            return 0;
        }
        if (document == null || language == null) {
            throw new IllegalArgumentException("Use both --document and --language, or omit both to validate YAML configuration.");
        }
        DocumentKind kind = DocumentKind.fromCliName(document);
        Language lang = Language.fromCliName(language);
        DocumentModelFactory factory = new DocumentModelFactory(new YamlRepository(configDirectory));
        Map<String, Object> model = kind == DocumentKind.RESUME
                ? factory.resume(lang, privateConfig)
                : factory.coverLetter(lang, privateConfig, null);
        Path pdf = output.resolve(FileNames.pdf(kind, lang, model));
        PdfReport report = new PdfValidator().validate(kind, lang, pdf, model);
        Path reportPath = output.resolve("reports").resolve(FileNames.report(kind, lang));
        new ReportWriter().write(report, reportPath);
        System.out.println("Validation " + (report.isValid() ? "passed" : "failed") + ": " + reportPath);
        return report.isValid() ? 0 : 2;
    }

    private void validateConfiguration() {
        DocumentModelFactory factory = new DocumentModelFactory(new YamlRepository(configDirectory));
        ConfigurationValidator validator = new ConfigurationValidator();
        GenerationMode generationMode = GenerationMode.fromCliName(mode);
        for (Language lang : Language.values()) {
            validator.validateModel(factory.resume(lang, privateConfig), DocumentKind.RESUME, lang, generationMode);
            validator.validateModel(factory.coverLetter(lang, privateConfig, null), DocumentKind.COVER_LETTER, lang, generationMode);
        }
    }
}
