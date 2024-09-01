package com.example.ssh.pool;

import com.example.ssh.config.SshClientConfigProperties;
import com.example.ssh.config.SshPoolConfigProperties;
import com.example.ssh.exception.SshPoolException;
import com.jcraft.jsch.Session;
import jakarta.annotation.PreDestroy;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SshClientConnectionPool {
    private static final Logger LOG = LoggerFactory.getLogger(SshClientConnectionPool.class.getName());
    private final GenericObjectPool<Session> pool;

    public SshClientConnectionPool(List<SshClientConfigProperties> sshClientProps, @DefaultValue SshPoolConfigProperties sshPoolProp) {
        SshClientFactory sshClientFactory = new SshClientFactory(sshClientProps);
        GenericObjectPoolConfig<Session> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(sshPoolProp.maxActive()); // Max number of connections
        config.setMinIdle(sshPoolProp.minIdle());   // Min number of idle connections
        config.setMaxIdle(sshPoolProp.maxIdle());   // Max number of idle connections
        config.setTestOnBorrow(true);  // Test connections on borrow
        config.setTestOnReturn(true);  // Test connections on return

        pool = new GenericObjectPool<>(sshClientFactory, config);
    }

    public Session borrowSession() throws SshPoolException {
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            LOG.error("Error borrowing SSH session from pool", e);
            throw new SshPoolException("Failed to borrow SSH session from the pool", e);
        }
    }

    public void returnSession(Session session) {
        try {
            if (session != null) {
                pool.returnObject(session);
            }
        } catch (Exception e) {
            LOG.error("Error returning SSH session to pool", e);
        }
    }

    @PreDestroy
    public void close() {
        pool.close();
    }
}
