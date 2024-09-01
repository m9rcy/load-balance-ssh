package com.example.ssh.config;

import org.springframework.boot.context.properties.bind.DefaultValue;

public record SshClientConfigProperties(
        String id,
        String host,
        @DefaultValue("22")
        int port,
        String username,
        String password,
        String privateKeyPath,
        @DefaultValue
        RetryConfigProperties retryConfig) {
}
