package com.dolimcom.semanticrouter.autoconfigure;

import com.dolimcom.semanticrouter.loader.YamlRouteDefinitionProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

class SpringResourceRouteDefinitionProvider extends YamlRouteDefinitionProvider {

    SpringResourceRouteDefinitionProvider(ResourceLoader resourceLoader, String location) {
        super(location, () -> {
            try {
                Resource resource = resourceLoader.getResource(location);
                return resource.getInputStream();
            } catch (IOException ex) {
                throw new IllegalStateException("Failed to load route resource: " + location, ex);
            }
        });
    }
}
