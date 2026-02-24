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
      <ThreeBackground mode="galaxy" :particle-count="4000" />
      <div class="hero-content">
        <div class="hero-badge">New Collection 2025</div>
        <h1>Discover Your <span class="gradient-text">Style</span></h1>
        <p>Premium products curated just for you. Experience the future of online shopping with fast delivery and secure payments.</p>
        <div class="hero-actions">
          <router-link to="/products" class="btn btn-accent btn-lg">Shop Now</router-link>
          <router-link v-if="!isLoggedIn" to="/register" class="btn btn-outline btn-lg" style="border-color: rgba(255,255,255,0.2); color: #fff;">Create Account</router-link>
        </div>
        <div class="hero-stats">
          <div class="hero-stat">
            <span class="hero-stat-value">500+</span>
            <span class="hero-stat-label">Products</span>
          </div>
          <div class="hero-stat-divider"></div>
          <div class="hero-stat">
            <span class="hero-stat-value">10K+</span>
            <span class="hero-stat-label">Happy Customers</span>
          </div>
          <div class="hero-stat-divider"></div>
          <div class="hero-stat">
            <span class="hero-stat-value">24/7</span>
            <span class="hero-stat-label">Support</span>
          </div>
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

<style scoped>
.hero-badge {
  display: inline-block;
  padding: 0.4rem 1.25rem;
  border-radius: 9999px;
  font-size: 0.8rem;
  font-weight: 600;
  letter-spacing: 0.05em;
  color: #00f0ff;
  border: 1px solid rgba(0, 240, 255, 0.25);
  background: rgba(0, 240, 255, 0.06);
  margin-bottom: 1.5rem;
  text-transform: uppercase;
}

.hero-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2rem;
  margin-top: 3rem;
  padding-top: 2rem;
  border-top: 1px solid rgba(255,255,255,0.08);
}

.hero-stat { text-align: center; }

.hero-stat-value {
  display: block;
  font-size: 1.5rem;
  font-weight: 800;
  color: #fff;
}

.hero-stat-label {
  font-size: 0.8rem;
  color: #5a6380;
  font-weight: 500;
}

.hero-stat-divider {
  width: 1px;
  height: 40px;
  background: rgba(255,255,255,0.1);
}

@media (max-width: 768px) {
  .hero-stats { gap: 1rem; }
  .hero-stat-value { font-size: 1.2rem; }
}
</style>
