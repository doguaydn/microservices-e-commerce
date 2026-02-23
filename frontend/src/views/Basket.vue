<script setup>
import { ref, onMounted, computed } from 'vue'
import { basketApi } from '../api'
import { useRouter } from 'vue-router'

const router = useRouter()
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
  } catch {
    error.value = 'Failed to load cart'
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
  } catch {
    error.value = 'Failed to update quantity'
    clearMsg()
  }
}

const removeItem = async (id) => {
  try {
    await basketApi.remove(id)
    items.value = items.value.filter(i => i.id !== id)
    window.dispatchEvent(new Event('cart-updated'))
    success.value = 'Item removed from cart'
    clearMsg()
  } catch {
    error.value = 'Failed to remove item'
    clearMsg()
  }
}

const checkout = async () => {
  if (!user.value || items.value.length === 0) return
  try {
    await basketApi.checkout(user.value.id)
    success.value = 'Order placed successfully! Check your email for confirmation.'
    items.value = []
    window.dispatchEvent(new Event('cart-updated'))
    clearMsg()
  } catch (err) {
    error.value = err.response?.data?.message || err.response?.data || 'Checkout failed'
    clearMsg()
  }
}

const clearMsg = () => setTimeout(() => { success.value = ''; error.value = '' }, 4000)

onMounted(fetchBasket)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h1>Shopping Cart</h1>
      <p v-if="items.length > 0">{{ items.length }} item(s) in your cart</p>
    </div>

    <div v-if="!user" class="alert alert-info">Please sign in to view your cart.</div>
    <div v-if="success" class="alert alert-success">{{ success }}</div>
    <div v-if="error" class="alert alert-error">{{ error }}</div>

    <div v-if="loading && user" class="loading">
      <div class="spinner"></div>
      <p>Loading cart...</p>
    </div>

    <template v-else-if="user">
      <div v-if="items.length === 0" class="empty-state">
        <div class="empty-icon">&#128722;</div>
        <h3>Your cart is empty</h3>
        <p>Discover amazing products and add them to your cart.</p>
        <router-link to="/products" class="btn btn-primary mt-2">Browse Products</router-link>
      </div>

      <div v-else class="grid-2" style="grid-template-columns: 1fr 340px; align-items: start;">
        <!-- Cart Items -->
        <div class="card" style="padding: 0; overflow: hidden;">
          <table>
            <thead>
              <tr>
                <th>Product</th>
                <th>Price</th>
                <th>Quantity</th>
                <th>Subtotal</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in items" :key="item.id">
                <td>
                  <strong>{{ item.productName }}</strong>
                </td>
                <td>${{ item.price?.toFixed(2) }}</td>
                <td>
                  <div class="qty-stepper">
                    <button @click="updateQuantity(item, -1)">-</button>
                    <span>{{ item.quantity }}</span>
                    <button @click="updateQuantity(item, 1)">+</button>
                  </div>
                </td>
                <td><strong>${{ (item.price * item.quantity).toFixed(2) }}</strong></td>
                <td>
                  <button class="btn btn-ghost" @click="removeItem(item.id)">&#128465;</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Order Summary -->
        <div class="card" style="position: sticky; top: 80px;">
          <h2 style="font-size: 1.125rem; font-weight: 700; margin-bottom: 1.25rem;">Order Summary</h2>
          <div class="flex-between mb-1">
            <span class="text-secondary">Subtotal</span>
            <strong>${{ totalAmount.toFixed(2) }}</strong>
          </div>
          <div class="flex-between mb-1">
            <span class="text-secondary">Shipping</span>
            <span style="color: var(--success); font-weight: 600;">Free</span>
          </div>
          <hr style="border: none; border-top: 1px solid var(--border); margin: 1rem 0;" />
          <div class="flex-between mb-2">
            <strong style="font-size: 1.1rem;">Total</strong>
            <strong style="font-size: 1.25rem; color: var(--accent);">${{ totalAmount.toFixed(2) }}</strong>
          </div>
          <button class="btn btn-success btn-block btn-lg" @click="checkout">
            Place Order
          </button>
        </div>
      </div>
    </template>
  </div>
</template>
