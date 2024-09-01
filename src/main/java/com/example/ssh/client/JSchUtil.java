package com.example.ssh.client;

import com.example.ssh.exception.SshPoolException;
import com.example.ssh.pool.SshClientConnectionPool;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JSchUtil {

    //TODO add to SSH Client Config
    public static final int SESSION_TIMEOUT = 30000;
    public static final int CONNECT_TIMEOUT = 3000;
    private static final Logger logger = Logger.getLogger(SshClientConnectionPool.class.getName());

    public static List<String> remoteExecute(SshClientConnectionPool sshClientPool, String command) throws JSchException {
        logger.log(Level.FINE, ">> {}", command);
        List<String> resultLines = new ArrayList<>();
        ChannelExec channel;
        Session session = null;
        try {
            // Borrow a session from the pool
            session = sshClientPool.borrowSession();
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            InputStream input = channel.getInputStream();
            channel.connect(CONNECT_TIMEOUT);
            try {
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(input));
                String inputLine;
                while ((inputLine = inputReader.readLine()) != null) {
                    logger.log(Level.FINE, " {}", inputLine);
                    resultLines.add(inputLine);
                }
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "JSch inputStream close error:", e);
                    }
                }
            }
        } catch (SshPoolException | IOException e) {
            logger.log(Level.SEVERE, "Exception:", e);
        } finally {
            // Return the session to the pool and close the pool
            if (session != null) {
                sshClientPool.returnSession(session);
            }
        }
        return resultLines;
    }
}
