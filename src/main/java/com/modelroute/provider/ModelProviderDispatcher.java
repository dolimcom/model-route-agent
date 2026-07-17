package com.modelroute.provider;

import com.modelroute.config.ModelRouteProperties;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

/**
 * Resolves the configured provider name to its protocol adapter.
 */
@Service
public class ModelProviderDispatcher {

    private static final Logger log = LoggerFactory.getLogger(ModelProviderDispatcher.class);

    private final Map<String, ModelProviderClient> clientsByProvider;

    public ModelProviderDispatcher(List<ModelProviderClient> clients) {
        this.clientsByProvider = clients.stream().collect(Collectors.toUnmodifiableMap(
                client -> normalize(client.providerType()), Function.identity(), (first, second) -> {
                    throw new IllegalStateException("Duplicate provider client: " + first.providerType());
                }));
    }

    public ProviderResponse complete(
            ModelRouteProperties.ModelDefinition model,
            List<ChatMessage> messages) {
        if (!model.isEnabled()) {
            throw new ModelProviderException("Configured model is disabled: " + model.getId());
        }
        ModelProviderClient client = clientsByProvider.get(normalize(model.getProvider()));
        if (client == null) {
            throw new ModelProviderException("No provider client is registered for: " + model.getProvider());
        }
        long startedAt = System.nanoTime();
        try {
            ProviderResponse response = client.complete(model, messages);
            log.info("Provider completion succeeded: model={}, provider={}, elapsedMs={}",
                    model.getId(), model.getProvider(), elapsedMillis(startedAt));
            return response;
        } catch (RuntimeException exception) {
            log.warn("Provider completion failed: model={}, provider={}, elapsedMs={}, error={}",
                    model.getId(), model.getProvider(), elapsedMillis(startedAt), exception.getMessage());
            throw exception;
        }
    }

    public Flux<String> stream(
            ModelRouteProperties.ModelDefinition model,
            List<ChatMessage> messages) {
        if (!model.isEnabled()) {
            return Flux.error(new ModelProviderException("Configured model is disabled: " + model.getId()));
        }
        ModelProviderClient client = clientsByProvider.get(normalize(model.getProvider()));
        if (client == null) {
            return Flux.error(new ModelProviderException(
                    "No provider client is registered for: " + model.getProvider()));
        }
        return Flux.defer(() -> {
            long startedAt = System.nanoTime();
            return client.stream(model, messages)
                    .doOnComplete(() -> log.info(
                            "Provider stream completed: model={}, provider={}, elapsedMs={}",
                            model.getId(), model.getProvider(), elapsedMillis(startedAt)))
                    .doOnError(exception -> log.warn(
                            "Provider stream failed: model={}, provider={}, elapsedMs={}, error={}",
                            model.getId(), model.getProvider(), elapsedMillis(startedAt), exception.getMessage()));
        });
    }

    private static String normalize(String provider) {
        return provider.toLowerCase(Locale.ROOT).trim();
    }

    private static long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000L;
    }
}
