import { MessageSquare, ThumbsUp, Lightbulb } from 'lucide-react';

interface QuestionPanelProps {
  questionNumber: number;
  questionText: string;
  feedback?: string;
  expectedKeywords?: string[];
}

export function QuestionPanel({ questionNumber, questionText, feedback, expectedKeywords }: QuestionPanelProps) {
  return (
    <div className="flex flex-col h-full rounded-2xl border border-gray-800 bg-gray-900 p-6 shadow-sm">
      <div className="flex items-center space-x-3 mb-4">
        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-indigo-500/20 text-indigo-400 font-bold">
          {questionNumber}
        </div>
        <h3 className="text-lg font-semibold text-white">Current Question</h3>
      </div>
      
      <div className="flex-1 overflow-y-auto pr-2 custom-scrollbar">
        <p className="text-xl leading-relaxed text-gray-200">
          {questionText || "Waiting for the interviewer to speak..."}
        </p>

        {feedback && (
          <div className="mt-8 rounded-xl bg-gray-800/50 p-4 border border-gray-700/50">
            <div className="flex items-center space-x-2 text-indigo-400 mb-2">
              <MessageSquare className="h-4 w-4" />
              <span className="text-sm font-medium">Interviewer Feedback</span>
            </div>
            <p className="text-sm text-gray-300 leading-relaxed">{feedback}</p>
          </div>
        )}

        {expectedKeywords && expectedKeywords.length > 0 && (
          <div className="mt-6">
            <div className="flex items-center space-x-2 text-emerald-400 mb-3">
              <Lightbulb className="h-4 w-4" />
              <span className="text-sm font-medium">Key concepts expected</span>
            </div>
            <div className="flex flex-wrap gap-2">
              {expectedKeywords.map((keyword, idx) => (
                <span 
                  key={idx} 
                  className="inline-flex items-center rounded-md bg-gray-800 px-2.5 py-1 text-xs font-medium text-gray-400 border border-gray-700"
                >
                  {keyword}
                </span>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
