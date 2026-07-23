package com.dolimcom.semanticrouter.loader;

import com.dolimcom.semanticrouter.exception.SemanticRouterException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RouteCorpusLoaderTest {

    private final RouteCorpusLoader loader = new RouteCorpusLoader();

    @Test
    void rejectsInvalidThresholdOrdering() {
        assertThatThrownBy(() -> load("""
                policy:
                  minScore: 0.4
                  outOfDomainThreshold: 0.5
                routes:
                  - routeId: general
                    target: model
                    utterances: [hello]
                """))
                .isInstanceOf(SemanticRouterException.class)
                .hasMessageContaining("outOfDomainThreshold");
    }

    @Test
    void rejectsUnknownDefaultFallbackRoute() {
        assertThatThrownBy(() -> load("""
                policy:
                  fallback:
                    lowConfidence: DEFAULT_ROUTE
                    defaultRouteId: missing
                routes:
                  - routeId: general
                    target: model
                    utterances: [hello]
                """))
                .isInstanceOf(SemanticRouterException.class)
                .hasMessageContaining("defaultRouteId");
    }

    private void load(String yaml) {
        loader.load(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
    }
}
