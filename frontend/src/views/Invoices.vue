<script setup>
import { ref, onMounted, computed } from 'vue'
import { invoiceApi } from '../api'

const invoices = ref([])
const loading = ref(true)
const error = ref('')

const user = computed(() => JSON.parse(localStorage.getItem('user') || 'null'))

const fetchInvoices = async () => {
  if (!user.value) return
  loading.value = true
  try {
    const res = await invoiceApi.getByUser(user.value.id)
    invoices.value = res.data
  } catch (err) {
    error.value = 'Failed to load invoices'
  } finally {
    loading.value = false
  }
}

onMounted(fetchInvoices)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h1>Invoices</h1>
      <p>Your invoice history</p>
    </div>

    <div v-if="!user" class="alert alert-info">Please login to view your invoices.</div>
    <div v-if="error" class="alert alert-error">{{ error }}</div>

    <div v-if="loading && user" class="loading">Loading invoices...</div>

    <template v-else-if="user">
      <div v-if="invoices.length === 0" class="empty-state">
        <h3>No invoices yet</h3>
        <p>Invoices are generated automatically when you place an order.</p>
      </div>

      <div class="card" v-else>
        <table>
          <thead>
            <tr>
              <th>Invoice ID</th>
              <th>Order ID</th>
              <th>Amount</th>
              <th>Created At</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="inv in invoices" :key="inv.id">
              <td><strong>#{{ inv.id }}</strong></td>
              <td>{{ inv.orderId }}</td>
              <td style="color: var(--primary); font-weight: 600;">${{ (inv.totalAmount || 0).toFixed(2) }}</td>
              <td class="text-light">{{ inv.createdAt }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </div>
</template>
