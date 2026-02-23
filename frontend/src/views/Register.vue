<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../api'
import ThreeBackground from '../components/ThreeBackground.vue'

const router = useRouter()
const form = ref({ name: '', surname: '', email: '', password: '' })
const error = ref('')
const success = ref('')
const loading = ref(false)

const register = async () => {
  error.value = ''
  success.value = ''
  loading.value = true
  try {
    await authApi.register(form.value)
    success.value = 'Account created! Redirecting to sign in...'
    setTimeout(() => router.push('/login'), 1500)
  } catch (err) {
    error.value = err.response?.data?.message || err.response?.data || 'Registration failed'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <ThreeBackground :particle-count="80" color1="#764ba2" color2="#667eea" />
    <div class="auth-card">
      <h1>Create Account</h1>
      <p class="auth-subtitle">Join NovaMart today</p>
      <div v-if="error" class="alert alert-error">{{ error }}</div>
      <div v-if="success" class="alert alert-success">{{ success }}</div>
      <form @submit.prevent="register">
        <div class="grid-2">
          <div class="form-group">
            <label>First Name</label>
            <input v-model="form.name" type="text" placeholder="John" required />
          </div>
          <div class="form-group">
            <label>Last Name</label>
            <input v-model="form.surname" type="text" placeholder="Doe" required />
          </div>
        </div>
        <div class="form-group">
          <label>Email</label>
          <input v-model="form.email" type="email" placeholder="your@email.com" required />
        </div>
        <div class="form-group">
          <label>Password</label>
          <input v-model="form.password" type="password" placeholder="Create a password" required />
        </div>
        <button type="submit" class="btn btn-primary btn-block btn-lg" :disabled="loading">
          {{ loading ? 'Creating account...' : 'Create Account' }}
        </button>
      </form>
      <p class="auth-footer">
        Already have an account? <router-link to="/login">Sign in</router-link>
      </p>
    </div>
  </div>
</template>
