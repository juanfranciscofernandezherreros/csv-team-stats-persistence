package com.example.csvteamstats.config;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessResourceException;

import java.sql.SQLException;

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
        SQLException uniqueViolation =
                new SQLException("duplicate key", "23505");
        DataIntegrityViolationException conflict =
                new DataIntegrityViolationException("concurrent upsert", uniqueViolation);

        assertThat(classifier.isRetryable(conflict)).isTrue();
    }

    @Test
    void treatsTransientDatabaseFailureAsRetryable() {
        assertThat(classifier.isRetryable(new TransientDataAccessResourceException("db unavailable"))).isTrue();
    }
}
