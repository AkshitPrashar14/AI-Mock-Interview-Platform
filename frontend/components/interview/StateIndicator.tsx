import { Loader2, Mic, BrainCircuit, CheckCircle2 } from 'lucide-react';

export type InterviewState = 'INITIALIZING' | 'INTERVIEWER_SPEAKING' | 'LISTENING' | 'EVALUATING' | 'COMPLETED';

interface StateIndicatorProps {
  state: InterviewState;
}

export function StateIndicator({ state }: StateIndicatorProps) {
  const getStateConfig = (currentState: InterviewState) => {
    switch (currentState) {
      case 'INITIALIZING':
        return {
          text: 'Initializing Room...',
          icon: Loader2,
          color: 'text-blue-400',
          bgColor: 'bg-blue-500/10',
          animate: 'animate-spin',
        };
      case 'INTERVIEWER_SPEAKING':
        return {
          text: 'Interviewer is speaking...',
          icon: BrainCircuit,
          color: 'text-indigo-400',
          bgColor: 'bg-indigo-500/10',
          animate: 'animate-pulse',
        };
      case 'LISTENING':
        return {
          text: 'Listening to your answer...',
          icon: Mic,
          color: 'text-emerald-400',
          bgColor: 'bg-emerald-500/10',
          animate: 'animate-pulse',
        };
      case 'EVALUATING':
        return {
          text: 'Evaluating answer...',
          icon: Loader2,
          color: 'text-yellow-400',
          bgColor: 'bg-yellow-500/10',
          animate: 'animate-spin',
        };
      case 'COMPLETED':
        return {
          text: 'Interview Completed',
          icon: CheckCircle2,
          color: 'text-emerald-500',
          bgColor: 'bg-emerald-500/10',
          animate: '',
        };
    }
  };

  const config = getStateConfig(state);
  const Icon = config.icon;

  return (
    <div className="flex items-center space-x-3 rounded-full bg-gray-900 border border-gray-800 px-4 py-2 shadow-sm">
      <div className={`flex h-8 w-8 items-center justify-center rounded-full ${config.bgColor}`}>
        <Icon className={`h-4 w-4 ${config.color} ${config.animate}`} />
      </div>
      <span className="text-sm font-medium text-gray-300">{config.text}</span>
    </div>
  );
}
