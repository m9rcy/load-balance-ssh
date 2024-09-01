package com.example.ssh;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.common.util.io.output.NoCloseOutputStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SshIntegrationTest {

    private static EmbeddedSshServer sshServer;
    private static final int SSH_PORT = 2222;

    @BeforeAll
    public static void setup() throws Exception {
        sshServer = new EmbeddedSshServer();
        sshServer.start(SSH_PORT);
    }

    @AfterAll
    public static void teardown() throws Exception {
        if (sshServer != null) {
            sshServer.stop();
        }
    }

    @Test
    public void testSshConnection() throws Exception {
        try (SshClient client = SshClient.setUpDefaultClient()) {
            client.start();

            try (ClientSession session = client.connect("test", "localhost", SSH_PORT).verify(10000).getSession()) {
                session.addPasswordIdentity("password");
                session.auth().verify(10000);

                ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                try (ChannelExec channel = session.createExecChannel("echo 'Hello, SSH!'")) {
                    channel.setOut(new NoCloseOutputStream(responseStream));
                    channel.open().verify(5, TimeUnit.SECONDS);
                    channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 0);

                    String response = responseStream.toString(StandardCharsets.UTF_8);
                    assertTrue(response.contains("Hello, SSH!"));
                }
            } finally {
                client.stop();
            }
        }
    }
}
