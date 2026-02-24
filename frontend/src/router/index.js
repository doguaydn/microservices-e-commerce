import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Home', component: () => import('../views/Home.vue') },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },
  { path: '/products', name: 'Products', component: () => import('../views/Products.vue') },
  { path: '/basket', name: 'Basket', component: () => import('../views/Basket.vue') },
  { path: '/orders', name: 'Orders', component: () => import('../views/Orders.vue') },
  { path: '/wishlist', name: 'Wishlist', component: () => import('../views/Wishlist.vue') },
  { path: '/invoices', name: 'Invoices', component: () => import('../views/Invoices.vue') },
  { path: '/admin', name: 'Admin', component: () => import('../views/Admin.vue'), meta: { requiresAdmin: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAdmin) {
    const user = JSON.parse(localStorage.getItem('user') || 'null')
    if (!user || user.role !== 'ADMIN') {
      next('/products')
      return
    }
  }
  next()
})

export default router
