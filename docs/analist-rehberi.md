# Projeye Yeni Katılan Analist Rehberi

**Proje:** etradebackend10 — E-Ticaret Backend API  
**Hedef Kitle:** Kod tecrübesi sınırlı, projeye sonradan dahil olan analistler  
**Tarih:** 19 Şubat 2026

---

## 1. Projeyi Tanımaya Nereden Başlamalı?

### 1.1 Önce Bu Dokümanları Oku (Önerilen Sıra)

| Sıra | Doküman | Ne Öğrenirsin? | Tahmini Süre |
|------|---------|-----------------|--------------|
| 1 | `docs/gereksinim-dokumani.md` | Sistem ne yapıyor, hangi modüller var, iş kuralları neler | 20 dk |
| 2 | `docs/api-dokumantasyonu.md` | API endpoint'leri, request/response formatları, hata yapıları | 30 dk |
| 3 | `docs/mimari-dokumantasyon.md` | Katmanlar nasıl çalışıyor, kod nerede duruyor | 15 dk |
| 4 | `.github/copilot-instructions.md` | Kodlama kuralları ve standartlar (AI agent için yazılmış ama herkes için geçerli) | 15 dk |

> **İpucu:** Dokümanları okurken yanına bir kağıt al. Anlamadığın terimleri not et, sonra bu rehberin "Terimler Sözlüğü" bölümüne bak.

### 1.2 Projeyi Çalıştır ve Gör

Projeyi görmeden anlamak zor. Aşağıdaki adımları takip et:

1. **Projeyi çalıştır:**
   ```
   .\mvnw.cmd spring-boot:run
   ```

2. **Swagger UI'ı aç:** Tarayıcıda `http://localhost:8080/swagger-ui.html` adresine git. Tüm endpoint'leri burada görebilir ve deneyebilirsin.

3. **H2 Console'u aç:** `http://localhost:8080/h2-console` adresine git. JDBC URL olarak `jdbc:h2:file:./data/etradedb` yaz. Veritabanındaki tabloları ve verileri görebilirsin.

4. **Bir API denemesi yap:** Swagger UI'dan `POST /api/categories` ile yeni bir kategori ekle:
   ```json
   {
     "name": "Elektronik",
     "description": "Elektronik ürünler"
   }
   ```
   Sonra `GET /api/categories` ile eklediğin kategoriyi listele.

---

## 2. Projenin Yapısını Anlamak

### 2.1 Basit Düşün: Bir İstek Nasıl İşlenir?

Bir kullanıcı "yeni ürün ekle" dediğinde:

```
Kullanıcı (Swagger/Frontend)
        │
        ▼
   Controller          → HTTP isteğini karşılar ("POST /api/products geldi")
        │
        ▼
   Service (İş Katmanı) → İş kurallarını kontrol eder ("Bu isimde ürün var mı?")
        │
        ▼
   Repository          → Veritabanına kaydeder ("INSERT INTO products...")
        │
        ▼
   Veritabanı (H2)     → Veri kalıcı olarak saklanır
```

**Önemli:** Veri her zaman yukarıdan aşağıya akar. Controller doğrudan veritabanına erişmez, hep Service üzerinden gider.

### 2.2 Klasör Yapısını Ezberle

```
src/main/java/com/turkcell/etradebackend10/
│
├── api/controllers/          ← API endpoint'leri BuRADA
│   ├── CategoriesController     "GET /api/categories" gibi URL'ler
│   └── ProductsController       "POST /api/products" gibi URL'ler
│
├── business/                 ← İŞ MANTIKLARI BURADA
│   ├── abstracts/               Service arayüzleri (ne yapılacağının listesi)
│   ├── concretes/               Service gerçek kodları (nasıl yapılacağı)
│   ├── exceptions/              Hata yönetimi sınıfları
│   └── rules/                   İş kuralları ("ürün adı tekrar edemez" gibi)
│
├── dataAccess/abstracts/     ← VERİTABANI ERİŞİMİ BURADA
│   ├── CategoryRepository       Kategori tablosu işlemleri
│   └── ProductRepository        Ürün tablosu işlemleri
│
├── entities/                 ← VERİ MODELLERİ BURADA
│   ├── concretes/               Veritabanı tabloları (Product, Category)
│   └── dtos/                    Veri transfer nesneleri
│       ├── requests/            Kullanıcıdan gelen veriler
│       └── responses/           Kullanıcıya dönen veriler
│
└── config/                   ← AYARLAR BURADA
    └── CorsConfig               Frontend bağlantı izinleri
```

