'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/store';
import { Settings, Play } from 'lucide-react';
import { createInterview, startInterview } from '@/services/interview.service';

export default function InterviewSetupPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    roleLevel: 'MID_LEVEL',
    targetRole: 'Software Engineer',
    difficultyLevel: 'MEDIUM',
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleStart = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      const interview = await createInterview({
        domain: formData.targetRole,
        roleLevel: formData.roleLevel,
        totalQuestions: 3 // Set to 3 for brevity
      });
      
      // The start endpoint returns the first question which will be loaded 
      // when the user hits the interview room page.
      await startInterview(interview.id);
      
      router.push(`/interview/${interview.id}`);
    } catch (error) {
      console.error("Failed to start interview", error);
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-[calc(100vh-4rem)] items-center justify-center bg-gray-950 p-4">
      <div className="w-full max-w-2xl rounded-2xl border border-gray-800 bg-gray-900 p-8 shadow-xl">
        <div className="flex items-center space-x-4 mb-8">
          <div className="rounded-xl bg-indigo-500/10 p-3">
            <Settings className="h-6 w-6 text-indigo-400" />
          </div>
          <div>
            <h2 className="text-2xl font-bold text-white">Configure Interview</h2>
            <p className="text-sm text-gray-400">Set up your AI mock interview parameters</p>
          </div>
        </div>

        <form onSubmit={handleStart} className="space-y-6">
          <div>
            <label className="block text-sm font-medium text-gray-300" htmlFor="targetRole">
              Target Role
            </label>
            <input
              type="text"
              id="targetRole"
              name="targetRole"
              value={formData.targetRole}
              onChange={handleChange}
              className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800 px-4 py-3 text-white placeholder-gray-500 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500 transition-colors"
              placeholder="e.g. Frontend Developer"
              required
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-medium text-gray-300" htmlFor="roleLevel">
                Experience Level
              </label>
              <select
                id="roleLevel"
                name="roleLevel"
                value={formData.roleLevel}
                onChange={handleChange}
                className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800 px-4 py-3 text-white focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500 transition-colors"
              >
                <option value="JUNIOR">Junior (0-2 years)</option>
                <option value="MID_LEVEL">Mid-Level (3-5 years)</option>
                <option value="SENIOR">Senior (5+ years)</option>
                <option value="STAFF">Staff / Principal</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-300" htmlFor="difficultyLevel">
                Difficulty
              </label>
              <select
                id="difficultyLevel"
                name="difficultyLevel"
                value={formData.difficultyLevel}
                onChange={handleChange}
                className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800 px-4 py-3 text-white focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500 transition-colors"
              >
                <option value="EASY">Easy</option>
                <option value="MEDIUM">Medium</option>
                <option value="HARD">Hard</option>
              </select>
            </div>
          </div>

          <div className="pt-4 flex justify-end">
            <button
              type="submit"
              disabled={loading}
              className="group relative flex items-center justify-center rounded-lg bg-indigo-600 px-6 py-3 text-sm font-semibold text-white hover:bg-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-600 focus:ring-offset-2 focus:ring-offset-gray-900 disabled:opacity-50 transition-all"
            >
              {loading ? (
                'Initializing Room...'
              ) : (
                <>
                  <Play className="mr-2 h-4 w-4" /> Start Interview
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
