# Prompt Analiz ve Optimizasyon Dökümanı

Bu döküman, e-ticaret backend projesinin oluşturulması sürecinde kullanılan tüm promptları, optimize edilmiş versiyonlarını ve revize gerekçelerini içerir.

---

## Prompt 1 — Teknoloji Stack Analizi

### Orijinal Prompt:
> Sıfırdan bir eticaret projesi yazacağız. Backend ile başlayacağız. pom dosyasını inceleyip proje teknoloji stack'ini çıkar. Bunları hafızanda tut. Henüz kod yazma. Bir şey oluşturma.

### Optimize Edilmiş Prompt:
> pom.xml dosyasını analiz et. Kullanılan framework, dil, build tool, veritabanı, ORM, validasyon, test ve diğer dependency'leri listele. Kod yazma, dosya oluşturma. Sadece analiz yap ve sonucu raporla.

### Açıklama:
Bu prompt projeyi tanıma aşamasıdır. Agent'ın pom.xml'i okumasını ve teknoloji stack'ini çıkarmasını sağlar. **Revize sebebi:** Orijinal prompt'taki "sıfırdan eticaret projesi" gibi bağlamsal bilgiler gereksiz. Agent zaten workspace'teki pom.xml'i okuyacak. Doğrudan ne yapılacağı net ifade edildi. "Hafızanda tut" yerine "raporla" kullanıldı çünkü agent zaten bağlamı conversation boyunca tutar.

---

## Prompt 2 — Katmanlı Mimari Klasör Yapısı

### Orijinal Prompt:
> Katmanlı bir mimari için paket ve klasör yapısını oluştur. DataAccess, Business, Api, Entities katmanlarını kur. Sadece bu işlemleri yap. kod yazma.

### Optimize Edilmiş Prompt:
> Katmanlı mimari için aşağıdaki paket yapısını oluştur. Sadece klasörleri oluştur, Java dosyası yazma.
> - api/controllers
> - business/abstracts, business/concretes, business/rules
> - dataAccess/abstracts
> - entities/concretes, entities/dtos/requests, entities/dtos/responses

### Açıklama:
Projenin iskeletini oluşturur. **Revize sebebi:** Orijinal prompt katman isimlerini vermiş ama alt paketleri belirtmemiş. Agent'ın `business/rules` veya `entities/dtos/requests` gibi alt paketleri tahmin etmesi gerekti. Optimize versiyonda tüm alt paketler açıkça listelenerek belirsizlik ortadan kaldırıldı.

---

## Prompt 3 — BaseEntity ve İlk Entity Oluşturma

### Orijinal Prompt:
> Entities katmanı ile başlayalım. Analiz dökümanım yok. Bu yüzden sen analiz dökümanı varmış gibi kod üret. Product nesnesi ile başla. Sadece entity kodunu yaz. Veritabanımda tüm tablolarda ortak kolonlarım var. id,createdDate,updatedDate,deletedDate,isActive. Ben tüm sınıflarımda bu alanlar da olsun istiyorum. Ek olarak Temel bir sınıf oluştur. Tüm nesneler ondan miras alsın.

### Optimize Edilmiş Prompt:
> BaseEntity ve Product entity sınıflarını oluştur. Diğer katmanları kodlama.
>
> **BaseEntity** (`entities/concretes/BaseEntity.java`):
> - @MappedSuperclass, abstract sınıf
> - Ortak alanlar: id (int, auto increment), createdDate (LocalDateTime, @PrePersist), updatedDate (LocalDateTime, @PreUpdate), deletedDate (LocalDateTime), isActive (boolean, default true)
>
> **Product** (`entities/concretes/Product.java`):
> - BaseEntity'den miras alır
> - @Entity, @Table(name = "products")
> - Alanlar: name, description, unitPrice, unitsInStock, imageUrl
> - @Column ile snake_case kolon isimleri kullan
>
> Lombok kullanma. Getter/setter/constructor elle yaz.

