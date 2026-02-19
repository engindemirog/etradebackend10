# E-Trade Backend API Dokümantasyonu

## Genel Bilgiler

| Bilgi | Değer |
|-------|-------|
| **Base URL** | `http://localhost:8080` |
| **API Prefix** | `/api` |
| **Content-Type** | `application/json` |
| **Veritabanı** | H2 (file-based) |
| **Swagger UI** | `http://localhost:8080/swagger-ui.html` |
| **H2 Console** | `http://localhost:8080/h2-console` |

---

## Veri Modeli (Entity İlişkileri)

```
┌──────────────────┐        ┌──────────────────┐
│    categories     │        │     products     │
├──────────────────┤        ├──────────────────┤
│ id (PK)          │───┐    │ id (PK)          │
│ name             │   │    │ name             │
│ description      │   │    │ description      │
│ created_date     │   └───>│ category_id (FK) │
│ updated_date     │        │ unit_price       │
│ deleted_date     │        │ units_in_stock   │
│ is_active        │        │ image_url        │
└──────────────────┘        │ created_date     │
                            │ updated_date     │
                            │ deleted_date     │
                            │ is_active        │
                            └──────────────────┘
```

**İlişki:** Bir kategori birden fazla ürüne sahip olabilir (One-to-Many).

---

## Tarih/Saat Formatı

Tüm tarih alanları `LocalDateTime` formatında döner:

```
"2026-02-19T14:30:00"
```

Format: `YYYY-MM-DDTHH:mm:ss`

---

## Hata Yönetimi (Global Exception Handler)

Sistem `@RestControllerAdvice` ile global hata yönetimi kullanır. Tüm iş kuralı ve validasyon hataları yapılandırılmış JSON formatında `400 Bad Request` olarak döner.

### İş Kuralı Hataları (400 Bad Request) — `BusinessErrorResponse`

İş kuralı ihlallerinde `BusinessException` fırlatılır ve aşağıdaki formatta yakalanır:

```json
{
  "status": 400,
  "message": "Bu ürün adı zaten mevcut: Laptop",
  "timestamp": "2026-02-19T14:30:00"
}
```

**TypeScript Interface:**
```typescript
interface BusinessErrorResponse {
  status: number;
  message: string;
  timestamp: string;
}
```

### Validasyon Hataları (400 Bad Request) — `ValidationErrorResponse`

Request body'deki alanlar Jakarta Validation kurallarını sağlamadığında döner. Her alan için ayrı hata mesajı içerir.

```json
{
  "status": 400,
  "message": "Validasyon hatası",
  "errors": {
    "name": "Ürün adı boş olamaz.",
    "unitPrice": "Birim fiyat 0'dan küçük olamaz."
  },
  "timestamp": "2026-02-19T14:30:00"
}
```

**TypeScript Interface:**
```typescript
interface ValidationErrorResponse {
  status: number;
  message: string;
  errors: Record<string, string>;  // { alanAdı: hataMesajı }
  timestamp: string;
}
```

### Frontend'de Hata Yakalama Örneği (Axios)

```typescript
import axios, { AxiosError } from "axios";

// İş kuralı veya validasyon hatasını ayırt etme
interface BusinessErrorResponse {
  status: number;
  message: string;
  timestamp: string;
}

interface ValidationErrorResponse {
  status: number;
  message: string;
  errors: Record<string, string>;
  timestamp: string;
}

type ApiErrorResponse = BusinessErrorResponse | ValidationErrorResponse;

function isValidationError(error: ApiErrorResponse): error is ValidationErrorResponse {
  return "errors" in error;
}

// Kullanım
try {
  await productService.add(request);
} catch (error) {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<ApiErrorResponse>;
    const data = axiosError.response?.data;

    if (data && isValidationError(data)) {
      // Validasyon hatası — alan bazlı hataları göster
      console.log(data.errors); // { name: "Ürün adı boş olamaz.", ... }
    } else if (data) {
      // İş kuralı hatası — genel mesajı göster
      console.log(data.message); // "Bu ürün adı zaten mevcut: Laptop"
    }
  }
}
```

---

# 1. KATEGORİ (Category) API

**Base Path:** `/api/categories`

## 1.1 Tüm Kategorileri Listele

```
GET /api/categories
```

**Açıklama:** Sistemdeki tüm kategorileri listeler.

