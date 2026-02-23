<script setup>
import { ref, computed, onMounted } from 'vue'
import { productApi, basketApi, wishlistApi } from '../api'
import ThreeBackground from '../components/ThreeBackground.vue'

const user = computed(() => JSON.parse(localStorage.getItem('user') || 'null'))
const isLoggedIn = computed(() => !!user.value)
const featuredProducts = ref([])

const fetchFeatured = async () => {
  try {
    const res = await productApi.getAll()
    featuredProducts.value = (res.data || []).slice(0, 4)
  } catch {
    featuredProducts.value = []
  }
}

const addToBasket = async (product) => {
  if (!user.value) return
  try {
    await basketApi.add({
      userId: user.value.id,
      productId: product.id,
      productName: product.name,
      price: product.price,
      quantity: 1
    })
    window.dispatchEvent(new Event('cart-updated'))
  } catch {}
}

const addToWishlist = async (product) => {
  if (!user.value) return
  try {
    await wishlistApi.add({ userId: user.value.id, productId: product.id })
  } catch {}
}

onMounted(fetchFeatured)
</script>

<template>
  <div>
    <!-- Hero Section -->
    <section class="hero">
      <ThreeBackground />
      <div class="hero-content">
        <h1>Discover Your <span class="gradient-text">Style</span></h1>
        <p>Premium products curated for you. Shop the latest trends with confidence and fast delivery.</p>
        <div class="hero-actions">
          <router-link to="/products" class="btn btn-accent btn-lg">Shop Now</router-link>
          <router-link v-if="!isLoggedIn" to="/register" class="btn btn-outline btn-lg" style="border-color: rgba(255,255,255,0.3); color: #fff;">Create Account</router-link>
        </div>
      </div>
    </section>

    <!-- Features Bar -->
    <div class="features-bar">
      <div class="feature-item">
        <div class="feature-icon">&#128666;</div>
        <h3>Free Shipping</h3>
        <p>On orders over $50</p>
      </div>
      <div class="feature-item">
        <div class="feature-icon">&#128274;</div>
        <h3>Secure Payment</h3>
        <p>100% secure checkout</p>
      </div>
      <div class="feature-item">
        <div class="feature-icon">&#9889;</div>
        <h3>Fast Delivery</h3>
        <p>Get it within 2-3 days</p>
      </div>
      <div class="feature-item">
        <div class="feature-icon">&#128257;</div>
        <h3>Easy Returns</h3>
        <p>30-day return policy</p>
      </div>
    </div>

    <!-- Featured Products -->
    <section class="page" v-if="featuredProducts.length > 0">
      <div class="flex-between mb-2">
        <h2 class="section-title">Featured Products</h2>
        <router-link to="/products" class="btn btn-outline btn-sm">View All</router-link>
      </div>
      <div class="product-grid">
        <div v-for="p in featuredProducts" :key="p.id" class="product-card">
          <div class="product-image">
            &#128717;
            <button v-if="isLoggedIn" class="wishlist-btn" @click="addToWishlist(p)">&#9825;</button>
          </div>
          <div class="product-body">
            <div class="product-name">{{ p.name }}</div>
            <div class="product-desc">{{ p.description || 'Premium quality product' }}</div>
            <div class="product-footer">
              <div class="product-price">${{ p.price?.toFixed(2) }}</div>
              <div :class="['product-stock', p.quantity <= 5 ? 'low' : '']">
                {{ p.quantity > 0 ? p.quantity + ' in stock' : 'Out of stock' }}
              </div>
            </div>
          </div>
          <div class="product-actions">
            <button v-if="isLoggedIn" class="btn btn-accent btn-sm" style="flex:1" @click="addToBasket(p)">Add to Cart</button>
            <router-link v-else to="/login" class="btn btn-accent btn-sm" style="flex:1">Sign in to Buy</router-link>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>
