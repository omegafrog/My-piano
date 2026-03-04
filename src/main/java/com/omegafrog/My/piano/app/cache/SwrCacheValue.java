package com.omegafrog.My.piano.app.cache;

import java.io.Serializable;

public record SwrCacheValue<T extends Serializable>(
        T payload,
        long createdAtEpochMs,
        long softExpireAtEpochMs,
        long hardExpireAtEpochMs
) implements Serializable {
    public boolean isFresh(long nowEpochMs) {
        return nowEpochMs < softExpireAtEpochMs;
    }

    public boolean isHardExpired(long nowEpochMs) {
        return nowEpochMs >= hardExpireAtEpochMs;
    }

    public boolean isStaleWindow(long nowEpochMs) {
        return !isFresh(nowEpochMs) && !isHardExpired(nowEpochMs);
    }
}
