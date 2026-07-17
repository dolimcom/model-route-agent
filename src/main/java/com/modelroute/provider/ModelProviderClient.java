package com.modelroute.provider;

import com.modelroute.config.ModelRouteProperties;
import java.util.List;
import reactor.core.publisher.Flux;

/**
 * Adapter contract for a concrete LLM provider protocol.
 */
public interface ModelProviderClient {

    String providerType();

    ProviderResponse complete(ModelRouteProperties.ModelDefinition model, List<ChatMessage> messages);

    Flux<String> stream(ModelRouteProperties.ModelDefinition model, List<ChatMessage> messages);
}
