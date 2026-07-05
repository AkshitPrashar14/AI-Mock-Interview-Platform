import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "@/styles/globals.css";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "AI Mock Interview Platform",
  description:
    "Production-grade audio-first AI interview platform. Conduct adaptive mock interviews and receive comprehensive AI-powered feedback.",
  keywords: ["mock interview", "AI interview", "interview preparation", "technical interview"],
};

/**
 * Root layout — wraps every page in the application.
 * Add global providers (ThemeProvider, AuthProvider, etc.) here when implemented.
 */
export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body className={inter.className}>
        {/* TODO: Add Providers wrapper here (ThemeProvider, AuthProvider, QueryClientProvider) */}
        {children}
      </body>
    </html>
  );
}
