package io.github.mattiacozzolino.cvascode.render;

import io.github.mattiacozzolino.cvascode.domain.DocumentKind;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

public final class TemplateRenderer {
    private final TemplateEngine templateEngine;

    public TemplateRenderer() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(resolver);
    }

    public String render(DocumentKind kind, Map<String, Object> model) {
        Context context = new Context(Locale.forLanguageTag(String.valueOf(model.get("languageTag"))));
        context.setVariables(model);
        context.setVariable("css", stylesheet(kind));
        return templateEngine.process(templateName(kind), context);
    }

    private String templateName(DocumentKind kind) {
        return kind == DocumentKind.RESUME ? "resume" : "cover-letter";
    }

    private String stylesheet(DocumentKind kind) {
        String resource = kind == DocumentKind.RESUME ? "styles/resume.css" : "styles/cover-letter.css";
        try (var input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalStateException("Missing stylesheet: " + resource);
            }
            return fontFace() + "\n" + new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String fontFace() {
        try (var input = Thread.currentThread().getContextClassLoader().getResourceAsStream("fonts/InterVariable.ttf")) {
            if (input == null) {
                throw new IllegalStateException("Missing bundled font: fonts/InterVariable.ttf");
            }
            String encoded = Base64.getEncoder().encodeToString(input.readAllBytes());
            return "@font-face { font-family: 'Inter'; font-style: normal; font-weight: 100 900; "
                    + "font-display: block; src: url('data:font/ttf;base64," + encoded + "') format('truetype'); }";
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
