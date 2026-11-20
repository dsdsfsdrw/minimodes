package com.skvisuals.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * In-memory cache with configurable TTL.
 * Stores arbitrary objects keyed by String.
 */
public final class CacheManager {
    private final Map<String, CacheEntry<?>> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "SkVisuals-CacheCleaner");
                t.setDaemon(true);
                return t;
            });

    private final long ttlMinutes;

    public CacheManager(long ttlMinutes) {
        this.ttlMinutes = ttlMinutes;
        cleaner.scheduleAtFixedRate(this::evictExpired, ttlMinutes, ttlMinutes, TimeUnit.MINUTES);
    }

    public long getTtlMinutes() {
        return ttlMinutes;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        CacheEntry<T> entry = (CacheEntry<T>) cache.get(key);
        if (entry == null || entry.isExpired()) {
            cache.remove(key);
            return null;
        }
        return entry.value();
    }

    public <T> void put(String key, T value) {
        cache.put(key, new CacheEntry<>(value, System.currentTimeMillis() + ttlMinutes * 60_000L));
    }

    public void remove(String key) {
        cache.remove(key);
    }

    public void clear() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }

    private void evictExpired() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    public void shutdown() {
        cleaner.shutdown();
        clear();
    }

    private record CacheEntry<T>(T value, long expiry) {
        boolean isExpired() {
            return System.currentTimeMillis() > expiry;
        }
    }
}
