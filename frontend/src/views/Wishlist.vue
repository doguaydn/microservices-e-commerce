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
    const res = await wishlistApi.getByUser(user.value.id)
    wishlistItems.value = res.data

    // Fetch product details for each item
    const productRes = await productApi.getAll()
    const map = {}
    productRes.data.forEach(p => { map[p.id] = p })
    products.value = map
  } catch (err) {
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
  } catch (err) {
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
    success.value = `${product.name} added to basket!`
    clearMsg()
  } catch (err) {
    error.value = 'Failed to add to basket'
    clearMsg()
  }
}

const clearMsg = () => {
  setTimeout(() => { success.value = ''; error.value = '' }, 3000)
}

onMounted(fetchWishlist)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h1>My Wishlist</h1>
      <p>Your favorite products</p>
    </div>

    <div v-if="!user" class="alert alert-info">Please login to view your wishlist.</div>
    <div v-if="success" class="alert alert-success">{{ success }}</div>
    <div v-if="error" class="alert alert-error">{{ error }}</div>

    <div v-if="loading && user" class="loading">Loading wishlist...</div>

    <template v-else-if="user">
      <div v-if="wishlistItems.length === 0" class="empty-state">
        <h3>Your wishlist is empty</h3>
        <p>Browse <router-link to="/products">products</router-link> and add your favorites.</p>
      </div>

      <div v-else class="product-grid">
        <div v-for="item in wishlistItems" :key="item.id" class="product-card">
          <template v-if="products[item.productId]">
            <h3>{{ products[item.productId].name }}</h3>
            <p class="text-sm text-light">{{ products[item.productId].description }}</p>
            <div class="price">${{ products[item.productId].price?.toFixed(2) }}</div>
            <div class="stock">Stock: {{ products[item.productId].quantity }}</div>
          </template>
          <template v-else>
            <h3>Product #{{ item.productId }}</h3>
            <p class="text-sm text-light">Product details not available</p>
          </template>
          <div class="text-sm text-light">Added: {{ item.createdAt }}</div>
          <div class="actions">
            <button class="btn btn-primary btn-sm" @click="addToBasket(item.productId)">Add to Basket</button>
            <button class="btn btn-danger btn-sm" @click="removeFromWishlist(item.id)">Remove</button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
