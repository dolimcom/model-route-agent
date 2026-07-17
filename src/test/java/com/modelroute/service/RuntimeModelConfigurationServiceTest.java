package com.modelroute.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.modelroute.config.ModelRouteProperties;
import com.modelroute.config.RuntimeConfigProperties;
import com.modelroute.domain.TaskType;
import com.modelroute.provider.ProviderTypeResolver;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RuntimeModelConfigurationServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void restoresCredentialsOmittedByLegacyRuntimeFileAndPersistsThem() throws Exception {
        Path runtimeFile = tempDir.resolve("models.local.yml");
        Files.writeString(runtimeFile, runtimeYaml("https://api.deepseek.com"));
        RegistryFixture fixture = registry("https://api.deepseek.com", "test-secret");

        RuntimeModelConfigurationService service = service(fixture, runtimeFile);
        service.initialize();

        assertThat(fixture.registry().getRegisteredModels())
                .allSatisfy(model -> assertThat(model.getApiKey()).isEqualTo("test-secret"));
        assertThat(Files.readString(runtimeFile))
                .contains("apiKey: \"test-secret\"");
    }

    @Test
    void doesNotReuseCredentialForDifferentBaseUrl() throws Exception {
        Path runtimeFile = tempDir.resolve("models.local.yml");
        Files.writeString(runtimeFile, runtimeYaml("https://different.example.com"));
        RegistryFixture fixture = registry("https://api.deepseek.com", "test-secret");

        RuntimeModelConfigurationService service = service(fixture, runtimeFile);

        assertThatThrownBy(service::initialize)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Model api-key must not be blank for general");
    }

    private RuntimeModelConfigurationService service(RegistryFixture fixture, Path runtimeFile) {
        RuntimeConfigProperties runtimeProperties = new RuntimeConfigProperties();
        runtimeProperties.setModelsFile(runtimeFile.toString());
        return new RuntimeModelConfigurationService(
                fixture.properties(), runtimeProperties, fixture.registry(), new ProviderTypeResolver());
    }

    private RegistryFixture registry(String baseUrl, String apiKey) {
        ModelRouteProperties properties = new ModelRouteProperties();
        ModelRouteProperties.ModelDefinition model = model(
                "bootstrap", baseUrl, apiKey, Arrays.asList(TaskType.values()));
        properties.setModels(List.of(model));

        ModelRouteProperties.Router router = new ModelRouteProperties.Router();
        router.setFallbackModelId("bootstrap");
        router.setKeywords(Map.of(TaskType.GENERAL, List.of("help")));
        router.setSemanticMappings(Map.of("general", mapping(TaskType.GENERAL, "bootstrap")));
        properties.setRouter(router);

        ModelRegistry registry = new ModelRegistry(properties);
        registry.afterPropertiesSet();
        return new RegistryFixture(properties, registry);
    }

    private ModelRouteProperties.ModelDefinition model(
            String id,
            String baseUrl,
            String apiKey,
            List<TaskType> tasks) {
        ModelRouteProperties.ModelDefinition model = new ModelRouteProperties.ModelDefinition();
        model.setId(id);
        model.setDisplayName("DeepSeek");
        model.setProvider("openai-compatible");
        model.setSupportedTasks(tasks);
        model.setEnabled(true);
        model.setBaseUrl(baseUrl);
        model.setApiKey(apiKey);
        model.setModelName("deepseek-chat");
        model.setTimeoutMs(15000);
        model.setMaxTokens(1024);
        model.setTemperature(0.7);
        return model;
    }

    private ModelRouteProperties.SemanticRouteMapping mapping(TaskType taskType, String modelId) {
        ModelRouteProperties.SemanticRouteMapping mapping = new ModelRouteProperties.SemanticRouteMapping();
        mapping.setTaskType(taskType);
        mapping.setModelId(modelId);
        return mapping;
    }

    private String runtimeYaml(String baseUrl) {
        StringBuilder yaml = new StringBuilder("models:\n");
        for (TaskType taskType : TaskType.values()) {
            String id = taskType.name().toLowerCase();
            yaml.append("- id: ").append(id).append('\n')
                    .append("  displayName: DeepSeek\n")
                    .append("  provider: openai-compatible\n")
                    .append("  supportedTasks: [").append(taskType.name()).append("]\n")
                    .append("  enabled: true\n")
                    .append("  baseUrl: ").append(baseUrl).append('\n')
                    .append("  modelName: deepseek-chat\n")
                    .append("  timeoutMs: 15000\n")
                    .append("  maxTokens: 1024\n")
                    .append("  temperature: 0.7\n");
        }
        return yaml.toString();
    }

    private record RegistryFixture(ModelRouteProperties properties, ModelRegistry registry) {
    }
}
