import { apiClient } from './api';

export interface InterviewReportResponse {
  reportId: string;
  interviewId: string;
  finalTechnicalScore: number;
  finalEnglishScore: number;
  finalBehavioralScore: number;
  finalCompositeScore: number;
  finalTier: string;
  verdict: string;
  executiveSummary: string;
  strengthHighlights: string[];
  improvementAreas: string[];
  studyPlan: string;
  reportStatus: string;
  generatedAt: string;
}

export const getInterviewReport = async (interviewId: string): Promise<InterviewReportResponse> => {
  const response = await apiClient.get(`/reports/${interviewId}`);
  return response.data.data;
};
