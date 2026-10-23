<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { apiService, type DetailAnggotaResponse } from '@/services/api'

const route = useRoute()
const router = useRouter()
const anggotaId = route.params.id as string

const anggota = ref<DetailAnggotaResponse | null>(null)
const loading = ref(true)
const saving = ref(false)
const error = ref<string | null>(null)
const validationErrors = ref<Record<string, string>>({})

const form = ref({
  nama: '',
  email: '',
  telepon: '',
  alamat: '',
})

const fetchAnggota = async () => {
  loading.value = true
  error.value = null
  try {
    const res = await apiService.getAnggotaById(anggotaId)
    anggota.value = res.data.data

    if (anggota.value.statusKeanggotaan === 'DITOLAK' || anggota.value.statusKeanggotaan === 'KELUAR') {
      error.value = 'Data anggota berstatus Ditolak atau Keluar tidak dapat diubah!'
      return
    }

    form.value.nama = anggota.value.nama
    form.value.email = anggota.value.email
    form.value.telepon = anggota.value.telepon
    form.value.alamat = anggota.value.alamat
  } catch (err: any) {
    error.value = err.response?.data?.message || err.message || 'Gagal memuat data anggota'
  } finally {
    loading.value = false
  }
}

const validate = (): boolean => {
  validationErrors.value = {}
  let valid = true

  if (!form.value.nama.trim()) {
    validationErrors.value.nama = 'Nama lengkap wajib diisi'
    valid = false
  }

  if (!form.value.email.trim()) {
    validationErrors.value.email = 'Email wajib diisi'
    valid = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email.trim())) {
    validationErrors.value.email = 'Format email tidak valid'
    valid = false
  }

  if (!form.value.telepon.trim()) {
    validationErrors.value.telepon = 'Nomor telepon wajib diisi'
    valid = false
  } else if (!/^\d{10,15}$/.test(form.value.telepon.trim())) {
    validationErrors.value.telepon = 'Nomor telepon harus terdiri dari 10 sampai 15 digit angka'
    valid = false
  }

  if (!form.value.alamat.trim()) {
    validationErrors.value.alamat = 'Alamat wajib diisi'
    valid = false
  }

  return valid
}

const handleSubmit = async () => {
  if (!validate()) return

  saving.value = true
  error.value = null

  try {
    await apiService.updateAnggota(anggotaId, form.value)
    router.push(`/anggota/${anggotaId}`)
  } catch (err: any) {
    error.value = err.response?.data?.message || err.message || 'Gagal memperbarui data anggota'
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchAnggota()
})
</script>

<template>
  <div class="anggota-update-view">
    <div class="row justify-content-center">
      <div class="col-12 col-md-8 col-lg-7">
        <!-- Back Link -->
        <RouterLink :to="`/anggota/${anggotaId}`" class="btn btn-sm btn-outline-secondary mb-3">
          <i class="bi bi-arrow-left me-1"></i> Kembali ke Detail Anggota
        </RouterLink>

        <!-- Loading -->
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Memuat data...</span>
          </div>
          <p class="mt-2 text-muted">Memuat data anggota...</p>
        </div>

        <!-- Form Card -->
        <div v-else-if="anggota" class="card border-0 shadow-sm">
          <div class="card-header bg-white py-3 border-bottom">
            <h4 class="fw-bold mb-0 text-primary">
              <i class="bi bi-pencil-square me-2"></i>Ubah Data Anggota
            </h4>
            <small class="text-muted">Perbarui data kontak dan alamat anggota {{ anggota.nama }}</small>
          </div>

          <div class="card-body p-4">
            <!-- Error Alert -->
            <div v-if="error" class="alert alert-danger" role="alert">
              <i class="bi bi-exclamation-triangle-fill me-2"></i> {{ error }}
            </div>

            <form v-if="anggota.statusKeanggotaan !== 'DITOLAK' && anggota.statusKeanggotaan !== 'KELUAR'" @submit.prevent="handleSubmit">
              <!-- Read-only Identifiers -->
              <div class="row g-3 mb-3">
                <div class="col-md-6">
                  <label class="form-label text-muted small">ID Anggota (Tetap)</label>
                  <input type="text" class="form-control font-monospace bg-light" :value="anggota.id" disabled />
                </div>
                <div class="col-md-6">
                  <label class="form-label text-muted small">Nomor Anggota (Tetap)</label>
                  <input type="text" class="form-control font-monospace bg-light" :value="anggota.nomorAnggota" disabled />
                </div>
                <div class="col-12">
                  <label class="form-label text-muted small">NIK (Tetap / Tidak Dapat Diubah)</label>
                  <input type="text" class="form-control font-monospace bg-light" :value="anggota.nik" disabled />
                </div>
              </div>

              <hr />

              <!-- Nama -->
              <div class="mb-3">
                <label class="form-label fw-semibold">Nama Lengkap <span class="text-danger">*</span></label>
                <input
                  type="text"
                  class="form-control"
                  :class="{ 'is-invalid': validationErrors.nama }"
                  v-model="form.nama"
                />
                <div class="invalid-feedback">{{ validationErrors.nama }}</div>
              </div>

              <!-- Email -->
              <div class="mb-3">
                <label class="form-label fw-semibold">Alamat Email <span class="text-danger">*</span></label>
                <input
                  type="email"
                  class="form-control"
                  :class="{ 'is-invalid': validationErrors.email }"
                  v-model="form.email"
                />
                <div class="invalid-feedback">{{ validationErrors.email }}</div>
              </div>

              <!-- Telepon -->
              <div class="mb-3">
                <label class="form-label fw-semibold">Nomor Telepon <span class="text-danger">*</span></label>
                <input
                  type="text"
                  maxlength="15"
                  class="form-control font-monospace"
                  :class="{ 'is-invalid': validationErrors.telepon }"
                  v-model="form.telepon"
                />
                <div class="invalid-feedback">{{ validationErrors.telepon }}</div>
              </div>

              <!-- Alamat -->
              <div class="mb-4">
                <label class="form-label fw-semibold">Alamat Lengkap <span class="text-danger">*</span></label>
                <textarea
                  class="form-control"
                  rows="3"
                  :class="{ 'is-invalid': validationErrors.alamat }"
                  v-model="form.alamat"
                ></textarea>
                <div class="invalid-feedback">{{ validationErrors.alamat }}</div>
              </div>

              <!-- Actions -->
              <div class="d-flex justify-content-end gap-2">
                <RouterLink :to="`/anggota/${anggotaId}`" class="btn btn-secondary" :disabled="saving">Batal</RouterLink>
                <button type="submit" class="btn btn-primary fw-semibold px-4" :disabled="saving">
                  <span v-if="saving" class="spinner-border spinner-border-sm me-1"></span>
                  Simpan Perubahan
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
