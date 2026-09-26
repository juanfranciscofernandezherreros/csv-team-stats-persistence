package com.example.csvteamstats.config;

import com.example.csvteamstats.service.ConcurrentUpsertConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessResourceException;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaErrorClassifierTest {
    private final KafkaErrorClassifier classifier = new KafkaErrorClassifier();

    @Test
    void treatsInvalidTeamStatsAsNonRetryable() {
        assertThat(classifier.isRetryable(new IllegalArgumentException("invalid team stats"))).isFalse();
        assertThat(classifier.isRetryable(new DataIntegrityViolationException("generic constraint"))).isFalse();
    }

    @Test
    void treatsConcurrentUniqueConstraintConflictAsRetryable() {
        assertThat(classifier.isRetryable(
                new ConcurrentUpsertConflictException(
                        "concurrent upsert",
                        new DataIntegrityViolationException("duplicate"))))
                .isTrue();
    }

    @Test
    void treatsTransientDatabaseFailureAsRetryable() {
        assertThat(classifier.isRetryable(new TransientDataAccessResourceException("db unavailable"))).isTrue();
    }
}
