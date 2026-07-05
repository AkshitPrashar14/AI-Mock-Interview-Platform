/**
 * Root page — redirects to /dashboard when authenticated,
 * or to /login when unauthenticated.
 *
 * TODO: Implement redirect logic once auth is added.
 */
export default function RootPage() {
  return (
    <main className="flex min-h-screen items-center justify-center">
      <p className="text-muted-foreground">AI Mock Interview Platform — scaffold</p>
    </main>
  );
}
