<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { apiService, type DetailAnggotaResponse } from '@/services/api'
import {
  formatDate,
  formatDateTime,
  formatRupiah,
  getStatusBadge,
  getJenisSimpananBadge,
} from '@/utils/formatters'

const route = useRoute()
const anggotaId = route.params.id as string

const anggota = ref<DetailAnggotaResponse | null>(null)
const loading = ref(true)
const actionLoading = ref(false)
const error = ref<string | null>(null)
const successMsg = ref<string | null>(null)

// Modal states
const showModal = ref(false)
const modalType = ref<'VERIFIKASI' | 'TOLAK' | 'BEKUKAN' | 'AKTIFKAN' | 'KELUAR' | null>(null)
const modalTitle = ref('')
const modalReason = ref('')
const modalReasonError = ref<string | null>(null)

const fetchDetail = async () => {
  loading.value = true
  error.value = null
  try {
    const res = await apiService.getAnggotaById(anggotaId)
    anggota.value = res.data.data
  } catch (err: any) {
    error.value = err.response?.data?.message || err.message || 'Gagal memuat detail anggota'
  } finally {
    loading.value = false
  }
}

const openModal = (type: 'VERIFIKASI' | 'TOLAK' | 'BEKUKAN' | 'AKTIFKAN' | 'KELUAR') => {
  modalType.value = type
  modalReason.value = ''
  modalReasonError.value = null

  if (type === 'VERIFIKASI') {
    modalTitle.value = 'Verifikasi Calon Anggota'
  } else if (type === 'TOLAK') {
    modalTitle.value = 'Tolak Pendaftaran Calon Anggota'
  } else if (type === 'BEKUKAN') {
    modalTitle.value = 'Bekukan Status Anggota'
  } else if (type === 'AKTIFKAN') {
    modalTitle.value = 'Aktifkan Kembali Anggota'
  } else if (type === 'KELUAR') {
    modalTitle.value = 'Proses Pengeluaran Anggota (Keluar)'
  }

  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  modalType.value = null
  modalReason.value = ''
  modalReasonError.value = null
}

