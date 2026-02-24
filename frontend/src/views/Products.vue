<script setup>
import { ref, onMounted, computed } from 'vue'
import { productApi, basketApi, wishlistApi } from '../api'

const products = ref([])
const loading = ref(true)
const error = ref('')
const success = ref('')
const searchQuery = ref('')

const user = computed(() => JSON.parse(localStorage.getItem('user') || 'null'))

const filteredProducts = computed(() => {
  if (!searchQuery.value) return products.value
  const q = searchQuery.value.toLowerCase()
  return products.value.filter(p =>
    p.name?.toLowerCase().includes(q) || p.description?.toLowerCase().includes(q)
  )
})

const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await productApi.getAll()
    products.value = res.data
  } catch {
    error.value = 'Failed to load products'
  } finally {
    loading.value = false
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
      quantity: getQuantity(product.id)
    })
    success.value = `${product.name} x${getQuantity(product.id)} added to cart!`
    quantities.value[product.id] = 1
    window.dispatchEvent(new Event('cart-updated'))
    clearMsg()
  } catch {
    error.value = 'Failed to add to cart'
    clearMsg()
  }
}

const addToWishlist = async (product) => {
  if (!user.value) return
  try {
    await wishlistApi.add({ userId: user.value.id, productId: product.id })
    success.value = `${product.name} added to wishlist!`
    clearMsg()
  } catch (err) {
    error.value = err.response?.data?.message || 'Already in wishlist'
    clearMsg()
  }
}

const quantities = ref({})
const getQuantity = (id) => quantities.value[id] || 1
const setQuantity = (id, val, max) => {
  quantities.value[id] = Math.max(1, Math.min(val, max, 99))
}

const clearMsg = () => setTimeout(() => { success.value = ''; error.value = '' }, 3000)

onMounted(fetchProducts)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h1>Products</h1>
      <p>{{ filteredProducts.length }} products available</p>
    </div>

    <div class="search-bar">
      <span class="search-icon">&#128269;</span>
      <input v-model="searchQuery" placeholder="Search products..." />
    </div>

    <div v-if="success" class="alert alert-success">{{ success }}</div>
    <div v-if="error" class="alert alert-error">{{ error }}</div>

    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>Loading products...</p>
    </div>

    <div v-else-if="filteredProducts.length === 0" class="empty-state">
      <div class="empty-icon">&#128717;</div>
      <h3>No products found</h3>
      <p v-if="searchQuery">Try a different search term</p>
      <p v-else>No products available yet.</p>
    </div>

    <div v-else class="product-grid">
      <div v-for="p in filteredProducts" :key="p.id" class="product-card">
        <div class="product-image">
          &#128717;
          <button v-if="user" class="wishlist-btn" @click="addToWishlist(p)">&#9825;</button>
        </div>
        <div class="product-body">
          <div class="product-name">{{ p.name }}</div>
          <div class="product-desc">{{ p.description || 'No description available' }}</div>
          <div class="product-footer">
            <div class="product-price">${{ p.price?.toFixed(2) }}</div>
            <div :class="['product-stock', p.quantity <= 5 ? 'low' : '']">
              {{ p.quantity > 0 ? p.quantity + ' in stock' : 'Out of stock' }}
            </div>
          </div>
        </div>
        <div class="product-actions">
          <div v-if="user" class="qty-stepper" style="margin-right: 0.5rem;">
            <button @click="setQuantity(p.id, getQuantity(p.id) - 1, p.quantity)">-</button>
            <span>{{ getQuantity(p.id) }}</span>
            <button @click="setQuantity(p.id, getQuantity(p.id) + 1, p.quantity)">+</button>
          </div>
          <button v-if="user" class="btn btn-accent btn-sm" style="flex:1" @click="addToBasket(p)" :disabled="p.quantity < 1">
            Add to Cart
          </button>
        </div>
      </div>
    </div>

  </div>
</template>
