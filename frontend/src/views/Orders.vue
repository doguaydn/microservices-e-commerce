<script setup>
import { ref, onMounted, computed } from 'vue'
import { orderApi } from '../api'

const orders = ref([])
const loading = ref(true)
const error = ref('')
const success = ref('')

const user = computed(() => JSON.parse(localStorage.getItem('user') || 'null'))

const fetchOrders = async () => {
  if (!user.value) return
  loading.value = true
  try {
    const res = await orderApi.getByUser(user.value.id)
    orders.value = res.data
  } catch {
    error.value = 'Failed to load orders'
  } finally {
    loading.value = false
  }
}

const cancelOrder = async (orderId) => {
  if (!confirm('Are you sure you want to cancel this order?')) return
  try {
    await orderApi.cancel(orderId)
    success.value = 'Order cancelled successfully'
    fetchOrders()
    clearMsg()
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to cancel order'
    clearMsg()
  }
}

const badgeClass = (status) => `badge badge-${status?.toLowerCase()}`

const parseItems = (items) => {
  if (!items) return []
  if (typeof items === 'string') {
    try { return JSON.parse(items) } catch { return [] }
  }
  return items
}

const clearMsg = () => setTimeout(() => { success.value = ''; error.value = '' }, 3000)

onMounted(fetchOrders)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h1>My Orders</h1>
      <p>Track and manage your orders</p>
    </div>

    <div v-if="!user" class="alert alert-info">Please sign in to view your orders.</div>
    <div v-if="success" class="alert alert-success">{{ success }}</div>
    <div v-if="error" class="alert alert-error">{{ error }}</div>

    <div v-if="loading && user" class="loading">
      <div class="spinner"></div>
      <p>Loading orders...</p>
    </div>

    <template v-else-if="user">
      <div v-if="orders.length === 0" class="empty-state">
        <div class="empty-icon">&#128230;</div>
        <h3>No orders yet</h3>
        <p>Your order history will appear here after your first purchase.</p>
        <router-link to="/products" class="btn btn-primary mt-2">Start Shopping</router-link>
      </div>

      <div v-for="(order, idx) in orders" :key="order.id || order.orderId" class="order-card">
        <div class="order-header">
          <div>
            <span class="order-id">Order #{{ orders.length - idx }}</span>
            <span class="order-date" style="margin-left: 1rem;">{{ order.createdAt }}</span>
          </div>
          <span :class="badgeClass(order.status)">{{ order.status }}</span>
        </div>

        <table>
          <thead>
            <tr>
              <th>Product</th>
              <th>Qty</th>
              <th>Price</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, idx) in parseItems(order.items)" :key="idx">
              <td>{{ item.productName || item.product_name || '-' }}</td>
              <td>{{ item.quantity || item.qty || '-' }}</td>
              <td>${{ (item.price || 0).toFixed(2) }}</td>
            </tr>
          </tbody>
        </table>

        <div class="flex-between mt-2">
          <div>
            <span class="text-secondary">Total: </span>
            <strong style="font-size: 1.1rem; color: var(--accent);">${{ (order.totalAmount || 0).toFixed(2) }}</strong>
          </div>
          <button
            v-if="order.status === 'PENDING'"
            class="btn btn-danger btn-sm"
            @click="cancelOrder(order.orderId)"
          >
            Cancel Order
          </button>
        </div>
      </div>
    </template>
  </div>
</template>
