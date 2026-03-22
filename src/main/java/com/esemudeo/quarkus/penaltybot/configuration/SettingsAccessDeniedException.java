package com.esemudeo.quarkus.penaltybot.configuration;

public class SettingsAccessDeniedException extends RuntimeException {
    public SettingsAccessDeniedException(String message) {
        super(message);
    }
}