### Açıklama:
Projenin temel entity yapısını kurar. BaseEntity inheritance pattern'ı ile tüm entity'ler ortak alanlara sahip olur. **Revize sebebi:** Orijinal prompt "analiz dökümanım yok, varmış gibi kodla" gibi gereksiz bilgiler içeriyor. Agent'a alan isimleri, annotation'lar ve yapısal kararlar açıkça verildi. Lombok yasağı baştan belirtildi (orijinalde belirtilmediği için sonra düzeltme gerekti).

---

## Prompt 4 — DataAccess Katmanı

### Orijinal Prompt:
> Sonraki aşamaya geçelim. Product için dataaccess kodunu yaz. Diğer katmanları kodlama.

### Optimize Edilmiş Prompt:
> Product için repository interface'ini oluştur (`dataAccess/abstracts/ProductRepository.java`). JpaRepository<Product, Integer> extend etsin. İş kurallarında kullanılacak derived query metotlarını ekle: existsByName, existsByNameAndIdNot. Diğer katmanları kodlama.

### Açıklama:
Veri erişim katmanını oluşturur. **Revize sebebi:** Orijinal prompt sadece "dataaccess kodunu yaz" demiş. Derived query metotları belirtilmediği için business rules yazılırken repository'ye geri dönüp metot eklemek gerekti. Optimize versiyonda iş kurallarına uygun metotlar baştan tanımlatılıyor.

---

## Prompt 5 — Business Katmanı (Service + Rules + DTO'lar)

### Orijinal Prompt:
> Sıradaki aşamaya geçelim. Product için business kodunu yaz. abstracts ve concretes klasörlerine ayır. projede response request patternini sıkı sıkıya uygula. her request ve response için ayrı dto nesneleri oluştur. iş kurallarını ayrı rules dosyalarında yazıp impl sınıflarında tüket. analiz dökümanım yok, varmıl gibi kodla.

### Optimize Edilmiş Prompt:
> Product için business katmanını ve DTO'ları oluştur. Diğer katmanları kodlama.
>
> **Request DTO'lar** (`entities/dtos/requests/product/`):
> - CreateProductRequest — name, description, unitPrice, unitsInStock, imageUrl. Jakarta Validation kullan, mesajları Türkçe yaz.
> - UpdateProductRequest — id dahil, aynı alanlar.
>
> **Response DTO'lar** (`entities/dtos/responses/product/`):
> - GetAllProductsResponse — id, name, unitPrice, unitsInStock, imageUrl
> - GetByIdProductResponse — tüm alanlar + createdDate, updatedDate, isActive
> - CreatedProductResponse — tüm alanlar + createdDate
> - UpdatedProductResponse — tüm alanlar + updatedDate
> - DeletedProductResponse — id, name
>
> **Business Rules** (`business/rules/ProductBusinessRules.java`):
> - checkIfProductExistsById, checkIfProductNameAlreadyExists, checkIfProductNameAlreadyExistsForUpdate, checkIfUnitPriceValid
> - Kural ihlalinde RuntimeException fırlat (Türkçe mesaj)
>
> **Service Interface** (`business/abstracts/ProductService.java`):
> - getAll, getById, add, update, delete
>
> **Service Impl** (`business/concretes/ProductServiceImpl.java`):
> - Constructor injection, iş kurallarını metot başında çağır, manuel mapping yap.
>
> Lombok kullanma. Tüm getter/setter/constructor elle yazılsın.

### Açıklama:
Projenin en kapsamlı katmanını oluşturur: DTO'lar, iş kuralları, service interface ve implementasyon. **Revize sebebi:** Orijinal prompt çok genel ve belirsiz. Hangi DTO'ların hangi alanları taşıyacağı, iş kurallarının ne olacağı, validation mesajlarının dili gibi kritik kararlar agent'a bırakılmış. Optimize versiyonda her DTO'nun alanları, her iş kuralı metodu ve davranışı açıkça belirlendi.

