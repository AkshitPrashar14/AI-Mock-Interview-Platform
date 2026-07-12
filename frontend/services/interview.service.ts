import { apiClient } from './api';

export interface CreateInterviewRequest {
  domain: string;
  roleLevel: string;
  totalQuestions: number;
}

export interface InterviewStartResponse {
  interviewId: string;
  firstQuestionText: string;
  difficulty: string;
  startedAt: string;
}

export const createInterview = async (data: CreateInterviewRequest) => {
  const response = await apiClient.post('/interviews', data);
  return response.data.data;
};

export const startInterview = async (id: string): Promise<InterviewStartResponse> => {
  const response = await apiClient.post(`/interviews/${id}/start`);
  return response.data.data;
};

export const submitAnswer = async (id: string, audioBlob: Blob) => {
  const formData = new FormData();
  formData.append('audio', audioBlob, 'answer.webm');
  
  const response = await apiClient.post(`/interviews/${id}/answers`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  return response.data.data;
};

export const endInterview = async (id: string) => {
  const response = await apiClient.post(`/interviews/${id}/end`);
  return response.data.data;
};

export const getInterview = async (id: string) => {
  const response = await apiClient.get(`/interviews/${id}`);
  return response.data.data;
};
