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

// User management
const users = ref([])
const usersLoading = ref(false)

// Product management
const allProducts = ref([])
const productsLoading = ref(false)
const showAddProduct = ref(false)
const newProduct = ref({ name: '', description: '', price: 0, quantity: 0 })
const productSuccess = ref('')
const productError = ref('')

// Invoice management
const allInvoices = ref([])
const invoicesLoading = ref(false)

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

const fetchUsers = async () => {
  usersLoading.value = true
  try {
    const res = await authApi.getUsers()
    users.value = res.data
  } catch (err) {
    console.error('Failed to load users', err)
  } finally {
    usersLoading.value = false
  }
}

const deleteUser = async (id) => {
  if (!confirm('Delete this user?')) return
  try {
    await authApi.deleteUser(id)
    fetchUsers()
  } catch (err) {
    alert('Failed to delete user: ' + (err.response?.data?.message || err.message))
  }
}

const fetchProducts = async () => {
  productsLoading.value = true
  try {
    const res = await productApi.getAll()
    allProducts.value = res.data
  } catch (err) {
    console.error('Failed to load products', err)
  } finally {
    productsLoading.value = false
  }
}

const addProduct = async () => {
  productError.value = ''
  try {
    await productApi.create(newProduct.value)
    productSuccess.value = 'Product added successfully!'
    showAddProduct.value = false
    newProduct.value = { name: '', description: '', price: 0, quantity: 0 }
    fetchProducts()
    setTimeout(() => productSuccess.value = '', 3000)
  } catch (err) {
    productError.value = err.response?.data?.message || 'Failed to add product'
  }
}

const deleteProduct = async (id) => {
  if (!confirm('Delete this product?')) return
  try {
    await productApi.delete(id)
    fetchProducts()
  } catch (err) {
    alert('Failed to delete product: ' + (err.response?.data?.message || err.message))
  }
}

const updateProduct = async (product) => {
  try {
    await productApi.update(product.id, { name: product.name, description: product.description, price: product.price, quantity: product.quantity })
    productSuccess.value = 'Product updated!'
    setTimeout(() => productSuccess.value = '', 3000)
  } catch (err) {
    alert('Failed to update: ' + (err.response?.data?.message || err.message))
  }
}

