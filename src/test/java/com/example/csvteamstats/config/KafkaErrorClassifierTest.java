package com.example.csvteamstats.config;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessResourceException;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaErrorClassifierTest {
    private final KafkaErrorClassifier classifier = new KafkaErrorClassifier();

    @Test
    void treatsInvalidTeamStatsAsNonRetryable() {
        assertThat(classifier.isRetryable(new IllegalArgumentException("invalid team stats"))).isFalse();
        assertThat(classifier.isRetryable(new DataIntegrityViolationException("constraint"))).isFalse();
    }

    @Test
    void treatsTransientDatabaseFailureAsRetryable() {
        assertThat(classifier.isRetryable(new TransientDataAccessResourceException("db unavailable"))).isTrue();
    }
}
