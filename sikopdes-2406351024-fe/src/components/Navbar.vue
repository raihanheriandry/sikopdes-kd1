<script setup lang="ts">
import { ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'

const route = useRoute()
const isDropdownOpen = ref(false)
const isNavbarOpen = ref(false)

const isActive = (path: string) => {
  if (!route || !route.path) return false
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

const closeAll = () => {
  isDropdownOpen.value = false
  isNavbarOpen.value = false
}

watch(
  () => route.fullPath,
  () => {
    closeAll()
  }
)
</script>

<template>
  <nav class="navbar navbar-expand-lg navbar-dark bg-navbar sticky-top">
    <div class="container">
      <RouterLink class="navbar-brand fw-bold d-flex align-items-center gap-2" to="/" @click="closeAll">
        <span class="brand-badge">SIKopDes</span>
        <span class="brand-subtext text-white-50">Desa Merah Biru</span>
      </RouterLink>

      <button
        class="navbar-toggler rounded-0 border-secondary"
        type="button"
        @click="isNavbarOpen = !isNavbarOpen"
        aria-controls="navbarMain"
        :aria-expanded="isNavbarOpen"
        aria-label="Toggle navigation"
      >
        <span class="navbar-toggler-icon"></span>
      </button>

      <div class="collapse navbar-collapse" :class="{ show: isNavbarOpen }" id="navbarMain">
        <ul class="navbar-nav ms-auto mb-2 mb-lg-0">
          <!-- Home -->
          <li class="nav-item">
            <RouterLink class="nav-link" :class="{ active: isActive('/') }" to="/" @click="closeAll">
              Home
            </RouterLink>
          </li>

          <!-- Anggota -->
          <li class="nav-item">
            <RouterLink class="nav-link" :class="{ active: isActive('/anggota') }" to="/anggota" @click="closeAll">
              Anggota
            </RouterLink>
          </li>

          <!-- Simpanan Dropdown (Hover-based) -->
          <li
            class="nav-item dropdown dropdown-hover"
            @mouseenter="isDropdownOpen = true"
            @mouseleave="isDropdownOpen = false"
          >
            <RouterLink
              class="nav-link dropdown-toggle"
              :class="{ active: isActive('/simpanan'), show: isDropdownOpen }"
              to="/simpanan"
              role="button"
              :aria-expanded="isDropdownOpen"
              @click="closeAll"
            >
              Simpanan
            </RouterLink>
            <ul
              class="dropdown-menu border-0 shadow"
              :class="{ show: isDropdownOpen }"
              @click="closeAll"
            >
              <li>
                <RouterLink class="dropdown-item" to="/simpanan" @click="closeAll">
                  Daftar Simpanan
                </RouterLink>
              </li>
              <li>
                <RouterLink class="dropdown-item" to="/simpanan/pokok/create" @click="closeAll">
                  Catat Simpanan Pokok
                </RouterLink>
              </li>
              <li>
                <RouterLink class="dropdown-item" to="/simpanan/create" @click="closeAll">
                  Catat Wajib / Sukarela
                </RouterLink>
              </li>
              <li><hr class="dropdown-divider" /></li>
              <li>
                <RouterLink class="dropdown-item" to="/simpanan/kepatuhan" @click="closeAll">
                  Kepatuhan Simpanan
                </RouterLink>
              </li>
            </ul>
          </li>

          <!-- Riwayat Status -->
          <li class="nav-item">
            <RouterLink class="nav-link" :class="{ active: isActive('/riwayat-status') }" to="/riwayat-status" @click="closeAll">
              Riwayat Status
            </RouterLink>
          </li>

          <!-- Statistik -->
          <li class="nav-item">
            <RouterLink class="nav-link" :class="{ active: isActive('/statistik') }" to="/statistik" @click="closeAll">
              Statistik
            </RouterLink>
          </li>
        </ul>

      </div>
    </div>
  </nav>
</template>

<style scoped>
.bg-navbar {
  background-color: #1e232a !important;
  border-bottom: 2px solid #b91c1c;
}

.brand-badge {
  background-color: #b91c1c;
  color: #ffffff;
  padding: 0.2rem 0.6rem;
  font-size: 0.95rem;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.brand-subtext {
  font-size: 0.85rem;
  font-weight: 400;
}

.nav-link {
  font-weight: 500;
  color: #cbd5e1 !important;
  padding: 0.6rem 0.9rem !important;
  transition: all 0.15s ease-in-out;
  position: relative;
}

.nav-link:hover {
  color: #ffffff !important;
  background-color: rgba(255, 255, 255, 0.05);
}

.nav-link.active {
  color: #ffffff !important;
  background-color: rgba(185, 28, 28, 0.25);
  font-weight: 600;
  border-bottom: 2px solid #b91c1c;
}

.dropdown-hover {
  position: relative;
}

@media (min-width: 992px) {
  .dropdown-hover:hover > .dropdown-menu {
    display: block !important;
    margin-top: 0;
  }

  .dropdown-hover:hover > .nav-link {
    color: #ffffff !important;
    background-color: rgba(255, 255, 255, 0.05);
  }
}

.dropdown-menu {
  background-color: #ffffff;
  padding: 0.25rem 0;
  min-width: 210px;
  margin-top: 0;
  border: 1px solid #e5e7eb !important;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.12) !important;
}

.dropdown-item {
  padding: 0.55rem 1rem;
  font-size: 0.875rem;
  color: #374151;
  transition: background-color 0.1s ease;
}

.dropdown-item:hover {
  background-color: #fee2e2;
  color: #991b1b;
}

.module-tag {
  border-left: 1px solid #4b5563;
  padding-left: 0.75rem;
  color: #9ca3af;
  font-size: 0.8rem;
}
</style>
