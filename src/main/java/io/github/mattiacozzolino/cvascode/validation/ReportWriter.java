package io.github.mattiacozzolino.cvascode.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.mattiacozzolino.cvascode.domain.PdfReport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ReportWriter {
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public void write(PdfReport report, Path target) {
        try {
            Files.createDirectories(target.getParent());
            mapper.writeValue(target.toFile(), report);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write report: " + target, e);
        }
    }
}
