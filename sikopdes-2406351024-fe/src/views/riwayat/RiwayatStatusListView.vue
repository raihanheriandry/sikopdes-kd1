<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { apiService, type RiwayatStatusAnggota } from '@/services/api'
import { formatDateTime, getStatusBadge } from '@/utils/formatters'

const route = useRoute()

const riwayatList = ref<RiwayatStatusAnggota[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

// Filters
const search = ref((route.query.search as string) || '')
const statusBaru = ref('ALL')
const startDate = ref('')
const endDate = ref('')

const statusOptions = [
  { value: 'ALL', label: 'Semua Status Baru' },
  { value: 'MENUNGGU_VERIFIKASI', label: 'Menunggu Verifikasi' },
  { value: 'MENUNGGU_SETORAN_POKOK', label: 'Menunggu Setoran Pokok' },
  { value: 'AKTIF', label: 'Aktif' },
  { value: 'DIBEKUKAN', label: 'Dibekukan' },
  { value: 'NONAKTIF', label: 'Nonaktif' },
  { value: 'DITOLAK', label: 'Ditolak' },
  { value: 'KELUAR', label: 'Keluar' },
]

const fetchRiwayat = async () => {
  loading.value = true
  error.value = null
  try {
    const res = await apiService.getAllRiwayat(
      search.value,
      statusBaru.value,
      startDate.value || undefined,
      endDate.value || undefined
    )
    riwayatList.value = res.data.data
  } catch (err: any) {
    error.value = err.response?.data?.message || err.message || 'Gagal memuat riwayat status'
  } finally {
    loading.value = false
  }
}

const resetFilter = () => {
  search.value = ''
  statusBaru.value = 'ALL'
  startDate.value = ''
  endDate.value = ''
  fetchRiwayat()
}

onMounted(() => {
  fetchRiwayat()
})
</script>

<template>
  <div class="riwayat-status-list-view">
    <!-- Header -->
    <div class="mb-4">
      <h3 class="fw-bold mb-1">
        <i class="bi bi-clock-history text-primary me-2"></i>Audit Trail Riwayat Status Keanggotaan
      </h3>
      <p class="text-muted mb-0">Catatan kronologis perubahan status keanggotaan oleh pengurus maupun job otomatis sistem</p>
    </div>

    <!-- Filter Card -->
    <div class="card border-0 shadow-sm mb-4">
      <div class="card-body p-3">
        <div class="row g-2 align-items-end">
          <!-- Search Bar -->
          <div class="col-12 col-md-4">
            <label class="form-label text-muted small mb-1">Pencarian</label>
            <div class="input-group">
              <span class="input-group-text bg-white border-end-0">
                <i class="bi bi-search text-muted"></i>
              </span>
              <input
                type="text"
                class="form-control border-start-0"
                placeholder="Cari No. Anggota atau Nama..."
                v-model="search"
                @keyup.enter="fetchRiwayat"
              />
            </div>
          </div>

          <!-- Status Baru Filter -->
          <div class="col-6 col-md-3">
            <label class="form-label text-muted small mb-1">Status Baru</label>
            <select class="form-select" v-model="statusBaru" @change="fetchRiwayat">
              <option v-for="opt in statusOptions" :key="opt.value" :value="opt.value">
                {{ opt.label }}
              </option>
            </select>
          </div>

          <!-- Rentang Tanggal: Start -->
          <div class="col-6 col-md-2">
            <label class="form-label text-muted small mb-1">Dari Tanggal</label>
            <input type="date" class="form-control" v-model="startDate" @change="fetchRiwayat" />
          </div>

          <!-- Rentang Tanggal: End -->
          <div class="col-6 col-md-2">
            <label class="form-label text-muted small mb-1">Sampai Tanggal</label>
            <input type="date" class="form-control" v-model="endDate" @change="fetchRiwayat" />
          </div>

          <!-- Action Buttons -->
          <div class="col-6 col-md-1 text-end">
            <button class="btn btn-light border w-100" @click="resetFilter" title="Reset Filter">
              <i class="bi bi-arrow-clockwise"></i>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Error Alert -->
    <div v-if="error" class="alert alert-danger" role="alert">
      <i class="bi bi-exclamation-triangle-fill me-2"></i> {{ error }}
    </div>

    <!-- Table Card -->
    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <!-- Loading -->
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Memuat data...</span>
          </div>
          <p class="mt-2 text-muted">Memuat data riwayat status...</p>
        </div>

        <!-- Empty State -->
        <div v-else-if="riwayatList.length === 0" class="text-center py-5">
          <i class="bi bi-inbox fs-1 text-muted"></i>
          <p class="mt-2 text-muted fw-semibold">Tidak ada riwayat perubahan status yang ditemukan.</p>
          <button class="btn btn-sm btn-outline-primary" @click="resetFilter">Reset Filter</button>
        </div>

        <!-- Table -->
        <div v-else class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th scope="col" class="text-center" style="width: 50px;">No</th>
                <th scope="col">ID Riwayat</th>
                <th scope="col">No. Anggota</th>
                <th scope="col">Nama Anggota</th>
                <th scope="col">Status Sebelumnya</th>
                <th scope="col">Status Baru</th>
                <th scope="col">Alasan Perubahan</th>
                <th scope="col">Waktu Perubahan</th>
                <th scope="col">Diubah Oleh</th>
                <th scope="col" class="text-center" style="width: 80px;">Aksi</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(r, index) in riwayatList" :key="r.id">
                <td class="text-center text-muted small">{{ index + 1 }}</td>
                <td class="fw-semibold font-monospace text-primary">{{ r.id }}</td>
                <td>
                  <RouterLink :to="`/anggota/${r.anggotaId}`" class="fw-semibold font-monospace text-decoration-none">
                    {{ r.nomorAnggota }}
                  </RouterLink>
                </td>
                <td class="fw-bold">{{ r.namaAnggota }}</td>
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
                <td class="small text-truncate" style="max-width: 200px;" :title="r.alasan || '-'">
                  {{ r.alasan || '-' }}
                </td>
                <td class="small text-muted">{{ formatDateTime(r.tanggalPerubahan) }}</td>
                <td>
                  <span class="badge" :class="r.diubahOleh === 'SISTEM' ? 'bg-dark text-white' : 'bg-secondary text-white'">
                    {{ r.diubahOleh }}
                  </span>
                </td>
                <td class="text-center">
                  <RouterLink :to="`/riwayat-status/${r.id}`" class="btn btn-sm btn-outline-primary">
                    <i class="bi bi-eye"></i> Detail
                  </RouterLink>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div v-if="riwayatList.length > 0" class="card-footer bg-white border-top text-muted small">
        Menampilkan <strong>{{ riwayatList.length }}</strong> data riwayat status (terurut descending berdasarkan tanggal perubahan).
      </div>
    </div>
  </div>
</template>
