package io.github.mattiacozzolino.cvascode.cli;

import io.github.mattiacozzolino.cvascode.domain.DocumentResult;
import io.github.mattiacozzolino.cvascode.domain.GenerationMode;
import io.github.mattiacozzolino.cvascode.domain.Language;
import io.github.mattiacozzolino.cvascode.domain.PhotoMode;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "generate-cv", description = "Generate one localized CV PDF.")
public final class GenerateCvCommand implements Callable<Integer> {
    @Option(names = "--language", required = true, description = "Language: it or en")
    private String language;

    @Option(names = "--private-config", description = "Path to private YAML config. Defaults to <config>/private.yaml.")
    private Path privateConfig;

    @Option(names = "--output", defaultValue = "./output", description = "Output directory.")
    private Path output;

    @Option(names = "--config", defaultValue = "./config", description = "Public config directory.")
    private Path configDirectory;

    @Option(names = "--keep-html", description = "Keep intermediate HTML under output/debug-html.")
    private boolean keepHtml;

    @Option(names = "--mode", defaultValue = "demo", description = "Generation mode: demo or production.")
    private String mode;

    @Option(names = "--include-photo", description = "Force photo on for this CV.")
    private boolean includePhoto;

    @Option(names = "--exclude-photo", description = "Force photo off for this CV.")
    private boolean excludePhoto;

    @Override
    public Integer call() {
        DocumentResult result = GenerationSupport.generator(configDirectory)
                .generateResume(Language.fromCliName(language), privateConfig, output, keepHtml,
                        GenerationMode.fromCliName(mode), photoMode());
        System.out.println("Generated " + result.pdf());
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
