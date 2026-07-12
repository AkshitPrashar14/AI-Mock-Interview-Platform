import { apiClient } from './api';

export interface AnalyticsSnapshot {
  totalInterviews: number;
  avgTechnicalScore: number;
  avgEnglishScore: number;
  avgBehavioralScore: number;
  avgCompositeScore: number;
  bestPerformanceTier: string;
  mostRecentVerdict: string;
  mostPracticedDomain: string;
  lastComputedAt: string;
}

export interface InterviewSummary {
  id: string;
  domain: string;
  roleLevel: string;
  state: string;
  totalQuestions: number;
  currentQuestionNumber: number;
  currentDifficulty: string;
  runningCompositeScore: number;
  startedAt: string;
  completedAt: string;
  createdAt: string;
}

export interface DashboardSummaryResponse {
  analytics: AnalyticsSnapshot | null;
  activeInterviews: InterviewSummary[];
  recentInterviews: InterviewSummary[];
}

export const getDashboardSummary = async (): Promise<DashboardSummaryResponse> => {
  const response = await apiClient.get('/dashboard/summary');
  // API Response wrapper expects data to be in response.data.data (from ApiResponse format)
  return response.data.data;
};
