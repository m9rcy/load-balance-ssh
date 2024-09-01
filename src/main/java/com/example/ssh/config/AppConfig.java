package com.example.ssh.config;

import com.example.ssh.pool.SshClientConnectionPool;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@Configuration
@EnableConfigurationProperties(LoadBalanceConfigProperties.class)
public class AppConfig {

    @Bean
    public SshClientConnectionPool sshClientConnectionPool(LoadBalanceConfigProperties props) {
        return new SshClientConnectionPool(props.sshClients(), props.sshPool());
    }

}