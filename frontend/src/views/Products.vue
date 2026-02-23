<script setup>
import { ref, onMounted, computed } from 'vue'
import { productApi, basketApi, wishlistApi } from '../api'

const products = ref([])
const loading = ref(true)
const error = ref('')
const success = ref('')
const showAddModal = ref(false)
const newProduct = ref({ name: '', description: '', price: 0, quantity: 0 })

const user = computed(() => JSON.parse(localStorage.getItem('user') || 'null'))

const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await productApi.getAll()
    products.value = res.data
  } catch (err) {
    error.value = 'Failed to load products'
  } finally {
    loading.value = false
  }
}

const addProduct = async () => {
  try {
    await productApi.create(newProduct.value)
    success.value = 'Product added!'
    showAddModal.value = false
    newProduct.value = { name: '', description: '', price: 0, quantity: 0 }
    fetchProducts()
    clearMsg()
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to add product'
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
    success.value = `${product.name} added to basket!`
    clearMsg()
  } catch (err) {
    error.value = 'Failed to add to basket'
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
    error.value = err.response?.data?.message || 'Failed to add to wishlist'
    clearMsg()
  }
}

const deleteProduct = async (id) => {
  if (!confirm('Delete this product?')) return
  try {
    await productApi.delete(id)
    success.value = 'Product deleted'
    fetchProducts()
    clearMsg()
  } catch (err) {
    error.value = 'Failed to delete product'
  }
}

const clearMsg = () => {
  setTimeout(() => { success.value = ''; error.value = '' }, 3000)
}

onMounted(fetchProducts)
</script>

<template>
  <div class="page">
    <div class="page-header flex-between">
      <div>
        <h1>Products</h1>
        <p>Browse and manage products</p>
      </div>
      <button class="btn btn-primary" @click="showAddModal = true">+ Add Product</button>
    </div>

    <div v-if="success" class="alert alert-success">{{ success }}</div>
    <div v-if="error" class="alert alert-error">{{ error }}</div>

    <div v-if="loading" class="loading">Loading products...</div>

    <div v-else-if="products.length === 0" class="empty-state">
      <h3>No products yet</h3>
      <p>Click "Add Product" to create your first product.</p>
    </div>

    <div v-else class="product-grid">
      <div v-for="p in products" :key="p.id" class="product-card">
        <h3>{{ p.name }}</h3>
        <p class="text-sm text-light">{{ p.description }}</p>
        <div class="price">${{ p.price?.toFixed(2) }}</div>
        <div class="stock">Stock: {{ p.quantity }}</div>
        <div class="actions">
          <button v-if="user" class="btn btn-primary btn-sm" @click="addToBasket(p)">Add to Basket</button>
          <button v-if="user" class="btn btn-outline btn-sm" @click="addToWishlist(p)">Wishlist</button>
          <button class="btn btn-danger btn-sm" @click="deleteProduct(p.id)">Delete</button>
        </div>
      </div>
    </div>

    <!-- Add Product Modal -->
    <div v-if="showAddModal" class="modal-overlay" @click.self="showAddModal = false">
      <div class="modal">
        <h2>Add New Product</h2>
        <form @submit.prevent="addProduct">
          <div class="form-group">
            <label>Name</label>
            <input v-model="newProduct.name" required />
          </div>
          <div class="form-group">
            <label>Description</label>
            <textarea v-model="newProduct.description" rows="3"></textarea>
          </div>
          <div class="grid-2">
            <div class="form-group">
              <label>Price</label>
              <input v-model.number="newProduct.price" type="number" step="0.01" min="0" required />
            </div>
            <div class="form-group">
              <label>Quantity</label>
              <input v-model.number="newProduct.quantity" type="number" min="0" required />
            </div>
          </div>
          <div class="modal-actions">
            <button type="button" class="btn btn-outline" @click="showAddModal = false">Cancel</button>
            <button type="submit" class="btn btn-primary">Add Product</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
