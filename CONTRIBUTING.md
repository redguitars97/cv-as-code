# Contributing

Thanks for considering a contribution to CV as Code.

## Development Setup

Requirements:

- Java 17 or newer.
- Maven Wrapper from this repository.
- Playwright Chromium for E2E PDF generation.

```bash
./mvnw -q exec:java \
  -Dexec.mainClass=com.microsoft.playwright.CLI \
  -Dexec.args="install chromium"
```

## Build And Test

Run unit tests:

```bash
./mvnw test
```

Run the full verification suite:

```bash
./mvnw verify -Dcv.e2e=true
```

The E2E suite generates sample PDFs and validates them with PDFBox.

## Project Boundaries

- Keep the project a CLI.
- Do not introduce a web app, database or server runtime for core generation.
- Keep YAML as the candidate-facing configuration interface.
- Keep templates ATS-conscious: linear DOM order, selectable text, no image-based text.
- Avoid candidate-specific layout rules in Java, HTML or CSS.

## Repository Hygiene

Do not commit:

- `target/`
- `output/`
- `demo-output/`
- `config/private.yaml`
- IDE metadata
- local Playwright browser output

Versioned preview images must be anonymized and generated from `test-fixtures/tizio-caio`.

## Pull Request Checklist

- [ ] `./mvnw test` passes.
- [ ] `./mvnw verify -Dcv.e2e=true` passes when the change affects rendering, validation, fixtures or CLI behavior.
- [ ] README examples still work.
- [ ] No private contact data is committed.
- [ ] Generated PDFs remain extractable and within configured page limits.
