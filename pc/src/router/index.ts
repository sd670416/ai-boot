import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/pages/LoginPage.vue'),
    meta: { guest: true },
  },
  {
    path: '/',
    component: () => import('@/layouts/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', name: 'dashboard', component: () => import('@/pages/AdminStatsPage.vue'), meta: { admin: true } },
      { path: 'products', name: 'products', component: () => import('@/pages/ProductsPage.vue') },
      { path: 'orders', name: 'orders', component: () => import('@/pages/OrdersPage.vue') },
      { path: 'users/backend', name: 'backend-users', component: () => import('@/pages/AdminUsersPage.vue'), meta: { admin: true, userType: 'BACKEND' } },
      { path: 'users/frontend', name: 'frontend-users', component: () => import('@/pages/AdminUsersPage.vue'), meta: { admin: true, userType: 'FRONTEND' } },
      { path: 'cart', name: 'cart', component: () => import('@/pages/CartPage.vue') },
      { path: 'addresses', name: 'addresses', component: () => import('@/pages/AddressesPage.vue') },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (auth.token && !auth.user) {
    try {
      await auth.refreshMe()
    } catch {
      auth.logout()
    }
  }

  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return '/login'
  }

  if (to.meta.guest && auth.isLoggedIn) {
    return auth.isAdmin ? '/dashboard' : '/products'
  }

  if (to.meta.admin && !auth.isAdmin) {
    return '/products'
  }

  return true
})

export default router
