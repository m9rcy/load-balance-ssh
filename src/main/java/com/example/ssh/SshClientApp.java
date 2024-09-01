package com.example.ssh;

import com.example.ssh.client.JSchUtil;
import com.example.ssh.pool.SshClientConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SshClientApp implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(SshClientApp.class.getName());

    private final SshClientConnectionPool clientConnectionPool;

    public SshClientApp(SshClientConnectionPool clientConnectionPool) {
        this.clientConnectionPool = clientConnectionPool;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<String> response = JSchUtil.remoteExecute(clientConnectionPool, "echo %date% %time%");
        LOG.info("Response for remote execution {}", response);
    }
}
