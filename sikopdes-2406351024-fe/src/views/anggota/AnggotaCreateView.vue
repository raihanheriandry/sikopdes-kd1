<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { apiService } from '@/services/api'

const router = useRouter()

const form = ref({
  nama: '',
  nik: '',
  email: '',
  telepon: '',
  alamat: '',
})

const loading = ref(false)
const error = ref<string | null>(null)
const validationErrors = ref<Record<string, string>>({})

const validate = (): boolean => {
  validationErrors.value = {}
  let valid = true

  if (!form.value.nama.trim()) {
    validationErrors.value.nama = 'Nama lengkap wajib diisi'
    valid = false
  }

  if (!form.value.nik.trim()) {
    validationErrors.value.nik = 'NIK wajib diisi'
    valid = false
  } else if (!/^\d{16}$/.test(form.value.nik.trim())) {
    validationErrors.value.nik = 'NIK harus terdiri dari tepat 16 digit angka'
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

  loading.value = true
  error.value = null

  try {
    const res = await apiService.createAnggota(form.value)
    const newAnggota = res.data.data
    router.push({
      path: `/anggota/${newAnggota.id}`,
    })
  } catch (err: any) {
    error.value = err.response?.data?.message || err.message || 'Gagal mendaftarkan anggota baru'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="anggota-create-view">
    <div class="row justify-content-center">
      <div class="col-12 col-md-8 col-lg-7">
        <!-- Back Link -->
        <RouterLink to="/anggota" class="btn btn-sm btn-outline-secondary mb-3">
          <i class="bi bi-arrow-left me-1"></i> Kembali ke Daftar Anggota
        </RouterLink>

        <!-- Form Card -->
        <div class="card border-0 shadow-sm">
          <div class="card-header bg-white py-3 border-bottom">
            <h4 class="fw-bold mb-0 text-primary">
              <i class="bi bi-person-plus-fill me-2"></i>Pendaftaran Calon Anggota Baru
            </h4>
            <small class="text-muted">Isi seluruh data identitas warga calon anggota koperasi</small>
          </div>

          <div class="card-body p-4">
            <!-- Error Alert -->
            <div v-if="error" class="alert alert-danger alert-dismissible fade show" role="alert">
              <i class="bi bi-exclamation-triangle-fill me-2"></i> {{ error }}
              <button type="button" class="btn-close" @click="error = null"></button>
            </div>

            <form @submit.prevent="handleSubmit">
              <!-- Nama -->
              <div class="mb-3">
                <label class="form-label fw-semibold">Nama Lengkap <span class="text-danger">*</span></label>
                <input
                  type="text"
                  class="form-control"
                  :class="{ 'is-invalid': validationErrors.nama }"
                  placeholder="Contoh: Budi Santoso"
                  v-model="form.nama"
                />
                <div class="invalid-feedback">{{ validationErrors.nama }}</div>
              </div>

              <!-- NIK -->
              <div class="mb-3">
                <label class="form-label fw-semibold">NIK (16 Digit) <span class="text-danger">*</span></label>
                <input
                  type="text"
                  maxlength="16"
                  class="form-control font-monospace"
                  :class="{ 'is-invalid': validationErrors.nik }"
                  placeholder="Contoh: 3171012345670001"
                  v-model="form.nik"
                />
                <div class="invalid-feedback">{{ validationErrors.nik }}</div>
                <div class="form-text">NIK harus tepat 16 digit dan unik di antara anggota yang tidak Ditolak.</div>
              </div>

              <!-- Email -->
              <div class="mb-3">
                <label class="form-label fw-semibold">Alamat Email <span class="text-danger">*</span></label>
                <input
                  type="email"
                  class="form-control"
                  :class="{ 'is-invalid': validationErrors.email }"
                  placeholder="Contoh: budi.santoso@example.com"
                  v-model="form.email"
                />
                <div class="invalid-feedback">{{ validationErrors.email }}</div>
                <div class="form-text">Email harus unik di antara anggota yang tidak Ditolak.</div>
              </div>

              <!-- Telepon -->
              <div class="mb-3">
                <label class="form-label fw-semibold">Nomor Telepon <span class="text-danger">*</span></label>
                <input
                  type="text"
                  maxlength="15"
                  class="form-control font-monospace"
                  :class="{ 'is-invalid': validationErrors.telepon }"
                  placeholder="Contoh: 081234567890"
                  v-model="form.telepon"
                />
                <div class="invalid-feedback">{{ validationErrors.telepon }}</div>
                <div class="form-text">Nomor telepon terdiri dari 10 sampai 15 digit angka.</div>
              </div>

              <!-- Alamat -->
              <div class="mb-4">
                <label class="form-label fw-semibold">Alamat Lengkap <span class="text-danger">*</span></label>
                <textarea
                  class="form-control"
                  rows="3"
                  :class="{ 'is-invalid': validationErrors.alamat }"
                  placeholder="Contoh: Jl. Desa Merah Biru No. 12, RT 01 / RW 02"
                  v-model="form.alamat"
                ></textarea>
                <div class="invalid-feedback">{{ validationErrors.alamat }}</div>
              </div>

              <!-- Notice Box -->
              <div class="alert alert-info py-2 small mb-4">
                <i class="bi bi-info-circle-fill me-1"></i>
                Setelah disimpan, status awal anggota adalah <strong>Menunggu Verifikasi</strong>. ID dan Nomor Anggota akan dibangkitkan otomatis oleh sistem.
              </div>

              <!-- Actions -->
              <div class="d-flex justify-content-end gap-2">
                <RouterLink to="/anggota" class="btn btn-secondary" :disabled="loading">Batal</RouterLink>
                <button type="submit" class="btn btn-primary fw-semibold px-4" :disabled="loading">
                  <span v-if="loading" class="spinner-border spinner-border-sm me-1"></span>
                  Simpan &amp; Daftarkan
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
