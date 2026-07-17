package com.dolimcom.semanticrouter.autoconfigure;

import com.dolimcom.semanticrouter.actuate.SemanticRouterEndpoint;
import com.dolimcom.semanticrouter.api.RouteDefinitionProvider;
import com.dolimcom.semanticrouter.api.RoutingEventListener;
import com.dolimcom.semanticrouter.api.SemanticEncoder;
import com.dolimcom.semanticrouter.api.SemanticRouter;
import com.dolimcom.semanticrouter.core.DefaultSemanticRouter;
import com.dolimcom.semanticrouter.core.RouteSnapshotFactory;
import com.dolimcom.semanticrouter.core.RouteSnapshotManager;
import com.dolimcom.semanticrouter.encoder.LocalModelDiscoveryClient;
import com.dolimcom.semanticrouter.encoder.OllamaDiscoveryClient;
import com.dolimcom.semanticrouter.encoder.OllamaEmbeddingEncoder;
import com.dolimcom.semanticrouter.encoder.OpenAiCompatibleDiscoveryClient;
import com.dolimcom.semanticrouter.encoder.OpenAiCompatibleEmbeddingEncoder;
import com.dolimcom.semanticrouter.index.InMemoryRouteIndex;
import com.dolimcom.semanticrouter.index.RouteIndex;
import com.dolimcom.semanticrouter.observability.MicrometerRoutingEventListener;
import com.dolimcom.semanticrouter.observability.StructuredLoggingRoutingEventListener;
import com.dolimcom.semanticrouter.policy.ConfigurableRoutingPolicy;
import com.dolimcom.semanticrouter.policy.HeuristicConfidenceCalibrator;
import com.dolimcom.semanticrouter.policy.RoutingPolicy;
import com.dolimcom.semanticrouter.scoring.CosineSimilarityScorer;
import com.dolimcom.semanticrouter.scoring.KeywordRuleScorer;
import com.dolimcom.semanticrouter.scoring.SimilarityScorer;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;

import java.net.URI;
import java.time.Duration;
import java.util.List;

@AutoConfiguration
@ConditionalOnProperty(prefix = "semantic.router", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SemanticRouterProperties.class)
public class SemanticRouterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    RouteDefinitionProvider routeDefinitionProvider(ResourceLoader resourceLoader, SemanticRouterProperties properties) {
        return new SpringResourceRouteDefinitionProvider(resourceLoader, properties.getRoutesLocation());
    }

    @Bean
    @ConditionalOnMissingBean
    SemanticEncoder semanticEncoder(SemanticRouterProperties properties) {
        URI baseUri = URI.create(properties.getLocalModel().getBaseUrl());
        Duration timeout = properties.getReadTimeout();
        return switch (properties.getLocalModel().getProvider()) {
            case OLLAMA -> new OllamaEmbeddingEncoder(baseUri, properties.getLocalModel().getModel(), timeout);
            case LM_STUDIO, LOCAL_AI, OPENAI_COMPATIBLE -> new OpenAiCompatibleEmbeddingEncoder(
                    baseUri,
                    properties.getLocalModel().getModel(),
                    properties.getLocalModel().getApiKey(),
                    timeout
            );
        };
    }

    @Bean
    @ConditionalOnMissingBean
    LocalModelDiscoveryClient localModelDiscoveryClient(SemanticRouterProperties properties) {
        URI baseUri = URI.create(properties.getLocalModel().getBaseUrl());
        Duration timeout = properties.getReadTimeout();
        return switch (properties.getLocalModel().getProvider()) {
            case OLLAMA -> new OllamaDiscoveryClient(baseUri, timeout);
            case LM_STUDIO -> new OpenAiCompatibleDiscoveryClient("LM_STUDIO", baseUri, properties.getLocalModel().getApiKey(), timeout);
            case LOCAL_AI -> new OpenAiCompatibleDiscoveryClient("LOCAL_AI", baseUri, properties.getLocalModel().getApiKey(), timeout);
            case OPENAI_COMPATIBLE -> new OpenAiCompatibleDiscoveryClient("OPENAI_COMPATIBLE", baseUri, properties.getLocalModel().getApiKey(), timeout);
        };
    }

    @Bean
    @ConditionalOnMissingBean
    SimilarityScorer similarityScorer() {
        return new CosineSimilarityScorer();
    }

    @Bean
    @ConditionalOnMissingBean
    KeywordRuleScorer keywordRuleScorer() {
        return new KeywordRuleScorer();
    }

    @Bean
    @ConditionalOnMissingBean
    RouteIndex routeIndex(SimilarityScorer similarityScorer, KeywordRuleScorer keywordRuleScorer) {
        return new InMemoryRouteIndex(similarityScorer, keywordRuleScorer);
    }

    @Bean
    @ConditionalOnMissingBean
    RoutingPolicy routingPolicy() {
        return new ConfigurableRoutingPolicy(new HeuristicConfidenceCalibrator());
    }

    @Bean
    @ConditionalOnMissingBean
    RoutingEventListener structuredLoggingRoutingEventListener() {
        return new StructuredLoggingRoutingEventListener();
    }

    @Bean
    @ConditionalOnMissingBean(name = "micrometerRoutingEventListener")
    RoutingEventListener micrometerRoutingEventListener(ObjectProvider<MeterRegistry> meterRegistryProvider) {
        MeterRegistry meterRegistry = meterRegistryProvider.getIfAvailable();
        if (meterRegistry == null) {
            return new RoutingEventListener() {
            };
        }
        return new MicrometerRoutingEventListener(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    RouteSnapshotFactory routeSnapshotFactory() {
        return new RouteSnapshotFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    RouteSnapshotManager routeSnapshotManager(
            RouteDefinitionProvider routeDefinitionProvider,
            SemanticEncoder semanticEncoder,
            RouteSnapshotFactory routeSnapshotFactory,
            ObjectProvider<RoutingEventListener> listenersProvider
    ) {
        List<RoutingEventListener> listeners = listenersProvider.orderedStream().toList();
        return new RouteSnapshotManager(routeDefinitionProvider, semanticEncoder, routeSnapshotFactory, listeners);
    }

    @Bean
    @ConditionalOnMissingBean
    SemanticRouter semanticRouter(
            SemanticEncoder semanticEncoder,
            RouteSnapshotManager routeSnapshotManager,
            RouteIndex routeIndex,
            RoutingPolicy routingPolicy,
            ObjectProvider<RoutingEventListener> listenersProvider
    ) {
        List<RoutingEventListener> listeners = listenersProvider.orderedStream().toList();
        return new DefaultSemanticRouter(semanticEncoder, routeSnapshotManager, routeIndex, routingPolicy, listeners);
    }

    @Bean
    @ConditionalOnMissingBean
    SemanticRouterEndpoint semanticRouterEndpoint(RouteSnapshotManager routeSnapshotManager) {
        return new SemanticRouterEndpoint(routeSnapshotManager);
    }
}
