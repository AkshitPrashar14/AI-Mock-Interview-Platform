import { TrendingUp } from 'lucide-react';

interface ImprovementAreasProps {
  areas: string[];
}

export function ImprovementAreas({ areas }: ImprovementAreasProps) {
  if (!areas || areas.length === 0) {
    return null;
  }

  return (
    <div className="rounded-2xl border border-gray-800 bg-gray-900 p-6 shadow-sm">
      <div className="flex items-center space-x-3 mb-6">
        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-500/10 text-blue-400">
          <TrendingUp className="h-5 w-5" />
        </div>
        <h3 className="text-lg font-semibold text-white">Areas for Improvement</h3>
      </div>
      
      <ul className="space-y-4">
        {areas.map((area, index) => (
          <li key={index} className="flex items-start">
            <span className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-gray-800 text-xs font-medium text-gray-400 mr-3 mt-0.5 border border-gray-700">
              {index + 1}
            </span>
            <p className="text-sm text-gray-300 leading-relaxed">{area}</p>
          </li>
        ))}
      </ul>
    </div>
  );
}