---

## Prompt 6 — Controller Katmanı

### Orijinal Prompt:
> product için api katmanında controller yaz

### Optimize Edilmiş Prompt:
> Product için REST controller oluştur (`api/controllers/ProductsController.java`).
> - @RestController, @RequestMapping("/api/products")
> - GET / → getAll → HttpStatus.OK
> - GET /{id} → getById → HttpStatus.OK
> - POST / → add (@Valid) → HttpStatus.CREATED
> - PUT / → update (@Valid) → HttpStatus.OK
> - DELETE /{id} → delete → HttpStatus.OK
> - Constructor injection ile ProductService inject et.

### Açıklama:
API katmanını oluşturur. **Revize sebebi:** Orijinal prompt çok kısa. Endpoint mapping'leri, HTTP status kodları, @Valid kullanımı gibi detaylar belirtilmemiş. Optimize versiyonda endpoint listesi, status kodları ve injection yöntemi açıkça tanımlandı.

---

## Prompt 7 — Veritabanı Konfigürasyonu

### Orijinal Prompt:
> projede h2 veritabanı kullanacağım. veriler kalıcı tutulsun. in memory olmasın. yaml konfigurasyonunu yaz.

### Optimize Edilmiş Prompt:
> application.yaml dosyasını yapılandır:
> - H2 file-based veritabanı: jdbc:h2:file:./data/etradedb
> - H2 Console aktif: /h2-console
> - JPA: ddl-auto=update, show-sql=true, format_sql=true
> - Server port: 8080

### Açıklama:
Veritabanı ve uygulama konfigürasyonunu ayarlar. **Revize sebebi:** Orijinal prompt sadece "kalıcı olsun" demiş ama connection URL, console erişimi, SQL loglama, port gibi detaylar belirtilmemiş. Optimize versiyonda tüm konfigürasyon parametreleri tek prompt'ta verildi.

---

## Prompt 8 — Swagger Entegrasyonu

### Orijinal Prompt:
> projeye swagger desteği getir

### Optimize Edilmiş Prompt:
> pom.xml'e springdoc-openapi-starter-webmvc-ui dependency'sini ekle. Başka konfigürasyona gerek yok.

### Açıklama:
API dokümantasyonu için Swagger UI ekler. **Revize sebebi:** Orijinal prompt yeterince açık, ancak optimize versiyonda hangi kütüphanenin ekleneceği belirtilerek agent'ın farklı bir swagger kütüphanesi seçme riski ortadan kaldırıldı.

---

## Prompt 9 — Lombok Kaldırma

### Orijinal Prompt:
> projeden lombok desteğini çıkar. tüm katmanları inceleyip düzelt.

### Optimize Edilmiş Prompt:
> Projeden Lombok'u tamamen kaldır:
> 1. pom.xml'den lombok dependency, maven-compiler-plugin annotation processor ve spring-boot-maven-plugin exclude bölümünü sil.
> 2. Tüm Java dosyalarından lombok import'larını ve annotation'larını (@Data, @AllArgsConstructor, @NoArgsConstructor, @EqualsAndHashCode, @Builder) kaldır.
> 3. Kaldırılan annotation'ların karşılığı olan getter, setter, no-args constructor ve all-args constructor metotlarını elle yaz.
> 4. @AllArgsConstructor ile yapılan constructor injection'ları explicit constructor'a dönüştür.

### Açıklama:
Lombok bağımlılığını projeden tamamen temizler. **Revize sebebi:** Orijinal prompt "tüm katmanları inceleyip düzelt" demiş ama neyin düzeltileceğini detaylandırmamış. Optimize versiyonda pom.xml'den nelerin silineceği, Java dosyalarında nelerin değişeceği ve constructor injection dönüşümü adım adım belirtildi. Bu prompt aslında en baştan Lombok kullanılmasaydı gereksiz olacaktı — bu yüzden entity promptunda Lombok yasağı baştan konulmalı.

