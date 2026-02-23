<script setup>
import { ref, onMounted, computed } from 'vue'
import { wishlistApi, productApi, basketApi } from '../api'

const wishlistItems = ref([])
const products = ref({})
const loading = ref(true)
const error = ref('')
const success = ref('')

const user = computed(() => JSON.parse(localStorage.getItem('user') || 'null'))

const fetchWishlist = async () => {
  if (!user.value) return
  loading.value = true
  try {
    const [wishRes, prodRes] = await Promise.all([
      wishlistApi.getByUser(user.value.id),
      productApi.getAll()
    ])
    wishlistItems.value = wishRes.data
    const map = {}
    prodRes.data.forEach(p => { map[p.id] = p })
    products.value = map
  } catch {
    error.value = 'Failed to load wishlist'
  } finally {
    loading.value = false
  }
}

const removeFromWishlist = async (id) => {
  try {
    await wishlistApi.remove(id)
    wishlistItems.value = wishlistItems.value.filter(i => i.id !== id)
    success.value = 'Removed from wishlist'
    clearMsg()
  } catch {
    error.value = 'Failed to remove item'
    clearMsg()
  }
}

const addToBasket = async (productId) => {
  const product = products.value[productId]
  if (!product || !user.value) return
  try {
    await basketApi.add({
      userId: user.value.id,
      productId: product.id,
      productName: product.name,
      price: product.price,
      quantity: 1
    })
    success.value = `${product.name} added to cart!`
    window.dispatchEvent(new Event('cart-updated'))
    clearMsg()
  } catch {
    error.value = 'Failed to add to cart'
    clearMsg()
  }
}

const clearMsg = () => setTimeout(() => { success.value = ''; error.value = '' }, 3000)

onMounted(fetchWishlist)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h1>My Wishlist</h1>
      <p>{{ wishlistItems.length }} saved items</p>
    </div>

    <div v-if="!user" class="alert alert-info">Please sign in to view your wishlist.</div>
    <div v-if="success" class="alert alert-success">{{ success }}</div>
    <div v-if="error" class="alert alert-error">{{ error }}</div>

    <div v-if="loading && user" class="loading">
      <div class="spinner"></div>
      <p>Loading wishlist...</p>
    </div>

    <template v-else-if="user">
      <div v-if="wishlistItems.length === 0" class="empty-state">
        <div class="empty-icon">&#9825;</div>
        <h3>Your wishlist is empty</h3>
        <p>Save products you love by clicking the heart icon.</p>
        <router-link to="/products" class="btn btn-primary mt-2">Discover Products</router-link>
      </div>

      <div v-else class="product-grid">
        <div v-for="item in wishlistItems" :key="item.id" class="product-card">
          <div class="product-image">
            &#128717;
            <button class="wishlist-btn active" @click="removeFromWishlist(item.id)">&#9829;</button>
          </div>
          <div class="product-body">
            <template v-if="products[item.productId]">
              <div class="product-name">{{ products[item.productId].name }}</div>
              <div class="product-desc">{{ products[item.productId].description }}</div>
              <div class="product-footer">
                <div class="product-price">${{ products[item.productId].price?.toFixed(2) }}</div>
                <div :class="['product-stock', products[item.productId].quantity <= 5 ? 'low' : '']">
                  {{ products[item.productId].quantity }} in stock
                </div>
              </div>
            </template>
            <template v-else>
              <div class="product-name">Product #{{ item.productId }}</div>
              <div class="product-desc">Product details unavailable</div>
            </template>
          </div>
          <div class="product-actions">
            <button class="btn btn-accent btn-sm" style="flex:1" @click="addToBasket(item.productId)">
              Add to Cart
            </button>
            <button class="btn btn-outline btn-sm" @click="removeFromWishlist(item.id)">Remove</button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
