package com.example.ssh.config;

import org.springframework.boot.context.properties.bind.DefaultValue;

public record SshPoolConfigProperties(
        @DefaultValue("5") int maxIdle,
        @DefaultValue("2") int minIdle,
        @DefaultValue("10") int maxActive) {
}
