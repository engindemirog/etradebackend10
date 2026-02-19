# Optimize Edilmiş Prompt Şablonları (Parametrik)

Bu dosya, yeni entity'ler eklerken kullanılabilecek parametrik prompt şablonlarını içerir.
`{Entity}` yerine entity adını (ör: Product, Category, Order), `{entity}` yerine küçük harfli versiyonunu yazın.
`{tableName}` yerine veritabanı tablo adını (ör: products, categories, orders) yazın.

---

## Prompt 1 — Teknoloji Stack Analizi (Tek seferlik)

```
pom.xml dosyasını analiz et. Kullanılan framework, dil, build tool, veritabanı, ORM, validasyon, test ve diğer dependency'leri listele. Kod yazma, dosya oluşturma. Sadece analiz yap ve sonucu raporla.
```

---

## Prompt 2 — Katmanlı Mimari Klasör Yapısı (Tek seferlik)

```
Katmanlı mimari için aşağıdaki paket yapısını oluştur. Sadece klasörleri oluştur, Java dosyası yazma.
- api/controllers
- business/abstracts, business/concretes, business/rules
- dataAccess/abstracts
- entities/concretes, entities/dtos/requests, entities/dtos/responses
```

---

## Prompt 3 — BaseEntity Oluşturma (Tek seferlik)

```
BaseEntity sınıfını oluştur (`entities/concretes/BaseEntity.java`):
- @MappedSuperclass, abstract sınıf
- Ortak alanlar: id (int, auto increment), createdDate (LocalDateTime, @PrePersist), updatedDate (LocalDateTime, @PreUpdate), deletedDate (LocalDateTime), isActive (boolean, default true)
- Lombok kullanma. Getter/setter/constructor elle yaz.
- Diğer katmanları kodlama.
```

---

## Prompt 4 — Entity Oluşturma (Her entity için tekrarlanır)

```
{Entity} entity sınıfını oluştur (`entities/concretes/{Entity}.java`):
- BaseEntity'den miras alır
- @Entity, @Table(name = "{tableName}")
- Alanlar: {alan1}, {alan2}, {alan3}, ...
- @Column ile snake_case kolon isimleri kullan
- Lombok kullanma. Getter/setter/constructor elle yaz.
- Diğer katmanları kodlama.
```

---

## Prompt 5 — DataAccess Katmanı (Her entity için tekrarlanır)

```
{Entity} için repository interface'ini oluştur (`dataAccess/abstracts/{Entity}Repository.java`).
JpaRepository<{Entity}, Integer> extend etsin.
İş kurallarında kullanılacak derived query metotlarını ekle: existsByName, existsByNameAndIdNot.
Diğer katmanları kodlama.
```

---

## Prompt 6 — Business Katmanı + DTO'lar (Her entity için tekrarlanır)

```
{Entity} için business katmanını ve DTO'ları oluştur. Diğer katmanları kodlama.

**Request DTO'lar** (`entities/dtos/requests/{entity}/`):
- Create{Entity}Request — {alan1}, {alan2}, {alan3}, ... Jakarta Validation kullan, mesajları Türkçe yaz.
- Update{Entity}Request — id dahil, aynı alanlar.

**Response DTO'lar** (`entities/dtos/responses/{entity}/`):
- GetAll{Entity}sResponse — id, {özet alanlar}
- GetById{Entity}Response — tüm alanlar + createdDate, updatedDate, isActive
- Created{Entity}Response — tüm alanlar + createdDate
- Updated{Entity}Response — tüm alanlar + updatedDate
- Deleted{Entity}Response — id, {tanımlayıcı alan}

**Business Rules** (`business/rules/{Entity}BusinessRules.java`):
- checkIf{Entity}ExistsById, checkIf{Entity}NameAlreadyExists, checkIf{Entity}NameAlreadyExistsForUpdate
- Kural ihlalinde RuntimeException fırlat (Türkçe mesaj)

**Service Interface** (`business/abstracts/{Entity}Service.java`):
- getAll, getById, add, update, delete

**Service Impl** (`business/concretes/{Entity}ServiceImpl.java`):
- Constructor injection, iş kurallarını metot başında çağır, manuel mapping yap.

Lombok kullanma. Tüm getter/setter/constructor elle yazılsın.
```

---

## Prompt 7 — Controller Katmanı (Her entity için tekrarlanır)

```
{Entity} için REST controller oluştur (`api/controllers/{Entity}sController.java`).
- @RestController, @RequestMapping("/api/{entity}s")
- GET / → getAll → HttpStatus.OK
- GET /{id} → getById → HttpStatus.OK
- POST / → add (@Valid) → HttpStatus.CREATED
- PUT / → update (@Valid) → HttpStatus.OK
- DELETE /{id} → delete → HttpStatus.OK
- Constructor injection ile {Entity}Service inject et.
- Lombok kullanma.
```

---

## Prompt 8 — Veritabanı Konfigürasyonu (Tek seferlik)

```
application.yaml dosyasını yapılandır:
- H2 file-based veritabanı: jdbc:h2:file:./data/etradedb
- H2 Console aktif: /h2-console
- JPA: ddl-auto=update, show-sql=true, format_sql=true
- Server port: 8080
```

---

## Prompt 9 — Swagger Entegrasyonu (Tek seferlik)

```
pom.xml'e springdoc-openapi-starter-webmvc-ui dependency'sini ekle.
```

---

## Prompt 10 — Agent Talimat Dosyası (Tek seferlik)

```
Projedeki tüm kodları ve mimariyi analiz et. .github/copilot-instructions.md dosyası oluştur. İçeriğe şunları dahil et: teknoloji stack, katmanlı mimari yapısı, Lombok yasağı, constructor injection kuralı, BaseEntity miras yapısı, DTO isimlendirme kuralları, repository/service/controller kodlama standartları, yeni entity ekleme akışı (checklist), genel kurallar (dil, mapper, exception, soft delete, API prefix).
```

