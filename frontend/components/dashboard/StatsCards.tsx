import { BarChart3, Clock, Target, Trophy } from 'lucide-react';

interface StatsCardsProps {
  totalInterviews: number;
  averageScore: number;
  totalDurationMinutes: number;
  bestScore: number;
}

export function StatsCards({ totalInterviews, averageScore, totalDurationMinutes, bestScore }: StatsCardsProps) {
  const stats = [
    {
      name: 'Total Interviews',
      value: totalInterviews.toString(),
      icon: Target,
      color: 'text-blue-500',
      bgColor: 'bg-blue-500/10',
    },
    {
      name: 'Average Score',
      value: `${averageScore}%`,
      icon: BarChart3,
      color: 'text-indigo-500',
      bgColor: 'bg-indigo-500/10',
    },
    {
      name: 'Best Score',
      value: `${bestScore}%`,
      icon: Trophy,
      color: 'text-yellow-500',
      bgColor: 'bg-yellow-500/10',
    },
    {
      name: 'Practice Time',
      value: `${totalDurationMinutes}m`,
      icon: Clock,
      color: 'text-emerald-500',
      bgColor: 'bg-emerald-500/10',
    },
  ];

  return (
    <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
      {stats.map((stat) => (
        <div
          key={stat.name}
          className="overflow-hidden rounded-2xl bg-gray-900 border border-gray-800 p-6 shadow-sm transition-all hover:border-gray-700 hover:shadow-md"
        >
          <div className="flex items-center">
            <div className={`rounded-xl p-3 ${stat.bgColor}`}>
              <stat.icon className={`h-6 w-6 ${stat.color}`} aria-hidden="true" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-400">{stat.name}</p>
              <p className="text-2xl font-bold text-white">{stat.value}</p>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
