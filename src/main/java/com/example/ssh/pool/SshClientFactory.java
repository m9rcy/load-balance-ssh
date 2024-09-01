package com.example.ssh.pool;

import com.example.ssh.config.SpringRetryLoggingListener;
import com.example.ssh.config.SshClientConfigProperties;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.support.RetryTemplate;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SshClientFactory implements PooledObjectFactory<Session> {

    private static final Logger LOG = LoggerFactory.getLogger(SshClientFactory.class.getName());
    private final List<SshClientConfigProperties> sshClientProps;
    private final RetryTemplate template = RetryTemplate.builder()
            .maxAttempts(3)
            .fixedBackoff(200)
            .withListener(new SpringRetryLoggingListener())
            .retryOn(Exception.class)
            .build();
    private final AtomicInteger serverIndex = new AtomicInteger(0);
    private JSch jsch;

    // For mocking
    public SshClientFactory(List<SshClientConfigProperties> sshClientProps, JSch jsch) {
        this.sshClientProps = sshClientProps;
        this.jsch = jsch;
    }

    public SshClientFactory(List<SshClientConfigProperties> sshClientProps) {
        this.sshClientProps = sshClientProps;
    }

    @Override
    public void activateObject(PooledObject<Session> pooledObject) throws Exception {
        //noop
    }

    @Override
    public void destroyObject(PooledObject<Session> pooledObject) throws Exception {
        Session session = pooledObject.getObject();
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    @Override
    public PooledObject<Session> makeObject() throws Exception {
        Session session = template.execute(ctx -> newSession());
        return new DefaultPooledObject<>(session);
    }

    private Session newSession() throws Exception {
        SshClientConfigProperties sshClient = getSshClient(); // Get the next server in the list
        Session session = null;

        try {
            session = getJsch().getSession(sshClient.username(), sshClient.host(), sshClient.port());
            session.setPassword(sshClient.password());
            session.setConfig("StrictHostKeyChecking", "no"); // Not recommended for production
            session.connect();
        } catch (JSchException e) {
            LOG.error("Failed to connect to SSH server: {}", sshClient.host(), e);
            throw new Exception("Unable to create SSH session for host: " + sshClient.host(), e);
        }
        return session;
    }

    private SshClientConfigProperties getSshClient() {
        // Round-robin selection of the next host configuration
        int index = serverIndex.getAndUpdate(i -> (i + 1) % sshClientProps.size());
        return sshClientProps.get(index);
    }

    @Override
    public void passivateObject(PooledObject<Session> pooledObject) throws Exception {
        //noop
    }

    @Override
    public boolean validateObject(PooledObject<Session> pooledObject) {
        Session session = pooledObject.getObject();
        if (session == null || !session.isConnected()) {
            LOG.warn("Invalid SSH session detected.");
            return false;
        }
        return true;
    }

    public JSch getJsch() {
        return jsch == null ? new JSch() : jsch;
    }

}

