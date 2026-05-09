'use client'

export default function UnauthorizedPage() {
  return (
    <div className="min-h-screen bg-background flex items-center justify-center">
      <div className="card text-center max-w-md">
        <h1 className="text-3xl font-bold text-error mb-4">403 - Unauthorized</h1>
        <p className="text-gray-600 mb-6">You don't have permission to access this page.</p>
        <a href="/login" className="btn-primary">
          Return to Login
        </a>
      </div>
    </div>
  )
}
