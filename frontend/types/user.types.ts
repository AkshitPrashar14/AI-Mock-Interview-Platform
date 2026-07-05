/**
 * User-related type definitions.
 * TODO: Define once the User module is implemented.
 */
export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  createdAt: string;
}

export type UserRole = 'CANDIDATE' | 'ADMIN';
