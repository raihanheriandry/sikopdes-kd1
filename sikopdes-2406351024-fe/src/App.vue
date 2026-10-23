<script setup lang="ts">
import { RouterView } from 'vue-router'
import Navbar from './components/Navbar.vue'
</script>

<template>
  <div class="app-layout d-flex flex-column min-vh-100 app-bg-gray">
    <Navbar />

    <main class="flex-grow-1 py-4">
      <div class="container">
        <RouterView />
      </div>
    </main>

    <footer class="app-footer border-top py-3 text-center small mt-auto">
      <div class="container">
        <span class="text-muted">
          &copy; 2026 <strong>SIKD1</strong> Made with ❤️ by Raihan Maulana Heriandry - 2406351024
        </span>
      </div>
    </footer>
  </div>
</template>

<style>
*,
*::before,
*::after {
  border-radius: 0 !important;
}

html,
body,
#app,
.app-layout,
.app-bg-gray {
  background-color: #e5e7eb !important;
  min-height: 100vh;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  color: #1f2937;
  margin: 0;
  padding: 0;
}

.app-footer {
  background-color: #ffffff;
  border-top: 1px solid #e5e7eb !important;
}

.card {
  border-radius: 0 !important;
  border: 1px solid #e2e5e9 !important;
  background-color: #ffffff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04) !important;
}

.card-header {
  background-color: #f8f9fa;
  border-bottom: 1px solid #e5e7eb !important;
  font-weight: 600;
  border-radius: 0 !important;
}

.btn {
  border-radius: 0 !important;
  font-weight: 500;
  letter-spacing: 0.2px;
  transition: all 0.15s ease-in-out;
}

.btn-primary {
  background-color: #b91c1c !important;
  border-color: #b91c1c !important;
  color: #ffffff !important;
}

.btn-primary:hover,
.btn-primary:focus,
.btn-primary:active {
  background-color: #991b1b !important;
  border-color: #991b1b !important;
  color: #ffffff !important;
}

.btn-outline-primary {
  color: #b91c1c !important;
  border-color: #b91c1c !important;
  background-color: transparent !important;
}

.btn-outline-primary:hover,
.btn-outline-primary:focus,
.btn-outline-primary:active {
  background-color: #b91c1c !important;
  border-color: #b91c1c !important;
  color: #ffffff !important;
}

.btn-danger {
  background-color: #b91c1c !important;
  border-color: #b91c1c !important;
}

.btn-danger:hover {
  background-color: #991b1b !important;
  border-color: #991b1b !important;
}

.form-control,
.form-select,
.input-group-text {
  border-radius: 0 !important;
  border-color: #d1d5db;
}

.form-control:focus,
.form-select:focus {
  border-color: #b91c1c !important;
  box-shadow: 0 0 0 2px rgba(185, 28, 28, 0.15) !important;
}

.table th {
  font-weight: 600;
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  background-color: #f8f9fa;
  color: #374151;
  border-bottom: 2px solid #e5e7eb !important;
}

.badge {
  border-radius: 0 !important;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.text-primary {
  color: #b91c1c !important;
}

.border-primary {
  border-color: #b91c1c !important;
}

.dropdown-menu {
  border-radius: 0 !important;
  border: 1px solid #e5e7eb !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08) !important;
}

.alert {
  border-radius: 0 !important;
}

.modal-content {
  border-radius: 0 !important;
}
</style>
