/**
 * Base API client — placeholder.
 * TODO: Implement with fetch/axios and JWT interceptors.
 */
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080/api/v1';

export const apiClient = {
  baseUrl: API_BASE_URL,
  // TODO: Add get(), post(), put(), delete() helpers
};
