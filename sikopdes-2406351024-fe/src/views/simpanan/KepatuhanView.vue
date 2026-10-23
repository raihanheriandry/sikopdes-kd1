<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { apiService, type KepatuhanData } from '@/services/api'
import { formatDateTime, getStatusBadge } from '@/utils/formatters'

const dataKepatuhan = ref<KepatuhanData | null>(null)
const bulanAcuan = ref(new Date().toISOString().substring(0, 7)) // e.g. "2026-10"
const loading = ref(true)
const jobLoading = ref(false)
const error = ref<string | null>(null)
const successMsg = ref<string | null>(null)

const fetchKepatuhan = async () => {
  loading.value = true
  error.value = null
  try {
    const res = await apiService.getKepatuhan(bulanAcuan.value)
    dataKepatuhan.value = res.data.data
  } catch (err: any) {
    error.value = err.response?.data?.message || err.message || 'Gagal memuat data kepatuhan'
  } finally {
    loading.value = false
  }
}

const triggerJob = async () => {
  jobLoading.value = true
  error.value = null
  successMsg.value = null
  try {
    const res = await apiService.triggerKepatuhanJob()
    const job = res.data.data
    successMsg.value = `Job kepatuhan berhasil dijalankan! (ID: ${job.id}, Diperiksa: ${job.jumlahDiperiksa}, Dinonaktifkan: ${job.jumlahDinonaktifkan})`
    await fetchKepatuhan()
  } catch (err: any) {
    error.value = err.response?.data?.message || err.message || 'Gagal menjalankan job kepatuhan'
  } finally {
    jobLoading.value = false
  }
}

onMounted(() => {
  fetchKepatuhan()
})
</script>

