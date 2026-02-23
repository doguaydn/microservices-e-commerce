<script setup>
import { ref, onMounted } from 'vue'
import { authApi, productApi, orderApi, invoiceApi } from '../api'

const authStats = ref({})
const stockStats = ref({})
const orderStats = ref({})
const invoiceStats = ref({})
const lowStockProducts = ref([])
const recentOrders = ref([])
const loading = ref(true)
const activeTab = ref('overview')

const fetchAll = async () => {
  loading.value = true
  try {
    const [auth, stock, orders, invoice] = await Promise.allSettled([
      authApi.adminStats(),
      productApi.adminStats(),
      orderApi.adminStats(),
      invoiceApi.adminStats(),
    ])
    if (auth.status === 'fulfilled') authStats.value = auth.value.data
    if (stock.status === 'fulfilled') stockStats.value = stock.value.data
    if (orders.status === 'fulfilled') orderStats.value = orders.value.data
    if (invoice.status === 'fulfilled') invoiceStats.value = invoice.value.data

    const [lowStock, ordersAll] = await Promise.allSettled([
      productApi.lowStock(5),
      orderApi.adminOrders(),
    ])
    if (lowStock.status === 'fulfilled') lowStockProducts.value = lowStock.value.data
    if (ordersAll.status === 'fulfilled') recentOrders.value = ordersAll.value.data?.slice(0, 10) || []
  } catch (err) {
    console.error('Failed to load admin data', err)
  } finally {
    loading.value = false
  }
}

const updateOrderStatus = async (orderId, status) => {
  try {
    await orderApi.updateStatus(orderId, status)
    fetchAll()
  } catch (err) {
    alert('Failed to update: ' + (err.response?.data?.message || err.message))
  }
}

const badgeClass = (status) => `badge badge-${status?.toLowerCase()}`

onMounted(fetchAll)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h1>Admin Dashboard</h1>
      <p>System overview and management</p>
    </div>

    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>Loading dashboard...</p>
    </div>

    <template v-else>
      <div class="flex gap-1 mb-3">
        <button
          :class="['btn btn-sm', activeTab === 'overview' ? 'btn-primary' : 'btn-outline']"
          @click="activeTab = 'overview'"
        >Overview</button>
        <button
          :class="['btn btn-sm', activeTab === 'orders' ? 'btn-primary' : 'btn-outline']"
          @click="activeTab = 'orders'"
        >Order Management</button>
        <button
          :class="['btn btn-sm', activeTab === 'stock' ? 'btn-primary' : 'btn-outline']"
          @click="activeTab = 'stock'"
        >Low Stock Alerts</button>
      </div>

      <!-- Overview -->
      <div v-if="activeTab === 'overview'">
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-value">{{ authStats.totalUsers || 0 }}</div>
            <div class="stat-label">Total Users</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ stockStats.totalProducts || 0 }}</div>
            <div class="stat-label">Products</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ orderStats.totalOrders || 0 }}</div>
            <div class="stat-label">Orders</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">${{ (orderStats.totalRevenue || 0).toFixed(2) }}</div>
            <div class="stat-label">Revenue</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ invoiceStats.totalInvoices || 0 }}</div>
            <div class="stat-label">Invoices</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ stockStats.lowStockCount || 0 }}</div>
            <div class="stat-label">Low Stock</div>
          </div>
        </div>

        <div class="grid-2">
          <div class="card">
            <h2 style="font-weight: 700; margin-bottom: 1rem;">Order Status</h2>
            <table>
              <tbody>
                <tr>
                  <td><span class="badge badge-pending">Pending</span></td>
                  <td><strong>{{ orderStats.pendingOrders || 0 }}</strong></td>
                </tr>
                <tr>
                  <td><span class="badge badge-confirmed">Confirmed</span></td>
                  <td><strong>{{ orderStats.confirmedOrders || 0 }}</strong></td>
                </tr>
                <tr>
                  <td><span class="badge badge-delivered">Delivered</span></td>
                  <td><strong>{{ orderStats.deliveredOrders || 0 }}</strong></td>
                </tr>
                <tr>
                  <td><span class="badge badge-cancelled">Cancelled</span></td>
                  <td><strong>{{ orderStats.cancelledOrders || 0 }}</strong></td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="card">
            <h2 style="font-weight: 700; margin-bottom: 1rem;">Invoice Summary</h2>
            <table>
              <tbody>
                <tr>
                  <td class="text-secondary">Total Invoices</td>
                  <td><strong>{{ invoiceStats.totalInvoices || 0 }}</strong></td>
                </tr>
                <tr>
                  <td class="text-secondary">Total Revenue</td>
                  <td><strong style="color: var(--accent);">${{ (invoiceStats.totalAmount || 0).toFixed(2) }}</strong></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- Orders -->
      <div v-if="activeTab === 'orders'">
        <div class="card" style="padding: 0; overflow: hidden;">
          <div class="card-header" style="padding: 1.25rem;">
            <h2>Recent Orders</h2>
            <button class="btn btn-outline btn-sm" @click="fetchAll">Refresh</button>
          </div>
          <div v-if="recentOrders.length === 0" class="empty-state" style="padding: 2rem;">
            <p>No orders found.</p>
          </div>
          <table v-else>
            <thead>
              <tr>
                <th>Order ID</th>
                <th>User</th>
                <th>Amount</th>
                <th>Status</th>
                <th>Update</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in recentOrders" :key="order.id || order.orderId">
                <td><strong>{{ order.orderId?.substring(0, 8) }}...</strong></td>
                <td>User #{{ order.userId }}</td>
                <td><strong>${{ (order.totalAmount || 0).toFixed(2) }}</strong></td>
                <td><span :class="badgeClass(order.status)">{{ order.status }}</span></td>
                <td>
                  <select
                    style="padding: 0.35rem 0.5rem; border: 1px solid var(--border); border-radius: var(--radius-sm); font-size: 0.75rem; font-family: inherit;"
                    :value="order.status"
                    @change="updateOrderStatus(order.orderId, $event.target.value)"
                  >
                    <option value="PENDING">PENDING</option>
                    <option value="CONFIRMED">CONFIRMED</option>
                    <option value="SHIPPED">SHIPPED</option>
                    <option value="DELIVERED">DELIVERED</option>
                    <option value="CANCELLED">CANCELLED</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Low Stock -->
      <div v-if="activeTab === 'stock'">
        <div class="card" style="padding: 0; overflow: hidden;">
          <div class="card-header" style="padding: 1.25rem;">
            <h2>Low Stock Products (threshold: 5)</h2>
          </div>
          <div v-if="lowStockProducts.length === 0" class="empty-state" style="padding: 2rem;">
            <p>All products are well stocked!</p>
          </div>
          <table v-else>
            <thead>
              <tr>
                <th>ID</th>
                <th>Product</th>
                <th>Price</th>
                <th>Stock</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in lowStockProducts" :key="p.id">
                <td>{{ p.id }}</td>
                <td><strong>{{ p.name }}</strong></td>
                <td>${{ (p.price || 0).toFixed(2) }}</td>
                <td><span style="color: var(--danger); font-weight: 700;">{{ p.quantity }}</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>
