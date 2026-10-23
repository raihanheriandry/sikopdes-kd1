export function formatRupiah(amount: number | null | undefined): string {
  if (amount === null || amount === undefined) return 'Rp 0'
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(amount)
}

export function formatDate(dateStr: string | null | undefined): string {
  if (!dateStr) return '-'
  try {
    const d = new Date(dateStr)
    return d.toLocaleDateString('id-ID', {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
    })
  } catch {
    return dateStr
  }
}

export function formatDateTime(dateTimeStr: string | null | undefined): string {
  if (!dateTimeStr) return '-'
  try {
    const d = new Date(dateTimeStr)
    return d.toLocaleString('id-ID', {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  } catch {
    return dateTimeStr
  }
}

export function getStatusBadge(status: string | null | undefined): { label: string; bgClass: string } {
  switch (status?.toUpperCase()) {
    case 'MENUNGGU_VERIFIKASI':
      return { label: 'Menunggu Verifikasi', bgClass: 'bg-warning text-dark' }
    case 'MENUNGGU_SETORAN_POKOK':
      return { label: 'Menunggu Setoran Pokok', bgClass: 'bg-info text-dark' }
    case 'AKTIF':
      return { label: 'Aktif', bgClass: 'bg-success text-white' }
    case 'DIBEKUKAN':
      return { label: 'Dibekukan', bgClass: 'bg-secondary text-white' }
    case 'NONAKTIF':
      return { label: 'Nonaktif', bgClass: 'bg-danger text-white' }
    case 'DITOLAK':
      return { label: 'Ditolak', bgClass: 'bg-dark text-white' }
    case 'KELUAR':
      return { label: 'Keluar', bgClass: 'bg-light text-muted border' }
    default:
      return { label: status || '-', bgClass: 'bg-secondary text-white' }
  }
}

export function getJenisSimpananBadge(jenis: string | null | undefined): { label: string; bgClass: string } {
  switch (jenis?.toUpperCase()) {
    case 'POKOK':
      return { label: 'Pokok', bgClass: 'bg-primary text-white' }
    case 'WAJIB':
      return { label: 'Wajib', bgClass: 'bg-success text-white' }
    case 'SUKARELA':
      return { label: 'Sukarela', bgClass: 'bg-info text-dark' }
    default:
      return { label: jenis || '-', bgClass: 'bg-secondary text-white' }
  }
}
