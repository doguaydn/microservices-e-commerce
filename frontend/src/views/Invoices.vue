<script setup>
import { ref, onMounted, computed } from 'vue'
import { invoiceApi, authApi } from '../api'

const invoices = ref([])
const loading = ref(true)
const error = ref('')
const userMap = ref({})

const user = computed(() => JSON.parse(localStorage.getItem('user') || 'null'))
const isAdmin = computed(() => user.value?.role === 'ADMIN')

const fetchInvoices = async () => {
  if (!user.value) return
  loading.value = true
  try {
    const res = isAdmin.value
      ? await invoiceApi.getAll()
      : await invoiceApi.getByUser(user.value.id)
    invoices.value = res.data
    if (isAdmin.value) {
      const usersRes = await authApi.getUsers()
      const map = {}
      for (const u of usersRes.data || []) {
        map[u.id] = u
      }
      userMap.value = map
    }
  } catch {
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
      <p>{{ isAdmin ? 'All invoices' : 'Your billing history' }}</p>
    </div>

    <div v-if="!user" class="alert alert-info">Please sign in to view your invoices.</div>
    <div v-if="error" class="alert alert-error">{{ error }}</div>

    <div v-if="loading && user" class="loading">
      <div class="spinner"></div>
      <p>Loading invoices...</p>
    </div>

    <template v-else-if="user">
      <div v-if="invoices.length === 0" class="empty-state">
        <div class="empty-icon">&#128196;</div>
        <h3>No invoices yet</h3>
        <p>Invoices are generated automatically when you place an order.</p>
      </div>

      <div class="card" v-else style="padding: 0; overflow: hidden;">
        <table>
          <thead>
            <tr>
              <th>Invoice</th>
              <th v-if="isAdmin">User</th>
              <th>Amount</th>
              <th>Status</th>
              <th>Date</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="inv in invoices" :key="inv.id">
              <td><strong>{{ inv.invoiceSlug || '#' + inv.id }}</strong></td>
              <td v-if="isAdmin">
                <template v-if="userMap[inv.userId]">
                  <strong>{{ userMap[inv.userId].name }} {{ userMap[inv.userId].surname }}</strong><br>
                  <span class="text-muted" style="font-size: 0.85rem;">{{ userMap[inv.userId].email }}</span>
                </template>
                <template v-else>User #{{ inv.userId }}</template>
              </td>
              <td style="color: var(--accent); font-weight: 700;">${{ (inv.totalAmount || 0).toFixed(2) }}</td>
              <td><span class="badge badge-created">{{ inv.status || 'CREATED' }}</span></td>
              <td class="text-muted">{{ inv.createdAt ? new Date(inv.createdAt).toLocaleDateString() : '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </div>
</template>
