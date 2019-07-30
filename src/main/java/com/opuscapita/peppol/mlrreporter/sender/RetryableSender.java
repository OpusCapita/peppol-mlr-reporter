package com.opuscapita.peppol.mlrreporter.sender;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

public interface RetryableSender {

    @Retryable(value = {Exception.class}, maxAttempts = 30, backoff = @Backoff(delay = 1200000))
    void send(String report, String fileName) throws Exception;

    @Retryable(value = {Exception.class}, maxAttempts = 5, backoff = @Backoff(delay = 1000))
    void retrySend(String report, String fileName) throws Exception;
}
