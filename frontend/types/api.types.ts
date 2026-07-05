/**
 * API request/response type definitions.
 * TODO: Align with backend DTOs once implemented.
 */
export interface ApiResponse<T> {
  data: T;
  message: string;
  timestamp: string;
}

export interface ApiError {
  status: number;
  error: string;
  message: string;
  timestamp: string;
  path: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}
