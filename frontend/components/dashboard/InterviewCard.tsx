import Link from 'next/link';
import { Calendar, Clock, ChevronRight } from 'lucide-react';

export interface InterviewSummary {
  id: string;
  role: string;
  date: string;
  durationMinutes: number;
  overallScore: number;
  status: 'COMPLETED' | 'IN_PROGRESS' | 'SCHEDULED';
}

interface InterviewCardProps {
  interview: InterviewSummary;
}

export function InterviewCard({ interview }: InterviewCardProps) {
  const isCompleted = interview.status === 'COMPLETED';

  return (
    <div className="group relative flex flex-col justify-between rounded-2xl border border-gray-800 bg-gray-900 p-6 shadow-sm transition-all hover:border-indigo-500 hover:shadow-md">
      <div>
        <div className="flex items-start justify-between">
          <h4 className="text-lg font-semibold text-white">{interview.role}</h4>
          {isCompleted ? (
            <span className="inline-flex items-center rounded-full bg-emerald-500/10 px-2.5 py-0.5 text-xs font-medium text-emerald-400">
              {interview.overallScore}% Score
            </span>
          ) : (
            <span className="inline-flex items-center rounded-full bg-blue-500/10 px-2.5 py-0.5 text-xs font-medium text-blue-400">
              {interview.status.replace('_', ' ')}
            </span>
          )}
        </div>
        
        <div className="mt-4 flex items-center space-x-4 text-sm text-gray-400">
          <div className="flex items-center">
            <Calendar className="mr-1.5 h-4 w-4" />
            {new Date(interview.date).toLocaleDateString()}
          </div>
          <div className="flex items-center">
            <Clock className="mr-1.5 h-4 w-4" />
            {interview.durationMinutes} min
          </div>
        </div>
      </div>

      <div className="mt-6">
        <Link 
          href={isCompleted ? `/report/${interview.id}` : `/interview/${interview.id}`}
          className="flex w-full items-center justify-center rounded-lg bg-gray-800 px-4 py-2 text-sm font-medium text-white transition-colors group-hover:bg-indigo-600"
        >
          {isCompleted ? 'View Report' : 'Resume Interview'}
          <ChevronRight className="ml-2 h-4 w-4" />
        </Link>
      </div>
    </div>
  );
}
