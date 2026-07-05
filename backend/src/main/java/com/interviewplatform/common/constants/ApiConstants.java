package com.interviewplatform.common.constants;

/**
 * Central repository of API-layer string constants.
 *
 * <p>Use these constants everywhere instead of inline string literals to prevent
 * typos and make global renames a single-point change.</p>
 *
 * <p><b>Sprint 1 — Backend Foundation</b></p>
 */
public final class ApiConstants {

    private ApiConstants() {
        // Utility class — do not instantiate
    }

    // ---- Versioning ----

    /** Current API version string. */
    public static final String API_VERSION = "v1";

    /** Base path prefix for all versioned API endpoints. */
    public static final String BASE_PATH = "/api/" + API_VERSION;

    // ---- HTTP Headers ----

    /** Header used to propagate and correlate a unique request identifier. */
    public static final String REQUEST_ID_HEADER = "X-Request-ID";

    /** Header used to identify the calling client or service. */
    public static final String USER_AGENT_HEADER = "User-Agent";

    // ---- MDC Keys ----

    /** MDC key under which the request ID is stored for log correlation. */
    public static final String MDC_REQUEST_ID = "requestId";

    // ---- Application Meta ----

    /** Canonical service name — used in health responses and log context. */
    public static final String SERVICE_NAME = "ai-mock-interview-platform";

    /** Current application version. Keep in sync with pom.xml. */
    public static final String APP_VERSION = "1.0.0";
}