### 2.3 Dosya İsimlendirme Kalıpları

Bu kalıpları öğrenirsen her yeni entity için dosyaların nerede olduğunu tahmin edebilirsin:

| Ne arıyorsun? | Dosya ismi kalıbı | Örnek |
|----------------|-------------------|-------|
| Veritabanı tablosu | `{Entity}.java` | `Product.java` |
| API endpoint'leri | `{Entity}sController.java` | `ProductsController.java` |
| İş mantığı arayüzü | `{Entity}Service.java` | `ProductService.java` |
| İş mantığı kodu | `{Entity}ServiceImpl.java` | `ProductServiceImpl.java` |
| İş kuralları | `{Entity}BusinessRules.java` | `ProductBusinessRules.java` |
| Veritabanı erişimi | `{Entity}Repository.java` | `ProductRepository.java` |
| Ekleme isteği | `Create{Entity}Request.java` | `CreateProductRequest.java` |
| Güncelleme isteği | `Update{Entity}Request.java` | `UpdateProductRequest.java` |
| Listeleme yanıtı | `GetAll{Entity}sResponse.java` | `GetAllProductsResponse.java` |
| Detay yanıtı | `GetById{Entity}Response.java` | `GetByIdProductResponse.java` |

---

## 3. Modülleri Anlamak

### 3.1 Şu An Var Olan Modüller

| Modül | Tablo | Endpoint Prefix | İlişki |
|-------|-------|-----------------|--------|
| **Kategori** | `categories` | `/api/categories` | Bir kategoride birden fazla ürün olabilir |
| **Ürün** | `products` | `/api/products` | Her ürün bir kategoriye bağlıdır |

### 3.2 Her Modülde Yapılabilen İşlemler (CRUD)

Her modül aynı 5 işlemi destekler:

| İşlem | HTTP Metodu | Açıklama | Örnek |
|-------|-------------|----------|-------|
| **Listeleme** | `GET /api/products` | Tüm kayıtları getirir | Tüm ürünleri listele |
| **Detay** | `GET /api/products/{id}` | Tek kayıt getirir | 5 numaralı ürünü getir |
| **Ekleme** | `POST /api/products` | Yeni kayıt oluşturur | Yeni ürün ekle |
| **Güncelleme** | `PUT /api/products` | Mevcut kaydı günceller | Ürün bilgilerini değiştir |
| **Silme** | `DELETE /api/products/{id}` | Kaydı siler (soft delete) | 5 numaralı ürünü sil |

> **Soft Delete nedir?** Kayıt veritabanından gerçekten silinmez. `deletedDate` alanı doldurulur ve `isActive` `false` yapılır. Böylece veri kaybı önlenir.

---

## 4. İş Kurallarını Anlamak

İş kuralları, "bu işlem yapılabilir mi?" sorusunu yanıtlar. Her kuralın bir kodu vardır.

### 4.1 Kategori İş Kuralları

| Kod | Ne kontrol eder? | Ne zaman çalışır? | Hata mesajı |
|-----|-------------------|--------------------|-------------|
| BRL-CAT-001 | Bu ID'de kategori var mı? | Detay, güncelleme, silme | "Kategori bulunamadı. ID: 5" |
| BRL-CAT-002 | Bu isimde başka kategori var mı? | Ekleme | "Bu kategori adı zaten mevcut: Elektronik" |
| BRL-CAT-003 | Güncelleme sırasında isim çakışıyor mu? | Güncelleme | "Bu kategori adı zaten başka bir kategoriye ait: Giyim" |

### 4.2 Ürün İş Kuralları

