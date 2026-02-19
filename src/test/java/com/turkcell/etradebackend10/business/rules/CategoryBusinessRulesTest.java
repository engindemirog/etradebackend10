package com.turkcell.etradebackend10.business.rules;

import com.turkcell.etradebackend10.business.exceptions.BusinessException;
import com.turkcell.etradebackend10.dataAccess.abstracts.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryBusinessRulesTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryBusinessRules categoryBusinessRules;

    @Nested
    @DisplayName("checkIfCategoryExistsById testleri")
    class CheckIfCategoryExistsById {

        @Test
        @DisplayName("Kategori mevcut olduğunda exception fırlatmamalı")
        void shouldNotThrowWhenCategoryExists() {
            // Arrange
            when(categoryRepository.existsById(1)).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> categoryBusinessRules.checkIfCategoryExistsById(1));
            verify(categoryRepository, times(1)).existsById(1);
        }

        @Test
        @DisplayName("Kategori mevcut olmadığında BusinessException fırlatmalı")
        void shouldThrowWhenCategoryDoesNotExist() {
            // Arrange
            when(categoryRepository.existsById(99)).thenReturn(false);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> categoryBusinessRules.checkIfCategoryExistsById(99));
            assertEquals("Kategori bulunamadı. ID: 99", exception.getMessage());
            verify(categoryRepository, times(1)).existsById(99);
        }
    }

    @Nested
    @DisplayName("checkIfCategoryNameAlreadyExists testleri")
    class CheckIfCategoryNameAlreadyExists {

        @Test
        @DisplayName("Kategori adı mevcut olmadığında exception fırlatmamalı")
        void shouldNotThrowWhenNameDoesNotExist() {
            // Arrange
            when(categoryRepository.existsByName("Elektronik")).thenReturn(false);

            // Act & Assert
            assertDoesNotThrow(() -> categoryBusinessRules.checkIfCategoryNameAlreadyExists("Elektronik"));
            verify(categoryRepository, times(1)).existsByName("Elektronik");
        }

        @Test
        @DisplayName("Kategori adı mevcut olduğunda BusinessException fırlatmalı")
        void shouldThrowWhenNameAlreadyExists() {
            // Arrange
            when(categoryRepository.existsByName("Elektronik")).thenReturn(true);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> categoryBusinessRules.checkIfCategoryNameAlreadyExists("Elektronik"));
            assertEquals("Bu kategori adı zaten mevcut: Elektronik", exception.getMessage());
            verify(categoryRepository, times(1)).existsByName("Elektronik");
        }
    }

    @Nested
    @DisplayName("checkIfCategoryNameAlreadyExistsForUpdate testleri")
    class CheckIfCategoryNameAlreadyExistsForUpdate {

        @Test
        @DisplayName("Güncelleme sırasında isim başka kategoride yoksa exception fırlatmamalı")
        void shouldNotThrowWhenNameNotUsedByAnotherCategory() {
            // Arrange
            when(categoryRepository.existsByNameAndIdNot("Elektronik", 1)).thenReturn(false);

            // Act & Assert
            assertDoesNotThrow(() -> categoryBusinessRules.checkIfCategoryNameAlreadyExistsForUpdate(1, "Elektronik"));
            verify(categoryRepository, times(1)).existsByNameAndIdNot("Elektronik", 1);
        }

        @Test
        @DisplayName("Güncelleme sırasında isim başka kategoride varsa BusinessException fırlatmalı")
        void shouldThrowWhenNameUsedByAnotherCategory() {
            // Arrange
            when(categoryRepository.existsByNameAndIdNot("Elektronik", 1)).thenReturn(true);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> categoryBusinessRules.checkIfCategoryNameAlreadyExistsForUpdate(1, "Elektronik"));
            assertEquals("Bu kategori adı zaten başka bir kategoriye ait: Elektronik", exception.getMessage());
            verify(categoryRepository, times(1)).existsByNameAndIdNot("Elektronik", 1);
        }
    }
}
