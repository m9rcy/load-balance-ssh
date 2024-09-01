package com.example.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//@Component
public class SshClientAppOne implements ApplicationRunner {
    private static final Logger logger = Logger.getLogger(SshClientAppOne.class.getName());

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("Hello, Spring Boot has started!");

        // Define the list of SSH servers
        List<String> hosts = Arrays.asList("192.168.0.203", "192.168.0.205");
        int port = 22;
        String username = "sshuser";
        String password = "sshuser";

        SshConnectionPool sshConnectionPool = new SshConnectionPool(hosts, port, username, password);

        int count = 0;
        while (count < 5) {
            System.out.println("Test" + count);
            execute(sshConnectionPool);
            count++;
        }

    }

    public void execute(SshConnectionPool sshConnectionPool) {
        System.out.println("Test");
        Session session = null;
        try {
            // Borrow a session from the pool
            session = sshConnectionPool.borrowSession();

            // Use the session (e.g., open a channel and execute commands)
            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            //channelExec.setCommand("ls -la");
            channelExec.setCommand("echo %date% %time%");
            channelExec.setErrStream(System.err);
            InputStream in = channelExec.getInputStream();
            channelExec.connect();

            // Read the command output
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Disconnect the channel after use
            channelExec.disconnect();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during SSH operation", e);
        } finally {
            // Return the session to the pool and close the pool
            if (session != null) {
                sshConnectionPool.returnSession(session);
            }
            //sshConnectionPool.close();
        }
    }

}