const fetchInvoices = async () => {
  invoicesLoading.value = true
  try {
    const res = await invoiceApi.getAll()
    allInvoices.value = res.data
  } catch (err) {
    console.error('Failed to load invoices', err)
  } finally {
    invoicesLoading.value = false
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

const onTabChange = (tab) => {
  activeTab.value = tab
  if (tab === 'users' && users.value.length === 0) fetchUsers()
  if (tab === 'products' && allProducts.value.length === 0) fetchProducts()
  if (tab === 'invoices' && allInvoices.value.length === 0) fetchInvoices()
}

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
      <div class="flex gap-1 mb-3" style="flex-wrap: wrap;">
        <button
          :class="['btn btn-sm', activeTab === 'overview' ? 'btn-primary' : 'btn-outline']"
          @click="onTabChange('overview')"
        >Overview</button>
        <button
          :class="['btn btn-sm', activeTab === 'orders' ? 'btn-primary' : 'btn-outline']"
          @click="onTabChange('orders')"
        >Order Management</button>
        <button
          :class="['btn btn-sm', activeTab === 'stock' ? 'btn-primary' : 'btn-outline']"
          @click="onTabChange('stock')"
        >Low Stock Alerts</button>
        <button
          :class="['btn btn-sm', activeTab === 'users' ? 'btn-primary' : 'btn-outline']"
          @click="onTabChange('users')"
        >User Management</button>
        <button
          :class="['btn btn-sm', activeTab === 'products' ? 'btn-primary' : 'btn-outline']"
          @click="onTabChange('products')"
        >Product Management</button>
        <button
          :class="['btn btn-sm', activeTab === 'invoices' ? 'btn-primary' : 'btn-outline']"
          @click="onTabChange('invoices')"
        >Invoice Management</button>
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

      <!-- User Management -->
      <div v-if="activeTab === 'users'">
        <div class="card" style="padding: 0; overflow: hidden;">
          <div class="card-header" style="padding: 1.25rem;">
            <h2>User Management</h2>
            <button class="btn btn-outline btn-sm" @click="fetchUsers">Refresh</button>
          </div>
          <div v-if="usersLoading" class="loading" style="padding: 2rem;">
            <div class="spinner"></div>
          </div>
          <div v-else-if="users.length === 0" class="empty-state" style="padding: 2rem;">
            <p>No users found.</p>
          </div>
          <table v-else>
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>Role</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="u in users" :key="u.id">
                <td>{{ u.id }}</td>
                <td><strong>{{ u.name }} {{ u.surname }}</strong></td>
                <td>{{ u.email }}</td>
                <td><span :class="['badge', u.role === 'ADMIN' ? 'badge-confirmed' : 'badge-pending']">{{ u.role || 'USER' }}</span></td>
                <td>
                  <button
                    class="btn btn-outline btn-sm"
                    style="color: var(--danger); border-color: var(--danger);"
                    @click="deleteUser(u.id)"
                    :disabled="u.role === 'ADMIN'"
                  >Delete</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Product Management -->
      <div v-if="activeTab === 'products'">
        <div class="card" style="padding: 0; overflow: hidden;">
          <div class="card-header" style="padding: 1.25rem;">
            <h2>Product Management</h2>
            <div class="flex gap-1">
              <button class="btn btn-outline btn-sm" @click="fetchProducts">Refresh</button>
              <button class="btn btn-primary btn-sm" @click="showAddProduct = true">+ Add Product</button>
            </div>
          </div>

          <div v-if="productSuccess" class="alert alert-success" style="margin: 1rem;">{{ productSuccess }}</div>

          <div v-if="productsLoading" class="loading" style="padding: 2rem;">
            <div class="spinner"></div>
          </div>
          <div v-else-if="allProducts.length === 0" class="empty-state" style="padding: 2rem;">
            <p>No products yet. Add your first product!</p>
          </div>
          <table v-else>
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Description</th>
                <th>Price</th>
                <th>Stock</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in allProducts" :key="p.id">
                <td>{{ p.id }}</td>
                <td>
                  <input v-model="p.name" style="width: 120px; padding: 0.25rem 0.5rem; border: 1px solid var(--border); border-radius: var(--radius-sm); font-family: inherit; font-size: 0.85rem;" />
                </td>
                <td>
                  <input v-model="p.description" style="width: 150px; padding: 0.25rem 0.5rem; border: 1px solid var(--border); border-radius: var(--radius-sm); font-family: inherit; font-size: 0.85rem;" />
                </td>
                <td>
                  <input v-model.number="p.price" type="number" step="0.01" min="0" style="width: 80px; padding: 0.25rem 0.5rem; border: 1px solid var(--border); border-radius: var(--radius-sm); font-family: inherit; font-size: 0.85rem;" />
                </td>
                <td>
                  <input v-model.number="p.quantity" type="number" min="0" style="width: 60px; padding: 0.25rem 0.5rem; border: 1px solid var(--border); border-radius: var(--radius-sm); font-family: inherit; font-size: 0.85rem;" />
                </td>
                <td>
                  <div class="flex gap-1">
                    <button class="btn btn-accent btn-sm" @click="updateProduct(p)">Save</button>
                    <button
                      class="btn btn-outline btn-sm"
                      style="color: var(--danger); border-color: var(--danger);"
                      @click="deleteProduct(p.id)"
                    >Delete</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Add Product Modal -->
        <div v-if="showAddProduct" class="modal-overlay" @click.self="showAddProduct = false">
          <div class="modal">
            <h2>Add New Product</h2>
            <div v-if="productError" class="alert alert-error">{{ productError }}</div>
            <form @submit.prevent="addProduct">
              <div class="form-group">
                <label>Product Name</label>
                <input v-model="newProduct.name" placeholder="e.g. Wireless Headphones" required />
              </div>
              <div class="form-group">
                <label>Description</label>
                <textarea v-model="newProduct.description" rows="3" placeholder="Describe your product..."></textarea>
              </div>
              <div class="grid-2">
                <div class="form-group">
                  <label>Price ($)</label>
                  <input v-model.number="newProduct.price" type="number" step="0.01" min="0" required />
                </div>
                <div class="form-group">
                  <label>Stock Quantity</label>
                  <input v-model.number="newProduct.quantity" type="number" min="0" required />
                </div>
              </div>
              <div class="modal-actions">
                <button type="button" class="btn btn-outline" @click="showAddProduct = false">Cancel</button>
                <button type="submit" class="btn btn-primary">Add Product</button>
              </div>
            </form>
          </div>
        </div>
      </div>

      <!-- Invoice Management -->
      <div v-if="activeTab === 'invoices'">
        <div class="card" style="padding: 0; overflow: hidden;">
          <div class="card-header" style="padding: 1.25rem;">
            <h2>Invoice Management</h2>
            <button class="btn btn-outline btn-sm" @click="fetchInvoices">Refresh</button>
          </div>
          <div v-if="invoicesLoading" class="loading" style="padding: 2rem;">
            <div class="spinner"></div>
          </div>
          <div v-else-if="allInvoices.length === 0" class="empty-state" style="padding: 2rem;">
            <p>No invoices found.</p>
          </div>
          <table v-else>
            <thead>
              <tr>
                <th>Invoice Slug</th>
                <th>User ID</th>
                <th>Items</th>
                <th>Total</th>
                <th>Status</th>
                <th>Date</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="inv in allInvoices" :key="inv.id">
                <td><strong>{{ inv.invoiceSlug || 'N/A' }}</strong></td>
                <td>User #{{ inv.userId }}</td>
                <td style="max-width: 250px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ inv.items }}</td>
                <td><strong style="color: var(--accent);">${{ (inv.totalAmount || 0).toFixed(2) }}</strong></td>
                <td><span :class="badgeClass(inv.status)">{{ inv.status }}</span></td>
                <td>{{ inv.createdAt ? new Date(inv.createdAt).toLocaleDateString() : '-' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>
