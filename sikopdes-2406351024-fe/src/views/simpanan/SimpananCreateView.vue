<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { apiService, type Anggota } from '@/services/api'

const route = useRoute()
const router = useRouter()

const initialAnggotaId = (route.query.anggotaId as string) || ''
const initialJenis = (route.query.jenis as string)?.toUpperCase() === 'WAJIB' ? 'WAJIB' : 'WAJIB'

const form = ref<{
  anggotaId: string
  jenisSimpanan: 'WAJIB' | 'SUKARELA'
  nominal: number
  tanggalSetor: string
  periode: string
}>({
  anggotaId: initialAnggotaId,
  jenisSimpanan: initialJenis as 'WAJIB' | 'SUKARELA',
  nominal: 20000,
  tanggalSetor: new Date().toISOString().substring(0, 10),
  periode: new Date().toISOString().substring(0, 7), // e.g. "2026-10"
})

const anggotaList = ref<Anggota[]>([])
const loadingAnggota = ref(true)
const submitting = ref(false)
const error = ref<string | null>(null)
const validationErrors = ref<Record<string, string>>({})

const selectedAnggota = computed(() => {
  return anggotaList.value.find((a) => a.id === form.value.anggotaId) || null
})

const fetchAnggota = async () => {
  loadingAnggota.value = true
  try {
    // Both Aktif and Nonaktif can pay simpanan wajib
    const resAktif = await apiService.getAllAnggota(undefined, 'AKTIF')
    const resNonaktif = await apiService.getAllAnggota(undefined, 'NONAKTIF')
    const combined = [...resAktif.data.data, ...resNonaktif.data.data]
    combined.sort((a, b) => a.nama.localeCompare(b.nama))
    anggotaList.value = combined
  } catch {
    error.value = 'Gagal memuat daftar anggota'
  } finally {
    loadingAnggota.value = false
  }
}

// Watch selected member: if Nonaktif, force jenis to WAJIB
watch(
  () => form.value.anggotaId,
  () => {
    if (selectedAnggota.value?.statusKeanggotaan === 'NONAKTIF') {
      form.value.jenisSimpanan = 'WAJIB'
    }
  }
)

// Watch jenis change: adjust default nominal and clear period if sukarela
watch(
  () => form.value.jenisSimpanan,
  (newJenis) => {
    if (newJenis === 'WAJIB') {
      if (form.value.nominal < 20000) form.value.nominal = 20000
      if (!form.value.periode) {
        form.value.periode = new Date().toISOString().substring(0, 7)
      }
    } else {
      form.value.periode = ''
      if (form.value.nominal <= 0) form.value.nominal = 50000
    }
  }
)

