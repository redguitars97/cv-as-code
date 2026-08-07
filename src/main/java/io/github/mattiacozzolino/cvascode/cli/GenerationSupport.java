package io.github.mattiacozzolino.cvascode.cli;

import io.github.mattiacozzolino.cvascode.config.DocumentModelFactory;
import io.github.mattiacozzolino.cvascode.config.YamlRepository;
import io.github.mattiacozzolino.cvascode.render.DocumentGenerator;

import java.nio.file.Path;

final class GenerationSupport {
    private GenerationSupport() {
    }

    static DocumentGenerator generator(Path configDirectory) {
        YamlRepository repository = new YamlRepository(configDirectory);
        return new DocumentGenerator(new DocumentModelFactory(repository));
    }
}
