package com.example.ssh.exception;

public class SshPoolException extends Exception {

    public SshPoolException(String message, Throwable error) {
        super(message);
        if (error != null) {
            initCause(error);
        }
    }

    public SshPoolException(String message) {
        this(message, null);
    }

}