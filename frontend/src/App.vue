<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { basketApi } from './api'
import ThreeBackground from './components/ThreeBackground.vue'

const router = useRouter()
const route = useRoute()

const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
const isLoggedIn = computed(() => !!user.value)
const isAdmin = computed(() => user.value?.role === 'ADMIN')
const cartCount = ref(0)

const userInitials = computed(() => {
  if (!user.value) return ''
  const n = user.value.name || ''
  const s = user.value.surname || ''
  return (n.charAt(0) + s.charAt(0)).toUpperCase()
})

const fetchCartCount = async () => {
  if (!user.value) return
  try {
    const res = await basketApi.getByUser(user.value.id)
    cartCount.value = res.data?.length || 0
  } catch {
    cartCount.value = 0
  }
}

const logout = () => {
  localStorage.removeItem('user')
  localStorage.removeItem('token')
  user.value = null
  cartCount.value = 0
  router.push('/login')
}

window.addEventListener('user-login', (e) => {
  user.value = e.detail
  fetchCartCount()
})

window.addEventListener('cart-updated', fetchCartCount)

onMounted(fetchCartCount)
watch(() => route.path, fetchCartCount)
</script>

<template>
  <div class="global-bg">
    <ThreeBackground mode="subtle" />
  </div>

  <nav class="navbar">
    <router-link to="/" class="navbar-brand">
      <div class="logo-icon">N</div>
      <span>NovaMart</span>
    </router-link>

    <ul class="navbar-nav">
      <li><router-link to="/products">Products</router-link></li>
      <template v-if="isLoggedIn">
        <li><router-link to="/orders">My Orders</router-link></li>
        <li><router-link to="/wishlist">Wishlist</router-link></li>
        <li><router-link to="/invoices">Invoices</router-link></li>
        <li v-if="isAdmin"><router-link to="/admin/dashboard">Admin</router-link></li>
      </template>
    </ul>

    <div class="navbar-right">
      <template v-if="isLoggedIn">
        <router-link to="/basket" class="navbar-cart">
          &#128722;
          <span v-if="cartCount > 0" class="cart-badge">{{ cartCount }}</span>
        </router-link>
        <div class="navbar-user">
          <div class="avatar">{{ userInitials }}</div>
          <span>{{ user.name }}</span>
          <button class="logout-btn" @click="logout">Logout</button>
        </div>
      </template>
      <template v-else>
        <router-link to="/login" class="navbar-auth-btn">Sign In</router-link>
      </template>
    </div>
  </nav>

  <router-view />

  <footer class="footer">
    <p>&copy; 2025 NovaMart. All rights reserved.</p>
  </footer>
</template>