**Parametreler:** Yok

**Response Status:** `200 OK`

**Response Body:** `GetAllCategoriesResponse[]`

| Alan | Tip | Açıklama |
|------|-----|----------|
| `id` | `number` | Kategori ID |
| `name` | `string` | Kategori adı |

**Örnek Response:**
```json
[
  {
    "id": 1,
    "name": "Elektronik"
  },
  {
    "id": 2,
    "name": "Giyim"
  },
  {
    "id": 3,
    "name": "Kitap"
  }
]
```

**Boş liste durumunda:**
```json
[]
```

---

## 1.2 ID ile Kategori Getir

```
GET /api/categories/{id}
```

**Açıklama:** Belirtilen ID'ye sahip kategorinin detaylarını getirir.

**Path Parametreleri:**

| Parametre | Tip | Zorunlu | Açıklama |
|-----------|-----|---------|----------|
| `id` | `number` | Evet | Kategori ID |

**Response Status:** `200 OK`

**Response Body:** `GetByIdCategoryResponse`

| Alan | Tip | Açıklama |
|------|-----|----------|
| `id` | `number` | Kategori ID |
| `name` | `string` | Kategori adı |
| `description` | `string` | Kategori açıklaması |
| `createdDate` | `string (datetime)` | Oluşturulma tarihi |
| `updatedDate` | `string (datetime) \| null` | Güncellenme tarihi |
| `isActive` | `boolean` | Aktiflik durumu |

**Örnek Response:**
```json
{
  "id": 1,
  "name": "Elektronik",
  "description": "Elektronik ürünler kategorisi",
  "createdDate": "2026-02-19T10:00:00",
  "updatedDate": "2026-02-19T12:30:00",
  "isActive": true
}
```

**Hata Durumları:**

| Durum | Mesaj |
|-------|-------|
| Kategori bulunamadı | `"Kategori bulunamadı. ID: {id}"` |

---

## 1.3 Yeni Kategori Ekle

```
POST /api/categories
```

**Açıklama:** Sisteme yeni bir kategori ekler.

**Request Body:** `CreateCategoryRequest`

| Alan | Tip | Zorunlu | Validasyon | Açıklama |
|------|-----|---------|------------|----------|
| `name` | `string` | Evet | Min: 2, Max: 100, Boş olamaz | Kategori adı |
| `description` | `string` | Hayır | Max: 500 | Kategori açıklaması |

**Örnek Request:**
```json
{
  "name": "Elektronik",
  "description": "Elektronik ürünler kategorisi"
}
```

**Response Status:** `201 Created`

**Response Body:** `CreatedCategoryResponse`

| Alan | Tip | Açıklama |
|------|-----|----------|
| `id` | `number` | Oluşturulan kategori ID |
| `name` | `string` | Kategori adı |
| `description` | `string` | Kategori açıklaması |
| `createdDate` | `string (datetime)` | Oluşturulma tarihi |

**Örnek Response:**
```json
{
  "id": 1,
  "name": "Elektronik",
  "description": "Elektronik ürünler kategorisi",
  "createdDate": "2026-02-19T14:30:00"
}
```

**Validasyon Hataları:**

| Alan | Kural | Hata Mesajı |
|------|-------|-------------|
| `name` | Boş olamaz | `"Kategori adı boş olamaz."` |
| `name` | 2-100 karakter | `"Kategori adı 2 ile 100 karakter arasında olmalıdır."` |
| `description` | Max 500 karakter | `"Açıklama en fazla 500 karakter olabilir."` |

**İş Kuralı Hataları:**

| Durum | Mesaj |
|-------|-------|
| Aynı isimde kategori var | `"Bu kategori adı zaten mevcut: {name}"` |

---

## 1.4 Kategori Güncelle

```
PUT /api/categories
```

**Açıklama:** Mevcut bir kategoriyi günceller.

**Request Body:** `UpdateCategoryRequest`

| Alan | Tip | Zorunlu | Validasyon | Açıklama |
|------|-----|---------|------------|----------|
| `id` | `number` | Evet | Boş olamaz | Güncellenecek kategori ID |
| `name` | `string` | Evet | Min: 2, Max: 100, Boş olamaz | Yeni kategori adı |
| `description` | `string` | Hayır | Max: 500 | Yeni kategori açıklaması |

