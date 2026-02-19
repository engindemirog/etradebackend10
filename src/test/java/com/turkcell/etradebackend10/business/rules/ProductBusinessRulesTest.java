package com.turkcell.etradebackend10.business.rules;

import com.turkcell.etradebackend10.business.exceptions.BusinessException;
import com.turkcell.etradebackend10.dataAccess.abstracts.ProductRepository;
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
class ProductBusinessRulesTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductBusinessRules productBusinessRules;

    @Nested
    @DisplayName("checkIfProductExistsById testleri")
    class CheckIfProductExistsById {

        @Test
        @DisplayName("Ürün mevcut olduğunda exception fırlatmamalı")
        void shouldNotThrowWhenProductExists() {
            when(productRepository.existsById(1)).thenReturn(true);

            assertDoesNotThrow(() -> productBusinessRules.checkIfProductExistsById(1));
            verify(productRepository, times(1)).existsById(1);
        }

        @Test
        @DisplayName("Ürün mevcut olmadığında BusinessException fırlatmalı")
        void shouldThrowWhenProductDoesNotExist() {
            when(productRepository.existsById(99)).thenReturn(false);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> productBusinessRules.checkIfProductExistsById(99));
            assertEquals("Ürün bulunamadı. ID: 99", exception.getMessage());
            verify(productRepository, times(1)).existsById(99);
        }
    }

    @Nested
    @DisplayName("checkIfProductNameAlreadyExists testleri")
    class CheckIfProductNameAlreadyExists {

        @Test
        @DisplayName("Ürün adı mevcut olmadığında exception fırlatmamalı")
        void shouldNotThrowWhenNameDoesNotExist() {
            when(productRepository.existsByName("Laptop")).thenReturn(false);

            assertDoesNotThrow(() -> productBusinessRules.checkIfProductNameAlreadyExists("Laptop"));
            verify(productRepository, times(1)).existsByName("Laptop");
        }

        @Test
        @DisplayName("Ürün adı mevcut olduğunda BusinessException fırlatmalı")
        void shouldThrowWhenNameAlreadyExists() {
            when(productRepository.existsByName("Laptop")).thenReturn(true);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> productBusinessRules.checkIfProductNameAlreadyExists("Laptop"));
            assertEquals("Bu ürün adı zaten mevcut: Laptop", exception.getMessage());
            verify(productRepository, times(1)).existsByName("Laptop");
        }
    }

    @Nested
    @DisplayName("checkIfProductNameAlreadyExistsForUpdate testleri")
    class CheckIfProductNameAlreadyExistsForUpdate {

        @Test
        @DisplayName("Güncelleme sırasında isim başka üründe yoksa exception fırlatmamalı")
        void shouldNotThrowWhenNameNotUsedByAnotherProduct() {
            when(productRepository.existsByNameAndIdNot("Laptop", 1)).thenReturn(false);

            assertDoesNotThrow(() -> productBusinessRules.checkIfProductNameAlreadyExistsForUpdate(1, "Laptop"));
            verify(productRepository, times(1)).existsByNameAndIdNot("Laptop", 1);
        }

        @Test
        @DisplayName("Güncelleme sırasında isim başka üründe varsa BusinessException fırlatmalı")
        void shouldThrowWhenNameUsedByAnotherProduct() {
            when(productRepository.existsByNameAndIdNot("Laptop", 1)).thenReturn(true);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> productBusinessRules.checkIfProductNameAlreadyExistsForUpdate(1, "Laptop"));
            assertEquals("Bu ürün adı zaten başka bir ürüne ait: Laptop", exception.getMessage());
            verify(productRepository, times(1)).existsByNameAndIdNot("Laptop", 1);
        }
    }

    @Nested
    @DisplayName("checkIfUnitPriceValid testleri")
    class CheckIfUnitPriceValid {

        @Test
        @DisplayName("Geçerli fiyat olduğunda exception fırlatmamalı")
        void shouldNotThrowWhenPriceIsValid() {
            assertDoesNotThrow(() -> productBusinessRules.checkIfUnitPriceValid(100.0));
        }

        @Test
        @DisplayName("Sıfır fiyat olduğunda exception fırlatmamalı")
        void shouldNotThrowWhenPriceIsZero() {
            assertDoesNotThrow(() -> productBusinessRules.checkIfUnitPriceValid(0.0));
        }

        @Test
        @DisplayName("Negatif fiyat olduğunda BusinessException fırlatmalı")
        void shouldThrowWhenPriceIsNegative() {
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> productBusinessRules.checkIfUnitPriceValid(-1.0));
            assertEquals("Birim fiyat 0'dan küçük olamaz.", exception.getMessage());
        }

        @Test
        @DisplayName("Çok küçük negatif fiyat olduğunda BusinessException fırlatmalı")
        void shouldThrowWhenPriceIsVeryNegative() {
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> productBusinessRules.checkIfUnitPriceValid(-0.01));
            assertEquals("Birim fiyat 0'dan küçük olamaz.", exception.getMessage());
        }
    }
}
