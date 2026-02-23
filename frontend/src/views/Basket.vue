<script setup>
import { ref, onMounted, computed } from 'vue'
import { basketApi } from '../api'

const items = ref([])
const loading = ref(true)
const error = ref('')
const success = ref('')

const user = computed(() => JSON.parse(localStorage.getItem('user') || 'null'))
const totalAmount = computed(() => items.value.reduce((sum, i) => sum + (i.price * i.quantity), 0))

const fetchBasket = async () => {
  if (!user.value) return
  loading.value = true
  try {
    const res = await basketApi.getByUser(user.value.id)
    items.value = res.data
  } catch (err) {
    error.value = 'Failed to load basket'
  } finally {
    loading.value = false
  }
}

const updateQuantity = async (item, delta) => {
  const newQty = item.quantity + delta
  if (newQty < 1) return
  try {
    await basketApi.update(item.id, { ...item, quantity: newQty })
    item.quantity = newQty
  } catch (err) {
    error.value = 'Failed to update quantity'
    clearMsg()
  }
}

const removeItem = async (id) => {
  try {
    await basketApi.remove(id)
    items.value = items.value.filter(i => i.id !== id)
    success.value = 'Item removed'
    clearMsg()
  } catch (err) {
    error.value = 'Failed to remove item'
    clearMsg()
  }
}

const checkout = async () => {
  if (!user.value || items.value.length === 0) return
  try {
    await basketApi.checkout(user.value.id)
    success.value = 'Checkout successful! Order has been placed.'
    items.value = []
    clearMsg()
  } catch (err) {
    error.value = err.response?.data?.message || err.response?.data || 'Checkout failed'
    clearMsg()
  }
}

const clearMsg = () => {
  setTimeout(() => { success.value = ''; error.value = '' }, 4000)
}

onMounted(fetchBasket)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h1>Shopping Basket</h1>
      <p>Your cart items</p>
    </div>

    <div v-if="!user" class="alert alert-info">Please login to view your basket.</div>
    <div v-if="success" class="alert alert-success">{{ success }}</div>
    <div v-if="error" class="alert alert-error">{{ error }}</div>

    <div v-if="loading && user" class="loading">Loading basket...</div>

    <template v-else-if="user">
      <div v-if="items.length === 0" class="empty-state">
        <h3>Your basket is empty</h3>
        <p>Go to <router-link to="/products">Products</router-link> to add items.</p>
      </div>

      <div v-else>
        <div class="card">
          <table>
            <thead>
              <tr>
                <th>Product</th>
                <th>Price</th>
                <th>Quantity</th>
                <th>Subtotal</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in items" :key="item.id">
                <td><strong>{{ item.productName }}</strong></td>
                <td>${{ item.price?.toFixed(2) }}</td>
                <td>
                  <div class="flex flex-center gap-1">
                    <button class="btn btn-outline btn-sm" @click="updateQuantity(item, -1)">-</button>
                    <span>{{ item.quantity }}</span>
                    <button class="btn btn-outline btn-sm" @click="updateQuantity(item, 1)">+</button>
                  </div>
                </td>
                <td><strong>${{ (item.price * item.quantity).toFixed(2) }}</strong></td>
                <td>
                  <button class="btn btn-danger btn-sm" @click="removeItem(item.id)">Remove</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="card flex-between">
          <div>
            <span class="text-light">Total: </span>
            <strong style="font-size: 1.5rem; color: var(--primary);">${{ totalAmount.toFixed(2) }}</strong>
          </div>
          <button class="btn btn-success" @click="checkout" style="font-size: 1rem; padding: 0.75rem 2rem;">
            Checkout
          </button>
        </div>
      </div>
    </template>
  </div>
</template>