**Örnek Request:**
```json
{
  "id": 1,
  "name": "Elektronik Cihazlar",
  "description": "Elektronik cihazlar ve aksesuarları"
}
```

**Response Status:** `200 OK`

**Response Body:** `UpdatedCategoryResponse`

| Alan | Tip | Açıklama |
|------|-----|----------|
| `id` | `number` | Kategori ID |
| `name` | `string` | Güncellenmiş kategori adı |
| `description` | `string` | Güncellenmiş açıklama |
| `updatedDate` | `string (datetime)` | Güncellenme tarihi |

**Örnek Response:**
```json
{
  "id": 1,
  "name": "Elektronik Cihazlar",
  "description": "Elektronik cihazlar ve aksesuarları",
  "updatedDate": "2026-02-19T15:00:00"
}
```

**Validasyon Hataları:**

| Alan | Kural | Hata Mesajı |
|------|-------|-------------|
| `id` | Boş olamaz | `"Kategori ID boş olamaz."` |
| `name` | Boş olamaz | `"Kategori adı boş olamaz."` |
| `name` | 2-100 karakter | `"Kategori adı 2 ile 100 karakter arasında olmalıdır."` |
| `description` | Max 500 karakter | `"Açıklama en fazla 500 karakter olabilir."` |

**İş Kuralı Hataları:**

| Durum | Mesaj |
|-------|-------|
| Kategori bulunamadı | `"Kategori bulunamadı. ID: {id}"` |
| Aynı isimde başka kategori var | `"Bu kategori adı zaten başka bir kategoriye ait: {name}"` |

---

## 1.5 Kategori Sil

```
DELETE /api/categories/{id}
```

**Açıklama:** Belirtilen ID'ye sahip kategoriyi siler (soft delete).

**Path Parametreleri:**

| Parametre | Tip | Zorunlu | Açıklama |
|-----------|-----|---------|----------|
| `id` | `number` | Evet | Silinecek kategori ID |

**Response Status:** `200 OK`

**Response Body:** `DeletedCategoryResponse`

| Alan | Tip | Açıklama |
|------|-----|----------|
| `id` | `number` | Silinen kategori ID |
| `name` | `string` | Silinen kategori adı |

**Örnek Response:**
```json
{
  "id": 1,
  "name": "Elektronik"
}
```

**Hata Durumları:**

| Durum | Mesaj |
|-------|-------|
| Kategori bulunamadı | `"Kategori bulunamadı. ID: {id}"` |

---

# 2. ÜRÜN (Product) API

**Base Path:** `/api/products`

## 2.1 Tüm Ürünleri Listele

```
GET /api/products
```

**Açıklama:** Sistemdeki tüm ürünleri listeler.

**Parametreler:** Yok

**Response Status:** `200 OK`

**Response Body:** `GetAllProductsResponse[]`

| Alan | Tip | Açıklama |
|------|-----|----------|
| `id` | `number` | Ürün ID |
| `name` | `string` | Ürün adı |
| `unitPrice` | `number` | Birim fiyat |
| `unitsInStock` | `number` | Stok adedi |
| `imageUrl` | `string \| null` | Ürün görseli URL |
| `categoryId` | `number` | Bağlı kategori ID |
| `categoryName` | `string` | Bağlı kategori adı |

**Örnek Response:**
```json
[
  {
    "id": 1,
    "name": "iPhone 15",
    "unitPrice": 64999.99,
    "unitsInStock": 50,
    "imageUrl": "https://example.com/iphone15.jpg",
    "categoryId": 1,
    "categoryName": "Elektronik"
  },
  {
    "id": 2,
    "name": "Samsung Galaxy S24",
    "unitPrice": 49999.99,
    "unitsInStock": 30,
    "imageUrl": "https://example.com/galaxy-s24.jpg",
    "categoryId": 1,
    "categoryName": "Elektronik"
  }
]
```

**Boş liste durumunda:**
```json
[]
```

---

## 2.2 ID ile Ürün Getir

```
GET /api/products/{id}
```

**Açıklama:** Belirtilen ID'ye sahip ürünün tüm detaylarını getirir.

**Path Parametreleri:**

| Parametre | Tip | Zorunlu | Açıklama |
|-----------|-----|---------|----------|
| `id` | `number` | Evet | Ürün ID |

**Response Status:** `200 OK`

