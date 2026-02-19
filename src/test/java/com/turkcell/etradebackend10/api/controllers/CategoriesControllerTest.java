package com.turkcell.etradebackend10.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.turkcell.etradebackend10.business.abstracts.CategoryService;
import com.turkcell.etradebackend10.business.exceptions.BusinessException;
import com.turkcell.etradebackend10.business.exceptions.GlobalExceptionHandler;
import com.turkcell.etradebackend10.entities.dtos.requests.category.CreateCategoryRequest;
import com.turkcell.etradebackend10.entities.dtos.requests.category.UpdateCategoryRequest;
import com.turkcell.etradebackend10.entities.dtos.responses.category.*;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoriesController.class)
@Import(GlobalExceptionHandler.class)
class CategoriesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private final LocalDateTime now = LocalDateTime.of(2026, 2, 19, 12, 0, 0);

    // ==================== GET /api/categories ====================

    @Nested
    @DisplayName("GET /api/categories")
    class GetAll {

        @Test
        @DisplayName("Kategoriler varsa 200 ve liste döndürmeli")
        void shouldReturnAllCategories() throws Exception {
            List<GetAllCategoriesResponse> responses = List.of(
                    new GetAllCategoriesResponse(1, "Elektronik"),
                    new GetAllCategoriesResponse(2, "Giyim")
            );
            when(categoryService.getAll()).thenReturn(responses);

            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id", is(1)))
                    .andExpect(jsonPath("$[0].name", is("Elektronik")))
                    .andExpect(jsonPath("$[1].id", is(2)))
                    .andExpect(jsonPath("$[1].name", is("Giyim")));

            verify(categoryService, times(1)).getAll();
        }

        @Test
        @DisplayName("Kategori yoksa 200 ve boş liste döndürmeli")
        void shouldReturnEmptyList() throws Exception {
            when(categoryService.getAll()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(categoryService, times(1)).getAll();
        }
    }

    // ==================== GET /api/categories/{id} ====================

    @Nested
    @DisplayName("GET /api/categories/{id}")
    class GetById {

        @Test
        @DisplayName("Mevcut ID ile 200 ve kategori döndürmeli")
        void shouldReturnCategoryById() throws Exception {
            GetByIdCategoryResponse response = new GetByIdCategoryResponse(
                    1, "Elektronik", "Elektronik ürünler", now, null, true
            );
            when(categoryService.getById(1)).thenReturn(response);

            mockMvc.perform(get("/api/categories/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Elektronik")))
                    .andExpect(jsonPath("$.description", is("Elektronik ürünler")))
                    .andExpect(jsonPath("$.active", is(true)));

            verify(categoryService, times(1)).getById(1);
        }

        @Test
        @DisplayName("Mevcut olmayan ID ile 400 ve hata mesajı döndürmeli")
        void shouldReturn400WhenCategoryNotFound() throws Exception {
            when(categoryService.getById(99))
                    .thenThrow(new BusinessException("Kategori bulunamadı. ID: 99"));

            mockMvc.perform(get("/api/categories/99"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.message", is("Kategori bulunamadı. ID: 99")));

            verify(categoryService, times(1)).getById(99);
        }
    }

    // ==================== POST /api/categories ====================

    @Nested
    @DisplayName("POST /api/categories")
    class Add {

        @Test
        @DisplayName("Geçerli istek ile 201 ve oluşturulan kategori döndürmeli")
        void shouldCreateCategory() throws Exception {
            CreateCategoryRequest request = new CreateCategoryRequest("Elektronik", "Elektronik ürünler");
            CreatedCategoryResponse response = new CreatedCategoryResponse(1, "Elektronik", "Elektronik ürünler", now);

            when(categoryService.add(any(CreateCategoryRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Elektronik")))
                    .andExpect(jsonPath("$.description", is("Elektronik ürünler")));

            verify(categoryService, times(1)).add(any(CreateCategoryRequest.class));
        }

        @Test
        @DisplayName("Boş isim ile 400 ve validasyon hatası döndürmeli")
        void shouldReturn400WhenNameIsBlank() throws Exception {
            CreateCategoryRequest request = new CreateCategoryRequest("", "Açıklama");

            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", is("Validasyon hatası")))
                    .andExpect(jsonPath("$.errors.name").exists());

            verify(categoryService, never()).add(any(CreateCategoryRequest.class));
        }

        @Test
        @DisplayName("Çok kısa isim ile 400 ve validasyon hatası döndürmeli")
        void shouldReturn400WhenNameTooShort() throws Exception {
            CreateCategoryRequest request = new CreateCategoryRequest("A", "Açıklama");

            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name").exists());

            verify(categoryService, never()).add(any(CreateCategoryRequest.class));
        }

        @Test
        @DisplayName("Mevcut kategori adı ile 400 ve iş kuralı hatası döndürmeli")
        void shouldReturn400WhenCategoryNameExists() throws Exception {
            CreateCategoryRequest request = new CreateCategoryRequest("Elektronik", "Açıklama");

            when(categoryService.add(any(CreateCategoryRequest.class)))
                    .thenThrow(new BusinessException("Bu kategori adı zaten mevcut: Elektronik"));

            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", is("Bu kategori adı zaten mevcut: Elektronik")));

            verify(categoryService, times(1)).add(any(CreateCategoryRequest.class));
        }

        @Test
        @DisplayName("Null body ile 400 döndürmeli")
        void shouldReturn400WhenBodyIsNull() throws Exception {
            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== PUT /api/categories ====================

    @Nested
    @DisplayName("PUT /api/categories")
    class Update {

        @Test
        @DisplayName("Geçerli istek ile 200 ve güncellenen kategori döndürmeli")
        void shouldUpdateCategory() throws Exception {
            UpdateCategoryRequest request = new UpdateCategoryRequest(1, "Elektronik v2", "Güncel açıklama");
            UpdatedCategoryResponse response = new UpdatedCategoryResponse(1, "Elektronik v2", "Güncel açıklama", now);

            when(categoryService.update(any(UpdateCategoryRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Elektronik v2")))
                    .andExpect(jsonPath("$.description", is("Güncel açıklama")));

            verify(categoryService, times(1)).update(any(UpdateCategoryRequest.class));
        }

        @Test
        @DisplayName("Boş isim ile güncelleme 400 dönmeli")
        void shouldReturn400WhenUpdateNameIsBlank() throws Exception {
            UpdateCategoryRequest request = new UpdateCategoryRequest(1, "", "Açıklama");

            mockMvc.perform(put("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name").exists());

            verify(categoryService, never()).update(any(UpdateCategoryRequest.class));
        }

        @Test
        @DisplayName("Mevcut olmayan kategori güncellenirken 400 dönmeli")
        void shouldReturn400WhenUpdatingNonExistentCategory() throws Exception {
            UpdateCategoryRequest request = new UpdateCategoryRequest(99, "Test", "Açıklama");

            when(categoryService.update(any(UpdateCategoryRequest.class)))
                    .thenThrow(new BusinessException("Kategori bulunamadı. ID: 99"));

            mockMvc.perform(put("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", is("Kategori bulunamadı. ID: 99")));
        }
    }

    // ==================== DELETE /api/categories/{id} ====================

    @Nested
    @DisplayName("DELETE /api/categories/{id}")
    class Delete {

        @Test
        @DisplayName("Mevcut ID ile silme 200 döndürmeli")
        void shouldDeleteCategory() throws Exception {
            DeletedCategoryResponse response = new DeletedCategoryResponse(1, "Elektronik");
            when(categoryService.delete(1)).thenReturn(response);

            mockMvc.perform(delete("/api/categories/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Elektronik")));

            verify(categoryService, times(1)).delete(1);
        }

        @Test
        @DisplayName("Mevcut olmayan ID ile silme 400 döndürmeli")
        void shouldReturn400WhenDeletingNonExistentCategory() throws Exception {
            when(categoryService.delete(99))
                    .thenThrow(new BusinessException("Kategori bulunamadı. ID: 99"));

            mockMvc.perform(delete("/api/categories/99"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", is("Kategori bulunamadı. ID: 99")));

            verify(categoryService, times(1)).delete(99);
        }
    }
}
