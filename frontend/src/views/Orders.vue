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
  } catch (err) {
    error.value = 'Failed to load orders'
  } finally {
    loading.value = false
  }
}

const cancelOrder = async (orderId) => {
  if (!confirm('Cancel this order?')) return
  try {
    await orderApi.cancel(orderId)
    success.value = 'Order cancelled'
    fetchOrders()
    clearMsg()
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to cancel order'
    clearMsg()
  }
}

const badgeClass = (status) => {
  const s = status?.toLowerCase()
  return `badge badge-${s}`
}

const parseItems = (items) => {
  if (!items) return []
  if (typeof items === 'string') {
    try { return JSON.parse(items) } catch { return [] }
  }
  return items
}

const clearMsg = () => {
  setTimeout(() => { success.value = ''; error.value = '' }, 3000)
}

onMounted(fetchOrders)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h1>My Orders</h1>
      <p>Track your order history</p>
    </div>

    <div v-if="!user" class="alert alert-info">Please login to view your orders.</div>
    <div v-if="success" class="alert alert-success">{{ success }}</div>
    <div v-if="error" class="alert alert-error">{{ error }}</div>

    <div v-if="loading && user" class="loading">Loading orders...</div>

    <template v-else-if="user">
      <div v-if="orders.length === 0" class="empty-state">
        <h3>No orders yet</h3>
        <p>Complete a checkout to see your orders here.</p>
      </div>

      <div v-for="order in orders" :key="order.id || order.orderId" class="card mb-1">
        <div class="flex-between mb-1">
          <div>
            <strong>Order #{{ order.orderId }}</strong>
            <span class="text-sm text-light ml-1" style="margin-left: 0.5rem;">
              {{ order.createdAt }}
            </span>
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

        <div class="flex-between mt-1">
          <strong>Total: ${{ (order.totalAmount || 0).toFixed(2) }}</strong>
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
