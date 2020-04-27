package com.example.nimclient.netty.policy;

import java.util.Random;

/**
 * @author kuro
 * @version v1.0
 * @date 20-3-8 下午2:05
 **/
public class DefaultRetryPolicy implements RetryPolicy {

    /**
     * 默认策略
     */
    public static final DefaultRetryPolicy DEFAULT =
            new DefaultRetryPolicy(500, 5, 2000);

    private static final int MAX_RETRIES_LIMIT = 29;

    private final Random random = new Random();
    /**
     * 基础睡眠时间
     */
    private final long baseSleepTimeMs;
    /**
     * 最大尝试次数
     */
    private final int maxRetries;
    /**
     * 最大睡眠时间
     */
    private final int maxSleepMs;

    public DefaultRetryPolicy(int baseSleepTimeMs, int maxRetries, int maxSleepMs) {
        this.baseSleepTimeMs = baseSleepTimeMs;
        this.maxRetries = maxRetries;
        this.maxSleepMs = maxSleepMs;
    }

    @Override
    public boolean allowRetry(int retryCount) {
        if (retryCount < maxRetries) {
            return true;
        }
        return false;
    }

    @Override
    public long getSleepIntervalMs(int retryCount) {
        if (retryCount < 0) {
            throw new IllegalArgumentException("retries count must greater than 0.");
        }
        if (retryCount > MAX_RETRIES_LIMIT) {
            retryCount = MAX_RETRIES_LIMIT;
        }
        long sleepMs = baseSleepTimeMs * Math.max(1, random.nextInt(1 << retryCount));
        if (sleepMs > maxSleepMs) {
            System.out.println(String.format("Sleep extension too large (%d). Pinning to %d", sleepMs, maxSleepMs));
            sleepMs = maxSleepMs;
        }
        return sleepMs;
    }
}
