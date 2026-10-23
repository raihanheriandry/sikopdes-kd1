<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { apiService, type DashboardStats } from '@/services/api'
import { formatRupiah } from '@/utils/formatters'

const router = useRouter()
const stats = ref<DashboardStats | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)

const fetchDashboard = async () => {
  loading.value = true
  error.value = null
  try {
    const res = await apiService.getDashboard()
    stats.value = res.data.data
  } catch (err: any) {
    error.value = err.response?.data?.message || err.message || 'Gagal memuat ringkasan beranda'
  } finally {
    loading.value = false
  }
}

const navigateToAnggota = (status: string) => {
  router.push({ path: '/anggota', query: { status } })
}

onMounted(() => {
  fetchDashboard()
})
</script>

<template>
  <div class="home-view-minimal">
    <div class="hero-box p-4 p-md-5 mb-4 text-white">
      <div class="container-fluid p-0">
        <div class="d-inline-block px-2 py-1 mb-2 bg-danger text-white small fw-bold text-uppercase tag-koperasi">
          SIKopDes &bull; Desa Merah Biru
        </div>
        <h1 class="display-6 fw-bold mb-2">Selamat Datang di SIKopDes</h1>
        <p class="col-md-9 fs-6 text-light-gray mb-4">
          Sistem Informasi Pengelolaan Keanggotaan dan Transaksi Simpanan Koperasi Desa.
        </p>

        <!-- Quick Action Buttons: Minimalist Kotak (No extra icons) -->
        <div class="d-flex flex-wrap gap-2">
          <RouterLink to="/anggota/create" class="btn btn-danger btn-kotak fw-semibold">
            Tambah Anggota Baru
          </RouterLink>
          <RouterLink to="/simpanan/create" class="btn btn-outline-light btn-kotak">
            Catat Simpanan
          </RouterLink>
          <RouterLink to="/simpanan/kepatuhan" class="btn btn-outline-light btn-kotak">
            Cek Kepatuhan
          </RouterLink>
        </div>
      </div>
    </div>

    <!-- Alert Message -->
    <div v-if="error" class="alert alert-danger alert-dismissible fade show rounded-0 border-start border-danger border-4 mb-4" role="alert">
      <strong>Terjadi Kesalahan:</strong> {{ error }}
      <button type="button" class="btn-close" @click="error = null"></button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-danger" role="status">
        <span class="visually-hidden">Memuat data...</span>
      </div>
      <p class="mt-2 text-muted small">Memuat ringkasan beranda...</p>
    </div>

    <!-- Content -->
    <div v-else-if="stats">
      <div class="d-flex justify-content-between align-items-center mb-3 border-bottom pb-2">
        <h5 class="fw-bold mb-0 text-dark text-uppercase section-heading">
          Ringkasan Keanggotaan
        </h5>
        <small class="text-muted">Klik kartu untuk lihat anggota</small>
      </div>

      <div class="row g-3 mb-4">
        <!-- 1. Menunggu Verifikasi -->
        <div class="col-12 col-sm-6 col-lg-4 col-xl">
          <div
            class="status-card-square card h-100 border-0 border-top border-warning border-3"
            role="button"
            @click="navigateToAnggota('MENUNGGU_VERIFIKASI')"
          >
            <div class="card-body p-3">
              <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="badge-kotak badge-warning-soft">Verifikasi</span>
              </div>
              <h3 class="fw-bold mb-0 text-dark">{{ stats.waitingVerificationCount }}</h3>
              <div class="small fw-semibold text-secondary mt-1">Menunggu Verifikasi</div>
              <div class="text-muted extra-small mt-1">Calon anggota mendaftar</div>
            </div>
          </div>
        </div>

        <!-- 2. Menunggu Setoran Pokok -->
        <div class="col-12 col-sm-6 col-lg-4 col-xl">
          <div
            class="status-card-square card h-100 border-0 border-top border-info border-3"
            role="button"
            @click="navigateToAnggota('MENUNGGU_SETORAN_POKOK')"
          >
            <div class="card-body p-3">
              <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="badge-kotak badge-info-soft">Setoran Pokok</span>
              </div>
              <h3 class="fw-bold mb-0 text-dark">{{ stats.waitingPokokCount }}</h3>
              <div class="small fw-semibold text-secondary mt-1">Menunggu Setoran Pokok</div>
              <div class="text-muted extra-small mt-1">Belum bayar pokok</div>
            </div>
          </div>
        </div>

        <!-- 3. Aktif -->
        <div class="col-12 col-sm-6 col-lg-4 col-xl">
          <div
            class="status-card-square card h-100 border-0 border-top border-success border-3"
            role="button"
            @click="navigateToAnggota('AKTIF')"
          >
            <div class="card-body p-3">
              <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="badge-kotak badge-success-soft">Aktif</span>
              </div>
              <h3 class="fw-bold mb-0 text-dark">{{ stats.activeCount }}</h3>
              <div class="small fw-semibold text-secondary mt-1">Anggota Aktif</div>
              <div class="text-muted extra-small mt-1">Anggota sah &amp; aktif</div>
            </div>
          </div>
        </div>

        <!-- 4. Dibekukan -->
        <div class="col-12 col-sm-6 col-lg-4 col-xl">
          <div
            class="status-card-square card h-100 border-0 border-top border-secondary border-3"
            role="button"
            @click="navigateToAnggota('DIBEKUKAN')"
          >
            <div class="card-body p-3">
              <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="badge-kotak badge-secondary-soft">Dibekukan</span>
              </div>
              <h3 class="fw-bold mb-0 text-dark">{{ stats.frozenCount }}</h3>
              <div class="small fw-semibold text-secondary mt-1">Dibekukan</div>
              <div class="text-muted extra-small mt-1">Layanan dihentikan</div>
            </div>
          </div>
        </div>

        <!-- 5. Nonaktif -->
        <div class="col-12 col-sm-6 col-lg-4 col-xl">
          <div
            class="status-card-square card h-100 border-0 border-top border-danger border-3"
            role="button"
            @click="navigateToAnggota('NONAKTIF')"
          >
            <div class="card-body p-3">
              <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="badge-kotak badge-danger-soft">Nonaktif</span>
              </div>
              <h3 class="fw-bold mb-0 text-dark">{{ stats.inactiveCount }}</h3>
              <div class="small fw-semibold text-secondary mt-1">Nonaktif</div>
              <div class="text-muted extra-small mt-1">Menunggak simpanan</div>
            </div>
          </div>
        </div>
      </div>

      <div class="financial-box-square card border-0 border-start border-danger border-4 mb-4">
        <div class="card-body p-4">
          <div class="row align-items-center">
            <div class="col-md-7">
              <div class="text-danger small fw-bold text-uppercase tracking-wide mb-1">
                Total Setoran Simpanan Bulan Ini
              </div>
              <p class="text-muted small mb-0">
                Akumulasi seluruh setoran simpanan (pokok, wajib, dan sukarela) pada bulan kalender berjalan.
              </p>
            </div>
            <div class="col-md-5 text-md-end mt-3 mt-md-0">
              <div class="fs-2 fw-bold text-dark mb-2">
                {{ formatRupiah(stats.totalCurrentMonthSimpanan) }}
              </div>
              <RouterLink to="/simpanan" class="btn btn-sm btn-outline-danger btn-kotak fw-semibold">
                Lihat Seluruh Transaksi &rarr;
              </RouterLink>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-view-minimal,
