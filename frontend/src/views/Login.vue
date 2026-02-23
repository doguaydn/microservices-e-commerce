<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../api'

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
    if (user.token) {
      localStorage.setItem('token', user.token)
    }
    window.dispatchEvent(new CustomEvent('user-login', { detail: user }))
    router.push('/products')
  } catch (err) {
    error.value = err.response?.data?.message || err.response?.data || 'Login failed'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-container">
    <div class="card">
      <h1>Login</h1>
      <div v-if="error" class="alert alert-error">{{ error }}</div>
      <form @submit.prevent="login">
        <div class="form-group">
          <label>Email</label>
          <input v-model="form.email" type="email" placeholder="your@email.com" required />
        </div>
        <div class="form-group">
          <label>Password</label>
          <input v-model="form.password" type="password" placeholder="Password" required />
        </div>
        <button type="submit" class="btn btn-primary" style="width:100%" :disabled="loading">
          {{ loading ? 'Logging in...' : 'Login' }}
        </button>
      </form>
      <p class="text-center text-sm mt-1">
        Don't have an account? <router-link to="/register">Register</router-link>
      </p>
    </div>
  </div>
</template>
