'use client';

import { useEffect, useState } from 'react';
import { useAuthStore } from '@/store';
import { useRouter } from 'next/navigation';
import { StatsCards } from '@/components/dashboard/StatsCards';
import { ScoreTrendChart, ScoreData } from '@/components/dashboard/ScoreTrendChart';
import { InterviewCard, InterviewSummary } from '@/components/dashboard/InterviewCard';
import Link from 'next/link';
import { Plus } from 'lucide-react';
import { getDashboardSummary, DashboardSummaryResponse } from '@/services/dashboard.service';

// Mock trend data (backend does not provide historical points yet)
const mockTrendData: ScoreData[] = [
  { date: 'Jan', technical: 70, behavioral: 80, english: 85, overall: 78 },
  { date: 'Feb', technical: 75, behavioral: 82, english: 85, overall: 80 },
  { date: 'Mar', technical: 82, behavioral: 85, english: 88, overall: 85 },
  { date: 'Apr', technical: 85, behavioral: 88, english: 90, overall: 87 },
  { date: 'May', technical: 90, behavioral: 92, english: 90, overall: 91 },
];

export default function DashboardPage() {
  const router = useRouter();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const user = useAuthStore((state) => state.user);

  const [loading, setLoading] = useState(true);
  const [dashboardData, setDashboardData] = useState<DashboardSummaryResponse | null>(null);

  useEffect(() => {
    if (!isAuthenticated) {
      router.push('/login');
    } else {
      getDashboardSummary()
        .then((data) => {
          setDashboardData(data);
          setLoading(false);
        })
        .catch((err) => {
          console.error('Failed to load dashboard', err);
          setLoading(false);
        });
    }
  }, [isAuthenticated, router]);

  if (loading) {
    return <div className="flex min-h-screen items-center justify-center bg-gray-950 text-white">Loading dashboard...</div>;
  }

  return (
    <div className="min-h-screen bg-gray-950 p-6 sm:p-10">
      <div className="mx-auto max-w-7xl space-y-8">
        <header className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 className="text-3xl font-bold tracking-tight text-white">Dashboard</h1>
            <p className="mt-1 text-sm text-gray-400">
              Welcome back, {user?.name || 'User'}. Here's how you're performing.
            </p>
          </div>
          <Link 
            href="/interview/setup"
            className="inline-flex items-center justify-center rounded-lg bg-indigo-600 px-5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600 transition-colors"
          >
            <Plus className="mr-2 h-4 w-4" />
            New Interview
          </Link>
        </header>

        {dashboardData?.analytics && (
          <StatsCards
            totalInterviews={dashboardData.analytics.totalInterviews}
            averageScore={dashboardData.analytics.avgCompositeScore || 0}
            totalDurationMinutes={dashboardData.analytics.totalInterviews * 45} // Approx duration
            bestScore={dashboardData.analytics.avgCompositeScore || 0} // Using average since bestScore isn't available
          />
        )}

        <div className="grid grid-cols-1 gap-8 lg:grid-cols-3">
          <div className="lg:col-span-2">
            <ScoreTrendChart data={mockTrendData} />
          </div>

          <div>
            <div className="mb-6 flex items-center justify-between">
              <h3 className="text-lg font-semibold text-white">Recent Interviews</h3>
              <Link href="/interviews" className="text-sm font-medium text-indigo-400 hover:text-indigo-300">
                View all
              </Link>
            </div>
            <div className="space-y-4">
              {dashboardData?.recentInterviews.map((interview) => (
                <InterviewCard 
                  key={interview.id} 
                  interview={{
                    id: interview.id,
                    role: interview.domain || 'Software Engineer',
                    date: interview.createdAt || interview.startedAt || new Date().toISOString(),
                    durationMinutes: 45,
                    overallScore: interview.runningCompositeScore || 0,
                    status: interview.state as 'COMPLETED' | 'IN_PROGRESS' | 'SCHEDULED'
                  }} 
                />
              ))}
              {(!dashboardData?.recentInterviews || dashboardData.recentInterviews.length === 0) && (
                <div className="text-gray-400 text-sm">No recent interviews found.</div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
