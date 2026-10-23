import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import AnggotaListView from '@/views/anggota/AnggotaListView.vue'
import AnggotaCreateView from '@/views/anggota/AnggotaCreateView.vue'
import AnggotaDetailView from '@/views/anggota/AnggotaDetailView.vue'
import AnggotaUpdateView from '@/views/anggota/AnggotaUpdateView.vue'
import SimpananListView from '@/views/simpanan/SimpananListView.vue'
import SimpananDetailView from '@/views/simpanan/SimpananDetailView.vue'
import SimpananPokokCreateView from '@/views/simpanan/SimpananPokokCreateView.vue'
import SimpananCreateView from '@/views/simpanan/SimpananCreateView.vue'
import KepatuhanView from '@/views/simpanan/KepatuhanView.vue'
import RiwayatStatusListView from '@/views/riwayat/RiwayatStatusListView.vue'
import RiwayatStatusDetailView from '@/views/riwayat/RiwayatStatusDetailView.vue'
import StatistikView from '@/views/statistik/StatistikView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    // Anggota
    {
      path: '/anggota',
      name: 'anggota-list',
      component: AnggotaListView,
    },
    {
      path: '/anggota/create',
      name: 'anggota-create',
      component: AnggotaCreateView,
    },
    {
      path: '/anggota/:id',
      name: 'anggota-detail',
      component: AnggotaDetailView,
    },
    {
      path: '/anggota/:id/update',
      name: 'anggota-update',
      component: AnggotaUpdateView,
    },
    // Simpanan
    {
      path: '/simpanan',
      name: 'simpanan-list',
      component: SimpananListView,
    },
    {
      path: '/simpanan/pokok/create',
      name: 'simpanan-pokok-create',
      component: SimpananPokokCreateView,
    },
    {
      path: '/simpanan/create',
      name: 'simpanan-create',
      component: SimpananCreateView,
    },
    {
      path: '/simpanan/kepatuhan',
      name: 'simpanan-kepatuhan',
      component: KepatuhanView,
    },
    {
      path: '/simpanan/:id',
      name: 'simpanan-detail',
      component: SimpananDetailView,
    },
    // Riwayat Status
    {
      path: '/riwayat-status',
      name: 'riwayat-status-list',
      component: RiwayatStatusListView,
    },
    {
      path: '/riwayat-status/:id',
      name: 'riwayat-status-detail',
      component: RiwayatStatusDetailView,
    },
    // Statistik
    {
      path: '/statistik',
      name: 'statistik',
      component: StatistikView,
    },
  ],
})

export default router
