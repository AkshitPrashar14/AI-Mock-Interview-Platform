package com.interviewplatform.ai.provider.cache;

/**
 * Provider-agnostic cache for LLM responses.
 */
public interface LlmCache {
    
    /**
     * Checks if a cached response exists for the given parameters.
     */
    String get(String provider, String model, String version, String systemPrompt, String userMessage, String schemaHint, double temperature);
    
    /**
     * Stores a response in the cache.
     */
    void put(String provider, String model, String version, String systemPrompt, String userMessage, String schemaHint, double temperature, String response);
    
    /**
     * Retrieves current cache statistics.
     */
    CacheStats getStats();
    
    record CacheStats(long hits, long misses, long totalRequests) {}
}