**Response Body:** `GetByIdProductResponse`

| Alan | Tip | Açıklama |
|------|-----|----------|
| `id` | `number` | Ürün ID |
| `name` | `string` | Ürün adı |
| `description` | `string` | Ürün açıklaması |
| `unitPrice` | `number` | Birim fiyat |
| `unitsInStock` | `number` | Stok adedi |
| `imageUrl` | `string \| null` | Ürün görseli URL |
| `categoryId` | `number` | Bağlı kategori ID |
| `categoryName` | `string` | Bağlı kategori adı |
| `createdDate` | `string (datetime)` | Oluşturulma tarihi |
| `updatedDate` | `string (datetime) \| null` | Güncellenme tarihi |
| `isActive` | `boolean` | Aktiflik durumu |

**Örnek Response:**
```json
{
  "id": 1,
  "name": "iPhone 15",
  "description": "Apple iPhone 15 128GB",
  "unitPrice": 64999.99,
  "unitsInStock": 50,
  "imageUrl": "https://example.com/iphone15.jpg",
  "categoryId": 1,
  "categoryName": "Elektronik",
  "createdDate": "2026-02-19T10:00:00",
  "updatedDate": "2026-02-19T12:30:00",
  "isActive": true
}
```

**Hata Durumları:**

| Durum | Mesaj |
|-------|-------|
| Ürün bulunamadı | `"Ürün bulunamadı. ID: {id}"` |

---

## 2.3 Yeni Ürün Ekle

```
POST /api/products
```

**Açıklama:** Sisteme yeni bir ürün ekler.

**Request Body:** `CreateProductRequest`

| Alan | Tip | Zorunlu | Validasyon | Açıklama |
|------|-----|---------|------------|----------|
| `name` | `string` | Evet | Min: 2, Max: 100, Boş olamaz | Ürün adı |
| `description` | `string` | Hayır | Max: 500 | Ürün açıklaması |
| `unitPrice` | `number` | Evet | Min: 0, Boş olamaz | Birim fiyat |
| `unitsInStock` | `number` | Hayır | Min: 0 | Stok adedi (varsayılan: 0) |
| `imageUrl` | `string` | Hayır | — | Ürün görseli URL |
| `categoryId` | `number` | Evet | Boş olamaz | Bağlanacak kategori ID |

**Örnek Request:**
```json
{
  "name": "iPhone 15",
  "description": "Apple iPhone 15 128GB",
  "unitPrice": 64999.99,
  "unitsInStock": 50,
  "imageUrl": "https://example.com/iphone15.jpg",
  "categoryId": 1
}
```

**Minimum zorunlu alanlarla örnek:**
```json
{
  "name": "iPhone 15",
  "unitPrice": 64999.99,
  "categoryId": 1
}
```

**Response Status:** `201 Created`

**Response Body:** `CreatedProductResponse`

| Alan | Tip | Açıklama |
|------|-----|----------|
| `id` | `number` | Oluşturulan ürün ID |
| `name` | `string` | Ürün adı |
| `description` | `string` | Ürün açıklaması |
| `unitPrice` | `number` | Birim fiyat |
| `unitsInStock` | `number` | Stok adedi |
| `imageUrl` | `string \| null` | Ürün görseli URL |
| `categoryId` | `number` | Bağlı kategori ID |
| `categoryName` | `string` | Bağlı kategori adı |
| `createdDate` | `string (datetime)` | Oluşturulma tarihi |

**Örnek Response:**
```json
{
  "id": 1,
  "name": "iPhone 15",
  "description": "Apple iPhone 15 128GB",
  "unitPrice": 64999.99,
  "unitsInStock": 50,
  "imageUrl": "https://example.com/iphone15.jpg",
  "categoryId": 1,
  "categoryName": "Elektronik",
  "createdDate": "2026-02-19T14:30:00"
}
```

**Validasyon Hataları:**

| Alan | Kural | Hata Mesajı |
|------|-------|-------------|
| `name` | Boş olamaz | `"Ürün adı boş olamaz."` |
| `name` | 2-100 karakter | `"Ürün adı 2 ile 100 karakter arasında olmalıdır."` |
| `description` | Max 500 karakter | `"Açıklama en fazla 500 karakter olabilir."` |
| `unitPrice` | Boş olamaz | `"Birim fiyat boş olamaz."` |
| `unitPrice` | Min 0 | `"Birim fiyat 0'dan küçük olamaz."` |
| `unitsInStock` | Min 0 | `"Stok adedi 0'dan küçük olamaz."` |
| `categoryId` | Boş olamaz | `"Kategori ID boş olamaz."` |

