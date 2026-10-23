<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { apiService, type Anggota } from '@/services/api'
import { formatDate, getStatusBadge } from '@/utils/formatters'

const route = useRoute()
const router = useRouter()

const anggotaList = ref<Anggota[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

// Filters
const search = ref<string>((route.query.search as string) || '')
const status = ref<string>((route.query.status as string) || 'ALL')

const statusOptions = [
  { value: 'ALL', label: 'Semua Status' },
  { value: 'MENUNGGU_VERIFIKASI', label: 'Menunggu Verifikasi' },
  { value: 'MENUNGGU_SETORAN_POKOK', label: 'Menunggu Setoran Pokok' },
  { value: 'AKTIF', label: 'Aktif' },
  { value: 'DIBEKUKAN', label: 'Dibekukan' },
  { value: 'NONAKTIF', label: 'Nonaktif' },
  { value: 'DITOLAK', label: 'Ditolak' },
  { value: 'KELUAR', label: 'Keluar' },
]

const fetchAnggota = async () => {
  loading.value = true
  error.value = null
  try {
    const res = await apiService.getAllAnggota(search.value, status.value)
    anggotaList.value = res.data.data
  } catch (err: any) {
    error.value = err.response?.data?.message || err.message || 'Gagal memuat daftar anggota'
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  router.replace({
    path: '/anggota',
    query: {
      ...(search.value ? { search: search.value } : {}),
      ...(status.value && status.value !== 'ALL' ? { status: status.value } : {}),
    },
  })
  fetchAnggota()
}

const resetFilter = () => {
  search.value = ''
  status.value = 'ALL'
  handleFilterChange()
}

watch(
  () => route.query,
  (newQuery) => {
    if (newQuery.status !== undefined) {
      status.value = (newQuery.status as string) || 'ALL'
    }
    if (newQuery.search !== undefined) {
      search.value = (newQuery.search as string) || ''
    }
  }
)

onMounted(() => {
  fetchAnggota()
})
</script>

<template>
  <div class="anggota-list-view">
    <!-- Header -->
    <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 gap-2">
      <div>
        <h3 class="fw-bold mb-1">
          <i class="bi bi-people-fill text-primary me-2"></i>Daftar Anggota Koperasi
        </h3>
        <p class="text-muted mb-0">Kelola seluruh data warga desa yang terdaftar dalam sistem koperasi</p>
      </div>
      <div>
        <RouterLink to="/anggota/create" class="btn btn-primary fw-semibold shadow-sm">
          <i class="bi bi-person-plus-fill me-1"></i> Tambah Anggota Baru
        </RouterLink>
      </div>
    </div>

    <!-- Filter Card -->
    <div class="card border-0 shadow-sm mb-4">
      <div class="card-body p-3">
        <div class="row g-2 align-items-center">
          <!-- Search Bar -->
          <div class="col-12 col-md-6 col-lg-7">
            <div class="input-group">
              <span class="input-group-text bg-white border-end-0">
                <i class="bi bi-search text-muted"></i>
              </span>
              <input
                type="text"
                class="form-control border-start-0"
                placeholder="Cari berdasarkan No. Anggota, Nama, NIK, atau Email..."
                v-model="search"
                @keyup.enter="handleFilterChange"
              />
              <button class="btn btn-outline-secondary" type="button" @click="handleFilterChange">
                Cari
              </button>
            </div>
          </div>

          <!-- Status Filter Dropdown -->
          <div class="col-12 col-sm-8 col-md-4 col-lg-3">
            <select class="form-select" v-model="status" @change="handleFilterChange">
              <option v-for="opt in statusOptions" :key="opt.value" :value="opt.value">
                {{ opt.label }}
              </option>
            </select>
          </div>

          <!-- Reset Button -->
          <div class="col-12 col-sm-4 col-md-2 col-lg-2 text-end">
            <button class="btn btn-light border w-100" @click="resetFilter" title="Reset Filter">
              <i class="bi bi-arrow-clockwise me-1"></i> Reset
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
          <p class="mt-2 text-muted">Memuat data anggota...</p>
        </div>

        <!-- Empty State -->
        <div v-else-if="anggotaList.length === 0" class="text-center py-5">
          <i class="bi bi-inbox fs-1 text-muted"></i>
          <p class="mt-2 text-muted fw-semibold">Tidak ada data anggota yang sesuai dengan kriteria pencarian.</p>
          <button class="btn btn-sm btn-outline-primary" @click="resetFilter">Reset Filter</button>
        </div>

        <!-- Table -->
        <div v-else class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th scope="col" class="text-center" style="width: 50px;">No</th>
                <th scope="col">ID Anggota</th>
                <th scope="col">No. Anggota</th>
                <th scope="col">Nama Lengkap</th>
                <th scope="col">NIK</th>
                <th scope="col">Email</th>
                <th scope="col">Telepon</th>
                <th scope="col" class="text-center">Status</th>
                <th scope="col">Tgl Daftar</th>
                <th scope="col" class="text-center" style="width: 90px;">Aksi</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(anggota, index) in anggotaList" :key="anggota.id">
                <td class="text-center text-muted small">{{ index + 1 }}</td>
                <td class="fw-semibold text-primary font-monospace">{{ anggota.id }}</td>
                <td class="fw-semibold font-monospace">{{ anggota.nomorAnggota }}</td>
                <td class="fw-bold">{{ anggota.nama }}</td>
                <td class="font-monospace text-muted small">{{ anggota.nik }}</td>
                <td>{{ anggota.email }}</td>
                <td>{{ anggota.telepon }}</td>
                <td class="text-center">
                  <span class="badge" :class="getStatusBadge(anggota.statusKeanggotaan).bgClass">
                    {{ getStatusBadge(anggota.statusKeanggotaan).label }}
                  </span>
                </td>
                <td class="small text-muted">{{ formatDate(anggota.tanggalDaftar) }}</td>
                <td class="text-center">
                  <RouterLink :to="`/anggota/${anggota.id}`" class="btn btn-sm btn-outline-primary">
                    <i class="bi bi-eye-fill me-1"></i> Detail
                  </RouterLink>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div v-if="anggotaList.length > 0" class="card-footer bg-white border-top text-muted small">
        Menampilkan <strong>{{ anggotaList.length }}</strong> data anggota (terurut ascending berdasarkan ID).
      </div>
    </div>
  </div>
</template>