| Kod | Ne kontrol eder? | Ne zaman çalışır? | Hata mesajı |
|-----|-------------------|--------------------|-------------|
| BRL-PRD-001 | Bu ID'de ürün var mı? | Detay, güncelleme, silme | "Ürün bulunamadı. ID: 10" |
| BRL-PRD-002 | Bu isimde başka ürün var mı? | Ekleme | "Bu ürün adı zaten mevcut: iPhone 15" |
| BRL-PRD-003 | Güncelleme sırasında isim çakışıyor mu? | Güncelleme | "Bu ürün adı zaten başka bir ürüne ait: iPhone 15" |
| BRL-PRD-004 | Fiyat 0'dan küçük mü? | Ekleme, güncelleme | "Birim fiyat 0'dan küçük olamaz." |
| BRL-PRD-005 | Bağlandığı kategori var mı? | Ekleme, güncelleme | "Kategori bulunamadı. ID: 99" |

### 4.3 İş Kuralı ile Validasyon Farkı

Bu ikisi sık karıştırılır:

| Özellik | Validasyon | İş Kuralı |
|---------|------------|-----------|
| **Ne kontrol eder?** | Verinin formatı doğru mu? | Verinin iş mantığına uygun mu? |
| **Örnek** | "Ürün adı boş olamaz" | "Bu ürün adı zaten kayıtlı" |
| **Nerede çalışır?** | DTO (Request) üzerinde | Service içinde (veritabanı sorgusuyla) |
| **Veritabanına bakar mı?** | Hayır | Evet |
| **Teknoloji** | `@NotBlank`, `@Min`, `@Size` | `ProductBusinessRules` sınıfı |

---

## 5. Hata Yapılarını Anlamak

API iki tür hata döndürür. Frontend geliştirici için bu ayrımı bilmek önemlidir:

### 5.1 İş Kuralı Hatası

Veritabanıyla ilgili bir kontrol başarısız olduğunda:

```json
{
  "status": 400,
  "message": "Bu ürün adı zaten mevcut: iPhone 15",
  "timestamp": "2026-02-19T14:30:00"
}
```

### 5.2 Validasyon Hatası

Gönderilen veri formatı yanlış olduğunda (birden fazla hata olabilir):

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

### 5.3 Nasıl Ayırt Edilir?

- Yanıtta `errors` alanı **varsa** → Validasyon hatası (birden fazla alan hatalı olabilir)
- Yanıtta `errors` alanı **yoksa** → İş kuralı hatası (tek bir mesaj döner)

---

## 6. Validasyon Kuralları Referans Tablosu

### 6.1 Kategori Alanları

| Alan | Zorunlu mu? | Kural | Hata Mesajı |
|------|-------------|-------|-------------|
| `name` | Evet | Boş olamaz, 2-100 karakter | "Kategori adı boş olamaz." / "...2 ile 100 karakter arasında olmalıdır." |
| `description` | Hayır | En fazla 500 karakter | "Açıklama en fazla 500 karakter olabilir." |
| `id` (güncelleme) | Evet | Boş olamaz | "Kategori ID boş olamaz." |

### 6.2 Ürün Alanları

| Alan | Zorunlu mu? | Kural | Hata Mesajı |
|------|-------------|-------|-------------|
| `name` | Evet | Boş olamaz, 2-100 karakter | "Ürün adı boş olamaz." / "...2 ile 100 karakter arasında olmalıdır." |
| `description` | Hayır | En fazla 500 karakter | "Açıklama en fazla 500 karakter olabilir." |
| `unitPrice` | Evet | Boş olamaz, 0'dan küçük olamaz | "Birim fiyat boş olamaz." / "...0'dan küçük olamaz." |
| `unitsInStock` | Hayır | 0'dan küçük olamaz | "Stok adedi 0'dan küçük olamaz." |
| `categoryId` | Evet | Boş olamaz | "Kategori ID boş olamaz." |
| `imageUrl` | Hayır | — | — |
| `id` (güncelleme) | Evet | Boş olamaz | "Ürün ID boş olamaz." |

---

## 7. Test Durumu

Proje kapsamlı şekilde test edilmiştir:

