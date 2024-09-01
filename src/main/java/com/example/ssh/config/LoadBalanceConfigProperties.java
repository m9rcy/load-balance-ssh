package com.example.ssh.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@ConfigurationProperties(prefix = "lb")
public record LoadBalanceConfigProperties(
        List<SshClientConfigProperties> sshClients,
        @DefaultValue SshPoolConfigProperties sshPool) {
}
