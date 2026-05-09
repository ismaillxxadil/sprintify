export const formatDate = (value?: string | null) => {
  if (!value) {
    return '—'
  }

  return new Date(value).toLocaleDateString()
}

export const formatDateTime = (value?: string | null) => {
  if (!value) {
    return '—'
  }

  return new Date(value).toLocaleString()
}
