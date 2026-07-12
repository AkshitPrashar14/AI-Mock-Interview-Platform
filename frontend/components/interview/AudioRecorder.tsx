import { useState, useEffect, useRef } from 'react';
import { Mic, Square, Loader2 } from 'lucide-react';

interface AudioRecorderProps {
  isListening: boolean;
  onStartRecording: () => void;
  onStopRecording: (audioBlob: Blob) => void;
}

export function AudioRecorder({ isListening, onStartRecording, onStopRecording }: AudioRecorderProps) {
  const [isRecording, setIsRecording] = useState(false);
  const mediaRecorderRef = useRef<MediaRecorder | null>(null);
  const audioChunksRef = useRef<Blob[]>([]);

  // Simulation logic for UI
  const handleStart = () => {
    setIsRecording(true);
    onStartRecording();
  };

  const handleStop = () => {
    setIsRecording(false);
    // Mock blob for now
    const blob = new Blob([], { type: 'audio/webm' });
    onStopRecording(blob);
  };

  return (
    <div className="flex h-32 w-full flex-col items-center justify-center rounded-2xl border border-gray-800 bg-gray-900 shadow-sm">
      {!isListening ? (
        <div className="flex flex-col items-center opacity-50">
          <Mic className="h-8 w-8 text-gray-500 mb-2" />
          <span className="text-sm text-gray-500">Wait for your turn...</span>
        </div>
      ) : (
        <div className="flex flex-col items-center space-y-4">
          {!isRecording ? (
            <button
              onClick={handleStart}
              className="group relative flex h-16 w-16 items-center justify-center rounded-full bg-emerald-500/20 text-emerald-500 hover:bg-emerald-500 hover:text-white transition-all shadow-[0_0_15px_rgba(16,185,129,0.3)] hover:shadow-[0_0_25px_rgba(16,185,129,0.5)]"
            >
              <Mic className="h-8 w-8" />
            </button>
          ) : (
            <button
              onClick={handleStop}
              className="group relative flex h-16 w-16 items-center justify-center rounded-full bg-red-500/20 text-red-500 hover:bg-red-500 hover:text-white transition-all animate-pulse shadow-[0_0_15px_rgba(239,68,68,0.3)]"
            >
              <Square className="h-6 w-6" />
            </button>
          )}
          <span className="text-sm font-medium text-gray-300">
            {isRecording ? 'Recording... Tap to stop' : 'Tap to start recording'}
          </span>
        </div>
      )}
    </div>
  );
}
