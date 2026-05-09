import clsx from 'clsx'

export function StatusPill({ label }: { label: string }) {
  const normalized = label.toUpperCase()

  return (
    <span
      className={clsx(
        'status-pill',
        normalized === 'ACTIVE' || normalized === 'DONE'
          ? 'status-active'
          : normalized === 'PENDING' || normalized === 'PLANNING' || normalized === 'IN_PROGRESS'
            ? 'status-pending'
            : normalized === 'REJECTED' || normalized === 'CLOSED'
              ? 'status-inactive'
              : 'status-error'
      )}
    >
      {label.replaceAll('_', ' ')}
    </span>
  )
}
