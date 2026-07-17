package com.modelroute.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.modelroute.dto.ModelCatalogEntry;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class ModelCatalogService {

    private final List<ModelCatalogEntry> entries;

    public ModelCatalogService() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream input = new ClassPathResource("model-catalog.yml").getInputStream()) {
            CatalogFile catalog = mapper.readValue(input, CatalogFile.class);
            this.entries = catalog.models() == null ? List.of() : List.copyOf(catalog.models());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load model catalog", exception);
        }
    }

    public List<ModelCatalogEntry> list() {
        return entries;
    }

    private record CatalogFile(List<ModelCatalogEntry> models) {
    }
}