---

## Hızlı Kullanım — Tek Prompt ile Komple Entity Ekleme

Aşağıdaki prompt, yukarıdaki 4-5-6-7 numaralı prompt'ları tek seferde çalıştırır:

```
{Entity} nesnesi için tüm katmanları oluştur. copilot-instructions.md dosyasındaki kodlama standartlarına uy.

**Entity:** {Entity} — @Table(name = "{tableName}")
- Alanlar: {alan1} (String), {alan2} (double), {alan3} (int), ...

Oluşturulacak dosyalar:
1. Entity → entities/concretes/{Entity}.java (BaseEntity'den miras)
2. Request DTO'lar → entities/dtos/requests/{entity}/Create{Entity}Request.java, Update{Entity}Request.java
3. Response DTO'lar → entities/dtos/responses/{entity}/GetAll{Entity}sResponse.java, GetById{Entity}Response.java, Created{Entity}Response.java, Updated{Entity}Response.java, Deleted{Entity}Response.java
4. Repository → dataAccess/abstracts/{Entity}Repository.java
5. Business Rules → business/rules/{Entity}BusinessRules.java
6. Service Interface → business/abstracts/{Entity}Service.java
7. Service Impl → business/concretes/{Entity}ServiceImpl.java
8. Controller → api/controllers/{Entity}sController.java
```

---

## Örnek — Category Entity Ekleme

```
Category nesnesi için tüm katmanları oluştur. copilot-instructions.md dosyasındaki kodlama standartlarına uy.

**Entity:** Category — @Table(name = "categories")
- Alanlar: name (String), description (String)

Oluşturulacak dosyalar:
1. Entity → entities/concretes/Category.java (BaseEntity'den miras)
2. Request DTO'lar → entities/dtos/requests/category/CreateCategoryRequest.java, UpdateCategoryRequest.java
3. Response DTO'lar → entities/dtos/responses/category/GetAllCategoriesResponse.java, GetByIdCategoryResponse.java, CreatedCategoryResponse.java, UpdatedCategoryResponse.java, DeletedCategoryResponse.java
4. Repository → dataAccess/abstracts/CategoryRepository.java
5. Business Rules → business/rules/CategoryBusinessRules.java
6. Service Interface → business/abstracts/CategoryService.java
7. Service Impl → business/concretes/CategoryServiceImpl.java
8. Controller → api/controllers/CategoriesController.java
```

---

## Prompt 11 — Business Katmanı Unit Test Yazımı (Her entity için tekrarlanır)

```
{Entity} için business katmanı unit testlerini yaz. Code coverage önemli. JaCoCo coverage raporu üret.

**Test edilecek sınıflar:**
1. `business/rules/{Entity}BusinessRules` → Tüm iş kuralı metotları (pozitif + negatif senaryolar)
2. `business/concretes/{Entity}ServiceImpl` → Tüm CRUD metotları (getAll, getById, add, update, delete)

**Test kuralları:**
- JUnit Jupiter + Mockito kullan
- @ExtendWith(MockitoExtension.class) ile test sınıflarını yapılandır
- Repository ve BusinessRules bağımlılıklarını @Mock ile mockla
- @InjectMocks ile test edilen sınıfı oluştur
- Her metot için en az bir pozitif ve bir negatif senaryo yaz
- Coverage %100 hedefle
```

---

## Prompt 12 — API (Controller) Katmanı Unit Test Yazımı (Her entity için tekrarlanır)

```
{Entity} için API controller unit testlerini yaz.

**Test edilecek sınıf:**
- `api/controllers/{Entity}sController` → Tüm endpoint'ler (GET, POST, PUT, DELETE)

**Test kuralları:**
- @WebMvcTest ile test sınıfını yapılandır
- MockMvc ile HTTP istekleri simüle et
- Service bağımlılığını @MockitoBean ile mockla
- Her endpoint için başarılı senaryo yaz
- Validasyon hata senaryoları yaz (@Valid ile tetiklenen hatalar)
- İş kuralı hata senaryoları yaz (BusinessException fırlatan durumlar)
- HTTP status kodlarını doğrula (200 OK, 201 Created, 400 Bad Request)
- Response body içeriğini doğrula (jsonPath ile)

**Spring Boot 4 notları:**
- @WebMvcTest import: org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
- @MockitoBean import: org.springframework.test.context.bean.override.mockito.MockitoBean
- ObjectMapper manuel oluşturulmalı (auto-configure edilmez)
```

---

## Prompt 13 — JaCoCo Code Coverage Konfigürasyonu (Tek seferlik)

```
pom.xml'e JaCoCo Maven plugin'ini ekle:
- Versiyon: 0.8.12
- prepare-agent ve report goal'larını tanımla
- Coverage raporunu target/site/jacoco/ altında üret
- Business katmanı için filter: com/turkcell/etradebackend10/business/**
```

---

## Prompt 14 — Global Exception Handler ve Hata Yönetimi (Tek seferlik)

```
Projeye global hata yönetimi ekle.

**Oluşturulacak dosyalar** (`business/exceptions/`):
1. BusinessException — RuntimeException'dan miras, Türkçe hata mesajı taşıyan özel exception
2. BusinessErrorResponse — İş kuralı hata yanıtı (status, message, timestamp)
3. ValidationErrorResponse — Validasyon hata yanıtı (status, message, errors map, timestamp)
4. GlobalExceptionHandler — @RestControllerAdvice ile:
   - BusinessException → 400 Bad Request + BusinessErrorResponse
   - MethodArgumentNotValidException → 400 Bad Request + ValidationErrorResponse (alan bazlı hatalar)
```
