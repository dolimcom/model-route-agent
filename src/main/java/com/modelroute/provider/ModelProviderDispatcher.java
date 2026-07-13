package com.modelroute.provider;

import com.modelroute.config.ModelRouteProperties;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Resolves the configured provider name to its protocol adapter.
 */
@Service
public class ModelProviderDispatcher {

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
        return client.complete(model, messages);
    }

    private static String normalize(String provider) {
        return provider.toLowerCase(Locale.ROOT).trim();
    }
}
