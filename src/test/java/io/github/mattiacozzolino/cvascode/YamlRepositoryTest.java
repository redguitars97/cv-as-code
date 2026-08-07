package io.github.mattiacozzolino.cvascode;

import io.github.mattiacozzolino.cvascode.config.YamlRepository;
import io.github.mattiacozzolino.cvascode.config.ConfigurationException;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class YamlRepositoryTest {
    private final YamlRepository repository = new YamlRepository(Path.of("config"));

    @Test
    void parsesResumeYaml() {
        var resume = repository.resume();

        assertThat(resume).containsKey("resume");
        assertThat(resume.toString()).contains("summary", "experiences", "technologies");
    }

    @Test
    void parsesSingleSettingsYaml() {
        var settings = repository.settings();

        assertThat(settings.toString()).contains("resume", "includePhoto", "fileNamePattern");
    }

    @Test
    void missingApplicationConfigFallsBackToEmptyData() {
        var application = repository.application(Path.of("config/missing-application.yaml"));

        assertThat(application).isEmpty();
    }

    @Test
    void missingRequiredYamlFailsFast() {
        assertThatThrownBy(() -> new YamlRepository(Path.of("missing-config")).resume())
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("Missing configuration file");
    }
}
