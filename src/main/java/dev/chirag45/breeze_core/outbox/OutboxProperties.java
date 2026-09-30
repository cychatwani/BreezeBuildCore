package dev.chirag45.breeze_core.outbox;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "breeze.outbox")
public class OutboxProperties {

    @Min(1) @Max(32)
    private int workerThreads = 1;
    @Min(100)
    private long pollIntervalMs = 1000;
    @Min(1)
    private int leaseSeconds = 60;
    @Min(1)
    private int maxAttempts = 5;
    @Min(1)
    private int retryBaseSeconds = 5;

    public int getWorkerThreads() { return workerThreads; }
    public void setWorkerThreads(int workerThreads) { this.workerThreads = workerThreads; }
    public long getPollIntervalMs() { return pollIntervalMs; }
    public void setPollIntervalMs(long pollIntervalMs) { this.pollIntervalMs = pollIntervalMs; }
    public int getLeaseSeconds() { return leaseSeconds; }
    public void setLeaseSeconds(int leaseSeconds) { this.leaseSeconds = leaseSeconds; }
    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
    public int getRetryBaseSeconds() { return retryBaseSeconds; }
    public void setRetryBaseSeconds(int retryBaseSeconds) { this.retryBaseSeconds = retryBaseSeconds; }
}
