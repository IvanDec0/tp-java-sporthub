package com.java.sportshub.config;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {
    private final LocalDateTime timestamp;
    private final String message;
    private final int status;
    private final String error;

    public ErrorResponse(LocalDateTime timestamp, String message, int status, String error) {
        this.timestamp = timestamp;
        this.message = message;
        this.status = status;
        this.error = error;
    }
}