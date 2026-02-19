# E-Trade Backend Projesi - Copilot Agent Talimatları

## Proje Genel Bilgileri
- **Proje Adı:** etradebackend10
- **Dil:** Java 17
- **Framework:** Spring Boot 4.0.2
- **Build Tool:** Maven
- **Veritabanı:** H2 (file-based, kalıcı — `jdbc:h2:file:./data/etradedb`)
- **ORM:** Spring Data JPA (Hibernate)
- **Validasyon:** Jakarta Bean Validation
- **API Dokümantasyonu:** SpringDoc OpenAPI (Swagger)
- **Lombok:** KULLANILMIYOR. Tüm getter/setter/constructor elle yazılır.

---

## Mimari Yaklaşım — Katmanlı Mimari

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
```

---

## Kodlama Standartları

### 1. Lombok Yasağı
- Projede Lombok **kesinlikle kullanılmaz**.
- Tüm getter, setter, constructor, toString, equals, hashCode metotları elle yazılır.
- `@Data`, `@Getter`, `@Setter`, `@AllArgsConstructor`, `@NoArgsConstructor`, `@Builder` gibi annotation'lar kullanılmaz.

### 2. Dependency Injection
- **Constructor Injection** kullanılır. Field injection (`@Autowired`) kullanılmaz.
- Bağımlılıklar `private final` olarak tanımlanır.
- Constructor parametreleri üzerinden inject edilir.

```java
private final ProductRepository productRepository;

public ProductServiceImpl(ProductRepository productRepository) {
    this.productRepository = productRepository;
}
```

### 3. Entity Sınıfları
- Tüm entity'ler `BaseEntity` sınıfından miras alır (`extends BaseEntity`).
- `BaseEntity` aşağıdaki ortak alanları içerir:
  - `id` (int, `@GeneratedValue(strategy = GenerationType.IDENTITY)`)
  - `createdDate` (LocalDateTime, `@PrePersist` ile otomatik set)
  - `updatedDate` (LocalDateTime, `@PreUpdate` ile otomatik set)
  - `deletedDate` (LocalDateTime, soft delete için)
  - `isActive` (boolean, oluşturulurken `true` olarak set)
- `BaseEntity` → `@MappedSuperclass` ile işaretlenir.
- Entity sınıfları → `@Entity` ve `@Table(name = "tablo_adi")` ile işaretlenir.
- Kolon adları → `@Column(name = "snake_case_isim")` ile belirlenir.
- No-args ve All-args constructor yazılır.

### 4. DTO (Data Transfer Object) Yapısı
- **Request/Response pattern** sıkı sıkıya uygulanır.
- Her operasyon için ayrı DTO nesnesi oluşturulur.
- DTO'lar entity bazlı alt paketlere ayrılır: `requests/product/`, `responses/product/`
- İsimlendirme kuralları:
  - **Request:** `CreateProductRequest`, `UpdateProductRequest`
  - **Response:** `GetAllProductsResponse`, `GetByIdProductResponse`, `CreatedProductResponse`, `UpdatedProductResponse`, `DeletedProductResponse`
- Request DTO'larında Jakarta Validation annotation'ları kullanılır (`@NotBlank`, `@NotNull`, `@Min`, `@Size` vb.).
- Validation mesajları Türkçe yazılır.
- DTO'larda no-args ve all-args constructor + getter/setter yazılır.

### 5. DataAccess (Repository) Katmanı
- `dataAccess/abstracts/` paketinde yer alır.
- `JpaRepository<Entity, Integer>` extend eder.
- Derived query metotları kullanılır: `existsByName`, `existsByNameAndIdNot` vb.
- İsimlendirme: `ProductRepository`

### 6. Business Katmanı

#### Service Interface (`business/abstracts/`)
- İsimlendirme: `ProductService`
- Standart CRUD metotları:
  - `List<GetAllProductsResponse> getAll()`
  - `GetByIdProductResponse getById(int id)`
  - `CreatedProductResponse add(CreateProductRequest request)`
  - `UpdatedProductResponse update(UpdateProductRequest request)`
  - `DeletedProductResponse delete(int id)`

#### Service Implementation (`business/concretes/`)
- İsimlendirme: `ProductServiceImpl`
- `@Service` annotation'ı kullanılır.
- Constructor injection ile repository ve business rules inject edilir.
- İş kuralları metot başında çağrılır (validation-first yaklaşımı).
- Entity ↔ DTO dönüşümleri manuel yapılır (mapper kütüphanesi kullanılmaz).

#### Business Rules (`business/rules/`)
- İsimlendirme: `ProductBusinessRules`
- `@Service` annotation'ı ile Spring bean olarak tanımlanır.
- Her iş kuralı ayrı bir metot olarak yazılır.
- Kural ihlalinde `RuntimeException` fırlatılır (Türkçe mesaj ile).
- Tipik kurallar:
  - `checkIfProductExistsById(int id)` — varlık kontrolü
  - `checkIfProductNameAlreadyExists(String name)` — tekrarlanma kontrolü
  - `checkIfProductNameAlreadyExistsForUpdate(int id, String name)` — güncelleme sırasında tekrarlanma
  - `checkIfUnitPriceValid(double unitPrice)` — değer doğrulama

### 7. API (Controller) Katmanı
- `api/controllers/` paketinde yer alır.
- İsimlendirme: `ProductsController` (çoğul isim)
- `@RestController` ve `@RequestMapping("/api/products")` kullanılır.
- HTTP metotları ve status kodları:
  - `GET /api/products` → `@GetMapping` → `HttpStatus.OK`
  - `GET /api/products/{id}` → `@GetMapping("/{id}")` → `HttpStatus.OK`
  - `POST /api/products` → `@PostMapping` → `HttpStatus.CREATED`
  - `PUT /api/products` → `@PutMapping` → `HttpStatus.OK`
  - `DELETE /api/products/{id}` → `@DeleteMapping("/{id}")` → `HttpStatus.OK`
- Request body'lerde `@Valid` annotation'ı kullanılır.
- Constructor injection ile service inject edilir.

---

## Yeni Entity Ekleme Akışı

Yeni bir entity eklenirken aşağıdaki sıra takip edilir:

1. **Entity** → `entities/concretes/` altında `BaseEntity`'den miras alan sınıf
2. **Request DTO'lar** → `entities/dtos/requests/{entity}/` altında `Create...Request`, `Update...Request`
3. **Response DTO'lar** → `entities/dtos/responses/{entity}/` altında `GetAll...Response`, `GetById...Response`, `Created...Response`, `Updated...Response`, `Deleted...Response`
4. **Repository** → `dataAccess/abstracts/` altında `{Entity}Repository`
5. **Business Rules** → `business/rules/` altında `{Entity}BusinessRules`
6. **Service Interface** → `business/abstracts/` altında `{Entity}Service`
7. **Service Implementation** → `business/concretes/` altında `{Entity}ServiceImpl`
8. **Controller** → `api/controllers/` altında `{Entity}sController`

---

## Genel Kurallar

- **Dil:** Kod İngilizce yazılır. Validation mesajları ve hata mesajları Türkçe yazılır.
- **Mapper:** Manuel mapping yapılır. ModelMapper, MapStruct gibi kütüphaneler kullanılmaz.
- **Exception Handling:** Şu an `RuntimeException` kullanılır (ileride global exception handler eklenebilir).
- **Soft Delete:** `deletedDate` ve `isActive` alanları ile desteklenir.
- **API Prefix:** Tüm endpoint'ler `/api/` ile başlar.
- **Veritabanı:** H2 file-based. DDL auto: update. SQL log açık ve formatlanmış.
