package io.github.mattiacozzolino.cvascode.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public final class ConfigurationModel {
    private ConfigurationModel() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CandidateRoot(Candidate candidate) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Candidate(
            String firstName,
            String lastName,
            LocalizedText location,
            LocalizedText professionalTitle,
            LocalizedText positioning,
            Photo photo
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContactInfo(String email, String phone, String linkedin, String github) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PrivateRoot(ContactInfo contact, Photo photo, Signature signature) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Photo(String path, LocalizedText alt) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Signature(Boolean enabled, String name, String imagePath) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LocalizedText(String it, String en) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResumeRoot(Resume resume) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Resume(
            LocalizedText summary,
            List<SkillCategory> expertise,
            List<Experience> experiences,
            List<Certification> certifications,
            List<Education> education,
            List<LanguageSkill> languages,
            List<AdditionalTraining> additionalTraining
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SkillCategory(LocalizedText category, List<String> items) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Experience(
            String id,
            String company,
            String displayMode,
            PaginationHint pagination,
            LocalizedText role,
            String startDate,
            String endDate,
            LocalizedText location,
            LocalizedText context,
            List<Highlight> highlights,
            List<Project> projects,
            List<String> technologies
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Project(
            String name,
            PaginationHint pagination,
            LocalizedText domain,
            LocalizedText label,
            LocalizedText description,
            List<Highlight> highlights,
            List<String> technologies
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PaginationHint(Boolean keepTogether, Boolean allowSplit, Boolean preferredBreakBefore, Integer priority) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Highlight(String it, String en) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Certification(String name, String issuer, String date) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Education(LocalizedText title, String institution, String startDate, String endDate, String details) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LanguageSkill(LocalizedText name, LocalizedText level) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AdditionalTraining(String name, String issuer, String date, Boolean visibleByDefault) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CoverLetterRoot(CoverLetter coverLetter) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CoverLetter(DefaultCoverLetter generic, @JsonProperty("default") DefaultCoverLetter defaultContent, List<Application> applications) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DefaultCoverLetter(
            LocalizedText subject,
            LocalizedText opening,
            LocalizedText valueProposition,
            LocalizedText professionalValue,
            LocalizedText concreteExperience,
            LocalizedText motivation,
            LocalizedText closing
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Application(
            String id,
            String company,
            LocalizedText role,
            LocalizedText recipient,
            LocalizedText companyReason,
            LocalizedText roleReason,
            List<Highlight> matchingPoints
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SettingsRoot(DocumentSettings document, ThemeSettings theme, OutputSettings output, Map<String, SectionSettings> sections) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DocumentSettings(ResumeSettings resume, CoverLetterSettings coverLetter) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResumeSettings(Integer maxPages, Map<String, Boolean> includePhoto, String generationMode) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CoverLetterSettings(Integer maxPages, String defaultMode) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ThemeSettings(String accentColor) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OutputSettings(String fileNamePattern) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SectionSettings(Boolean enabled) {
    }
}