| Katman | Ne test edildi? | Test Sayısı | Durum |
|--------|-----------------|-------------|-------|
| İş Kuralları | Tüm business rules metotları | 16 | ✅ Geçti |
| Servis | Tüm CRUD operasyonları | 28 | ✅ Geçti |
| API Controller | Tüm endpoint'ler + hata senaryoları | 29 | ✅ Geçti |
| **Toplam** | | **73** | **✅ Tümü Geçti** |

**Code Coverage:** Business katmanı %100 test kapsama oranına sahiptir.

---

## 8. Terimler Sözlüğü

Kod tecrübesi az olan analistler için sık kullanılan terimler:

| Terim | Basit Açıklama |
|-------|----------------|
| **API** | Uygulamaların birbiriyle konuşma yolu. "Bana şu veriyi ver" veya "bu veriyi kaydet" gibi istekler |
| **Endpoint** | API'nin bir adresi. Örn: `/api/products` bir endpoint'tir |
| **REST** | API tasarım standardı. GET=getir, POST=ekle, PUT=güncelle, DELETE=sil |
| **CRUD** | Create (oluştur), Read (oku), Update (güncelle), Delete (sil) — temel 4 işlem |
| **DTO** | Data Transfer Object. Veriyi taşıyan basit bir kutu. İçinde sadece alanlar, getter ve setter var |
| **Request** | Kullanıcının API'ye gönderdiği veri (örn: yeni ürün bilgileri) |
| **Response** | API'nin kullanıcıya döndürdüğü veri (örn: oluşturulan ürünün bilgileri) |
| **Entity** | Veritabanındaki bir tablonun Java karşılığı. `Product` sınıfı = `products` tablosu |
| **Repository** | Veritabanıyla konuşan katman. SQL yazmadan veri çekme/kaydetme sağlar |
| **Service** | İş mantığının yazıldığı katman. "Fiyat negatif olamaz" gibi kurallar burada |
| **Controller** | HTTP isteklerini karşılayan katman. URL'leri dinler ve Service'e yönlendirir |
| **Validation** | Verinin formatını kontrol etme. "İsim boş olamaz", "fiyat 0'dan büyük olmalı" gibi |
| **Business Rules** | İş kuralları. Veritabanına bakarak kontrol yapar. "Bu isimde ürün var mı?" gibi |
| **Soft Delete** | Silme işleminde veriyi gerçekten silmek yerine "silindi" olarak işaretleme |
| **Constructor Injection** | Bir sınıfın ihtiyaç duyduğu araçları, oluşturulurken (constructor ile) alması |
| **Annotation** | `@` ile başlayan işaretler. Java'ya "bunu şöyle kullan" diyen etiketler |
| **Swagger** | API'yi görsel olarak gösteren ve deneme yapmanı sağlayan araç |
| **JSON** | Veri formatı. `{ "name": "Laptop", "price": 5000 }` şeklinde anahtar-değer çiftleri |
| **Maven** | Projeyi derleyen, bağımlılıkları yöneten araç. `pom.xml` dosyasını kullanır |
| **JPA/Hibernate** | Java ile veritabanı arasında köprü kuran teknoloji. SQL yazmadan tablo işlemleri |
| **H2** | Hafif, dosya tabanlı veritabanı. Kurulum gerektirmez, proje içinde çalışır |
| **JaCoCo** | Kodun ne kadarının test edildiğini gösteren araç (code coverage) |
| **MockMvc** | Controller testlerinde gerçek HTTP isteği göndermeden API'yi test etme aracı |
| **Mockito** | Test sırasında gerçek veritabanı yerine sahte (mock) veri kullanmayı sağlayan araç |

---

## 9. Sık Yapılan Hatalar ve Uyarılar

### ❌ Yapma

| Hata | Neden Yanlış? |
|------|---------------|
| Request/Response için aynı DTO'yu kullanmak | Her operasyonun farklı alanları olabilir. Ekleme'de ID yok, güncelleme'de var |
| Controller'dan doğrudan Repository çağırmak | Mimari katman atlama. Her zaman Service üzerinden git |
| Lombok (`@Data`, `@Getter`) kullanmak | Projede yasak. Tüm getter/setter elle yazılır |
| `@Autowired` ile field injection yapmak | Constructor injection kullanılır |
| Hata mesajlarını İngilizce yazmak | Bu projede validasyon ve hata mesajları Türkçe yazılır |
| Hard delete (fiziksel silme) yapmak | Soft delete kullanılır: `deletedDate` set et, `isActive = false` yap |