.home-view-minimal * {
  border-radius: 0 !important;
}

.hero-box {
  background: linear-gradient(135deg, #1e232a 0%, #2b303a 50%, #8b1e28 100%);
  border: 1px solid #374151;
}

.text-light-gray {
  color: #d1d5db;
}

.tag-koperasi {
  font-size: 0.72rem;
  letter-spacing: 0.5px;
}

/* Button Kotak */
.btn-kotak {
  border-radius: 0 !important;
  padding: 0.5rem 1.15rem;
  transition: all 0.15s ease-in-out;
}

.btn-danger {
  background-color: #b91c1c;
  border-color: #b91c1c;
}

.btn-danger:hover {
  background-color: #991b1b;
  border-color: #991b1b;
}

.btn-outline-danger {
  color: #b91c1c;
  border-color: #b91c1c;
}

.btn-outline-danger:hover {
  background-color: #b91c1c;
  border-color: #b91c1c;
  color: #ffffff;
}

/* Section Heading */
.section-heading {
  font-size: 0.95rem;
  letter-spacing: 0.5px;
}

.tracking-wide {
  letter-spacing: 0.5px;
}

.status-card-square {
  background-color: #ffffff;
  border: 1px solid #e5e7eb !important;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: transform 0.15s ease, box-shadow 0.15s ease, border-color 0.15s ease;
  cursor: pointer;
}

.status-card-square:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.08);
  border-color: #d1d5db !important;
}

/* Badges Kotak */
.badge-kotak {
  display: inline-block;
  font-size: 0.7rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.4px;
  padding: 0.2rem 0.5rem;
  border-radius: 0 !important;
}

.badge-warning-soft {
  background-color: #fef3c7;
  color: #92400e;
}

.badge-info-soft {
  background-color: #e0f2fe;
  color: #075985;
}

.badge-success-soft {
  background-color: #dcfce7;
  color: #166534;
}

.badge-secondary-soft {
  background-color: #f1f5f9;
  color: #475569;
}

.badge-danger-soft {
  background-color: #fee2e2;
  color: #991b1b;
}

.extra-small {
  font-size: 0.75rem;
}

/* Financial Box Square */
.financial-box-square {
  background-color: #ffffff;
  border: 1px solid #e5e7eb !important;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}
</style>
