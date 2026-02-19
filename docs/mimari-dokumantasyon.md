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
│   ├── exceptions           → Hata yönetimi (GlobalExceptionHandler, BusinessException)
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
- **Sorumluluk:** Ortak konfigürasyonlar (ör. CORS)
- **Örnek:** `CorsConfig`

### 2.6 Exceptions Katmanı
- **Konum:** `business/exceptions/`
- **Sorumluluk:** Global hata yönetimi ve exception sınıfları
- **Sınıflar:**
  - `GlobalExceptionHandler` — `@RestControllerAdvice` ile tüm hataları yakalar
  - `BusinessException` — İş kuralı ihlalleri için özel exception
  - `BusinessErrorResponse` — İş kuralı hata yanıt formatı (`status`, `message`, `timestamp`)
  - `ValidationErrorResponse` — Validasyon hata yanıt formatı (`status`, `message`, `errors`, `timestamp`)

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

## 10. Test Mimarisi

### 10.1 Test Kapsamı
Projede **73 unit test** yazılmıştır ve tümü başarıyla geçmektedir.

| Katman | Test Sınıfı | Test Sayısı | Yöntem |
|--------|------------|-------------|--------|
| Business Rules | `CategoryBusinessRulesTest` | 6 | Mockito + JUnit |
| Business Rules | `ProductBusinessRulesTest` | 10 | Mockito + JUnit |
| Service | `CategoryServiceImplTest` | 11 | Mockito + JUnit |
| Service | `ProductServiceImplTest` | 17 | Mockito + JUnit |
| Controller | `CategoriesControllerTest` | 14 | MockMvc + @WebMvcTest |
| Controller | `ProductsControllerTest` | 15 | MockMvc + @WebMvcTest |

### 10.2 Test Yapısı
```
src/test/java/com/turkcell/etradebackend10/
├── business/
│   ├── concretes/
│   │   ├── CategoryServiceImplTest.java
│   │   └── ProductServiceImplTest.java
│   └── rules/
│       ├── CategoryBusinessRulesTest.java
│       └── ProductBusinessRulesTest.java
└── api/
    └── controllers/
        ├── CategoriesControllerTest.java
        └── ProductsControllerTest.java
```

### 10.3 Code Coverage
- **Araç:** JaCoCo 0.8.12
- **Business Rules:** %100 coverage
- **Service Impl:** %100 coverage
- **Rapor:** `target/site/jacoco/index.html`

### 10.4 Test Teknolojileri
| Teknoloji | Versiyon | Kullanım Alanı |
|-----------|---------|----------------|
| JUnit Jupiter | 6.0.2 | Test framework |
| Mockito | 5.20.0 | Mocking |
| MockMvc | Spring Test | API testi |
| JaCoCo | 0.8.12 | Code coverage |

### 10.5 Spring Boot 4 Test Notları
- `@WebMvcTest` import: `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`
- `@MockitoBean` import: `org.springframework.test.context.bean.override.mockito.MockitoBean`
- `ObjectMapper` @WebMvcTest içinde otomatik konfigüre edilmez, manuel oluşturulmalıdır

---

## 11. Genişletilebilirlik
- **Yeni Entity Ekleme:** Katmanlı mimari sayesinde kolay
- **Test Ekleme:** Mevcut test pattern'ı takip edilerek yeni entity testleri yazılabilir

---

## 12. Özet

Bu mimari, sürdürülebilir, genişletilebilir ve test edilebilir bir backend uygulaması sağlar. Her katman kendi sorumluluğuna odaklanır, bağımlılıklar net ve Spring IoC ile yönetilir. Hata yönetimi ve validasyon kullanıcıya anlamlı şekilde iletilir. **73 unit test** ile business, service ve controller katmanları %100 test kapsamına sahiptir.
