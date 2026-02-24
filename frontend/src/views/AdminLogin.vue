<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../api'
import ThreeBackground from '../components/ThreeBackground.vue'

const router = useRouter()
const form = ref({ email: '', password: '' })
const error = ref('')
const loading = ref(false)

const login = async () => {
  error.value = ''
  loading.value = true
  try {
    const res = await authApi.adminLogin(form.value)
    const user = res.data
    localStorage.setItem('user', JSON.stringify(user))
    if (user.token) localStorage.setItem('token', user.token)
    window.dispatchEvent(new CustomEvent('user-login', { detail: user }))
    router.push('/admin/dashboard')
  } catch (err) {
    error.value = err.response?.data?.message || err.response?.data || 'Invalid email or password'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <ThreeBackground :particle-count="80" />
    <div class="auth-card">
      <div style="text-align: center; margin-bottom: 0.5rem;">
        <span style="font-size: 2rem; font-weight: 800; color: var(--accent);">Admin Panel</span>
      </div>
      <p class="auth-subtitle">Sign in with your admin account</p>
      <div v-if="error" class="alert alert-error">{{ error }}</div>
      <form @submit.prevent="login">
        <div class="form-group">
          <label>Email</label>
          <input v-model="form.email" type="email" placeholder="admin@admin.com" required />
        </div>
        <div class="form-group">
          <label>Password</label>
          <input v-model="form.password" type="password" placeholder="Enter admin password" required />
        </div>
        <button type="submit" class="btn btn-primary btn-block btn-lg" :disabled="loading">
          {{ loading ? 'Signing in...' : 'Admin Login' }}
        </button>
      </form>
      <p class="auth-footer">
        <router-link to="/login">Back to user login</router-link>
      </p>
    </div>
  </div>
</template>