**İş Kuralı Hataları:**

| Durum | Mesaj |
|-------|-------|
| Aynı isimde ürün var | `"Bu ürün adı zaten mevcut: {name}"` |
| Birim fiyat geçersiz | `"Birim fiyat 0'dan küçük olamaz."` |

---

## 2.4 Ürün Güncelle

```
PUT /api/products
```

**Açıklama:** Mevcut bir ürünü günceller.

**Request Body:** `UpdateProductRequest`

| Alan | Tip | Zorunlu | Validasyon | Açıklama |
|------|-----|---------|------------|----------|
| `id` | `number` | Evet | Boş olamaz | Güncellenecek ürün ID |
| `name` | `string` | Evet | Min: 2, Max: 100, Boş olamaz | Yeni ürün adı |
| `description` | `string` | Hayır | Max: 500 | Yeni ürün açıklaması |
| `unitPrice` | `number` | Evet | Min: 0, Boş olamaz | Yeni birim fiyat |
| `unitsInStock` | `number` | Hayır | Min: 0 | Yeni stok adedi |
| `imageUrl` | `string` | Hayır | — | Yeni ürün görseli URL |
| `categoryId` | `number` | Evet | Boş olamaz | Yeni kategori ID |

**Örnek Request:**
```json
{
  "id": 1,
  "name": "iPhone 15 Pro",
  "description": "Apple iPhone 15 Pro 256GB",
  "unitPrice": 79999.99,
  "unitsInStock": 25,
  "imageUrl": "https://example.com/iphone15pro.jpg",
  "categoryId": 1
}
```

**Response Status:** `200 OK`

**Response Body:** `UpdatedProductResponse`

| Alan | Tip | Açıklama |
|------|-----|----------|
| `id` | `number` | Ürün ID |
| `name` | `string` | Güncellenmiş ürün adı |
| `description` | `string` | Güncellenmiş açıklama |
| `unitPrice` | `number` | Güncellenmiş birim fiyat |
| `unitsInStock` | `number` | Güncellenmiş stok adedi |
| `imageUrl` | `string \| null` | Güncellenmiş görsel URL |
| `categoryId` | `number` | Güncellenmiş kategori ID |
| `categoryName` | `string` | Güncellenmiş kategori adı |
| `updatedDate` | `string (datetime)` | Güncellenme tarihi |

**Örnek Response:**
```json
{
  "id": 1,
  "name": "iPhone 15 Pro",
  "description": "Apple iPhone 15 Pro 256GB",
  "unitPrice": 79999.99,
  "unitsInStock": 25,
  "imageUrl": "https://example.com/iphone15pro.jpg",
  "categoryId": 1,
  "categoryName": "Elektronik",
  "updatedDate": "2026-02-19T15:00:00"
}
```

**Validasyon Hataları:**

| Alan | Kural | Hata Mesajı |
|------|-------|-------------|
| `id` | Boş olamaz | `"Ürün ID boş olamaz."` |
| `name` | Boş olamaz | `"Ürün adı boş olamaz."` |
| `name` | 2-100 karakter | `"Ürün adı 2 ile 100 karakter arasında olmalıdır."` |
| `description` | Max 500 karakter | `"Açıklama en fazla 500 karakter olabilir."` |
| `unitPrice` | Boş olamaz | `"Birim fiyat boş olamaz."` |
| `unitPrice` | Min 0 | `"Birim fiyat 0'dan küçük olamaz."` |
| `unitsInStock` | Min 0 | `"Stok adedi 0'dan küçük olamaz."` |
| `categoryId` | Boş olamaz | `"Kategori ID boş olamaz."` |

**İş Kuralı Hataları:**

| Durum | Mesaj |
|-------|-------|
| Ürün bulunamadı | `"Ürün bulunamadı. ID: {id}"` |
| Aynı isimde başka ürün var | `"Bu ürün adı zaten başka bir ürüne ait: {name}"` |
| Birim fiyat geçersiz | `"Birim fiyat 0'dan küçük olamaz."` |

---

