package com.example.ssh.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SpringRetryLoggingListener implements RetryListener {

    private final static Logger LOG = LoggerFactory.getLogger(SpringRetryLoggingListener.class);

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        LOG.warn("Retrying for the {}th time for exception {}", context.getRetryCount(), throwable.toString());
        RetryListener.super.onError(context, callback, throwable);
    }
}