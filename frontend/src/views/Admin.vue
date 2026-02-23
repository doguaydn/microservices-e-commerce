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

    // Fetch low stock and recent orders
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
    alert('Failed to update status: ' + (err.response?.data?.message || err.message))
  }
}

const badgeClass = (status) => `badge badge-${status?.toLowerCase()}`

onMounted(fetchAll)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h1>Admin Panel</h1>
      <p>System overview and management</p>
    </div>

    <div v-if="loading" class="loading">Loading admin data...</div>

    <template v-else>
      <!-- Tab navigation -->
      <div class="flex gap-1 mb-2">
        <button
          :class="['btn btn-sm', activeTab === 'overview' ? 'btn-primary' : 'btn-outline']"
          @click="activeTab = 'overview'"
        >Overview</button>
        <button
          :class="['btn btn-sm', activeTab === 'orders' ? 'btn-primary' : 'btn-outline']"
          @click="activeTab = 'orders'"
        >Orders</button>
        <button
          :class="['btn btn-sm', activeTab === 'stock' ? 'btn-primary' : 'btn-outline']"
          @click="activeTab = 'stock'"
        >Low Stock</button>
      </div>

      <!-- Overview Tab -->
      <div v-if="activeTab === 'overview'">
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-value">{{ authStats.totalUsers || 0 }}</div>
            <div class="stat-label">Total Users</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ stockStats.totalProducts || 0 }}</div>
            <div class="stat-label">Total Products</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ orderStats.totalOrders || 0 }}</div>
            <div class="stat-label">Total Orders</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">${{ (orderStats.totalRevenue || 0).toFixed(2) }}</div>
            <div class="stat-label">Total Revenue</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ invoiceStats.totalInvoices || 0 }}</div>
            <div class="stat-label">Total Invoices</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ stockStats.lowStockCount || 0 }}</div>
            <div class="stat-label">Low Stock Items</div>
          </div>
        </div>

        <div class="grid-2">
          <div class="card">
            <h2 class="mb-1">Order Status Breakdown</h2>
            <table>
              <tbody>
                <tr>
                  <td><span class="badge badge-pending">PENDING</span></td>
                  <td><strong>{{ orderStats.pendingOrders || 0 }}</strong></td>
                </tr>
                <tr>
                  <td><span class="badge badge-confirmed">CONFIRMED</span></td>
                  <td><strong>{{ orderStats.confirmedOrders || 0 }}</strong></td>
                </tr>
                <tr>
                  <td><span class="badge badge-delivered">DELIVERED</span></td>
                  <td><strong>{{ orderStats.deliveredOrders || 0 }}</strong></td>
                </tr>
                <tr>
                  <td><span class="badge badge-cancelled">CANCELLED</span></td>
                  <td><strong>{{ orderStats.cancelledOrders || 0 }}</strong></td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="card">
            <h2 class="mb-1">Invoice Summary</h2>
            <table>
              <tbody>
                <tr>
                  <td class="text-light">Total Invoices</td>
                  <td><strong>{{ invoiceStats.totalInvoices || 0 }}</strong></td>
                </tr>
                <tr>
                  <td class="text-light">Total Amount</td>
                  <td><strong style="color: var(--primary);">${{ (invoiceStats.totalAmount || 0).toFixed(2) }}</strong></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- Orders Tab -->
      <div v-if="activeTab === 'orders'">
        <div class="card">
          <div class="card-header">
            <h2>Recent Orders</h2>
            <button class="btn btn-outline btn-sm" @click="fetchAll">Refresh</button>
          </div>
          <div v-if="recentOrders.length === 0" class="empty-state">
            <p>No orders found.</p>
          </div>
          <table v-else>
            <thead>
              <tr>
                <th>Order ID</th>
                <th>User ID</th>
                <th>Amount</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in recentOrders" :key="order.id || order.orderId">
                <td><strong>{{ order.orderId }}</strong></td>
                <td>{{ order.userId }}</td>
                <td>${{ (order.totalAmount || 0).toFixed(2) }}</td>
                <td><span :class="badgeClass(order.status)">{{ order.status }}</span></td>
                <td>
                  <div class="flex gap-1">
                    <select
                      class="btn-sm"
                      style="padding: 0.25rem 0.5rem; border: 1px solid var(--border); border-radius: var(--radius); font-size: 0.75rem;"
                      :value="order.status"
                      @change="updateOrderStatus(order.orderId, $event.target.value)"
                    >
                      <option value="PENDING">PENDING</option>
                      <option value="CONFIRMED">CONFIRMED</option>
                      <option value="SHIPPED">SHIPPED</option>
                      <option value="DELIVERED">DELIVERED</option>
                      <option value="CANCELLED">CANCELLED</option>
                    </select>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Low Stock Tab -->
      <div v-if="activeTab === 'stock'">
        <div class="card">
          <div class="card-header">
            <h2>Low Stock Products (threshold: 5)</h2>
          </div>
          <div v-if="lowStockProducts.length === 0" class="empty-state">
            <p>All products are well stocked!</p>
          </div>
          <table v-else>
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Price</th>
                <th>Stock</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in lowStockProducts" :key="p.id">
                <td>{{ p.id }}</td>
                <td><strong>{{ p.name }}</strong></td>
                <td>${{ (p.price || 0).toFixed(2) }}</td>
                <td>
                  <span style="color: var(--danger); font-weight: 600;">{{ p.quantity }}</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>
