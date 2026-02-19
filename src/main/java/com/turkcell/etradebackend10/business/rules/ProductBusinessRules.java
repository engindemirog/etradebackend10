package com.turkcell.etradebackend10.business.rules;

import com.turkcell.etradebackend10.dataAccess.abstracts.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductBusinessRules {

    private final ProductRepository productRepository;

    public ProductBusinessRules(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void checkIfProductExistsById(int id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Ürün bulunamadı. ID: " + id);
        }
    }

    public void checkIfProductNameAlreadyExists(String name) {
        if (productRepository.existsByName(name)) {
            throw new RuntimeException("Bu ürün adı zaten mevcut: " + name);
        }
    }

    public void checkIfProductNameAlreadyExistsForUpdate(int id, String name) {
        if (productRepository.existsByNameAndIdNot(name, id)) {
            throw new RuntimeException("Bu ürün adı zaten başka bir ürüne ait: " + name);
        }
    }

    public void checkIfUnitPriceValid(double unitPrice) {
        if (unitPrice < 0) {
            throw new RuntimeException("Birim fiyat 0'dan küçük olamaz.");
        }
    }
}
