package io.github.mattiacozzolino.cvascode;

import io.github.mattiacozzolino.cvascode.config.DocumentModelFactory;
import io.github.mattiacozzolino.cvascode.config.YamlRepository;
import io.github.mattiacozzolino.cvascode.domain.DocumentKind;
import io.github.mattiacozzolino.cvascode.domain.Language;
import io.github.mattiacozzolino.cvascode.render.TemplateRenderer;
import io.github.mattiacozzolino.cvascode.util.MapValues;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateRendererTest {
    private final DocumentModelFactory factory = new DocumentModelFactory(new YamlRepository(Path.of("config")));
    private final TemplateRenderer renderer = new TemplateRenderer();

    @Test
    void rendersItalianResumeWithoutPublicHtmlOutputContractLeak() {
        var model = factory.resume(Language.IT, Path.of("config/private.example.yaml"));

        String html = renderer.render(DocumentKind.RESUME, model);
        Map<String, Object> personal = MapValues.map(MapValues.map(model, "candidate"), "personal");
        Map<String, Object> sections = MapValues.map(MapValues.map(model, "labels"), "sections");
        Map<String, Object> privacy = MapValues.map(model, "privacy");
        Map<String, Object> firstSkillGroup = (Map<String, Object>) MapValues.list(MapValues.map(model, "resume"), "skills").get(0);
        String fullName = MapValues.string(personal, "firstName") + " " + MapValues.string(personal, "lastName");

        assertThat(html)
                .contains(fullName)
                .contains(MapValues.string(personal, "professionalTitle"))
                .contains(MapValues.string(personal, "positioning"))
                .contains(MapValues.string(sections, "summary"))
                .contains(MapValues.string(privacy, "text"))
                .contains(String.valueOf(MapValues.list(firstSkillGroup, "items").get(0)));
    }

    @Test
    void rendersGenericEnglishCoverLetter() {
        var model = factory.coverLetter(Language.EN, Path.of("config/private.example.yaml"), null);

        String html = renderer.render(DocumentKind.COVER_LETTER, model);
        Map<String, Object> personal = MapValues.map(MapValues.map(model, "candidate"), "personal");
        Map<String, Object> coverLabels = MapValues.map(MapValues.map(model, "labels"), "cover");

        assertThat(html)
                .contains(MapValues.string(coverLabels, "genericGreeting"))
                .contains(MapValues.string(personal, "professionalTitle"))
                .contains(MapValues.string(coverLabels, "closing"))
                .doesNotContain("Example Company");
    }

    @Test
    void rendersPersonalizedCoverLetterOnlyFromApplicationYaml() {
        var model = factory.coverLetter(
                Language.EN,
                Path.of("config/private.example.yaml"),
                Path.of("config/application.example.yaml"));

        String html = renderer.render(DocumentKind.COVER_LETTER, model);
        Map<String, Object> personal = MapValues.map(MapValues.map(model, "candidate"), "personal");
        Map<String, Object> application = MapValues.map(model, "application");
        List<Object> matchingPoints = MapValues.list(application, "matchingPoints");

        assertThat(html)
                .contains(MapValues.string(application, "company"))
                .contains(String.valueOf(matchingPoints.get(0)))
                .contains(MapValues.string(personal, "professionalTitle"));
    }
}