const handleModalSubmit = async () => {
  modalReasonError.value = null

  // Reason is mandatory for TOLAK, BEKUKAN, and KELUAR
  if (['TOLAK', 'BEKUKAN', 'KELUAR'].includes(modalType.value || '') && !modalReason.value.trim()) {
    modalReasonError.value = 'Alasan wajib diisi!'
    return
  }

  actionLoading.value = true
  error.value = null
  successMsg.value = null

  try {
    if (modalType.value === 'VERIFIKASI') {
      await apiService.verifikasiAnggota(anggotaId, 'VERIFIKASI', modalReason.value)
      successMsg.value = 'Anggota berhasil diverifikasi. Status berubah menjadi Menunggu Setoran Pokok.'
    } else if (modalType.value === 'TOLAK') {
      await apiService.verifikasiAnggota(anggotaId, 'TOLAK', modalReason.value)
      successMsg.value = 'Pendaftaran anggota telah ditolak.'
    } else if (modalType.value === 'BEKUKAN') {
      await apiService.updateStatusAnggota(anggotaId, 'DIBEKUKAN', modalReason.value)
      successMsg.value = 'Status keanggotaan berhasil dibekukan.'
    } else if (modalType.value === 'AKTIFKAN') {
      await apiService.updateStatusAnggota(anggotaId, 'AKTIF', modalReason.value)
      successMsg.value = 'Anggota berhasil diaktifkan kembali.'
    } else if (modalType.value === 'KELUAR') {
      await apiService.keluarAnggota(anggotaId, modalReason.value)
      successMsg.value = 'Anggota telah dikeluarkan dari keanggotaan koperasi.'
    }

    closeModal()
    await fetchDetail()
  } catch (err: any) {
    modalReasonError.value = err.response?.data?.message || err.message || 'Terjadi kesalahan saat memproses permintaan'
  } finally {
    actionLoading.value = false
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<template>
  <div class="anggota-detail-view">
    <!-- Back Button & Breadcrumb -->
    <div class="d-flex align-items-center gap-2 mb-3">
      <RouterLink to="/anggota" class="btn btn-sm btn-outline-secondary">
        <i class="bi bi-arrow-left me-1"></i> Kembali ke Daftar Anggota
      </RouterLink>
    </div>

    <!-- Alert Messages -->
    <div v-if="successMsg" class="alert alert-success alert-dismissible fade show" role="alert">
      <i class="bi bi-check-circle-fill me-2"></i> {{ successMsg }}
      <button type="button" class="btn-close" @click="successMsg = null"></button>
    </div>
    <div v-if="error" class="alert alert-danger alert-dismissible fade show" role="alert">
      <i class="bi bi-exclamation-triangle-fill me-2"></i> {{ error }}
      <button type="button" class="btn-close" @click="error = null"></button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Memuat data...</span>
      </div>
      <p class="mt-2 text-muted">Memuat detail anggota...</p>
    </div>

    <div v-else-if="anggota">
      <!-- Main Info & Actions Card -->
      <div class="card border-0 shadow-sm mb-4">
        <div class="card-body p-4">
          <div class="d-flex flex-wrap justify-content-between align-items-start gap-3 border-bottom pb-3 mb-3">
            <div>
              <div class="d-flex align-items-center gap-2">
                <h3 class="fw-bold mb-0 text-dark">{{ anggota.nama }}</h3>
                <span class="badge" :class="getStatusBadge(anggota.statusKeanggotaan).bgClass">
                  {{ getStatusBadge(anggota.statusKeanggotaan).label }}
                </span>
              </div>
              <p class="text-muted mb-0 font-monospace">
                ID: <strong>{{ anggota.id }}</strong> | No. Anggota: <strong>{{ anggota.nomorAnggota }}</strong>
              </p>
            </div>

            <!-- Contextual Action Buttons -->
            <div class="d-flex flex-wrap gap-2">
              <!-- Menunggu Verifikasi -->
              <template v-if="anggota.statusKeanggotaan === 'MENUNGGU_VERIFIKASI'">
                <RouterLink :to="`/anggota/${anggota.id}/update`" class="btn btn-outline-primary">
                  <i class="bi bi-pencil-square me-1"></i> Ubah
                </RouterLink>
                <button class="btn btn-success" @click="openModal('VERIFIKASI')">
                  <i class="bi bi-check-lg me-1"></i> Verifikasi
                </button>
                <button class="btn btn-danger" @click="openModal('TOLAK')">
                  <i class="bi bi-x-lg me-1"></i> Tolak
                </button>
              </template>

              <!-- Menunggu Setoran Pokok -->
              <template v-else-if="anggota.statusKeanggotaan === 'MENUNGGU_SETORAN_POKOK'">
                <RouterLink :to="`/anggota/${anggota.id}/update`" class="btn btn-outline-primary">
                  <i class="bi bi-pencil-square me-1"></i> Ubah
                </RouterLink>
                <RouterLink
                  :to="{ path: '/simpanan/pokok/create', query: { anggotaId: anggota.id } }"
                  class="btn btn-primary"
                >
                  <i class="bi bi-cash-stack me-1"></i> Catat Simpanan Pokok
                </RouterLink>
              </template>

              <!-- Aktif -->
              <template v-else-if="anggota.statusKeanggotaan === 'AKTIF'">
                <RouterLink :to="`/anggota/${anggota.id}/update`" class="btn btn-outline-primary">
                  <i class="bi bi-pencil-square me-1"></i> Ubah
                </RouterLink>
                <RouterLink
                  :to="{ path: '/simpanan/create', query: { anggotaId: anggota.id } }"
                  class="btn btn-success"
                >
                  <i class="bi bi-plus-circle me-1"></i> Tambah Simpanan
                </RouterLink>
                <button class="btn btn-warning text-dark" @click="openModal('BEKUKAN')">
                  <i class="bi bi-pause-circle me-1"></i> Bekukan
                </button>
                <button class="btn btn-outline-danger" @click="openModal('KELUAR')">
                  <i class="bi bi-box-arrow-right me-1"></i> Keluarkan
                </button>
              </template>

              <!-- Nonaktif -->
              <template v-else-if="anggota.statusKeanggotaan === 'NONAKTIF'">
                <RouterLink :to="`/anggota/${anggota.id}/update`" class="btn btn-outline-primary">
                  <i class="bi bi-pencil-square me-1"></i> Ubah
                </RouterLink>
                <RouterLink
                  :to="{ path: '/simpanan/create', query: { anggotaId: anggota.id, jenis: 'WAJIB' } }"
                  class="btn btn-success"
                >
                  <i class="bi bi-plus-circle me-1"></i> Tambah Simpanan (Wajib)
                </RouterLink>
                <button class="btn btn-primary" @click="openModal('AKTIFKAN')">
                  <i class="bi bi-arrow-repeat me-1"></i> Aktifkan Kembali
                </button>
                <button class="btn btn-outline-danger" @click="openModal('KELUAR')">
                  <i class="bi bi-box-arrow-right me-1"></i> Keluarkan
                </button>
              </template>

              <!-- Dibekukan -->
              <template v-else-if="anggota.statusKeanggotaan === 'DIBEKUKAN'">
                <RouterLink :to="`/anggota/${anggota.id}/update`" class="btn btn-outline-primary">
                  <i class="bi bi-pencil-square me-1"></i> Ubah
                </RouterLink>
                <button class="btn btn-primary" @click="openModal('AKTIFKAN')">
                  <i class="bi bi-play-circle me-1"></i> Aktifkan Kembali
                </button>
                <button class="btn btn-outline-danger" @click="openModal('KELUAR')">
                  <i class="bi bi-box-arrow-right me-1"></i> Keluarkan
                </button>
              </template>

              <!-- Ditolak / Keluar -->
              <template v-else>
                <span class="badge bg-light text-muted border p-2">
                  <i class="bi bi-info-circle me-1"></i> Status final - Tidak ada aksi lanjutan
                </span>
              </template>
            </div>
          </div>

          <!-- Identity Details Grid -->
          <div class="row g-3">
            <div class="col-md-3 col-sm-6">
              <span class="text-muted small d-block">Nomor Induk Kependudukan (NIK)</span>
              <strong class="font-monospace fs-6">{{ anggota.nik }}</strong>
            </div>
            <div class="col-md-3 col-sm-6">
              <span class="text-muted small d-block">Email</span>
              <span class="fs-6">{{ anggota.email }}</span>
            </div>
            <div class="col-md-3 col-sm-6">
              <span class="text-muted small d-block">Nomor Telepon</span>
              <span class="fs-6">{{ anggota.telepon }}</span>
            </div>
            <div class="col-md-3 col-sm-6">
              <span class="text-muted small d-block">Tanggal Terdaftar</span>
              <span class="fs-6">{{ formatDate(anggota.tanggalDaftar) }}</span>
            </div>
            <div class="col-md-6">
              <span class="text-muted small d-block">Alamat Tinggal</span>
              <span class="fs-6">{{ anggota.alamat }}</span>
            </div>
            <div class="col-md-6">
              <span class="text-muted small d-block">Tanggal Aktivasi</span>
              <span class="fs-6 fw-semibold text-primary">
                {{ anggota.tanggalAktivasi ? formatDateTime(anggota.tanggalAktivasi) : 'Belum Pernah Diaktifkan' }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Financial Balance Summary Cards -->
      <div class="row g-3 mb-4">
        <div class="col-sm-6 col-lg-3">
          <div class="card border-0 shadow-sm border-start border-primary border-4">
            <div class="card-body">
              <span class="text-muted small text-uppercase fw-bold">Simpanan Pokok</span>
              <h4 class="fw-bold text-primary mb-0 mt-1">{{ formatRupiah(anggota.totalSimpananPokok) }}</h4>
            </div>
          </div>
        </div>
        <div class="col-sm-6 col-lg-3">
          <div class="card border-0 shadow-sm border-start border-success border-4">
            <div class="card-body">
              <span class="text-muted small text-uppercase fw-bold">Simpanan Wajib</span>
              <h4 class="fw-bold text-success mb-0 mt-1">{{ formatRupiah(anggota.totalSimpananWajib) }}</h4>
            </div>
          </div>
        </div>
        <div class="col-sm-6 col-lg-3">
          <div class="card border-0 shadow-sm border-start border-info border-4">
            <div class="card-body">
              <span class="text-muted small text-uppercase fw-bold">Simpanan Sukarela</span>
              <h4 class="fw-bold text-info mb-0 mt-1">{{ formatRupiah(anggota.totalSimpananSukarela) }}</h4>
            </div>
          </div>
        </div>
        <div class="col-sm-6 col-lg-3">
          <div class="card border-0 shadow-sm border-start border-dark border-4 bg-dark text-white">
            <div class="card-body">
              <span class="text-white-50 small text-uppercase fw-bold">Total Seluruh Simpanan</span>
              <h4 class="fw-bold text-warning mb-0 mt-1">{{ formatRupiah(anggota.totalSimpanan) }}</h4>
            </div>
          </div>
        </div>
      </div>

      <!-- Tables: Riwayat Simpanan & Riwayat Status -->
      <div class="row g-4">
        <!-- Riwayat Simpanan -->
        <div class="col-12 col-lg-6">
          <div class="card border-0 shadow-sm h-100">
            <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
              <h5 class="fw-bold mb-0">
                <i class="bi bi-wallet2 text-success me-2"></i>Riwayat Simpanan
              </h5>
              <span class="badge bg-light text-dark border">{{ anggota.listSimpanan.length }} Transaksi</span>
            </div>
            <div class="card-body p-0">
              <div v-if="anggota.listSimpanan.length === 0" class="text-center py-4 text-muted">
                <i class="bi bi-inbox fs-2"></i>
                <p class="mb-0 mt-1 small">Belum ada riwayat transaksi simpanan.</p>
              </div>
              <div v-else class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                  <thead class="table-light">
                    <tr>
                      <th scope="col">ID</th>
                      <th scope="col">Jenis</th>
                      <th scope="col">Nominal</th>
                      <th scope="col">Periode</th>
                      <th scope="col">Tgl Setor</th>
                      <th scope="col" class="text-center">Aksi</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="s in anggota.listSimpanan" :key="s.id">
                      <td class="font-monospace small text-primary fw-semibold">{{ s.id }}</td>
                      <td>
                        <span class="badge" :class="getJenisSimpananBadge(s.jenisSimpanan).bgClass">
                          {{ getJenisSimpananBadge(s.jenisSimpanan).label }}
                        </span>
                      </td>
                      <td class="fw-bold">{{ formatRupiah(s.nominal) }}</td>
                      <td class="font-monospace small">{{ s.periode || '-' }}</td>
                      <td class="small text-muted">{{ formatDate(s.tanggalSetor) }}</td>
                      <td class="text-center">
                        <RouterLink :to="`/simpanan/${s.id}`" class="btn btn-sm btn-outline-secondary">
                          <i class="bi bi-eye"></i>
                        </RouterLink>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>

        <!-- Riwayat Status Keanggotaan -->
        <div class="col-12 col-lg-6">
          <div class="card border-0 shadow-sm h-100">
            <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
              <h5 class="fw-bold mb-0">
                <i class="bi bi-clock-history text-primary me-2"></i>Riwayat Status Keanggotaan
              </h5>
              <RouterLink
                :to="{ path: '/riwayat-status', query: { search: anggota.nomorAnggota } }"
                class="btn btn-sm btn-outline-primary"
              >
                Lihat Semua <i class="bi bi-arrow-right ms-1"></i>
              </RouterLink>
            </div>
            <div class="card-body p-0">
              <div v-if="anggota.listRiwayatStatus.length === 0" class="text-center py-4 text-muted">
                <i class="bi bi-inbox fs-2"></i>
                <p class="mb-0 mt-1 small">Belum ada riwayat perubahan status.</p>
              </div>
              <div v-else class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                  <thead class="table-light">
                    <tr>
                      <th scope="col">Status Lama</th>
                      <th scope="col">Status Baru</th>
                      <th scope="col">Alasan</th>
                      <th scope="col">Waktu</th>
                      <th scope="col">Oleh</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="r in anggota.listRiwayatStatus" :key="r.id">
                      <td>
                        <span v-if="r.statusSebelumnya" class="badge" :class="getStatusBadge(r.statusSebelumnya).bgClass">
                          {{ getStatusBadge(r.statusSebelumnya).label }}
                        </span>
                        <span v-else class="text-muted small">-</span>
                      </td>
                      <td>
                        <span class="badge" :class="getStatusBadge(r.statusBaru).bgClass">
                          {{ getStatusBadge(r.statusBaru).label }}
                        </span>
                      </td>
                      <td class="small text-truncate" style="max-width: 160px;" :title="r.alasan || '-'">
                        {{ r.alasan || '-' }}
                      </td>
                      <td class="small text-muted">{{ formatDateTime(r.tanggalPerubahan) }}</td>
                      <td>
                        <span class="badge" :class="r.diubahOleh === 'SISTEM' ? 'bg-dark' : 'bg-secondary'">
                          {{ r.diubahOleh }}
                        </span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Form for Actions -->
    <div v-if="showModal" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.5);">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
          <div class="modal-header">
            <h5 class="modal-title fw-bold">{{ modalTitle }}</h5>
            <button type="button" class="btn-close" @click="closeModal"></button>
          </div>
          <div class="modal-body">
            <!-- Modal Error alert -->
            <div v-if="modalReasonError" class="alert alert-danger" role="alert">
              <i class="bi bi-exclamation-triangle-fill me-2"></i> {{ modalReasonError }}
            </div>

            <p class="text-muted small">
              <span v-if="modalType === 'VERIFIKASI'">
                Verifikasi data calon anggota <strong>{{ anggota?.nama }}</strong>. Status akan berubah menjadi <strong>Menunggu Setoran Pokok</strong>.
              </span>
              <span v-else-if="modalType === 'TOLAK'">
                Tolak pendaftaran <strong>{{ anggota?.nama }}</strong>. Tindakan ini bersifat final. Alasan penolakan wajib diisi.
              </span>
              <span v-else-if="modalType === 'BEKUKAN'">
                Bekukan akses keanggotaan <strong>{{ anggota?.nama }}</strong>. Alasan pembekuan wajib diisi.
              </span>
              <span v-else-if="modalType === 'AKTIFKAN'">
                Aktifkan kembali keanggotaan <strong>{{ anggota?.nama }}</strong>. Jika anggota sebelumnya Nonaktif, sistem akan memvalidasi pelunasan simpanan wajib pada 3 periode kalender selesai terakhir.
              </span>
              <span v-else-if="modalType === 'KELUAR'">
                Keluarkan <strong>{{ anggota?.nama }}</strong> dari koperasi. Tindakan ini bersifat final (soft delete bisnis). Alasan keluar wajib diisi.
              </span>
            </p>

            <div class="mb-3">
              <label class="form-label fw-semibold">
                Alasan / Catatan Perubahan
                <span v-if="['TOLAK', 'BEKUKAN', 'KELUAR'].includes(modalType || '')" class="text-danger">*</span>
                <span v-else class="text-muted fw-normal">(Opsional)</span>
              </label>
              <textarea
                class="form-control"
                rows="3"
                placeholder="Masukkan alasan atau catatan perubahan..."
                v-model="modalReason"
              ></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal" :disabled="actionLoading">
              Batal
            </button>
            <button
              type="button"
              class="btn"
              :class="{
                'btn-success': modalType === 'VERIFIKASI' || modalType === 'AKTIFKAN',
                'btn-warning': modalType === 'BEKUKAN',
                'btn-danger': modalType === 'TOLAK' || modalType === 'KELUAR',
              }"
              @click="handleModalSubmit"
              :disabled="actionLoading"
            >
              <span v-if="actionLoading" class="spinner-border spinner-border-sm me-1"></span>
              Konfirmasi Simpan
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
