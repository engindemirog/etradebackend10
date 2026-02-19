package com.turkcell.etradebackend10.business.concretes;

import com.turkcell.etradebackend10.business.exceptions.BusinessException;
import com.turkcell.etradebackend10.business.rules.CategoryBusinessRules;
import com.turkcell.etradebackend10.dataAccess.abstracts.CategoryRepository;
import com.turkcell.etradebackend10.entities.concretes.Category;
import com.turkcell.etradebackend10.entities.dtos.requests.category.CreateCategoryRequest;
import com.turkcell.etradebackend10.entities.dtos.requests.category.UpdateCategoryRequest;
import com.turkcell.etradebackend10.entities.dtos.responses.category.*;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryBusinessRules categoryBusinessRules;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        category = new Category("Elektronik", "Elektronik ürünler");
        category.setId(1);
        category.setCreatedDate(now);
        category.setActive(true);
    }

    // ==================== getAll ====================

    @Nested
    @DisplayName("getAll testleri")
    class GetAll {

        @Test
        @DisplayName("Kategoriler varsa listeyi döndürmeli")
        void shouldReturnAllCategories() {
            Category category2 = new Category("Giyim", "Giyim ürünleri");
            category2.setId(2);

            when(categoryRepository.findAll()).thenReturn(List.of(category, category2));

            List<GetAllCategoriesResponse> result = categoryService.getAll();

            assertEquals(2, result.size());
            assertEquals("Elektronik", result.get(0).getName());
            assertEquals(1, result.get(0).getId());
            assertEquals("Giyim", result.get(1).getName());
            assertEquals(2, result.get(1).getId());
            verify(categoryRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Kategori yoksa boş liste döndürmeli")
        void shouldReturnEmptyListWhenNoCategories() {
            when(categoryRepository.findAll()).thenReturn(Collections.emptyList());

            List<GetAllCategoriesResponse> result = categoryService.getAll();

            assertTrue(result.isEmpty());
            verify(categoryRepository, times(1)).findAll();
        }
    }

    // ==================== getById ====================

    @Nested
    @DisplayName("getById testleri")
    class GetById {

        @Test
        @DisplayName("Mevcut ID ile kategori döndürmeli")
        void shouldReturnCategoryWhenExists() {
            category.setUpdatedDate(now.plusHours(1));
            doNothing().when(categoryBusinessRules).checkIfCategoryExistsById(1);
            when(categoryRepository.findById(1)).thenReturn(Optional.of(category));

            GetByIdCategoryResponse result = categoryService.getById(1);

            assertEquals(1, result.getId());
            assertEquals("Elektronik", result.getName());
            assertEquals("Elektronik ürünler", result.getDescription());
            assertEquals(now, result.getCreatedDate());
            assertEquals(now.plusHours(1), result.getUpdatedDate());
            assertTrue(result.isActive());
            verify(categoryBusinessRules, times(1)).checkIfCategoryExistsById(1);
            verify(categoryRepository, times(1)).findById(1);
        }

        @Test
        @DisplayName("Mevcut olmayan ID ile BusinessException fırlatmalı")
        void shouldThrowWhenCategoryDoesNotExist() {
            doThrow(new BusinessException("Kategori bulunamadı. ID: 99"))
                    .when(categoryBusinessRules).checkIfCategoryExistsById(99);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> categoryService.getById(99));
            assertEquals("Kategori bulunamadı. ID: 99", exception.getMessage());
            verify(categoryBusinessRules, times(1)).checkIfCategoryExistsById(99);
            verify(categoryRepository, never()).findById(anyInt());
        }
    }

    // ==================== add ====================

    @Nested
    @DisplayName("add testleri")
    class Add {

        @Test
        @DisplayName("Geçerli istek ile kategori eklemeli ve response döndürmeli")
        void shouldAddCategorySuccessfully() {
            CreateCategoryRequest request = new CreateCategoryRequest("Elektronik", "Elektronik ürünler");

            doNothing().when(categoryBusinessRules).checkIfCategoryNameAlreadyExists("Elektronik");
            when(categoryRepository.save(any(Category.class))).thenReturn(category);

            CreatedCategoryResponse result = categoryService.add(request);

            assertNotNull(result);
            assertEquals(1, result.getId());
            assertEquals("Elektronik", result.getName());
            assertEquals("Elektronik ürünler", result.getDescription());
            assertEquals(now, result.getCreatedDate());
            verify(categoryBusinessRules, times(1)).checkIfCategoryNameAlreadyExists("Elektronik");
            verify(categoryRepository, times(1)).save(any(Category.class));
        }

        @Test
        @DisplayName("Zaten var olan kategori adı ile BusinessException fırlatmalı")
        void shouldThrowWhenCategoryNameAlreadyExists() {
            CreateCategoryRequest request = new CreateCategoryRequest("Elektronik", "Elektronik ürünler");

            doThrow(new BusinessException("Bu kategori adı zaten mevcut: Elektronik"))
                    .when(categoryBusinessRules).checkIfCategoryNameAlreadyExists("Elektronik");

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> categoryService.add(request));
            assertEquals("Bu kategori adı zaten mevcut: Elektronik", exception.getMessage());
            verify(categoryRepository, never()).save(any(Category.class));
        }
    }

    // ==================== update ====================

    @Nested
    @DisplayName("update testleri")
    class Update {

        @Test
        @DisplayName("Geçerli istek ile kategori güncellemeli")
        void shouldUpdateCategorySuccessfully() {
            UpdateCategoryRequest request = new UpdateCategoryRequest(1, "Elektronik Güncellendi", "Yeni açıklama");

            Category updatedCategory = new Category("Elektronik Güncellendi", "Yeni açıklama");
            updatedCategory.setId(1);
            updatedCategory.setUpdatedDate(now.plusHours(1));

            doNothing().when(categoryBusinessRules).checkIfCategoryExistsById(1);
            doNothing().when(categoryBusinessRules).checkIfCategoryNameAlreadyExistsForUpdate(1, "Elektronik Güncellendi");
            when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
            when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

            UpdatedCategoryResponse result = categoryService.update(request);

            assertNotNull(result);
            assertEquals(1, result.getId());
            assertEquals("Elektronik Güncellendi", result.getName());
            assertEquals("Yeni açıklama", result.getDescription());
            assertEquals(now.plusHours(1), result.getUpdatedDate());
            verify(categoryBusinessRules, times(1)).checkIfCategoryExistsById(1);
            verify(categoryBusinessRules, times(1)).checkIfCategoryNameAlreadyExistsForUpdate(1, "Elektronik Güncellendi");
            verify(categoryRepository, times(1)).findById(1);
            verify(categoryRepository, times(1)).save(any(Category.class));
        }

        @Test
        @DisplayName("Mevcut olmayan kategori güncellenirken BusinessException fırlatmalı")
        void shouldThrowWhenUpdatingNonExistentCategory() {
            UpdateCategoryRequest request = new UpdateCategoryRequest(99, "Test", "Test desc");

            doThrow(new BusinessException("Kategori bulunamadı. ID: 99"))
                    .when(categoryBusinessRules).checkIfCategoryExistsById(99);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> categoryService.update(request));
            assertEquals("Kategori bulunamadı. ID: 99", exception.getMessage());
            verify(categoryRepository, never()).save(any(Category.class));
        }

        @Test
        @DisplayName("Güncelleme sırasında aynı isim başka kategoride varsa BusinessException fırlatmalı")
        void shouldThrowWhenUpdatingWithDuplicateName() {
            UpdateCategoryRequest request = new UpdateCategoryRequest(1, "Giyim", "Açıklama");

            doNothing().when(categoryBusinessRules).checkIfCategoryExistsById(1);
            doThrow(new BusinessException("Bu kategori adı zaten başka bir kategoriye ait: Giyim"))
                    .when(categoryBusinessRules).checkIfCategoryNameAlreadyExistsForUpdate(1, "Giyim");

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> categoryService.update(request));
            assertEquals("Bu kategori adı zaten başka bir kategoriye ait: Giyim", exception.getMessage());
            verify(categoryRepository, never()).save(any(Category.class));
        }
    }

    // ==================== delete ====================

    @Nested
    @DisplayName("delete testleri")
    class Delete {

        @Test
        @DisplayName("Mevcut kategori silinmeli")
        void shouldDeleteCategorySuccessfully() {
            doNothing().when(categoryBusinessRules).checkIfCategoryExistsById(1);
            when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
            doNothing().when(categoryRepository).delete(category);

            DeletedCategoryResponse result = categoryService.delete(1);

            assertNotNull(result);
            assertEquals(1, result.getId());
            assertEquals("Elektronik", result.getName());
            verify(categoryBusinessRules, times(1)).checkIfCategoryExistsById(1);
            verify(categoryRepository, times(1)).findById(1);
            verify(categoryRepository, times(1)).delete(category);
        }

        @Test
        @DisplayName("Mevcut olmayan kategori silinirken BusinessException fırlatmalı")
        void shouldThrowWhenDeletingNonExistentCategory() {
            doThrow(new BusinessException("Kategori bulunamadı. ID: 99"))
                    .when(categoryBusinessRules).checkIfCategoryExistsById(99);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> categoryService.delete(99));
            assertEquals("Kategori bulunamadı. ID: 99", exception.getMessage());
            verify(categoryRepository, never()).findById(anyInt());
            verify(categoryRepository, never()).delete(any(Category.class));
        }
    }
}
