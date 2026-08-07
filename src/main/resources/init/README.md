# My Resume Workspace

Edit these files:

- `config/candidate.yaml`: name, title, positioning and location.
- `config/private.yaml`: real email, phone, LinkedIn, GitHub, optional private asset paths and signature. Keep it private.
- `config/resume.yaml`: summary, expertise, experiences, projects, certifications, education and languages.
- `config/cover-letter.yaml`: generic cover letter text.
- `config/application.example.yaml`: example for a customized application.
- `config/settings.yaml`: page limits, photo defaults, sections and output naming.

Validate and generate:

```bash
CV_AS_CODE_JAR="/path/to/cv-as-code.jar"

java -jar "$CV_AS_CODE_JAR" validate --config ./config
java -jar "$CV_AS_CODE_JAR" generate-all --config ./config --output ./output
java -jar "$CV_AS_CODE_JAR" preview --pdf ./output/Name_Surname_CV_IT.pdf --output ./output/preview
```
