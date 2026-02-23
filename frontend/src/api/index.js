import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

// Auth
export const authApi = {
  register: (data) => api.post('/auth/users', data),
  login: (data) => api.post('/auth/users/login', data),
  getUsers: () => api.get('/auth/users'),
  getUser: (id) => api.get(`/auth/users/${id}`),
  deleteUser: (id) => api.delete(`/auth/users/${id}`),
  forgotPassword: (data) => api.post('/auth/users/forgot-password', data),
  changePassword: (data) => api.post('/auth/users/change-password', data),
  adminStats: () => api.get('/auth/admin/stats'),
}

// Products
export const productApi = {
  getAll: () => api.get('/stock/products'),
  get: (id) => api.get(`/stock/products/${id}`),
  create: (data) => api.post('/stock/products', data),
  update: (id, data) => api.put(`/stock/products/${id}`, data),
  delete: (id) => api.delete(`/stock/products/${id}`),
  reduceStock: (id, quantity) => api.put(`/stock/products/${id}/reduce-stock`, { quantity }),
  adminStats: () => api.get('/stock/admin/stats'),
  lowStock: (threshold = 5) => api.get(`/stock/admin/low-stock?threshold=${threshold}`),
}

// Basket
export const basketApi = {
  getAll: () => api.get('/basket/basket-items'),
  getByUser: (userId) => api.get(`/basket/basket-items/user/${userId}`),
  add: (data) => api.post('/basket/basket-items', data),
  update: (id, data) => api.put(`/basket/basket-items/${id}`, data),
  remove: (id) => api.delete(`/basket/basket-items/${id}`),
  checkout: (userId) => api.post(`/basket/basket-items/checkout/${userId}`),
}

// Orders
export const orderApi = {
  getAll: () => api.get('/basket/orders'),
  get: (orderId) => api.get(`/basket/orders/${orderId}`),
  getByUser: (userId) => api.get(`/basket/orders/user/${userId}`),
  updateStatus: (orderId, status) => api.put(`/basket/orders/${orderId}/status`, { status }),
  cancel: (orderId) => api.delete(`/basket/orders/${orderId}/cancel`),
  adminStats: () => api.get('/basket/admin/stats'),
  adminOrders: (status) => api.get(`/basket/admin/orders${status ? '?status=' + status : ''}`),
}

// Wishlist
export const wishlistApi = {
  getByUser: (userId) => api.get(`/basket/wishlist/user/${userId}`),
  add: (data) => api.post('/basket/wishlist', data),
  remove: (id) => api.delete(`/basket/wishlist/${id}`),
}

// Invoice
export const invoiceApi = {
  getAll: () => api.get('/invoice/invoices'),
  get: (id) => api.get(`/invoice/invoices/${id}`),
  getByUser: (userId) => api.get(`/invoice/invoices/user/${userId}`),
  getByOrder: (orderId) => api.get(`/invoice/invoices/order/${orderId}`),
  adminStats: () => api.get('/invoice/admin/stats'),
}

export default api
