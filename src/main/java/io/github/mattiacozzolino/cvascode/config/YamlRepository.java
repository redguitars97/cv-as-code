package io.github.mattiacozzolino.cvascode.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.mattiacozzolino.cvascode.domain.ConfigurationModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class YamlRepository {
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper mapper;
    private final Path configDirectory;

    public YamlRepository(Path configDirectory) {
        this.configDirectory = configDirectory;
        this.mapper = new ObjectMapper(new YAMLFactory())
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public Map<String, Object> candidate() {
        return readRequired(configDirectory.resolve("candidate.yaml"));
    }

    public ConfigurationModel.CandidateRoot candidateModel() {
        return convert(candidate(), ConfigurationModel.CandidateRoot.class, "config/candidate.yaml");
    }

    public Map<String, Object> resume() {
        return readRequired(configDirectory.resolve("resume.yaml"));
    }

    public ConfigurationModel.ResumeRoot resumeModel() {
        return convert(resume(), ConfigurationModel.ResumeRoot.class, "config/resume.yaml");
    }

    public Map<String, Object> privateConfig(Path privateConfig) {
        return readRequired(privateConfig == null ? configDirectory.resolve("private.yaml") : privateConfig);
    }

    public ConfigurationModel.PrivateRoot privateModel(Path privateConfig) {
        return convert(privateConfig(privateConfig), ConfigurationModel.PrivateRoot.class, "config/private.yaml");
    }

    public Map<String, Object> coverLetter() {
        return readRequired(configDirectory.resolve("cover-letter.yaml"));
    }

    public ConfigurationModel.CoverLetterRoot coverLetterModel() {
        return convert(coverLetter(), ConfigurationModel.CoverLetterRoot.class, "config/cover-letter.yaml");
    }

    public Map<String, Object> settings() {
        return readRequired(configDirectory.resolve("settings.yaml"));
    }

    public ConfigurationModel.SettingsRoot settingsModel() {
        return convert(settings(), ConfigurationModel.SettingsRoot.class, "config/settings.yaml");
    }

    public Map<String, Object> application(Path applicationConfig) {
        if (applicationConfig == null || !Files.exists(applicationConfig)) {
            return new LinkedHashMap<>();
        }
        return readRequired(applicationConfig);
    }

    private Map<String, Object> readRequired(Path path) {
        if (!Files.exists(path)) {
            throw new ConfigurationException("Missing configuration file: " + path
                    + ". Create it with `java -jar cv-as-code.jar init --target ./my-resume` or copy the matching example.");
        }
        try {
            Map<String, Object> value = mapper.readValue(path.toFile(), MAP_TYPE);
            return value == null ? new LinkedHashMap<>() : value;
        } catch (IOException e) {
            throw new ConfigurationException("Invalid YAML file: " + path
                    + ". Check indentation, quotes, and list markers.", e);
        }
    }

    public Path configDirectory() {
        return configDirectory;
    }

    private <T> T convert(Map<String, Object> value, Class<T> type, String source) {
        try {
            return mapper.convertValue(value, type);
        } catch (IllegalArgumentException e) {
            throw new ConfigurationException("Invalid typed configuration model for " + source + ".", e);
        }
    }
}
