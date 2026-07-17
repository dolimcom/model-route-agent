package com.dolimcom.semanticrouter.scoring;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CosineSimilarityScorerTest {

    private final CosineSimilarityScorer scorer = new CosineSimilarityScorer();

    @Test
    void shouldReturnOneForIdenticalVectors() {
        assertThat(scorer.score(new double[]{1.0d, 0.0d}, new double[]{1.0d, 0.0d})).isEqualTo(1.0d);
    }

    @Test
    void shouldHandleZeroVector() {
        assertThat(scorer.score(new double[]{0.0d, 0.0d}, new double[]{1.0d, 0.0d})).isZero();
    }
}
