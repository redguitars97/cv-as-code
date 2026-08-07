package io.github.mattiacozzolino.cvascode.cli;

import io.github.mattiacozzolino.cvascode.domain.DocumentResult;
import io.github.mattiacozzolino.cvascode.domain.GenerationMode;
import io.github.mattiacozzolino.cvascode.domain.Language;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "generate-cover-letter", description = "Generate one localized cover letter PDF.")
public final class GenerateCoverLetterCommand implements Callable<Integer> {
    @Option(names = "--language", required = true, description = "Language: it or en")
    private String language;

    @Option(names = "--application-config", description = "Optional application YAML config.")
    private Path applicationConfig;

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

    @Override
    public Integer call() {
        DocumentResult result = GenerationSupport.generator(configDirectory)
                .generateCoverLetter(Language.fromCliName(language), privateConfig, applicationConfig, output, keepHtml,
                        GenerationMode.fromCliName(mode));
        System.out.println("Generated " + result.pdf());
        return 0;
    }
}
