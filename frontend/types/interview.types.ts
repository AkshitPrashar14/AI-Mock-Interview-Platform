/**
 * Interview session type definitions.
 * TODO: Define once the Interview module is implemented.
 */
export interface InterviewSession {
  id: string;
  userId: string;
  status: InterviewStatus;
  startedAt: string | null;
  endedAt: string | null;
  durationMinutes: number | null;
}

export type InterviewStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface Question {
  id: string;
  text: string;
  category: QuestionCategory;
  difficultyLevel: number;
}

export type QuestionCategory = 'TECHNICAL' | 'BEHAVIORAL' | 'ENGLISH';
