'use client';

import { useParams, useRouter } from 'next/navigation';
import { VerdictBanner } from '@/components/report/VerdictBanner';
import { ScoreBreakdown } from '@/components/report/ScoreBreakdown';
import { ImprovementAreas } from '@/components/report/ImprovementAreas';
import { ArrowLeft, Download } from 'lucide-react';
import Link from 'next/link';
import { useEffect, useState } from 'react';
import { getInterviewReport, InterviewReportResponse } from '@/services/report.service';

export default function ReportPage() {
  const params = useParams();
  const router = useRouter();
  
  const [report, setReport] = useState<InterviewReportResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getInterviewReport(params.id as string)
      .then((data) => {
        setReport(data);
        setLoading(false);
      })
      .catch((error) => {
        console.error("Failed to load report", error);
        setLoading(false);
      });
  }, [params.id]);

  if (loading) {
    return <div className="flex min-h-screen items-center justify-center bg-gray-950 text-white">Loading report...</div>;
  }

  if (!report) {
    return <div className="flex min-h-screen items-center justify-center bg-gray-950 text-white">Report not found or still generating...</div>;
  }

  return (
    <div className="min-h-screen bg-gray-950 p-6 sm:p-10">
      <div className="mx-auto max-w-5xl space-y-8">
        
        {/* Header Actions */}
        <header className="flex items-center justify-between">
          <Link 
            href="/dashboard"
            className="group flex items-center text-sm font-medium text-gray-400 hover:text-white transition-colors"
          >
            <ArrowLeft className="mr-2 h-4 w-4 transition-transform group-hover:-translate-x-1" />
            Back to Dashboard
          </Link>
          
          <button className="flex items-center rounded-lg bg-gray-800 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-gray-700">
            <Download className="mr-2 h-4 w-4" />
            Download PDF
          </button>
        </header>

        <div>
          <h1 className="text-3xl font-bold tracking-tight text-white mb-2">Interview Report</h1>
          <p className="text-gray-400">
            {new Date(report.generatedAt).toLocaleDateString()}
          </p>
        </div>

        <VerdictBanner verdict={report.verdict as any} overallScore={report.finalCompositeScore} />

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          <ScoreBreakdown 
            technicalScore={report.finalTechnicalScore}
            behavioralScore={report.finalBehavioralScore}
            englishScore={report.finalEnglishScore}
          />
          <ImprovementAreas areas={report.improvementAreas || []} />
        </div>
      </div>
    </div>
  );
}
