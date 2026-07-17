package com.dolimcom.semanticrouter.loader;

import com.dolimcom.semanticrouter.api.RouteDefinitionProvider;
import com.dolimcom.semanticrouter.exception.SemanticRouterException;
import com.dolimcom.semanticrouter.model.RouteCorpus;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Supplier;

public class YamlRouteDefinitionProvider implements RouteDefinitionProvider {

    private final String description;
    private final Supplier<InputStream> inputStreamSupplier;
    private final RouteCorpusLoader loader;

    public YamlRouteDefinitionProvider(Path path) {
        this(path.toAbsolutePath().toString(), () -> {
            try {
                return Files.newInputStream(path);
            } catch (IOException ex) {
                throw new SemanticRouterException("Failed to open route corpus: " + path, ex);
            }
        });
    }

    public YamlRouteDefinitionProvider(String description, Supplier<InputStream> inputStreamSupplier) {
        this.description = Objects.requireNonNull(description, "description");
        this.inputStreamSupplier = Objects.requireNonNull(inputStreamSupplier, "inputStreamSupplier");
        this.loader = new RouteCorpusLoader();
    }

    @Override
    public RouteCorpus load() {
        try (InputStream inputStream = inputStreamSupplier.get()) {
            return loader.load(inputStream);
        } catch (IOException ex) {
            throw new SemanticRouterException("Failed to close route corpus input stream", ex);
        }
    }

    @Override
    public String description() {
        return description;
    }
}