### ✅ Yap

| Doğru Pratik | Açıklama |
|--------------|----------|
| Her entity için ayrı DTO'lar oluştur | Create, Update, GetAll, GetById, Created, Updated, Deleted |
| İş kurallarını ayrı sınıfta yaz | `ProductBusinessRules` gibi. Service'i temiz tut |
| Validasyon mesajlarını Türkçe yaz | `"Ürün adı boş olamaz."` gibi |
| Yeni entity eklerken checklist takip et | Gereksinim dokümanındaki sırayı izle |
| Değişiklik sonrası testleri çalıştır | `.\mvnw.cmd test` komutuyla |

---

## 10. Yeni Entity Eklenirken Analist Checklist'i

Yeni bir modül (örn: "Sipariş") eklenecekse, analist olarak şunları hazırla:

### Analiz Aşaması
- [ ] Entity'nin alanlarını belirle (hangi bilgiler saklanacak?)
- [ ] Her alanın tipini belirle (metin, sayı, tarih?)
- [ ] Hangi alanlar zorunlu, hangileri opsiyonel?
- [ ] Validasyon kurallarını yaz (min/max uzunluk, min/max değer)
- [ ] Hata mesajlarını Türkçe olarak yaz
- [ ] Diğer entity'lerle ilişkileri belirle (Sipariş → Ürün, Sipariş → Müşteri vb.)

### İş Kuralları Aşaması
- [ ] Ekleme sırasında hangi kontroller yapılmalı?
- [ ] Güncelleme sırasında hangi kontroller yapılmalı?
- [ ] Silme sırasında hangi kontroller yapılmalı? (ilişkili kayıtlar var mı?)
- [ ] Her iş kuralı için Türkçe hata mesajı belirle

### Dokümantasyon Aşaması
- [ ] Gereksinim dokümanına yeni modülü ekle
- [ ] API dokümanına endpoint'leri ekle
- [ ] Request/Response örnekleri hazırla

### Doğrulama Aşaması
- [ ] Swagger UI'dan tüm endpoint'leri test et
- [ ] Validasyon hatalarını test et (boş alan, geçersiz değer)
- [ ] İş kuralı hatalarını test et (tekrar eden isim, olmayan ID)
- [ ] Soft delete'in çalıştığını doğrula

---

## 11. Faydalı Linkler ve Araçlar

| Araç | Adres | Ne İşe Yarar? |
|------|-------|---------------|
| Swagger UI | `http://localhost:8080/swagger-ui.html` | API'yi görsel olarak test et |
| H2 Console | `http://localhost:8080/h2-console` | Veritabanı tablolarını incele |
| JaCoCo Raporu | `target/site/jacoco/index.html` | Test kapsama raporunu gör |

### Komutlar

| Ne yapmak istiyorsun? | Komut |
|------------------------|-------|
| Projeyi çalıştır | `.\mvnw.cmd spring-boot:run` |
| Testleri çalıştır | `.\mvnw.cmd test` |
| Projeyi derle | `.\mvnw.cmd clean install` |
| Coverage raporu üret | `.\mvnw.cmd test` (otomatik üretilir) |

---

## 12. Sorularını Kime Sorabilirsin?

Bu doküman yeterli gelmezse:
1. **Önce dokümanları kontrol et** — Çoğu cevap `docs/` klasöründe
2. **Swagger UI'dan dene** — API davranışını canlı görmek çok şey öğretir
3. **Copilot Agent'a sor** — `.github/copilot-instructions.md` sayesinde proje kurallarını bilir
4. **Mevcut kodu incele** — Category modülü basit bir referans model. Product modülü ilişki (kategori bağlantısı) örneği içerir

> **Son söz:** Bu proje düzenli bir mimari yapıya sahip. Bir modülü anladığında diğerleri aynı kalıbı takip eder. Category modülünü baştan sona anla — geri kalanı aynı mantık.
