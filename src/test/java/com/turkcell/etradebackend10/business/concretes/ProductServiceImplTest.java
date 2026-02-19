package com.turkcell.etradebackend10.business.concretes;

import com.turkcell.etradebackend10.business.exceptions.BusinessException;
import com.turkcell.etradebackend10.business.rules.ProductBusinessRules;
import com.turkcell.etradebackend10.dataAccess.abstracts.CategoryRepository;
import com.turkcell.etradebackend10.dataAccess.abstracts.ProductRepository;
import com.turkcell.etradebackend10.entities.concretes.Category;
import com.turkcell.etradebackend10.entities.concretes.Product;
import com.turkcell.etradebackend10.entities.dtos.requests.product.CreateProductRequest;
import com.turkcell.etradebackend10.entities.dtos.requests.product.UpdateProductRequest;
import com.turkcell.etradebackend10.entities.dtos.responses.product.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductBusinessRules productBusinessRules;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private Category category;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        category = new Category("Elektronik", "Elektronik ürünler");
        category.setId(1);
        category.setCreatedDate(now);

        product = new Product("Laptop", "İyi bir laptop", 15000.0, 10, "laptop.jpg", category);
        product.setId(1);
        product.setCreatedDate(now);
        product.setActive(true);
    }

    // ==================== getAll ====================

    @Nested
    @DisplayName("getAll testleri")
    class GetAll {

        @Test
        @DisplayName("Ürünler varsa listeyi döndürmeli")
        void shouldReturnAllProducts() {
            Product product2 = new Product("Telefon", "Akıllı telefon", 10000.0, 20, "telefon.jpg", category);
            product2.setId(2);

            when(productRepository.findAll()).thenReturn(List.of(product, product2));

            List<GetAllProductsResponse> result = productService.getAll();

            assertEquals(2, result.size());
            assertEquals("Laptop", result.get(0).getName());
            assertEquals(1, result.get(0).getId());
            assertEquals(15000.0, result.get(0).getUnitPrice());
            assertEquals(10, result.get(0).getUnitsInStock());
            assertEquals("laptop.jpg", result.get(0).getImageUrl());
            assertEquals(1, result.get(0).getCategoryId());
            assertEquals("Elektronik", result.get(0).getCategoryName());

            assertEquals("Telefon", result.get(1).getName());
            assertEquals(2, result.get(1).getId());
            verify(productRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Ürün yoksa boş liste döndürmeli")
        void shouldReturnEmptyListWhenNoProducts() {
            when(productRepository.findAll()).thenReturn(Collections.emptyList());

            List<GetAllProductsResponse> result = productService.getAll();

            assertTrue(result.isEmpty());
            verify(productRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Kategorisi null olan ürünler için kategori bilgisi set edilmemeli")
        void shouldHandleProductWithNullCategory() {
            Product productWithoutCategory = new Product("Ürün", "Açıklama", 100.0, 5, "img.jpg", null);
            productWithoutCategory.setId(3);

            when(productRepository.findAll()).thenReturn(List.of(productWithoutCategory));

            List<GetAllProductsResponse> result = productService.getAll();

            assertEquals(1, result.size());
            assertEquals("Ürün", result.get(0).getName());
            assertEquals(0, result.get(0).getCategoryId());
            assertNull(result.get(0).getCategoryName());
            verify(productRepository, times(1)).findAll();
        }
    }

    // ==================== getById ====================

    @Nested
    @DisplayName("getById testleri")
    class GetById {

        @Test
        @DisplayName("Mevcut ID ile ürün döndürmeli")
        void shouldReturnProductWhenExists() {
            product.setUpdatedDate(now.plusHours(1));
            doNothing().when(productBusinessRules).checkIfProductExistsById(1);
            when(productRepository.findById(1)).thenReturn(Optional.of(product));

            GetByIdProductResponse result = productService.getById(1);

            assertEquals(1, result.getId());
            assertEquals("Laptop", result.getName());
            assertEquals("İyi bir laptop", result.getDescription());
            assertEquals(15000.0, result.getUnitPrice());
            assertEquals(10, result.getUnitsInStock());
            assertEquals("laptop.jpg", result.getImageUrl());
            assertEquals(1, result.getCategoryId());
            assertEquals("Elektronik", result.getCategoryName());
            assertEquals(now, result.getCreatedDate());
            assertEquals(now.plusHours(1), result.getUpdatedDate());
            assertTrue(result.isActive());
            verify(productBusinessRules, times(1)).checkIfProductExistsById(1);
            verify(productRepository, times(1)).findById(1);
        }

        @Test
        @DisplayName("Kategorisi null olan ürün için kategori bilgisi set edilmemeli")
        void shouldHandleProductWithNullCategoryOnGetById() {
            Product productNoCategory = new Product("Ürün", "Açıklama", 50.0, 3, "img.jpg", null);
            productNoCategory.setId(5);
            productNoCategory.setCreatedDate(now);
            productNoCategory.setActive(true);

            doNothing().when(productBusinessRules).checkIfProductExistsById(5);
            when(productRepository.findById(5)).thenReturn(Optional.of(productNoCategory));

            GetByIdProductResponse result = productService.getById(5);

            assertEquals(5, result.getId());
            assertEquals("Ürün", result.getName());
            assertEquals(0, result.getCategoryId());
            assertNull(result.getCategoryName());
        }

        @Test
        @DisplayName("Mevcut olmayan ID ile BusinessException fırlatmalı")
        void shouldThrowWhenProductDoesNotExist() {
            doThrow(new BusinessException("Ürün bulunamadı. ID: 99"))
                    .when(productBusinessRules).checkIfProductExistsById(99);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> productService.getById(99));
            assertEquals("Ürün bulunamadı. ID: 99", exception.getMessage());
            verify(productBusinessRules, times(1)).checkIfProductExistsById(99);
            verify(productRepository, never()).findById(anyInt());
        }
    }

    // ==================== add ====================

    @Nested
    @DisplayName("add testleri")
    class Add {

        @Test
        @DisplayName("Geçerli istek ile ürün eklemeli")
        void shouldAddProductSuccessfully() {
            CreateProductRequest request = new CreateProductRequest("Laptop", "İyi bir laptop", 15000.0, 10, "laptop.jpg", 1);

            doNothing().when(productBusinessRules).checkIfProductNameAlreadyExists("Laptop");
            doNothing().when(productBusinessRules).checkIfUnitPriceValid(15000.0);
            when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
            when(productRepository.save(any(Product.class))).thenReturn(product);

            CreatedProductResponse result = productService.add(request);

            assertNotNull(result);
            assertEquals(1, result.getId());
            assertEquals("Laptop", result.getName());
            assertEquals("İyi bir laptop", result.getDescription());
            assertEquals(15000.0, result.getUnitPrice());
            assertEquals(10, result.getUnitsInStock());
            assertEquals("laptop.jpg", result.getImageUrl());
            assertEquals(1, result.getCategoryId());
            assertEquals("Elektronik", result.getCategoryName());
            assertEquals(now, result.getCreatedDate());
            verify(productBusinessRules, times(1)).checkIfProductNameAlreadyExists("Laptop");
            verify(productBusinessRules, times(1)).checkIfUnitPriceValid(15000.0);
            verify(categoryRepository, times(1)).findById(1);
            verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("Mevcut ürün adı ile BusinessException fırlatmalı")
        void shouldThrowWhenProductNameExists() {
            CreateProductRequest request = new CreateProductRequest("Laptop", "Desc", 100.0, 5, "img.jpg", 1);

            doThrow(new BusinessException("Bu ürün adı zaten mevcut: Laptop"))
                    .when(productBusinessRules).checkIfProductNameAlreadyExists("Laptop");

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> productService.add(request));
            assertEquals("Bu ürün adı zaten mevcut: Laptop", exception.getMessage());
            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Negatif fiyat ile BusinessException fırlatmalı")
        void shouldThrowWhenPriceIsNegative() {
            CreateProductRequest request = new CreateProductRequest("Yeni Ürün", "Desc", -10.0, 5, "img.jpg", 1);

            doNothing().when(productBusinessRules).checkIfProductNameAlreadyExists("Yeni Ürün");
            doThrow(new BusinessException("Birim fiyat 0'dan küçük olamaz."))
                    .when(productBusinessRules).checkIfUnitPriceValid(-10.0);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> productService.add(request));
            assertEquals("Birim fiyat 0'dan küçük olamaz.", exception.getMessage());
            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Mevcut olmayan kategori ID ile RuntimeException fırlatmalı")
        void shouldThrowWhenCategoryDoesNotExist() {
            CreateProductRequest request = new CreateProductRequest("Yeni Ürün", "Desc", 100.0, 5, "img.jpg", 99);

            doNothing().when(productBusinessRules).checkIfProductNameAlreadyExists("Yeni Ürün");
            doNothing().when(productBusinessRules).checkIfUnitPriceValid(100.0);
            when(categoryRepository.findById(99)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> productService.add(request));
            assertEquals("Kategori bulunamadı. ID: 99", exception.getMessage());
            verify(productRepository, never()).save(any(Product.class));
        }
    }

    // ==================== update ====================

    @Nested
    @DisplayName("update testleri")
    class Update {

        @Test
        @DisplayName("Geçerli istek ile ürün güncellemeli")
        void shouldUpdateProductSuccessfully() {
            UpdateProductRequest request = new UpdateProductRequest(1, "Laptop Pro", "Güncellenmiş laptop", 20000.0, 15, "laptop2.jpg", 1);

            Product updatedProduct = new Product("Laptop Pro", "Güncellenmiş laptop", 20000.0, 15, "laptop2.jpg", category);
            updatedProduct.setId(1);
            updatedProduct.setUpdatedDate(now.plusHours(1));

            doNothing().when(productBusinessRules).checkIfProductExistsById(1);
            doNothing().when(productBusinessRules).checkIfProductNameAlreadyExistsForUpdate(1, "Laptop Pro");
            doNothing().when(productBusinessRules).checkIfUnitPriceValid(20000.0);
            when(productRepository.findById(1)).thenReturn(Optional.of(product));
            when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
            when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

            UpdatedProductResponse result = productService.update(request);

            assertNotNull(result);
            assertEquals(1, result.getId());
            assertEquals("Laptop Pro", result.getName());
            assertEquals("Güncellenmiş laptop", result.getDescription());
            assertEquals(20000.0, result.getUnitPrice());
            assertEquals(15, result.getUnitsInStock());
            assertEquals("laptop2.jpg", result.getImageUrl());
            assertEquals(1, result.getCategoryId());
            assertEquals("Elektronik", result.getCategoryName());
            assertEquals(now.plusHours(1), result.getUpdatedDate());
            verify(productBusinessRules, times(1)).checkIfProductExistsById(1);
            verify(productBusinessRules, times(1)).checkIfProductNameAlreadyExistsForUpdate(1, "Laptop Pro");
            verify(productBusinessRules, times(1)).checkIfUnitPriceValid(20000.0);
        }

        @Test
        @DisplayName("Mevcut olmayan ürün güncellenirken BusinessException fırlatmalı")
        void shouldThrowWhenUpdatingNonExistentProduct() {
            UpdateProductRequest request = new UpdateProductRequest(99, "Test", "Desc", 100.0, 5, "img.jpg", 1);

            doThrow(new BusinessException("Ürün bulunamadı. ID: 99"))
                    .when(productBusinessRules).checkIfProductExistsById(99);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> productService.update(request));
            assertEquals("Ürün bulunamadı. ID: 99", exception.getMessage());
            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Güncelleme sırasında aynı isim başka üründe varsa BusinessException fırlatmalı")
        void shouldThrowWhenUpdatingWithDuplicateName() {
            UpdateProductRequest request = new UpdateProductRequest(1, "Telefon", "Desc", 100.0, 5, "img.jpg", 1);

            doNothing().when(productBusinessRules).checkIfProductExistsById(1);
            doThrow(new BusinessException("Bu ürün adı zaten başka bir ürüne ait: Telefon"))
                    .when(productBusinessRules).checkIfProductNameAlreadyExistsForUpdate(1, "Telefon");

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> productService.update(request));
            assertEquals("Bu ürün adı zaten başka bir ürüne ait: Telefon", exception.getMessage());
            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Güncelleme sırasında negatif fiyat ile BusinessException fırlatmalı")
        void shouldThrowWhenUpdatingWithNegativePrice() {
            UpdateProductRequest request = new UpdateProductRequest(1, "Laptop", "Desc", -5.0, 5, "img.jpg", 1);

            doNothing().when(productBusinessRules).checkIfProductExistsById(1);
            doNothing().when(productBusinessRules).checkIfProductNameAlreadyExistsForUpdate(1, "Laptop");
            doThrow(new BusinessException("Birim fiyat 0'dan küçük olamaz."))
                    .when(productBusinessRules).checkIfUnitPriceValid(-5.0);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> productService.update(request));
            assertEquals("Birim fiyat 0'dan küçük olamaz.", exception.getMessage());
            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Güncelleme sırasında mevcut olmayan kategori ile RuntimeException fırlatmalı")
        void shouldThrowWhenUpdatingWithNonExistentCategory() {
            UpdateProductRequest request = new UpdateProductRequest(1, "Laptop", "Desc", 100.0, 5, "img.jpg", 99);

            doNothing().when(productBusinessRules).checkIfProductExistsById(1);
            doNothing().when(productBusinessRules).checkIfProductNameAlreadyExistsForUpdate(1, "Laptop");
            doNothing().when(productBusinessRules).checkIfUnitPriceValid(100.0);
            when(productRepository.findById(1)).thenReturn(Optional.of(product));
            when(categoryRepository.findById(99)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> productService.update(request));
            assertEquals("Kategori bulunamadı. ID: 99", exception.getMessage());
            verify(productRepository, never()).save(any(Product.class));
        }
    }

    // ==================== delete ====================

    @Nested
    @DisplayName("delete testleri")
    class Delete {

        @Test
        @DisplayName("Mevcut ürün silinmeli")
        void shouldDeleteProductSuccessfully() {
            doNothing().when(productBusinessRules).checkIfProductExistsById(1);
            when(productRepository.findById(1)).thenReturn(Optional.of(product));
            doNothing().when(productRepository).delete(product);

            DeletedProductResponse result = productService.delete(1);

            assertNotNull(result);
            assertEquals(1, result.getId());
            assertEquals("Laptop", result.getName());
            verify(productBusinessRules, times(1)).checkIfProductExistsById(1);
            verify(productRepository, times(1)).findById(1);
            verify(productRepository, times(1)).delete(product);
        }

        @Test
        @DisplayName("Mevcut olmayan ürün silinirken BusinessException fırlatmalı")
        void shouldThrowWhenDeletingNonExistentProduct() {
            doThrow(new BusinessException("Ürün bulunamadı. ID: 99"))
                    .when(productBusinessRules).checkIfProductExistsById(99);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> productService.delete(99));
            assertEquals("Ürün bulunamadı. ID: 99", exception.getMessage());
            verify(productRepository, never()).findById(anyInt());
            verify(productRepository, never()).delete(any(Product.class));
        }
    }
}
