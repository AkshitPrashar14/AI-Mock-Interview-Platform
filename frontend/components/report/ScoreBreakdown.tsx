import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts';

interface ScoreBreakdownProps {
  technicalScore: number;
  behavioralScore: number;
  englishScore: number;
}

export function ScoreBreakdown({ technicalScore, behavioralScore, englishScore }: ScoreBreakdownProps) {
  const data = [
    { name: 'Technical', value: technicalScore, color: '#34D399' }, // emerald
    { name: 'Behavioral', value: behavioralScore, color: '#FBBF24' }, // amber
    { name: 'English', value: englishScore, color: '#818CF8' }, // indigo
  ];

  return (
    <div className="rounded-2xl border border-gray-800 bg-gray-900 p-6 shadow-sm flex flex-col h-full">
      <h3 className="mb-4 text-lg font-semibold text-white">Score Breakdown</h3>
      <div className="flex-1 flex flex-col md:flex-row items-center justify-center">
        <div className="h-48 w-48 relative">
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie
                data={data}
                cx="50%"
                cy="50%"
                innerRadius={60}
                outerRadius={80}
                paddingAngle={5}
                dataKey="value"
              >
                {data.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} stroke="none" />
                ))}
              </Pie>
              <Tooltip 
                contentStyle={{ backgroundColor: '#1F2937', border: 'none', borderRadius: '8px', color: '#F3F4F6' }}
                itemStyle={{ fontSize: '14px' }}
              />
            </PieChart>
          </ResponsiveContainer>
        </div>
        
        <div className="mt-6 md:mt-0 md:ml-8 flex flex-col justify-center space-y-4">
          {data.map((item) => (
            <div key={item.name} className="flex items-center space-x-3">
              <div className="h-3 w-3 rounded-full" style={{ backgroundColor: item.color }} />
              <span className="text-sm font-medium text-gray-300">{item.name}</span>
              <span className="text-sm font-bold text-white ml-auto">{item.value}%</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
