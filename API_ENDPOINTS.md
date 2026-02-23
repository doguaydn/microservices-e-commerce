# API Endpoints

> Gateway: `http://localhost:9090`

---

## Auth Service (Port 9092)

| Method | Gateway URL | Aciklama | Request Body |
|--------|------------|----------|-------------|
| POST | `/auth/users` | Kayit ol | `{ name, surname, email, password, phone }` |
| POST | `/auth/users/login` | Giris yap | `{ email, password }` |
| GET | `/auth/users` | Tum kullanicilar | - |
| GET | `/auth/users/{id}` | Kullanici getir | - |
| PUT | `/auth/users/{id}` | Kullanici guncelle | `{ name, surname, email, password, phone }` |
| DELETE | `/auth/users/{id}` | Kullanici sil | - |
| POST | `/auth/users/forgot-password` | Sifre sifirla | `{ email, newPassword }` |
| POST | `/auth/users/change-password` | Sifre degistir | `{ userId, oldPassword, newPassword }` |
| POST | `/auth/users/migrate-passwords` | Eski sifreleri hashle | - |
| GET | `/auth/admin/stats` | Admin: kullanici istatistikleri | - |

---

## Stock Service (Port 9093)

| Method | Gateway URL | Aciklama | Request Body |
|--------|------------|----------|-------------|
| POST | `/stock/products` | Urun ekle | `{ name, description, price, quantity }` |
| GET | `/stock/products` | Tum urunler | - |
| GET | `/stock/products/{id}` | Urun getir | - |
| PUT | `/stock/products/{id}` | Urun guncelle | `{ name, description, price, quantity }` |
| DELETE | `/stock/products/{id}` | Urun sil | - |
| PUT | `/stock/products/{id}/reduce-stock` | Stok azalt | `{ quantity }` |
| GET | `/stock/admin/stats` | Admin: urun istatistikleri | - |
| GET | `/stock/admin/low-stock?threshold=5` | Admin: dusuk stoklu urunler | - |

---

## Basket Service (Port 9091)

### Sepet

| Method | Gateway URL | Aciklama | Request Body |
|--------|------------|----------|-------------|
| POST | `/basket/basket-items` | Sepete ekle | `{ userId, productId, quantity }` |
| GET | `/basket/basket-items` | Tum sepet ogeleri | - |
| GET | `/basket/basket-items/{id}` | Sepet ogesi getir | - |
| GET | `/basket/basket-items/user/{userId}` | Kullanicinin sepeti | - |
| PUT | `/basket/basket-items/{id}` | Sepet ogesi guncelle | `{ userId, productId, quantity }` |
| DELETE | `/basket/basket-items/{id}` | Sepetten kaldir | - |
| POST | `/basket/basket-items/checkout/{userId}` | Siparis ver (stok duser, order olusur) | - |

### Siparisler

| Method | Gateway URL | Aciklama | Request Body |
|--------|------------|----------|-------------|
| GET | `/basket/orders` | Tum siparisler | - |
| GET | `/basket/orders/{orderId}` | Siparis detayi | - |
| GET | `/basket/orders/user/{userId}` | Kullanicinin siparisleri | - |
| PUT | `/basket/orders/{orderId}/status` | Siparis durumu guncelle | `{ status }` |
| DELETE | `/basket/orders/{orderId}/cancel` | Siparis iptal et | - |

### Favoriler (Wishlist)

| Method | Gateway URL | Aciklama | Request Body |
|--------|------------|----------|-------------|
| POST | `/basket/wishlist` | Favorilere ekle | `{ userId, productId }` |
| GET | `/basket/wishlist/user/{userId}` | Kullanicinin favorileri | - |
| DELETE | `/basket/wishlist/{id}` | Favoriden kaldir | - |

### Admin

| Method | Gateway URL | Aciklama | Request Body |
|--------|------------|----------|-------------|
| GET | `/basket/admin/stats` | Admin: siparis istatistikleri | - |
| GET | `/basket/admin/orders?status=PENDING` | Admin: duruma gore siparisler | - |

---

## Invoice Service (Port 9094)

| Method | Gateway URL | Aciklama | Request Body |
|--------|------------|----------|-------------|
| GET | `/invoice/invoices` | Tum faturalar | - |
| GET | `/invoice/invoices/{id}` | Fatura getir | - |
| GET | `/invoice/invoices/order/{orderId}` | Siparise gore fatura | - |
| GET | `/invoice/invoices/user/{userId}` | Kullanicinin faturalari | - |
| GET | `/invoice/admin/stats` | Admin: fatura istatistikleri | - |

---

## Notification Service (Port 9095)

REST endpoint yok. RabbitMQ uzerinden event dinler:

| Event | Queue | Aciklama |
|-------|-------|----------|
| user.registered | notification.user.registered.queue | Yeni kullanici bildirimi |
| order.created | notification.order.created.queue | Siparis bildirimi |
| invoice.created | notification.invoice.created.queue | Fatura bildirimi |

---

## Siparis Durumlari

```
PENDING → CONFIRMED → SHIPPED → DELIVERED
                                    ↓
                               CANCELLED
```

---

## Ornek Akis

```
1. POST /auth/users                        → Kayit ol
2. POST /auth/users/login                  → Token al
3. POST /stock/products                    → Urun ekle (admin)
4. POST /basket/wishlist                   → Favorilere ekle
5. POST /basket/basket-items               → Sepete ekle
6. POST /basket/basket-items/checkout/1    → Siparis ver
   → Order DB'de PENDING olarak kaydedilir
   → Urun stoklari duser
   → Invoice otomatik olusur (RabbitMQ)
   → Notification loglari yazilir (RabbitMQ)
7. GET  /basket/orders/user/1              → Siparisleri gor
8. PUT  /basket/orders/{orderId}/status    → Durumu guncelle
9. GET  /invoice/invoices/user/1           → Faturalari gor
10. GET /basket/admin/stats                → Admin istatistikleri
```

---

**Toplam: 37 endpoint (4 servis) + 3 RabbitMQ event**
