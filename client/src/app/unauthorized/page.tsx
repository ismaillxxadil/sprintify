import Link from 'next/link'

export default function UnauthorizedPage() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-background px-4">
      <div className="max-w-md rounded-3xl border border-slate-200 bg-white p-8 text-center shadow-sm">
        <h1 className="text-3xl font-bold text-slate-900">Unauthorized</h1>
        <p className="mt-3 text-sm text-slate-600">You do not have permission to access this page.</p>
        <Link href="/" className="button-primary mt-6">
          Return to Sprintify
        </Link>
      </div>
    </div>
  )
}
