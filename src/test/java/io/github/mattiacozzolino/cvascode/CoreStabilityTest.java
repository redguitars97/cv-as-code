package io.github.mattiacozzolino.cvascode;

import io.github.mattiacozzolino.cvascode.cli.InitCommand;
import io.github.mattiacozzolino.cvascode.config.YamlRepository;
import io.github.mattiacozzolino.cvascode.domain.ConfigurationModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CoreStabilityTest {
    @TempDir
    Path tempDir;

    @Test
    void sourceTemplatesAndValidatorsDoNotContainCandidateSpecificLayoutRules() throws IOException {
        List<String> forbidden = List.of("Mattia", "Cozzolino", "ITSvil", "Capgemini", "AAROI", "Walgreens",
                "capgemini", "itsvil", "walgreens", "multitrade");
        for (Path file : Files.walk(Path.of("src/main"))
                .filter(Files::isRegularFile)
                .filter(path -> !path.toString().contains("resources/fonts/"))
                .filter(path -> !path.toString().endsWith(".jpg"))
                .filter(path -> !path.toString().endsWith(".png"))
                .toList()) {
            String text = Files.readString(file, StandardCharsets.UTF_8);
            assertThat(text)
                    .as(file.toString())
                    .doesNotContain(forbidden);
        }
    }

    @Test
    void yamlCanBeReadAsTypedConfigurationModel() {
        YamlRepository repository = new YamlRepository(Path.of("config"));

        ConfigurationModel.CandidateRoot candidate = repository.candidateModel();
        ConfigurationModel.ResumeRoot resume = repository.resumeModel();
        ConfigurationModel.PrivateRoot privateConfig = repository.privateModel(Path.of("config/private.example.yaml"));
        ConfigurationModel.CoverLetterRoot coverLetter = repository.coverLetterModel();
        ConfigurationModel.SettingsRoot settings = repository.settingsModel();

        assertThat(candidate.candidate().firstName()).isNotBlank();
        assertThat(candidate.candidate().professionalTitle().it()).isNotBlank();
        assertThat(candidate.candidate().positioning().it()).isNotBlank();
        assertThat(resume.resume().experiences()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(privateConfig.contact().email()).contains("@");
        assertThat(privateConfig.signature().enabled()).isTrue();
        assertThat(coverLetter.coverLetter().generic().subject().en()).isNotBlank();
        assertThat(settings.document().resume().maxPages()).isEqualTo(2);
    }

    @Test
    void bundledFontIsAvailableAsClasspathResource() throws IOException {
        try (var input = Thread.currentThread().getContextClassLoader().getResourceAsStream("fonts/InterVariable.ttf")) {
            assertThat(input).isNotNull();
            assertThat(input.readNBytes(4)).containsExactly((byte) 0, (byte) 1, (byte) 0, (byte) 0);
        }
    }

    @Test
    void initCreatesCanonicalUserWorkspaceWithoutCandidateContacts() {
        Path target = tempDir.resolve("my-resume");
        int exit = new CommandLine(new InitCommand()).execute("--target", target.toString());

        assertThat(exit).isZero();
        assertThat(target.resolve("config/candidate.yaml")).exists();
        assertThat(target.resolve("config/private.yaml")).exists();
        assertThat(target.resolve("config/resume.yaml")).exists();
        assertThat(target.resolve("config/cover-letter.yaml")).exists();
        assertThat(target.resolve("config/settings.yaml")).exists();
        assertThat(target.resolve("assets/photo-placeholder.png")).exists();
        assertThat(target.resolve("output")).isDirectory();
        assertThat(target.resolve("config/application.example.yaml")).exists();
        assertThat(read(target.resolve("config/candidate.yaml"))).doesNotContain("contacts:");
        assertThat(read(target.resolve("config/candidate.yaml"))).doesNotContain("photo:");
        assertThat(read(target.resolve("config/cover-letter.yaml"))).doesNotContain("applications:");
        assertThat(read(target.resolve("config/private.yaml"))).contains("contact:");
        assertThat(read(target.resolve("config/private.yaml"))).contains("photo:");
        assertThat(read(target.resolve("config/private.yaml"))).contains("signature:");
        assertThat(read(target.resolve("config/settings.yaml"))).doesNotContain("signature:");
        assertThat(read(target.resolve("config/settings.yaml"))).contains("privacy:", "text:");
    }

    private String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
