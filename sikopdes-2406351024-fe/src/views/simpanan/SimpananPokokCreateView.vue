<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { apiService, type Anggota } from '@/services/api'

const route = useRoute()
const router = useRouter()

const initialAnggotaId = (route.query.anggotaId as string) || ''

const form = ref({
  anggotaId: initialAnggotaId,
  nominal: 100000,
  tanggalSetor: new Date().toISOString().substring(0, 10),
})

const eligibleAnggota = ref<Anggota[]>([])
const loadingAnggota = ref(true)
const submitting = ref(false)
const error = ref<string | null>(null)
const validationErrors = ref<Record<string, string>>({})

const fetchEligibleAnggota = async () => {
  loadingAnggota.value = true
  try {
    const res = await apiService.getAllAnggota(undefined, 'MENUNGGU_SETORAN_POKOK')
    eligibleAnggota.value = res.data.data

    // If initialAnggotaId is provided but not in list (e.g. invalid), keep or reset
    if (initialAnggotaId && !eligibleAnggota.value.some((a) => a.id === initialAnggotaId)) {
      // Also try fetching the specific member to verify
      try {
        const single = await apiService.getAnggotaById(initialAnggotaId)
        if (single.data.data.statusKeanggotaan === 'MENUNGGU_SETORAN_POKOK') {
          eligibleAnggota.value.push(single.data.data)
        }
      } catch {
        // Ignored
      }
    }
  } catch {
    error.value = 'Gagal memuat daftar anggota yang menunggu setoran pokok'
  } finally {
    loadingAnggota.value = false
  }
}

const validate = (): boolean => {
  validationErrors.value = {}
  let valid = true

  if (!form.value.anggotaId) {
    validationErrors.value.anggotaId = 'Pilih anggota yang akan membayar simpanan pokok'
    valid = false
  }

  if (!form.value.nominal || form.value.nominal < 100000) {
    validationErrors.value.nominal = 'Nominal simpanan pokok minimal Rp100.000'
    valid = false
  }

  const todayStr = new Date().toISOString().substring(0, 10)
  if (!form.value.tanggalSetor) {
    validationErrors.value.tanggalSetor = 'Tanggal setor wajib diisi'
    valid = false
  } else if (form.value.tanggalSetor > todayStr) {
    validationErrors.value.tanggalSetor = 'Tanggal setor tidak boleh berada di masa depan'
    valid = false
  }

  return valid
}

const handleSubmit = async () => {
  if (!validate()) return

  submitting.value = true
  error.value = null

  try {
    const res = await apiService.createSimpananPokok({
      anggotaId: form.value.anggotaId,
      nominal: Number(form.value.nominal),
      tanggalSetor: form.value.tanggalSetor,
    })
    const saved = res.data.data
    router.push(`/anggota/${saved.anggotaId}`)
  } catch (err: any) {
    error.value = err.response?.data?.message || err.message || 'Gagal mencatat simpanan pokok'
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchEligibleAnggota()
})
</script>

<template>
  <div class="simpanan-pokok-create-view">
    <div class="row justify-content-center">
      <div class="col-12 col-md-8 col-lg-7">
        <!-- Back Link -->
        <RouterLink to="/simpanan" class="btn btn-sm btn-outline-secondary mb-3">
          <i class="bi bi-arrow-left me-1"></i> Kembali ke Daftar Simpanan
        </RouterLink>

        <!-- Form Card -->
        <div class="card border-0 shadow-sm">
          <div class="card-header bg-white py-3 border-bottom">
            <h4 class="fw-bold mb-0 text-primary">
              <i class="bi bi-cash-stack me-2"></i>Pencatatan Simpanan Pokok
            </h4>
            <small class="text-muted">Syarat aktivasi pertama kali calon anggota koperasi</small>
          </div>

          <div class="card-body p-4">
            <!-- Error Alert -->
            <div v-if="error" class="alert alert-danger alert-dismissible fade show" role="alert">
              <i class="bi bi-exclamation-triangle-fill me-2"></i> {{ error }}
              <button type="button" class="btn-close" @click="error = null"></button>
            </div>

            <!-- Loading Anggota -->
            <div v-if="loadingAnggota" class="text-center py-4">
              <div class="spinner-border spinner-border-sm text-primary" role="status"></div>
              <span class="ms-2 text-muted small">Memuat daftar anggota...</span>
            </div>

            <!-- No Eligible Anggota Warning -->
            <div v-else-if="eligibleAnggota.length === 0" class="alert alert-warning">
              <i class="bi bi-exclamation-circle-fill me-2"></i>
              Tidak ada anggota yang saat ini berstatus <strong>Menunggu Setoran Pokok</strong>. Pastikan calon anggota telah diverifikasi terlebih dahulu sebelum membayar simpanan pokok.
            </div>

            <form v-else @submit.prevent="handleSubmit">
              <!-- Pilih Anggota -->
              <div class="mb-3">
                <label class="form-label fw-semibold">Pilih Anggota <span class="text-danger">*</span></label>
                <select
                  class="form-select"
                  :class="{ 'is-invalid': validationErrors.anggotaId }"
                  v-model="form.anggotaId"
                >
                  <option value="" disabled>-- Pilih Calon Anggota (Menunggu Setoran Pokok) --</option>
                  <option v-for="a in eligibleAnggota" :key="a.id" :value="a.id">
                    {{ a.nomorAnggota }} - {{ a.nama }} (NIK: {{ a.nik }})
                  </option>
                </select>
                <div class="invalid-feedback">{{ validationErrors.anggotaId }}</div>
              </div>

              <!-- Nominal -->
              <div class="mb-3">
                <label class="form-label fw-semibold">Nominal Simpanan Pokok (Rp) <span class="text-danger">*</span></label>
                <div class="input-group">
                  <span class="input-group-text">Rp</span>
                  <input
                    type="number"
                    min="100000"
                    step="10000"
                    class="form-control fw-bold text-success fs-5"
                    :class="{ 'is-invalid': validationErrors.nominal }"
                    v-model.number="form.nominal"
                  />
                  <div class="invalid-feedback">{{ validationErrors.nominal }}</div>
                </div>
                <div class="form-text">Minimal nominal setoran pokok adalah <strong>Rp 100.000</strong>.</div>
              </div>

              <!-- Tanggal Setor -->
              <div class="mb-4">
                <label class="form-label fw-semibold">Tanggal Setor <span class="text-danger">*</span></label>
                <input
                  type="date"
                  class="form-control"
                  :class="{ 'is-invalid': validationErrors.tanggalSetor }"
                  v-model="form.tanggalSetor"
                />
                <div class="invalid-feedback">{{ validationErrors.tanggalSetor }}</div>
                <div class="form-text">Tanggal setor tidak boleh berada di masa depan.</div>
              </div>

              <!-- Notice Box -->
              <div class="alert alert-info py-2 small mb-4">
                <i class="bi bi-info-circle-fill me-1"></i>
                <strong>Dampak Tindakan:</strong> Setelah transaksi ini disimpan, status anggota akan otomatis berubah menjadi <strong>Aktif</strong>, tanggal aktivasi dicatat, dan riwayat status keanggotaan diperbarui dalam satu transaksi atomik.
              </div>

              <!-- Actions -->
              <div class="d-flex justify-content-end gap-2">
                <RouterLink to="/simpanan" class="btn btn-secondary" :disabled="submitting">Batal</RouterLink>
                <button type="submit" class="btn btn-primary fw-semibold px-4" :disabled="submitting">
                  <span v-if="submitting" class="spinner-border spinner-border-sm me-1"></span>
                  Catat &amp; Aktifkan Anggota
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
