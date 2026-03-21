package com.esemudeo.quarkus.penaltybot.web;

public class SettingsAccessDeniedException extends RuntimeException {
    public SettingsAccessDeniedException(String message) {
        super(message);
    }
}