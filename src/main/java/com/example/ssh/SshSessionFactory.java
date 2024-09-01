package com.example.ssh;

import com.example.ssh.config.SpringRetryLoggingListener;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.springframework.retry.support.RetryTemplate;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SshSessionFactory implements PooledObjectFactory<Session> {

    private static final Logger logger = Logger.getLogger(SshSessionFactory.class.getName());

    private final List<String> hosts;
    private final int port;
    private final String username;
    private final String password;
    private final JSch jsch;

    private final RetryTemplate template = RetryTemplate.builder()
            .maxAttempts(3)
            .fixedBackoff(100)
            .withListener(new SpringRetryLoggingListener())
            .retryOn(JSchException.class)
            .build();

    private final AtomicInteger serverIndex = new AtomicInteger(0);

    public SshSessionFactory(List<String> hosts, int port, String username, String password, JSch jsch) {
        this.hosts = hosts;
        this.port = port;
        this.username = username;
        this.password = password;
        this.jsch = jsch != null ? jsch : new JSch();
    }


    @Override
    public PooledObject<Session> makeObject() throws Exception {
        Session session = template.execute(ctx -> newSession());
        return new DefaultPooledObject<>(session);
    }

    @Override
    public void destroyObject(PooledObject<Session> pooledObject) {
        Session session = pooledObject.getObject();
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    @Override
    public boolean validateObject(PooledObject<Session> pooledObject) {
        Session session = pooledObject.getObject();
        if (session == null || !session.isConnected()) {
            logger.warning("Invalid SSH session detected.");
            return false;
        }
        logger.info("Validating the session from SSH Server: " + session.getHost());
        return true;
    }

    @Override
    public void activateObject(PooledObject<Session> pooledObject) {
        // No activation required
    }

    @Override
    public void passivateObject(PooledObject<Session> pooledObject) {
        // No passivation required
    }

    private String getNextHost() {
        // Round-robin selection of the next host
        int index = serverIndex.getAndUpdate(i -> (i + 1) % hosts.size());
        return hosts.get(index);
    }

    private Session newSession() throws JSchException {
        String host = getNextHost(); // Get the next server in the list
        Session session = null;
        try {
            session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no"); // Not recommended for production
            session.connect();
            return session;
        } catch (JSchException e) {
            logger.log(Level.SEVERE, "Failed to connect to SSH server: " + host, e);
            throw e;
        }
    }
}
