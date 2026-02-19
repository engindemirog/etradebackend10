# E-Trade Backend — Gereksinim Dokümanı

**Proje:** etradebackend10  
**Versiyon:** 1.0  
**Tarih:** 19 Şubat 2026  
**Durum:** Uygulandı ✅

---

## 1. Genel Bakış

E-Trade Backend, bir e-ticaret platformunun arka uç API'sini sağlar. Sistem şu an **Kategori** ve **Ürün** modüllerinden oluşmaktadır. Her modül CRUD (Create, Read, Update, Delete) operasyonlarını destekler.

### 1.1 Teknoloji Stack

| Bileşen | Teknoloji |
|---------|-----------|
| Dil | Java 17 |
| Framework | Spring Boot 4.0.2 |
| Build | Maven |
| Veritabanı | H2 (file-based) |
| ORM | Spring Data JPA (Hibernate) |
| Validasyon | Jakarta Bean Validation |
| Hata Yönetimi | Global Exception Handler (`@RestControllerAdvice`) |

### 1.2 Mimari

Katmanlı mimari (Layered Architecture) uygulanır:

```
Controller → Service → Repository → Database
                ↑
          Business Rules
```

---

## 2. KATEGORİ (Category) MODÜLÜ

### 2.1 Veri Modeli

**Tablo adı:** `categories`

| Alan | Veritabanı Kolonu | Tip | Zorunlu | Açıklama |
|------|-------------------|-----|---------|----------|
| id | `id` | int (PK, Auto Increment) | Otomatik | Benzersiz tanımlayıcı |
| name | `name` | String | Evet | Kategori adı |
| description | `description` | String | Hayır | Kategori açıklaması |
| createdDate | `created_date` | LocalDateTime | Otomatik | Oluşturulma tarihi |
| updatedDate | `updated_date` | LocalDateTime | Otomatik | Güncellenme tarihi |
| deletedDate | `deleted_date` | LocalDateTime | Otomatik | Silinme tarihi (soft delete) |
| isActive | `is_active` | boolean | Otomatik | Aktiflik durumu (varsayılan: true) |

**İlişkiler:**
- Bir kategori birden fazla ürüne sahip olabilir (**One-to-Many** → `Product`)

---

### 2.2 Fonksiyonel Gereksinimler

#### REQ-CAT-001: Tüm Kategorileri Listeleme

| Özellik | Detay |
|---------|-------|
| **Endpoint** | `GET /api/categories` |
| **Açıklama** | Sistemdeki tüm kategoriler listelenir. |
| **Giriş** | — |
| **Çıkış** | Kategori listesi (`id`, `name`) |
| **İş Kuralları** | — |
| **Durum** | ✅ Uygulandı |

#### REQ-CAT-002: ID ile Kategori Getirme

| Özellik | Detay |
|---------|-------|
| **Endpoint** | `GET /api/categories/{id}` |
| **Açıklama** | Belirtilen ID'ye sahip kategorinin detaylı bilgileri getirilir. |
| **Giriş** | Path: `id` (int) |
| **Çıkış** | Kategori detayı (`id`, `name`, `description`, `createdDate`, `updatedDate`, `isActive`) |
| **İş Kuralları** | BRL-CAT-001 |
| **Durum** | ✅ Uygulandı |

#### REQ-CAT-003: Yeni Kategori Ekleme

| Özellik | Detay |
|---------|-------|
| **Endpoint** | `POST /api/categories` |
| **Açıklama** | Sisteme yeni bir kategori eklenir. |
| **Giriş** | `name` (zorunlu), `description` (opsiyonel) |
| **Çıkış** | Oluşturulan kategorinin bilgileri (`id`, `name`, `description`, `createdDate`) |
| **Validasyonlar** | VAL-CAT-001, VAL-CAT-002 |
| **İş Kuralları** | BRL-CAT-002 |
| **Durum** | ✅ Uygulandı |

#### REQ-CAT-004: Kategori Güncelleme

