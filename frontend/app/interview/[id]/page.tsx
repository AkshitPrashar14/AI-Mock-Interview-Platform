'use client';

import { useState, useEffect, useRef } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { StateIndicator, InterviewState } from '@/components/interview/StateIndicator';
import { QuestionPanel } from '@/components/interview/QuestionPanel';
import { AudioRecorder } from '@/components/interview/AudioRecorder';
import { submitAnswer, getInterview } from '@/services/interview.service';

export default function InterviewRoomPage() {
  const params = useParams();
  const router = useRouter();
  const [state, setState] = useState<InterviewState>('INITIALIZING');
  const [questionNum, setQuestionNum] = useState(1);
  const [questionText, setQuestionText] = useState('');
  const [feedback, setFeedback] = useState('');

  const pollIntervalRef = useRef<NodeJS.Timeout | null>(null);

  const fetchInterviewState = async () => {
    try {
      const interview = await getInterview(params.id as string);
      
      // Map backend state to frontend InterviewState component strings
      if (interview.state === 'COMPLETED') {
        setState('COMPLETED');
        setTimeout(() => router.push(`/report/${params.id}`), 2000);
        if (pollIntervalRef.current) clearInterval(pollIntervalRef.current);
        return;
      }
      
      if (interview.state === 'WAITING_FOR_RESPONSE') {
        setState('LISTENING');
      } else if (interview.state === 'TRANSCRIBING' || interview.state === 'EVALUATING' || interview.state === 'AGGREGATING') {
        setState('EVALUATING');
      } else if (interview.state === 'QUESTION_GENERATED' || interview.state === 'QUESTION_DELIVERED') {
        setState('INTERVIEWER_SPEAKING');
        // Auto transition to LISTENING after simulated speaker time
        setTimeout(() => setState('LISTENING'), 3000);
      } else {
        setState('INITIALIZING');
      }

      setQuestionNum(interview.currentQuestionNumber || 1);
      if (interview.currentQuestionText) {
        setQuestionText(interview.currentQuestionText);
      }
      if (interview.lastEvaluationFeedback) {
        setFeedback(interview.lastEvaluationFeedback);
      }

    } catch (err) {
      console.error("Failed to fetch interview state", err);
    }
  };

  useEffect(() => {
    // Initial fetch
    fetchInterviewState();
    
    // Poll every 3 seconds
    pollIntervalRef.current = setInterval(fetchInterviewState, 3000);
    
    return () => {
      if (pollIntervalRef.current) {
        clearInterval(pollIntervalRef.current);
      }
    };
  }, [params.id]);

  const handleStartRecording = () => {
    // Component handles audio UI internally
  };

  const handleStopRecording = async (audioBlob: Blob) => {
    setState('EVALUATING');
    try {
      await submitAnswer(params.id as string, audioBlob);
      // Polling will naturally pick up the state change to TRANSCRIBING/EVALUATING
    } catch (error) {
      console.error("Failed to submit answer", error);
      setState('LISTENING'); // Reset back to listening on error
    }
  };

  return (
    <div className="flex min-h-[calc(100vh-4rem)] flex-col bg-gray-950 p-4 sm:p-8">
      <header className="mb-8 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white">Interview Room</h1>
          <p className="text-sm text-gray-400">Session ID: {params.id}</p>
        </div>
        <StateIndicator state={state} />
      </header>

      <div className="flex flex-1 flex-col gap-6 lg:flex-row">
        {/* Left Column: Video/Avatar Placeholder and Audio Controls */}
        <div className="flex w-full flex-col gap-6 lg:w-1/3">
          <div className="relative flex aspect-video w-full flex-col items-center justify-center rounded-2xl border border-gray-800 bg-gray-900 shadow-sm overflow-hidden">
            <div className="absolute inset-0 bg-gradient-to-br from-indigo-900/20 to-gray-900" />
            <div className="z-10 flex h-24 w-24 items-center justify-center rounded-full bg-indigo-500/20 shadow-[0_0_30px_rgba(99,102,241,0.2)]">
               <span className="text-3xl font-bold text-indigo-400">AI</span>
            </div>
            <p className="z-10 mt-4 text-sm font-medium text-gray-400">Interviewer</p>
          </div>
          
          <AudioRecorder 
            isListening={state === 'LISTENING'} 
            onStartRecording={handleStartRecording}
            onStopRecording={handleStopRecording}
          />
        </div>

        {/* Right Column: Question & Feedback Panel */}
        <div className="flex-1">
          <QuestionPanel 
            questionNumber={questionNum}
            questionText={questionText}
            feedback={feedback}
            expectedKeywords={['Optimization', 'Metrics', 'Profiling', 'Database Indexing']}
          />
        </div>
      </div>
    </div>
  );
}
