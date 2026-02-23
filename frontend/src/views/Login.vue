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
    const res = await authApi.login(form.value)
    const user = res.data
    localStorage.setItem('user', JSON.stringify(user))
    if (user.token) localStorage.setItem('token', user.token)
    window.dispatchEvent(new CustomEvent('user-login', { detail: user }))
    router.push('/products')
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
      <h1>Welcome Back</h1>
      <p class="auth-subtitle">Sign in to your account</p>
      <div v-if="error" class="alert alert-error">{{ error }}</div>
      <form @submit.prevent="login">
        <div class="form-group">
          <label>Email</label>
          <input v-model="form.email" type="email" placeholder="your@email.com" required />
        </div>
        <div class="form-group">
          <label>Password</label>
          <input v-model="form.password" type="password" placeholder="Enter your password" required />
        </div>
        <button type="submit" class="btn btn-primary btn-block btn-lg" :disabled="loading">
          {{ loading ? 'Signing in...' : 'Sign In' }}
        </button>
      </form>
      <p class="auth-footer">
        Don't have an account? <router-link to="/register">Create one</router-link>
      </p>
    </div>
  </div>
</template>