| Özellik | Detay |
|---------|-------|
| **Endpoint** | `PUT /api/categories` |
| **Açıklama** | Mevcut bir kategori güncellenir. |
| **Giriş** | `id` (zorunlu), `name` (zorunlu), `description` (opsiyonel) |
| **Çıkış** | Güncellenen kategorinin bilgileri (`id`, `name`, `description`, `updatedDate`) |
| **Validasyonlar** | VAL-CAT-001, VAL-CAT-002, VAL-CAT-003 |
| **İş Kuralları** | BRL-CAT-001, BRL-CAT-003 |
| **Durum** | ✅ Uygulandı |

#### REQ-CAT-005: Kategori Silme

| Özellik | Detay |
|---------|-------|
| **Endpoint** | `DELETE /api/categories/{id}` |
| **Açıklama** | Belirtilen ID'ye sahip kategori silinir. |
| **Giriş** | Path: `id` (int) |
| **Çıkış** | Silinen kategorinin bilgileri (`id`, `name`) |
| **İş Kuralları** | BRL-CAT-001 |
| **Durum** | ✅ Uygulandı |

---

### 2.3 Validasyon Kuralları

| Kod | Alan | Kural | Hata Mesajı |
|-----|------|-------|-------------|
| VAL-CAT-001 | `name` | Boş veya null olamaz | `"Kategori adı boş olamaz."` |
| VAL-CAT-002 | `name` | 2 - 100 karakter arasında olmalı | `"Kategori adı 2 ile 100 karakter arasında olmalıdır."` |
| VAL-CAT-003 | `id` | Null olamaz | `"Kategori ID boş olamaz."` |
| VAL-CAT-004 | `description` | En fazla 500 karakter olabilir | `"Açıklama en fazla 500 karakter olabilir."` |

---

### 2.4 İş Kuralları (Business Rules)

| Kod | Kural | Uygulama Noktası | Hata Mesajı |
|-----|-------|-------------------|-------------|
| BRL-CAT-001 | Belirtilen ID'ye sahip kategori veritabanında mevcut olmalıdır. | getById, update, delete | `"Kategori bulunamadı. ID: {id}"` |
| BRL-CAT-002 | Aynı isimde başka bir kategori bulunmamalıdır. | add | `"Bu kategori adı zaten mevcut: {name}"` |
| BRL-CAT-003 | Güncelleme sırasında, aynı isimde başka bir kategori (farklı ID) bulunmamalıdır. | update | `"Bu kategori adı zaten başka bir kategoriye ait: {name}"` |

---

## 3. ÜRÜN (Product) MODÜLÜ

### 3.1 Veri Modeli

**Tablo adı:** `products`

| Alan | Veritabanı Kolonu | Tip | Zorunlu | Açıklama |
|------|-------------------|-----|---------|----------|
| id | `id` | int (PK, Auto Increment) | Otomatik | Benzersiz tanımlayıcı |
| name | `name` | String | Evet | Ürün adı |
| description | `description` | String | Hayır | Ürün açıklaması |
| unitPrice | `unit_price` | double | Evet | Birim fiyat |
| unitsInStock | `units_in_stock` | int | Hayır | Stok adedi |
| imageUrl | `image_url` | String | Hayır | Ürün görseli URL adresi |
| category | `category_id` (FK) | Category (ManyToOne) | Evet | Ürünün bağlı olduğu kategori |
| createdDate | `created_date` | LocalDateTime | Otomatik | Oluşturulma tarihi |
| updatedDate | `updated_date` | LocalDateTime | Otomatik | Güncellenme tarihi |
| deletedDate | `deleted_date` | LocalDateTime | Otomatik | Silinme tarihi (soft delete) |
| isActive | `is_active` | boolean | Otomatik | Aktiflik durumu (varsayılan: true) |

**İlişkiler:**
- Her ürün bir kategoriye bağlıdır (**Many-to-One** → `Category`)

---

### 3.2 Fonksiyonel Gereksinimler

#### REQ-PRD-001: Tüm Ürünleri Listeleme

