package com.example.csvteamstats.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class KafkaErrorClassifier {
    static final String UNIQUE_VIOLATION_SQL_STATE = "23505";

    public Map<Class<? extends Throwable>, Boolean> classifications() {
        Map<Class<? extends Throwable>, Boolean> classifications = new LinkedHashMap<>();
        classifications.put(IllegalArgumentException.class, false);
        classifications.put(TransientDataAccessException.class, true);
        return classifications;
    }

    public boolean isRetryable(Throwable error) {
        if (error instanceof DataIntegrityViolationException) {
            return isUniqueConstraintConflict(error);
        }

        for (Map.Entry<Class<? extends Throwable>, Boolean> entry : classifications().entrySet()) {
            if (entry.getKey().isInstance(error)) {
                return entry.getValue();
            }
        }
        return true;
    }

    private boolean isUniqueConstraintConflict(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof SQLException sqlException
                    && UNIQUE_VIOLATION_SQL_STATE.equals(sqlException.getSQLState())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
