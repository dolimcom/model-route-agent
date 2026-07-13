package com.modelroute.provider;

import static org.assertj.core.api.Assertions.assertThat;

import com.modelroute.config.ModelRouteProperties;
import java.util.List;
import org.junit.jupiter.api.Test;

class ModelProviderDispatcherTest {

    @Test
    void dispatchesToMockProvider() {
        ModelProviderDispatcher dispatcher = new ModelProviderDispatcher(List.of(new MockModelProviderClient()));
        ModelRouteProperties.ModelDefinition model = new ModelRouteProperties.ModelDefinition();
        model.setId("test-mock");
        model.setProvider("mock");
        model.setEnabled(true);

        ProviderResponse response = dispatcher.complete(model, List.of(ChatMessage.user("hello")));

        assertThat(response.content()).contains("test-mock");
    }
}
