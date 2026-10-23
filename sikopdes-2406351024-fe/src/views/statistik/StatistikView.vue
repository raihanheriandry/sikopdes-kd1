<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { apiService, type StatistikData } from '@/services/api'
import { formatRupiah, getStatusBadge } from '@/utils/formatters'
import Chart from 'chart.js/auto'

const statistik = ref<StatistikData | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)

// Filters
const tahun = ref<number>(2026)
const periodeType = ref<string>('ALL')
const filterValue = ref<string>('ALL')

const statusCanvasRef = ref<HTMLCanvasElement | null>(null)
const trendCanvasRef = ref<HTMLCanvasElement | null>(null)

let statusChartInstance: Chart | null = null
let trendChartInstance: Chart | null = null

const fetchStatistik = async () => {
  loading.value = true
  error.value = null
  try {
    const res = await apiService.getStatistik(tahun.value, periodeType.value, filterValue.value)
    statistik.value = res.data.data
  } catch (err: any) {
    error.value = err.response?.data?.message || err.message || 'Gagal memuat data statistik'
  } finally {
    loading.value = false
  }

  if (statistik.value && !error.value) {
    await nextTick()
    renderCharts()
  }
}

const handlePeriodeTypeChange = () => {
  if (periodeType.value === 'ALL') {
    filterValue.value = 'ALL'
  } else if (periodeType.value === 'MONTHLY') {
    filterValue.value = '10' // default to current month
  } else if (periodeType.value === 'QUARTERLY') {
    filterValue.value = 'Q4' // default to current quarter
  }
  fetchStatistik()
}

