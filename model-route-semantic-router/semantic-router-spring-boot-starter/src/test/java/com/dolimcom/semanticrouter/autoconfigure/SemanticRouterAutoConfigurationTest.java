package com.dolimcom.semanticrouter.autoconfigure;

import com.dolimcom.semanticrouter.api.SemanticEncoder;
import com.dolimcom.semanticrouter.api.SemanticRouter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SemanticRouterAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SemanticRouterAutoConfiguration.class))
            .withUserConfiguration(TestConfiguration.class)
            .withPropertyValues(
                    "semantic.router.enabled=true",
                    "semantic.router.startup-mode=FAIL_FAST",
                    "semantic.router.routes-location=classpath:semantic-router/routes.yml"
            );

    @Test
    void shouldAutoConfigureSemanticRouter() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(SemanticRouter.class);
            assertThat(context).hasBean("semanticRouterEndpoint");
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class TestConfiguration {

        @Bean
        SemanticEncoder semanticEncoder() {
            return new SemanticEncoder() {
                @Override
                public List<double[]> encodeAll(List<String> texts) {
                    return texts.stream().map(text -> new double[]{1.0d, 0.0d}).toList();
                }

                @Override
                public String version() {
                    return "test";
                }
            };
        }
    }
}
