package io.github.mattiacozzolino.cvascode.cli;

import picocli.CommandLine.Command;

@Command(
        name = "cv-as-code",
        mixinStandardHelpOptions = true,
        version = "cv-as-code 1.0.0",
        description = "Generate ATS-friendly CV and cover letter PDFs from YAML.",
        subcommands = {
                GenerateCvCommand.class,
                GenerateCoverLetterCommand.class,
                GenerateAllCommand.class,
                ValidateCommand.class,
                InspectCommand.class,
                PreviewCommand.class,
                InitCommand.class
        }
)
public final class RootCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("Use one of: init, generate-cv, generate-cover-letter, generate-all, validate, inspect, preview");
    }
}
