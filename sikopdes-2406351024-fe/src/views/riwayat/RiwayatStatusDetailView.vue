<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { apiService, type RiwayatStatusAnggota } from '@/services/api'
import { formatDateTime, getStatusBadge } from '@/utils/formatters'

const route = useRoute()
const riwayatId = route.params.id as string

const riwayat = ref<RiwayatStatusAnggota | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)

const fetchDetail = async () => {
  loading.value = true
  error.value = null
  try {
    const res = await apiService.getRiwayatById(riwayatId)
    riwayat.value = res.data.data
  } catch (err: any) {
    error.value = err.response?.data?.message || err.message || 'Gagal memuat detail riwayat status'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<template>
  <div class="riwayat-status-detail-view">
    <div class="row justify-content-center">
      <div class="col-12 col-md-8 col-lg-7">
        <!-- Back Link -->
        <RouterLink to="/riwayat-status" class="btn btn-sm btn-outline-secondary mb-3">
          <i class="bi bi-arrow-left me-1"></i> Kembali ke Daftar Riwayat
        </RouterLink>

        <!-- Loading -->
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Memuat data...</span>
          </div>
          <p class="mt-2 text-muted">Memuat detail riwayat status...</p>
        </div>

        <!-- Error -->
        <div v-else-if="error" class="alert alert-danger" role="alert">
          <i class="bi bi-exclamation-triangle-fill me-2"></i> {{ error }}
        </div>

        <!-- Detail Card -->
        <div v-else-if="riwayat" class="card border-0 shadow-sm mb-4">
          <div class="card-header bg-white py-3 border-bottom d-flex justify-content-between align-items-center">
            <div>
              <h4 class="fw-bold mb-0 text-dark">
                <i class="bi bi-clock-history text-primary me-2"></i>Detail Perubahan Status
              </h4>
              <span class="text-muted small font-monospace">ID: {{ riwayat.id }}</span>
            </div>
            <span class="badge" :class="riwayat.diubahOleh === 'SISTEM' ? 'bg-dark' : 'bg-primary'">
              {{ riwayat.diubahOleh }}
            </span>
          </div>

          <div class="card-body p-4">
            <!-- Status Transition Banner -->
            <div class="p-3 bg-light rounded-3 mb-4 d-flex justify-content-center align-items-center gap-3">
              <div>
                <span class="text-muted small d-block text-center">Status Sebelumnya</span>
                <span v-if="riwayat.statusSebelumnya" class="badge p-2 fs-6" :class="getStatusBadge(riwayat.statusSebelumnya).bgClass">
                  {{ getStatusBadge(riwayat.statusSebelumnya).label }}
                </span>
                <span v-else class="text-muted fw-bold d-block text-center">Status Awal (Null)</span>
              </div>

              <i class="bi bi-arrow-right fs-3 text-muted"></i>

              <div>
                <span class="text-muted small d-block text-center">Status Baru</span>
                <span class="badge p-2 fs-6" :class="getStatusBadge(riwayat.statusBaru).bgClass">
                  {{ getStatusBadge(riwayat.statusBaru).label }}
                </span>
              </div>
            </div>

            <!-- Details Grid -->
            <div class="row g-3">
              <div class="col-sm-6">
                <span class="text-muted small d-block">Nomor Anggota</span>
                <RouterLink
                  :to="`/anggota/${riwayat.anggotaId}`"
                  class="fw-bold font-monospace fs-5 text-primary text-decoration-none"
                >
                  {{ riwayat.nomorAnggota || riwayat.anggotaId }} <i class="bi bi-box-arrow-up-right fs-6"></i>
                </RouterLink>
              </div>

              <div class="col-sm-6">
                <span class="text-muted small d-block">Nama Lengkap Anggota</span>
                <span class="fw-bold fs-5 text-dark">{{ riwayat.namaAnggota || '-' }}</span>
              </div>

              <div class="col-sm-6">
                <span class="text-muted small d-block">Waktu Perubahan</span>
                <span class="fw-semibold">{{ formatDateTime(riwayat.tanggalPerubahan) }}</span>
              </div>

              <div class="col-sm-6">
                <span class="text-muted small d-block">Pihak Pengubah</span>
                <span class="fw-semibold">{{ riwayat.diubahOleh }}</span>
              </div>

              <div class="col-12">
                <span class="text-muted small d-block">Alasan / Catatan Perubahan</span>
                <div class="p-3 bg-light rounded border mt-1">
                  {{ riwayat.alasan || 'Tidak ada alasan yang dicatat.' }}
                </div>
              </div>
            </div>

            <!-- If Diubah Oleh SISTEM, show Pemeriksaan details -->
            <div v-if="riwayat.diubahOleh === 'SISTEM' && riwayat.pemeriksaan" class="mt-4 pt-3 border-top">
              <h6 class="fw-bold text-dark mb-3">
                <i class="bi bi-cpu-fill text-warning me-2"></i>Informasi Eksekusi Job Pemeriksaan Kepatuhan
              </h6>
              <div class="row g-3 p-3 rounded bg-light border">
                <div class="col-sm-6">
                  <span class="text-muted small d-block">ID Pemeriksaan</span>
                  <strong class="font-monospace text-primary">{{ riwayat.pemeriksaan.id }}</strong>
                </div>
                <div class="col-sm-6">
                  <span class="text-muted small d-block">Waktu Eksekusi Job</span>
                  <span>{{ formatDateTime(riwayat.pemeriksaan.waktuEksekusi) }}</span>
                </div>
                <div class="col-sm-6">
                  <span class="text-muted small d-block">Rentang Periode Diperiksa</span>
                  <span class="font-monospace fw-semibold">
                    {{ riwayat.pemeriksaan.periodeAwal }} s.d. {{ riwayat.pemeriksaan.periodeAkhir }}
                  </span>
                </div>
                <div class="col-sm-6">
                  <span class="text-muted small d-block">Hasil Job</span>
                  <span>
                    Diperiksa: <strong>{{ riwayat.pemeriksaan.jumlahDiperiksa }}</strong> | Dinonaktifkan:
                    <strong class="text-danger">{{ riwayat.pemeriksaan.jumlahDinonaktifkan }}</strong>
                  </span>
                </div>
              </div>
            </div>

            <hr class="my-4" />

            <div class="alert alert-secondary small mb-0 d-flex align-items-center gap-2">
              <i class="bi bi-shield-lock-fill fs-5 text-secondary"></i>
              <div>
                <strong>Audit Trail Bersifat Append-Only:</strong> Data riwayat status ini tidak dapat diubah atau dihapus untuk menjamin akuntabilitas perubahan data sistem.
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