## 2.5 Ürün Sil

```
DELETE /api/products/{id}
```

**Açıklama:** Belirtilen ID'ye sahip ürünü siler (soft delete).

**Path Parametreleri:**

| Parametre | Tip | Zorunlu | Açıklama |
|-----------|-----|---------|----------|
| `id` | `number` | Evet | Silinecek ürün ID |

**Response Status:** `200 OK`

**Response Body:** `DeletedProductResponse`

| Alan | Tip | Açıklama |
|------|-----|----------|
| `id` | `number` | Silinen ürün ID |
| `name` | `string` | Silinen ürün adı |

**Örnek Response:**
```json
{
  "id": 1,
  "name": "iPhone 15"
}
```

**Hata Durumları:**

| Durum | Mesaj |
|-------|-------|
| Ürün bulunamadı | `"Ürün bulunamadı. ID: {id}"` |

---

# 3. ENDPOINT ÖZET TABLOSU

| # | Metot | Endpoint | Açıklama | Status |
|---|-------|----------|----------|--------|
| 1 | `GET` | `/api/categories` | Tüm kategorileri listele | 200 |
| 2 | `GET` | `/api/categories/{id}` | ID ile kategori getir | 200 |
| 3 | `POST` | `/api/categories` | Yeni kategori ekle | 201 |
| 4 | `PUT` | `/api/categories` | Kategori güncelle | 200 |
| 5 | `DELETE` | `/api/categories/{id}` | Kategori sil | 200 |
| 6 | `GET` | `/api/products` | Tüm ürünleri listele | 200 |
| 7 | `GET` | `/api/products/{id}` | ID ile ürün getir | 200 |
| 8 | `POST` | `/api/products` | Yeni ürün ekle | 201 |
| 9 | `PUT` | `/api/products` | Ürün güncelle | 200 |
| 10 | `DELETE` | `/api/products/{id}` | Ürün sil | 200 |

---

# 4. FRONTEND İÇİN TYPESCRIPT INTERFACE'LERİ

Aşağıdaki TypeScript arayüzleri, frontend projesinde doğrudan kullanılmak üzere hazırlanmıştır:

## 4.1 Category Interfaces

```typescript
// ===== REQUEST INTERFACES =====

interface CreateCategoryRequest {
  name: string;         // Zorunlu, 2-100 karakter
  description?: string; // Opsiyonel, max 500 karakter
}

interface UpdateCategoryRequest {
  id: number;           // Zorunlu
  name: string;         // Zorunlu, 2-100 karakter
  description?: string; // Opsiyonel, max 500 karakter
}

// ===== RESPONSE INTERFACES =====

interface GetAllCategoriesResponse {
  id: number;
  name: string;
}

interface GetByIdCategoryResponse {
  id: number;
  name: string;
  description: string;
  createdDate: string;    // ISO 8601 datetime
  updatedDate: string | null;
  isActive: boolean;
}

interface CreatedCategoryResponse {
  id: number;
  name: string;
  description: string;
  createdDate: string;
}

interface UpdatedCategoryResponse {
  id: number;
  name: string;
  description: string;
  updatedDate: string;
}

interface DeletedCategoryResponse {
  id: number;
  name: string;
}
```

## 4.2 Product Interfaces

```typescript
// ===== REQUEST INTERFACES =====

interface CreateProductRequest {
  name: string;          // Zorunlu, 2-100 karakter
  description?: string;  // Opsiyonel, max 500 karakter
  unitPrice: number;     // Zorunlu, min 0
  unitsInStock?: number; // Opsiyonel, min 0, varsayılan 0
  imageUrl?: string;     // Opsiyonel
  categoryId: number;    // Zorunlu
}

interface UpdateProductRequest {
  id: number;            // Zorunlu
  name: string;          // Zorunlu, 2-100 karakter
  description?: string;  // Opsiyonel, max 500 karakter
  unitPrice: number;     // Zorunlu, min 0
  unitsInStock?: number; // Opsiyonel, min 0
  imageUrl?: string;     // Opsiyonel
  categoryId: number;    // Zorunlu
}

// ===== RESPONSE INTERFACES =====

interface GetAllProductsResponse {
  id: number;
  name: string;
  unitPrice: number;
  unitsInStock: number;
  imageUrl: string | null;
  categoryId: number;
  categoryName: string;
}

interface GetByIdProductResponse {
  id: number;
  name: string;
  description: string;
  unitPrice: number;
  unitsInStock: number;
  imageUrl: string | null;
  categoryId: number;
  categoryName: string;
  createdDate: string;
  updatedDate: string | null;
  isActive: boolean;
}

interface CreatedProductResponse {
  id: number;
  name: string;
  description: string;
  unitPrice: number;
  unitsInStock: number;
  imageUrl: string | null;
  categoryId: number;
  categoryName: string;
  createdDate: string;
}

interface UpdatedProductResponse {
  id: number;
  name: string;
  description: string;
  unitPrice: number;
  unitsInStock: number;
  imageUrl: string | null;
  categoryId: number;
  categoryName: string;
  updatedDate: string;
}

interface DeletedProductResponse {
  id: number;
  name: string;
}
```

