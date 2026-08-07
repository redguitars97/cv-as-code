package io.github.mattiacozzolino.cvascode;

import io.github.mattiacozzolino.cvascode.config.ConfigurationException;
import io.github.mattiacozzolino.cvascode.config.ConfigurationValidator;
import io.github.mattiacozzolino.cvascode.config.DocumentModelFactory;
import io.github.mattiacozzolino.cvascode.config.YamlRepository;
import io.github.mattiacozzolino.cvascode.domain.DocumentKind;
import io.github.mattiacozzolino.cvascode.domain.GenerationMode;
import io.github.mattiacozzolino.cvascode.domain.Language;
import io.github.mattiacozzolino.cvascode.domain.PhotoMode;
import io.github.mattiacozzolino.cvascode.render.DocumentGenerator;
import io.github.mattiacozzolino.cvascode.util.MapValues;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfigurationValidatorTest {
    @TempDir
    Path output;

    @Test
    void productionModeRejectsPlaceholderContacts() {
        DocumentGenerator generator = new DocumentGenerator(new DocumentModelFactory(new YamlRepository(Path.of("config"))));

        assertThatThrownBy(() -> generator.generateResume(
                Language.IT,
                Path.of("config/private.example.yaml"),
                output,
                false,
                GenerationMode.PRODUCTION,
                PhotoMode.EXCLUDE))
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("contact.email")
                .hasMessageContaining("contact.phone")
                .hasMessageContaining("production mode");
    }

    @Test
    void enabledPrivacyRequiresConfiguredLocalizedText() {
        DocumentModelFactory factory = new DocumentModelFactory(new YamlRepository(Path.of("config")));
        Map<String, Object> model = factory.resume(Language.IT, Path.of("config/private.example.yaml"));
        Map<String, Object> privacy = new LinkedHashMap<>(MapValues.map(model, "privacy"));
        privacy.put("enabled", true);
        privacy.put("text", "");
        model.put("privacy", privacy);

        assertThatThrownBy(() -> new ConfigurationValidator()
                .validateModel(model, DocumentKind.RESUME, Language.IT, GenerationMode.DEMO))
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("config/settings.yaml -> privacy.text.it");
    }
}
