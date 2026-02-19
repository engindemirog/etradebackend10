# E-Trade Backend — Mimari Dokümantasyon

**Proje:** etradebackend10  
**Versiyon:** 1.0  
**Tarih:** 19 Şubat 2026

---

## 1. Genel Mimari Yaklaşım

E-Trade Backend projesi, modern Spring Boot tabanlı **katmanlı mimari** (Layered Architecture) ile geliştirilmiştir. Her katman, sorumlulukları net şekilde ayrılmıştır ve bağımlılıklar yukarıdan aşağıya doğru inject edilir.

### Katmanlar:

```
com.turkcell.etradebackend10
├── api
│   └── controllers          → REST Controller'lar
├── business
│   ├── abstracts            → Service interface'leri
│   ├── concretes            → Service implementasyonları
│   └── rules                → İş kuralları sınıfları
├── dataAccess
│   └── abstracts            → Repository interface'leri (JPA)
├── entities
│   ├── concretes            → Entity (JPA) sınıfları
│   └── dtos
│       ├── requests         → İstek DTO'ları (entity bazlı alt paket)
│       └── responses        → Yanıt DTO'ları (entity bazlı alt paket)
├── config                   → Ortak konfigürasyonlar (ör. CORS)
```

---

## 2. Katman Detayları

### 2.1 API Katmanı (Controllers)
- **Konum:** `api/controllers/`
- **Sorumluluk:** HTTP isteklerini karşılar, request/response DTO'ları ile çalışır.
- **Yöntemler:** CRUD endpoint'leri (`GET`, `POST`, `PUT`, `DELETE`)
- **Validasyon:** Request body'de `@Valid` ile alan doğrulama
- **Injection:** Service interface'leri constructor ile inject edilir
- **Örnek:** `ProductsController`, `CategoriesController`

### 2.2 Business Katmanı
- **Konum:** `business/abstracts/`, `business/concretes/`, `business/rules/`
- **abstracts:** Service interface'leri (ör. `ProductService`)
- **concretes:** Service implementasyonları (ör. `ProductServiceImpl`)
- **rules:** İş kuralları sınıfları (ör. `ProductBusinessRules`)
- **Sorumluluk:** İş mantığı, validasyon-first yaklaşımı, entity ↔ DTO dönüşümleri
- **Injection:** Repository ve business rules constructor ile inject edilir
- **Örnek:**
  - `ProductServiceImpl` → CRUD işlemleri, iş kurallarını çağırır
  - `ProductBusinessRules` → Varlık kontrolü, isim tekrarı, fiyat validasyonu

### 2.3 DataAccess Katmanı
- **Konum:** `dataAccess/abstracts/`
- **Sorumluluk:** Veritabanı erişimi, JPA repository interface'leri
- **Yöntemler:** Derived query metotları (`existsByName`, `findById`, vb.)
- **Örnek:** `ProductRepository`, `CategoryRepository`

### 2.4 Entities Katmanı
- **Konum:** `entities/concretes/`, `entities/dtos/requests/`, `entities/dtos/responses/`
- **concretes:** JPA entity sınıfları (ör. `Product`, `Category`)
- **dtos/requests:** İstek DTO'ları (ör. `CreateProductRequest`)
- **dtos/responses:** Yanıt DTO'ları (ör. `GetAllProductsResponse`)
- **Sorumluluk:** Domain model ve veri transfer nesneleri
- **Validasyon:** DTO'larda Jakarta Bean Validation annotation'ları
- **Örnek:**
  - `Product` → `BaseEntity`'den miras, kategori ilişkisi (ManyToOne)
  - `CreateProductRequest` → Alan validasyonu, no-args/all-args constructor

### 2.5 Config Katmanı
- **Konum:** `config/`
- **Sorumluluk:** Ortak konfigürasyonlar (ör. CORS, global exception handler)
- **Örnek:** `CorsConfig`, `GlobalExceptionHandler`

---

## 3. Bağımlılık Yönetimi ve Injection
- **Constructor Injection:** Tüm bağımlılıklar `private final` olarak tanımlanır ve constructor üzerinden inject edilir.
- **Field Injection (Autowired):** Kullanılmaz.
- **Spring IoC Container:** Tüm bean'ler Spring tarafından yönetilir.

---

## 4. Entity ve DTO Tasarımı
- **BaseEntity:** Tüm entity'ler ortak alanları içerir (`id`, `createdDate`, `updatedDate`, `deletedDate`, `isActive`)
- **Entity Sınıfları:** JPA ile `@Entity`, `@Table`, `@Column` annotation'ları
- **DTO Pattern:** Her işlem için ayrı request/response DTO'su
- **Validasyon:** DTO'larda alan bazlı validasyon, hata mesajları Türkçe
- **Mapper:** Manuel mapping, otomatik kütüphane kullanılmaz

---

## 5. İş Kuralları (Business Rules)
- **Her iş kuralı ayrı metot olarak yazılır**
- **Kural ihlalinde:** `BusinessException` fırlatılır, global handler ile kullanıcıya gösterilir
- **Örnekler:**
  - Varlık kontrolü: `checkIfProductExistsById(int id)`
  - İsim tekrarı: `checkIfProductNameAlreadyExists(String name)`
  - Fiyat validasyonu: `checkIfUnitPriceValid(double unitPrice)`

---

## 6. Hata Yönetimi
- **Global Exception Handler:** `@RestControllerAdvice` ile tüm controller'lar kapsanır
- **BusinessException:** İş kuralı hataları için özel exception
- **ValidationErrorResponse:** Validasyon hataları için alan bazlı hata yanıtı
- **Yanıt Formatı:**
  - İş kuralı hatası: `{ status, message, timestamp }`
  - Validasyon hatası: `{ status, message, errors, timestamp }`

---

## 7. Soft Delete ve Otomatik Alanlar
- **Soft Delete:** Silme işlemlerinde `deletedDate` set edilir, `isActive` false yapılır
- **Otomatik Alanlar:**
  - `id`: Auto-increment
  - `createdDate`: `@PrePersist` ile otomatik
  - `updatedDate`: `@PreUpdate` ile otomatik
  - `isActive`: Varsayılan true

---

## 8. API Tasarımı
- **Prefix:** Tüm endpoint'ler `/api/` ile başlar
- **Status Kodları:**
  - GET/PUT/DELETE: `200 OK`
  - POST: `201 Created`
- **Request/Response:** JSON formatında, camelCase alan isimleri
- **Swagger/OpenAPI:** Otomatik dokümantasyon desteği

---

## 9. Güvenlik ve CORS
- **CORS:** Tüm origin'lere, tüm metotlara ve header'lara açık (`CorsConfig`)
- **Authentication:** Şu an yok, ileride eklenebilir

---

## 10. Genişletilebilirlik ve Test
- **Yeni Entity Ekleme:** Katmanlı mimari sayesinde kolay
- **Unit Test:** Service ve business rules katmanında yazılabilir
- **Integration Test:** Controller ve repository katmanında

---

## 11. Özet

Bu mimari, sürdürülebilir, genişletilebilir ve test edilebilir bir backend uygulaması sağlar. Her katman kendi sorumluluğuna odaklanır, bağımlılıklar net ve Spring IoC ile yönetilir. Hata yönetimi ve validasyon kullanıcıya anlamlı şekilde iletilir.
