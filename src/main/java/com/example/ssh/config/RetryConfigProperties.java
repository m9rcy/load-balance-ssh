package com.example.ssh.config;

import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

public record RetryConfigProperties(
        @DefaultValue("3") int maxAttempts,
        @DefaultValue("100ms") Duration backoff) {
}
