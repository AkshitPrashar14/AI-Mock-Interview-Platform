import { ShieldCheck, AlertTriangle } from 'lucide-react';

interface VerdictBannerProps {
  verdict: 'STRONG_HIRE' | 'HIRE' | 'NO_HIRE';
  overallScore: number;
}

export function VerdictBanner({ verdict, overallScore }: VerdictBannerProps) {
  const isHire = verdict === 'STRONG_HIRE' || verdict === 'HIRE';
  
  return (
    <div className={`relative overflow-hidden rounded-2xl border p-8 shadow-sm ${
      isHire 
        ? 'border-emerald-500/30 bg-gradient-to-r from-emerald-900/20 to-gray-900' 
        : 'border-amber-500/30 bg-gradient-to-r from-amber-900/20 to-gray-900'
    }`}>
      {/* Decorative background shape */}
      <div className={`absolute -right-10 -top-10 h-40 w-40 rounded-full opacity-10 blur-2xl ${isHire ? 'bg-emerald-500' : 'bg-amber-500'}`} />
      
      <div className="relative z-10 flex flex-col md:flex-row items-center justify-between">
        <div className="flex items-center space-x-6">
          <div className={`flex h-16 w-16 items-center justify-center rounded-2xl ${
            isHire ? 'bg-emerald-500/20 text-emerald-400' : 'bg-amber-500/20 text-amber-400'
          }`}>
            {isHire ? <ShieldCheck className="h-8 w-8" /> : <AlertTriangle className="h-8 w-8" />}
          </div>
          
          <div>
            <h2 className="text-2xl font-bold text-white mb-1">
              {verdict.replace('_', ' ')}
            </h2>
            <p className="text-gray-400">
              {isHire 
                ? "Excellent performance. You have demonstrated strong competency for this role." 
                : "You need more preparation for this role. Focus on your improvement areas."}
            </p>
          </div>
        </div>

        <div className="mt-6 md:mt-0 text-center md:text-right">
          <p className="text-sm font-medium text-gray-400 mb-1">Overall Score</p>
          <p className={`text-4xl font-extrabold ${isHire ? 'text-emerald-400' : 'text-amber-400'}`}>
            {overallScore}%
          </p>
        </div>
      </div>
    </div>
  );
}