| Özellik | Detay |
|---------|-------|
| **Endpoint** | `GET /api/products` |
| **Açıklama** | Sistemdeki tüm ürünler listelenir. |
| **Giriş** | — |
| **Çıkış** | Ürün listesi (`id`, `name`, `unitPrice`, `unitsInStock`, `imageUrl`, `categoryId`, `categoryName`) |
| **İş Kuralları** | — |
| **Durum** | ✅ Uygulandı |

#### REQ-PRD-002: ID ile Ürün Getirme

| Özellik | Detay |
|---------|-------|
| **Endpoint** | `GET /api/products/{id}` |
| **Açıklama** | Belirtilen ID'ye sahip ürünün tüm detayları getirilir. |
| **Giriş** | Path: `id` (int) |
| **Çıkış** | Ürün detayı (`id`, `name`, `description`, `unitPrice`, `unitsInStock`, `imageUrl`, `categoryId`, `categoryName`, `createdDate`, `updatedDate`, `isActive`) |
| **İş Kuralları** | BRL-PRD-001 |
| **Durum** | ✅ Uygulandı |

#### REQ-PRD-003: Yeni Ürün Ekleme

| Özellik | Detay |
|---------|-------|
| **Endpoint** | `POST /api/products` |
| **Açıklama** | Sisteme yeni bir ürün eklenir. Ürün mutlaka bir kategoriye bağlanmalıdır. |
| **Giriş** | `name` (zorunlu), `description` (opsiyonel), `unitPrice` (zorunlu), `unitsInStock` (opsiyonel), `imageUrl` (opsiyonel), `categoryId` (zorunlu) |
| **Çıkış** | Oluşturulan ürünün bilgileri (`id`, `name`, `description`, `unitPrice`, `unitsInStock`, `imageUrl`, `categoryId`, `categoryName`, `createdDate`) |
| **Validasyonlar** | VAL-PRD-001, VAL-PRD-002, VAL-PRD-003, VAL-PRD-004, VAL-PRD-005, VAL-PRD-006 |
| **İş Kuralları** | BRL-PRD-002, BRL-PRD-004, BRL-PRD-005 |
| **Durum** | ✅ Uygulandı |

#### REQ-PRD-004: Ürün Güncelleme

| Özellik | Detay |
|---------|-------|
| **Endpoint** | `PUT /api/products` |
| **Açıklama** | Mevcut bir ürün güncellenir. |
| **Giriş** | `id` (zorunlu), `name` (zorunlu), `description` (opsiyonel), `unitPrice` (zorunlu), `unitsInStock` (opsiyonel), `imageUrl` (opsiyonel), `categoryId` (zorunlu) |
| **Çıkış** | Güncellenen ürünün bilgileri (`id`, `name`, `description`, `unitPrice`, `unitsInStock`, `imageUrl`, `categoryId`, `categoryName`, `updatedDate`) |
| **Validasyonlar** | VAL-PRD-001, VAL-PRD-002, VAL-PRD-003, VAL-PRD-004, VAL-PRD-005, VAL-PRD-006, VAL-PRD-007 |
| **İş Kuralları** | BRL-PRD-001, BRL-PRD-003, BRL-PRD-004, BRL-PRD-005 |
| **Durum** | ✅ Uygulandı |

#### REQ-PRD-005: Ürün Silme

| Özellik | Detay |
|---------|-------|
| **Endpoint** | `DELETE /api/products/{id}` |
| **Açıklama** | Belirtilen ID'ye sahip ürün silinir. |
| **Giriş** | Path: `id` (int) |
| **Çıkış** | Silinen ürünün bilgileri (`id`, `name`) |
| **İş Kuralları** | BRL-PRD-001 |
| **Durum** | ✅ Uygulandı |

---

### 3.3 Validasyon Kuralları

