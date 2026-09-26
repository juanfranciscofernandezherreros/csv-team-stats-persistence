package com.example.csvteamstats.service;

import org.springframework.dao.TransientDataAccessException;

public class ConcurrentUpsertConflictException extends TransientDataAccessException {
    public ConcurrentUpsertConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