---

# 5. FRONTEND SERVİS KULLANIM ÖRNEKLERİ (Axios)

```typescript
import axios from "axios";

const API_BASE_URL = "http://localhost:8080/api";

// ===== CATEGORY SERVİSİ =====

const categoryService = {
  getAll: () =>
    axios.get<GetAllCategoriesResponse[]>(`${API_BASE_URL}/categories`),

  getById: (id: number) =>
    axios.get<GetByIdCategoryResponse>(`${API_BASE_URL}/categories/${id}`),

  add: (request: CreateCategoryRequest) =>
    axios.post<CreatedCategoryResponse>(`${API_BASE_URL}/categories`, request),

  update: (request: UpdateCategoryRequest) =>
    axios.put<UpdatedCategoryResponse>(`${API_BASE_URL}/categories`, request),

  delete: (id: number) =>
    axios.delete<DeletedCategoryResponse>(`${API_BASE_URL}/categories/${id}`),
};

// ===== PRODUCT SERVİSİ =====

const productService = {
  getAll: () =>
    axios.get<GetAllProductsResponse[]>(`${API_BASE_URL}/products`),

  getById: (id: number) =>
    axios.get<GetByIdProductResponse>(`${API_BASE_URL}/products/${id}`),

  add: (request: CreateProductRequest) =>
    axios.post<CreatedProductResponse>(`${API_BASE_URL}/products`, request),

  update: (request: UpdateProductRequest) =>
    axios.put<UpdatedProductResponse>(`${API_BASE_URL}/products`, request),

  delete: (id: number) =>
    axios.delete<DeletedProductResponse>(`${API_BASE_URL}/products/${id}`),
};
```

---

# 6. CORS AYARLARI

> **Not:** Backend'de şu an CORS konfigürasyonu bulunmamaktadır. Frontend ile entegrasyon için backend'e CORS ayarı eklenmelidir.

**Önerilen CORS konfigürasyonu:**

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*");
    }
}
```

---

# 7. ÖNEMLİ NOTLAR

1. **Soft Delete:** Silme işlemleri `deletedDate` alanını set eder ve `isActive` alanını `false` yapar. Kayıt veritabanından fiziksel olarak silinmez.

2. **Otomatik Alanlar:**
   - `id` → Veritabanı tarafından otomatik üretilir (auto-increment)
   - `createdDate` → Kayıt oluşturulurken otomatik set edilir (`@PrePersist`)
   - `updatedDate` → Kayıt güncellenirken otomatik set edilir (`@PreUpdate`)
   - `isActive` → Oluşturulurken `true` olarak set edilir

3. **Kategori-Ürün İlişkisi:** Bir ürün mutlaka bir kategoriye bağlı olmalıdır (`categoryId` zorunlu). Bir kategori silindiğinde, o kategoriye bağlı ürünlerin durumu kontrol edilmelidir.

4. **JSON Alanlar Camel Case:** Tüm JSON field isimleri camelCase formatındadır (Java naming convention).

5. **Sayısal ID'ler:** Tüm ID alanları `int` (integer) tipindedir — `0`'dan başlayabilir, auto-increment ile artar.

6. **Global Exception Handling:** Tüm hatalar `@RestControllerAdvice` ile yakalanır. İş kuralı hataları `BusinessException` olarak fırlatılır ve `400 Bad Request` döner. Validasyon hataları `MethodArgumentNotValidException` olarak yakalanır ve alan bazlı hata detayları ile `400 Bad Request` döner.
