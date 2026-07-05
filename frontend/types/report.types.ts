/**
 * Report type definitions.
 * TODO: Define once the Report module is implemented.
 */
export interface InterviewReport {
  id: string;
  interviewId: string;
  overallScore: number;
  technicalScore: number;
  englishScore: number;
  behaviorScore: number;
  feedback: AgentFeedback[];
  generatedAt: string;
}

export interface AgentFeedback {
  agentType: AgentType;
  score: number;
  summary: string;
  strengths: string[];
  improvements: string[];
}

export type AgentType = 'TECHNICAL' | 'ENGLISH' | 'BEHAVIOR';
