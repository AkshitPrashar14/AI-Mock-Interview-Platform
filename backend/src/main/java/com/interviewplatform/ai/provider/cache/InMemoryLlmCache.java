package com.interviewplatform.ai.provider.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory implementation of the LlmCache.
 */
@Slf4j
@Component
public class InMemoryLlmCache implements LlmCache {

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);

    @Value("${app.ai.cache.enabled:false}")
    private boolean enabled;

    @Value("${app.ai.cache.ttl-minutes:10}")
    private long ttlMinutes;

    @Value("${app.ai.cache.max-entries:1000}")
    private int maxEntries;

    @Override
    public String get(String provider, String model, String version, String systemPrompt, String userMessage, String schemaHint, double temperature) {
        if (!enabled) return null;
        
        String key = generateKey(provider, model, version, systemPrompt, userMessage, schemaHint, temperature);
        CacheEntry entry = cache.get(key);
        
        if (entry != null && !isExpired(entry)) {
            hits.incrementAndGet();
            return entry.response();
        }
        
        if (entry != null && isExpired(entry)) {
            cache.remove(key);
        }
        
        misses.incrementAndGet();
        return null;
    }

    @Override
    public void put(String provider, String model, String version, String systemPrompt, String userMessage, String schemaHint, double temperature, String response) {
        if (!enabled) return;
        
        if (cache.size() >= maxEntries) {
            // Simple eviction strategy: clear cache when full to avoid OOM
            // In a real system, we'd use LRU
            log.warn("LlmCache max entries ({}) reached, clearing cache", maxEntries);
            cache.clear();
        }
        
        String key = generateKey(provider, model, version, systemPrompt, userMessage, schemaHint, temperature);
        cache.put(key, new CacheEntry(response, System.currentTimeMillis()));
    }

    @Override
    public CacheStats getStats() {
        long h = hits.get();
        long m = misses.get();
        return new CacheStats(h, m, h + m);
    }

    private boolean isExpired(CacheEntry entry) {
        long ageMinutes = (System.currentTimeMillis() - entry.timestamp()) / (60 * 1000);
        return ageMinutes > ttlMinutes;
    }

    private String generateKey(String provider, String model, String version, String systemPrompt, String userMessage, String schemaHint, double temperature) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String raw = String.format("%s|%s|%s|%s|%s|%s|%s", provider, model, version, systemPrompt, userMessage, schemaHint, temperature);
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private record CacheEntry(String response, long timestamp) {}
}