<template>
  <div class="kepatuhan-view">
    <!-- Header -->
    <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 gap-2">
      <div>
        <h3 class="fw-bold mb-1">
          <i class="bi bi-shield-check text-warning me-2"></i>Kepatuhan Simpanan Wajib
        </h3>
        <p class="text-muted mb-0">Evaluasi kepatuhan 3 periode kalender selesai terakhir bagi anggota Aktif dan Nonaktif</p>
      </div>
      <div>
        <button class="btn btn-primary" @click="triggerJob" :disabled="jobLoading">
          <span v-if="jobLoading" class="spinner-border spinner-border-sm me-1"></span>
          <i v-else class="bi bi-play-circle-fill me-1"></i>
          Jalankan Pemeriksaan Otomatis Sekarang
        </button>
      </div>
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

    <!-- Top Section: 5 Eksekusi Pemeriksaan Otomatis Terakhir -->
    <div class="card border-0 shadow-sm mb-4">
      <div class="card-header bg-white py-3 border-bottom d-flex justify-content-between align-items-center">
        <h5 class="fw-bold mb-0 text-dark">
          <i class="bi bi-clock-history text-primary me-2"></i>5 Riwayat Eksekusi Pemeriksaan Otomatis Terakhir
        </h5>
        <span class="badge bg-light text-muted border">Background Scheduled Job</span>
      </div>
      <div class="card-body p-0">
        <div v-if="!dataKepatuhan || dataKepatuhan.ringkasanEksekusi.length === 0" class="text-center py-4 text-muted small">
          Belum ada riwayat eksekusi pemeriksaan otomatis.
        </div>
        <div v-else class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th scope="col">ID Job</th>
                <th scope="col">Waktu Eksekusi</th>
                <th scope="col">Periode Diperiksa</th>
                <th scope="col" class="text-center">Jumlah Diperiksa</th>
                <th scope="col" class="text-center">Jumlah Dinonaktifkan</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="job in dataKepatuhan.ringkasanEksekusi" :key="job.id">
                <td class="fw-semibold font-monospace text-primary">{{ job.id }}</td>
                <td class="small">{{ formatDateTime(job.waktuEksekusi) }}</td>
                <td class="font-monospace small">{{ job.periodeAwal }} s.d. {{ job.periodeAkhir }}</td>
                <td class="text-center fw-semibold">{{ job.jumlahDiperiksa }} Anggota</td>
                <td class="text-center">
                  <span
                    class="badge"
                    :class="job.jumlahDinonaktifkan > 0 ? 'bg-danger text-white' : 'bg-success text-white'"
                  >
                    {{ job.jumlahDinonaktifkan }} Dinonaktifkan
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Filter Bulan Acuan Card -->
    <div class="card border-0 shadow-sm mb-4">
      <div class="card-body p-3">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-md-5">
            <label class="form-label text-muted small mb-1 fw-semibold">Pilih Bulan Acuan Evaluasi</label>
            <div class="input-group">
              <span class="input-group-text bg-white border-end-0">
                <i class="bi bi-calendar-month text-muted"></i>
              </span>
              <input
                type="month"
                class="form-control border-start-0"
                v-model="bulanAcuan"
                @change="fetchKepatuhan"
              />
              <button class="btn btn-outline-primary" type="button" @click="fetchKepatuhan">
                Tampilkan
              </button>
            </div>
          </div>
          <div class="col-12 col-md-7 text-md-end text-muted small">
            <span v-if="dataKepatuhan?.periodeEvaluasi">
              Periode Selesai Dievaluasi:
              <strong class="text-primary">{{ dataKepatuhan.periodeEvaluasi.join(', ') }}</strong>
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Table Evaluasi Kepatuhan Anggota -->
    <div class="card border-0 shadow-sm">
      <div class="card-header bg-white py-3 border-bottom d-flex justify-content-between align-items-center">
        <h5 class="fw-bold mb-0 text-dark">
          <i class="bi bi-person-lines-fill text-success me-2"></i>Daftar Kepatuhan Anggota (Aktif &amp; Nonaktif)
        </h5>
        <span class="badge bg-light text-muted border" v-if="dataKepatuhan">
          {{ dataKepatuhan.daftarAnggota.length }} Anggota Terdata
        </span>
      </div>

      <div class="card-body p-0">
        <!-- Loading -->
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Memuat data...</span>
          </div>
          <p class="mt-2 text-muted">Memuat data evaluasi kepatuhan...</p>
        </div>

        <!-- Table -->
        <div v-else-if="dataKepatuhan && dataKepatuhan.daftarAnggota.length > 0" class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th scope="col" class="text-center" style="width: 50px;">No</th>
                <th scope="col">No. Anggota</th>
                <th scope="col">Nama Lengkap</th>
                <th scope="col" class="text-center">Status</th>
                <!-- Columns for the 3 evaluated periods -->
                <th
                  v-for="p in dataKepatuhan.periodeEvaluasi"
                  :key="p"
                  scope="col"
                  class="text-center"
                >
                  {{ p }}
                </th>
                <th scope="col" class="text-center">Hasil Kepatuhan</th>
                <th scope="col" class="text-center" style="width: 140px;">Aksi</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(a, idx) in dataKepatuhan.daftarAnggota" :key="a.id">
                <td class="text-center text-muted small">{{ idx + 1 }}</td>
                <td class="fw-semibold font-monospace">{{ a.nomorAnggota }}</td>
                <td class="fw-bold">{{ a.nama }}</td>
                <td class="text-center">
                  <span class="badge" :class="getStatusBadge(a.statusKeanggotaan).bgClass">
                    {{ getStatusBadge(a.statusKeanggotaan).label }}
                  </span>
                </td>

                <!-- Period Paid / Unpaid Badges -->
                <td
                  v-for="p in dataKepatuhan.periodeEvaluasi"
                  :key="p"
                  class="text-center"
                >
                  <span
                    v-if="a.statusBayarPerPeriode[p]"
                    class="badge bg-success-subtle text-success border border-success"
                  >
                    <i class="bi bi-check-circle-fill me-1"></i> Terbayar
                  </span>
                  <span
                    v-else
                    class="badge bg-danger-subtle text-danger border border-danger"
                  >
                    <i class="bi bi-x-circle-fill me-1"></i> Belum
                  </span>
                </td>

                <!-- Hasil Kepatuhan -->
                <td class="text-center">
                  <span
                    v-if="a.hasilKepatuhan === 'PATUH'"
                    class="badge bg-success p-2"
                  >
                    <i class="bi bi-shield-fill-check me-1"></i> Patuh
                  </span>
                  <span
                    v-else-if="a.hasilKepatuhan === 'MENUNGGAK'"
                    class="badge bg-danger p-2"
                  >
                    <i class="bi bi-shield-fill-x me-1"></i> Menunggak
                  </span>
                  <span
                    v-else
                    class="badge bg-secondary p-2"
                  >
                    <i class="bi bi-dash-circle me-1"></i> Belum Dievaluasi
                  </span>
                </td>

                <!-- Action Buttons -->
                <td class="text-center">
                  <div class="d-flex justify-content-center gap-1">
                    <RouterLink
                      :to="`/anggota/${a.id}`"
                      class="btn btn-sm btn-outline-primary"
                      title="Lihat Detail Anggota"
                    >
                      <i class="bi bi-eye"></i>
                    </RouterLink>
                    <RouterLink
                      :to="{ path: '/simpanan/create', query: { anggotaId: a.id, jenis: 'WAJIB' } }"
                      class="btn btn-sm btn-outline-success"
                      title="Catat Simpanan Wajib"
                    >
                      <i class="bi bi-cash"></i>
                    </RouterLink>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>
