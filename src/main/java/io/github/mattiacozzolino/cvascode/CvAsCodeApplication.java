package io.github.mattiacozzolino.cvascode;

import io.github.mattiacozzolino.cvascode.config.ConfigurationException;
import io.github.mattiacozzolino.cvascode.cli.RootCommand;
import picocli.CommandLine;

public final class CvAsCodeApplication {
    private CvAsCodeApplication() {
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new RootCommand())
                .setExecutionExceptionHandler((ex, commandLine, parseResult) -> {
                    if (ex instanceof ConfigurationException || ex instanceof IllegalArgumentException
                            || (ex instanceof IllegalStateException && ex.getMessage() != null
                            && ex.getMessage().startsWith("Generated PDF failed validation"))) {
                        commandLine.getErr().println(ex.getMessage());
                        commandLine.getErr().println("Re-run with --help to inspect available options.");
                        return 2;
                    }
                    ex.printStackTrace(commandLine.getErr());
                    return 1;
                })
                .execute(args);
        System.exit(exitCode);
    }
}