const validate = (): boolean => {
  validationErrors.value = {}
  let valid = true

  if (!form.value.anggotaId) {
    validationErrors.value.anggotaId = 'Pilih anggota penyetor'
    valid = false
  }

  if (selectedAnggota.value?.statusKeanggotaan === 'NONAKTIF' && form.value.jenisSimpanan !== 'WAJIB') {
    validationErrors.value.jenisSimpanan = 'Anggota Nonaktif hanya diperbolehkan menyetor simpanan Wajib'
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

  if (form.value.jenisSimpanan === 'WAJIB') {
    if (!form.value.nominal || form.value.nominal < 20000) {
      validationErrors.value.nominal = 'Nominal simpanan wajib minimal Rp20.000'
      valid = false
    }

    if (!form.value.periode || !/^\d{4}-(0[1-9]|1[0-2])$/.test(form.value.periode.trim())) {
      validationErrors.value.periode = 'Periode harus dalam format YYYY-MM (misal: 2026-09)'
      valid = false
    } else {
      const currentYmStr = new Date().toISOString().substring(0, 7)
      if (form.value.periode > currentYmStr) {
        validationErrors.value.periode = `Periode tidak boleh melebihi bulan berjalan (${currentYmStr})`
        valid = false
      }
      if (selectedAnggota.value?.tanggalAktivasi) {
        const aktYmStr = selectedAnggota.value.tanggalAktivasi.substring(0, 7)
        if (form.value.periode < aktYmStr) {
          validationErrors.value.periode = `Periode tidak boleh lebih awal dari bulan tanggal aktivasi (${aktYmStr})`
          valid = false
        }
      }
    }
  } else {
    // SUKARELA
    if (!form.value.nominal || form.value.nominal <= 0) {
      validationErrors.value.nominal = 'Nominal simpanan sukarela harus lebih dari Rp0'
      valid = false
    }
  }

  return valid
}

const handleSubmit = async () => {
  if (!validate()) return

  submitting.value = true
  error.value = null

  try {
    const payload = {
      anggotaId: form.value.anggotaId,
      jenisSimpanan: form.value.jenisSimpanan,
      nominal: Number(form.value.nominal),
      tanggalSetor: form.value.tanggalSetor,
      periode: form.value.jenisSimpanan === 'WAJIB' ? form.value.periode : null,
    }
    const res = await apiService.createSimpanan(payload)
    const saved = res.data.data
    router.push(`/simpanan/${saved.id}`)
  } catch (err: any) {
    error.value = err.response?.data?.message || err.message || 'Gagal mencatat transaksi simpanan'
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchAnggota()
})
</script>

<template>
  <div class="simpanan-create-view">
    <div class="row justify-content-center">
      <div class="col-12 col-md-8 col-lg-7">
        <!-- Back Link -->
        <RouterLink to="/simpanan" class="btn btn-sm btn-outline-secondary mb-3">
          <i class="bi bi-arrow-left me-1"></i> Kembali ke Daftar Simpanan
        </RouterLink>

        <!-- Form Card -->
        <div class="card border-0 shadow-sm">
          <div class="card-header bg-white py-3 border-bottom">
            <h4 class="fw-bold mb-0 text-success">
              <i class="bi bi-plus-circle me-2"></i>Pencatatan Simpanan Wajib &amp; Sukarela
            </h4>
            <small class="text-muted">Catat setoran iuran bulanan wajib atau tabungan sukarela anggota</small>
          </div>

          <div class="card-body p-4">
            <!-- Error Alert -->
            <div v-if="error" class="alert alert-danger alert-dismissible fade show" role="alert">
              <i class="bi bi-exclamation-triangle-fill me-2"></i> {{ error }}
              <button type="button" class="btn-close" @click="error = null"></button>
            </div>

            <form @submit.prevent="handleSubmit">
              <!-- Pilih Anggota -->
              <div class="mb-3">
                <label class="form-label fw-semibold">Pilih Anggota <span class="text-danger">*</span></label>
                <select
                  class="form-select"
                  :class="{ 'is-invalid': validationErrors.anggotaId }"
                  v-model="form.anggotaId"
                  :disabled="loadingAnggota"
                >
                  <option value="" disabled>-- Pilih Anggota (Aktif atau Nonaktif) --</option>
                  <option v-for="a in anggotaList" :key="a.id" :value="a.id">
                    {{ a.nomorAnggota }} - {{ a.nama }} (Status: {{ a.statusKeanggotaan }})
                  </option>
                </select>
                <div class="invalid-feedback">{{ validationErrors.anggotaId }}</div>
                <div v-if="selectedAnggota" class="form-text text-primary">
                  Status Anggota: <strong>{{ selectedAnggota.statusKeanggotaan }}</strong>
                  <span v-if="selectedAnggota.tanggalAktivasi">
                    | Tgl Aktivasi: {{ selectedAnggota.tanggalAktivasi.substring(0, 10) }}
                  </span>
                </div>
              </div>

              <!-- Pilihan Jenis Simpanan -->
              <div class="mb-3">
                <label class="form-label fw-semibold">Jenis Simpanan <span class="text-danger">*</span></label>
                <div class="d-flex gap-4">
                  <div class="form-check">
                    <input
                      class="form-check-input"
                      type="radio"
                      name="jenisSimpanan"
                      id="jenisWajib"
                      value="WAJIB"
                      v-model="form.jenisSimpanan"
                    />
                    <label class="form-check-label fw-semibold text-success" for="jenisWajib">
                      <i class="bi bi-calendar-check me-1"></i> Simpanan Wajib
                    </label>
                  </div>
                  <div class="form-check">
                    <input
                      class="form-check-input"
                      type="radio"
                      name="jenisSimpanan"
                      id="jenisSukarela"
                      value="SUKARELA"
                      v-model="form.jenisSimpanan"
                      :disabled="selectedAnggota?.statusKeanggotaan === 'NONAKTIF'"
                    />
                    <label class="form-check-label fw-semibold text-info" for="jenisSukarela">
                      <i class="bi bi-piggy-bank me-1"></i> Simpanan Sukarela
                    </label>
                  </div>
                </div>
                <div v-if="selectedAnggota?.statusKeanggotaan === 'NONAKTIF'" class="form-text text-danger">
                  Anggota Nonaktif hanya dapat menyetor Simpanan Wajib untuk melunasi tunggakan.
                </div>
              </div>

              <!-- Periode (Hanya untuk Simpanan Wajib) -->
              <div v-if="form.jenisSimpanan === 'WAJIB'" class="mb-3">
                <label class="form-label fw-semibold">Periode Simpanan Wajib (YYYY-MM) <span class="text-danger">*</span></label>
                <input
                  type="text"
                  class="form-control font-monospace"
                  :class="{ 'is-invalid': validationErrors.periode }"
                  placeholder="Contoh: 2026-10"
                  v-model="form.periode"
                />
                <div class="invalid-feedback">{{ validationErrors.periode }}</div>
                <div class="form-text">
                  Format harus <code>YYYY-MM</code>. Tidak boleh mendahului bulan aktivasi dan tidak boleh melebihi bulan berjalan.
                </div>
              </div>

              <!-- Nominal -->
              <div class="mb-3">
                <label class="form-label fw-semibold">Nominal Setoran (Rp) <span class="text-danger">*</span></label>
                <div class="input-group">
                  <span class="input-group-text">Rp</span>
                  <input
                    type="number"
                    :min="form.jenisSimpanan === 'WAJIB' ? 20000 : 1"
                    step="5000"
                    class="form-control fw-bold fs-5"
                    :class="{ 'is-invalid': validationErrors.nominal }"
                    v-model.number="form.nominal"
                  />
                  <div class="invalid-feedback">{{ validationErrors.nominal }}</div>
                </div>
                <div class="form-text">
                  <span v-if="form.jenisSimpanan === 'WAJIB'">Minimal Rp20.000 untuk simpanan wajib.</span>
                  <span v-else>Nominal lebih dari Rp0 untuk simpanan sukarela.</span>
                </div>
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

              <!-- Actions -->
              <div class="d-flex justify-content-end gap-2">
                <RouterLink to="/simpanan" class="btn btn-secondary" :disabled="submitting">Batal</RouterLink>
                <button type="submit" class="btn btn-success fw-semibold px-4" :disabled="submitting">
                  <span v-if="submitting" class="spinner-border spinner-border-sm me-1"></span>
                  Simpan Transaksi
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
