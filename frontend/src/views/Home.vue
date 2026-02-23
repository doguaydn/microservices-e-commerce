<script setup>
import { ref, computed } from 'vue'

const user = computed(() => JSON.parse(localStorage.getItem('user') || 'null'))
const isLoggedIn = computed(() => !!user.value)
</script>

<template>
  <div class="page">
    <div class="text-center" style="padding: 4rem 0;">
      <h1 style="font-size: 2.5rem; font-weight: 800; margin-bottom: 0.5rem;">E-Commerce Microservices</h1>
      <p class="text-light" style="font-size: 1.1rem; margin-bottom: 2rem;">
        Spring Boot + Vue.js ile mikroservis mimarisi
      </p>

      <div v-if="!isLoggedIn" class="flex gap-2" style="justify-content: center;">
        <router-link to="/login" class="btn btn-primary">Login</router-link>
        <router-link to="/register" class="btn btn-outline">Register</router-link>
      </div>
      <div v-else class="flex gap-2" style="justify-content: center;">
        <router-link to="/products" class="btn btn-primary">Browse Products</router-link>
        <router-link to="/orders" class="btn btn-outline">My Orders</router-link>
      </div>
    </div>

    <div class="stats-grid" style="max-width: 800px; margin: 0 auto;">
      <div class="stat-card">
        <div class="stat-value">5</div>
        <div class="stat-label">Microservices</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">37</div>
        <div class="stat-label">API Endpoints</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">4</div>
        <div class="stat-label">Databases</div>
      </div>
    </div>

    <div class="grid-3 mt-2" style="max-width: 900px; margin: 2rem auto 0;">
      <div class="card">
        <h3>Auth Service</h3>
        <p class="text-sm text-light">JWT authentication, BCrypt hashing, user management</p>
      </div>
      <div class="card">
        <h3>Stock Service</h3>
        <p class="text-sm text-light">Product management, stock tracking, low-stock alerts</p>
      </div>
      <div class="card">
        <h3>Basket Service</h3>
        <p class="text-sm text-light">Shopping cart, checkout, orders, wishlist</p>
      </div>
      <div class="card">
        <h3>Invoice Service</h3>
        <p class="text-sm text-light">Invoice generation, order-based invoicing</p>
      </div>
      <div class="card">
        <h3>Notification Service</h3>
        <p class="text-sm text-light">RabbitMQ events, email notifications</p>
      </div>
      <div class="card">
        <h3>API Gateway</h3>
        <p class="text-sm text-light">Route management, service discovery via Eureka</p>
      </div>
    </div>
  </div>
</template>
