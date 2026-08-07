package io.github.mattiacozzolino.cvascode.cli;

import io.github.mattiacozzolino.cvascode.domain.Language;
import io.github.mattiacozzolino.cvascode.domain.GenerationMode;
import io.github.mattiacozzolino.cvascode.domain.PhotoMode;
import io.github.mattiacozzolino.cvascode.render.DocumentGenerator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "generate-all", description = "Generate CV and cover letter PDFs in Italian and English.")
public final class GenerateAllCommand implements Callable<Integer> {
    @Option(names = "--private-config", description = "Path to private YAML config. Defaults to <config>/private.yaml.")
    private Path privateConfig;

    @Option(names = "--output", defaultValue = "./output", description = "Output directory.")
    private Path output;

    @Option(names = "--config", defaultValue = "./config", description = "Public config directory.")
    private Path configDirectory;

    @Option(names = "--application-config", description = "Optional application YAML config for both cover letters.")
    private Path applicationConfig;

    @Option(names = "--keep-html", description = "Keep intermediate HTML under output/debug-html.")
    private boolean keepHtml;

    @Option(names = "--mode", defaultValue = "demo", description = "Generation mode: demo or production.")
    private String mode;

    @Option(names = "--include-photo", description = "Force photo on for generated CVs.")
    private boolean includePhoto;

    @Option(names = "--exclude-photo", description = "Force photo off for generated CVs.")
    private boolean excludePhoto;

    @Override
    public Integer call() {
        DocumentGenerator generator = GenerationSupport.generator(configDirectory);
        GenerationMode generationMode = GenerationMode.fromCliName(mode);
        PhotoMode photoMode = photoMode();
        for (Language language : Language.values()) {
            generator.generateResume(language, privateConfig, output, keepHtml, generationMode, photoMode);
            generator.generateCoverLetter(language, privateConfig, applicationConfig, output, keepHtml, generationMode);
        }
        System.out.println("Generated all documents in " + output);
        return 0;
    }

    private PhotoMode photoMode() {
        if (includePhoto && excludePhoto) {
            throw new IllegalArgumentException("Use either --include-photo or --exclude-photo, not both.");
        }
        if (includePhoto) {
            return PhotoMode.INCLUDE;
        }
        if (excludePhoto) {
            return PhotoMode.EXCLUDE;
        }
        return PhotoMode.CONFIG;
    }
}
