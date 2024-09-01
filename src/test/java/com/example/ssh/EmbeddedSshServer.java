package com.example.ssh;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.ProcessShellCommandFactory;
import org.apache.sshd.server.shell.ProcessShellFactory;

import java.io.IOException;
import java.nio.file.Paths;

public class EmbeddedSshServer {

    private SshServer sshServer;

    public void start(int port) throws IOException {
        sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(port);
        sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(Paths.get("hostkey.ser")));
        sshServer.setPasswordAuthenticator((username, password, session) -> "test".equals(username) && "password".equals(password));
        //sshServer.setShellFactory(new ProcessShellFactory("/bin/sh", "-i"));
        sshServer.setCommandFactory(new ProcessShellCommandFactory());
        sshServer.start();
    }

    public void stop() throws IOException {
        if (sshServer != null) {
            sshServer.stop();
        }
    }
}
