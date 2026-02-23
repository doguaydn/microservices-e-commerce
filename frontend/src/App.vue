<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
const isLoggedIn = computed(() => !!user.value)

const logout = () => {
  localStorage.removeItem('user')
  localStorage.removeItem('token')
  user.value = null
  router.push('/login')
}

// Listen for login events
window.addEventListener('user-login', (e) => {
  user.value = e.detail
})
</script>

<template>
  <nav class="nav">
    <router-link to="/" class="nav-brand">E-Commerce</router-link>
    <ul class="nav-links">
      <li><router-link to="/products">Products</router-link></li>
      <template v-if="isLoggedIn">
        <li><router-link to="/basket">Basket</router-link></li>
        <li><router-link to="/orders">Orders</router-link></li>
        <li><router-link to="/wishlist">Wishlist</router-link></li>
        <li><router-link to="/invoices">Invoices</router-link></li>
      </template>
    </ul>
    <div class="nav-user" v-if="isLoggedIn">
      <span>{{ user.name }}</span>
      <button @click="logout">Logout</button>
    </div>
    <div class="nav-user" v-else>
      <router-link to="/login" class="btn btn-sm btn-primary">Login</router-link>
    </div>
  </nav>
  <router-view />
</template>
