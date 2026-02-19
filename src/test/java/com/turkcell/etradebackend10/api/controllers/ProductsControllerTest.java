package com.turkcell.etradebackend10.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.turkcell.etradebackend10.business.abstracts.ProductService;
import com.turkcell.etradebackend10.business.exceptions.BusinessException;
import com.turkcell.etradebackend10.business.exceptions.GlobalExceptionHandler;
import com.turkcell.etradebackend10.entities.dtos.requests.product.CreateProductRequest;
import com.turkcell.etradebackend10.entities.dtos.requests.product.UpdateProductRequest;
import com.turkcell.etradebackend10.entities.dtos.responses.product.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductsController.class)
@Import(GlobalExceptionHandler.class)
class ProductsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private final LocalDateTime now = LocalDateTime.of(2026, 2, 19, 12, 0, 0);

    // ==================== GET /api/products ====================

    @Nested
    @DisplayName("GET /api/products")
    class GetAll {

        @Test
        @DisplayName("Ürünler varsa 200 ve liste döndürmeli")
        void shouldReturnAllProducts() throws Exception {
            List<GetAllProductsResponse> responses = List.of(
                    new GetAllProductsResponse(1, "Laptop", 15000.0, 10, "laptop.jpg", 1, "Elektronik"),
                    new GetAllProductsResponse(2, "Telefon", 10000.0, 20, "telefon.jpg", 1, "Elektronik")
            );
            when(productService.getAll()).thenReturn(responses);

            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id", is(1)))
                    .andExpect(jsonPath("$[0].name", is("Laptop")))
                    .andExpect(jsonPath("$[0].unitPrice", is(15000.0)))
                    .andExpect(jsonPath("$[0].unitsInStock", is(10)))
                    .andExpect(jsonPath("$[0].imageUrl", is("laptop.jpg")))
                    .andExpect(jsonPath("$[0].categoryId", is(1)))
                    .andExpect(jsonPath("$[0].categoryName", is("Elektronik")))
                    .andExpect(jsonPath("$[1].id", is(2)))
                    .andExpect(jsonPath("$[1].name", is("Telefon")));

            verify(productService, times(1)).getAll();
        }

        @Test
        @DisplayName("Ürün yoksa 200 ve boş liste döndürmeli")
        void shouldReturnEmptyList() throws Exception {
            when(productService.getAll()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(productService, times(1)).getAll();
        }
    }

    // ==================== GET /api/products/{id} ====================

    @Nested
    @DisplayName("GET /api/products/{id}")
    class GetById {

        @Test
        @DisplayName("Mevcut ID ile 200 ve ürün döndürmeli")
        void shouldReturnProductById() throws Exception {
            GetByIdProductResponse response = new GetByIdProductResponse(
                    1, "Laptop", "İyi bir laptop", 15000.0, 10,
                    "laptop.jpg", 1, "Elektronik", now, null, true
            );
            when(productService.getById(1)).thenReturn(response);

            mockMvc.perform(get("/api/products/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Laptop")))
                    .andExpect(jsonPath("$.description", is("İyi bir laptop")))
                    .andExpect(jsonPath("$.unitPrice", is(15000.0)))
                    .andExpect(jsonPath("$.unitsInStock", is(10)))
                    .andExpect(jsonPath("$.imageUrl", is("laptop.jpg")))
                    .andExpect(jsonPath("$.categoryId", is(1)))
                    .andExpect(jsonPath("$.categoryName", is("Elektronik")))
                    .andExpect(jsonPath("$.active", is(true)));

            verify(productService, times(1)).getById(1);
        }

        @Test
        @DisplayName("Mevcut olmayan ID ile 400 ve hata mesajı döndürmeli")
        void shouldReturn400WhenProductNotFound() throws Exception {
            when(productService.getById(99))
                    .thenThrow(new BusinessException("Ürün bulunamadı. ID: 99"));

            mockMvc.perform(get("/api/products/99"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.message", is("Ürün bulunamadı. ID: 99")));

            verify(productService, times(1)).getById(99);
        }
    }

    // ==================== POST /api/products ====================

    @Nested
    @DisplayName("POST /api/products")
    class Add {

        @Test
        @DisplayName("Geçerli istek ile 201 ve oluşturulan ürün döndürmeli")
        void shouldCreateProduct() throws Exception {
            CreateProductRequest request = new CreateProductRequest(
                    "Laptop", "İyi bir laptop", 15000.0, 10, "laptop.jpg", 1
            );
            CreatedProductResponse response = new CreatedProductResponse(
                    1, "Laptop", "İyi bir laptop", 15000.0, 10, "laptop.jpg", 1, "Elektronik", now
            );

            when(productService.add(any(CreateProductRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Laptop")))
                    .andExpect(jsonPath("$.description", is("İyi bir laptop")))
                    .andExpect(jsonPath("$.unitPrice", is(15000.0)))
                    .andExpect(jsonPath("$.unitsInStock", is(10)))
                    .andExpect(jsonPath("$.categoryId", is(1)))
                    .andExpect(jsonPath("$.categoryName", is("Elektronik")));

            verify(productService, times(1)).add(any(CreateProductRequest.class));
        }

        @Test
        @DisplayName("Boş ürün adı ile 400 ve validasyon hatası döndürmeli")
        void shouldReturn400WhenNameIsBlank() throws Exception {
            CreateProductRequest request = new CreateProductRequest(
                    "", "Açıklama", 100.0, 5, "img.jpg", 1
            );

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", is("Validasyon hatası")))
                    .andExpect(jsonPath("$.errors.name").exists());

            verify(productService, never()).add(any(CreateProductRequest.class));
        }

        @Test
        @DisplayName("Çok kısa ürün adı ile 400 döndürmeli")
        void shouldReturn400WhenNameTooShort() throws Exception {
            CreateProductRequest request = new CreateProductRequest(
                    "A", "Açıklama", 100.0, 5, "img.jpg", 1
            );

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name").exists());

            verify(productService, never()).add(any(CreateProductRequest.class));
        }

        @Test
        @DisplayName("Mevcut ürün adı ile 400 ve iş kuralı hatası döndürmeli")
        void shouldReturn400WhenProductNameExists() throws Exception {
            CreateProductRequest request = new CreateProductRequest(
                    "Laptop", "Açıklama", 100.0, 5, "img.jpg", 1
            );

            when(productService.add(any(CreateProductRequest.class)))
                    .thenThrow(new BusinessException("Bu ürün adı zaten mevcut: Laptop"));

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", is("Bu ürün adı zaten mevcut: Laptop")));

            verify(productService, times(1)).add(any(CreateProductRequest.class));
        }

        @Test
        @DisplayName("Negatif fiyat ile 400 ve iş kuralı hatası döndürmeli")
        void shouldReturn400WhenPriceIsNegative() throws Exception {
            CreateProductRequest request = new CreateProductRequest(
                    "Yeni Ürün", "Açıklama", -10.0, 5, "img.jpg", 1
            );

            when(productService.add(any(CreateProductRequest.class)))
                    .thenThrow(new BusinessException("Birim fiyat 0'dan küçük olamaz."));

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.message").isNotEmpty());
        }
    }

    // ==================== PUT /api/products ====================

    @Nested
    @DisplayName("PUT /api/products")
    class Update {

        @Test
        @DisplayName("Geçerli istek ile 200 ve güncellenen ürün döndürmeli")
        void shouldUpdateProduct() throws Exception {
            UpdateProductRequest request = new UpdateProductRequest(
                    1, "Laptop Pro", "Güncellenmiş laptop", 20000.0, 15, "laptop2.jpg", 1
            );
            UpdatedProductResponse response = new UpdatedProductResponse(
                    1, "Laptop Pro", "Güncellenmiş laptop", 20000.0, 15, "laptop2.jpg", 1, "Elektronik", now
            );

            when(productService.update(any(UpdateProductRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Laptop Pro")))
                    .andExpect(jsonPath("$.description", is("Güncellenmiş laptop")))
                    .andExpect(jsonPath("$.unitPrice", is(20000.0)))
                    .andExpect(jsonPath("$.unitsInStock", is(15)))
                    .andExpect(jsonPath("$.categoryId", is(1)))
                    .andExpect(jsonPath("$.categoryName", is("Elektronik")));

            verify(productService, times(1)).update(any(UpdateProductRequest.class));
        }

        @Test
        @DisplayName("Boş isim ile güncelleme 400 dönmeli")
        void shouldReturn400WhenUpdateNameIsBlank() throws Exception {
            UpdateProductRequest request = new UpdateProductRequest(
                    1, "", "Açıklama", 100.0, 5, "img.jpg", 1
            );

            mockMvc.perform(put("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name").exists());

            verify(productService, never()).update(any(UpdateProductRequest.class));
        }

        @Test
        @DisplayName("Mevcut olmayan ürün güncellenirken 400 dönmeli")
        void shouldReturn400WhenUpdatingNonExistentProduct() throws Exception {
            UpdateProductRequest request = new UpdateProductRequest(
                    99, "Test", "Açıklama", 100.0, 5, "img.jpg", 1
            );

            when(productService.update(any(UpdateProductRequest.class)))
                    .thenThrow(new BusinessException("Ürün bulunamadı. ID: 99"));

            mockMvc.perform(put("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", is("Ürün bulunamadı. ID: 99")));
        }

        @Test
        @DisplayName("Güncelleme sırasında isim çakışması ile 400 dönmeli")
        void shouldReturn400WhenUpdatingWithDuplicateName() throws Exception {
            UpdateProductRequest request = new UpdateProductRequest(
                    1, "Telefon", "Açıklama", 100.0, 5, "img.jpg", 1
            );

            when(productService.update(any(UpdateProductRequest.class)))
                    .thenThrow(new BusinessException("Bu ürün adı zaten başka bir ürüne ait: Telefon"));

            mockMvc.perform(put("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", is("Bu ürün adı zaten başka bir ürüne ait: Telefon")));
        }
    }

    // ==================== DELETE /api/products/{id} ====================

    @Nested
    @DisplayName("DELETE /api/products/{id}")
    class Delete {

        @Test
        @DisplayName("Mevcut ID ile silme 200 döndürmeli")
        void shouldDeleteProduct() throws Exception {
            DeletedProductResponse response = new DeletedProductResponse(1, "Laptop");
            when(productService.delete(1)).thenReturn(response);

            mockMvc.perform(delete("/api/products/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Laptop")));

            verify(productService, times(1)).delete(1);
        }

        @Test
        @DisplayName("Mevcut olmayan ID ile silme 400 döndürmeli")
        void shouldReturn400WhenDeletingNonExistentProduct() throws Exception {
            when(productService.delete(99))
                    .thenThrow(new BusinessException("Ürün bulunamadı. ID: 99"));

            mockMvc.perform(delete("/api/products/99"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", is("Ürün bulunamadı. ID: 99")));

            verify(productService, times(1)).delete(99);
        }
    }
}
