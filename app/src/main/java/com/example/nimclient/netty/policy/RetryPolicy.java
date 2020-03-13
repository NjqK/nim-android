package com.example.nimclient.netty.policy;

/**
 * @author kuro
 * @version v1.0
 * @date 20-3-8 下午2:04
 **/
public interface RetryPolicy {

    /**
     * 能否再重试
     * @param retryCount
     * @return
     */
    boolean allowRetry(int retryCount);

    /**
     * 重试的间隔
     * @param retryCount
     * @return
     */
    long getSleepIntervalMs(int retryCount);
}

