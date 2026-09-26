package com.example.csvteamstats.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;

import java.util.LinkedHashMap;
import java.util.Map;

public class KafkaErrorClassifier {
    public Map<Class<? extends Throwable>, Boolean> classifications() {
        Map<Class<? extends Throwable>, Boolean> classifications = new LinkedHashMap<>();
        classifications.put(IllegalArgumentException.class, false);
        classifications.put(DataIntegrityViolationException.class, false);
        classifications.put(TransientDataAccessException.class, true);
        return classifications;
    }

    public boolean isRetryable(Throwable error) {
        for (Map.Entry<Class<? extends Throwable>, Boolean> entry : classifications().entrySet()) {
            if (entry.getKey().isInstance(error)) return entry.getValue();
        }
        return true;
    }
}
