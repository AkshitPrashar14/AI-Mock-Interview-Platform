'use client';

import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend
} from 'recharts';

export interface ScoreData {
  date: string;
  technical: number;
  behavioral: number;
  english: number;
  overall: number;
}

interface ScoreTrendChartProps {
  data: ScoreData[];
}

export function ScoreTrendChart({ data }: ScoreTrendChartProps) {
  if (!data || data.length === 0) {
    return (
      <div className="flex h-72 w-full items-center justify-center rounded-2xl border border-gray-800 bg-gray-900">
        <p className="text-gray-500">No score data available yet.</p>
      </div>
    );
  }

  return (
    <div className="h-96 w-full rounded-2xl border border-gray-800 bg-gray-900 p-6 shadow-sm">
      <h3 className="mb-6 text-lg font-semibold text-white">Performance Trend</h3>
      <div className="h-72">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart
            data={data}
            margin={{ top: 5, right: 20, left: -20, bottom: 5 }}
          >
            <CartesianGrid strokeDasharray="3 3" stroke="#374151" vertical={false} />
            <XAxis 
              dataKey="date" 
              stroke="#9CA3AF" 
              tick={{ fill: '#9CA3AF', fontSize: 12 }} 
              tickLine={false}
              axisLine={false}
            />
            <YAxis 
              stroke="#9CA3AF" 
              tick={{ fill: '#9CA3AF', fontSize: 12 }} 
              tickLine={false}
              axisLine={false}
              domain={[0, 100]}
            />
            <Tooltip 
              contentStyle={{ backgroundColor: '#1F2937', border: 'none', borderRadius: '8px', color: '#F3F4F6' }}
              itemStyle={{ fontSize: '14px' }}
            />
            <Legend wrapperStyle={{ paddingTop: '20px' }} />
            <Line 
              type="monotone" 
              dataKey="overall" 
              name="Overall"
              stroke="#818CF8" 
              strokeWidth={3} 
              dot={{ r: 4, strokeWidth: 2 }} 
              activeDot={{ r: 6 }} 
            />
            <Line 
              type="monotone" 
              dataKey="technical" 
              name="Technical"
              stroke="#34D399" 
              strokeWidth={2} 
              dot={false}
            />
            <Line 
              type="monotone" 
              dataKey="behavioral" 
              name="Behavioral"
              stroke="#FBBF24" 
              strokeWidth={2} 
              dot={false}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
