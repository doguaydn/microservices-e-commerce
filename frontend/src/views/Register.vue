<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../api'

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
    success.value = 'Registration successful! Redirecting to login...'
    setTimeout(() => router.push('/login'), 1500)
  } catch (err) {
    error.value = err.response?.data?.message || err.response?.data || 'Registration failed'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-container">
    <div class="card">
      <h1>Register</h1>
      <div v-if="error" class="alert alert-error">{{ error }}</div>
      <div v-if="success" class="alert alert-success">{{ success }}</div>
      <form @submit.prevent="register">
        <div class="form-group">
          <label>Name</label>
          <input v-model="form.name" type="text" placeholder="Name" required />
        </div>
        <div class="form-group">
          <label>Surname</label>
          <input v-model="form.surname" type="text" placeholder="Surname" required />
        </div>
        <div class="form-group">
          <label>Email</label>
          <input v-model="form.email" type="email" placeholder="your@email.com" required />
        </div>
        <div class="form-group">
          <label>Password</label>
          <input v-model="form.password" type="password" placeholder="Password" required />
        </div>
        <button type="submit" class="btn btn-primary" style="width:100%" :disabled="loading">
          {{ loading ? 'Registering...' : 'Register' }}
        </button>
      </form>
      <p class="text-center text-sm mt-1">
        Already have an account? <router-link to="/login">Login</router-link>
      </p>
    </div>
  </div>
</template>