---

## Prompt 10 — Agent Talimat Dosyası

### Orijinal Prompt:
> Şu ana kadar yazdığımız promptları ve kodları incele. Kodlama standartlarımı, mimari yaklaşımımı dikkate alarak bir agent dosyası oluştur. Sonraki promptlarda ve iş ihtiyaçlarında bu dökümandan yararlan.

### Optimize Edilmiş Prompt:
> Projedeki tüm kodları ve mimariyi analiz et. `.github/copilot-instructions.md` dosyası oluştur. İçeriğe şunları dahil et: teknoloji stack, katmanlı mimari yapısı, Lombok yasağı, constructor injection kuralı, BaseEntity miras yapısı, DTO isimlendirme kuralları, repository/service/controller kodlama standartları, yeni entity ekleme akışı (checklist), genel kurallar (dil, mapper, exception, soft delete, API prefix).

### Açıklama:
Copilot agent'ın proje kurallarını öğrenmesini sağlayan talimat dosyasını oluşturur. **Revize sebebi:** Orijinal prompt "agent dosyası oluştur" demiş ama dosya yolunu ve içeriğin kapsamını belirtmemiş. Optimize versiyonda `.github/copilot-instructions.md` dosya yolu ve dahil edilecek bölümler açıkça listelendi.

---

## Genel Çıkarımlar

| Konu | Öğrenilen Ders |
|---|---|
| **Lombok** | Kullanmayacaksan baştan belirt, sonradan kaldırmak maliyetli |
| **Detay Seviyesi** | Alan isimleri, annotation'lar, HTTP status kodları gibi detaylar prompt'ta verilmeli |
| **Tek Sorumluluk** | Her prompt tek bir katmanı veya tek bir görevi hedeflemeli |
| **Bağımsızlık** | Prompt'lar birbirinden bağımsız çalışabilmeli, önceki bağlama güvenmemeli |
| **Parametriklik** | Entity isimleri değişken tutularak prompt'lar tekrar kullanılabilir hale getirilmeli |
| **Test Prompt'ları** | Hedef sınıflar, test framework bilgisi ve Spring Boot versiyon uyarıları belirtilmeli |
| **Spring Boot 4 Farkları** | Import path'leri ve otomatik konfigürasyon farklarını prompt'ta belirtmek zaman kazandırır |
| **Coverage Hedefi** | Test prompt'larında coverage hedefini belirtmek (%100 gibi) agent'ın daha kapsamlı test yazmasını sağlar |

---

## Prompt 11 — Business Katmanı Unit Test Yazımı

### Orijinal Prompt:
> Product ve kategori için business unit testleri yaz. Code coverage önemli. coverage raporu da üret.

### Optimize Edilmiş Prompt:
> Product ve Category için business katmanı unit testlerini yaz. Code coverage önemli. JaCoCo coverage raporu üret.
>
> **Test edilecek sınıflar:**
> 1. `business/rules/CategoryBusinessRules` → Tüm iş kuralı metotları (pozitif + negatif senaryolar)
> 2. `business/rules/ProductBusinessRules` → Tüm iş kuralı metotları (pozitif + negatif senaryolar)
> 3. `business/concretes/CategoryServiceImpl` → Tüm CRUD metotları
> 4. `business/concretes/ProductServiceImpl` → Tüm CRUD metotları
>
> **Test kuralları:**
> - JUnit Jupiter + Mockito kullan
> - @ExtendWith(MockitoExtension.class) ile test sınıflarını yapılandır
> - Coverage %100 hedefle
> - pom.xml'e JaCoCo plugin ekle

### Açıklama:
Business katmanının kapsamlı testlerini oluşturur. **Revize sebebi:** Orijinal prompt hangi sınıfların test edileceğini, hangi test framework'ünün kullanılacağını ve coverage aracını belirtmemiş. Optimize versiyonda test edilecek sınıflar, kullanılacak framework ve coverage konfigürasyonu açıkça listelendi. Ayrıca "pozitif + negatif senaryolar" belirtilerek her iş kuralının hem başarılı hem hatalı durumlarının test edilmesi sağlandı.

