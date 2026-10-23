import axios from 'axios'

// Create Axios client with base URL pointing to backend
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

export interface BaseResponse<T> {
  status: number
  message: string
  timestamp: string
  data: T
}

export interface Anggota {
  id: string
  nomorAnggota: string
  nama: string
  nik: string
  email: string
  telepon: string
  alamat: string
  tanggalDaftar: string
  tanggalAktivasi?: string | null
  statusKeanggotaan: string
  createdAt: string
  updatedAt: string
}

export interface Simpanan {
  id: string
  anggotaId: string
  nomorAnggota?: string
  namaAnggota?: string
  jenisSimpanan: 'POKOK' | 'WAJIB' | 'SUKARELA'
  nominal: number
  tanggalSetor: string
  periode?: string | null
  createdAt: string
  updatedAt: string
}

export interface RiwayatStatusAnggota {
  id: string
  anggotaId: string
  nomorAnggota?: string
  namaAnggota?: string
  pemeriksaanId?: string | null
  statusSebelumnya?: string | null
  statusBaru: string
  tanggalPerubahan: string
  alasan?: string | null
  diubahOleh: 'PENGURUS' | 'SISTEM'
  createdAt: string
  updatedAt: string
  pemeriksaan?: PemeriksaanKepatuhan | null
}

export interface PemeriksaanKepatuhan {
  id: string
  waktuEksekusi: string
  periodeAwal: string
  periodeAkhir: string
  jumlahDiperiksa: number
  jumlahDinonaktifkan: number
  createdAt: string
  updatedAt: string
}

export interface DetailAnggotaResponse extends Anggota {
  totalSimpananPokok: number
  totalSimpananWajib: number
  totalSimpananSukarela: number
  totalSimpanan: number
  listSimpanan: Simpanan[]
  listRiwayatStatus: RiwayatStatusAnggota[]
}

export interface DashboardStats {
  waitingVerificationCount: number
  waitingPokokCount: number
  activeCount: number
  frozenCount: number
  inactiveCount: number
  totalCurrentMonthSimpanan: number
}

export interface AnggotaKepatuhan {
  id: string
  nomorAnggota: string
  nama: string
  statusKeanggotaan: string
  tanggalAktivasi: string | null
  statusBayarPerPeriode: Record<string, boolean>
  hasilKepatuhan: 'PATUH' | 'MENUNGGAK' | 'BELUM_DIEVALUASI'
}

export interface KepatuhanData {
  bulanAcuan: string
  periodeEvaluasi: string[]
  ringkasanEksekusi: PemeriksaanKepatuhan[]
  daftarAnggota: AnggotaKepatuhan[]
}

export interface TrenSimpanan {
  label: string
  totalPokok: number
  totalWajib: number
  totalSukarela: number
  total: number
}

export interface StatistikData {
  tahun: number
  periodeType: string
  filterValue: string
  anggotaBaruCount: number
  anggotaByStatus: Record<string, number>
  totalSimpananByJenis: Record<string, number>
  trenSimpanan: TrenSimpanan[]
}

// API Service Methods
export const apiService = {
  // Dashboard
  getDashboard() {
    return api.get<BaseResponse<DashboardStats>>('/dashboard')
  },

  // Anggota
  getAllAnggota(search?: string, status?: string) {
    const params: Record<string, string> = {}
    if (search) params.search = search
    if (status && status !== 'ALL') params.status = status
    return api.get<BaseResponse<Anggota[]>>('/anggota', { params })
  },

  getAnggotaById(id: string) {
    return api.get<BaseResponse<DetailAnggotaResponse>>(`/anggota/${id}`)
  },

  createAnggota(payload: { nama: string; nik: string; email: string; telepon: string; alamat: string }) {
    return api.post<BaseResponse<Anggota>>('/anggota', payload)
  },

  updateAnggota(id: string, payload: { nama: string; email: string; telepon: string; alamat: string }) {
    return api.put<BaseResponse<Anggota>>(`/anggota/${id}`, payload)
  },

  verifikasiAnggota(id: string, action: 'VERIFIKASI' | 'TOLAK', alasan?: string) {
    return api.post<BaseResponse<Anggota>>(`/anggota/${id}/verifikasi`, { action, alasan })
  },

  keluarAnggota(id: string, alasan: string) {
    return api.delete<BaseResponse<Anggota>>(`/anggota/${id}/keluar`, { data: { alasan } })
  },

  updateStatusAnggota(id: string, status: 'DIBEKUKAN' | 'AKTIF', alasan?: string) {
    return api.put<BaseResponse<Anggota>>(`/anggota/${id}/status`, { status, alasan })
  },

  // Simpanan
  getAllSimpanan(search?: string, jenis?: string, periode?: string, nominalMin?: number, nominalMax?: number) {
    const params: Record<string, any> = {}
    if (search) params.search = search
    if (jenis && jenis !== 'ALL') params.jenis = jenis
    if (periode) params.periode = periode
    if (nominalMin !== undefined && nominalMin !== null && nominalMin !== 0) params.nominalMin = nominalMin
    if (nominalMax !== undefined && nominalMax !== null && nominalMax !== 0) params.nominalMax = nominalMax
    return api.get<BaseResponse<Simpanan[]>>('/simpanan', { params })
  },

  getSimpananById(id: string) {
    return api.get<BaseResponse<Simpanan>>(`/simpanan/${id}`)
  },

  createSimpananPokok(payload: { anggotaId: string; nominal: number; tanggalSetor: string }) {
    return api.post<BaseResponse<Simpanan>>('/simpanan/pokok', payload)
  },

  createSimpanan(payload: {
    anggotaId: string
    jenisSimpanan: 'WAJIB' | 'SUKARELA'
    nominal: number
    tanggalSetor: string
    periode?: string | null
  }) {
    return api.post<BaseResponse<Simpanan>>('/simpanan', payload)
  },

  // Riwayat Status
  getAllRiwayat(search?: string, statusBaru?: string, startDate?: string, endDate?: string) {
    const params: Record<string, string> = {}
    if (search) params.search = search
    if (statusBaru && statusBaru !== 'ALL') params.statusBaru = statusBaru
    if (startDate) params.startDate = startDate
    if (endDate) params.endDate = endDate
    return api.get<BaseResponse<RiwayatStatusAnggota[]>>('/riwayat-status', { params })
  },

  getRiwayatById(id: string) {
    return api.get<BaseResponse<RiwayatStatusAnggota>>(`/riwayat-status/${id}`)
  },

  // Kepatuhan
  getKepatuhan(bulanAcuan?: string) {
    const params: Record<string, string> = {}
    if (bulanAcuan) params.bulanAcuan = bulanAcuan
    return api.get<BaseResponse<KepatuhanData>>('/simpanan/kepatuhan', { params })
  },

  triggerKepatuhanJob() {
    return api.post<BaseResponse<PemeriksaanKepatuhan>>('/simpanan/kepatuhan/trigger')
  },

  // Statistik
  getStatistik(tahun?: number, periodeType?: string, filterValue?: string) {
    const params: Record<string, any> = {}
    if (tahun) params.tahun = tahun
    if (periodeType) params.periodeType = periodeType
    if (filterValue) params.filterValue = filterValue
    return api.get<BaseResponse<StatistikData>>('/statistik', { params })
  },
}

export default api
