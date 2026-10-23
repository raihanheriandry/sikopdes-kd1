<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { apiService, type Simpanan } from '@/services/api'
import { formatDate, formatDateTime, formatRupiah, getJenisSimpananBadge } from '@/utils/formatters'

const route = useRoute()
const simpananId = route.params.id as string

const simpanan = ref<Simpanan | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)

const fetchDetail = async () => {
  loading.value = true
  error.value = null
  try {
    const res = await apiService.getSimpananById(simpananId)
    simpanan.value = res.data.data
  } catch (err: any) {
    error.value = err.response?.data?.message || err.message || 'Gagal memuat detail simpanan'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<template>
  <div class="simpanan-detail-view">
    <div class="row justify-content-center">
      <div class="col-12 col-md-8 col-lg-7">
        <!-- Back Link -->
        <RouterLink to="/simpanan" class="btn btn-sm btn-outline-secondary mb-3">
          <i class="bi bi-arrow-left me-1"></i> Kembali ke Daftar Simpanan
        </RouterLink>

        <!-- Loading -->
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Memuat data...</span>
          </div>
          <p class="mt-2 text-muted">Memuat detail transaksi simpanan...</p>
        </div>

        <!-- Error -->
        <div v-else-if="error" class="alert alert-danger" role="alert">
          <i class="bi bi-exclamation-triangle-fill me-2"></i> {{ error }}
        </div>

        <!-- Detail Card -->
        <div v-else-if="simpanan" class="card border-0 shadow-sm">
          <div class="card-header bg-white py-3 border-bottom d-flex justify-content-between align-items-center">
            <div>
              <h4 class="fw-bold mb-0 text-dark">
                <i class="bi bi-receipt text-success me-2"></i>Detail Transaksi Simpanan
              </h4>
              <span class="text-muted small font-monospace">ID: {{ simpanan.id }}</span>
            </div>
            <span class="badge" :class="getJenisSimpananBadge(simpanan.jenisSimpanan).bgClass">
              {{ getJenisSimpananBadge(simpanan.jenisSimpanan).label }}
            </span>
          </div>

          <div class="card-body p-4">
            <!-- Amount Banner -->
            <div class="text-center py-4 bg-light rounded-3 mb-4">
              <span class="text-muted small text-uppercase fw-semibold d-block">Nominal Setoran</span>
              <h1 class="display-6 fw-bold text-success mb-0">{{ formatRupiah(simpanan.nominal) }}</h1>
            </div>

            <!-- Details List -->
            <div class="row g-3">
              <div class="col-sm-6">
                <span class="text-muted small d-block">Nomor Anggota</span>
                <RouterLink
                  :to="`/anggota/${simpanan.anggotaId}`"
                  class="fw-bold font-monospace fs-5 text-primary text-decoration-none"
                >
                  {{ simpanan.nomorAnggota || simpanan.anggotaId }} <i class="bi bi-box-arrow-up-right fs-6"></i>
                </RouterLink>
              </div>

              <div class="col-sm-6">
                <span class="text-muted small d-block">Nama Lengkap Anggota</span>
                <span class="fw-bold fs-5 text-dark">{{ simpanan.namaAnggota || '-' }}</span>
              </div>

              <div class="col-sm-6">
                <span class="text-muted small d-block">Jenis Simpanan</span>
                <span class="fw-semibold">{{ simpanan.jenisSimpanan }}</span>
              </div>

              <div class="col-sm-6" v-if="simpanan.jenisSimpanan === 'WAJIB'">
                <span class="text-muted small d-block">Periode Simpanan Wajib</span>
                <span class="fw-bold font-monospace text-primary fs-6">{{ simpanan.periode }}</span>
              </div>

              <div class="col-sm-6">
                <span class="text-muted small d-block">Tanggal Setor</span>
                <span class="fw-semibold">{{ formatDate(simpanan.tanggalSetor) }}</span>
              </div>

              <div class="col-sm-6">
                <span class="text-muted small d-block">Waktu Pencatatan Sistem</span>
                <span class="text-muted small">{{ formatDateTime(simpanan.createdAt) }}</span>
              </div>
            </div>

            <hr class="my-4" />

            <div class="alert alert-secondary small mb-0 d-flex align-items-center gap-2">
              <i class="bi bi-shield-lock-fill fs-5 text-secondary"></i>
              <div>
                <strong>Transaksi Bersifat Final:</strong> Data transaksi simpanan ini tidak dapat diubah maupun dihapus untuk menjaga integritas buku kas koperasi.
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
