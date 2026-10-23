<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { apiService, type Simpanan } from '@/services/api'
import { formatDate, formatRupiah, getJenisSimpananBadge } from '@/utils/formatters'

const simpananList = ref<Simpanan[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

// Filters
const search = ref('')
const jenis = ref('ALL')
const periode = ref('')
const nominalMin = ref<number | undefined>(undefined)
const nominalMax = ref<number | undefined>(undefined)

const fetchSimpanan = async () => {
  loading.value = true
  error.value = null
  try {
    const res = await apiService.getAllSimpanan(
      search.value,
      jenis.value,
      periode.value,
      nominalMin.value,
      nominalMax.value
    )
    simpananList.value = res.data.data
  } catch (err: any) {
    error.value = err.response?.data?.message || err.message || 'Gagal memuat transaksi simpanan'
  } finally {
    loading.value = false
  }
}

const resetFilter = () => {
  search.value = ''
  jenis.value = 'ALL'
  periode.value = ''
  nominalMin.value = undefined
  nominalMax.value = undefined
  fetchSimpanan()
}

onMounted(() => {
  fetchSimpanan()
})
</script>

<template>
  <div class="simpanan-list-view">
    <!-- Header -->
    <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 gap-2">
      <div>
        <h3 class="fw-bold mb-1">
          <i class="bi bi-wallet2 text-success me-2"></i>Daftar Transaksi Simpanan
        </h3>
        <p class="text-muted mb-0">Catatan mutasi setoran simpanan pokok, wajib, dan sukarela anggota koperasi</p>
      </div>
      <div class="d-flex flex-wrap gap-2">
        <RouterLink to="/simpanan/pokok/create" class="btn btn-outline-primary fw-semibold">
          <i class="bi bi-plus-circle me-1"></i> Tambah Simpanan Pokok
        </RouterLink>
        <RouterLink to="/simpanan/create" class="btn btn-success fw-semibold">
          <i class="bi bi-cash-stack me-1"></i> Tambah Simpanan
        </RouterLink>
      </div>
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
                placeholder="ID, No. Anggota, Nama..."
                v-model="search"
                @keyup.enter="fetchSimpanan"
              />
            </div>
          </div>

          <!-- Filter Jenis -->
          <div class="col-6 col-md-2">
            <label class="form-label text-muted small mb-1">Jenis Simpanan</label>
            <select class="form-select" v-model="jenis" @change="fetchSimpanan">
              <option value="ALL">Semua Jenis</option>
              <option value="POKOK">Pokok</option>
              <option value="WAJIB">Wajib</option>
              <option value="SUKARELA">Sukarela</option>
            </select>
          </div>

          <!-- Filter Periode -->
          <div class="col-6 col-md-2">
            <label class="form-label text-muted small mb-1">Periode (Wajib)</label>
            <input
              type="text"
              class="form-control font-monospace"
              placeholder="YYYY-MM"
              v-model="periode"
              @keyup.enter="fetchSimpanan"
            />
          </div>

          <!-- Nominal Min -->
          <div class="col-6 col-md-2">
            <label class="form-label text-muted small mb-1">Nominal Min (Rp)</label>
            <input
              type="number"
              class="form-control"
              placeholder="Min"
              v-model.number="nominalMin"
              @keyup.enter="fetchSimpanan"
            />
          </div>

          <!-- Nominal Max -->
          <div class="col-6 col-md-2">
            <label class="form-label text-muted small mb-1">Nominal Max (Rp)</label>
            <input
              type="number"
              class="form-control"
              placeholder="Max"
              v-model.number="nominalMax"
              @keyup.enter="fetchSimpanan"
            />
          </div>

          <!-- Filter & Reset Buttons -->
          <div class="col-12 d-flex justify-content-end gap-2 mt-2">
            <button class="btn btn-light border" @click="resetFilter">
              <i class="bi bi-arrow-clockwise me-1"></i> Reset
            </button>
            <button class="btn btn-primary" @click="fetchSimpanan">
              <i class="bi bi-funnel me-1"></i> Terapkan Filter
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
          <p class="mt-2 text-muted">Memuat data transaksi simpanan...</p>
        </div>

        <!-- Empty State -->
        <div v-else-if="simpananList.length === 0" class="text-center py-5">
          <i class="bi bi-inbox fs-1 text-muted"></i>
          <p class="mt-2 text-muted fw-semibold">Tidak ada transaksi simpanan yang sesuai dengan filter.</p>
          <button class="btn btn-sm btn-outline-primary" @click="resetFilter">Reset Filter</button>
        </div>

        <!-- Table -->
        <div v-else class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th scope="col" class="text-center" style="width: 50px;">No</th>
                <th scope="col">ID Simpanan</th>
                <th scope="col">No. Anggota</th>
                <th scope="col">Nama Anggota</th>
                <th scope="col">Jenis</th>
                <th scope="col">Nominal</th>
                <th scope="col">Periode</th>
                <th scope="col">Tanggal Setor</th>
                <th scope="col" class="text-center" style="width: 90px;">Aksi</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(simpanan, index) in simpananList" :key="simpanan.id">
                <td class="text-center text-muted small">{{ index + 1 }}</td>
                <td class="fw-semibold text-primary font-monospace">{{ simpanan.id }}</td>
                <td>
                  <RouterLink :to="`/anggota/${simpanan.anggotaId}`" class="fw-semibold font-monospace text-decoration-none">
                    {{ simpanan.nomorAnggota }}
                  </RouterLink>
                </td>
                <td class="fw-semibold">{{ simpanan.namaAnggota }}</td>
                <td>
                  <span class="badge" :class="getJenisSimpananBadge(simpanan.jenisSimpanan).bgClass">
                    {{ getJenisSimpananBadge(simpanan.jenisSimpanan).label }}
                  </span>
                </td>
                <td class="fw-bold">{{ formatRupiah(simpanan.nominal) }}</td>
                <td class="font-monospace small">{{ simpanan.periode || '-' }}</td>
                <td class="small text-muted">{{ formatDate(simpanan.tanggalSetor) }}</td>
                <td class="text-center">
                  <RouterLink :to="`/simpanan/${simpanan.id}`" class="btn btn-sm btn-outline-primary">
                    <i class="bi bi-eye me-1"></i> Detail
                  </RouterLink>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div v-if="simpananList.length > 0" class="card-footer bg-white border-top text-muted small">
        Menampilkan <strong>{{ simpananList.length }}</strong> transaksi simpanan (terurut descending berdasarkan tanggal setor).
      </div>
    </div>
  </div>
</template>