| Kod | Alan | Kural | Hata Mesajı |
|-----|------|-------|-------------|
| VAL-PRD-001 | `name` | Boş veya null olamaz | `"Ürün adı boş olamaz."` |
| VAL-PRD-002 | `name` | 2 - 100 karakter arasında olmalı | `"Ürün adı 2 ile 100 karakter arasında olmalıdır."` |
| VAL-PRD-003 | `unitPrice` | Null olamaz | `"Birim fiyat boş olamaz."` |
| VAL-PRD-004 | `unitPrice` | 0'dan küçük olamaz | `"Birim fiyat 0'dan küçük olamaz."` |
| VAL-PRD-005 | `unitsInStock` | 0'dan küçük olamaz | `"Stok adedi 0'dan küçük olamaz."` |
| VAL-PRD-006 | `categoryId` | Null olamaz | `"Kategori ID boş olamaz."` |
| VAL-PRD-007 | `id` | Null olamaz (güncelleme) | `"Ürün ID boş olamaz."` |
| VAL-PRD-008 | `description` | En fazla 500 karakter olabilir | `"Açıklama en fazla 500 karakter olabilir."` |

---

### 3.4 İş Kuralları (Business Rules)

| Kod | Kural | Uygulama Noktası | Hata Mesajı |
|-----|-------|-------------------|-------------|
| BRL-PRD-001 | Belirtilen ID'ye sahip ürün veritabanında mevcut olmalıdır. | getById, update, delete | `"Ürün bulunamadı. ID: {id}"` |
| BRL-PRD-002 | Aynı isimde başka bir ürün bulunmamalıdır. | add | `"Bu ürün adı zaten mevcut: {name}"` |
| BRL-PRD-003 | Güncelleme sırasında, aynı isimde başka bir ürün (farklı ID) bulunmamalıdır. | update | `"Bu ürün adı zaten başka bir ürüne ait: {name}"` |
| BRL-PRD-004 | Birim fiyat 0'dan küçük olamaz. | add, update | `"Birim fiyat 0'dan küçük olamaz."` |
| BRL-PRD-005 | Ürünün bağlandığı kategori veritabanında mevcut olmalıdır. | add, update | `"Kategori bulunamadı. ID: {categoryId}"` |

---

## 4. ORTAK ALTYAPI GEREKSİNİMLERİ

### 4.1 BaseEntity Gereksinimleri

| Kod | Gereksinim | Durum |
|-----|------------|-------|
| REQ-BASE-001 | Tüm entity'ler `id`, `createdDate`, `updatedDate`, `deletedDate`, `isActive` alanlarını barındırmalıdır. | ✅ |
| REQ-BASE-002 | `id` alanı veritabanı tarafından auto-increment ile üretilmelidir. | ✅ |
| REQ-BASE-003 | `createdDate` alanı kayıt oluşturulduğunda otomatik olarak set edilmelidir (`@PrePersist`). | ✅ |
| REQ-BASE-004 | `updatedDate` alanı kayıt güncellendiğinde otomatik olarak set edilmelidir (`@PreUpdate`). | ✅ |
| REQ-BASE-005 | `isActive` alanı kayıt oluşturulduğunda `true` olarak set edilmelidir. | ✅ |

### 4.2 Hata Yönetimi Gereksinimleri

| Kod | Gereksinim | Durum |
|-----|------------|-------|
| REQ-ERR-001 | İş kuralı ihlalleri `BusinessException` olarak fırlatılmalı ve `400 Bad Request` ile dönmelidir. | ✅ |
| REQ-ERR-002 | Validasyon hataları `MethodArgumentNotValidException` olarak yakalanmalı ve alan bazlı hata detayları ile `400 Bad Request` dönmelidir. | ✅ |
| REQ-ERR-003 | İş kuralı hata yanıtı `status`, `message`, `timestamp` alanlarını içermelidir. | ✅ |
| REQ-ERR-004 | Validasyon hata yanıtı `status`, `message`, `errors` (alan→mesaj map), `timestamp` alanlarını içermelidir. | ✅ |
| REQ-ERR-005 | Global exception handler `@RestControllerAdvice` ile tüm controller'ları kapsamalıdır. | ✅ |

### 4.3 Genel API Gereksinimleri

