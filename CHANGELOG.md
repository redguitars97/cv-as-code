# Changelog

## 1.0.0 - 2026-08-07

Initial stable release.

### Changed

- Promoted the final release candidate to `1.0.0`.
- Polished company-project headings without hardcoding candidate-specific names.
- Switched cover-letter dates to natural localized formatting.

## 1.0.0-rc.2 - 2026-08-06

Final release-candidate hardening pass.

### Changed

- Removed the standalone `docs/` tree and consolidated public guidance into the README.
- Moved anonymized preview images to `assets/previews/`.
- Tightened README flow around generation, candidate setup, rendering, ATS validation and contribution basics.
- Polished resume contact rendering as a compact ATS-safe micro-grid.
- Added official relative-path photo support with repository placeholder fallback.
- Added optional YAML-driven signature and localized privacy footer support.
- Moved signature configuration to `private.yaml`; `settings.yaml` remains reserved for public document behavior.
- Moved photo path ownership fully to `private.yaml`; `candidate.yaml` now contains only public candidate identity.
- Removed embedded application examples from `cover-letter.yaml`; custom applications live in dedicated application YAML files.
- Removed the redundant `document.resume.showAdditionalTraining` setting; `sections.additionalTraining.enabled` is now the single section toggle.
- Set project version to `1.0.0-rc.2`.

## 1.0.0-rc.1 - 2026-08-06

First release candidate.

### Added

- Java CLI for generating CV and cover-letter PDFs from YAML.
- Italian and English document generation.
- Candidate initialization command.
- Production-mode placeholder validation.
- Playwright/Chromium HTML-to-PDF rendering.
- PDFBox inspection and JSON validation reports.
- Anonymized Tizio Caio fixture for reuse and regression testing.
- README preview images generated from the anonymized fixture.

### Changed

- Consolidated the default CV template into a single editorial, ATS-conscious layout.
- Prepared public documentation for open-source release.
- Set project version to `1.0.0-rc.1`.

### Notes

- Generated folders such as `target/`, `output/` and `demo-output/` are intentionally ignored.
- Private contact data belongs in `config/private.yaml`, which is ignored by Git.
