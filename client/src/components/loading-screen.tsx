export function LoadingScreen({ label = 'Loading...' }: { label?: string }) {
  return (
    <div className="flex min-h-screen items-center justify-center px-4" style={{ backgroundColor: 'var(--background)' }}>
      <div className="rounded-2xl border bg-surface px-6 py-5 text-sm font-medium shadow-sm" style={{ borderColor: 'var(--border)', color: 'var(--text-secondary)' }}>
        {label}
      </div>
    </div>
  )
}
