import clsx from 'clsx'

export function StatusPill({ label }: { label: string }) {
  const normalized = label.toUpperCase()

  return (
    <span
      className={clsx(
        'inline-flex rounded-full px-2.5 py-1 text-xs font-semibold',
        normalized === 'ACTIVE' || normalized === 'DONE'
          ? 'bg-emerald-100 text-emerald-700'
          : normalized === 'PENDING' || normalized === 'PLANNING' || normalized === 'IN_PROGRESS'
            ? 'bg-amber-100 text-amber-700'
            : normalized === 'REJECTED' || normalized === 'CLOSED'
              ? 'bg-slate-200 text-slate-700'
              : 'bg-blue-100 text-blue-700'
      )}
    >
      {label.replaceAll('_', ' ')}
    </span>
  )
}