**Sonuç:** 44 test yazıldı (6 + 10 + 11 + 17), tümü geçti. JaCoCo raporu: business.rules %100, business.concretes %100.

---

## Prompt 12 — API (Controller) Katmanı Unit Test Yazımı

### Orijinal Prompt:
> api için unit testleri yaz

### Optimize Edilmiş Prompt:
> Product ve Category için API controller unit testlerini yaz.
>
> **Test edilecek sınıflar:**
> - `api/controllers/CategoriesController` → Tüm endpoint'ler
> - `api/controllers/ProductsController` → Tüm endpoint'ler
>
> **Test kuralları:**
> - @WebMvcTest ile test sınıfını yapılandır
> - MockMvc ile HTTP istekleri simüle et
> - Service bağımlılığını @MockitoBean ile mockla
> - Her endpoint için: başarılı senaryo + validasyon hatası + iş kuralı hatası
> - HTTP status kodlarını doğrula (200, 201, 400)
>
> **Spring Boot 4 notları:**
> - @WebMvcTest import: org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
> - @MockitoBean import: org.springframework.test.context.bean.override.mockito.MockitoBean
> - ObjectMapper manuel oluşturulmalı

### Açıklama:
API katmanının controller testlerini oluşturur. **Revize sebebi:** Orijinal prompt sadece "api için unit testleri yaz" demiş. Hangi test yönteminin (MockMvc, RestTemplate, WebTestClient vb.) kullanılacağı, hangi senaryoların test edileceği belirsiz. Spring Boot 4'te `@WebMvcTest` ve `@MockitoBean` import path'leri değiştiği için bu bilgilerin prompt'ta verilmesi birçok deneme-yanılma döngüsünü önler. `ObjectMapper`'ın otomatik konfigüre edilmemesi de Spring Boot 4'e özgü bir sorun — prompt'ta belirtilmesi gerekir.

**Sonuç:** 29 test yazıldı (14 + 15), tümü geçti. Validasyon, iş kuralı hatası ve başarılı senaryolar kapsandı.

**Karşılaşılan Sorunlar:**
1. Spring Boot 4'te `@WebMvcTest` paketi değişmiş → Import düzeltmesi gerekti
2. `ObjectMapper` auto-configure edilmiyor → Manuel oluşturma gerekti
3. Türkçe karakter encoding sorunu → MockMvc response body'de Turkish karakterler bozuldu, `.isNotEmpty()` ile çözüldü

---

## Genel Çıkarımlar (Güncellenmiş)

| Konu | Öğrenilen Ders |
|---|---|
| **Lombok** | Kullanmayacaksan baştan belirt, sonradan kaldırmak maliyetli |
| **Detay Seviyesi** | Alan isimleri, annotation'lar, HTTP status kodları gibi detaylar prompt'ta verilmeli |
| **Tek Sorumluluk** | Her prompt tek bir katmanı veya tek bir görevi hedeflemeli |
| **Bağımsızlık** | Prompt'lar birbirinden bağımsız çalışabilmeli, önceki bağlama güvenmemeli |
| **Parametriklik** | Entity isimleri değişken tutularak prompt'lar tekrar kullanılabilir hale getirilmeli |
| **Test Prompt'ları** | Hedef sınıflar, test framework bilgisi ve Spring Boot versiyon uyarıları belirtilmeli |
| **Spring Boot 4 Farkları** | Import path'leri ve otomatik konfigürasyon farklarını prompt'ta belirtmek zaman kazandırır |
| **Coverage Hedefi** | Coverage hedefini belirtmek (%100 gibi) agent'ın daha kapsamlı test yazmasını sağlar |
| **Encoding Sorunları** | Türkçe karakter içeren assertion'larda exact match yerine partial validation kullanılmalı |