const renderCharts = () => {
  if (!statistik.value) return

  // 1. Status Doughnut Chart
  const statusCanvas = statusCanvasRef.value || (document.getElementById('statusChart') as HTMLCanvasElement | null)
  if (statusCanvas) {
    if (statusChartInstance) {
      statusChartInstance.destroy()
      statusChartInstance = null
    }

    const statusColorMap: Record<string, string> = {
      MENUNGGU_VERIFIKASI: '#ffc107',
      MENUNGGU_SETORAN_POKOK: '#0dcaf0',
      AKTIF: '#198754',
      DIBEKUKAN: '#6c757d',
      NONAKTIF: '#dc3545',
      DITOLAK: '#212529',
      KELUAR: '#adb5bd',
    }

    const statusLabels: string[] = []
    const statusData: number[] = []
    const statusColors: string[] = []

    if (statistik.value.anggotaByStatus) {
      for (const [key, val] of Object.entries(statistik.value.anggotaByStatus)) {
        statusLabels.push(getStatusBadge(key).label)
        statusData.push(val)
        statusColors.push(statusColorMap[key] || '#6c757d')
      }
    }

    statusChartInstance = new Chart(statusCanvas, {
      type: 'doughnut',
      data: {
        labels: statusLabels,
        datasets: [
          {
            data: statusData,
            backgroundColor: statusColors,
            borderWidth: 2,
            hoverOffset: 4,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'right',
          },
        },
      },
    })
  }

  // 2. Deposit Trend Line/Bar Chart
  const trendCanvas = trendCanvasRef.value || (document.getElementById('trendChart') as HTMLCanvasElement | null)
  if (trendCanvas) {
    if (trendChartInstance) {
      trendChartInstance.destroy()
      trendChartInstance = null
    }

    const labels = (statistik.value.trenSimpanan || []).map((t) => t.label)
    const pokokData = (statistik.value.trenSimpanan || []).map((t) => t.totalPokok)
    const wajibData = (statistik.value.trenSimpanan || []).map((t) => t.totalWajib)
    const sukarelaData = (statistik.value.trenSimpanan || []).map((t) => t.totalSukarela)

    trendChartInstance = new Chart(trendCanvas, {
      type: 'bar',
      data: {
        labels,
        datasets: [
          {
            label: 'Simpanan Pokok',
            data: pokokData,
            backgroundColor: '#0d6efd',
          },
          {
            label: 'Simpanan Wajib',
            data: wajibData,
            backgroundColor: '#198754',
          },
          {
            label: 'Simpanan Sukarela',
            data: sukarelaData,
            backgroundColor: '#0dcaf0',
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          x: {
            stacked: true,
          },
          y: {
            stacked: true,
            ticks: {
              callback: function (value) {
                return 'Rp ' + Number(value).toLocaleString('id-ID')
              },
            },
          },
        },
        plugins: {
          tooltip: {
            callbacks: {
              label: function (context) {
                return `${context.dataset.label}: ${formatRupiah(context.parsed.y)}`
              },
            },
          },
        },
      },
    })
  }
}

onMounted(() => {
  fetchStatistik()
})

onUnmounted(() => {
  if (statusChartInstance) {
    statusChartInstance.destroy()
    statusChartInstance = null
  }
  if (trendChartInstance) {
    trendChartInstance.destroy()
    trendChartInstance = null
  }
})
</script>

<template>
  <div class="statistik-view">
    <!-- Header -->
    <div class="mb-4">
      <h3 class="fw-bold mb-1">
        <i class="bi bi-graph-up-arrow text-primary me-2"></i>Statistika Keanggotaan &amp; Simpanan
      </h3>
      <p class="text-muted mb-0">Visualisasi data pertumbuhan anggota dan tren mutasi keuangan simpanan</p>
    </div>

    <!-- Filter Card -->
    <div class="card border-0 shadow-sm mb-4">
      <div class="card-body p-3">
        <div class="row g-2 align-items-end">
          <!-- Filter Tahun -->
          <div class="col-6 col-md-3">
            <label class="form-label text-muted small mb-1 fw-semibold">Pilih Tahun</label>
            <select class="form-select" v-model.number="tahun" @change="fetchStatistik">
              <option :value="2024">2024</option>
              <option :value="2025">2025</option>
              <option :value="2026">2026</option>
              <option :value="2027">2027</option>
            </select>
          </div>

          <!-- Filter Tipe Periode -->
          <div class="col-6 col-md-3">
            <label class="form-label text-muted small mb-1 fw-semibold">Jenis Filter Periode</label>
            <select class="form-select" v-model="periodeType" @change="handlePeriodeTypeChange">
              <option value="ALL">Satu Tahun Penuh</option>
              <option value="MONTHLY">Bulanan</option>
              <option value="QUARTERLY">Kuartalan</option>
            </select>
          </div>

          <!-- Filter Value (Bulan atau Kuartal) -->
          <div class="col-12 col-md-4" v-if="periodeType !== 'ALL'">
            <label class="form-label text-muted small mb-1 fw-semibold">
              {{ periodeType === 'MONTHLY' ? 'Pilih Bulan' : 'Pilih Kuartal' }}
            </label>
            <select
              v-if="periodeType === 'MONTHLY'"
              class="form-select"
              v-model="filterValue"
              @change="fetchStatistik"
            >
              <option value="1">Januari</option>
              <option value="2">Februari</option>
              <option value="3">Maret</option>
              <option value="4">April</option>
              <option value="5">Mei</option>
              <option value="6">Juni</option>
              <option value="7">Juli</option>
              <option value="8">Agustus</option>
              <option value="9">September</option>
              <option value="10">Oktober</option>
              <option value="11">November</option>
              <option value="12">Desember</option>
            </select>
            <select
              v-else-if="periodeType === 'QUARTERLY'"
              class="form-select"
              v-model="filterValue"
              @change="fetchStatistik"
            >
              <option value="Q1">Kuartal 1 (Jan - Mar)</option>
              <option value="Q2">Kuartal 2 (Apr - Jun)</option>
              <option value="Q3">Kuartal 3 (Jul - Sep)</option>
              <option value="Q4">Kuartal 4 (Okt - Des)</option>
            </select>
          </div>

          <div class="col-12 col-md-2 text-md-end">
            <button class="btn btn-primary w-100" @click="fetchStatistik">
              <i class="bi bi-funnel me-1"></i> Terapkan
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Error Alert -->
    <div v-if="error" class="alert alert-danger" role="alert">
      <i class="bi bi-exclamation-triangle-fill me-2"></i> {{ error }}
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Memuat data...</span>
      </div>
      <p class="mt-2 text-muted">Memuat data analitik statistik...</p>
    </div>

    <div v-else-if="statistik">
      <!-- Summary Metric Cards -->
      <div class="row g-3 mb-4">
        <!-- Anggota Baru -->
        <div class="col-sm-6 col-lg-3">
          <div class="card border-0 shadow-sm border-start border-primary border-4 h-100">
            <div class="card-body">
              <div class="d-flex justify-content-between align-items-center mb-1">
                <span class="text-muted small text-uppercase fw-bold">Anggota Baru</span>
                <i class="bi bi-person-check-fill text-primary fs-4"></i>
              </div>
              <h2 class="fw-bold mb-0 text-dark">{{ statistik.anggotaBaruCount }}</h2>
              <small class="text-muted">Terdaftar pada periode terpilih</small>
            </div>
          </div>
        </div>

        <!-- Total Simpanan Pokok -->
        <div class="col-sm-6 col-lg-3">
          <div class="card border-0 shadow-sm border-start border-primary border-4 h-100">
            <div class="card-body">
              <div class="d-flex justify-content-between align-items-center mb-1">
                <span class="text-muted small text-uppercase fw-bold">Simpanan Pokok</span>
                <i class="bi bi-cash-stack text-primary fs-4"></i>
              </div>
              <h4 class="fw-bold mb-0 text-primary">{{ formatRupiah(statistik.totalSimpananByJenis.POKOK) }}</h4>
              <small class="text-muted">Setoran periode terpilih</small>
            </div>
          </div>
        </div>

        <!-- Total Simpanan Wajib -->
        <div class="col-sm-6 col-lg-3">
          <div class="card border-0 shadow-sm border-start border-success border-4 h-100">
            <div class="card-body">
              <div class="d-flex justify-content-between align-items-center mb-1">
                <span class="text-muted small text-uppercase fw-bold">Simpanan Wajib</span>
                <i class="bi bi-calendar2-check-fill text-success fs-4"></i>
              </div>
              <h4 class="fw-bold mb-0 text-success">{{ formatRupiah(statistik.totalSimpananByJenis.WAJIB) }}</h4>
              <small class="text-muted">Setoran periode terpilih</small>
            </div>
          </div>
        </div>

        <!-- Total Seluruh Simpanan -->
        <div class="col-sm-6 col-lg-3">
          <div class="card border-0 shadow-sm border-start border-dark border-4 h-100 bg-dark text-white">
            <div class="card-body">
              <div class="d-flex justify-content-between align-items-center mb-1">
                <span class="text-white-50 small text-uppercase fw-bold">Total Seluruh Setoran</span>
                <i class="bi bi-bank text-warning fs-4"></i>
              </div>
              <h4 class="fw-bold mb-0 text-warning">{{ formatRupiah(statistik.totalSimpananByJenis.TOTAL) }}</h4>
              <small class="text-white-50">Pokok + Wajib + Sukarela</small>
            </div>
          </div>
        </div>
      </div>

      <!-- Charts Row -->
      <div class="row g-4 mb-4">
        <!-- Status Snapshot Doughnut Chart -->
        <div class="col-12 col-lg-5">
          <div class="card border-0 shadow-sm h-100">
            <div class="card-header bg-white py-3 border-bottom">
              <h5 class="fw-bold mb-0 text-dark">
                <i class="bi bi-pie-chart-fill text-primary me-2"></i>Kondisi Status Anggota Saat Ini
              </h5>
            </div>
            <div class="card-body p-3">
              <div style="position: relative; height: 320px;">
                <canvas ref="statusCanvasRef" id="statusChart"></canvas>
              </div>
            </div>
          </div>
        </div>

        <!-- Trend Simpanan Bar/Line Chart -->
        <div class="col-12 col-lg-7">
          <div class="card border-0 shadow-sm h-100">
            <div class="card-header bg-white py-3 border-bottom">
              <h5 class="fw-bold mb-0 text-dark">
                <i class="bi bi-bar-chart-fill text-success me-2"></i>Tren Simpanan Bulanan Tahun {{ statistik.tahun }}
              </h5>
              <small class="text-muted">Akumulasi setoran simpanan per bulan</small>
            </div>
            <div class="card-body p-3">
              <div style="position: relative; height: 320px;">
                <canvas ref="trendCanvasRef" id="trendChart"></canvas>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
