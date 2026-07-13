package com.modelroute.provider;

import com.modelroute.config.ModelRouteProperties;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Zero-cost provider used by the task-specific mock models and automated tests.
 */
@Component
public class MockModelProviderClient implements ModelProviderClient {

    @Override
    public String providerType() {
        return "mock";
    }

    @Override
    public ProviderResponse complete(ModelRouteProperties.ModelDefinition model, List<ChatMessage> messages) {
        return new ProviderResponse("[Mock response] Request routed to " + model.getId()
                + ". Real provider integration will be added in a later iteration.");
    }
}
