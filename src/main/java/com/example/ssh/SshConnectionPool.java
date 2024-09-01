package com.example.ssh;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.retry.annotation.Retryable;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SshConnectionPool {

    private static final Logger logger = Logger.getLogger(SshConnectionPool.class.getName());
    private GenericObjectPool<Session> pool;

    public SshConnectionPool(List<String> hosts, int port, String username, String password) {
        SshSessionFactory factory = new SshSessionFactory(hosts, port, username, password, null);
        GenericObjectPoolConfig<Session> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(10); // Max number of connections
        config.setMinIdle(2);   // Min number of idle connections
        config.setMaxIdle(5);   // Max number of idle connections
        config.setTestOnBorrow(true);  // Test connections on borrow
        config.setTestOnReturn(true);  // Test connections on return

        pool = new GenericObjectPool<>(factory, config);
    }

    public SshConnectionPool(List<String> hosts, int port, String username, String password, JSch jSch) {
        SshSessionFactory factory = new SshSessionFactory(hosts, port, username, password, jSch);
        GenericObjectPoolConfig<Session> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(10); // Max number of connections
        config.setMinIdle(2);   // Min number of idle connections
        config.setMaxIdle(5);   // Max number of idle connections
        config.setTestOnBorrow(true);  // Test connections on borrow
        config.setTestOnReturn(true);  // Test connections on return
        config.setBlockWhenExhausted(false);
        //config.setBlockWhenExhausted(true);
        //config.setMaxWait(Duration.ofMillis(120*1000));
        //config.setTimeBetweenEvictionRuns(Duration.ofMillis(120*1000));

        pool = new GenericObjectPool<>(factory, config);
    }

    @Retryable
    public Session borrowSession() throws Exception {
        try {
            if (pool == null || pool.isClosed()) {
                throw new IllegalStateException("Pool not initialized or closed");
            }
            return pool.borrowObject();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error borrowing SSH session from pool", e);
            throw new Exception("Failed to borrow SSH session from the pool", e);
        }
    }

    public void returnSession(Session session) {
        try {
            if (session != null && pool != null && !pool.isClosed()) {
                pool.returnObject(session);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error returning SSH session to pool", e);
        }
    }

    public void close() {
        if (pool != null) {
            pool.close();
        }
    }
}