| Kod | Gereksinim | Durum |
|-----|------------|-------|
| REQ-API-001 | Tüm endpoint'ler `/api/` prefix'i ile başlamalıdır. | ✅ |
| REQ-API-002 | Request/Response formatı JSON olmalıdır. | ✅ |
| REQ-API-003 | POST işlemlerinde `201 Created` status kodu dönmelidir. | ✅ |
| REQ-API-004 | GET, PUT, DELETE işlemlerinde `200 OK` status kodu dönmelidir. | ✅ |
| REQ-API-005 | Request body validasyonları `@Valid` annotation'ı ile tetiklenmelidir. | ✅ |

---

## 5. ENTITY İLİŞKİ DİYAGRAMI

```
┌─────────────────────────────┐
│       BaseEntity            │
│  (MappedSuperclass)        │
├─────────────────────────────┤
│  id          : int (PK)    │
│  createdDate : LocalDateTime│
│  updatedDate : LocalDateTime│
│  deletedDate : LocalDateTime│
│  isActive    : boolean      │
└──────────┬──────────────────┘
           │ extends
     ┌─────┴──────┐
     │            │
┌────▼────┐  ┌───▼─────┐
│ Category│  │ Product  │
├─────────┤  ├─────────-┤
│ name    │  │ name     │
│ desc.   │  │ desc.    │
│         │  │ unitPrice│
│ products│◄─┤ category │
│ (1:N)   │  │ stock    │
│         │  │ imageUrl │
└─────────┘  └──────────┘
```

---

## 6. KURAL-OPERASYON MATRİSİ

Hangi iş kuralının hangi operasyonda uygulandığını gösteren matris:

### Kategori

| Kural | getAll | getById | add | update | delete |
|-------|--------|---------|-----|--------|--------|
| BRL-CAT-001 (Varlık kontrolü) | — | ✅ | — | ✅ | ✅ |
| BRL-CAT-002 (İsim tekrar - ekleme) | — | — | ✅ | — | — |
| BRL-CAT-003 (İsim tekrar - güncelleme) | — | — | — | ✅ | — |

### Ürün

| Kural | getAll | getById | add | update | delete |
|-------|--------|---------|-----|--------|--------|
| BRL-PRD-001 (Varlık kontrolü) | — | ✅ | — | ✅ | ✅ |
| BRL-PRD-002 (İsim tekrar - ekleme) | — | — | ✅ | — | — |
| BRL-PRD-003 (İsim tekrar - güncelleme) | — | — | — | ✅ | — |
| BRL-PRD-004 (Fiyat kontrolü) | — | — | ✅ | ✅ | — |
| BRL-PRD-005 (Kategori varlık kontrolü) | — | — | ✅ | ✅ | — |

---

## 7. GEREKSİNİM İZLENEBİLİRLİK MATRİSİ

| Gereksinim | Kaynak Dosya | Test Durumu |
|------------|-------------|-------------|
| REQ-CAT-001 | `CategoryServiceImpl.getAll()` | ⬜ Yazılacak |
| REQ-CAT-002 | `CategoryServiceImpl.getById()` | ⬜ Yazılacak |
| REQ-CAT-003 | `CategoryServiceImpl.add()` | ⬜ Yazılacak |
| REQ-CAT-004 | `CategoryServiceImpl.update()` | ⬜ Yazılacak |
| REQ-CAT-005 | `CategoryServiceImpl.delete()` | ⬜ Yazılacak |
| REQ-PRD-001 | `ProductServiceImpl.getAll()` | ⬜ Yazılacak |
| REQ-PRD-002 | `ProductServiceImpl.getById()` | ⬜ Yazılacak |
| REQ-PRD-003 | `ProductServiceImpl.add()` | ⬜ Yazılacak |
| REQ-PRD-004 | `ProductServiceImpl.update()` | ⬜ Yazılacak |
| REQ-PRD-005 | `ProductServiceImpl.delete()` | ⬜ Yazılacak |
| REQ-ERR-001 | `GlobalExceptionHandler` | ⬜ Yazılacak |
| REQ-ERR-002 | `GlobalExceptionHandler` | ⬜ Yazılacak |
